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
package dk.netarkivet.arc;

import java.io.IOException;
import java.util.Collection;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.common.HttpResponse;
import dk.netarkivet.common.Payload;

/**
 * This class represents a parsed ARC record header including possible 
 * validation and format warnings/errors encountered in the process.
 * The payload of the ARC record is accessible through a wrapped payload
 * object.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcRecord extends ArcRecordBase {

    /** Special content-type for none. */
    public static final String CONTENT_TYPE_NO_TYPE = "no-type";

    /** HttpResponse header content parse from payload. */
    public HttpResponse httpResponse = null;

    /**
     * Protected constructor to force instantiation of record header
     * from stream.
     */
    protected ArcRecord() {
    }

    /**
     * Creates new <code>ArcRecord</code> based on data read from input
     * stream.
     * @param in <code>InputStream</code> used to read record header
     * @param versionBlock ARC file <code>VersionBlock</code>
     * @return an <code>ArcRecord</code> or null if none was found.
     */
    public static ArcRecord parseArcRecord(ByteCountingPushBackInputStream in,
    					ArcVersionBlock versionBlock) throws IOException {
        ArcRecord ar = new ArcRecord();
        ar.versionBlock = versionBlock;
        ar.version = versionBlock.version;

        // Read record line.
        // Looping past empty lines.
        ar.startOffset = in.getConsumed();
        String recordLine = in.readLine();
        while ((recordLine != null) && (recordLine.length() == 0)) {
            ar.startOffset = in.getConsumed();
            recordLine = in.readLine();
        }
        if (recordLine != null) {
            ar.parseRecord(recordLine);
        } else {
            // EOF
            ar = null;
        }
        if (ar != null) {
            ar.processPayload(in);
        }
        return ar;
    }

    @Override
    protected void processPayload(ByteCountingPushBackInputStream in)
                                                        throws IOException {
        payload = null;
        if (recLength != null && recLength > 0L) {
            payload = new Payload(in, recLength.longValue());
            if (HttpResponse.isSupported(protocol)
                            && !CONTENT_TYPE_NO_TYPE.equals(recContentType)) {
                httpResponse = HttpResponse.processPayload(
                			payload.getInputStream(), recLength.longValue());
            }
        } else if (HttpResponse.isSupported(protocol)
                            && !CONTENT_TYPE_NO_TYPE.equals(recContentType)) {
            // TODO warning payload expected
        }
        return;
    }

    /**
     * Checks if the ARC record payload has warnings.
     * @return true/false based on whether the ARC record has warnings or not
     */
    @Override
    public boolean hasWarnings() {
        return ((httpResponse != null) && (httpResponse.hasWarnings()));
    }

    /**
     * Returns the ARC record payload warnings.
     * @return validation errors list/
     */
    @Override
    public Collection<String> getWarnings() {
        return (hasWarnings())? httpResponse.getWarnings() : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("\nArcRecord [");
        builder.append(super.toString());
        builder.append(']');
        if (httpResponse != null) {
            builder.append(httpResponse.toString());
        }
        return builder.toString();
    }

}
