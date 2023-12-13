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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
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
import org.jwat.common.DigestInputStreamChunkedNoSkip;
import org.jwat.common.HeaderLine;
import org.jwat.common.HttpHeader;
import org.jwat.common.NewlineParser;
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
public class WarcRecord implements PayloadOnClosedHandler, Closeable {

    /** Reader instance used, required for file compliance. */
    protected WarcReader reader;

    /** Input stream used to read this record. */
    protected ByteCountingPushBackInputStream in;

    /** Is this record compliant ie. error free. */
    protected boolean bIsCompliant;

    /** WARC record parsing start offset relative to the source WARC file input
     *  stream. Used to keep track of the uncompressed amount of bytes consumed. */
    protected long startOffset = -1;

    /** Uncompressed bytes consumed while validating this record. */
    protected long consumed;

    /** Validation errors and warnings. */
    public final Diagnostics diagnostics = new Diagnostics();

    /** Newline parser for counting/validating trailing newlines. */
    public NewlineParser nlp = new NewlineParser();

    /** Is Warc-Block-Digest valid. (Null is equal to not tested) */
    public Boolean isValidBlockDigest = null;

    /** Is Warc-Payload-Digest valid. (Null is equal to not tested) */
    public Boolean isValidPayloadDigest = null;

    /** Number of trailing newlines after record. */
    public int trailingNewlines;

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

    public WarcDigest computedChunkedDigest;

    /**
     * Non public constructor to allow unit testing.
     */
    protected WarcRecord() {
    }

    /**
     * Create a <code>WarcRecord</code> and prepare it for writing.
     * @param writer writer which will be used to write the record
     * @return a <code>WarcRecord</code> ready to be changed and then written
     */
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
     * @throws IOException I/O exception in the process of reading record
     */
    public static WarcRecord parseRecord(ByteCountingPushBackInputStream in,
                                    WarcReader reader) throws IOException {
        WarcRecord record = new WarcRecord();
        record.in = in;
        record.reader = reader;
        record.startOffset = in.getConsumed();
        /*
        if (record.startOffset == 569895) {
            System.out.println("Debug point.");
        }
        */
        // Initialize WarcHeader with required context.
        record.header = WarcHeader.initHeader(reader, in.getConsumed(), record.diagnostics);
        WarcHeader header = record.header;
        // Initialize WarcFieldParser to report diagnoses here.
        reader.fieldParsers.diagnostics = record.diagnostics;
        if (header.parseHeader(in)) {
            ++reader.records;
            if (reader.wrpCallback != null) {
                reader.wrpCallback.warcParsedRecordHeader(reader, record.startOffset, header);
            }
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
                        // Try to read a valid HTTP request/response header from the payload.
                        record.httpHeader = HttpHeader.processPayload(httpHeaderType,
                                record.payload.getInputStream(), header.contentLength,
                                digestAlgorithm);
                        if (record.httpHeader != null) {
                            if (record.httpHeader.isValid()) {
                                record.payload.setPayloadHeaderWrapped(record.httpHeader);
                            } else if (reader.bReportHttpHeaderError) {
                                record.diagnostics.addWarning(
                                        DiagnosisType.ERROR, "http header", "Unable to parse http header!");
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
            reader.bIsCompliant &= record.bIsCompliant;
        } else {
            // In case no record is found the errors/warnings in the record object are transfered to the Reader.
            long excess = in.getConsumed() - record.startOffset;
            reader.consumed += excess;
            reader.diagnostics.addAll(record.diagnostics);
            if (record.diagnostics.hasErrors() || record.diagnostics.hasWarnings()) {
                reader.errors += record.diagnostics.getErrors().size();
                reader.warnings += record.diagnostics.getWarnings().size();
                reader.bIsCompliant = false;
            }
            // Require one or more records to be present.
            if (reader.records == 0) {
                reader.diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "WARC file", "One or more records"));
                ++reader.errors;
                reader.bIsCompliant = false;
            }
            if (excess != 0) {
                reader.diagnostics.addError(new Diagnosis(DiagnosisType.UNDESIRED_DATA, "Trailing data", "Garbage data found at offset=" + record.startOffset + " - length=" + excess));
            }
            // EOF
            record = null;
        }
        return record;
    }

