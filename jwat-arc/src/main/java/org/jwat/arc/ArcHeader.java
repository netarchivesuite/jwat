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
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Date;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.Uri;

/**
 * Class for parsing and validating the common ARC record header present in
 * both ARC records and ARC version blocks.
 *
 * @author nicl
 */
public class ArcHeader {

    /** Associated WarcReader context.
     * Must be set prior to calling the various methods. */
    protected ArcReader reader;

    /** Diagnostics used to report diagnoses.
     * Must be set prior to calling the various methods. */
    protected Diagnostics<Diagnosis> diagnostics;

    /** ARC field parser used.
     * Must be set prior to calling the various methods. */
    protected ArcFieldParsers fieldParsers;

    /** ARC <code>DateFormat</code> as specified by the ARC specifications. */
    protected DateFormat warcDateFormat;

    /** ARC record starting offset relative to the source ARC file input
     *  stream. The offset is correct for both compressed and uncompressed streams. */
    protected long startOffset = -1;

    /*
     * Version related fields.
     */

    /** Which version of the record fields was parsed, 1.x or 2.0. */
    public int recordFieldVersion;

    /*
     * ARC header fields.
     */

    /** Do the record fields comply in number with the one dictated by its
     *  version. */
    protected boolean hasCompliantFields = false;

    /** ARC record URL field string value. */
    public String urlStr;
    /** ARC record URL validated and converted to an <code>URI</code> object. */
    public Uri urlUri;
    /** URI Scheme (lowercase). (filedesc, http, https, dns, etc.) */
    public String urlScheme;

    /** ARC record IP-Address field string value. */
    public String ipAddressStr;
    /** IP-Address validated and converted to an <code>InetAddress</code> object. */
    public InetAddress inetAddress;

    /** ARC record archive-date field string value. */
    public String archiveDateStr;
    /** Archive date validated and converted to a <code>Date</code> object. */
    public Date archiveDate;

    /** ARC record content-type field string value. */
    public String contentTypeStr;
    /** Content-Type wrapper object with optional parameters. */
    public ContentType contentType;

    /** ARC record result-code field string value. */
    public String resultCodeStr;
    /** Result-code validated and converted into an <code>Integer</code> object. */
    public Integer resultCode;

    /** ARC record checksum field string value. */
    public String checksumStr;

    /** ARC record location field string value. */
    public String locationStr;

    /** ARC record offset field string value. */
    public String offsetStr;
    /** Offset validated and converted into a <code>Long</code> object. */
    public Long offset;

    /** ARC record filename field string value. */
    public String filenameStr;

    /** ARC record archive-length field string value. */
    public String archiveLengthStr;
    /** Archive-length validated and converted into a <code>Long</code> object. */
    public Long archiveLength;

    /**
     * Create and initialize a new <code>ArcHeader</code> for writing.
     * @param writer writer which shall be used
     * @param diagnostics diagnostics object used by writer
     * @return a <code>ArcHeader</code> prepared for writing
     */
    public static ArcHeader initHeader(ArcWriter writer, Diagnostics<Diagnosis> diagnostics) {
        ArcHeader header = new ArcHeader();
        header.fieldParsers = writer.fieldParsers;
        header.warcDateFormat = writer.arcDateFormat;
        header.diagnostics = diagnostics;
        return header;
    }

    /**
     * Create and initialize a new <code>ArcHeader</code> for reading.
     * @param reader reader which shall be used
     * @param startOffset start offset of header
     * @param diagnostics diagnostics object used by reader
     * @return a <code>ArcHeader</code> prepared for reading
     */
    public static ArcHeader initHeader(ArcReader reader, long startOffset, Diagnostics<Diagnosis> diagnostics) {
        ArcHeader header = new ArcHeader();
        header.reader = reader;
        header.fieldParsers = reader.fieldParsers;
        header.diagnostics = diagnostics;
        // This is only relevant for uncompressed sequentially read records
        header.startOffset = startOffset;
        return header;
    }

