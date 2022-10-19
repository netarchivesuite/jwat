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
package org.jwat.warc;

import java.net.InetAddress;
import java.util.List;

import org.jwat.common.ContentType;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.Uri;

/**
 * WARC validator abstract base class. Has some common fields and methods.
 *
 * @author nicl
 */
public abstract class WarcValidatorBase extends WarcConstants {

    /** Version denoted as an integer array. */
    public int[] usedOnVersion;

    /** A (Warc-Types x Warc-Header-Fields) matrix used for policy validation. */
    protected int[][] field_policy;

    /**
     * Internal constructor.
     */
    protected WarcValidatorBase() {
    }

    /**
     * Set the version this validator is used on. Differs from the internal version
     * if it is used on WARC version that are not yet supported.
     * @param arr integer array denoting a version
     */
    public void setVersions(int [] arr) {
        if (arr != null) {
            usedOnVersion = new int[arr.length];
            System.arraycopy(arr, 0, usedOnVersion, 0, arr.length);
        }
    }

    /**
     * The following section initializes the policy matrix used to check the
     * usage of each known warc header line against each known warc record
     * type.
     * The ISO standard was used to build the data in the matrix.
     */
    public abstract void setWarcHeaderPolicyMatrix();

    /**
     * Validate the WARC header relative to the WARC-Type and according to the
     * WARC ISO standard.
     */
    protected void checkFields(WarcHeader header) {
        Diagnostics diagnostics = header.diagnostics;

        String entityStr;
        String infoStr;
        Integer warcTypeIdx = header.warcTypeIdx;
        String warcTypeStr = header.warcTypeStr;
        Integer warcProfileIdx = header.warcProfileIdx;
        Uri warcProfileUri = header.warcProfileUri;
        String warcProfileStr = header.warcProfileStr;
        Uri warcRecordIdUri = header.warcRecordIdUri;
        String warcRecordIdStr = header.warcRecordIdStr;
        WarcDate warcDate = header.warcDate;
        String warcDateStr = header.warcDateStr;
        Long contentLength = header.contentLength;
        String contentLengthStr = header.contentLengthStr;
        ContentType contentType = header.contentType;
        String contentTypeStr = header.contentTypeStr;
        Integer warcSegmentNumber = header.warcSegmentNumber;
        String warcSegmentNumberStr = header.warcSegmentNumberStr;

        boolean bMandatoryMissing = false;

        /*
         * Unknown Warc-Type and/or Warc-Profile.
         */

        if (warcTypeIdx != null && warcTypeIdx == RT_IDX_UNKNOWN) {
            // Warning: Unknown Warc-Type.
            entityStr = "'" + FN_WARC_TYPE + "' value";
            diagnostics.addWarning(DiagnosisType.UNKNOWN, entityStr, warcTypeStr);
        }

        if (warcProfileIdx != null) {
            if (warcProfileIdx == WARC_PROFILE_IDX_UNKNOWN) {
                // Warning: Unknown Warc-Profile.
                entityStr = "'" + FN_WARC_PROFILE + "' value";
                diagnostics.addWarning(DiagnosisType.UNKNOWN, entityStr, warcProfileStr);
            }
            else {
                switch (warcProfileIdx) {
                case WARC10_PROFILE_IDX_IDENTICAL_PAYLOAD_DIGEST:
                case WARC10_PROFILE_IDX_SERVER_NOT_MODIFIED:
                    if (header.major != 1 || header.minor != 0) {
                        // Warning: Unknown Warc-Profile.
                        entityStr = "'" + FN_WARC_PROFILE + "' value";
                        infoStr = "WARC/" + header.major + "." + header.minor + " profile";
                        diagnostics.addWarning(DiagnosisType.ERROR_EXPECTED, entityStr, infoStr);
                    }
                    break;
                case WARC11_PROFILE_IDX_IDENTICAL_PAYLOAD_DIGEST:
                case WARC11_PROFILE_IDX_SERVER_NOT_MODIFIED:
                    if (header.major != 1 || header.minor != 1) {
                        // Warning: Unknown Warc-Profile.
                        entityStr = "'" + FN_WARC_PROFILE + "' value";
                        infoStr = "WARC/" + header.major + "." + header.minor + " profile";
                        diagnostics.addWarning(DiagnosisType.ERROR_EXPECTED, entityStr, infoStr);
                    }
                    break;
                default:
                    break;
                }
            }
        }

        /*
         * Mandatory fields.
         */

        // TODO Required yes, but is it always invalid.
        if (warcTypeIdx == null) {
            // Mandatory valid Warc-Type missing.
            entityStr = "'" + FN_WARC_TYPE + "' header";
            diagnostics.addError(DiagnosisType.REQUIRED_INVALID, entityStr, warcTypeStr);
            bMandatoryMissing = true;
        }
        if (warcRecordIdUri == null) {
            // Mandatory valid Warc-Record-Id missing.
            entityStr = "'" + FN_WARC_RECORD_ID + "' header";
            diagnostics.addError(DiagnosisType.REQUIRED_INVALID, entityStr, warcRecordIdStr);
            bMandatoryMissing = true;
        }
        if (warcDate == null) {
            // Mandatory valid Warc-Date missing.
            entityStr = "'" + FN_WARC_DATE + "' header";
            diagnostics.addError(DiagnosisType.REQUIRED_INVALID, entityStr, warcDateStr);
            bMandatoryMissing = true;
        }
        if (contentLength == null) {
            // Mandatory valid Content-Length missing.
            entityStr = "'" + FN_CONTENT_LENGTH + "' header";
            diagnostics.addError(DiagnosisType.REQUIRED_INVALID, entityStr, contentLengthStr);
            bMandatoryMissing = true;
        }

        if (warcDate != null) {
            if (header.major == 1 && header.minor == 0 && warcDate.precision != WarcDate.P_SECOND) {
                diagnostics.addError(DiagnosisType.ERROR_EXPECTED, "'" + FN_WARC_DATE + "' value", warcDateStr, WarcConstants.WARC_DATE_FORMAT);
            }
        }

        header.bMandatoryMissing = bMandatoryMissing;

        /*
         * Content-Type should be present if Content-Length > 0.
         * Except for continuation records.
         */

        if (contentLength != null && contentLength.longValue() > 0L &&
                        (contentTypeStr == null || contentTypeStr.length() == 0)) {
            if (warcTypeIdx == null || warcTypeIdx != WarcConstants.RT_IDX_CONTINUATION) {
                entityStr = "'" + FN_CONTENT_TYPE + "' header";
                diagnostics.addWarning(DiagnosisType.RECOMMENDED_MISSING, entityStr);
            }
        }

        /*
         * WARC record type dependent policies.
         */

        if (warcTypeIdx != null) {
            /*
             * Warcinfo record should have "application/warc-fields" content-type.
             */

            if (warcTypeIdx == WarcConstants.RT_IDX_WARCINFO) {
                if (contentType != null &&
                        (!contentType.contentType.equals("application")
                        || !contentType.mediaType.equals("warc-fields"))) {
                    entityStr = "'" + FN_CONTENT_TYPE + "' value";
                    diagnostics.addWarning(DiagnosisType.RECOMMENDED, entityStr, WarcConstants.CT_APP_WARC_FIELDS, contentTypeStr);
                }
            }

            if (warcTypeIdx == WarcConstants.RT_IDX_RESPONSE) {
                if (warcSegmentNumber != null && warcSegmentNumber != 1) {
                    entityStr = "'" + FN_WARC_SEGMENT_NUMBER + "' value";
                    diagnostics.addError(DiagnosisType.INVALID_EXPECTED, entityStr, warcSegmentNumber.toString(), "1");
                }
            }

            if (warcTypeIdx == WarcConstants.RT_IDX_CONTINUATION) {
                if (warcSegmentNumber != null && warcSegmentNumber < 2) {
                    entityStr = "'" + FN_WARC_SEGMENT_NUMBER + "' value";
                    diagnostics.addError(DiagnosisType.INVALID_EXPECTED, entityStr, warcSegmentNumber.toString(), ">1");
                }
            }

            /*
             * Check the policies for each field.
             */

            InetAddress warcInetAddress = header.warcInetAddress;
            String warcIpAddress = header.warcIpAddress;
            List<WarcConcurrentTo> warcConcurrentToList = header.warcConcurrentToList;
            Uri warcRefersToUri = header.warcRefersToUri;
            String warcRefersToStr = header.warcRefersToStr;
            Uri warcTargetUriUri = header.warcTargetUriUri;
            String warcTargetUriStr = header.warcTargetUriStr;
            Integer warcTruncatedIdx = header.warcTruncatedIdx;
            String warcTruncatedStr = header.warcTruncatedStr;
            Uri warcWarcinfoIdUri = header.warcWarcinfoIdUri;
            String warcWarcinfoIdStr = header.warcWarcinfoIdStr;
            WarcDigest warcBlockDigest = header.warcBlockDigest;
            String warcBlockDigestStr = header.warcBlockDigestStr;
            WarcDigest warcPayloadDigest = header.warcPayloadDigest;
            String warcPayloadDigestStr = header.warcPayloadDigestStr;
            String warcFilename = header.warcFilename;
            ContentType warcIdentifiedPayloadType = header.warcIdentifiedPayloadType;
            String warcIdentifiedPayloadTypeStr = header.warcIdentifiedPayloadTypeStr;
            Uri warcSegmentOriginIdUrl = header.warcSegmentOriginIdUrl;
            String warcSegmentOriginIdStr = header.warcSegmentOriginIdStr;
            Long warcSegmentTotalLength = header.warcSegmentTotalLength;
            String warcSegmentTotalLengthStr = header.warcSegmentTotalLengthStr;
            Uri warcRefersToTargetUriUri = header.warcRefersToTargetUriUri;
            String warcRefersToTargetUriStr = header.warcRefersToTargetUriStr;
            WarcDate warcRefersToDate = header.warcRefersToDate;
            String warcRefersToDateStr = header.warcRefersToDateStr;

            WarcConcurrentTo warcConcurrentTo;
            if (warcTypeIdx  > 0) {
                checkFieldPolicy(warcTypeIdx, FN_IDX_CONTENT_TYPE, contentType, contentTypeStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_IP_ADDRESS, warcInetAddress, warcIpAddress, diagnostics);
                for (int i=0; i<warcConcurrentToList.size(); ++i) {
                    warcConcurrentTo = warcConcurrentToList.get(0);
                    checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_CONCURRENT_TO, warcConcurrentTo.warcConcurrentToUri, warcConcurrentTo.warcConcurrentToStr, diagnostics);
                }
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_REFERS_TO, warcRefersToUri, warcRefersToStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_TARGET_URI, warcTargetUriUri, warcTargetUriStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_TRUNCATED, warcTruncatedIdx, warcTruncatedStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_WARCINFO_ID, warcWarcinfoIdUri, warcWarcinfoIdStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_BLOCK_DIGEST, warcBlockDigest, warcBlockDigestStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_PAYLOAD_DIGEST, warcPayloadDigest, warcPayloadDigestStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_FILENAME, warcFilename, warcFilename, diagnostics);
                // Could also use warcProfileIdx for really strict.
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_PROFILE, warcProfileUri, warcProfileStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE, warcIdentifiedPayloadType, warcIdentifiedPayloadTypeStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_SEGMENT_NUMBER, warcSegmentNumber, warcSegmentNumberStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_SEGMENT_ORIGIN_ID, warcSegmentOriginIdUrl, warcSegmentOriginIdStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_SEGMENT_TOTAL_LENGTH, warcSegmentTotalLength, warcSegmentTotalLengthStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_REFERS_TO_TARGET_URI, warcRefersToTargetUriUri, warcRefersToTargetUriStr, diagnostics);
                checkFieldPolicy(warcTypeIdx, FN_IDX_WARC_REFERS_TO_DATE, warcRefersToDate, warcRefersToDateStr, diagnostics);
            }
        }
    }

    /**
     * Given a WARC record type and a WARC field looks up the policy in a
     * matrix build from the WARC ISO standard.
     * @param recordType WARC record type id
     * @param fieldType WARC field type id
     * @param fieldObj WARC field
     * @param valueStr WARC raw field value
     */
    public void checkFieldPolicy(int recordType, int fieldType, Object fieldObj, String valueStr, Diagnostics diagnostics) {
        int policy = field_policy[recordType][fieldType];
        String entityStr;
        switch (policy) {
        case POLICY_MANDATORY:
            if (fieldObj == null) {
                entityStr = "'" + FN_IDX_STRINGS[fieldType] + "' value";
                diagnostics.addError(DiagnosisType.REQUIRED_INVALID, entityStr, valueStr);
            }
            break;
        case POLICY_SHALL:
            if (fieldObj == null) {
                entityStr = "'" + FN_IDX_STRINGS[fieldType] + "' value";
                diagnostics.addError(DiagnosisType.REQUIRED_INVALID, entityStr, valueStr);
            }
            break;
        case POLICY_SHALL_NOT:
            if (fieldObj != null) {
                entityStr = "'" + FN_IDX_STRINGS[fieldType] + "' value";
                diagnostics.addError(DiagnosisType.UNDESIRED_DATA, entityStr, valueStr);
            }
            break;
        case POLICY_MAY_NOT:
            if (fieldObj != null) {
                entityStr = "'" + FN_IDX_STRINGS[fieldType] + "' value";
                diagnostics.addWarning(DiagnosisType.UNDESIRED_DATA, entityStr, valueStr);
            }
            break;
        case POLICY_MAY:
        case POLICY_IGNORE:
        default:
            break;
        }
    }

}
