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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.common.Payload;

/**
 * This class represents a parsed ARC version block including possible
 * validation and format warnings/errors encountered in the process.
 * This class also contains the specific ARC version block parser which is
 * intended to be called by the <code>ARCReader</code>.
 * If present any metadata in the ARC version block is accessible
 * through the payload object. Currently the payload is automatically loaded
 * into a string which is accessible through the version block api.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcVersionBlock extends ArcRecordBase {

    /** Buffer size used in payload processing. */
    public static final int PROCESSPAYLOAD_BUFFER_SIZE = 1024;

    /** Buffer size used in toString(). */
    public static final int TOSTRING_BUFFER_SIZE = 256;

    /*
     * Validity.
     */

    /** Did we find the magic ARC number. */
    protected boolean isMagicArcFile = false;

    /** Did we find a valid version number. */
    protected boolean isVersionValid = false;

    /** Did we recognize the field description line. */
    protected boolean isValidFieldDesc = false;

    /*
     * Fields.
     */

    /** Version description field. */
    public Integer versionNumber;

    /** Reserved field, used for version 1.1. */
    public Integer reserved;

    /** Version block origin code. */
    public String originCode;

    /** Version 1.1 xml config. */
    public String xml = null;

    /*
     * ValidatOr.
     */

    /** <code>FieldValidator</code> used to validate record fields. */
    protected ArcFieldValidator descValidator = null;

    /** <code>FieldValidator</code> for version fields. */
    protected static ArcFieldValidator versionValidator =
            ArcFieldValidator.prepare(ArcConstants.VERSION_DESC_FIELDS);

    /** <code>FieldValidator</code> for version 1 description fields. */
    protected static ArcFieldValidator version1DescValidator =
            ArcFieldValidator.prepare(ArcConstants.VERSION_1_BLOCK_FIELDS);

    /** <code>FieldValidator</code> for version 2 description fields. */
    protected static ArcFieldValidator version2DescValidator =
            ArcFieldValidator.prepare(ArcConstants.VERSION_2_BLOCK_FIELDS);

    /**
     * Protected constructor to force instantiation of version block
     * from stream.
     */
    protected ArcVersionBlock() {
    }

    /**
     * Creates new <code>VersionBlock</code> based on data read from input
     * stream.
     * @param in <code>InputStream</code> used to read version block
     * @return an <code>ArcVersionBlock</code> or null if none was found.
     * @throws IOException io exception in the process of reading version block
     */
    public static ArcVersionBlock parseVersionBlock(
                    ByteCountingPushBackInputStream in) throws IOException {
        ArcVersionBlock vb = new ArcVersionBlock();
        vb.versionBlock = vb;

        vb.isMagicArcFile = false;
        vb.isVersionValid = false;
        vb.isValidFieldDesc = false;
        // Read 3 line header.
        vb.startOffset = in.getConsumed();
        String recordLine = in.readLine();
        in.setCounter(0);
        String versionLine = in.readLine();
        String fieldLine = in.readLine();
        // Check for magic number
        if (recordLine != null) {
            vb.checkFileDesc(recordLine);
            // Extract the path
            //this.path =
            // desc.url.substring(ArcConstants.ARC_SCHEME.length());
        }
        // Check for version and parse if present.
        if (versionLine != null && versionLine.length() > 0) {
            String[] versionArr = versionLine.split(" ", -1);
            if (versionArr.length != ArcConstants.VERSION_DESC_FIELDS.length) {
                vb.addValidationError(ArcErrorType.INVALID, ARC_RECORD,
                                        "Invalid version description");
            }
            // Get version and origin
            vb.versionNumber = vb.parseInteger(
                        ArcFieldValidator.getArrayValue(versionArr, 0),
                        ArcConstants.VERSION_FIELD, false);
            vb.reserved = vb.parseInteger(
                        ArcFieldValidator.getArrayValue(versionArr, 1),
                        ArcConstants.RESERVED_FIELD, false);
            vb.originCode = vb.parseString(
                        ArcFieldValidator.getArrayValue(versionArr, 2),
                        ArcConstants.ORIGIN_FIELD, false);
            vb.checkVersion();
            // TODO default version
        }
        // Extract format description.
        if (fieldLine != null) {
            if (ArcConstants.VERSION_1_BLOCK_DEF.equals(fieldLine)) {
                vb.isValidFieldDesc = true;
                vb.descValidator = version1DescValidator;
            } else if (ArcConstants.VERSION_2_BLOCK_DEF.equals(fieldLine)) {
                vb.isValidFieldDesc = true;
                vb.descValidator = version2DescValidator;
            } else {
                //Using version-1-block fields in this case
                vb.descValidator = version1DescValidator;
                vb.addValidationError(ArcErrorType.INVALID, ARC_FILE,
                        "Unsupported version block definition -> "
                        + "Using version-1-block definition");
            }
        }
        // Parse record.
        if (recordLine != null) {
            vb.parseRecord(recordLine);
        } else {
            // EOF
            vb = null;
        }
        if (vb != null) {
            if (vb.recLength == null) {
                // Missing length.
                vb.addValidationError(ArcErrorType.INVALID, ARC_FILE,
                        "VersionBlock length missing!");
            } else if (in.getCounter() > vb.recLength) {
                // Mismatch in consumed and declare length.
                vb.addValidationError(ArcErrorType.INVALID, ARC_FILE,
                        "VersionBlock length to small!");
            }
            // Process payload = xml config
            vb.processPayload(in);
        }
        return vb;
    }

    /**
     * Checks if the ARC record is valid.
     * @return true/false based on whether the ARC record is valid or not
     */
    @Override
    public boolean isValid() {
        return (isMagicArcFile && isVersionValid && isValidFieldDesc
                && super.isValid());
    }

    /**
     * Checks if the processed file is an ARC file.
     * @param recordLine First line in the version block header.
     */
    protected void checkFileDesc(String recordLine) {
        if (recordLine != null){
            // Check file ARC magic number
            if(recordLine.startsWith(ArcConstants.ARC_SCHEME)) {
                isMagicArcFile = true;
            }
        }
        if (!isMagicArcFile){
            // Adding validation error
            addValidationError(ArcErrorType.INVALID, ARC_FILE,
                    "Invalid file magic number");
        }
    }

    /**
     * Checks {@link ArcVersion} description.
     */
    protected void checkVersion() {
        version = null;
        if (versionNumber != null && reserved != null) {
            // Check ARC version number
            version = ArcVersion.fromValues(versionNumber.intValue(),
                    reserved.intValue());
        }
        isVersionValid = (version != null);
        if (!isVersionValid) {
            // Add validation error
            addValidationError(ArcErrorType.INVALID, ARC_FILE,
                "Invalid version : [version number : " + versionNumber
                 + ",reserved : " + reserved +']');
        }
    }

    /**
     * Parses version block content type.
     * @param contentType the content type to parse
     * @return the version block content type
     */
    @Override
    public String parseContentType(String contentType) {
        String ct = super.parseContentType(contentType);
        if (ct == null || ct.length() == 0) {
            // Version block content-type is required.
            addValidationError(ArcErrorType.MISSING,
                                    ArcConstants.CONTENT_TYPE_FIELD, ct);
            ct = null;
        } else if (!ArcConstants.VERSION_BLOCK_CONTENT_TYPE.equalsIgnoreCase(
                ct)) {
            // Version block content-type should be equal to "text/plain"
            addValidationError(ArcErrorType.INVALID,
                                    ArcConstants.CONTENT_TYPE_FIELD, ct);
            ct = ct.toLowerCase();
        }
        return ct;
    }

    /**
     * An ARC v1.1 version block should have a payload consisting of XML
     * formatted
     * metadata related to the harvesters configuration.
     * @param in input stream containing the payload
     * @throws IOException io exception in the process of reading payload
     */
    @Override
    protected void processPayload(ByteCountingPushBackInputStream in)
                                                        throws IOException {
        payload = null;
        if (recLength != null && (recLength - in.getCounter()) > 0L) {
            payload = new Payload(in, recLength.longValue()
                                            - in.getCounter(), null);
            payload.setOnClosedHandler(this);
            // Look for trailing XML formatted metadata.
            byte[] buffer = new byte[PROCESSPAYLOAD_BUFFER_SIZE];
            int read = 0;
            ByteArrayOutputStream payloadData =
                    new ByteArrayOutputStream((int) payload.getLength());
            while (payload.getInputStream().available() > 0 && read != -1) {
                read = payload.getInputStream().read(buffer, 0, buffer.length);
                if (read != -1) {
                    payloadData.write(buffer, 0, read);
                }
            }
            byte[] xmlBytes = payloadData.toByteArray();
            payloadData.close();
            xml = new String(xmlBytes);
            payload.close();
        }
        if ((payload == null) && ArcVersion.VERSION_1_1.equals(version)) {
            addValidationError(ArcErrorType.INVALID, ARC_FILE,
                    "Required network doc not found in the version block");
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(TOSTRING_BUFFER_SIZE);
        builder.append("\nVersionBlock : [\n");
        builder.append(super.toString());
        builder.append("versionNumber:");
        if(versionNumber != null){
            builder.append(versionNumber);
        }
        builder.append(',');
        builder.append("reserved:");
        if(reserved != null){
            builder.append(reserved);
        }
        builder.append(',');
        builder.append("originCode:");
        if(originCode != null){
            builder.append(originCode);
        }
        builder.append("]\n");
        return builder.toString();
    }

}
