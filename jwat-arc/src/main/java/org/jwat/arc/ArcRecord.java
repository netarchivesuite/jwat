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
package org.jwat.arc;

import java.io.IOException;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.HttpHeader;
import org.jwat.common.Payload;

/**
 * This class represents a parsed ARC record header including possible
 * validation and format warnings/errors encountered in the process.
 * This class also contains the specific ARC record parser which is
 * intended to be called by the <code>ARCReader</code>.
 * The payload of the ARC record is accessible through a wrapped payload
 * object.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcRecord extends ArcRecordBase {

    /** Buffer size used in toString(). */
    public static final int TOSTRING_BUFFER_SIZE = 256;

    /**
     * Protected constructor to force instantiation of record header
     * from stream.
     */
    protected ArcRecord() {
    }

    public static ArcRecord create(ArcWriter writer) {
        ArcRecord ar = new ArcRecord();
        ar.trailingNewLines = 1;
        ar.diagnostics = new Diagnostics<Diagnosis>();
        ar.header = ArcHeader.initHeader(writer, ar.diagnostics);
        writer.fieldParsers.diagnostics = ar.diagnostics;
        return ar;
    }

    /**
     * Creates new <code>ArcRecord</code> based on data read from input
     * stream.
     * @param in <code>InputStream</code> used to read record header
     * @param versionBlock ARC file <code>VersionBlock</code>
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @return an <code>ArcRecord</code> or null if none was found.
     * @throws IOException io exception while parsing arc record
     */
    public static ArcRecord parseArcRecord(ArcReader reader,
            Diagnostics<Diagnosis> diagnostics,
            ArcHeader header, ByteCountingPushBackInputStream in)
                                                          throws IOException {
        ArcRecord ar = new ArcRecord();
        ar.recordType = RT_ARC_RECORD;
        ar.reader = reader;
        ar.diagnostics = diagnostics;
        ar.header = header;
        ar.in = in;
        // Process payload.
        ar.processPayload(in, reader);
        // Updated consumed after payload has been consumed.
        ar.consumed = in.getConsumed() - ar.header.startOffset;
        return ar;
    }

    @Override
    protected void processPayload(ByteCountingPushBackInputStream in,
                                        ArcReader reader) throws IOException {
        payload = null;
        if (header.archiveLength != null && header.archiveLength > 0L) {
            String digestAlgorithm = null;
            if (reader.bBlockDigest) {
                digestAlgorithm = reader.blockDigestAlgorithm;
            }
            payload = Payload.processPayload(in, header.archiveLength.longValue(),
                    reader.payloadHeaderMaxSize, digestAlgorithm);
            payload.setOnClosedHandler(this);
            // HttpHeader.
            if (HttpHeader.isSupported(header.urlScheme)
                    && !ArcConstants.CONTENT_TYPE_NO_TYPE.equals(
                            header.contentTypeStr)) {
                digestAlgorithm = null;
                if (reader.bPayloadDigest) {
                    digestAlgorithm = reader.payloadDigestAlgorithm;
                }
                // Try to read a valid HTTP response header from the payload.
                httpHeader = HttpHeader.processPayload(HttpHeader.HT_RESPONSE,
                            payload.getInputStream(), header.archiveLength.longValue(),
                            digestAlgorithm);
                if (httpHeader != null) {
                    if (httpHeader.isValid()) {
                        payload.setPayloadHeaderWrapped(httpHeader);
                    } else {
                        diagnostics.addError(
                                new Diagnosis(DiagnosisType.ERROR,
                                        "http header",
                                        "Unable to parse http header!"));
                    }
                }
            }
        } else if (HttpHeader.isSupported(header.urlScheme)
                            && !ArcConstants.CONTENT_TYPE_NO_TYPE.equals(
                                    header.contentTypeStr)) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    ArcConstants.ARC_FILE,
                    "Expected payload not found in the record block"));
        }
        return;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(TOSTRING_BUFFER_SIZE);
        builder.append("\nArcRecord [");
        builder.append(super.toString());
        builder.append(']');
        if (httpHeader != null) {
            builder.append(httpHeader.toString());
        }
        return builder.toString();
    }

}
