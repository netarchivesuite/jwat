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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
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

/**
 * This abstract class represents the common base ARC data which is present in
 * both a record or version block. This includes possible common
 * validation and format warnings/errors encountered in the process.
 * This class also contains the common parts of the ARC parser which are
 * intended to be called by extending classes.
 *
 * @author lbihanic, selghissassi, nicl
 */
public abstract class ArcRecordBase implements PayloadOnClosedHandler {

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

    /** ARC record version block. */
    //protected ArcVersionBlock versionBlock;

    /** ARC record parsing start offset relative to the source arc file input
     *  stream. Used to keep track of the amount of bytes consumed. */
    protected long startOffset;

    /** Uncompressed bytes consumed while validating this record. */
    protected long consumed;

    /** Validation errors and warnings. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /** Newline parser for counting/validating trailing newlines. */
    public NewlineParser nlp = new NewlineParser();

    /** Record type, version block or arc record. */
    public int recordType;

    /*
     * Header-Fields.
     */

    /** WARC header. */
    public ArcHeader header;

    /*
     * Payload
     */

    /** Has payload been closed before. */
    protected boolean bPayloadClosed;

    /** Has record been closed flag. */
    protected boolean bClosed;

    /** Payload object if any exists. */
    protected Payload payload;

    protected ArcVersionHeader versionHeader;

    /** HTTP header content parsed from payload. */
    protected HttpHeader httpHeader;

    /** Computed block digest. */
    public Digest computedBlockDigest;

    /** Computed payload digest. */
    public Digest computedPayloadDigest;

    /**
     * Creates an ARC record from the specified record description.
     * @param recordLine ARC record string
     */
    public static ArcRecordBase parseRecord(ByteCountingPushBackInputStream in, ArcReader reader) throws IOException {
        // debug
        //System.out.println(in.getConsumed());
        ArcRecordBase record = null;
        long startOffset = in.getConsumed();
        // Initialize ArcHeader with required context.
        Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();
        ArcHeader header = ArcHeader.initHeader(reader, startOffset, diagnostics);
        // Initialize ArcFieldParser to report diagnoses here.
        reader.fieldParsers.diagnostics = diagnostics;
        if (header.parseHeader(in)) {
            if (header.urlScheme != null && header.urlScheme.startsWith(ArcConstants.ARC_SCHEME)) {
                record = ArcVersionBlock.parseVersionBlock(reader, header, reader.fieldParsers, in);
                reader.versionHeader = record.versionHeader;
                if (record.versionHeader != null) {
                    record.version = record.versionHeader.version;
                }
            }
            if (record == null) {
                record = ArcRecord.parseArcRecord(reader, header, in);
            }
        }
        if (record != null) {
            record.startOffset = startOffset;
            // Check read and computed offset value only if we're reading
            // a plain ARC file, not a GZipped ARC.
            if ((header.offset != null) && (header.startOffset > 0L)
                                && (header.offset.longValue() != header.startOffset)) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA,
                        "'" + ArcConstants.FN_OFFSET + "' value",
                        header.offset.toString()));
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
            reader.consumed += in.getConsumed() - startOffset;
            reader.diagnostics.addAll(diagnostics);
            if (diagnostics.hasErrors() || diagnostics.hasWarnings()) {
                reader.bIsCompliant = false;
                reader.errors += diagnostics.getErrors().size();
                reader.warnings += diagnostics.getWarnings().size();
            }
        }
        return record;
        // Compare to expected numbers of fields.
        // Extract mandatory version-independent header data.
        // TODO
        /*
        hasCompliantFields = (records.length
                          == versionBlock.descValidator.fieldNames.length);
        if(!hasCompliantFields) {
            diagnostics.addError(new Diagnosis(DiagnosisType.INVALID,
                    ARC_RECORD,
                    "URL record definition and record definition are not "
                            + "compliant"));
        }
        */
        /*
        hasCompliantFields = false;
        if (recordLine != null) {
            String[] records = recordLine.split(" ", -1);
            // Compare to expected numbers of fields.
            // Extract mandatory version-independent header data.
            hasCompliantFields = (records.length
                              == versionBlock.descValidator.fieldNames.length);
            if(!hasCompliantFields) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID,
                        ARC_RECORD,
                        "URL record definition and record definition are not "
                                + "compliant"));
            }
        }
        */
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
            int newlines = nlp.parseLFs(in, diagnostics);
            if (newlines != ArcConstants.ARC_RECORD_TRAILING_NEWLINES) {
                addErrorDiagnosis(DiagnosisType.INVALID_EXPECTED,
                        "Trailing newlines",
                        Integer.toString(newlines),
                        "1");
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
            // debug
            //System.out.println(in.getConsumed());
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
     * @throws IOException io exception close the payload resources
     */
    public void close() throws IOException {
        if (!bClosed) {
            // Ensure input stream is at the end of the record payload.
            if (payload != null) {
                payload.close();
            }
            payloadClosed();
            payload = null;
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
     * Process the ARC record stream for possible payload data.
     * @param in ARC record <code>InputStream</code>
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @throws IOException io exception in the parsing process
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
     * Parses the remaining input stream and validates that the characters
     * encountered are equal to LF or CR.
     * @param in <code>InputStream</code> to validate
     * @return true/false based on whether the remaining input stream contains
     * only LF and CR characters or not.
     * @throws IOException io exception in parsing
     */
    /*
    public boolean isValid(InputStream in) throws IOException{
        if (in == null) {
            throw new IllegalArgumentException("in");
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
    public URI getUrl() {
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
