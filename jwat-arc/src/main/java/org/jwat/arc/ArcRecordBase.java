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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Date;

import org.jwat.common.Base16;
import org.jwat.common.Base32;
import org.jwat.common.Base64;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.Digest;
import org.jwat.common.HttpHeader;
import org.jwat.common.NewlineParser;
import org.jwat.common.Payload;
import org.jwat.common.PayloadOnClosedHandler;
import org.jwat.common.PayloadWithHeaderAbstract;
import org.jwat.common.Uri;

/**
 * This abstract class represents the common base ARC data which is present in
 * both a record or version block. The parser in this class is responsible for
 * parsing the general record line and determining if the rest of the record
 * should be processed as an ARC record or an ARC version block.
 * This includes possible common validation and format warnings/errors
 * encountered in the process.
 *
 * @author lbihanic, selghissassi, nicl
 */
public abstract class ArcRecordBase implements PayloadOnClosedHandler, Closeable {

    /** Version block record type. */
    public static final int RT_VERSION_BLOCK = 1;

    /** Arc record record type. */
    public static final int RT_ARC_RECORD = 2;

    /** Buffer size used in toString(). */
    public static final int TOSTRING_BUFFER_SIZE = 256;

    /** Is this record compliant ie. error free. */
    protected boolean bIsCompliant;

    /** Reader instance used, required for file compliance. */
    protected ArcReader reader;

    /** Input stream used to read this record. */
    protected ByteCountingPushBackInputStream in;

    /** ARC record version. */
    protected ArcVersion version;

    /** ARC record parsing start offset relative to the source ARC file input
     *  stream. Used to keep track of the uncompressed amount of bytes consumed. */
    protected long startOffset = -1;

    /** Uncompressed bytes consumed while validating this record. */
    protected long consumed;

    /** Validation errors and warnings. */
    public Diagnostics<Diagnosis> diagnostics;

    /** Newline parser for counting/validating trailing newlines. */
    public NewlineParser nlp = new NewlineParser();

    /** Record type, version block or arc record. */
    public int recordType;

    /** Number of trailing newlines after record. */
    public int trailingNewLines;

    /*
     * Header-Fields.
     */

    /** ARC record header. */
    public ArcHeader header;

    /** ARC version header. */
    public ArcVersionHeader versionHeader;

    /*
     * Payload
     */

    /** Has payload been closed before. */
    protected boolean bPayloadClosed;

    /** Has record been closed flag. */
    protected boolean bClosed;

    /** Payload object if any exists. */
    protected Payload payload;

    /** Has all the payload data been processed while reading the record header. */
    protected boolean bHasPseudoEmptyPayload;

    /** HTTP header content parsed from payload. */
    protected HttpHeader httpHeader;

    /** Computed block digest. */
    public Digest computedBlockDigest;

    /** Computed payload digest. */
    public Digest computedPayloadDigest;

    /** This array is used to store metadata which should not be present.
     *  Only available for forensic purposes. */
    public byte[] excessiveMetadata;

