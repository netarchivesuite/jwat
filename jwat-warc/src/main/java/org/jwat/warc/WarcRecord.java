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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jwat.common.Base16;
import org.jwat.common.Base32;
import org.jwat.common.Base64;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.Digest;
import org.jwat.common.HeaderLine;
import org.jwat.common.HttpResponse;
import org.jwat.common.IPAddressParser;
import org.jwat.common.Payload;
import org.jwat.common.PayloadOnClosedHandler;

/**
 * This class represents a parsed WARC record header block including
 * possible validation and format warnings/errors encountered in the process.
 * The payload of the WARC record is accessible through a wrapped payload
 * object.
 *
 * @author nicl
 */
public class WarcRecord implements PayloadOnClosedHandler {

    /** Pushback size used in payload. */
    public static final int PAYLOAD_PUSHBACK_SIZE = 8192;

    /** Reader instance used, required for file compliance. */
    protected WarcReader reader;

    /** Input stream used to read this record. */
    protected ByteCountingPushBackInputStream in;

    /** Is this record compliant ie. error free. */
    protected boolean bIsCompliant;

    /** Validation errors and warnings. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /** Is Warc-Block-Digest valid. (Null is equal to not tested) */
    public Boolean isValidBlockDigest = null;

    /** Is Warc-Payload-Digest valid. (Null is equal to not tested) */
    public Boolean isValidPayloadDigest = null;

    /*
     * Version related fields.
     */

    boolean bMagicIdentified;
    boolean bVersionParsed;

    int major = -1;
    int minor = -1;

    protected long startOffset = -1;

    /** Bytes consumed while validating this record. */
    long consumed = 0;

    boolean bMandatoryMissing;

    /*
     * Warc-Fields.
     */

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

    public List<String> warcConcurrentToStrList;
    public List<URI> warcConcurrentToUriList;

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
     * Header-Fields.
     */

    /** List of parsed header fields. */
    protected List<HeaderLine> headerList;

    /** Map of parsed header fields. */
    protected Map<String, HeaderLine> headerMap;

    /*
     * Payload
     */

    /** Has payload been closed before. */
    protected boolean bPayloadClosed;

    /** Has record been closed before. */
    protected boolean bClosed;

    /** Payload object if any exists. */
    protected Payload payload;

    /** HttpResponse header content parse from payload. */
    protected HttpResponse httpResponse;

    /** Computed block digest. */
    public Digest computedBlockDigest;

    /** Computed payload digest. */
    public Digest computedPayloadDigest;

