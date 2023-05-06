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
import java.util.Collections;
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
import org.jwat.common.HeaderLineReader;
import org.jwat.common.MaxLengthRecordingInputStream;
import org.jwat.common.Uri;
import org.jwat.common.UriProfile;

/**
 * Central class for working with WARC headers. This class includes support for
 * reading and writing WARC headers. Methods are also available to validate
 * individual headers and a WARC header as a whole.
 *
 * @author nicl
 */
public class WarcHeader {

    /** An URI with encapsulating &lt;&gt; characters. */
    public static final boolean URI_LTGT = true;

    /** An URI without encapsulating &lt;&gt; characters. */
    public static final boolean URI_NAKED = false;

    /** Associated WarcReader context.
     *  Must be set prior to calling the various methods. */
    protected WarcReader reader;

    /** Version based WARC validation. */
    protected WarcValidatorBase warcValidator;

    /** Diagnostics used to report diagnoses.
     *  Must be set prior to calling the various methods. */
    protected Diagnostics diagnostics;

    /** WARC-Target-URI profile. */
    protected UriProfile warcTargetUriProfile;

    /** URI profile. */
    protected UriProfile uriProfile;

    /** WARC field parser used.
     *  Must be set prior to calling the various methods. */
    protected WarcFieldParsers fieldParsers;

    /** Max size allowed for a record header. */
    protected int recordHeaderMaxSize = -1;

    /** Line reader used to read version lines. */
    protected HeaderLineReader lineReader;

    /** Header line reader used to read the WARC headers. */
    protected HeaderLineReader headerLineReader;

    /** WARC record starting offset relative to the source WARC file input
     *  stream. The offset is correct for both compressed and uncompressed streams. */
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
    protected boolean[] seen = new boolean[WarcConstants.FN_INDEX_OF_LAST];

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
    /** WARC-Record-Id converted to an <code>Uri</code> object, if valid. */
    public Uri warcRecordIdUri;

    /** WARC-Date field string value. */
    public String warcDateStr;
    /** WARC-Date converted to a <code>Date</code> object, if valid. */
    public WarcDate warcDate;

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
    /** WARC-Refers-To converted to an <code>Uri</code> object, if valid. */
    public Uri warcRefersToUri;

    /** WARC_Target-URI field string value. */
    public String warcTargetUriStr;
    /** WARC-TargetURI converted to an <code>Uri</code> object, if valid. */
    public Uri warcTargetUriUri;

    /** WARC-Warcinfo-Id field string value. */
    public String warcWarcinfoIdStr;
    /** WARC-Warcinfo-Id converted to an <code>Uri</code> object, if valid. */
    public Uri warcWarcinfoIdUri;

    /** WARC-Block-Digest field string value. */
    public String warcBlockDigestStr;
    /** WARC-Block-Digest converted to a <code>WarcDigest</code> object, if valid. */
    public WarcDigest warcBlockDigest;

    /** WARC-Payload-Digest field string value. */
    public String warcPayloadDigestStr;
    /** WARC-Payload-Digest converted to a <code>WarcDigest</code> object, if valid. */
    public WarcDigest warcPayloadDigest;

    /** WARC-Identified-Payload-Type field string value. */
    public String warcIdentifiedPayloadTypeStr;
    /** WARC-Identified-Payload-Type converted to a <code>ContentType</code> object, if valid. */
    public ContentType warcIdentifiedPayloadType;

    /** WARC-Profile field string value.
     *  (revisit record only) */
    public String warcProfileStr;
    /** WARC-Profile field converted to an <code>Uri</code> object, if valid.
     *  (revisit record only) */
    public Uri warcProfileUri;
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
    /** WARC-Segment-Origin-Id converted to an <code>Uri</code> object, if valid.
     *  (continuation record only) */
    public Uri warcSegmentOriginIdUrl;

