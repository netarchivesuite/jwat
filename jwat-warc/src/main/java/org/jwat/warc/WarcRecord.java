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
import org.jwat.common.Digest;
import org.jwat.common.HeaderLine;
import org.jwat.common.HttpResponse;
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

    /** Bytes consumed while validating this record. */
    long consumed = 0;

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

    /** HttpResponse header content parse from payload. */
    protected HttpResponse httpResponse;

    /** Computed block digest. */
    public Digest computedBlockDigest;

    /** Computed payload digest. */
    public Digest computedPayloadDigest;

    /**
     * Non public constructor to allow unit testing.
     */
    protected WarcRecord() {
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
        WarcRecord wr = new WarcRecord();
        wr.in = in;
        wr.reader = reader;
        // Initialize WarcHeader with required context.
        wr.header = WarcHeader.initHeader(reader, in.getConsumed(), wr.diagnostics);
        WarcHeader header = wr.header;
        // Initialize WarcFieldParser to report diagnoses here.
        reader.fieldParser.diagnostics = wr.diagnostics;
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
                wr.payload = Payload.processPayload(in, header.contentLength,
                                         PAYLOAD_PUSHBACK_SIZE, digestAlgorithm);
                wr.payload.setOnClosedHandler(wr);
                /*
                 * HttpResponse.
                 */
                if (header.contentType != null
                        && header.contentType.contentType.equals("application")
                        && header.contentType.mediaType.equals("http")) {
                    String value = header.contentType.getParameter("msgtype");
                    // request
                    if ("response".equals(value)) {
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
                        wr.httpResponse = HttpResponse.processPayload(
                                wr.payload.getInputStream(), header.contentLength,
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
            wr.consumed = in.getConsumed() - header.startOffset;
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
                if (header.warcBlockDigest != null && header.warcBlockDigest.digestString != null) {
                    if (computedBlockDigest != null) {
                        computedBlockDigest.algorithm = header.warcBlockDigest.algorithm;
                        if ((computedBlockDigest.digestBytes.length + 2) / 3 * 4 == header.warcBlockDigest.digestString.length()) {
                            digest = Base64.decodeToArray(header.warcBlockDigest.digestString, true);
                            header.warcBlockDigest.encoding = "base64";
                            computedBlockDigest.encoding = header.warcBlockDigest.encoding;
                        } else if ((computedBlockDigest.digestBytes.length + 4) / 5 * 8 == header.warcBlockDigest.digestString.length()) {
                            digest = Base32.decodeToArray(header.warcBlockDigest.digestString, true);
                            header.warcBlockDigest.encoding = "base32";
                            computedBlockDigest.encoding = header.warcBlockDigest.encoding;
                        } else if (computedBlockDigest.digestBytes.length * 2 == header.warcBlockDigest.digestString.length()) {
                            digest = Base16.decodeToArray(header.warcBlockDigest.digestString);
                            header.warcBlockDigest.encoding = "base16";
                            computedBlockDigest.encoding = header.warcBlockDigest.encoding;
                        } else {
                            digest = null;
                            // Encoding - Unrecognized block digest encoding scheme
                            addErrorDiagnosis(DiagnosisType.UNKNOWN,
                                    "Block digest encoding scheme",
                                    header.warcBlockDigest.digestString);
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
                    if (header.warcPayloadDigest != null && header.warcPayloadDigest.digestString != null ) {
                        if (computedPayloadDigest != null) {
                            computedPayloadDigest.algorithm = header.warcPayloadDigest.algorithm;
                            if ((computedPayloadDigest.digestBytes.length + 2) / 3 * 4 == header.warcPayloadDigest.digestString.length()) {
                                digest = Base64.decodeToArray(header.warcPayloadDigest.digestString, true);
                                header.warcPayloadDigest.encoding = "base64";
                                computedPayloadDigest.encoding = header.warcPayloadDigest.encoding;
                            } else if ((computedPayloadDigest.digestBytes.length + 4) / 5 * 8 == header.warcPayloadDigest.digestString.length()) {
                                digest = Base32.decodeToArray(header.warcPayloadDigest.digestString, true);
                                header.warcPayloadDigest.encoding = "base32";
                                computedPayloadDigest.encoding = header.warcPayloadDigest.encoding;
                            } else if (computedPayloadDigest.digestBytes.length * 2 == header.warcPayloadDigest.digestString.length()) {
                                digest = Base16.decodeToArray(header.warcPayloadDigest.digestString);
                                header.warcPayloadDigest.encoding = "base16";
                                computedPayloadDigest.encoding = header.warcPayloadDigest.encoding;
                            } else {
                                digest = null;
                                // Encoding - Unrecognized payload digest encoding scheme
                                addErrorDiagnosis(DiagnosisType.UNKNOWN,
                                        "Payload digest encoding scheme",
                                        header.warcPayloadDigest.digestString);
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
            consumed = in.getConsumed() - header.startOffset;
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
        return header.startOffset;
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
        if (header.headerList != null) {
            return Collections.unmodifiableList(header.headerList);
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
        if (header.headerMap != null && field != null) {
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

}