    /**
     * Reads from the input stream and tries to parse and identify an
     * <code>ArcRecord</code> or <code>ArcVersionBlock</code> record.
     * @param in input stream with ARC record data
     * @param reader ARC reader used with access to overall configuration and status
     * @return <code>ArcRecord</code>, <code>ArcVersionBlock</code> or null
     * @throws IOException I/O exception while parsing ARC record data
     */
    public static ArcRecordBase parseRecord(ByteCountingPushBackInputStream in, ArcReader reader) throws IOException {
        ArcRecordBase record = null;
        long startOffset = in.getConsumed();
        // Initialize ArcHeader with required context.
        Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();
        ArcHeader header = ArcHeader.initHeader(reader, startOffset, diagnostics);
        // Initialize ArcFieldParser to report diagnoses here.
        reader.fieldParsers.diagnostics = diagnostics;
        // Returns true if a record has been processed.
        // And false if only garbage has been processed.
        if (header.parseHeader(in)) {
            if (reader.arpCallback != null) {
                reader.arpCallback.arcParsedRecordHeader(reader, startOffset, header);
            }
            if (header.urlScheme != null && header.urlScheme.startsWith(ArcConstants.ARC_SCHEME)) {
                record = ArcVersionBlock.parseVersionBlock(reader, diagnostics, header, reader.fieldParsers, in);
                if (record != null) {
                    reader.versionHeader = record.versionHeader;
                }
            }
            if (record == null) {
                record = ArcRecord.parseArcRecord(reader, diagnostics, header, in);
                if (record != null && reader.versionHeader != null) {
                    record.version = reader.versionHeader.version;
                }
            }
        }
        // Record identified above.
        if (record != null) {
            ++reader.records;
            record.startOffset = startOffset;
            // Check read and computed offset value only if we're reading
            // a plain ARC file, not a GZipped ARC.
            if ((header.offset != null) && (header.startOffset > 0L)
                                && (header.offset.longValue() != header.startOffset)) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED,
                        "'" + ArcConstants.FN_OFFSET + "' value",
                        header.offset.toString(),
                        Long.toString(header.startOffset)));
            }
            if (reader.records == 1) {
                if (record.recordType == ArcRecordBase.RT_ARC_RECORD) {
                    diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                            ArcConstants.ARC_FILE,
                            "Expected a version block as the first record."));
                }
            } else {
                if (record.recordType == ArcRecordBase.RT_VERSION_BLOCK) {
                    diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                            ArcConstants.ARC_FILE,
                            "Expected an ARC record not version block."));
                }
            }
            if (reader.versionHeader != null && reader.versionHeader.blockDescVersion > 0
                    && record.header.recordFieldVersion != reader.versionHeader.blockDescVersion) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED,
                        "ARC record does not match the version block definition",
                        Integer.toString(record.header.recordFieldVersion),
                        Integer.toString(reader.versionHeader.blockDescVersion)));
            }
            // Preliminary compliance status, will be updated when the
            // payload/record is closed.
            if (diagnostics.hasErrors() || diagnostics.hasWarnings()) {
                record.bIsCompliant = false;
            } else {
                record.bIsCompliant = true;
            }
            reader.bIsCompliant &= record.bIsCompliant;
        } else {
            // Transfer errors/warnings identified in the header parser to the reader since we are not returning a record.
            long excess = in.getConsumed() - startOffset;
            reader.consumed += excess;
            reader.diagnostics.addAll(diagnostics);
            if (diagnostics.hasErrors() || diagnostics.hasWarnings()) {
                reader.errors += diagnostics.getErrors().size();
                reader.warnings += diagnostics.getWarnings().size();
                reader.bIsCompliant = false;
            }
            // Require one or more records to be present.
            if (reader.records == 0) {
                reader.diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "ARC file", "One or more records"));
                ++reader.errors;
                reader.bIsCompliant = false;
            }
            if (excess != 0) {
                reader.diagnostics.addError(new Diagnosis(DiagnosisType.UNDESIRED_DATA, "Trailing data", "Garbage data found at offset=" + startOffset + " - length=" + excess));
            }
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
                if (digest != null) {
                    computedBlockDigest = new Digest();
                    computedBlockDigest.digestBytes = digest;
                    computedBlockDigest.algorithm = reader.blockDigestAlgorithm;
                    if (reader.blockDigestEncoding != null) {
                        if ("base32".equals(reader.blockDigestEncoding)) {
                            computedBlockDigest.encoding = "base32";
                            computedBlockDigest.digestString = Base32.encodeArray(computedBlockDigest.digestBytes);
                        } else if ("base64".equals(reader.blockDigestEncoding)) {
                            computedBlockDigest.encoding = "base64";
                            computedBlockDigest.digestString = Base64.encodeArray(computedBlockDigest.digestBytes);
                        } else if ("base16".equals(reader.blockDigestEncoding)) {
                            computedBlockDigest.encoding = "base16";
                            computedBlockDigest.digestString = Base16.encodeArray(computedBlockDigest.digestBytes);
                        } else {
                            // Encoding - Unknown block digest encoding scheme ..
                            addErrorDiagnosis(DiagnosisType.INVALID_DATA,
                                    "Block digest encoding scheme",
                                    reader.blockDigestEncoding);
                        }
                    }
                }
                PayloadWithHeaderAbstract payloadHeaderWrapped = payload.getPayloadHeaderWrapped();
                if (payloadHeaderWrapped != null && payloadHeaderWrapped.isValid()) {
                    /*
                     * Check payload digest.
                     */
                    digest = payloadHeaderWrapped.getDigest();
                    if (digest != null) {
                        computedPayloadDigest = new Digest();
                        computedPayloadDigest.digestBytes = digest;
                        computedPayloadDigest.algorithm = reader.payloadDigestAlgorithm;
                        if (reader.payloadDigestEncoding != null) {
                            if ("base32".equals(reader.payloadDigestEncoding)) {
                                computedPayloadDigest.encoding = "base32";
                                computedPayloadDigest.digestString = Base32.encodeArray(computedPayloadDigest.digestBytes);
                            } else if ("base64".equals(reader.payloadDigestEncoding)) {
                                computedPayloadDigest.encoding = "base64";
                                computedPayloadDigest.digestString = Base64.encodeArray(computedPayloadDigest.digestBytes);
                            } else if ("base16".equals(reader.payloadDigestEncoding)) {
                                computedPayloadDigest.encoding = "base16";
                                computedPayloadDigest.digestString = Base16.encodeArray(computedPayloadDigest.digestBytes);
                            } else {
                                // Encoding - Unknown payload digest encoding scheme ..
                                addErrorDiagnosis(DiagnosisType.INVALID_DATA,
                                        "Payload digest encoding scheme",
                                        reader.payloadDigestEncoding);
                            }
                        }
                    }
                }
            }
            // Check for trailing newlines.
            trailingNewLines = nlp.parseLFs(in, diagnostics);
            if (reader.bStrict && trailingNewLines != ArcConstants.ARC_RECORD_TRAILING_NEWLINES) {
                addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                        "Trailing newlines",
                        Integer.toString(trailingNewLines),
                        Integer.toString(ArcConstants.ARC_RECORD_TRAILING_NEWLINES));
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

    /**
     * Check to see if the record has been closed.
     * @return boolean indicating whether this record is closed or not
     */
    public boolean isClosed() {
        return bClosed;
    }

    /**
     * Close resources associated with the ARC record.
     * Mainly payload stream if any.
     * @throws IOException I/O exception close the payload resources
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
     * Returns a boolean indicating the standard compliance of this record.
     * @return a boolean indicating the standard compliance of this record
     */
    public boolean isCompliant() {
        return bIsCompliant;
    }

    /**
     * Returns the parsing start offset of the record in the containing ARC.
     * @return the parsing start offset of the record
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
     * Process the ARC record stream for possible payload data.
     * @param in ARC record <code>InputStream</code>
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @throws IOException I/O exception in the parsing process
     */
    protected abstract void processPayload(ByteCountingPushBackInputStream in,
                                        ArcReader reader) throws IOException;

    /**
     * Specifies whether this record has a payload or not.
     * @return true/false whether the ARC record has a payload
     */
    public boolean hasPayload() {
        return (payload != null);
    }

    /**
     * Specifies whether this record has had all it's payload processed already.
     * @return true/false whether this record's payload has been completely processed
     */
    public boolean hasPseudoEmptyPayload() {
        return bHasPseudoEmptyPayload;
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
     * Parses the remaining input stream and validates that the characters
     * encountered are equal to LF or CR.
     * @param in <code>InputStream</code> to validate
     * @return true/false based on whether the remaining input stream contains
     * only LF and CR characters or not.
     * @throws IOException I/O exception in parsing
     */
    public boolean isValidStreamOfCRLF(InputStream in) throws IOException{
        if (in == null) {
            throw new IllegalArgumentException("'in' is null!");
        }
        boolean isValid = true;
        int b;
        while ((b = in.read()) != -1) {
            if (b != '\n' && b != '\r') {
                isValid = false;
                break;
            }
        }
        return isValid;
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
    /*
    protected void addWarningDiagnosis(DiagnosisType type, String entity, String... information) {
        diagnostics.addWarning(new Diagnosis(type, entity, information));
    }
    */

    /**
     * Version getter.
     * @return the version
     */
    public ArcVersion getVersion() {
        return version;
    }

    /**
     * Raw URL getter.
     * @return the raw URL
     */
    public String getUrlStr() {
        return header.urlStr;
    }

    /**
     * URL getter.
     * @return the URL
     */
    public Uri getUrl() {
        return header.urlUri;
    }

    /**
     * Protocol getter.
     * @return the protocol
     */
    public String getScheme() {
        return header.urlScheme;
    }

    /**
     * IpAddress getter.
     * @return the ipAddress
     */
    public String getIpAddress() {
        return header.ipAddressStr;
    }

    /**
     * <code>inetAddress</code> getter.
     * @return the InetAddress
     */
    public InetAddress getInetAddress() {
        return header.inetAddress;
    }

    /**
     * Raw ArchiveDate getter.
     * @return the rawArchiveDate
     */
    public String getArchiveDateStr() {
        return header.archiveDateStr;
    }

    /**
     * ArchiveDate getter.
     * @return the archiveDate
     */
    public Date getArchiveDate() {
        return header.archiveDate;
    }

    /**
     * Raw Content-Type getter.
     * @return the contentType
     */
    public String getContentTypeStr() {
        return header.contentTypeStr;
    }

    /**
     * Content-Type getter.
     * @return the contentType
     */
    public ContentType getContentType() {
        return header.contentType;
    }

    /**
     * Raw Result-Code getter.
     * @return the resultCode
     */
    public String getResultCodeStr() {
        return header.resultCodeStr;
    }

    /**
     * Result-Code getter.
     * @return the resultCode
     */
    public Integer getResultCode() {
        return header.resultCode;
    }

    /**
     * Checksum getter.
     * @return the checksum
     */
    public String getChecksum() {
        return header.checksumStr;
    }

    /**
     * Location getter.
     * @return the location
     */
    public String getLocation() {
        return header.locationStr;
    }

    /**
     * Raw Offset getter.
     * @return the offset
     */
    public String getOffsetStr() {
        return header.offsetStr;
    }

    /**
     * Offset getter.
     * @return the offset
     */
    public Long getOffset() {
        return header.offset;
    }

    /**
     * FileName getter.
     * @return the fileName
     */
    public String getFileName() {
        return header.filenameStr;
    }

    /**
     * Raw Length getter.
     * @return the length
     */
    public String getArchiveLengthStr() {
        return header.archiveLengthStr;
    }

    /**
     * Length getter.
     * @return the length
     */
    public Long getArchiveLength() {
        return header.archiveLength;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(TOSTRING_BUFFER_SIZE);
        header.toStringBuilder(sb);
        return sb.toString();
    }

}
