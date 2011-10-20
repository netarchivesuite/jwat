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

import java.io.IOException;
import java.io.StringReader;

/**
 * <code>StringReader</code> that keeps tracks of the amount of chars
 * read at any point in time.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class StringCountingReader extends StringReader {

    /** New line delimiter. */
    protected static final int NL = '\n';

    /** Version block header length. */
    protected final long consumedLength;

    /** Offset relative to beginning of stream. */
    protected long offset = 0;

    /** Relative byte counter. */
    protected long counter = 0;

    /**
     * Creates a new <code>StringReader</code> that keeps track of
     * consumed characters.
     * @param string Arbitrary string.
     * @param consumedLength version block header length.
     */
    public StringCountingReader(String string, long consumedLength) {
        super(string);
        this.consumedLength = consumedLength;
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

    /**
     * Consumed length getter.
     * @return the consumed length
     */
    public long getConsumedDataLength() {
        return this.consumedLength;
    }

    /**
     * Retrieve the current byte offset value.
     * @return current byte offset in stream
     */
    public long getOffset() {
        return offset;
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
     * Get the current number of read characters.
     * @return number of read characters
     */
    public long getCounter() {
        return counter;
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c != -1) {
            ++offset;
            ++counter;
        }
        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int n = super.read(cbuf, off, len);
        if (n > 0) {
            offset += n;
            counter += n;
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        n = super.skip(n);
        this.offset += n;
        return n;
    }

    /**
     * Reads a line defined as characters read until encountering a
     * <code>LF</code> or EOF.
     * @return Line read from buffered <code>StringReader</code>
     * @throws IOException io exception while reading line
     */
    public String readLine() throws IOException {
        StringBuilder buf = new StringBuilder();
        for (int c = read(); (c != -1) && (c != NL); c = read()) {
            buf.append((char) c);
        }
        return buf.toString();
    }

}