    /** WARC-Segment-Total-Length field string value.
     *  (continuation record only) */
    public String warcSegmentTotalLengthStr;
    /** WARC-Segment-Total-Length converted to a <code>Long</code> object, if valid.
     *  (continuation record only) */
    public Long warcSegmentTotalLength;

    // see https://docs.google.com/document/d/1QyQBA7Ykgxie75V8Jziz_O7hbhwf7PF6_u9O6w6zgp0/edit
    /** WARC-Refers-To-Target-URI field string value. */
    public String warcRefersToTargetUriStr;
    /** WARC-Refers-To-Target-URI converted to an <code>Uri</code> object, if valid. */
    public Uri warcRefersToTargetUriUri;
    /** WARC-Refers-To-Date */
    public String warcRefersToDateStr;
    /** WARC-Date converted to a <code>Date</code> object, if valid. */
    public WarcDate warcRefersToDate;

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
     * Create a new <code>WarcHeader</code> for recreating a header object.
     * @param recordHeaderMaxSize max size allowed for a record header
     * @param lineReader line reader used to read version lines
     * @param headerLineReader header line reader used to read the WARC headers
     * @param fieldParsers parsers used for the individual types
     * @param uriProfile uri profile used to validate urls
     * @param diagnostics diagnostics object used by parser
     * @return a <code>WarcHeader</code> prepared for reanimation
     */
    public static WarcHeader initHeader(int recordHeaderMaxSize, HeaderLineReader lineReader, HeaderLineReader headerLineReader, WarcFieldParsers fieldParsers, UriProfile uriProfile, Diagnostics diagnostics) {
        WarcHeader header = new WarcHeader();
        header.warcTargetUriProfile = uriProfile;
        header.uriProfile = uriProfile;
        header.fieldParsers = fieldParsers;
        header.diagnostics = diagnostics;
        header.recordHeaderMaxSize = recordHeaderMaxSize;
        header.lineReader = lineReader;
        header.headerLineReader = headerLineReader;
        return header;
    }

    /**
     * Create and initialize a new <code>WarcHeader</code> for writing.
     * @param writer writer which shall be used
     * @param diagnostics diagnostics object used by writer
     * @return a <code>WarcHeader</code> prepared for writing
     */
    public static WarcHeader initHeader(WarcWriter writer, Diagnostics diagnostics) {
        WarcHeader header = new WarcHeader();
        // Set default version to "1.0".
        header.major = 1;
        header.minor = 0;
        header.warcTargetUriProfile = writer.warcTargetUriProfile;
        header.uriProfile = writer.uriProfile;
        header.fieldParsers = writer.fieldParsers;
        header.diagnostics = diagnostics;
        return header;
    }