    /**
     * Tries to read lines from the input stream to determine if they could
     * be possible ARC record header lines. Returns true if a line was
     * encountered with delimited fields matching either a v1.x or v2.0
     * description block. False is no ARC record header candidate was found
     * in the remaining input stream.
     * @param in input stream supposedly containing ARC records
     * @return true if an ARC record header was parsed and validated
     * @throws IOException if an i/o exception occurs while trying to read an
     * ARC record header
     */
    public boolean parseHeader(ByteCountingPushBackInputStream in) throws IOException {
        boolean bHeaderParsed = false;
        boolean bInvalidDataBeforeVersion = false;
        boolean bEmptyLinesBeforeVersion = false;
        boolean bSeekRecord = true;
        while (bSeekRecord) {
            startOffset = in.getConsumed();
            String recordLine = in.readLine();
            if (recordLine != null) {
                if (recordLine.length() > 0) {
                    String[] fields = recordLine.split(" ", -1);
                    if (fields.length == ArcConstants.VERSION_1_BLOCK_NUMBER_FIELDS
                            || fields.length == ArcConstants.VERSION_2_BLOCK_NUMBER_FIELDS) {
                        // debug
                        //System.out.println(recordLine);
                        parseHeaders(fields);
                        bHeaderParsed = true;
                        bSeekRecord = false;
                    } else {
                        bInvalidDataBeforeVersion = true;
                    }
                } else {
                    bEmptyLinesBeforeVersion = true;
                }
            }
            else {
                bSeekRecord = false;
            }
        }
        if (bInvalidDataBeforeVersion) {
            diagnostics.addError(new Diagnosis(DiagnosisType.INVALID, "Data before ARC record"));
        }
        if (bEmptyLinesBeforeVersion) {
            diagnostics.addError(new Diagnosis(DiagnosisType.INVALID, "Empty lines before ARC record"));
        }
        return bHeaderParsed;
    }