    /**
     * Given an <code>InputStream</code> it tries to read and validate a WARC
     * header block.
     * @param in <code>InputStream</code> containing WARC record data
     * @param reader <code>WarcReader</code> used, with access to user defined
     * options
     * @return <code>WarcRecord</code> or <code>null</code>
     * @throws IOException io exception in the process of reading record
     */
    public static WarcRecord parseRecord(ByteCountingPushBackInputStream in,
                                    WarcReader reader) throws IOException {
        WarcRecord wr = new WarcRecord();
        wr.in = in;
        wr.reader = reader;
        wr.startOffset = in.getConsumed();
        if (wr.parseVersion(in)) {
            // debug
            //System.out.println(wr.bMagicIdentified);
            //System.out.println(wr.bVersionParsed);
            //System.out.println(wr.major + "." + wr.minor);

            wr.parseFields(in);
            wr.checkFields();

            /*
             * Payload processing.
             */
            if (wr.contentLength != null && wr.contentLength > 0) {
                /*
                 * Payload.
                 */
                String digestAlgorithm = null;
                if (reader.bBlockDigest) {
                    if (wr.warcBlockDigest != null && wr.warcBlockDigest.algorithm != null) {
                        // If a WARC block digest header is present in the
                        // record, use that algorithm.
                        digestAlgorithm = wr.warcBlockDigest.algorithm;
                    } else {
                        // If no WARC block digest header is present,
                        // use the optional user specified algorithm.
                        // Can be null in which case nothing is computed.
                        digestAlgorithm = reader.blockDigestAlgorithm;
                    }
                }
                wr.payload = Payload.processPayload(in, wr.contentLength,
                                         PAYLOAD_PUSHBACK_SIZE, digestAlgorithm);
                wr.payload.setOnClosedHandler(wr);
                /*
                 * HttpResponse.
                 */
                if (wr.contentType != null
                        && wr.contentType.contentType.equals("application")
                        && wr.contentType.mediaType.equals("http")) {
                    String value = wr.contentType.getParameter("msgtype");
                    // request
                    if ("response".equals(value)) {
                        digestAlgorithm = null;
                        if (reader.bPayloadDigest) {
                            if (wr.warcPayloadDigest != null && wr.warcPayloadDigest.algorithm != null) {
                                // If a WARC payload digest header is present in the
                                // record, use that algorithm.
                                digestAlgorithm = wr.warcPayloadDigest.algorithm;
                            } else {
                                // If no WARC payload digest header is present,
                                // use the optional user specified algorithm.
                                // Can be null in which case nothing is computed.
                                digestAlgorithm = reader.payloadDigestAlgorithm;
                            }
                        }
                        wr.httpResponse = HttpResponse.processPayload(
                                wr.payload.getInputStream(), wr.contentLength,
                                digestAlgorithm);
                        if (wr.httpResponse != null) {
                            wr.payload.setHttpResponse(wr.httpResponse);
                        }
                    }
                }
            }
            // Preliminary compliance status, will be updated when the
            // payload/record is closed.
            if (wr.diagnostics.hasErrors() || wr.diagnostics.hasWarnings()) {
                wr.bIsCompliant = false;
            } else {
                wr.bIsCompliant = true;
            }
            wr.consumed = in.getConsumed() - wr.startOffset;
            wr.reader.bIsCompliant &= wr.bIsCompliant;
        } else {
            if (wr.diagnostics.hasErrors() || wr.diagnostics.hasWarnings()) {
                wr.reader.errors += wr.diagnostics.getErrors().size();
                wr.reader.warnings += wr.diagnostics.getWarnings().size();
                wr.reader.bIsCompliant = false;
            }
            // EOF
            wr = null;
        }
        return wr;
    }