    /**
     * Called when the payload object is closed and final steps in the
     * validation process can be performed.
     * @throws IOException I/O exception in final validation processing
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
                byte[] digest = payload.getDigest();
                byte[] chunkedDigest;
                // Check for computed block digest.
                if (digest != null) {
                    computedBlockDigest = new WarcDigest();
                    computedBlockDigest.digestBytes = digest;
                }
                // Auto detect encoding used in WARC header.
                if (header.warcBlockDigest != null && header.warcBlockDigest.digestString != null) {
                    deduceWarcDigestEncoding(header.warcBlockDigest, "block");
                    isValidBlockDigest = processWarcDigest(header.warcBlockDigest, computedBlockDigest, null, "block");
                }
                // Adjust information about computed block digest.
                if (computedBlockDigest != null) {
                    encodeComputedDigest(computedBlockDigest,
                            reader.blockDigestAlgorithm, reader.blockDigestEncoding, "block");
                }
                // Revisit payload digest refers to the original. Continuation payload digest in first record also refers to original.
                if ((header.warcTypeIdx != null && header.warcTypeIdx != WarcConstants.RT_IDX_REVISIT && header.warcTypeIdx != WarcConstants.RT_IDX_CONTINUATION) && httpHeader != null && httpHeader.isValid()) {
                    /*
                     * Check payload digest.
                     */
                    digest = httpHeader.getPayloadDigest();
                    chunkedDigest = httpHeader.getChunkedDigest();
                    // Check for computed payload digest.
                    if (digest != null) {
                        computedPayloadDigest = new WarcDigest();
                        computedPayloadDigest.digestBytes = digest;
                    }
                    if (chunkedDigest != null) {
                        computedChunkedDigest = new WarcDigest();
                        computedChunkedDigest.digestBytes = chunkedDigest;
                    }
                    // Auto detect encoding used in WARC header.
                    if (header.warcPayloadDigest != null && header.warcPayloadDigest.digestString != null ) {
                        deduceWarcDigestEncoding(header.warcPayloadDigest, "payload");
                        isValidPayloadDigest = processWarcDigest(header.warcPayloadDigest, computedPayloadDigest, computedChunkedDigest, "payload");
                    }
                    // Adjust information about computed payload digest.
                    if (computedPayloadDigest != null) {
                        encodeComputedDigest(computedPayloadDigest,
                                reader.payloadDigestAlgorithm, reader.payloadDigestEncoding, "payload");
                    }
                }
            }
            // Check for trailing newlines.
            trailingNewlines = nlp.parseCRLFs(in, diagnostics);
            if (trailingNewlines != WarcConstants.WARC_RECORD_TRAILING_NEWLINES) {
                addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                        "Trailing newlines",
                        Integer.toString(trailingNewlines),
                        Integer.toString(WarcConstants.WARC_RECORD_TRAILING_NEWLINES));
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
            // Don't not close payload again.
            bPayloadClosed = true;
            // Callback.
            reader.recordClosed();
        }
    }

    protected void deduceWarcDigestEncoding(WarcDigest warcDigest, String digestName) {
        byte[] digest;
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
    }

    /**
     * Auto-detect encoding used in WARC digest header and compare it to the
     * internal one, if it has been computed.
     * @param warcDigest digest from WARC header
     * @param computedDigest internally compute digest
     * @param digestName used to identify the digest ("block" or "payload")
     * @return WARC digest validity indication
     */
    protected Boolean processWarcDigest(WarcDigest warcDigest, WarcDigest computedDigest, WarcDigest chunkedDigest, String digestName) {
        Boolean isValidDigest = null;
        String tmpStr;
        if (computedDigest != null) {
            computedDigest.algorithm = warcDigest.algorithm;
            computedDigest.encoding = warcDigest.encoding;
            if (chunkedDigest != null) {
                chunkedDigest.algorithm = warcDigest.algorithm;
                chunkedDigest.encoding = warcDigest.encoding;
            }
            if (warcDigest.digestBytes != null) {
                if (Arrays.equals(computedDigest.digestBytes, warcDigest.digestBytes)) {
                    isValidDigest = true;
                } else {
                    isValidDigest = false;
                }
                if (!isValidDigest) {
                    if (chunkedDigest != null) {
                        if (Arrays.equals(chunkedDigest.digestBytes, warcDigest.digestBytes)) {
                            isValidDigest = true;
                        }
                    }
                }
                if (!isValidDigest) {
                    tmpStr = Base16.encodeArray(computedDigest.digestBytes);
                    if (chunkedDigest != null) {
                        tmpStr += "/" + Base16.encodeArray(chunkedDigest.digestBytes);
                    }
                    addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                            "Incorrect " + digestName + " digest",
                            Base16.encodeArray(warcDigest.digestBytes),
                            tmpStr);
                    if (chunkedDigest != null) {
                        if (httpHeader.digestISChunked.getState() != DigestInputStreamChunkedNoSkip.S_DONE) {
                            addWarningDiagnosis(DiagnosisType.INVALID_ENCODING,
                                    "Chunked HTTP payload",
                                    "Invalid transfer-encoding",
                                    "RFC 9112 - HTTP/1.1");
                        }
                    }
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
     * @param digestName used to identify the digest ("block" or "payload")
     */
    protected void encodeComputedDigest(WarcDigest computedDigest, String digestAlgorithm, String digestEncoding, String digestName) {
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
     * Returns a boolean indicating the ISO compliance status of this record.
     * @return a boolean indicating the ISO compliance status of this record
     */
    public boolean isCompliant() {
        return bIsCompliant;
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
     * Return number of uncompressed bytes consumed validating this record.
     * @return number of uncompressed bytes consumed validating this record
     */
    public long getConsumed() {
        return consumed;
    }

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
     * @return <code>HeaderLine</code> structure or null
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
     * Returns the <code>HttpHeader</code> object if identified in the payload,
     * or null.
     * @return the <code>HttpHeader</code> object if identified or null
     */
    public HttpHeader getHttpHeader() {
        return httpHeader;
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

}
