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
import java.text.DateFormat;
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
import org.jwat.common.HeaderLine;
import org.jwat.common.MaxLengthRecordingInputStream;

/**
 * Central class for working with WARC headers. This class includes support for
 * reading and writing WARC headers. Methods are also available to validate
 * individual headers and a WARC header as a whole.
 *
 * @author nicl
 */
public class WarcHeader {

    /** Associated WarcReader context.
     *  Must be set prior to calling the various methods. */
    protected WarcReader reader;

    /** Diagnostics used to report diagnoses.
     *  Must be set prior to calling the various methods. */
    protected Diagnostics<Diagnosis> diagnostics;

    /** WARC field parser used.
     *  Must be set prior to calling the various methods. */
    protected WarcFieldParsers fieldParsers;

    /** WARC <code>DateFormat</code> as specified by the WARC ISO standard. */
    protected DateFormat warcDateFormat;

    /** WARC record starting offset relative to the source WARC file input
     *  stream. The offset is correct for compressed and uncompressed streams. */
    protected long startOffset = -1;

    /*
     * Version related fields.
     */

    /** Was "WARC/" identified while looking for the version string. */
    public boolean bMagicIdentified;
    /** Did the version string include between 2 and 4 substrings delimited by ".". */
    public boolean bVersionParsed;
    /** Is the version format valid. */
    public boolean bValidVersionFormat;
    /** Is the version recognized. (0.17, 0.18 or 1.0) */
    public boolean bValidVersion;

    /** Raw version string. */
    public String versionStr;
    /** Array based on the version string split by the "." delimiter and converted to integers. */
    public int[] versionArr;

    /** Major version number from WARC header. */
    public int major = -1;
    /** Minor version number from WARC header. */
    public int minor = -1;

    /*
     * WARC header fields.
     */

    /** Array used for duplicate header detection. */
    protected boolean[] seen = new boolean[WarcConstants.FN_MAX_NUMBER];

    /** Is the header missing one of the mandatory headers. */
    public boolean bMandatoryMissing;

    /** WARC-Type field string value. */
    public String warcTypeStr;
    /** WARC-Type converted to an integer id, if identified. */
    public Integer warcTypeIdx;

    /** WARC-Filename field string value.
     *  (warcinfo record type only) */
    public String warcFilename;

    /** WARC-Record-Id field string value. */
    public String warcRecordIdStr;
    /** WARC-Record-Id converted to an <code>URI</code> object, if valid. */
    public URI warcRecordIdUri;

    /** WARC-Date field string value. */
    public String warcDateStr;
    /** WARC-Date converted to a <code>Date</code> object, if valid. */
    public Date warcDate;

    /** Content-Length field string value. */
    public String contentLengthStr;
    /** Content-Length converted to a <code>Long</code> object, if valid. */
    public Long contentLength;

    /** Content-Type field string value. */
    public String contentTypeStr;
    /** Content-Type converted to a <code>ContentType</code> object, if valid. */
    public ContentType contentType;

    /** WARC-Truncated field string value. */
    public String warcTruncatedStr;
    /** WARC-Truncated converted to an integer id, if valid. */
    public Integer warcTruncatedIdx;

    /** WARC-IP-Address field string value. */
    public String warcIpAddress;
    /** WARC-IP-Address converted to an <code>InetAddress</code> object, if valid. */
    public InetAddress warcInetAddress;

    /** List of WARC-Concurrent-To field string values and converted <code>URI</code> objects,  if valid. */
    public List<WarcConcurrentTo> warcConcurrentToList = new LinkedList<WarcConcurrentTo>();

    /** WARC-Refers-To field string value. */
    public String warcRefersToStr;
    /** WARC-Refers-To converted to an <code>URI</code> object, if valid. */
    public URI warcRefersToUri;

    /** WARC_Target-URI field string value. */
    public String warcTargetUriStr;
    /** WARC-TargetURI converted to an <code>URI</code> object, if valid. */
    public URI warcTargetUriUri;

    /** WARC-Warcinfo-Id field string value. */
    public String warcWarcinfoIdStr;
    /** WARC-Warcinfo-Id converted to an <code>URI</code> object, if valid. */
    public URI warcWarcinfoIdUri;

    /** WARC-Block-Digest field string value. */
    public String warcBlockDigestStr;
    /** WARC-Block-Digest converted to an <code>URI</code> object, if valid. */
    public WarcDigest warcBlockDigest;