    /**
     * Examines an array of strings to determine if it could be an ARC record
     * header. If the number of fields is not equal to that of either v1.x or
     * v2.0 nothing is parsed.
     * @param fields array of possible ARC record header fields
     */
    public void parseHeaders(String[] fields) {
        if (fields.length == ArcConstants.VERSION_1_BLOCK_FIELDS.length
                || fields.length == ArcConstants.VERSION_2_BLOCK_FIELDS.length) {
            recordFieldVersion = 1;
            /*
             * Version 1.
             */
            urlStr = fields[ArcConstants.FN_IDX_URL];
            if ("-".equals(urlStr)) {
                urlStr = null;
            }
            urlUri = fieldParsers.parseUri(urlStr, ArcConstants.FN_URL, false);
            if (urlUri != null) {
                urlScheme = urlUri.getScheme();
                if (urlScheme != null) {
                    urlScheme = urlScheme.toLowerCase();
                }
            }

            ipAddressStr = fields[ArcConstants.FN_IDX_IP_ADDRESS];
            if ("-".equals(ipAddressStr)) {
                ipAddressStr = null;
            }
            inetAddress = fieldParsers.parseIpAddress(ipAddressStr, ArcConstants.FN_IP_ADDRESS, false);

            archiveDateStr = fields[ArcConstants.FN_IDX_ARCHIVE_DATE];
            if ("-".equals(archiveDateStr)) {
                archiveDateStr = null;
            }
            archiveDate = fieldParsers.parseDate(archiveDateStr, ArcConstants.FN_ARCHIVE_DATE, false);

            contentTypeStr = fields[ArcConstants.FN_IDX_CONTENT_TYPE];
            if ("-".equals(contentTypeStr)) {
                contentTypeStr = null;
            }
            if (!ArcConstants.CONTENT_TYPE_NO_TYPE.equalsIgnoreCase(contentTypeStr)) {
                contentType = fieldParsers.parseContentType(contentTypeStr, ArcConstants.FN_CONTENT_TYPE, false);
            }

            if (fields.length == ArcConstants.VERSION_2_BLOCK_FIELDS.length) {
                recordFieldVersion = 2;
                /*
                 *  Version 2.
                 */
                resultCodeStr = fields[ArcConstants.FN_IDX_RESULT_CODE];
                if ("-".equals(resultCodeStr)) {
                    resultCodeStr = null;
                }
                resultCode = fieldParsers.parseInteger(
                        resultCodeStr, ArcConstants.FN_RESULT_CODE, false);
                if (resultCode != null && (resultCode < 100 || resultCode > 999)) {
                    diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED,
                            "'" + ArcConstants.FN_RESULT_CODE + "' value",
                            resultCodeStr,
                            "A number between 100 and 999"));
                }

                checksumStr = fields[ArcConstants.FN_IDX_CHECKSUM];
                if ("-".equals(checksumStr)) {
                    checksumStr = null;
                }
                checksumStr = fieldParsers.parseString(
                        checksumStr, ArcConstants.FN_CHECKSUM, true);

                locationStr = fields[ArcConstants.FN_IDX_LOCATION];
                if ("-".equals(locationStr)) {
                    locationStr = null;
                }
                locationStr = fieldParsers.parseString(
                        locationStr, ArcConstants.FN_LOCATION, true);

                offsetStr = fields[ArcConstants.FN_IDX_OFFSET];
                if ("-".equals(offsetStr)) {
                    offsetStr = null;
                }
                offset = fieldParsers.parseLong(
                        offsetStr, ArcConstants.FN_OFFSET, false);
                if (offset != null && offset < 0) {
                    diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED,
                            "'" + ArcConstants.FN_OFFSET + "' value",
                            offsetStr,
                            "A non negative number"));
                }

                filenameStr = fields[ArcConstants.FN_IDX_FILENAME];
                if ("-".equals(filenameStr)) {
                    filenameStr = null;
                }
                filenameStr = reader.fieldParsers.parseString(
                        filenameStr, ArcConstants.FN_FILENAME, false);
            }
            archiveLengthStr = fields[fields.length - 1];
            if ("-".equals(archiveLengthStr)) {
                archiveLengthStr = null;
            }
            archiveLength = reader.fieldParsers.parseLong(
                    archiveLengthStr, ArcConstants.FN_ARCHIVE_LENGTH, false);
            if (archiveLength != null && archiveLength < 0) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED,
                        "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value",
                        archiveLengthStr,
                        "A non negative number"));
            }
        }
    }

    /**
     * Returns the starting offset of the record in the containing ARC.
     * @return the starting offset of the record
     */
    public long getStartOffset() {
        return startOffset;
    }

    /**
     * Add object information to <code>StringBuilder</code>.
     * @param sb <code>StringBuilder</code> where to add information
     */
    public void toStringBuilder(StringBuilder sb) {
        if (urlStr != null) {
            sb.append("url: ").append(urlStr);
        }
        if (ipAddressStr != null) {
            sb.append(", ipAddress: ").append(ipAddressStr);
        }
        if (archiveDateStr != null) {
            sb.append(", archiveDate: ").append(archiveDateStr);
        }
        if (contentTypeStr != null) {
            sb.append(", contentType: ").append(contentTypeStr);
        }
        if (resultCodeStr != null) {
            sb.append(", resultCode: ").append(resultCodeStr);
        }
        if (checksumStr != null) {
            sb.append(", checksum: ").append(checksumStr);
        }
        if (locationStr != null) {
            sb.append(", location: ").append(locationStr);
        }
        if (offsetStr != null) {
            sb.append(", offset: ").append(offsetStr);
        }
        if (filenameStr != null) {
            sb.append(", fileName: ").append(filenameStr);
        }
        if (archiveLengthStr != null) {
            sb.append(", length: ").append(archiveLengthStr);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringBuilder(sb);
        return sb.toString();
    }

}
