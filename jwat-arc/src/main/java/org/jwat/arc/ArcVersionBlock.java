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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.Payload;

/**
 * This class represents an ARC version block and header including possible
 * validation and format warnings/errors encountered in the process.
 * This class also contains the specific ARC version block payload parser.
 * Any metadata present in the ARC version block is accessible
 * through the payload object.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcVersionBlock extends ArcRecordBase {

    /** Buffer size used in toString(). */
    public static final int TOSTRING_BUFFER_SIZE = 256;

    /**
     * Protected constructor to force instantiation of version block
     * from stream.
     */
    protected ArcVersionBlock() {
    }

    /**
     * Create and initialize a new <code>ArcVersionBlock</code> for writing.
     * @param writer writer which shall be used
     * @return an <code>ArcVersionBlock</code> prepared for writing
     */
    public static ArcVersionBlock create(ArcWriter writer) {
        ArcVersionBlock vb = new ArcVersionBlock();
        vb.trailingNewLines = 1;
        vb.diagnostics = new Diagnostics<Diagnosis>();
        vb.header = ArcHeader.initHeader(writer, vb.diagnostics);
        writer.fieldParsers.diagnostics = vb.diagnostics;
        return vb;
    }

    /**
     * Creates a new <code>VersionBlock</code> based on the supplied header and
     * the version block in the payload, if present.
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @param diagnostics diagnostics used to report errors and/or warnings
     * @param header record header that has already been processed
     * @param fieldParsers parser used to read and validate fields
     * @param in <code>InputStream</code> used to read version block
     * @return an <code>ArcVersionBlock</code>
     * @throws IOException if an i/o exception occurs in the process of reading
     * version block
     */
    public static ArcVersionBlock parseVersionBlock(ArcReader reader,
            Diagnostics<Diagnosis> diagnostics,
            ArcHeader header, ArcFieldParsers fieldParsers,
            ByteCountingPushBackInputStream in) throws IOException {
        ArcVersionBlock vb = new ArcVersionBlock();
        vb.recordType = RT_VERSION_BLOCK;
        vb.reader = reader;
        vb.diagnostics = diagnostics;
        vb.header = header;
        vb.in = in;
        vb.processPayload(in, reader);
        vb.consumed = in.getConsumed() - vb.header.startOffset;
        return vb;
    }

    /**
     * Validates the version block content type.
     * Errors and/or warnings are reported on the diagnostics object.
     */
    protected void validateContentType() {
        if (header.contentType == null) {
            // Version block content-type is required.
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    ArcConstants.FN_CONTENT_TYPE,
                    ArcConstants.CONTENT_TYPE_FORMAT));
        } else if (!ArcConstants.VERSION_BLOCK_CONTENT_TYPE.equals(
                header.contentType.contentType) ||
                !ArcConstants.VERSION_BLOCK_MEDIA_TYPE.equals(header.contentType.mediaType)) {
            // Version block content-type should be equal to "text/plain"
            diagnostics.addWarning(new Diagnosis(DiagnosisType.INVALID_EXPECTED,
                    ArcConstants.FN_CONTENT_TYPE,
                    header.contentTypeStr,
                    ArcConstants.CONTENT_TYPE_TEXT_PLAIN));
        }
    }

    /**
     * An ARC version block payload must include a version line and a
     * block description line and in v1.1 the rest of the payload consists
     * of XML formatted metadata related to the harvesters configuration.
     * @param in input stream containing the payload
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @throws IOException if an i/o exception occurs while reading the payload
     */
    @Override
    protected void processPayload(ByteCountingPushBackInputStream in,
                                        ArcReader reader) throws IOException {
        payload = null;
        validateContentType();
        if (header.archiveLength != null && header.archiveLength > 0L) {
            String digestAlgorithm = null;
            if (reader.bBlockDigest) {
                digestAlgorithm = reader.blockDigestAlgorithm;
            }
            payload = Payload.processPayload(in, header.archiveLength.longValue(),
                    reader.payloadHeaderMaxSize, digestAlgorithm);
            payload.setOnClosedHandler(this);
            // ArcVersionHeader.
            digestAlgorithm = null;
            if (reader.bPayloadDigest) {
                digestAlgorithm = reader.payloadDigestAlgorithm;
            }
            // Try to read a valid ARC version block from the payload.
            versionHeader = ArcVersionHeader.processPayload(
                    payload.getInputStream(), header.archiveLength.longValue(),
                    digestAlgorithm, reader.fieldParsers, diagnostics);
            if (versionHeader != null) {
                version = versionHeader.version;
                if (versionHeader.isValid()) {
                    payload.setPayloadHeaderWrapped(versionHeader);
                } else {
                    diagnostics.addError(
                            new Diagnosis(DiagnosisType.ERROR,
                                    ArcConstants.ARC_VERSION_BLOCK,
                                    "Version block is not valid!"));
                }
            }
        } else {
            diagnostics.addError(
                    new Diagnosis(DiagnosisType.INVALID,
                            ArcConstants.ARC_FILE,
                            "VersionBlock length missing!"));
        }
        if (versionHeader != null && versionHeader.isValid()) {
            if (ArcVersion.VERSION_1_1.equals(version)) {
                if ((versionHeader.getRemaining() == 0)) {
                    bHasEmptyPayload = true;
                    diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                            ArcConstants.ARC_FILE,
                            "Expected metadata payload not found in the version block"));
                }
            } else {
                if (versionHeader.getRemaining() == 0) {
                    bHasEmptyPayload = true;
                } else {
                    if (!reader.bStrict) {
                        // I'm going on a limb here that IA's ARC writer will
                        // not write in excess of 4GB useless newlines.
                        ByteArrayOutputStream out_payload = new ByteArrayOutputStream(
                                (int)versionHeader.getRemaining());
                        InputStream in_payload = versionHeader.getPayloadInputStream();
                        int read;
                        byte[] tmpBuf = new byte[1024];
                        while ((read = in_payload.read(tmpBuf)) != -1) {
                            out_payload.write(tmpBuf, 0, read);
                        }
                        in_payload.close();
                        out_payload.close();
                        excessiveMetadata = out_payload.toByteArray();
                        ByteArrayInputStream in_newlines = new ByteArrayInputStream(excessiveMetadata);
                        if (!isValidStreamOfCRLF(in_newlines)) {
                            diagnostics.addError(new Diagnosis(DiagnosisType.UNDESIRED_DATA,
                                    "version block metadata payload",
                                    "Metadata payload must not be present in this version"));
                        } else {
                            bHasEmptyPayload = true;
                        }
                        in_newlines.close();
                    } else {
                        diagnostics.addError(new Diagnosis(DiagnosisType.UNDESIRED_DATA,
                                "version block metadata payload",
                                "Metadata payload must not be present in this version"));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(TOSTRING_BUFFER_SIZE);
        builder.append("\nVersionBlock : [\n");
        builder.append(super.toString());
        builder.append("]\n");
        if (versionHeader != null) {
            builder.append(versionHeader.toString());
        }
        return builder.toString();
    }

}