    /** WARC-Payload-Digest field string value. */
    public String warcPayloadDigestStr;
    /** WARC-Payload-Digest converted to an <code>URI</code> object, if valid. */
    public WarcDigest warcPayloadDigest;

    /** WARC-Identified-Payload-Type field string value. */
    public String warcIdentifiedPayloadTypeStr;
    /** WARC-Identified-Payload-Type converted to a <code>ContentType</code> object, if valid. */
    public ContentType warcIdentifiedPayloadType;

    /** WARC-Profile field string value.
     *  (revisit record only) */
    public String warcProfileStr;
    /** WARC-Profile converted to an integer id, if valid.
     *  (revisit record only) */
    public Integer warcProfileIdx;

    /** WARC-Segment-Number field string value. */
    public String warcSegmentNumberStr;
    /** WARC-Segment-Number converted to an <code>Integer</code> object, if valid. */
    public Integer warcSegmentNumber;

    /** WARC-Segment-Origin-Id field string value.
     *  (continuation record only) */
    public String warcSegmentOriginIdStr;
    /** WARC-Segment-Origin-Id converted to an <code>URI</code> object, if valid.
     *  (continuation record only) */
    public URI warcSegmentOriginIdUrl;

    /** WARC-Segment-Total-Length field string value.
     *  (continuation record only) */
    public String warcSegmentTotalLengthStr;
    /** WARC-Segment-Total-Length converted to a <code>Long</code> object, if valid.
     *  (continuation record only) */
    public Long warcSegmentTotalLength;

    /*
     * WARC header fields collections.
     */

    /** Raw WARC header output stream. */
    protected ByteArrayOutputStream headerBytesOut = new ByteArrayOutputStream();

    /** Raw WARC header byte array. */
    public byte[] headerBytes;

    /** List of parsed header fields. */
    protected List<HeaderLine> headerList = new LinkedList<HeaderLine>();

    /** Map of parsed header fields. */
    protected Map<String, HeaderLine> headerMap = new HashMap<String, HeaderLine>();

    /**
     * Non public constructor to allow unit testing.
     */
    protected WarcHeader() {
    }

    /**
     * Create and initialize a new <code>WarcHeader</code> for writing.
     * @param writer writer which shall be used
     * @param diagnostics diagnostics object used by writer
     * @return a <code>WarcHeader</code> prepared for writing
     */
    public static WarcHeader initHeader(WarcWriter writer, Diagnostics<Diagnosis> diagnostics) {
        WarcHeader header = new WarcHeader();
        header.major = 1;
        header.minor = 0;
        header.fieldParsers = writer.fieldParsers;
        header.warcDateFormat = writer.warcDateFormat;
        header.diagnostics = diagnostics;
        return header;
    }

    /**
     * Create and initialize a new <code>WarcHeader</code> for reading.
     * @param reader reader which shall be used
     * @param startOffset
     * @param diagnostics diagnostics object used by reader
     * @return a <code>WarcHeader</code> prepared for reading
     */
    public static WarcHeader initHeader(WarcReader reader, long startOffset, Diagnostics<Diagnosis> diagnostics) {
        WarcHeader header = new WarcHeader();
        header.reader = reader;
        header.fieldParsers = reader.fieldParsers;
        header.diagnostics = diagnostics;
        // This is only relevant for uncompressed sequentially read records
        header.startOffset = startOffset;
        return header;
    }

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

    /**
     * Try to parse a WARC header and return a boolean indicating the success or
     * failure of this.
     * @param in input stream with WARC data
     * @return boolean indicating whether a header was parsed or not
     * @throws IOException if an i/o exception occurs while parsing for a header
     */
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

            MaxLengthRecordingInputStream mrin = new MaxLengthRecordingInputStream(in, reader.warcHeaderMaxSize);
            ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(mrin, reader.warcHeaderMaxSize);

            parseHeaders(pbin);
            pbin.close();

            checkFields();

