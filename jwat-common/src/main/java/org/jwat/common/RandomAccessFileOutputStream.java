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
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * This class wraps a <code>RandomAccessFile</code> into a usable
 * <code>OutputStream</code> which supports random re-position.
 * Re-positioning is done by using seek() on the <code>RandomAccessFile</code>
 * object. (@see RandomAccessFile#seek())
 *
 * @author nicl
 */
public class RandomAccessFileOutputStream extends OutputStream {

    /** Encapsulated <code>RandomAccessFile</code> used for stream data. */
    protected RandomAccessFile raf;

    /**
     * Create a new random access <code>OutputStream</code> with repositioning
     * capabilities.
     * @param raf <code>RandomAccessFile</code> used for stream data
     */
    public RandomAccessFileOutputStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    /**
     * Closing this stream has no effect.
     */
    @Override
    public void close() throws IOException {
        raf = null;
    }

    /**
     * Flushing this stream has no effect.
     */
    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(int b) throws IOException {
        raf.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        raf.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }

}
