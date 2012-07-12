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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jwat.common.Base16;
import org.jwat.common.Base32;
import org.jwat.common.Base64;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.HeaderLine;
import org.jwat.common.HttpHeader;
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

    /** Reader instance used, required for file compliance. */
    protected WarcReader reader;

    /** Bytes consumed while validating this record. */
    //long consumed = 0;

    /** Input stream used to read this record. */
    protected ByteCountingPushBackInputStream in;

    /** Is this record compliant ie. error free. */
    protected boolean bIsCompliant;

    /** Validation errors and warnings. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /** Did the reader detect a missing CR while parsing newlines. */
    protected boolean bMissingCr = false;

    /** Did the reader detect a missing LF while parsing newlines. */
    protected boolean bMissingLf = false;

    /** Did the reader detect a misplaced CR while parsing newlines. */
    protected boolean bMisplacedCr = false;

    /** Did the reader detect a misplaced LF while parsing newlines. */
    protected boolean bMisplacedLf = false;

    /** Is Warc-Block-Digest valid. (Null is equal to not tested) */
    public Boolean isValidBlockDigest = null;

    /** Is Warc-Payload-Digest valid. (Null is equal to not tested) */
    public Boolean isValidPayloadDigest = null;

    /*
     * Header-Fields.
     */

    /** WARC header. */
    public WarcHeader header;

    /*
     * Payload
     */

    /** Has payload been closed before. */
    protected boolean bPayloadClosed;

    /** Has record been closed before. */
    protected boolean bClosed;

    /** Payload object if any exists. */
    protected Payload payload;

    /** HTTP header content parsed from payload. */
    protected HttpHeader httpHeader;

    /** Computed block digest. */
    public WarcDigest computedBlockDigest;

    /** Computed payload digest. */
    public WarcDigest computedPayloadDigest;

    /**
     * Non public constructor to allow unit testing.
     */
    protected WarcRecord() {
    }

    public static WarcRecord createRecord(WarcWriter writer) {
        WarcRecord record = new WarcRecord();
        record.header = WarcHeader.initHeader(writer, record.diagnostics);
        writer.fieldParsers.diagnostics = record.diagnostics;
        return record;
    }

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
        WarcRecord record = new WarcRecord();
        record.in = in;
        record.reader = reader;
        // Initialize WarcHeader with required context.
        record.header = WarcHeader.initHeader(reader, in.getConsumed(), record.diagnostics);
        WarcHeader header = record.header;
        // Initialize WarcFieldParser to report diagnoses here.
        reader.fieldParsers.diagnostics = record.diagnostics;
        if (header.parseHeader(in)) {
            /*
             * Payload processing.
             */
            if (header.contentLength != null && header.contentLength > 0) {
                /*
                 * Payload.
                 */
                String digestAlgorithm = null;
                if (reader.bBlockDigest) {
                    if (header.warcBlockDigest != null && header.warcBlockDigest.algorithm != null) {
                        // If a WARC block digest header is present in the
                        // record, use that algorithm.
                        digestAlgorithm = header.warcBlockDigest.algorithm;
                    } else {
                        // If no WARC block digest header is present,
                        // use the optional user specified algorithm.
                        // Can be null in which case nothing is computed.
                        digestAlgorithm = reader.blockDigestAlgorithm;
                    }
                }
                record.payload = Payload.processPayload(in, header.contentLength,
                                         reader.payloadHeaderMaxSize, digestAlgorithm);
                record.payload.setOnClosedHandler(record);
                /*
                 * HttpHeader.
                 */
                if (header.contentType != null
                        && header.contentType.contentType.equals("application")
                        && header.contentType.mediaType.equals("http")) {
                    String value = header.contentType.getParameter("msgtype");
                    // request
                    int httpHeaderType = 0;
                    if ("response".equalsIgnoreCase(value)) {
                        httpHeaderType = HttpHeader.HT_RESPONSE;
                    } else if ("request".equalsIgnoreCase(value)) {
                        httpHeaderType = HttpHeader.HT_REQUEST;
                    }
                    if (httpHeaderType != 0) {
                        digestAlgorithm = null;
                        if (reader.bPayloadDigest) {
                            if (header.warcPayloadDigest != null && header.warcPayloadDigest.algorithm != null) {
                                // If a WARC payload digest header is present in the
                                // record, use that algorithm.
                                digestAlgorithm = header.warcPayloadDigest.algorithm;
                            } else {
                                // If no WARC payload digest header is present,
                                // use the optional user specified algorithm.
                                // Can be null in which case nothing is computed.
                                digestAlgorithm = reader.payloadDigestAlgorithm;
                            }
                        }
                        record.httpHeader = HttpHeader.processPayload(httpHeaderType,
                                record.payload.getInputStream(), header.contentLength,
                                digestAlgorithm);
                        if (record.httpHeader != null) {
                            if (record.httpHeader.isValid()) {
                                record.payload.setPayloadHeaderWrapped(record.httpHeader);
                            } else {
                                record.diagnostics.addError(
                                        new Diagnosis(DiagnosisType.ERROR,
                                                "http header",
                                                "Unable to parse http header!"));
                            }
                        }
                    }
                }
            }
            // Preliminary compliance status, will be updated when the
            // payload/record is closed.
            if (record.diagnostics.hasErrors() || record.diagnostics.hasWarnings()) {
                record.bIsCompliant = false;
            } else {
                record.bIsCompliant = true;
            }
            //wr.consumed = in.getConsumed() - header.startOffset;
            record.reader.bIsCompliant &= record.bIsCompliant;
        } else {
            if (record.diagnostics.hasErrors() || record.diagnostics.hasWarnings()) {
                record.reader.errors += record.diagnostics.getErrors().size();
                record.reader.warnings += record.diagnostics.getWarnings().size();
                record.reader.bIsCompliant = false;
            }
            // In case no record is found the errors/warnings in the record
            // object are transfered to the Reader.
            reader.diagnostics.addAll(record.diagnostics);
            // EOF
            record = null;
        }
        return record;
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
                // Check for computed block digest.
                if (md != null) {
                    computedBlockDigest = new WarcDigest();
                    computedBlockDigest.digestBytes = md.digest();
                }
                // Auto detect encoding used in WARC header.
                if (header.warcBlockDigest != null && header.warcBlockDigest.digestString != null) {
                    isValidBlockDigest = processWarcDigest(header.warcBlockDigest, computedBlockDigest, "block");
                }
                // Adjust information about computed block digest.
                if (computedBlockDigest != null) {
                    processComputedDigest(computedBlockDigest,
                            reader.blockDigestAlgorithm, reader.blockDigestEncoding, "block");
                }
                if (httpHeader != null && httpHeader.isValid()) {
                    /*
                     * Check payload digest.
                     */
                    md = httpHeader.getMessageDigest();
                    // Check for computed payload digest.
                    if (md != null) {
                        computedPayloadDigest = new WarcDigest();
                        computedPayloadDigest.digestBytes = md.digest();
                    }
                    // Auto detect encoding used in WARC header.
                    if (header.warcPayloadDigest != null && header.warcPayloadDigest.digestString != null ) {
                        isValidPayloadDigest = processWarcDigest(header.warcPayloadDigest, computedPayloadDigest, "payload");
                    }
                    // Adjust information about computed payload digest.
                    if (computedPayloadDigest != null) {
                        processComputedDigest(computedPayloadDigest,
                                reader.payloadDigestAlgorithm, reader.payloadDigestEncoding, "payload");
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
            if (bMissingCr) {
                addWarningDiagnosis(DiagnosisType.ERROR_EXPECTED,
                        "Missing CR");
            }
            if (bMissingLf) {
                addWarningDiagnosis(DiagnosisType.ERROR_EXPECTED,
                        "Missing LF");
            }
            if (bMisplacedCr) {
                addWarningDiagnosis(DiagnosisType.ERROR_EXPECTED,
                        "Misplaced CR");
            }
            if (bMisplacedLf) {
                addWarningDiagnosis(DiagnosisType.ERROR_EXPECTED,
                        "Misplaced LF");
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
            //consumed = in.getConsumed() - header.startOffset;
            //reader.consumed += consumed;
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
     * Auto-detect encoding used in WARC digest header and compare it to the
     * internal one, if it has been computed.
     * @param warcDigest digest from WARC header
     * @param computedDigest internally compute digest
     * @return WARC digest validity indication
     */
    protected Boolean processWarcDigest(WarcDigest warcDigest, WarcDigest computedDigest, String digestName) {
        byte[] digest;
        Boolean isValidDigest = null;
        int digestAlgorithmLength = WarcDigest.digestAlgorithmLength(warcDigest.algorithm);
        digest = Base16.decodeToArray(warcDigest.digestString);
        if (digest != null && digest.length == digestAlgorithmLength) {
            warcDigest.digestBytes = digest;
            warcDigest.encoding = "base16";
        }
        if (warcDigest.digestBytes == null) {
            digest = Base32.decodeToArray(warcDigest.digestString, true);
            if (digest != null && digest.length == digestAlgorithmLength) {
                warcDigest.digestBytes = digest;
                warcDigest.encoding = "base32";
            }
            if (warcDigest.digestBytes == null) {
                digest = Base64.decodeToArray(warcDigest.digestString, true);
                if (digest != null && digest.length == digestAlgorithmLength) {
                    warcDigest.digestBytes = digest;
                    warcDigest.encoding = "base64";
                }
            }
        }
        if (warcDigest.encoding == null) {
            // Encoding - Unrecognized block digest encoding scheme
            addErrorDiagnosis(DiagnosisType.UNKNOWN,
                    "Record " + digestName + " digest encoding scheme",
                    warcDigest.digestString);
        }
        if (computedDigest != null) {
            computedDigest.algorithm = warcDigest.algorithm;
            computedDigest.encoding = warcDigest.encoding;
            if (warcDigest.digestBytes != null) {
                if (!Arrays.equals(computedDigest.digestBytes, warcDigest.digestBytes)) {
                    // Block digest - Computed block digest does not match
                    addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                            "Incorrect " + digestName + " digest",
                            Base16.encodeArray(warcDigest.digestBytes),
                            Base16.encodeArray(computedDigest.digestBytes));
                    isValidDigest = false;
                } else {
                    isValidDigest = true;
                }
            } else {
                isValidDigest = false;
            }
        }
        return isValidDigest;
    }

    /**
     * Adjust algorithm and encoding information about computed block digest.
     * @param computedDigest internally compute digest
     * @param digestAlgorithm default algorithm
     * @param digestEncoding default encoding
     */
    protected void processComputedDigest(WarcDigest computedDigest, String digestAlgorithm, String digestEncoding, String digestName) {
        if (computedDigest.algorithm == null) {
            computedDigest.algorithm = digestAlgorithm;
        }
        if (computedDigest.encoding == null && digestEncoding != null) {
            if ("base32".equals(digestEncoding)) {
                computedDigest.encoding = "base32";
            } else if ("base64".equals(digestEncoding)) {
                computedDigest.encoding = "base64";
            } else if ("base16".equals(digestEncoding)) {
                computedDigest.encoding = "base16";
            } else {
                // Encoding - Unknown block digest encoding scheme ..
                addErrorDiagnosis(DiagnosisType.UNKNOWN,
                        "Default " + digestName + " digest encoding scheme",
                        digestEncoding);
            }
        }
        if (computedDigest.encoding != null) {
            if ("base32".equals(computedDigest.encoding)) {
                computedDigest.digestString = Base32.encodeArray(computedDigest.digestBytes);
            } else if ("base64".equals(computedDigest.encoding)) {
                computedDigest.digestString = Base64.encodeArray(computedDigest.digestBytes);
            } else if ("base16".equals(computedDigest.encoding)) {
                computedDigest.digestString = Base16.encodeArray(computedDigest.digestBytes);
            }
        }
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
        return header.startOffset;
    }

    /**
     * Return number of bytes consumed validating this record.
     * @return number of bytes consumed validating this record
     */
    /*
    public Long getConsumed() {
        return consumed;
    }
    */

    /**
     * Get a <code>List</code> of all the non-standard WARC headers found
     * during parsing.
     * @return <code>List</code> of <code>HeaderLine</code>
     */
    public List<HeaderLine> getHeaderList() {
        return Collections.unmodifiableList(header.headerList);
    }

    /**
     * Get a non-standard WARC header or null, if nothing is stored for this
     * header name.
     * @param field header name
     * @return WARC header line structure or null
     */
    public HeaderLine getHeader(String field) {
        if (field != null && field.length() > 0) {
            return header.headerMap.get(field.toLowerCase());
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
     * Looks forward in the inputstream and counts the number of newlines
     * found. Non newlines characters are pushed back onto the inputstream.
     * @param in data inputstream
     * @return newlines found in inputstream
     * @throws IOException if an error occurs while reading data
     */
    protected int parseNewLines(ByteCountingPushBackInputStream in) throws IOException {
        bMissingCr = false;
        bMissingLf = false;
        bMisplacedCr = false;
        bMisplacedLf = false;
        int newlines = 0;
        byte[] buffer = new byte[2];
        boolean b = true;
        while (b) {
            int read = in.read(buffer);
            switch (read) {
            case 1:
                if (buffer[0] == '\n') {
                    ++newlines;
                    bMissingCr = true;
                } else if ((buffer[0] == '\r')) {
                    ++newlines;
                    bMissingLf = true;
                } else {
                    in.unread(buffer[0]);
                    b = false;
                }
                break;
            case 2:
                if (buffer[0] == '\r') {
                    if (buffer[1] == '\n') {
                        ++newlines;
                    } else {
                        ++newlines;
                        bMissingLf = true;
                        in.unread(buffer[1]);
                    }
                } else if (buffer[0] == '\n') {
                    if (buffer[1] == '\r') {
                        ++newlines;
                        bMisplacedCr = true;
                        bMisplacedLf = true;
                    } else {
                        ++newlines;
                        bMissingCr = true;
                        in.unread(buffer[1]);
                    }
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

}
