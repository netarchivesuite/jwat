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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>InputStream</code> that keeps tracks of the number of consumed bytes
 * at any given time.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ByteCountingInputStream extends FilterInputStream {

    /** Read line initial size. */
    public static final int READLINE_INITIAL_SIZE = 128;

    /** Offset relative to beginning of stream. */
    protected long consumed = 0;

    /** Byte counter which can also be changed. */
    protected long counter = 0;

    /**
     * Constructs an <code>InputStream</code> that counts the bytes
     * as it reads them.
     * @param parent InputStream to wrap
     */
    public ByteCountingInputStream(InputStream parent) {
        super(parent);
    }

    /**
     * Retrieve the number of bytes consumed by this stream.
     * @return current byte offset in this stream
     */
    public long getConsumed() {
        return consumed;
    }

    /**
     * Change the counter value.
     * Useful for reading zero indexed relative data.
     * @param bytes new counter value
     */
    public void setCounter(long bytes) {
        counter = bytes;
    }

    /**
     * Retrieve the current counter value.
     * @return current counter value
     */
    public long getCounter() {
        return counter;
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
        int b = in.read();
        if (b != -1) {
            ++consumed;
            ++counter;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = in.read(b, off, len);
        if (n > 0) {
            consumed += n;
            counter += n;
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        n = in.skip(n);
        consumed += n;
        counter += n;
        return n;
    }

}
