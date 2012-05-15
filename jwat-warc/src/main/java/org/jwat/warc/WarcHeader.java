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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.Digest;
import org.jwat.common.HeaderLine;
import org.jwat.common.MaxLengthRecordingInputStream;

public class WarcHeader {

    /** Associated WarcReader context.
     * Must be set prior to calling the various methods. */
    protected WarcReader reader;

    /** Diagnostics used to report diagnoses.
     * Must be set prior to calling the various methods. */
    protected Diagnostics<Diagnosis> diagnostics;

    /*
     * Version related fields.
     */

    boolean bMagicIdentified;
    boolean bVersionParsed;
    boolean bValidVersion;

    String versionStr;
    int[] versionArr;

    int major = -1;
    int minor = -1;

    protected long startOffset = -1;

    /*
     * WARC header fields.
     */

    boolean bMandatoryMissing;

    public String warcTypeStr;
    public Integer warcTypeIdx;

    // Warcinfo record only
    public String warcFilename;

    public String warcRecordIdStr;
    public URI warcRecordIdUri;

    public String warcDateStr;
    public Date warcDate;

    public String contentLengthStr;
    public Long contentLength;

    public String contentTypeStr;
    public ContentType contentType;

    public String warcTruncatedStr;
    public Integer warcTruncatedIdx;

    public String warcIpAddress;
    public InetAddress warcInetAddress;

    public List<WarcConcurrentTo> warcConcurrentToList = new LinkedList<WarcConcurrentTo>();
    //public List<String> warcConcurrentToStrList;
    //public List<URI> warcConcurrentToUriList;

    public String warcRefersToStr;
    public URI warcRefersToUri;

    public String warcTargetUriStr;
    public URI warcTargetUriUri;

    public String warcWarcinfoIdStr;
    public URI warcWarcInfoIdUri;

    public String warcBlockDigestStr;
    public Digest warcBlockDigest;

    public String warcPayloadDigestStr;
    public Digest warcPayloadDigest;

    public String warcIdentifiedPayloadTypeStr;
    public ContentType warcIdentifiedPayloadType;

    // revisit record only
    public String warcProfileStr;
    public Integer warcProfileIdx;

    public String warcSegmentNumberStr;
    public Integer warcSegmentNumber;

    // continuation record only
    public String warcSegmentOriginIdStr;
    public URI warcSegmentOriginIdUrl;

    //continuation record only
    public String warcSegmentTotalLengthStr;
    public Long warcSegmentTotalLength;

    /*
     * WARC header fields collections.
     */

    /** Raw WARC header output stream. */
    protected ByteArrayOutputStream headerBytesOut = new ByteArrayOutputStream();

    /** Raw WARC header byte array. */
    public byte[] headerBytes;

    /** List of parsed header fields. */
    protected List<HeaderLine> headerList;

    /** Map of parsed header fields. */
    protected Map<String, HeaderLine> headerMap;

    /**
     * Add an error diagnosis of the given type on a specific entity with
     * optional extra information. The information varies according to the
     * diagnosis type.
     * @param type diagnosis type
     * @param entity entity examined
     * @param information optional extra information
     */
    protected void addErrorDiagnosis(DiagnosisType type, String entity, String... information) {
        diagnostics.addError(new Diagnosis(type, entity, information));
    }

    /**
     * Add a warning diagnosis of the given type on a specific entity with
     * optional extra information. The information varies according to the
     * diagnosis type.
     * @param type diagnosis type
     * @param entity entity examined
     * @param information optional extra information
     */
    protected void addWarningDiagnosis(DiagnosisType type, String entity, String... information) {
        diagnostics.addWarning(new Diagnosis(type, entity, information));
    }

    public static WarcHeader initHeader(WarcReader reader, long startOffset, Diagnostics<Diagnosis> diagnostics) {
        WarcHeader header = new WarcHeader();
        header.reader = reader;
    	// This is only relevant for uncompressed sequentially read records
        header.startOffset = startOffset;
        header.diagnostics = diagnostics;
        return header;
    }

