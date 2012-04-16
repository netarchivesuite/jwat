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

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>InputStream</code> with a maximum amount of bytes available to read.
 * When the stream is closed the remaining bytes are left untouched.
 *
 * @author nicl
 */
public class MaxLengthRecordingInputStream extends FilterInputStream {

    /** Buffer size to use when read skipping. */
    public static final int SKIP_READ_BUFFER_SIZE = 1024;

    /** Read buffer used by the skip method. */
    protected byte[] skip_read_buffer = new byte[SKIP_READ_BUFFER_SIZE];

    /** Output stream used to keep a record of data read. */
    protected ByteArrayOutputStream record;

    /** Maximum remaining bytes available. */
    protected long available;

    /**
     * Create a new input stream with a maximum number of bytes available from
     * the underlying stream.
     * @param in the input stream to wrap
     * @param available maximum number of bytes available through this stream
     */
    public MaxLengthRecordingInputStream(InputStream in, long available) {
        super(in);
        this.record = new ByteArrayOutputStream();
        this.available = available;
    }

    /**
     * Return the bytes recorded by the stream.
     * @return recorded data as a byte array
     */
    public byte[] getRecording() {
        return record.toByteArray();
    }

    /**
     * Closing will only close the recording and not call the parent's close
     * method.
     * @throws IOException if an io error occurs while closing stream
     */
    @Override
    public void close() throws IOException {
        record.close();
    }

    @Override
    public int available() throws IOException {
        return (available > Integer.MAX_VALUE)
                                ? Integer.MAX_VALUE : (int) (available);
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
    public int read() throws IOException {
        int b = -1;
        if (available > 0) {
            b = in.read();
            if (b != -1) {
                --available;
                record.write(b);
            }
        }
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = -1;
        if (available > 0) {
            bytesRead = in.read(b, off, (int) Math.min(len, available));
            if (bytesRead > 0) {
                available -= bytesRead;
                record.write(b, off, bytesRead);
            }
        }
        return bytesRead;
    }

    @Override
    public long skip(long n) throws IOException {
        long bytesSkipped = 0;
        if (available > 0) {
            bytesSkipped = read(skip_read_buffer, 0, (int) Math.min(
                            Math.min(n, available), SKIP_READ_BUFFER_SIZE));
            if (bytesSkipped == -1) {
                bytesSkipped = 0;
            }
        }
        return bytesSkipped;
    }

}