    /**
     * Called when the payload object is closed and final steps in the
     * validation process can be performed.
     * @throws IOException io exception in final validation processing
     */
    @Override
    public void payloadClosed() throws IOException {
        if (!bPayloadClosed) {
            if (payload != null) {
                // Check for truncated payload.
                if (payload.getUnavailable() > 0) {
                    // Payload length mismatch - Payload truncated
                    addErrorDiagnosis(DiagnosisType.INVALID_DATA, "Payload length mismatch", "Payload truncated");
                }
                /*
                 * Check block digest.
                 */
                MessageDigest md = payload.getMessageDigest();
                byte[] digest;
                // Check for computed block digest.
                if (md != null) {
                    computedBlockDigest = new Digest();
                    computedBlockDigest.digestBytes = md.digest();
                }
                // Auto detect encoding used in WARC header.
                if (warcBlockDigest != null && warcBlockDigest.digestString != null) {
                    if (computedBlockDigest != null) {
                        computedBlockDigest.algorithm = warcBlockDigest.algorithm;
                        if ((computedBlockDigest.digestBytes.length + 2) / 3 * 4 == warcBlockDigest.digestString.length()) {
                            digest = Base64.decodeToArray(warcBlockDigest.digestString);
                            warcBlockDigest.encoding = "base64";
                            computedBlockDigest.encoding = warcBlockDigest.encoding;
                        } else if ((computedBlockDigest.digestBytes.length + 4) / 5 * 8 == warcBlockDigest.digestString.length()) {
                            digest = Base32.decodeToArray(warcBlockDigest.digestString);
                            warcBlockDigest.encoding = "base32";
                            computedBlockDigest.encoding = warcBlockDigest.encoding;
                        } else if (computedBlockDigest.digestBytes.length * 2 == warcBlockDigest.digestString.length()) {
                            digest = Base16.decodeToArray(warcBlockDigest.digestString);
                            warcBlockDigest.encoding = "base16";
                            computedBlockDigest.encoding = warcBlockDigest.encoding;
                        } else {
                            digest = null;
                            // Encoding - Unrecognized block digest encoding scheme
                            addErrorDiagnosis(DiagnosisType.UNKNOWN,
                                    "Block digest encoding scheme",
                                    warcBlockDigest.digestString);
                        }
                        if (digest != null) {
                            if (!Arrays.equals(computedBlockDigest.digestBytes, digest)) {
                                // Block digest - Computed block digest does not match
                                addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                                        "Block digest",
                                        Base16.encodeArray(digest),
                                        Base16.encodeArray(computedBlockDigest.digestBytes));
                                isValidBlockDigest = false;
                            } else {
                                isValidBlockDigest = true;
                            }
                        }
                    }
                }
                // Adjust information about computed block digest.
                if (computedBlockDigest != null) {
                    if (computedBlockDigest.algorithm == null) {
                        computedBlockDigest.algorithm = reader.blockDigestAlgorithm;
                    }
                    if (computedBlockDigest.encoding == null && reader.blockDigestEncoding != null) {
                        if ("base32".equals(reader.blockDigestEncoding)) {
                            computedBlockDigest.encoding = "base32";
                        } else if ("base64".equals(reader.blockDigestEncoding)) {
                            computedBlockDigest.encoding = "base64";
                        } else if ("base16".equals(reader.blockDigestEncoding)) {
                            computedBlockDigest.encoding = "base16";
                        } else {
                            // Encoding - Unknown block digest encoding scheme ..
                            addErrorDiagnosis(DiagnosisType.INVALID_DATA,
                                    "Block digest encoding scheme",
                                    reader.blockDigestEncoding);
                        }
                    }
                    if (computedBlockDigest.encoding != null) {
                        if ("base32".equals(computedBlockDigest.encoding)) {
                            computedBlockDigest.digestString = Base32.encodeArray(computedBlockDigest.digestBytes);
                        } else if ("base64".equals(computedBlockDigest.encoding)) {
                            computedBlockDigest.digestString = Base64.encodeArray(computedBlockDigest.digestBytes);
                        } else if ("base16".equals(computedBlockDigest.encoding)) {
                            computedBlockDigest.digestString = Base16.encodeArray(computedBlockDigest.digestBytes);
                        }
                    }
                }
                if (httpResponse != null) {
                    /*
                     * Check payload digest.
                     */
                    md = httpResponse.getMessageDigest();
                    // Check for computed payload digest.
                    if (md != null) {
                        computedPayloadDigest = new Digest();
                        computedPayloadDigest.digestBytes = md.digest();
                    }
                    // Auto detect encoding used in WARC header.
                    if (warcPayloadDigest != null && warcPayloadDigest.digestString != null ) {
                        if (computedPayloadDigest != null) {
                            computedPayloadDigest.algorithm = warcPayloadDigest.algorithm;
                            if ((computedPayloadDigest.digestBytes.length + 2) / 3 * 4 == warcPayloadDigest.digestString.length()) {
                                digest = Base64.decodeToArray(warcPayloadDigest.digestString);
                                warcPayloadDigest.encoding = "base64";
                                computedPayloadDigest.encoding = warcPayloadDigest.encoding;
                            } else if ((computedPayloadDigest.digestBytes.length + 4) / 5 * 8 == warcPayloadDigest.digestString.length()) {
                                digest = Base32.decodeToArray(warcPayloadDigest.digestString);
                                warcPayloadDigest.encoding = "base32";
                                computedPayloadDigest.encoding = warcPayloadDigest.encoding;
                            } else if (computedPayloadDigest.digestBytes.length * 2 == warcPayloadDigest.digestString.length()) {
                                digest = Base16.decodeToArray(warcPayloadDigest.digestString);
                                warcPayloadDigest.encoding = "base16";
                                computedPayloadDigest.encoding = warcPayloadDigest.encoding;
                            } else {
                                digest = null;
                                // Encoding - Unrecognized payload digest encoding scheme
                                addErrorDiagnosis(DiagnosisType.UNKNOWN,
                                        "Payload digest encoding scheme",
                                        warcPayloadDigest.digestString);
                            }
                            if (digest != null) {
                                if (!Arrays.equals(computedPayloadDigest.digestBytes, digest)) {
                                    // Payload digest - Computed payload digest does not match
                                    addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                                            "Payload digest",
                                            Base16.encodeArray(digest),
                                            Base16.encodeArray(computedPayloadDigest.digestBytes));
                                    isValidPayloadDigest = false;
                                } else {
                                    isValidPayloadDigest = true;
                                }
                            }
                        }
                    }
                    // Adjust information about computed payload digest.
                    if (computedPayloadDigest != null) {
                        if (computedPayloadDigest.algorithm == null) {
                            computedPayloadDigest.algorithm = reader.payloadDigestAlgorithm;
                        }
                        if (computedPayloadDigest.encoding == null && reader.payloadDigestEncoding != null) {
                            if ("base32".equals(reader.payloadDigestEncoding)) {
                                computedPayloadDigest.encoding = "base32";
                            } else if ("base64".equals(reader.payloadDigestEncoding)) {
                                computedPayloadDigest.encoding = "base64";
                            } else if ("base16".equals(reader.payloadDigestEncoding)) {
                                computedPayloadDigest.encoding = "base16";
                            } else {
                                // Encoding - Unknown payload digest encoding scheme ..
                                addErrorDiagnosis(DiagnosisType.INVALID_DATA,
                                        "Payload digest encoding scheme",
                                        reader.payloadDigestEncoding);
                            }
                        }
                        if (computedPayloadDigest.encoding != null) {
                            if ("base32".equals(computedPayloadDigest.encoding)) {
                                computedPayloadDigest.digestString = Base32.encodeArray(computedPayloadDigest.digestBytes);
                            } else if ("base64".equals(computedPayloadDigest.encoding)) {
                                computedPayloadDigest.digestString = Base64.encodeArray(computedPayloadDigest.digestBytes);
                            } else if ("base16".equals(computedPayloadDigest.encoding)) {
                                computedPayloadDigest.digestString = Base16.encodeArray(computedPayloadDigest.digestBytes);
                            }
                        }
                    }
                }
            }
            // Check for trailing newlines.
            int newlines = parseNewLines(in);
            if (newlines != WarcConstants.WARC_RECORD_TRAILING_NEWLINES) {
                addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                        "Trailing newlines",
                        Integer.toString(newlines),
                        "2");
            }
            // isCompliant status update.
            if (diagnostics.hasErrors() || diagnostics.hasWarnings()) {
                bIsCompliant = false;
                reader.errors += diagnostics.getErrors().size();
                reader.warnings += diagnostics.getWarnings().size();
            } else {
                bIsCompliant = true;
            }
            reader.bIsCompliant &= bIsCompliant;
            // Updated consumed after payload has been consumed.
            consumed = in.getConsumed() - startOffset;
            reader.consumed += consumed;
            // Dont not close payload again.
            bPayloadClosed = true;
        }
    }

    /**
     * Check to see if the record has been closed.
     * @return boolean indicating whether this record is closed or not
     */
    public boolean isClosed() {
        return bClosed;
    }

    /**
     * Close resources associated with the WARC record.
     * Mainly payload stream if any.
     * @throws IOException if unable to close resources
     */
    public void close() throws IOException {
        if (!bClosed) {
            // Ensure input stream is at the end of the record payload.
            if (payload != null) {
                payload.close();
            }
            payloadClosed();
            reader = null;
            in = null;
            bClosed = true;
        }
    }

    /**
     * Get the record offset relative to the start of the WARC file
     * <code>InputStream</code>.
     * @return the record offset relative to the start of the WARC file
     */
    public long getStartOffset() {
        return startOffset;
    }

    /**
     * Return number of bytes consumed validating this record.
     * @return number of bytes consumed validating this record
     */
    public Long getConsumed() {
        return consumed;
    }

    /**
     * Get a <code>List</code> of all the non-standard WARC headers found
     * during parsing.
     * @return <code>List</code> of <code>HeaderLine</code>
     */
    public List<HeaderLine> getHeaderList() {
        if (headerList != null) {
            return Collections.unmodifiableList(headerList);
        } else {
            return null;
        }
    }

    /**
     * Get a non-standard WARC header or null, if nothing is stored for this
     * header name.
     * @param field header name
     * @return WARC header line structure or null
     */
    public HeaderLine getHeader(String field) {
        if (headerMap != null && field != null) {
            return headerMap.get(field.toLowerCase());
        } else {
            return null;
        }
    }

    /**
     * Specifies whether this record has a payload or not.
     * @return true/false whether the ARC record has a payload
     */
    public boolean hasPayload() {
        return (payload != null);
    }

    /**
     * Return Payload object.
     * @return payload or <code>null</code>
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * Payload content <code>InputStream</code> getter.
     * @return Payload content <code>InputStream</code>
     */
    public InputStream getPayloadContent() {
        return (payload != null) ? payload.getInputStream() : null;
    }

    /**
     * Returns a boolean indicating the ISO compliance status of this record.
     * @return a boolean indicating the ISO compliance status of this record
     */
    public boolean isCompliant() {
        return bIsCompliant;
    }

    /**
     * Checks if the WARC record has errors.
     * @return true/false based on whether the WARC record is valid or not
     */
    /*
    public boolean hasErrors() {
        return ((errors != null) && (!errors.isEmpty()));
    }
    */

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
     * Add a warning diagnosis on the given entity stating that it is empty.
     * @param entity entity examined
     */
    protected void addEmptyWarning(String entity) {
        diagnostics.addWarning(new Diagnosis(DiagnosisType.EMPTY, entity));
    }

    /**
     * Add an error diagnosis on the given entity stating that it is invalid
     * and something else was expected. The optional information should provide
     * more details and/or format information.
     * @param entity entity examined
     * @param information optional extra information
     */
    protected void addInvalidExpectedError(String entity, String... information) {
        diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED, entity, information));
    }

    /**
     * Looks forward in the inputstream and counts the number of newlines
     * found. Non newlines characters are pushed back onto the inputstream.
     * @param in data inputstream
     * @return newlines found in inputstream
     * @throws IOException if an error occurs while reading data
     */
    protected int parseNewLines(ByteCountingPushBackInputStream in) throws IOException {
        int newlines = 0;
        byte[] buffer = new byte[2];
        boolean b = true;
        while (b) {
            int read = in.read(buffer);
            switch (read) {
            case 1:
                if (buffer[0] == '\n') {
                    ++newlines;
                } else {
                    in.unread(buffer[0]);
                    b = false;
                }
                break;
            case 2:
                if (buffer[0] == '\r' && buffer[1] == '\n') {
                    ++newlines;
                } else if (buffer[0] == '\n') {
                    ++newlines;
                    in.unread(buffer[1]);
                } else {
                    in.unread(buffer);
                    b = false;
                }
                break;
            default:
                b = false;
                break;
            }
        }
        return newlines;
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
            startOffset = in.getConsumed();
            line = reader.lineReader.readLine(in);
            if (line != null) {
                tmpStr = line.line;
                if (tmpStr != null) {
                    // debug
                    //System.out.println(tmpStr);
                    if (tmpStr.length() > 0) {
                        if (tmpStr.toUpperCase().startsWith(WarcConstants.WARC_MAGIC_HEADER)) {
                            bMagicIdentified = true;
                            String versionStr = tmpStr.substring(WarcConstants.WARC_MAGIC_HEADER.length());
                            String[] tmpArr = versionStr.split("\\.", -1);        // Not optimal
                            if (tmpArr.length >= 2 && tmpArr.length <= 4) {
                                bVersionParsed = true;
                                int[] versionArr = new int[tmpArr.length];
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
                            bSeekMagic = false;
                        } else {
                            // Invalid data aka Gibberish.
                            bInvalidDataBeforeVersion = true;
                        }
                    } else {
                        // Empty line.
                        bEmptyLinesBeforeVersion = true;

                    }
                } else {
                    // Headerline.
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
            if (headerLine != null) {
                // An empty line means the name/value pair was used.
                if (headerLine.line == null) {
                    if (headerLine.name != null && headerLine.name.length() > 0) {
                        // debug
                        //System.out.println(headerLine.name);
                        //System.out.println(headerLine.value);

                        parseField(headerLine, seen);
                    } else {
                        // Empty field name.
                        addWarningDiagnosis(DiagnosisType.EMPTY, "Header line");
                    }
                } else {
                    if (headerLine.line.length() == 0) {
                        // Empty line.
                        bFields = false;
                    } else {
                        // Unknown header line.
                        addWarningDiagnosis(DiagnosisType.UNKNOWN, "Header line", headerLine.line);
                    }
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
        Integer fn_idx = WarcConstants.fieldNameIdxMap.get(field.toLowerCase());
        if (fn_idx != null) {
            if (!seen[fn_idx] || WarcConstants.fieldNamesRepeatableLookup[fn_idx]) {
                seen[fn_idx] = true;
                switch (fn_idx.intValue()) {
                case WarcConstants.FN_IDX_WARC_TYPE:
                    warcTypeStr = parseString(value,
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
                    warcRecordIdUri = parseUri(value,
                            WarcConstants.FN_WARC_RECORD_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_DATE:
                    warcDateStr = value;
                    warcDate = parseDate(value,
                            WarcConstants.FN_WARC_DATE);
                    break;
                case WarcConstants.FN_IDX_CONTENT_LENGTH:
                    contentLengthStr = value;
                    contentLength = parseLong(value,
                            WarcConstants.FN_CONTENT_LENGTH);
                    break;
                case WarcConstants.FN_IDX_CONTENT_TYPE:
                    contentTypeStr = value;
                    contentType = parseContentType(value,
                            WarcConstants.FN_CONTENT_TYPE);
                    break;
                case WarcConstants.FN_IDX_WARC_CONCURRENT_TO:
                    if (value != null && value.trim().length() > 0) {
                        if (warcConcurrentToStrList == null) {
                            warcConcurrentToStrList = new ArrayList<String>();
                        }
                        warcConcurrentToStrList.add( value );
                    }
                    URI tmpUri = parseUri(value,
                            WarcConstants.FN_WARC_CONCURRENT_TO);
                    if (tmpUri != null) {
                        if (warcConcurrentToUriList == null) {
                            warcConcurrentToUriList = new ArrayList<URI>();
                        }
                        warcConcurrentToUriList.add(tmpUri);
                    }
                    break;
                case WarcConstants.FN_IDX_WARC_BLOCK_DIGEST:
                    warcBlockDigestStr = value;
                    warcBlockDigest = parseDigest(value,
                            WarcConstants.FN_WARC_BLOCK_DIGEST);
                    break;
                case WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST:
                    warcPayloadDigestStr = value;
                    warcPayloadDigest = parseDigest(value,
                            WarcConstants.FN_WARC_PAYLOAD_DIGEST);
                    break;
                case WarcConstants.FN_IDX_WARC_IP_ADDRESS:
                    warcIpAddress = value;
                    warcInetAddress = parseIpAddress(value,
                            WarcConstants.FN_WARC_IP_ADDRESS);
                    break;
                case WarcConstants.FN_IDX_WARC_REFERS_TO:
                    warcRefersToStr = value;
                    warcRefersToUri = parseUri(value,
                            WarcConstants.FN_WARC_REFERS_TO);
                    break;
                case WarcConstants.FN_IDX_WARC_TARGET_URI:
                    warcTargetUriStr = value;
                    warcTargetUriUri = parseUri(value,
                            WarcConstants.FN_WARC_TARGET_URI);
                    break;
                case WarcConstants.FN_IDX_WARC_TRUNCATED:
                    warcTruncatedStr = parseString(value,
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
                    warcWarcInfoIdUri = parseUri(value,
                            WarcConstants.FN_WARC_WARCINFO_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_FILENAME:
                    warcFilename = parseString(value,
                            WarcConstants.FN_WARC_FILENAME);
                    break;
                case WarcConstants.FN_IDX_WARC_PROFILE:
                    warcProfileStr = parseString(value,
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
                    warcIdentifiedPayloadType = parseContentType(value,
                            WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID:
                    warcSegmentOriginIdStr = value;
                    warcSegmentOriginIdUrl = parseUri(value,
                            WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER:
                    warcSegmentNumberStr = value;
                    warcSegmentNumber = parseInteger(value,
                            WarcConstants.FN_WARC_SEGMENT_NUMBER);
                    break;
                case WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH:
                    warcSegmentTotalLengthStr = value;
                    warcSegmentTotalLength = parseLong(value,
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
                headerList = new ArrayList<HeaderLine>();
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

            if (warcTypeIdx  > 0) {
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_CONTENT_TYPE, contentType, contentTypeStr);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_IP_ADDRESS, warcInetAddress, warcIpAddress);
                checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_WARC_CONCURRENT_TO, warcConcurrentToUriList, warcConcurrentToStrList);
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

    /**
     * Given a WARC record type and a WARC field looks up the policy in a
     * matrix build from the WARC ISO standard.
     * @param rtype WARC record type id
     * @param ftype WARC field type id
     * @param fieldObj WARC field
     * @param valueList WARC raw field values
     */
    protected void checkFieldPolicy(int rtype, int ftype, List<?> fieldObj, List<String> valueList) {
        String valueStr = null;
        int policy = WarcConstants.field_policy[rtype][ftype];
        switch (policy) {
        case WarcConstants.POLICY_MANDATORY:
            if (fieldObj == null) {
                valueStr = listToStr(valueList);
                addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_SHALL:
            if (fieldObj == null) {
                valueStr = listToStr(valueList);
                addErrorDiagnosis(DiagnosisType.REQUIRED_INVALID,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_MAY:
            break;
        case WarcConstants.POLICY_MAY_NOT:
            if (fieldObj != null) {
                valueStr = listToStr(valueList);
                addWarningDiagnosis(DiagnosisType.UNDESIRED_DATA,
                        "'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value",
                        valueStr);
            }
            break;
        case WarcConstants.POLICY_SHALL_NOT:
            if (fieldObj != null) {
                valueStr = listToStr(valueList);
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

    /**
     * Concatenate a <code>List</code> of strings into one single delimited
     * string.
     * @param list <code>List</code> of strings to concatenate
     * @return concatenated string
     */
    protected String listToStr(List<String> list) {
        StringBuffer sb = new StringBuffer();
        String str = null;
        if (list != null) {
            for (int i=0; i<list.size(); ++i) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(list.get(i));
            }
            str = sb.toString();
        }
        return str;
    }

    /**
     * Returns an Integer object holding the value of the specified string.
     * @param intStr the value to parse.
     * @param field field name
     * @return an integer object holding the value of the specified string
     */
    protected Integer parseInteger(String intStr, String field) {
         Integer iVal = null;
         if (intStr != null && intStr.length() > 0) {
            try {
                iVal = Integer.valueOf(intStr);
            } catch (Exception e) {
                // Invalid integer value.
                addInvalidExpectedError("'" + field + "' value",
                        intStr,
                        "Numeric format");
            }
         } else {
             // Missing integer value.
             addEmptyWarning("'" + field + "' field");
         }
         return iVal;
    }

    /**
     * Returns a Long object holding the value of the specified string.
     * @param longStr the value to parse.
     * @param field field name
     * @return a long object holding the value of the specified string
     */
    protected Long parseLong(String longStr, String field) {
        Long lVal = null;
         if (longStr != null && longStr.length() > 0) {
            try {
                lVal = Long.valueOf(longStr);
            } catch (Exception e) {
                // Invalid long value.
                addInvalidExpectedError("'" + field + "' value",
                        longStr,
                        "Numeric format");
            }
         } else {
             // Missing long value.
             addEmptyWarning("'" + field + "' field");
         }
         return lVal;
    }

    /**
     * Parses a string.
     * @param str the value to parse
     * @param field field name
     * @return the parsed value
     */
    protected String parseString(String str, String field) {
        if (((str == null) || (str.trim().length() == 0))) {
            addEmptyWarning("'" + field + "' field");
        }
        return str;
    }

    /**
     * Parses WARC record date.
     * @param dateStr the date to parse.
     * @param field field name
     * @return the formatted date.
     */
    protected Date parseDate(String dateStr, String field) {
        Date date = null;
        if (dateStr != null && dateStr.length() > 0) {
                date = WarcDateParser.getDate(dateStr);
                if (date == null) {
                    // Invalid date.
                    addInvalidExpectedError("'" + field + "' value",
                            dateStr,
                            WarcConstants.WARC_DATE_FORMAT);
                }
        } else {
            // Missing date.
            addEmptyWarning("'" + field + "' field");
        }
        return date;
    }

    /**
     * Parses WARC record IP address.
     * @param ipAddress the IP address to parse
     * @param field field name
     * @return the IP address
     */
    protected InetAddress parseIpAddress(String ipAddress, String field) {
        InetAddress inetAddr = null;
        if (ipAddress != null && ipAddress.length() > 0) {
            inetAddr = IPAddressParser.getAddress(ipAddress);
            if (inetAddr == null) {
                // Invalid ip address.
                addInvalidExpectedError("'" + field + "' value",
                        ipAddress,
                        "IPv4 or IPv6 format");
            }
        } else {
            // Missing ip address.
            addEmptyWarning("'" + field + "' field");
        }
        return inetAddr;
    }

    /**
     * Returns an URL object holding the value of the specified string.
     * @param uriStr the URL to parse
     * @param field field name
     * @return an URL object holding the value of the specified string
     */
    protected URI parseUri(String uriStr, String field) {
        URI uri = null;
        if (uriStr != null && uriStr.length() != 0) {
            if (uriStr.startsWith("<") && uriStr.endsWith(">")) {
                uriStr = uriStr.substring(1, uriStr.length() - 1);
            }
            try {
                uri = new URI(uriStr);
            } catch (Exception e) {
                // Invalid URI.
                addInvalidExpectedError("'" + field + "' value",
                        uriStr,
                        "URI format");
            }
        } else {
            // Missing URI.
            addEmptyWarning("'" + field + "' field");
        }
        return uri;
    }

    /**
     * Parse and validate content-type string with optional parameters.
     * @param contentTypeStr content-type string to parse
     * @param field field name
     * @return content-type wrapper object or null
     */
    protected ContentType parseContentType(String contentTypeStr, String field) {
        ContentType contentType = null;
        if (contentTypeStr != null && contentTypeStr.length() != 0) {
            contentType = ContentType.parseContentType(contentTypeStr);
            if (contentType == null) {
                // Invalid content-type.
                addInvalidExpectedError("'" + field + "' value",
                        contentTypeStr,
                        WarcConstants.CONTENT_TYPE_FORMAT);
            }
        } else {
            // Missing content-type.
            addEmptyWarning("'" + field + "' field");
        }
        return contentType;
    }

    /**
     * Parse and validate WARC digest string.
     * @param labelledDigest WARC digest string to parse
     * @param field field name
     * @return digest wrapper object or null
     */
    protected Digest parseDigest(String labelledDigest, String field) {
        Digest digest = null;
        if (labelledDigest != null && labelledDigest.length() > 0) {
                digest = WarcDigest.parseDigest(labelledDigest);
                if (digest == null) {
                    // Invalid digest.
                    addInvalidExpectedError("'" + field + "' value",
                            labelledDigest,
                            WarcConstants.WARC_DIGEST_FORMAT);
                }
        } else {
            // Missing digest.
            addEmptyWarning("'" + field + "' field");
        }
        return digest;
    }

    /*
    protected String readLine(PushbackInputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        int b;
        while (true) {
            b = in.read();
            if (b == -1) {
                return null;    //Unexpected EOF
            }
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                bos.write(b);
            }
        }
        return bos.toString("US-ASCII");
    }
    */

}
