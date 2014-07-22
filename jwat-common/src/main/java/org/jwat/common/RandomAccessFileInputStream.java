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
import java.io.RandomAccessFile;

/**
 * This class wraps a <code>RandomAccessFile</code> into a usable
 * <code>InputStream</code> which supports random re-position.
 * Re-positioning is done by using seek() on the <code>RandomAccessFile</code>
 * object. (@see RandomAccessFile#seek())
 *
 * @author nicl
 */
public class RandomAccessFileInputStream extends InputStream {

    /** Encapsulated <code>RandomAccessFile</code> used for stream data. */
    protected RandomAccessFile raf;

    /** Current mark position in file. */
    protected long mark_position = -1;

    /**
     * Create a new random access <code>InputStream</code> with repositioning
     * capabilities.
     * @param raf <code>RandomAccessFile</code> used for stream data
     */
    public RandomAccessFileInputStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    /**
     * Closing this stream has no effect.
     * @throws IOException if an i/o error occurs while closing stream
     */
    @Override
    public void close() throws IOException {
        raf = null;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            mark_position = raf.getFilePointer();
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if (mark_position == -1) {
            throw new IOException("Mark not set or is invalid");
        }
        raf.seek(mark_position);
    }

    @Override
    public int available() throws IOException {
        long avail = raf.length() - raf.getFilePointer();
        return (int) (Math.min(avail, Integer.MAX_VALUE));
    }

    @Override
    public long skip(long n) throws IOException {
        long skip = Math.min(n, raf.length() - raf.getFilePointer());
        raf.seek(raf.getFilePointer() + skip);
        return skip;
    }

    @Override
    public int read() throws IOException {
        return raf.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return raf.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

}