    public boolean parseHeader(ByteCountingPushBackInputStream in) throws IOException {
        if (parseVersion(in)) {
            // debug
            //System.out.println(wr.bMagicIdentified);
            //System.out.println(wr.bVersionParsed);
            //System.out.println(wr.major + "." + wr.minor);

            if (bVersionParsed && versionArr.length == 2) {
                switch (major) {
                case 1:
                    if (minor == 0) {
                        bValidVersion = true;
                    }
                    break;
                case 0:
                    switch (minor) {
                    case 17:
                    case 18:
                        bValidVersion = true;
                        break;
                    }
                    break;
                default:
                    break;
                }
                if (!bValidVersion) {
                    diagnostics.addError(
                            new Diagnosis(DiagnosisType.UNKNOWN,
                                    "Magic version number", versionStr));
                }
            } else {
                diagnostics.addError(
                        new Diagnosis(DiagnosisType.INVALID_DATA,
                                "Magic Version string", versionStr));
            }

            MaxLengthRecordingInputStream mrin = new MaxLengthRecordingInputStream(in, 8192);
            ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(mrin, 8192);

            parseFields(pbin);
            pbin.close();

            checkFields();

            headerBytes = headerBytesOut.toByteArray();
        }
        return bMagicIdentified;
    }

    /**
     * Looks forward in the inputstream for a valid WARC version line.
     * @param in data inputstream
     * @return true, if magic WARC header found
     * @throws IOException if an error occurs while reading version data
     */
    protected boolean parseVersion(ByteCountingPushBackInputStream in) throws IOException {
        bMagicIdentified = false;
        bVersionParsed = false;
        boolean bInvalidDataBeforeVersion = false;
        boolean bEmptyLinesBeforeVersion = false;
        HeaderLine line;
        String tmpStr;
        boolean bSeekMagic = true;
        while (bSeekMagic) {
        	// This is only relevant for uncompressed sequentially read records
            startOffset = in.getConsumed();
            line = reader.lineReader.readLine(in);
            if (!reader.lineReader.bEof) {
                switch (line.type) {
                case HeaderLine.HLT_LINE:
                    tmpStr = line.line;
                    // debug
                    //System.out.println(tmpStr);
                    if (tmpStr.length() > 0) {
                        if (tmpStr.toUpperCase().startsWith(WarcConstants.WARC_MAGIC_HEADER)) {
                            bMagicIdentified = true;
                            versionStr = tmpStr.substring(WarcConstants.WARC_MAGIC_HEADER.length());
                            String[] tmpArr = versionStr.split("\\.", -1);        // Not optimal
                            if (tmpArr.length >= 2 && tmpArr.length <= 4) {
                                bVersionParsed = true;
                                versionArr = new int[tmpArr.length];
                                for (int i=0; i<tmpArr.length; ++i) {
                                    try {
                                        versionArr[i] = Integer.parseInt(tmpArr[i]);
                                    } catch (NumberFormatException e) {
                                        versionArr[i] = -1;
                                    }
                                }
                                major = versionArr[0];
                                minor = versionArr[1];
                            }
                            headerBytesOut.write(line.raw);
                            bSeekMagic = false;
                        } else {
                            // Invalid data aka Gibberish.
                            bInvalidDataBeforeVersion = true;
                        }
                    } else {
                        // Empty line.
                        bEmptyLinesBeforeVersion = true;

                    }
                    break;
                case HeaderLine.HLT_HEADERLINE:
                    // Invalid data - header or binary.
                    bInvalidDataBeforeVersion = true;
                    break;
                }
            } else {
                // EOF.
                bSeekMagic = false;
            }
        }
        if (bInvalidDataBeforeVersion) {
            addErrorDiagnosis(DiagnosisType.INVALID, "Data before WARC version");
        }
        if (bEmptyLinesBeforeVersion) {
            addErrorDiagnosis(DiagnosisType.INVALID, "Empty lines before WARC version");
        }
        return bMagicIdentified;
    }

    /**
     * Reads WARC header lines one line at a time until an empty line is
     * encountered.
     * @param in header input stream
     * @throws IOException if an error occurs while reading the WARC header
     */
    protected void parseFields(ByteCountingPushBackInputStream in) throws IOException {
        HeaderLine headerLine;
        boolean[] seen = new boolean[WarcConstants.FN_MAX_NUMBER];
        boolean bFields = true;
        while (bFields) {
            headerLine = reader.headerLineReader.readLine(in);
            if (!reader.headerLineReader.bEof) {
                headerBytesOut.write(headerLine.raw);
                switch (headerLine.type) {
                case HeaderLine.HLT_HEADERLINE:
                    if (headerLine.name != null && headerLine.name.length() > 0) {
                        // debug
                        //System.out.println(headerLine.name);
                        //System.out.println(headerLine.value);

                        parseField(headerLine, seen);
                    } else {
                        // Empty field name.
                        addWarningDiagnosis(DiagnosisType.EMPTY, "Header line");
                    }
                    break;
                case HeaderLine.HLT_LINE:
                    if (headerLine.line.length() == 0) {
                        // Empty line.
                        bFields = false;
                    } else {
                        // Unknown header line.
                        addWarningDiagnosis(DiagnosisType.UNKNOWN, "Header line", headerLine.line);
                    }
                    break;
                // TODO handle
                /*
                case HeaderLine.HLT_RAW:
                    bFields = false;
                    // Unknown header line.
                    addWarningDiagnosis(DiagnosisType.INVALID, "Header line");
                    break;
                */
                }
            } else {
                // EOF.
                bFields = false;
            }
        }
    }

