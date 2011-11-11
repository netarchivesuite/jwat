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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Arc record payload.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class Payload {

    /** Payload content. */
    private static final int BUFFER_SIZE = 4096;

    /** Payload content. */
    protected InputStream in;

    /** Payload length. */
    protected long length;

    /** Handler called when this payloads stream has been fully consumed. */
    protected PayloadOnClosedHandler onClosedHandler;

    /**
     * Creates new <code>ArcPayload</code> instance.
     * @param in the input stream to parse.
     * @param length payload length.
     * @throws IOException io exception in reading process
     */
    public Payload(InputStream in, long length) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length");
        }
        this.length = length;
        this.in = new BufferedInputStream(
                        new FixedLengthInputStream(in, length), BUFFER_SIZE);
    }

    /**
     * Get <code>InputStream</code> to read payload data.
     * @return <code>InputStream</code> to read payload data.
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * Get payload length.
     * @return payload length
     */
    public long getLength() {
        return length;
    }

    /**
     * Set optional handler to be called when payload is closed.
     * @param onClosedHandler on closed handler implementation
     */
    public void setOnClosedHandler(PayloadOnClosedHandler onClosedHandler) {
    	this.onClosedHandler = onClosedHandler;
    }

    /**
     * Closes the this payload stream, skipping unread bytes in the process.
     * @throws IOException io exception in closing process
     */
    public void close() throws IOException {
    	if (in != null) {
            in.close();
            in = null;
    	}
        if (onClosedHandler != null) {
        	onClosedHandler.payloadClosed();
        	onClosedHandler = null;
        }
    }

}