            headerBytes = headerBytesOut.toByteArray();
        }
        return bMagicIdentified;
    }

    /**
     * Looks forward in the input stream for a valid WARC version line.
     * @param in data input stream
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
                                bValidVersionFormat = true;
                                versionArr = new int[tmpArr.length];
                                for (int i=0; i<tmpArr.length; ++i) {
                                    try {
                                        versionArr[i] = Integer.parseInt(tmpArr[i]);
                                    } catch (NumberFormatException e) {
                                        versionArr[i] = -1;
                                        bValidVersionFormat = false;
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
    protected void parseHeaders(ByteCountingPushBackInputStream in) throws IOException {
        HeaderLine headerLine;
        boolean bLoop = true;
        while (bLoop) {
            headerLine = reader.headerLineReader.readLine(in);
            if (!reader.headerLineReader.bEof) {
                headerBytesOut.write(headerLine.raw);
                switch (headerLine.type) {
                case HeaderLine.HLT_HEADERLINE:
                    if (headerLine.name != null && headerLine.name.length() > 0) {
                        // debug
                        //System.out.println(headerLine.name);
                        //System.out.println(headerLine.value);
                        addHeader(headerLine);
                    } else {
                        // Empty field name.
                        addWarningDiagnosis(DiagnosisType.EMPTY, "Header line");
                    }
                    break;
                case HeaderLine.HLT_LINE:
                    if (headerLine.line.length() == 0) {
                        // Empty line.
                        bLoop = false;
                    } else {
                        // Unknown header line.
                        addWarningDiagnosis(DiagnosisType.UNKNOWN, "Header line", headerLine.line);
                    }
                    break;
                default:
                    throw new IllegalStateException("Invalid HeaderLine output!");
                }
            } else {
                // EOF.
                bLoop = false;
            }
        }
    }

    /**
     * Identify a (WARC) header name, validate the value and set the header.
     * @param fieldName header name
     * @param fieldValue header value
     * @param seen array of headers seen so far used for duplication check
     */
    protected void addHeader(HeaderLine headerLine) {
        String fieldName = headerLine.name;
        String fieldValue = headerLine.value;
        WarcConcurrentTo warcConcurrentTo;
        Integer fn_idx = WarcConstants.fieldNameIdxMap.get(fieldName.toLowerCase());
        if (fn_idx != null) {
            // Recognized WARC field name.
            if (!seen[fn_idx] || WarcConstants.fieldNamesRepeatableLookup[fn_idx]) {
                seen[fn_idx] = true;
                switch (fn_idx.intValue()) {
                case WarcConstants.FN_IDX_WARC_TYPE:
                    warcTypeStr = fieldParsers.parseString(fieldValue,
                            WarcConstants.FN_WARC_TYPE);
                    if (warcTypeStr != null) {
                        warcTypeIdx = WarcConstants.recordTypeIdxMap.get(warcTypeStr.toLowerCase());
                    }
                    if (warcTypeIdx == null && warcTypeStr != null && warcTypeStr.length() > 0) {
                        warcTypeIdx = WarcConstants.RT_IDX_UNKNOWN;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_RECORD_ID:
                    warcRecordIdStr = fieldValue;
                    warcRecordIdUri = fieldParsers.parseUri(fieldValue,
                            WarcConstants.FN_WARC_RECORD_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_DATE:
                    warcDateStr = fieldValue;
                    warcDate = fieldParsers.parseDate(fieldValue,
                            WarcConstants.FN_WARC_DATE);
                    break;
                case WarcConstants.FN_IDX_CONTENT_LENGTH:
                    contentLengthStr = fieldValue;
                    contentLength = fieldParsers.parseLong(fieldValue,
                            WarcConstants.FN_CONTENT_LENGTH);
                    break;
                case WarcConstants.FN_IDX_CONTENT_TYPE:
                    contentTypeStr = fieldValue;
                    contentType = fieldParsers.parseContentType(fieldValue,
                            WarcConstants.FN_CONTENT_TYPE);
                    break;
                case WarcConstants.FN_IDX_WARC_CONCURRENT_TO:
                    URI tmpUri = fieldParsers.parseUri(fieldValue,
                            WarcConstants.FN_WARC_CONCURRENT_TO);
                    if (fieldValue != null && fieldValue.trim().length() > 0) {
                        warcConcurrentTo = new WarcConcurrentTo();
                        warcConcurrentTo.warcConcurrentToStr = fieldValue;
                        warcConcurrentTo.warcConcurrentToUri = tmpUri;
                        warcConcurrentToList.add(warcConcurrentTo);
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_BLOCK_DIGEST:
                    warcBlockDigestStr = fieldValue;
                    warcBlockDigest = fieldParsers.parseDigest(fieldValue,
                            WarcConstants.FN_WARC_BLOCK_DIGEST);
                    break;
                case WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST:
                    warcPayloadDigestStr = fieldValue;
                    warcPayloadDigest = fieldParsers.parseDigest(fieldValue,
                            WarcConstants.FN_WARC_PAYLOAD_DIGEST);
                    break;
                case WarcConstants.FN_IDX_WARC_IP_ADDRESS:
                    warcIpAddress = fieldValue;
                    warcInetAddress = fieldParsers.parseIpAddress(fieldValue,
                            WarcConstants.FN_WARC_IP_ADDRESS);
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO:
                    warcRefersToStr = fieldValue;
                    warcRefersToUri = fieldParsers.parseUri(fieldValue,
                            WarcConstants.FN_WARC_REFERS_TO);
                    break;
                case WarcConstants.FN_IDX_WARC_TARGET_URI:
                    warcTargetUriStr = fieldValue;
                    warcTargetUriUri = fieldParsers.parseUri(fieldValue,
                            WarcConstants.FN_WARC_TARGET_URI);
                    break;
                case WarcConstants.FN_IDX_WARC_TRUNCATED:
                    warcTruncatedStr = fieldParsers.parseString(fieldValue,
                            WarcConstants.FN_WARC_TRUNCATED);
                    if (warcTruncatedStr != null) {
                        warcTruncatedIdx = WarcConstants.truncatedTypeIdxMap.get(warcTruncatedStr.toLowerCase());
                    }
                    if (warcTruncatedIdx == null && warcTruncatedStr != null && warcTruncatedStr.length() > 0) {
                        warcTruncatedIdx = WarcConstants.TT_IDX_FUTURE_REASON;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_WARCINFO_ID:
                    warcWarcinfoIdStr = fieldValue;
                    warcWarcinfoIdUri = fieldParsers.parseUri(fieldValue,
                            WarcConstants.FN_WARC_WARCINFO_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_FILENAME:
                    warcFilename = fieldParsers.parseString(fieldValue,
                            WarcConstants.FN_WARC_FILENAME);
                    break;
                case WarcConstants.FN_IDX_WARC_PROFILE:
                    warcProfileStr = fieldParsers.parseString(fieldValue,
                            WarcConstants.FN_WARC_PROFILE);
                    if (warcProfileStr != null) {
                        warcProfileIdx = WarcConstants.profileIdxMap.get(warcProfileStr.toLowerCase());
                    }
                    if (warcProfileIdx == null && warcProfileStr != null && warcProfileStr.length() > 0) {
                        warcProfileIdx = WarcConstants.PROFILE_IDX_UNKNOWN;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE:
                    warcIdentifiedPayloadTypeStr = fieldValue;
                    warcIdentifiedPayloadType = fieldParsers.parseContentType(fieldValue,
                            WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID:
                    warcSegmentOriginIdStr = fieldValue;
                    warcSegmentOriginIdUrl = fieldParsers.parseUri(fieldValue,
                            WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER:
                    warcSegmentNumberStr = fieldValue;
                    warcSegmentNumber = fieldParsers.parseInteger(fieldValue,
                            WarcConstants.FN_WARC_SEGMENT_NUMBER);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH:
                    warcSegmentTotalLengthStr = fieldValue;
                    warcSegmentTotalLength = fieldParsers.parseLong(fieldValue,
                            WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH);
                    break;
                }
            } else {
                // Duplicate field.
                addErrorDiagnosis(DiagnosisType.DUPLICATE, "'" + fieldName + "' header", fieldValue);
            }
        }
        HeaderLine tmpLine = headerMap.get(fieldName.toLowerCase());
        if (tmpLine == null) {
            headerMap.put(fieldName.toLowerCase(), headerLine);
        } else {
            tmpLine.lines.add(headerLine);
        }
        headerList.add(headerLine);
    }

    /**
     * Add a String header using the supplied string and return a
     * <code>HeaderLine</code> object corresponding to how the header would be
     * read.
     * @param fieldName name of field to add
     * @param fieldValue field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, String fieldValue) {
        HeaderLine headerLine = new HeaderLine();
        headerLine.name = fieldName;
        headerLine.value = fieldValue;
        addHeader(headerLine);
        return headerLine;
    }

    /**
     * Add an Integer header using the supplied string and object values and return
     * a <code>HeaderLine</code> object corresponding to how the header would be read.
     * If both string and object values are not null they are used as is.
     * If the string value is null and the object is not null,
     * the object's toString method is called.
     * If the object is null and the string is not null, the string is parsed
     * and validated resulting in an object, if valid. 
     * @param fieldName name of field to add
     * @param uriFieldValue <code>Integer</code> field value object
     * @param fieldValueStr Integer field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, Integer integerFieldValue, String fieldValueStr) {
        if (integerFieldValue == null && fieldValueStr != null) {
            integerFieldValue = fieldParsers.parseInteger(fieldValueStr, fieldName);
        } else if (fieldValueStr == null && integerFieldValue != null) {
            fieldValueStr = integerFieldValue.toString();
        }
        return addHeader(fieldName, fieldValueStr, WarcConstants.FDT_INTEGER,
                integerFieldValue, null, null, null, null, null, null);
    }

    /**
     * Add a Long header using the supplied string and object values and return
     * a <code>HeaderLine</code> object corresponding to how the header would be read.
     * If both string and object values are not null they are used as is.
     * If the string value is null and the object is not null,
     * the object's toString method is called.
     * If the object is null and the string is not null, the string is parsed
     * and validated resulting in an object, if valid. 
     * @param fieldName name of field to add
     * @param uriFieldValue <code>Long</code> field value object
     * @param fieldValueStr Long field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, Long longFieldValue, String fieldValueStr) {
        if (longFieldValue == null && fieldValueStr != null) {
            longFieldValue = fieldParsers.parseLong(fieldValueStr, fieldName);
        } else if (fieldValueStr == null && longFieldValue != null) {
            fieldValueStr = longFieldValue.toString();
        }
        return addHeader(fieldName, fieldValueStr, WarcConstants.FDT_LONG,
                null, longFieldValue, null, null, null, null, null);
    }

    /**
     * Add an Digest header using the supplied string and object values and return
     * a <code>HeaderLine</code> object corresponding to how the header would be read.
     * If both string and object values are not null they are used as is.
     * If the string value is null and the object is not null,
     * the object's toString method is called.
     * If the object is null and the string is not null, the string is parsed
     * and validated resulting in an object, if valid. 
     * @param fieldName name of field to add
     * @param uriFieldValue <code>Digest</code> field value object
     * @param fieldValueStr Digest field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, WarcDigest digestFieldValue, String fieldValueStr) {
        if (digestFieldValue == null && fieldValueStr != null) {
            digestFieldValue = fieldParsers.parseDigest(fieldValueStr, fieldName);
        } else if (fieldValueStr == null && digestFieldValue != null) {
            fieldValueStr = digestFieldValue.toString();
        }
        return addHeader(fieldName, fieldValueStr, WarcConstants.FDT_DIGEST,
                null, null, digestFieldValue, null, null, null, null);
    }

    /**
     * Add an Content-Type header using the supplied string and object values and return
     * a <code>HeaderLine</code> object corresponding to how the header would be read.
     * If both string and object values are not null they are used as is.
     * If the string value is null and the object is not null,
     * the object's toString method is called.
     * If the object is null and the string is not null, the string is parsed
     * and validated resulting in an object, if valid. 
     * @param fieldName name of field to add
     * @param uriFieldValue <code>ContentType</code> field value object
     * @param fieldValueStr Content-Type field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, ContentType contentTypeFieldValue, String fieldValueStr) {
        if (contentTypeFieldValue == null && fieldValueStr != null) {
            contentTypeFieldValue = fieldParsers.parseContentType(fieldValueStr, fieldName);
        } else if (fieldValueStr == null && contentTypeFieldValue != null) {
            fieldValueStr = contentTypeFieldValue.toString();
        }
        return addHeader(fieldName, fieldValueStr, WarcConstants.FDT_CONTENTTYPE,
                null, null, null, contentTypeFieldValue, null, null, null);
    }

    /**
     * Add an Date header using the supplied string and object values and return
     * a <code>HeaderLine</code> object corresponding to how the header would be read.
     * If both string and object values are not null they are used as is.
     * If the string value is null and the object is not null,
     * the object's toString method is called.
     * If the object is null and the string is not null, the string is parsed
     * and validated resulting in an object, if valid. 
     * @param fieldName name of field to add
     * @param uriFieldValue <code>Date</code> field value object
     * @param fieldValueStr Date field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, Date dateFieldValue, String fieldValueStr) {
        if (dateFieldValue == null && fieldValueStr != null) {
            dateFieldValue = fieldParsers.parseDate(fieldValueStr, fieldName);
        } else if (fieldValueStr == null && dateFieldValue != null) {
            fieldValueStr = warcDateFormat.format(dateFieldValue);
        }
        return addHeader(fieldName, fieldValueStr, WarcConstants.FDT_DATE,
                null, null, null, null, dateFieldValue, null, null);
    }

    /**
     * Add an InetAddress header using the supplied string and object values and return
     * a <code>HeaderLine</code> object corresponding to how the header would be read.
     * If both string and object values are not null they are used as is.
     * If the string value is null and the object is not null,
     * the object's toString method is called.
     * If the object is null and the string is not null, the string is parsed
     * and validated resulting in an object, if valid. 
     * @param fieldName name of field to add
     * @param uriFieldValue <code>InetAddress</code> field value object
     * @param fieldValueStr IP-Address field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, InetAddress inetAddrFieldValue, String fieldValueStr) {
        if (inetAddrFieldValue == null && fieldValueStr != null) {
            inetAddrFieldValue = fieldParsers.parseIpAddress(fieldValueStr, fieldName);
        } else if (fieldValueStr == null && inetAddrFieldValue != null) {
            fieldValueStr = inetAddrFieldValue.getHostAddress();
        }
        return addHeader(fieldName, fieldValueStr, WarcConstants.FDT_INETADDRESS,
                null, null, null, null, null, inetAddrFieldValue, null);
    }

    /**
     * Add an URI header using the supplied string and object values and return
     * a <code>HeaderLine</code> object corresponding to how the header would be read.
     * If both string and object values are not null they are used as is.
     * If the string value is null and the object is not null,
     * the object's toString method is called.
     * If the object is null and the string is not null, the string is parsed
     * and validated resulting in an object, if valid. 
     * @param fieldName name of field to add
     * @param uriFieldValue <code>URI</code> field value object
     * @param fieldValueStr URI field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, URI uriFieldValue, String fieldValueStr) {
        if (uriFieldValue == null && fieldValueStr != null) {
            uriFieldValue = fieldParsers.parseUri(fieldValueStr, fieldName);
        } else if (fieldValueStr == null && uriFieldValue != null) {
            fieldValueStr = uriFieldValue.toString();
        }
        return addHeader(fieldName, fieldValueStr, WarcConstants.FDT_URI,
                null, null, null, null, null, null, uriFieldValue);
    }

    /**
     * Add a header with the supplied field name, data type and value and
     * return a <code>HeaderLine</code> corresponding to how the header will
     * be read. The data type is validated against the field data type.
     * The values used are the field value string and the parameter
     * corresponding to the data type.
     * @param fieldName header field name
     * @param fieldValueStr field value in string form
     * @param dt data type of the field value string when converted to an object
     * @param integerFieldValue <code>Integer</code> object field value
     * @param longFieldValue <code>Long</code> object field value
     * @param digestFieldValue <code>Digest</code> object field value
     * @param contentTypeFieldValue <code>ContentType</code> object field value
     * @param dateFieldValue <code>Date</code> object field value
     * @param inetAddrFieldValue <code>InetAddress</code> object field value
     * @param uriFieldValue <code>URI</code> object field value
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, String fieldValueStr, int dt,
            Integer integerFieldValue, Long longFieldValue,
            WarcDigest digestFieldValue, ContentType contentTypeFieldValue,
            Date dateFieldValue, InetAddress inetAddrFieldValue,
            URI uriFieldValue) {
        Integer fn_idx = WarcConstants.fieldNameIdxMap.get(fieldName.toLowerCase());
        if (fn_idx != null) {
            if (WarcConstants.FN_IDX_DT[fn_idx] == WarcConstants.FDT_LONG
                    && dt == WarcConstants.FDT_INTEGER) {
                longFieldValue = (long)integerFieldValue;
                dt = WarcConstants.FDT_LONG;
            }
            if (dt == WarcConstants.FN_IDX_DT[fn_idx]) {
                // Recognized WARC field name.
                if (seen[fn_idx] && !WarcConstants.fieldNamesRepeatableLookup[fn_idx]) {
                    // Duplicate field.
                    addErrorDiagnosis(DiagnosisType.DUPLICATE,
                            "'" + fieldName + "' header",
                            fieldValueStr);
                }
                seen[fn_idx] = true;
                switch (fn_idx.intValue()) {
                /*
                 * Integer.
                 */
                case WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER:
                    warcSegmentNumberStr = fieldValueStr;
                    warcSegmentNumber = integerFieldValue;
                    break;
                /*
                 * Long.
                 */
                case WarcConstants.FN_IDX_CONTENT_LENGTH:
                    contentLengthStr = fieldValueStr;
                    contentLength = longFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH:
                    warcSegmentTotalLengthStr = fieldValueStr;
                    warcSegmentTotalLength = longFieldValue;
                    break;
                /*
                 * Digest.
                 */
                case WarcConstants.FN_IDX_WARC_BLOCK_DIGEST:
                    warcBlockDigestStr = fieldValueStr;
                    warcBlockDigest = digestFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST:
                    warcPayloadDigestStr = fieldValueStr;
                    warcPayloadDigest = digestFieldValue;
                    break;
                /*
                 * ContentType.
                 */
                case WarcConstants.FN_IDX_CONTENT_TYPE:
                    contentTypeStr = fieldValueStr;
                    contentType = contentTypeFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE:
                    warcIdentifiedPayloadTypeStr = fieldValueStr;
                    warcIdentifiedPayloadType = contentTypeFieldValue;
                    break;
                /*
                 * Date.
                 */
                case WarcConstants.FN_IDX_WARC_DATE:
                    warcDateStr = fieldValueStr;
                    warcDate = dateFieldValue;
                    break;
                /*
                 * InetAddress.
                 */
                case WarcConstants.FN_IDX_WARC_IP_ADDRESS:
                    warcIpAddress = fieldValueStr;
                    warcInetAddress = inetAddrFieldValue;
                    break;
                /*
                 * URI.
                 */
                case WarcConstants.FN_IDX_WARC_RECORD_ID:
                    warcRecordIdStr = fieldValueStr;
                    warcRecordIdUri = uriFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_CONCURRENT_TO:
                    if (fieldValueStr != null || uriFieldValue != null) {
                        WarcConcurrentTo warcConcurrentTo = new WarcConcurrentTo();
                        warcConcurrentTo.warcConcurrentToStr = fieldValueStr;
                        warcConcurrentTo.warcConcurrentToUri = uriFieldValue;
                        warcConcurrentToList.add(warcConcurrentTo);
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO:
                    warcRefersToStr = fieldValueStr;
                    warcRefersToUri = uriFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_TARGET_URI:
                    warcTargetUriStr = fieldValueStr;
                    warcTargetUriUri = uriFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_WARCINFO_ID:
                    warcWarcinfoIdStr = fieldValueStr;
                    warcWarcinfoIdUri = uriFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID:
                    warcSegmentOriginIdStr = fieldValueStr;
                    warcSegmentOriginIdUrl = uriFieldValue;
                    break;
                default:
                    break;
                }
            } else {
                // Invalid datatype for field.
                addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                        "Invalid datatype for '" + fieldName + "' header",
                        WarcConstants.FDT_IDX_STRINGS[WarcConstants.FN_IDX_DT[fn_idx]],
                        WarcConstants.FDT_IDX_STRINGS[dt]);
                // Consider throwing exception at some point.
            }
        }
        HeaderLine headerLine = new HeaderLine();
        headerLine.name = fieldName;
        headerLine.value = fieldValueStr;
        HeaderLine tmpLine = headerMap.get(fieldName.toLowerCase());
        if (tmpLine == null) {
            headerMap.put(fieldName.toLowerCase(), headerLine);
        } else {
            tmpLine.lines.add(headerLine);
        }
        return headerLine;
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
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_WARCINFO_ID, warcWarcinfoIdUri, warcWarcinfoIdStr);
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
        case WarcConstants.POLICY_SHALL_NOT:
            if (fieldObj != null) {
                addErrorDiagnosis(DiagnosisType.UNDESIRED_DATA,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_MAY_NOT:
            if (fieldObj != null) {
                addWarningDiagnosis(DiagnosisType.UNDESIRED_DATA,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_MAY:
        case WarcConstants.POLICY_IGNORE:
        default:
            break;
        }
    }

}
