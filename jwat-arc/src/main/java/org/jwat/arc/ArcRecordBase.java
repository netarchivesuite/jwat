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
import java.security.MessageDigest;
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
import org.jwat.common.HttpResponse;
import org.jwat.common.Payload;
import org.jwat.common.PayloadOnClosedHandler;

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

    /** Buffer size used in toString(). */
    public static final int TOSTRING_BUFFER_SIZE = 256;

    /** Invalid ARC file property. */
    protected static final String ARC_FILE = "ARC file";

    /** Invalid ARC record property. */
    protected static final String ARC_RECORD = "ARC record";

    /** Is this record compliant ie. error free. */
    protected boolean bIsCompliant;

    /** Reader instance used, required for file compliance. */
    protected ArcReader reader;

    /** Input stream used to read this record. */
    protected ByteCountingPushBackInputStream in;

    /** ARC record version. */
    protected ArcVersion version;

    /** ARC record version block. */
    protected ArcVersionBlock versionBlock;

    /** ARC record starting offset relative to the source arc file input
     *  stream. */
    protected long startOffset = -1L;

    /** Bytes consumed while validating this record. */
    protected long consumed;

    /** Validation errors. */
    //protected List<ArcValidationError> errors = null;

    /** Validation errors and warnings. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /** Do the record fields comply in number with the one dictated by its
     *  version. */
    protected boolean hasCompliantFields = false;

    /*
     * Raw fields.
     */

    /** ARC record string field: url. */
    public String recUrl;

    /** ARC record string field: ip address. */
    public String recIpAddress;

    /** ARC record string field: archive date. */
    public String recArchiveDate;

    /** ARC record string field: content-type. */
    public String recContentType;

    /** ARC record string field: result code. */
    public Integer recResultCode = null;

    /** ARC record string field: checksum. */
    public String recChecksum = "-";

    /** ARC record string field: location. */
    public String recLocation = "-";

    /** ARC record string field: offset. */
    public Long recOffset = null;

    /** ARC record string field: filename. */
    public String recFilename;

    /** ARC record string field: length. */
    public Long recLength;

    /*
     * Parsed fields.
     */

    /** String Url parsed and validated into an <code>URI</code> object. */
    public URI url;

    /** Url Scheme. (filedesc, http, https, dns, etc.) */
    public String protocol;

    /** IpAddress parsed and validated to a <code>InetAddress</code> object. */
    public InetAddress inetAddress;

    /** String to <code>Date</code> conversion from "YYYYMMDDhhmmss" format. */
    public Date archiveDate;

    /** Content-Type wrapper object with optional parameters. */
    public ContentType contentType;

    /** Specifies whether the network has been already validated or not. */
    //private boolean isNetworkDocValidated = false;

    /*
     * Payload
     */

    /** Has payload been closed before. */
    protected boolean bPayloadClosed;

    /** Has record been closed flag. */
    protected boolean bClosed;

    /** Payload object if any exists. */
    protected Payload payload;

    /** HttpResponse header content parsed from payload. */
    protected HttpResponse httpResponse;

    /** Computed block digest. */
    public Digest computedBlockDigest;

    /** Computed payload digest. */
    public Digest computedPayloadDigest;

    /**
     * Creates an ARC record from the specified record description.
     * @param recordLine ARC record string
     */
    public void parseRecord(String recordLine) {
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
            // Parse
            recUrl = ArcFieldValidator.getArrayValue(records,
                    ArcConstants.AF_IDX_URL);
            recIpAddress = ArcFieldValidator.getArrayValue(records,
                    ArcConstants.AF_IDX_IPADDRESS);
            recArchiveDate = ArcFieldValidator.getArrayValue(records,
                    ArcConstants.AF_IDX_ARCHIVEDATE);
            recContentType = ArcFieldValidator.getArrayValue(records,
                    ArcConstants.AF_IDX_CONTENTTYPE);
            // Validate
            url = reader.fieldParser.parseUri(recUrl, ArcConstants.URL_FIELD);
            if (url != null) {
                protocol = url.getScheme();
            }
            inetAddress = reader.fieldParser.parseIpAddress(recIpAddress, ArcConstants.IP_ADDRESS_FIELD);
            archiveDate = reader.fieldParser.parseDate(recArchiveDate, ArcConstants.DATE_FIELD);
            contentType = reader.fieldParser.parseContentType(recContentType, ArcConstants.CONTENT_TYPE_FIELD);
            // Version 2
            if (version.equals(ArcVersion.VERSION_2)) {
                recResultCode = reader.fieldParser.parseInteger(
                        ArcFieldValidator.getArrayValue(records,
                                ArcConstants.AF_IDX_RESULTCODE),
                        ArcConstants.RESULT_CODE_FIELD, false);
                recChecksum = reader.fieldParser.parseString(
                        ArcFieldValidator.getArrayValue(records,
                                ArcConstants.AF_IDX_CHECKSUM),
                        ArcConstants.CHECKSUM_FIELD);
                recLocation = reader.fieldParser.parseString(
                        ArcFieldValidator.getArrayValue(records,
                                ArcConstants.AF_IDX_LOCATION),
                        ArcConstants.LOCATION_FIELD, true);
                recOffset = reader.fieldParser.parseLong(
                        ArcFieldValidator.getArrayValue(records,
                                ArcConstants.AF_IDX_OFFSET),
                        ArcConstants.OFFSET_FIELD);
                recFilename = reader.fieldParser.parseString(
                        ArcFieldValidator.getArrayValue(records,
                                ArcConstants.AF_IDX_FILENAME),
                        ArcConstants.FILENAME_FIELD);
            }
            recLength = reader.fieldParser.parseLong(
                    ArcFieldValidator.getArrayValue(records,
                            records.length - 1), ArcConstants.LENGTH_FIELD);
            // Check read and computed offset value only if we're reading
            // a plain ARC file, not a GZipped ARC.
            if ((recOffset != null) && (startOffset > 0L)
                                && (recOffset.longValue() != startOffset)) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA,
                        "'" + ArcConstants.OFFSET_FIELD + "' value",
                        recOffset.toString()));
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
     * Checks if the ARC record is valid.
     * @return true/false based on whether the ARC record is valid or not
     */
    /*
    public boolean isValid() {
        return (hasCompliantFields && !hasErrors());
    }
    */

    /**
     * Checks if the ARC record has errors.
     * @return true/false based on whether the ARC record is valid or not
     */
    /*
    public boolean hasErrors() {
        return ((errors != null) && (!errors.isEmpty()));
    }
    */

    /**
     * Validation errors getter.
     * @return validation errors list
     */
    /*
    public Collection<ArcValidationError> getValidationErrors() {
        return (hasErrors())? Collections.unmodifiableList(errors) : null;
    }
    */

    /**
     * Add validation error.
     * @param errorType the error type {@link ArcErrorType}.
     * @param field the field name
     * @param value the error value
     */
    /*
    protected void addValidationError(ArcErrorType errorType,
                                      String field, String value) {
        if (errors == null) {
            errors = new LinkedList<ArcValidationError>();
        }
        errors.add(new ArcValidationError(errorType, field, value));
    }
    */

    /**
     * Checks if the ARC record has warnings.
     * @return true/false based on whether the ARC record has warnings or not
     */
    /*
    public boolean hasWarnings() {
        return false;
    }
    */

    /**
     * Gets Network doc warnings.
     * @return validation errors list/
     */
    /*
    public Collection<String> getWarnings() {
        return null;
    }
    */

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
                if (md != null) {
                    computedBlockDigest = new Digest();
                    computedBlockDigest.digestBytes = md.digest();
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
                if (httpResponse != null) {
                    /*
                     * Check payload digest.
                     */
                    md = httpResponse.getMessageDigest();
                    if (md != null) {
                        computedPayloadDigest = new Digest();
                        computedPayloadDigest.digestBytes = md.digest();
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
     * Process the ARC record stream for possible payload data.
     * @param in ARC record <code>InputStream</code>
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @throws IOException io exception in the parsing process
     */
    protected abstract void processPayload(ByteCountingPushBackInputStream in,
                                        ArcReader reader) throws IOException;

    /**
     * isNetworkDocValidated getter.
     * @return the isNetworkDocValidated
     */
    /*
    public boolean isNetworkDocValidated() {
        return isNetworkDocValidated;
    }
    */

    /**
     * Validates the network doc. Subclasses have to check the
     * coherence of the network doc.
     */
    //public abstract void validateNetworkDoc();

    /**
     * Validates the network doc. This method is called when processing
     * compressed ARC.
     * @throws IOException io exception in parsing
     */
    /*
    public final void validateNetworkDocContent(InputStream in)
                                                    throws IOException {
        if(payload != null && !isNetworkDocValidated){
            boolean isValid = this.isValid(in);
            isNetworkDocValidated = true;
            if(!isValid){
                this.addValidationError(ArcErrorType.INVALID, ARC_RECORD,
                    "Non LF characters encountered after network doc");
            }
        }
    }
    */

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

    /**
     * Version getter.
     * @return the version
     */
    public ArcVersion getVersion() {
        return version;
    }

    /**
     * URL getter.
     * @return the URL
     */
    public URI getUrl() {
        return url;
    }

    /**
     * Raw URL getter.
     * @return the raw URL
     */
    public String getRawUrl() {
        return recUrl;
    }

    /**
     * <code>inetAddress</code> getter.
     * @return the InetAddress
     */
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    /**
     * IpAddress getter.
     * @return the ipAddress
     */
    public String getRawIpAddress() {
        return recIpAddress;
    }

    /**
     * ArchiveDate getter.
     * @return the archiveDate
     */
    public Date getArchiveDate() {
        return archiveDate;
    }

    /**
     * Raw ArchiveDate getter.
     * @return the rawArchiveDate
     */
    public String getRawArchiveDate() {
        return recArchiveDate;
    }

    /**
     * Content-Type getter.
     * @return the contentType
     */
    public String getContentType() {
        return recContentType;
    }

    /**
     * Result-Code getter.
     * @return the resultCode
     */
    public Integer getResultCode() {
        return recResultCode;
    }

    /**
     * Checksum getter.
     * @return the checksum
     */
    public String getChecksum() {
        return recChecksum;
    }

    /**
     * Location getter.
     * @return the location
     */
    public String getLocation() {
        return recLocation;
    }

    /**
     * Offset getter.
     * @return the offset
     */
    public Long getOffset() {
        return recOffset;
    }

    /**
     * Return number of bytes consumed validating this record.
     * @return number of bytes consumed validating this record
     */
    public Long getConsumed() {
        return consumed;
    }

    /**
     * FileName getter.
     * @return the fileName
     */
    public String getFileName() {
        return recFilename;
    }

    /**
     * Length getter.
     * @return the length
     */
    public Long getLength() {
        return recLength;
    }

    /**
     * Protocol getter.
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(TOSTRING_BUFFER_SIZE);
        if (recUrl != null) {
            builder.append("url:").append(recUrl);
        }
        if (recIpAddress != null) {
            builder.append(", ipAddress: ").append(recIpAddress);
        }
        if (recArchiveDate != null) {
            builder.append(", archiveDate: ").append(recArchiveDate);
        }
        if (recContentType != null) {
            builder.append(", contentType: ").append(recContentType);
        }
        if (recResultCode != null) {
            builder.append(", resultCode: ").append(recResultCode);
        }
        if (recChecksum != null) {
            builder.append(", checksum: ").append(recChecksum);
        }
        if (recLocation != null) {
            builder.append(", location: ").append(recLocation);
        }
        if (recOffset != null) {
            builder.append(", offset: ").append(recOffset);
        }
        if (recFilename != null) {
            builder.append(", fileName: ")
                .append(recFilename);
            if (recLength != null) {
                builder.append(", length: ").append(recLength);
            }
        }
        return builder.toString();
    }

}