    /**
     * Identify a WARC header line and validate it accordingly.
     * @param headerLine WARC header line
     * @param seen array of headers seen so far used for duplication check
     */
    protected void parseField(HeaderLine headerLine, boolean[] seen) {
        String field = headerLine.name;
        String value = headerLine.value;
        WarcConcurrentTo warcConcurrentTo;
        Integer fn_idx = WarcConstants.fieldNameIdxMap.get(field.toLowerCase());
        if (fn_idx != null) {
            if (!seen[fn_idx] || WarcConstants.fieldNamesRepeatableLookup[fn_idx]) {
                seen[fn_idx] = true;
                switch (fn_idx.intValue()) {
                case WarcConstants.FN_IDX_WARC_TYPE:
                    warcTypeStr = reader.fieldParser.parseString(value,
                            WarcConstants.FN_WARC_TYPE);
                    if (warcTypeStr != null) {
                        warcTypeIdx = WarcConstants.recordTypeIdxMap.get(warcTypeStr.toLowerCase());
                    }
                    if (warcTypeIdx == null && warcTypeStr != null && warcTypeStr.length() > 0) {
                        warcTypeIdx = WarcConstants.RT_IDX_UNKNOWN;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_RECORD_ID:
                    warcRecordIdStr = value;
                    warcRecordIdUri = reader.fieldParser.parseUri(value,
                            WarcConstants.FN_WARC_RECORD_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_DATE:
                    warcDateStr = value;
                    warcDate = reader.fieldParser.parseDate(value,
                            WarcConstants.FN_WARC_DATE);
                    break;
                case WarcConstants.FN_IDX_CONTENT_LENGTH:
                    contentLengthStr = value;
                    contentLength = reader.fieldParser.parseLong(value,
                            WarcConstants.FN_CONTENT_LENGTH);
                    break;
                case WarcConstants.FN_IDX_CONTENT_TYPE:
                    contentTypeStr = value;
                    contentType = reader.fieldParser.parseContentType(value,
                            WarcConstants.FN_CONTENT_TYPE);
                    break;
                case WarcConstants.FN_IDX_WARC_CONCURRENT_TO:
                    URI tmpUri = reader.fieldParser.parseUri(value,
                            WarcConstants.FN_WARC_CONCURRENT_TO);
                    if (value != null && value.trim().length() > 0) {
                        if (warcConcurrentToList == null) {
                            warcConcurrentToList = new LinkedList<WarcConcurrentTo>();
                        }
                        warcConcurrentTo = new WarcConcurrentTo();
                        warcConcurrentTo.warcConcurrentToStr = value;
                        warcConcurrentTo.warcConcurrentToUri = tmpUri;
                        warcConcurrentToList.add(warcConcurrentTo);
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_BLOCK_DIGEST:
                    warcBlockDigestStr = value;
                    warcBlockDigest = reader.fieldParser.parseDigest(value,
                            WarcConstants.FN_WARC_BLOCK_DIGEST);
                    break;
                case WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST:
                    warcPayloadDigestStr = value;
                    warcPayloadDigest = reader.fieldParser.parseDigest(value,
                            WarcConstants.FN_WARC_PAYLOAD_DIGEST);
                    break;
                case WarcConstants.FN_IDX_WARC_IP_ADDRESS:
                    warcIpAddress = value;
                    warcInetAddress = reader.fieldParser.parseIpAddress(value,
                            WarcConstants.FN_WARC_IP_ADDRESS);
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO:
                    warcRefersToStr = value;
                    warcRefersToUri = reader.fieldParser.parseUri(value,
                            WarcConstants.FN_WARC_REFERS_TO);
                    break;
                case WarcConstants.FN_IDX_WARC_TARGET_URI:
                    warcTargetUriStr = value;
                    warcTargetUriUri = reader.fieldParser.parseUri(value,
                            WarcConstants.FN_WARC_TARGET_URI);
                    break;
                case WarcConstants.FN_IDX_WARC_TRUNCATED:
                    warcTruncatedStr = reader.fieldParser.parseString(value,
                            WarcConstants.FN_WARC_TRUNCATED);
                    if (warcTruncatedStr != null) {
                        warcTruncatedIdx = WarcConstants.truncatedTypeIdxMap.get(warcTruncatedStr.toLowerCase());
                    }
                    if (warcTruncatedIdx == null && warcTruncatedStr != null && warcTruncatedStr.length() > 0) {
                        warcTruncatedIdx = WarcConstants.TT_IDX_FUTURE_REASON;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_WARCINFO_ID:
                    warcWarcinfoIdStr = value;
                    warcWarcInfoIdUri = reader.fieldParser.parseUri(value,
                            WarcConstants.FN_WARC_WARCINFO_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_FILENAME:
                    warcFilename = reader.fieldParser.parseString(value,
                            WarcConstants.FN_WARC_FILENAME);
                    break;
                case WarcConstants.FN_IDX_WARC_PROFILE:
                    warcProfileStr = reader.fieldParser.parseString(value,
                            WarcConstants.FN_WARC_PROFILE);
                    if (warcProfileStr != null) {
                        warcProfileIdx = WarcConstants.profileIdxMap.get(warcProfileStr.toLowerCase());
                    }
                    if (warcProfileIdx == null && warcProfileStr != null && warcProfileStr.length() > 0) {
                        warcProfileIdx = WarcConstants.PROFILE_IDX_UNKNOWN;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE:
                    warcIdentifiedPayloadTypeStr = value;
                    warcIdentifiedPayloadType = reader.fieldParser.parseContentType(value,
                            WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID:
                    warcSegmentOriginIdStr = value;
                    warcSegmentOriginIdUrl = reader.fieldParser.parseUri(value,
                            WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER:
                    warcSegmentNumberStr = value;
                    warcSegmentNumber = reader.fieldParser.parseInteger(value,
                            WarcConstants.FN_WARC_SEGMENT_NUMBER);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH:
                    warcSegmentTotalLengthStr = value;
                    warcSegmentTotalLength = reader.fieldParser.parseLong(value,
                            WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH);
                    break;
                }
            } else {
                // Duplicate field.
                addErrorDiagnosis(DiagnosisType.DUPLICATE, "'" + field + "' header", value);
            }
        } else {
            // Not a recognized WARC field name.
            if (headerList == null) {
                headerList = new LinkedList<HeaderLine>();
            }
            if (headerMap == null) {
                headerMap = new HashMap<String, HeaderLine>();
            }
            // Uses a list because there can be multiple occurrences.
            headerList.add(headerLine);
            // Uses a map for fast lookup of single header.
            headerMap.put(field.toLowerCase(), headerLine);
        }
    }

    /**
     * Validate the WARC header relative to the WARC-Type and according the
     * WARC ISO standard.
     */
    protected void checkFields() {
        bMandatoryMissing = false;

        /*
         * Unknown Warc-Type and/or Warc-Profile.
         */

        if (warcTypeIdx != null && warcTypeIdx == WarcConstants.RT_IDX_UNKNOWN) {
            // Warning: Unknown Warc-Type.
            addWarningDiagnosis(DiagnosisType.UNKNOWN, "'" + WarcConstants.FN_WARC_TYPE + "' value", warcTypeStr);
        }

        if (warcProfileIdx != null && warcProfileIdx == WarcConstants.PROFILE_IDX_UNKNOWN) {
            // Warning: Unknown Warc-Profile.
            addWarningDiagnosis(DiagnosisType.UNKNOWN, "'" + WarcConstants.FN_WARC_PROFILE + "' value", warcProfileStr);
        }

        /*
         * Mandatory fields.
         */

        if (warcTypeIdx == null) {
            // Mandatory valid Warc-Type missing.
            addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_TYPE + "' header", warcTypeStr);
            bMandatoryMissing = true;
        }
        if (warcRecordIdUri == null) {
            // Mandatory valid Warc-Record-Id missing.
            addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_RECORD_ID + "' header", warcRecordIdStr);
            bMandatoryMissing = true;
        }
        if (warcDate == null) {
            // Mandatory valid Warc-Date missing.
            addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_DATE + "' header", warcDateStr);
            bMandatoryMissing = true;
        }
        if (contentLength == null) {
            // Mandatory valid Content-Length missing.
            addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_CONTENT_LENGTH + "' header", contentLengthStr);
            bMandatoryMissing = true;
        }

        /*
         * Content-Type should be present if Content-Length > 0.
         * Except for continuation records.
         */

        if (contentLength != null && contentLength.longValue() > 0L &&
                        (contentTypeStr == null || contentTypeStr.length() == 0)) {
            if (warcTypeIdx == null || warcTypeIdx != WarcConstants.RT_IDX_CONTINUATION) {
                addWarningDiagnosis(DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' header");
            }
        }

        /*
         * Warc record type dependent policies.
         */

        if (warcTypeIdx != null) {
            /*
             * Warcinfo record should have "application/warc-fields" content-type.
             */

            if (warcTypeIdx == WarcConstants.RT_IDX_WARCINFO) {
                // !WarcConstants.CT_APP_WARC_FIELDS.equalsIgnoreCase(contentTypeStr)) {
                if (contentType != null &&
                        (!contentType.contentType.equals("application")
                        || !contentType.mediaType.equals("warc-fields"))) {
                    addWarningDiagnosis(DiagnosisType.RECOMMENDED,
                            "'" + WarcConstants.FN_CONTENT_TYPE + "' value",
                            WarcConstants.CT_APP_WARC_FIELDS,
                            contentTypeStr);
                }
            }

            if (warcTypeIdx == WarcConstants.RT_IDX_RESPONSE) {
                if (warcSegmentNumber != null && warcSegmentNumber != 1) {
                    addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                            "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER + "' value",
                            warcSegmentNumber.toString(),
                            "1");
                }
            }

            if (warcTypeIdx == WarcConstants.RT_IDX_CONTINUATION) {
                if (warcSegmentNumber != null && warcSegmentNumber < 2) {
                    addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                            "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER + "' value",
                            warcSegmentNumber.toString(),
                            ">1");
                }
            }

            /*
             * Check the policies for each field.
             */

            WarcConcurrentTo warcConcurrentTo;
            if (warcTypeIdx  > 0) {
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_CONTENT_TYPE, contentType, contentTypeStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_IP_ADDRESS, warcInetAddress, warcIpAddress);
                for (int i=0; i<warcConcurrentToList.size(); ++i) {
                    warcConcurrentTo = warcConcurrentToList.get(0);
                    checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_CONCURRENT_TO, warcConcurrentTo.warcConcurrentToUri, warcConcurrentTo.warcConcurrentToStr);
                }
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_REFERS_TO, warcRefersToUri, warcRefersToStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_TARGET_URI, warcTargetUriUri, warcTargetUriStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_TRUNCATED, warcTruncatedIdx, warcTruncatedStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_WARCINFO_ID, warcWarcInfoIdUri, warcWarcinfoIdStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_BLOCK_DIGEST, warcBlockDigest, warcBlockDigestStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST, warcPayloadDigest, warcPayloadDigestStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_FILENAME, warcFilename, warcFilename);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_PROFILE, warcProfileIdx, warcProfileStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE, warcIdentifiedPayloadType, warcIdentifiedPayloadTypeStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER, warcSegmentNumber, warcSegmentNumberStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID, warcSegmentOriginIdUrl, warcSegmentOriginIdStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH, warcSegmentTotalLength, warcSegmentTotalLengthStr);
            }
        }
    }

    /**
     * Given a WARC record type and a WARC field looks up the policy in a
     * matrix build from the WARC ISO standard.
     * @param rtype WARC record type id
     * @param ftype WARC field type id
     * @param fieldObj WARC field
     * @param valueStr WARC raw field value
     */
    protected void checkFieldPolicy(int rtype, int ftype, Object fieldObj, String valueStr) {
        int policy = WarcConstants.field_policy[rtype][ftype];
        switch (policy) {
        case WarcConstants.POLICY_MANDATORY:
            if (fieldObj == null) {
                addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_SHALL:
            if (fieldObj == null) {
                addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_MAY:
            break;
        case WarcConstants.POLICY_MAY_NOT:
            if (fieldObj != null) {
                addWarningDiagnosis(DiagnosisType.UNDESIRED_DATA,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_SHALL_NOT:
            if (fieldObj != null) {
                addErrorDiagnosis(DiagnosisType.UNDESIRED_DATA,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_IGNORE:
        default:
            break;
        }
    }

}
