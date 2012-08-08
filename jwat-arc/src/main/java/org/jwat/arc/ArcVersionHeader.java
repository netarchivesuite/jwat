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
import org.jwat.common.MaxLengthRecordingInputStream;
import org.jwat.common.PayloadWithHeaderAbstract;

public class ArcVersionHeader extends PayloadWithHeaderAbstract {

    /** ARC field parser used.
     * Must be set prior to calling the various methods. */
    protected ArcFieldParsers fieldParsers;

    /** Is the version format valid. */
    public boolean bValidVersionFormat;

    /** Version string. */
    public String versionStr;

    /** Did we find a valid version number. */
    protected boolean isVersionValid;

    /** Did we recognize the block description line. */
    public boolean isValidBlockdDesc;

    public int blockDescVersion;

    /*
     * Fields.
     */

    /** Version description field string value. */
    public String versionNumberStr;

    /** Version description field integer value. */
    public Integer versionNumber;

    /** Reserved field value string, used for version 1.1. */
    public String reservedStr;

    /** Reserved field value, used for version 1.1. */
    public Integer reserved;

    /** Version block origin code. */
    public String originCode;

    /** ARC record version. */
    public ArcVersion version;

    public static ArcVersionHeader processPayload(ByteCountingPushBackInputStream pbin,
            long length, String digestAlgorithm, ArcFieldParsers fieldParsers,
            Diagnostics<Diagnosis> diagnostics) throws IOException {
        if (pbin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'pbin' is null");
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "The 'length' is less than zero: " + length);
        }
        if (fieldParsers == null) {
            throw new IllegalArgumentException(
                    "'fieldParsers' is null");
        }
        if (diagnostics == null) {
            throw new IllegalArgumentException(
                    "'diagnostics' is null");
        }
        ArcVersionHeader avh = new ArcVersionHeader();
        avh.in_pb = pbin;
        avh.totalLength = length;
        avh.digestAlgorithm = digestAlgorithm;
        avh.fieldParsers = fieldParsers;
        avh.diagnostics = diagnostics;
        avh.initProcess();
        return avh;
    }

    @Override
    protected boolean readHeader(MaxLengthRecordingInputStream in,
            long payloadLength) throws IOException {
        ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(in, 16);
        String versionLine = pbin.readLine();
        String blockDescLine = pbin.readLine();
        // debug
        //System.out.println(versionLine);
        //System.out.println(blockDescLine);
        /*
         * Check for version and parse if present.
         */
        if (versionLine != null && versionLine.length() > 0) {
            String[] versionArr = versionLine.split(" ", -1);
            if (versionArr.length != ArcConstants.VERSION_DESC_FIELDS.length) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID,
                        ArcConstants.ARC_RECORD,
                        "Invalid version description"));
            }
            /*
             * Get version and origin.
             */
            switch (versionArr.length) {
            default:
            case 3:
                originCode = versionArr[ArcConstants.FN_IDX_ORIGIN_CODE];
                originCode = fieldParsers.parseString(
                            originCode, ArcConstants.FN_ORIGIN_CODE, false);
            case 2:
                reservedStr = versionArr[ArcConstants.FN_IDX_RESERVED];
                reserved = fieldParsers.parseInteger(
                            reservedStr, ArcConstants.FN_RESERVED, false);
            case 1:
                versionNumberStr = versionArr[ArcConstants.FN_IDX_VERSION_NUMBER];
                versionNumber = fieldParsers.parseInteger(
                            versionNumberStr, ArcConstants.FN_VERSION_NUMBER, false);
            case 0:
                break;
            }
            /*
             *  Check version.
             */
            version = null;
            if (versionNumber != null && reserved != null) {
                bValidVersionFormat = true;
                versionStr = Integer.toString(versionNumber) + "." + Integer.toString(reserved);
                // Check ARC version number
                version = ArcVersion.fromValues(versionNumber.intValue(),
                        reserved.intValue());
            }
            isVersionValid = (version != null);
            if (!isVersionValid) {
                // Add validation error
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID,
                        ArcConstants.ARC_FILE,
                        "Invalid version : [version number : " + versionNumber
                        + ",reserved : " + reserved +']'));
            }
        }
        /*
         * Identify block description.
         */
        if (blockDescLine != null) {
            if (ArcConstants.VERSION_1_BLOCK_DEF.equals(blockDescLine)) {
                isValidBlockdDesc = true;
                blockDescVersion = 1;
            } else if (ArcConstants.VERSION_2_BLOCK_DEF.equals(blockDescLine)) {
                isValidBlockdDesc = true;
                blockDescVersion = 2;
            } else {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID,
                        ArcConstants.ARC_FILE,
                        "Unsupported version block definition -> "
                                + "Using version-1-block definition"));
            }
        }
        boolean bIsValidVersionBlock = (version != null) && (blockDescVersion > 0);
        if (bIsValidVersionBlock) {
            switch (blockDescVersion) {
            case 1:
                if (version != ArcVersion.VERSION_1 && version != ArcVersion.VERSION_1_1) {
                    bIsValidVersionBlock = false;
                }
                break;
            case 2:
                if (version != ArcVersion.VERSION_2) {
                    bIsValidVersionBlock = false;
                }
                break;
            }
        }
        return bIsValidVersionBlock;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("\nArcVersionHeader : [\n");
        builder.append("isValid:");
        builder.append(isValid());
        builder.append(',');
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
        builder.append(',');
        builder.append("blockDescVersion:");
        builder.append(blockDescVersion);
        builder.append("]\n");
        return builder.toString();
    }

}
