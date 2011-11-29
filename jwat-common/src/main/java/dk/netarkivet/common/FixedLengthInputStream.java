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
package dk.netarkivet.common;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>InputStream</code> with a fixed amount of bytes available to read.
 * When the stream is closed the remaining bytes that have not been read are 
 * read or skipped.
 *
 * @author lbihanic, selghissassi
 */
public final class FixedLengthInputStream extends FilterInputStream {

    /** Remaining bytes available. */
    private long remaining;

    /**
     * Create a new input stream with a fixed number of bytes available from
     * the underlying stream.
     * @param in the input stream to wrap
     * @param length fixed number of bytes available through this stream
     */
    public FixedLengthInputStream(InputStream in, long length) {
        super(in);
        this.remaining = length;
    }

    @Override
    public void close() throws IOException {
        long skippedLast = 0;
        while (remaining > 0 && skippedLast != -1) {
            remaining -= skippedLast;
            skippedLast = in.skip(remaining);
        }
    }

    @Override
    public int available() throws IOException {
        return (remaining > Integer.MAX_VALUE)
                                ? Integer.MAX_VALUE : (int) (remaining);
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
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
    public int read(byte[] b, int off, int len) throws IOException {
        int l = -1;
        if (remaining > 0L) {
            l = super.read(b, off, (int) Math.min(len, remaining));
            if(l > 0){
                remaining -= l;
            }
        }
        return l;
    }

    @Override
    public int read() throws IOException {
        int b = -1;
        if (remaining > 0L) {
            b = super.read();
            --remaining;
        }
        return b;
    }

    @Override
    public long skip(long n) throws IOException {
        long l = -1;
        if (remaining > 0L){
            l = super.skip(Math.min(n, remaining));
            if (l > 0L) {
                remaining -= l;
            }
        }
        return l;
    }

}
