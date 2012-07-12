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
import org.jwat.common.Payload;

/**
 * This class represents a parsed ARC version block including possible
 * validation and format warnings/errors encountered in the process.
 * This class also contains the specific ARC version block parser which is
 * intended to be called by the <code>ARCReader</code>.
 * Any metadata present in the ARC version block is accessible
 * through the payload object. Currently the payload is automatically loaded
 * into a string which is accessible through the version block api.
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

    public static ArcVersionBlock create(ArcWriter writer) {
        ArcVersionBlock vb = new ArcVersionBlock();
        vb.header = ArcHeader.initHeader(writer, vb.diagnostics);
        writer.fieldParsers.diagnostics = vb.diagnostics;
        return vb;
    }

    /**
     * Creates new <code>VersionBlock</code> based on data read from input
     * stream.
     * @param in <code>InputStream</code> used to read version block
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @return an <code>ArcVersionBlock</code> or null if none was found.
     * @throws IOException io exception in the process of reading version block
     */
    public static ArcVersionBlock parseVersionBlock(ArcReader reader,
            ArcHeader header, ArcFieldParsers fieldParsers,
            ByteCountingPushBackInputStream in) throws IOException {
        ArcVersionBlock vb = new ArcVersionBlock();
        //vb.versionBlock = vb;
        vb.recordType = RT_VERSION_BLOCK;
        vb.reader = reader;
        vb.header = header;
        vb.in = in;
        // Process payload.
        vb.processPayload(in, reader);
        // Updated consumed after payload has been consumed.
        vb.consumed = in.getConsumed() - vb.header.startOffset;
        return vb;
    }

    /**
     * Checks if the ARC record is valid.
     * @return true/false based on whether the ARC record is valid or not
     */
    /*
    @Override
    public boolean isValid() {
        return (isMagicArcFile && isVersionValid && isValidFieldDesc
                && super.isValid());
    }
    */

    /**
     * Checks if the processed file is an ARC file.
     * @param recordLine First line in the version block header.
     */
    /*
    protected void checkFileDesc(String recordLine) {
        if (recordLine != null){
            // Check file ARC magic number
            if(recordLine.startsWith(ArcConstants.ARC_SCHEME)) {
                isMagicArcFile = true;
            }
        }
        if (!isMagicArcFile){
            // Adding validation error
            diagnostics.addError(new Diagnosis(DiagnosisType.INVALID,
                    ARC_FILE,
                    "Invalid file magic number"));
        }
    }
    */

    /**
     * Validates the version block content type.
     * Errors and/or warnings are reported on the diagnostics object.
     */
    protected void valdateContentType() {
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
     * An ARC v1.1 version block should have a payload consisting of XML
     * formatted
     * metadata related to the harvesters configuration.
     * @param in input stream containing the payload
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @throws IOException io exception in the process of reading payload
     */
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
            // ArcVersionHeader.
            digestAlgorithm = null;
            if (reader.bPayloadDigest) {
                digestAlgorithm = reader.payloadDigestAlgorithm;
            }
            versionHeader = ArcVersionHeader.processPayload(
                    payload.getInputStream(), header.archiveLength.longValue(),
                    digestAlgorithm, reader.fieldParsers, diagnostics);
            // TODO
        }
        if ((payload == null) && ArcVersion.VERSION_1_1.equals(version)) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    ArcConstants.ARC_FILE,
                    "Required metadata payload not found in the version block"));
        }
    }

    /*
    if (vb != null) {
        if (vb.header.archiveLength == null) {
            // Missing length.
            vb.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID,
                    ARC_FILE,
                    "VersionBlock length missing!"));
        } else if (in.getCounter() > vb.header.archiveLength) {
            // Mismatch in consumed and declare length.
            vb.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID,
                    ARC_FILE,
                    "VersionBlock length to small!"));
        }
    }
    */

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
