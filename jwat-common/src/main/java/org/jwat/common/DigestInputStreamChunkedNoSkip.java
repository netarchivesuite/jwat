/**
 * Java Web Archive Toolkit - Software to read and validate ARC, WARC
 * and GZip files. (http://jwat.org/)
 * Copyright 2011-2012 Netarkivet.dk (http://netarkivet.dk/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * Alternative to the standard <code>DigestInputStream</code> as this implementation supports chunked
 * transter-encoding (HTTP/1.1).
 * Unlike the standard <code>DigestInputStream</code> this can also digest when using the skip method.
 *
 * @author nicl
 */
public class DigestInputStreamChunkedNoSkip extends InputStream {

    /** Buffer size to use when read skipping. */
    public static final int BUFFER_SIZE = 1024 * 128;

    protected InputStream stream;

    protected MessageDigest chunkedDigest;

    protected MessageDigest payloadDigest;

    protected OutputStream out;

    /** Read buffer used by the skip method. */
    protected byte[] bytes = new byte[BUFFER_SIZE];

    protected int bufferSize = bytes.length;

    protected int lastRead;

    protected int position;

    protected int limit;

    public DigestInputStreamChunkedNoSkip(InputStream stream, MessageDigest chunkedDigest, MessageDigest payloadDigest, OutputStream out) {
        this.stream = stream;
        this.chunkedDigest = chunkedDigest;
        this.payloadDigest = payloadDigest;
        this.out = out;
        state = 0;
        chunkLen = 0;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int available() throws IOException {
        return limit - position;
    }

    @Override
    public void close() throws IOException {
        // TODO Not called for some reason.
        //System.out.println("close()");
    }

    public void compact() {
        // Debug
        //System.out.println(">compact() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
        int idx;
        if (position == limit) {
            position = 0;
            limit = 0;
        }
        else if (position > 0) {
            idx = 0;
            while (position < limit) {
                bytes[idx++] = bytes[position++];
            }
            position = 0;
            limit = idx;
        }
        // Debug
        //System.out.println("<compact() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
    }

    public void fillBuffer() throws IOException {
        // Debug
        //System.out.println(">fillBuffer() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
        if (lastRead != -1) {
            lastRead = 0;
            if ((position == limit || limit == bufferSize) && position > 0) {
                compact();
            }
            while (lastRead == 0 && limit != bufferSize) {
                lastRead = stream.read(bytes, limit, bufferSize - limit);
                // Debug
                //System.out.println("limit: " + limit + " len: " + (bufferSize - limit) + " read: " + lastRead);
                if (lastRead > 0) {
                    scanChunk();
                    limit += lastRead;
                }
            }
        }
        // Debug
        //System.out.println("<fillBuffer() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
    }

    public static final int S_LENGTH = 0;
    public static final int S_LENGTH_CR = 1;
    public static final int S_LENGTH_LF = 2;
    public static final int S_CHUNK_CR = 3;
    public static final int S_CHUNK_LF = 4;
    public static final int S_END_CR = 5;
    public static final int S_END_LF = 6;
    public static final int S_DONE = 7;
    public static final int S_ERROR = 10;

    protected int state;

    protected int chunkLen;

    protected int overflow;

    public int getState() {
        return state;
    }

    public int getOverflow() {
        return overflow;
    }

    public void scanChunk() throws IOException {
        int idx;
        int readLimit;
        int digestable;
        int c;
        if (lastRead > 0) {
            idx = limit;
            readLimit = limit + lastRead;
            if (payloadDigest != null) {
                payloadDigest.update(bytes, idx, readLimit - idx);
            }
            while (idx < readLimit) {
                // Debug
                //System.out.println("state: " + state);
                c = bytes[idx] & 255;
                switch (state) {
                case S_LENGTH:
                    if (c >= '0' && c <= '9') {
                        chunkLen = (chunkLen << 4) + c - '0';
                        ++idx;
                    }
                    else if (c >= 'a' && c <= 'f') {
                        chunkLen = (chunkLen << 4) + c - ('a' - 10);
                        ++idx;
                    }
                    else if (c >= 'A' && c <= 'F') {
                        chunkLen = (chunkLen << 4) + c - ('A' - 10);
                        ++idx;
                    }
                    else if (c == '\r') {
                        state = S_LENGTH_CR;
                        ++idx;
                    }
                    else if (c == '\n') {
                        // Debug
                        //System.out.println(chunkLen);
                        state = S_LENGTH_LF;
                        ++idx;
                    }
                    else {
                        state = S_ERROR;
                    }
                    break;
                case S_LENGTH_CR:
                    if (c == '\n') {
                        // Debug
                        //System.out.println(chunkLen + " - " + Integer.toHexString(chunkLen));
                        if (chunkLen > 0) {
                            state = S_LENGTH_LF;
                        }
                        else {
                            state = S_END_CR;
                        }
                        ++idx;
                    }
                    else {
                        state = S_ERROR;
                    }
                    break;
                case S_LENGTH_LF:
                    digestable = readLimit - idx;
                    if (digestable > chunkLen) {
                        digestable = chunkLen;
                    }
                    if (chunkedDigest != null) {
                        chunkedDigest.update(bytes, idx, digestable);
                    }
                    if (out != null) {
                        out.write(bytes, idx, digestable);
                    }
                    idx += digestable;
                    chunkLen -= digestable;
                    if (chunkLen == 0) {
                        state = S_CHUNK_CR;
                    }
                    break;
                case S_CHUNK_CR:
                    if (c == '\r') {
                        state = S_CHUNK_LF;
                        ++idx;
                    }
                    else if (c == '\n') {
                        state = S_LENGTH;
                        ++idx;
                    }
                    else {
                        state = S_ERROR;
                    }
                    break;
                case S_CHUNK_LF:
                    if (c == '\n') {
                        state = S_LENGTH;
                        ++idx;
                    }
                    else {
                        state = S_ERROR;
                    }
                    break;
                case S_END_CR:
                    if (c == '\r') {
                        state = S_END_LF;
                        ++idx;
                    }
                    else if (c == '\n') {
                        state = S_DONE;
                        ++idx;
                    }
                    else {
                        state = S_ERROR;
                    }
                    break;
                case S_END_LF:
                    if (c == '\n') {
                        state = S_DONE;
                        ++idx;
                    }
                    else {
                        state = S_ERROR;
                    }
                    break;
                case S_DONE:
                    overflow = readLimit - idx;
                    idx = readLimit;
                    state = S_ERROR;
                    break;
                case S_ERROR:
                    overflow = readLimit - idx;
                    idx = readLimit;
                    // Debug
                    //System.out.println(c);
                    //++idx;
                    break;
                default:
                    throw new IllegalStateException("Epic fail!");
                }
            }
        }
    }

    @Override
    public int read() throws IOException {
        // Debug
        //System.out.println(">read() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
        //int possible;
        if (position == limit) {
            if (lastRead == -1) {
                // Debug
                //System.out.println("<read() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
                return -1;
            }
            compact();
            fillBuffer();
        }
        if (position < limit) {
            // Debug
            //System.out.println("<read() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
            return bytes[position++];
        }
        if (lastRead == -1) {
            // Debug
            //System.out.println("<read() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
            return -1;
        }
        // Debug
        //System.out.println("<read() - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
        throw new IllegalStateException("Epic fail!");
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // Debug
        //System.out.println(">read(byte[], " + off + ", " + len + ") - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
        int read = 0;
        int possible;
        int copyLen;
        while (read == 0 && len > 0) {
            possible = limit - position;
            while (possible < len && lastRead != -1) {
                fillBuffer();
                possible = limit - position;
            }
            if (possible > 0) {
                if (possible > len) {
                    possible = len;
                }
                copyLen = possible;
                read += copyLen;
                len -= copyLen;
                while (copyLen > 0) {
                    b[off++] = bytes[position++];
                    --copyLen;
                }
            }
            if (possible == 0) {
                if (lastRead != -1) {
                    throw new IllegalStateException("Epic fail!");
                }
                len = 0;
            }
        }
        if (read == 0 && lastRead != -1) {
            throw new IllegalStateException();
        }
        if (position == limit && lastRead == -1) {
            read = -1;
        }
        // Debug
        //System.out.println("<read(byte[], " + off + ", " + len + ") - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        // Debug
        //System.out.println(">skip(" + n + ") - position: " + position + " - limit: " + limit + " - lastRead: " + lastRead);
        long remaining = n;
        int possible;
        long skipped = 0;
        while (remaining > 0) {
            possible = limit - position;
            if (possible > 0) {
                if (possible > remaining) {
                    possible = (int)remaining;
                }
                position += possible;
                remaining -= possible;
                skipped += possible;
            }
            else {
                fillBuffer();
                possible = limit - position;
                if (possible == 0) {
                    if (lastRead != -1) {
                        throw new IllegalStateException("Epic fail!");
                    }
                    remaining = 0;
                }
            }
        }
        // Debug
        //System.out.println("<skip(" + n + ") - position: " + position + " - limit: " + limit + " - skipped: " + skipped + " - lastRead: " + lastRead);
        return skipped;
    }

}
