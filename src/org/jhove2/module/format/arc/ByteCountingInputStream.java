/**
 * JHOVE2 - Next-generation architecture for format-aware characterization
 *
 * Copyright (c) 2009 by The Regents of the University of California,
 * Ithaka Harbors, Inc., and The Board of Trustees of the Leland Stanford
 * Junior University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * o Neither the name of the University of California/California Digital
 *   Library, Ithaka Harbors/Portico, or Stanford University, nor the names of
 *   its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jhove2.module.format.arc;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>InputStream</code> that keeps tracks of the amount of bytes
 * read at any point in time.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ByteCountingInputStream extends FilterInputStream {

    /** New line delimiter. */
    public static final int NL = '\n';

    /** Offset relative to begining of stream. */
    protected long consumed = 0;

    /** Relative byte counter. */
    protected long counter = 0;

    /**
     * Constructs an <code>InputStream</code> that counts the bytes
     * its reads.
     * @param parent InputStream to wrap
     */
    public ByteCountingInputStream(InputStream parent) {
        super(parent);
    }

    /**
     * Retrieve the number of consumed bytes by this stream.
     * @return current byte offset in this stream
     */
    public long getConsumed() {
        return consumed;
    }

    /**
     * Change the bytes read value.
     * Useful for reading zero indexed relative data.
     * @param bytes new value
     * @return
     */
    public void setCounter(long bytes) {
        counter = bytes;
    }

    /**
     * Retrieve the current relative counter value.
     * @return current relative counter value
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
        int b = super.read();
        if (b != -1) {
            ++consumed;
            ++counter;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            consumed += n;
            counter += n;
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        n = super.skip(n);
        this.consumed += n;
        return n;
    }

    /**
     * Read a single line into a string.
     * @return single string line
     * @throws IOException io exception while reading line
     */
    public String readLine() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        int b;
        while (true) {
            b = this.read();
            if (b == -1) {
                return null;    //Unexpected EOF
            }
            if (b == NL){
                break;
            }
            bos.write(b);
        }
        return bos.toString("US-ASCII");
    }

    /**
     * Read several lines into one string.
     * @param lines number of lines to read
     * @return String counting the requested amount of lines.
     * @throws IOException io exception while reading line
     */
    public String readLines(int lines) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        int i = lines;
        while (i > 0) {
            int b = this.read();
            if (b == -1) {
                bos = null;
                break;             // Unexpected EOF!
            }
            bos.write(b);
            if (b == NL) {
                --i;
            }
        }
        return (bos!=null)? bos.toString("US-ASCII") : null;
    }

}