    /**
     * Create and initialize a new <code>WarcHeader</code> for reading.
     * @param reader reader which shall be used
     * @param startOffset start offset of header
     * @param diagnostics diagnostics object used by reader
     * @return a <code>WarcHeader</code> prepared for reading
     */
    public static WarcHeader initHeader(WarcReader reader, long startOffset, Diagnostics diagnostics) {
        WarcHeader header = new WarcHeader();
        header.reader = reader;
        header.warcTargetUriProfile = reader.warcTargetUriProfile;
        header.uriProfile = reader.uriProfile;
        header.fieldParsers = reader.fieldParsers;
        header.diagnostics = diagnostics;
        header.recordHeaderMaxSize = reader.recordHeaderMaxSize;
        header.lineReader = reader.lineReader;
        header.headerLineReader = reader.headerLineReader;
        // This is only relevant for uncompressed sequentially read records
        header.startOffset = startOffset;
        return header;
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
     * Returns the starting offset of the record in the containing WARC.
     * @return the starting offset of the record
     */
    public long getStartOffset() {
        return startOffset;
    }

    /**
     * Try to parse a WARC header and return a boolean indicating the success or
     * failure of this.
     * @param in input stream with WARC data
     * @return boolean indicating whether a header was parsed or not
     * @throws IOException if an I/O exception occurs while parsing for a header
     */
    public boolean parseHeader(ByteCountingPushBackInputStream in) throws IOException {
        if (parseVersion(in)) {
            // debug
            //System.out.println(wr.bMagicIdentified);
            //System.out.println(wr.bVersionParsed);
            //System.out.println(wr.major + "." + wr.minor);
            warcValidator = reader.validation.getValidatorFor(this, diagnostics);

            MaxLengthRecordingInputStream mrin = new MaxLengthRecordingInputStream(in, recordHeaderMaxSize);
            ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(mrin, recordHeaderMaxSize);

            parseHeaders(pbin);
            pbin.close();

            warcValidator.checkFields(this);

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
    public boolean parseVersion(ByteCountingPushBackInputStream in) throws IOException {
        bMagicIdentified = false;
        bVersionParsed = false;
        boolean bInvalidDataBeforeVersion = false;
        boolean bEmptyLinesBeforeVersion = false;
        HeaderLine line;
        String tmpStr;
        boolean bSeekMagic = true;
        // Loop until when have found something that looks like a version line.
        while (bSeekMagic) {
            // This is only relevant for uncompressed sequentially read records
            startOffset = in.getConsumed();
            line = lineReader.readLine(in);
            if (!lineReader.bEof) {
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
            diagnostics.addError(DiagnosisType.INVALID, "Data before WARC version");
        }
        if (bEmptyLinesBeforeVersion) {
            diagnostics.addError(DiagnosisType.INVALID, "Empty lines before WARC version");
        }
        return bMagicIdentified;
    }

    /**
     * Reads WARC header lines one line at a time until an empty line is
     * encountered.
     * @param in header input stream
     * @throws IOException if an error occurs while reading the WARC header
     */
    public void parseHeaders(ByteCountingPushBackInputStream in) throws IOException {
        HeaderLine headerLine;
        boolean bLoop = true;
        while (bLoop) {
            headerLine = headerLineReader.readLine(in);
            if ((headerLine.bfErrors & HeaderLineReader.E_BIT_INVALID_CHARSET) != 0) {
                diagnostics.addError(DiagnosisType.INVALID_ENCODING, "Invalid encoding in header line", headerLine.value, "UNKNOWN");
            }
            if (!headerLineReader.bEof) {
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
     * @param headerLine the headerLine
     */
    public void addHeader(HeaderLine headerLine) {
        String fieldName = headerLine.name;
        String fieldValue = headerLine.value;
        WarcConcurrentTo warcConcurrentTo;
        Integer fn_idx = WarcConstants.fieldNameIdxMap.get(fieldName.toLowerCase());
        if (fn_idx != null) {
            // WARC field name defined in WARC specification.
            if (!seen[fn_idx] || WarcConstants.fieldNamesRepeatableLookup[fn_idx]) {
                seen[fn_idx] = true;
                switch (fn_idx.intValue()) {
                case WarcConstants.FN_IDX_WARC_TYPE:
                    warcTypeStr = fieldParsers.parseString(fieldValue, WarcConstants.FN_WARC_TYPE);
                    if (warcTypeStr != null) {
                        warcTypeIdx = WarcConstants.recordTypeIdxMap.get(warcTypeStr.toLowerCase());
                    }
                    if (warcTypeIdx == null && warcTypeStr != null && warcTypeStr.length() > 0) {
                        warcTypeIdx = WarcConstants.RT_IDX_UNKNOWN;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_RECORD_ID:
                    warcRecordIdStr = fieldValue;
                    warcRecordIdUri = fieldParsers.parseUri(fieldValue, URI_LTGT, uriProfile, WarcConstants.FN_WARC_RECORD_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_DATE:
                    warcDateStr = fieldValue;
                    //warcDate = fieldParsers.parseDate(fieldValue, WarcConstants.FN_WARC_DATE);
                    warcDate = fieldParsers.parseWarcDate(fieldValue, WarcConstants.FN_WARC_DATE);
                    break;
                case WarcConstants.FN_IDX_CONTENT_LENGTH:
                    contentLengthStr = fieldValue;
                    contentLength = fieldParsers.parseLong(fieldValue, WarcConstants.FN_CONTENT_LENGTH);
                    break;
                case WarcConstants.FN_IDX_CONTENT_TYPE:
                    contentTypeStr = fieldValue;
                    contentType = fieldParsers.parseContentType(fieldValue, WarcConstants.FN_CONTENT_TYPE);
                    break;
                case WarcConstants.FN_IDX_WARC_CONCURRENT_TO:
                    Uri tmpUri = fieldParsers.parseUri(fieldValue, URI_LTGT, uriProfile, WarcConstants.FN_WARC_CONCURRENT_TO);
                    if (fieldValue != null && fieldValue.trim().length() > 0) {
                        warcConcurrentTo = new WarcConcurrentTo();
                        warcConcurrentTo.warcConcurrentToStr = fieldValue;
                        warcConcurrentTo.warcConcurrentToUri = tmpUri;
                        warcConcurrentToList.add(warcConcurrentTo);
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_BLOCK_DIGEST:
                    warcBlockDigestStr = fieldValue;
                    warcBlockDigest = fieldParsers.parseDigest(fieldValue, WarcConstants.FN_WARC_BLOCK_DIGEST);
                    break;
                case WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST:
                    warcPayloadDigestStr = fieldValue;
                    warcPayloadDigest = fieldParsers.parseDigest(fieldValue, WarcConstants.FN_WARC_PAYLOAD_DIGEST);
                    break;
                case WarcConstants.FN_IDX_WARC_IP_ADDRESS:
                    warcIpAddress = fieldValue;
                    warcInetAddress = fieldParsers.parseIpAddress(fieldValue, WarcConstants.FN_WARC_IP_ADDRESS);
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO:
                    warcRefersToStr = fieldValue;
                    warcRefersToUri = fieldParsers.parseUri(fieldValue, URI_LTGT, uriProfile, WarcConstants.FN_WARC_REFERS_TO);
                    break;
                case WarcConstants.FN_IDX_WARC_TARGET_URI:
                    warcTargetUriStr = fieldValue;
                    warcTargetUriUri = fieldParsers.parseUri(fieldValue, URI_NAKED, warcTargetUriProfile, WarcConstants.FN_WARC_TARGET_URI);
                    break;
                case WarcConstants.FN_IDX_WARC_TRUNCATED:
                    warcTruncatedStr = fieldParsers.parseString(fieldValue, WarcConstants.FN_WARC_TRUNCATED);
                    if (warcTruncatedStr != null) {
                        warcTruncatedIdx = WarcConstants.truncatedTypeIdxMap.get(warcTruncatedStr.toLowerCase());
                    }
                    if (warcTruncatedIdx == null && warcTruncatedStr != null && warcTruncatedStr.length() > 0) {
                        warcTruncatedIdx = WarcConstants.TT_IDX_FUTURE_REASON;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_WARCINFO_ID:
                    warcWarcinfoIdStr = fieldValue;
                    warcWarcinfoIdUri = fieldParsers.parseUri(fieldValue, URI_LTGT, uriProfile, WarcConstants.FN_WARC_WARCINFO_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_FILENAME:
                    warcFilename = fieldParsers.parseString(fieldValue, WarcConstants.FN_WARC_FILENAME);
                    break;
                case WarcConstants.FN_IDX_WARC_PROFILE:
                    warcProfileStr = fieldValue;
                    warcProfileUri = fieldParsers.parseUri(fieldValue, URI_NAKED, uriProfile, WarcConstants.FN_WARC_PROFILE);
                    if (warcProfileStr != null) {
                        warcProfileIdx = WarcConstants.profileIdxMap.get(warcProfileStr.toLowerCase());
                    }
                    if (warcProfileIdx == null && warcProfileStr != null && warcProfileStr.length() > 0) {
                        warcProfileIdx = WarcConstants.WARC_PROFILE_IDX_UNKNOWN;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE:
                    warcIdentifiedPayloadTypeStr = fieldValue;
                    warcIdentifiedPayloadType = fieldParsers.parseContentType(fieldValue, WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID:
                    warcSegmentOriginIdStr = fieldValue;
                    warcSegmentOriginIdUrl = fieldParsers.parseUri(fieldValue, URI_LTGT, uriProfile, WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER:
                    warcSegmentNumberStr = fieldValue;
                    warcSegmentNumber = fieldParsers.parseInteger(fieldValue, WarcConstants.FN_WARC_SEGMENT_NUMBER);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH:
                    warcSegmentTotalLengthStr = fieldValue;
                    warcSegmentTotalLength = fieldParsers.parseLong(fieldValue, WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH);
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO_TARGET_URI:
                    warcRefersToTargetUriStr = fieldValue;
                    warcRefersToTargetUriUri = fieldParsers.parseUri(fieldValue, URI_NAKED, warcTargetUriProfile, WarcConstants.FN_WARC_REFERS_TO_TARGET_URI);
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO_DATE:
                    warcRefersToDateStr = fieldValue;
                    //warcRefersToDate = fieldParsers.parseDate(fieldValue, WarcConstants.FN_WARC_REFERS_TO_DATE);
                    warcRefersToDate = fieldParsers.parseWarcDate(fieldValue, WarcConstants.FN_WARC_REFERS_TO_DATE);
                    break;
                }
            } else {
                // Duplicate field.
                diagnostics.addError(DiagnosisType.DUPLICATE, "'" + fieldName + "' header", fieldValue);
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
     * Get a <code>List</code> of all the headers found during parsing.
     * @return <code>List</code> of <code>HeaderLine</code>
     */
    public List<HeaderLine> getHeaderList() {
        return Collections.unmodifiableList(headerList);
    }

    /**
     * Get a header line structure or null, if no header line structure is
     * stored with the given header name.
     * @param field header name
     * @return <code>HeaderLine</code> structure or null
     */
    public HeaderLine getHeader(String field) {
        if (field != null && field.length() > 0) {
            return headerMap.get(field.toLowerCase());
        } else {
            return null;
        }
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
     * @param integerFieldValue <code>Integer</code> field value object
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
     * @param longFieldValue <code>Long</code> field value object
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
     * @param digestFieldValue <code>Digest</code> field value object
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
     * @param contentTypeFieldValue <code>ContentType</code> field value object
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
     * @param dateFieldValue <code>Date</code> field value object
     * @param fieldValueStr Date field value string
     * @return <code>HeaderLine</code> object corresponding to what would have been read
     */
    public HeaderLine addHeader(String fieldName, WarcDate dateFieldValue, String fieldValueStr) {
        if (dateFieldValue == null && fieldValueStr != null) {
            // FIXME Check check check.
            //dateFieldValue = fieldParsers.parseDate(fieldValueStr, fieldName);
            dateFieldValue = fieldParsers.parseWarcDate(fieldValueStr, fieldName);
        } else if (fieldValueStr == null && dateFieldValue != null) {
            // FIXME Check check check.
            //fieldValueStr = WarcDateParser.getDateFormat().format(dateFieldValue);
            // FIXME Tricky since precision is WARC version dependent!
            fieldValueStr = dateFieldValue.toString();
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
     * @param inetAddrFieldValue <code>InetAddress</code> field value object
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
    public HeaderLine addHeader(String fieldName, Uri uriFieldValue, String fieldValueStr) {
        if (uriFieldValue == null && fieldValueStr != null) {
            if (WarcConstants.FN_WARC_TARGET_URI.equalsIgnoreCase(fieldName)) {
                uriFieldValue = fieldParsers.parseUri(fieldValueStr, URI_NAKED, warcTargetUriProfile, fieldName);
            } else if (WarcConstants.FN_WARC_PROFILE.equalsIgnoreCase(fieldName)) {
                uriFieldValue = fieldParsers.parseUri(fieldValueStr, URI_NAKED, uriProfile, fieldName);
            } else if (WarcConstants.FN_WARC_REFERS_TO_TARGET_URI.equalsIgnoreCase(fieldName)) {
                uriFieldValue = fieldParsers.parseUri(fieldValueStr, URI_NAKED, warcTargetUriProfile, fieldName);
            } else {
                uriFieldValue = fieldParsers.parseUri(fieldValueStr, URI_LTGT, uriProfile, fieldName);
            }
        } else if (fieldValueStr == null && uriFieldValue != null) {
            if (WarcConstants.FN_WARC_TARGET_URI.equalsIgnoreCase(fieldName)
                    || WarcConstants.FN_WARC_PROFILE.equalsIgnoreCase(fieldName)
                    || WarcConstants.FN_WARC_REFERS_TO_TARGET_URI.equalsIgnoreCase(fieldName)) {
                fieldValueStr = uriFieldValue.toString();
            } else {
                fieldValueStr = "<" + uriFieldValue.toString() + ">";
            }
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
            WarcDate dateFieldValue, InetAddress inetAddrFieldValue,
            Uri uriFieldValue) {
        Integer fn_idx = WarcConstants.fieldNameIdxMap.get(fieldName.toLowerCase());
        if (fn_idx != null) {
            // Implicit cast from integer to long, if needed.
            if (WarcConstants.FN_IDX_DT[fn_idx] == WarcConstants.FDT_LONG && dt == WarcConstants.FDT_INTEGER) {
                longFieldValue = (long)integerFieldValue;
                dt = WarcConstants.FDT_LONG;
            }
            if (dt == WarcConstants.FN_IDX_DT[fn_idx]) {
                // WARC field name defined in WARC specification.
                if (seen[fn_idx] && !WarcConstants.fieldNamesRepeatableLookup[fn_idx]) {
                    // Duplicate field.
                    diagnostics.addError(DiagnosisType.DUPLICATE, "'" + fieldName + "' header", fieldValueStr);
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
                    // FIXME Maybe change parameter type if it does not break too many things.
                    //warcDate = WarcDate.getWarcDate(dateFieldValue);
                    warcDate = dateFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO_DATE:
                    warcRefersToDateStr = fieldValueStr;
                    // FIXME Maybe change parameter type if it does not break too many things.
                    //warcRefersToDate = WarcDate.getWarcDate(dateFieldValue);
                    warcRefersToDate = dateFieldValue;
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
                case WarcConstants.FN_IDX_WARC_PROFILE:
                    warcProfileStr = fieldValueStr;
                    warcProfileUri = uriFieldValue;
                    if (warcProfileStr != null) {
                        warcProfileIdx = WarcConstants.profileIdxMap.get(warcProfileStr.toLowerCase());
                    }
                    if (warcProfileIdx == null && warcProfileStr != null && warcProfileStr.length() > 0) {
                        warcProfileIdx = WarcConstants.WARC_PROFILE_IDX_UNKNOWN;
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID:
                    warcSegmentOriginIdStr = fieldValueStr;
                    warcSegmentOriginIdUrl = uriFieldValue;
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO_TARGET_URI:
                    warcRefersToTargetUriStr = fieldValueStr;
                    warcRefersToTargetUriUri = uriFieldValue;
                    break;
                default:
                    break;
                }
            } else {
                // Invalid datatype for field.
                diagnostics.addError(DiagnosisType.INVALID_EXPECTED,
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

}
