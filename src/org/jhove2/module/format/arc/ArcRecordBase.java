/**
 * JHOVE2 - Next-generation architecture for format-aware characterization
 *
 * Copyright (c) 2009 by The Regents of the University of California,
 * Ithaka Harbors, Inc., and The Board of Trustees of the Leland Stanford
 * Junior University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * o Neither the name of the University of California/California Digital
 *   Library, Ithaka Harbors/Portico, or Stanford University, nor the names of
 *   its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jhove2.module.format.arc;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract ARC record parser.
 *
 * @author lbihanic, selghissassi, nicl
 */
public abstract class ArcRecordBase {

	/** Invalid ARC file property. */
	protected static final String ARC_FILE = "ARC file";

	/** Invalid ARC record property. */
    protected static final String ARC_RECORD = "ARC record";
	
	/** ARC record version. */
	protected ArcVersion version;

	/** ARC record version block. */
	protected ArcVersionBlock versionBlock;

	/** ARC record starting offset relative to the source arc file input stream. */
	protected long startOffset = -1L;

    /** Validation errors */
    protected List<ArcValidationError> errors = null;

    /*
	 * Raw fields.
	 */

	/** ARC record string field: url. */
	public String r_url;

	/** ARC record string field: ip address. */
	public String r_ipAddress;

	/** ARC record string field: archive date. */
	public String r_archiveDate;

	/** ARC record string field: content-type. */
	public String r_contentType;

	/** ARC record string field: result code. */
	public Integer r_resultCode = null;

	/** ARC record string field: checksum. */
	public String r_checksum = "-";

	/** ARC record string field: location. */
	public String r_location = "-";

	/** ARC record string field: offset. */
	public Long r_offset = null;

	/** ARC record string field: filename. */
	public String r_filename;

	/** ARC record string field: length. */
	public Long r_length;

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

	/** Specifies whether the network has been already validated or not. */
	private boolean isNetworkDocValidated = false;

	/** Network doc parser. */
	protected ArcPayload payload;

	/**
	 * Creates an ARC record with the specified version and record description
	 * @param version ARC record version
	 * @param desc record description
	 */
	public void parseRecord(String recordLine) {
    	// TODO
    	/*
        // Parse URL Record Definition
        String[] hdr = this.parse(recordDef, fieldDesc, this.fields);
        // Extract mandatory version-independent header data.
        this.valid = (hdr.length == fieldDesc.length); 
        if(!this.valid) {
            this.addValidationError(ErrorType.INVALID, ARC_RECORD,
                    "URL record definition and record definition are not compliant");
        }
        */
		if (recordLine != null) {
			String[] records = recordLine.split(" ", -1);
			// Parse
	        r_url = FieldValidator.getArrayValue(records, 0);
	        r_ipAddress = FieldValidator.getArrayValue(records, 1);
	        r_archiveDate = FieldValidator.getArrayValue(records, 2);
	        r_contentType = FieldValidator.getArrayValue(records, 3);
	        // Validate
			url = this.parseUri(r_url);
	        inetAddress = parseIpAddress(r_ipAddress);
			archiveDate = parseDate(r_archiveDate);
			r_contentType = parseContentType(r_contentType);
            // Version 2
	        if ( version.equals(ArcVersion.VERSION_2) ) {
				r_resultCode = parseInteger(
						FieldValidator.getArrayValue(records, 4), 
						ArcConstants.RESULT_CODE_FIELD, false);
				r_checksum = parseString(
						FieldValidator.getArrayValue(records, 5),
						ArcConstants.CHECKSUM_FIELD);
				r_location = parseString(
						FieldValidator.getArrayValue(records, 6), 
						ArcConstants.LOCATION_FIELD, true);
				r_offset = this.parseLong(
						FieldValidator.getArrayValue(records, 7),
						ArcConstants.OFFSET_FIELD);
				r_filename = parseString(
						FieldValidator.getArrayValue(records, 8), 
						ArcConstants.FILENAME_FIELD);
	        }
			r_length = parseLong(
					FieldValidator.getArrayValue(records, records.length - 1),
					ArcConstants.LENGTH_FIELD);
			// Check read and computed offset value only if we're reading
			// a plain ARC file, not a GZipped ARC.
			if ((r_offset != null) && (startOffset > 0L) &&
			    (r_offset.longValue() != startOffset)) {
			    addValidationError(ArcErrorType.INVALID,
	                    ArcConstants.OFFSET_FIELD, r_offset.toString());
			}
		}
	}

	/**
     * Returns the starting offset of the record in the containing ARC.
     * @return the starting offset of the record.
     */
	public long getStartOffset() {
	    return startOffset;
	}

	/**
	 * Checks if the ARC record is valid.
	 * @return true/false based on whether the ARC record is valid or not 
	 */
	public boolean isValid() {
	    return (!hasErrors());
	}

    public boolean hasErrors() {
        return ((errors != null) && (!errors.isEmpty()));
    }

	/**
	 * Validation errors getter.
	 * @return validation errors list/
	 */
	public Collection<ArcValidationError> getValidationErrors() {
	    return (hasErrors())? Collections.unmodifiableList(errors): null;
	}

	/**
	 * Adds validation errors
	 * @param error the error type {@link ArcErrorType}.
	 * @param field the field name
	 * @param value the error value
	 */
	protected void addValidationError(ArcErrorType errorType,
                                      String field, String value) {
        if (errors == null) {
            errors = new LinkedList<ArcValidationError>();
        }
        errors.add(new ArcValidationError(errorType, field, value));
	}

	/**
	 * Checks if the ARC record has warnings.
	 * @return true/false based on whether the ARC record has warnings or not 
	 */
	public boolean hasWarnings() {
	    return false;
	}

	/**
	 * Gets Network doc warnings.
	 * @return validation errors list/
	 */
	public Collection<String> getWarnings() {
	    return null;
	}

	public void close() {
	    if (payload != null) {
	        payload.close();
	    }
	}

	/**
	 * Protocol response fields
	 */
	//private Integer protocolResultCode;
	//private String protocolVersion;
	//private String protocolContentType;

	/**
	 * Network doc setter.
	 * @throws IOException
	 */
	/*
	public void setNetworkDoc() throws IOException{
		ArcPayload networkDoc = this.processNetworkDoc();
		if(networkDoc != null){
			this.payload = networkDoc;
			this.protocolResultCode = this.parseInteger(networkDoc.resultCode,
					                                    null,true);
			this.protocolVersion = this.parseString(networkDoc.protocolVersion,
					                                null, true);
			this.protocolContentType = this.parseString(networkDoc.contentType,
					                                    null,true);
		}
		this.validateNetworkDoc();
	}
	*/

	/**
	 * Gets network doc content type.
	 * @return the content type of the network doc.
	 */
	/*
	public String getFormat(){
		return (protocolContentType != null && protocolContentType.length() > 0)
				? protocolContentType : r_contentType;
	}
	*/

	protected abstract void processPayload(ByteCountingInputStream in) throws IOException;

	/**
	 * Validates the network doc. Subclasses have to check the 
	 * coherence of the network doc.
	 */
	//public abstract void validateNetworkDoc();

	/**
	 * Validates the network doc. This method is called when processing compressed ARC.
	 * @throws IOException
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
	 * Specifies whether the ARC record has a payload or not.
	 */
	public boolean hasPayload(){
		return (payload != null); 
	}
	/**
	 * Payload getter
	 * @return the networkDoc
	 */
	public ArcPayload getPayload() {
		return payload;
	}

	/**
	 * Payload content getter
	 * @return Payload content
	 */
	public InputStream getPayloadContent() {
		return (payload != null) ?
		        new FilterInputStream(payload.in) {
					/*
		            @Override
		            public void close() throws IOException {
		                // NOP
		            }
		            */
		        }: null;
	}

	/**
	 * Parses the remaining input stream and validates that the characters 
	 * encountered are equal to LF or CR.
	 * @param in
	 * @return true/false based on whether the remaining input stream contains 
	 * only LF and CR characters or not.
	 * @throws IOException
	 */
	public boolean isValid(InputStream in) throws IOException{
		if(in == null){
			throw new IllegalArgumentException("in");
		}
		boolean isValid = true;
		int b;
		while ((b = in.read()) != -1){
			if (b != '\n' && b != '\r'){
				isValid = false;
				break;
			}
		}
		return isValid;
	}
	
	/**
	 * Gets ARC record object size.
	 * @return the object size
	 */
	/*
	public long getObjectSize() {
		return (payload != null) ? payload.objectSize : 0L;
	}
	*/

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
        	}
        	catch (Exception e) {
        		// Invalid long value.
        		this.addValidationError(ArcErrorType.INVALID, field, intStr);
        	}
         }
         return iVal;
	}

	protected Integer parseInteger(String value, String field, boolean optional) {
		Integer result = this.parseInteger(value, field);
		if((result == null) && (!optional)){
			// Missing mandatory value.
	         this.addValidationError(ArcErrorType.MISSING, field, value);
		}
		return result;
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
        	}
        	catch (Exception e) {
        		// Invalid long value.
        		this.addValidationError(ArcErrorType.INVALID, field, longStr);
        	}
         }
         else {
        	 // Missing mandatory value.
        	 this.addValidationError(ArcErrorType.MISSING, field, longStr);
         }
         return lVal;
	}

	/**
	 * Parses a string.
	 * @param str the value to parse.
	 * @param field field name.
	 * @param optional specifies if the value is optional or not.
	 * @return the parsed value.
	 */
	protected String parseString(String str, String field, boolean optional){
		if (((str == null) || (str.trim().length() == 0))
		    && (! optional)) {
			this.addValidationError(ArcErrorType.MISSING, field, str);
		}
		return str;
	}

	protected String parseString(String value,String field){
		return parseString(value,field,false);
	}
	
	/**
	 * Returns an URL object holding the value of the specified string.
	 * @param value the URL to parse.
	 * @param field field name
	 * @return an URL object holding the value of the specified string
	 */
	protected URI parseUri(String value) {
        URI uri = null;
        if ((value != null) && (value.length() != 0)) {
            try {
                uri = new URI(value);
                protocol = uri.getScheme();
            }
            catch (Exception e) {
                // Invalid URI.
                addValidationError(ArcErrorType.INVALID, ArcConstants.URL_FIELD, value);
            }
        }
        else {
            // Missing mandatory value.
            addValidationError(ArcErrorType.MISSING, ArcConstants.URL_FIELD, value);
        }
        return uri;
    }

	/**
	 * Parses ARC record IP address.
	 * @param ipAddress the IP address to parse.
	 * @return the IP address.
	 */
	protected InetAddress parseIpAddress(String ipAddress) {
	    InetAddress inetAddress = null;
	    if (ipAddress != null && ipAddress.length() > 0) {
	        inetAddress = IPAddressParser.getAddress(ipAddress);
	        if(inetAddress == null){
	            // Invalid date.
	            addValidationError(ArcErrorType.INVALID, ArcConstants.IP_ADDRESS_FIELD, ipAddress);
	        }
	    }
	    else {
	        // Missing mandatory value.
	        addValidationError(ArcErrorType.MISSING, ArcConstants.IP_ADDRESS_FIELD, ipAddress);
	    }
	    return inetAddress;
	}

	/**
	 * Parses ARC record date.
	 * @param dateStr the date to parse.
	 * @return the formatted date.
	 */
	protected Date parseDate(String dateStr) {
        Date date = null;
        if (dateStr != null && dateStr.length() > 0) {
        		date = DateParser.getDate(dateStr);
        		if(date == null){
        			// Invalid date.
        			addValidationError(ArcErrorType.INVALID, ArcConstants.DATE_FIELD, dateStr);
        		}
        }
        else {
            // Missing mandatory value.
            addValidationError(ArcErrorType.MISSING, ArcConstants.DATE_FIELD, dateStr);
        }
        return date;
    }
	
	/**
	 * Parses ARC record content type
	 * @param contentType ARC record content type.
	 * @return ARC record content type.
	 */
	protected String parseContentType(String contentType){
	    return parseString(contentType, ArcConstants.CONTENT_TYPE_FIELD);
	}

	/**
	 * version getter
	 * @return the version
	 */
	public ArcVersion getVersion() {
		return version;
	}

	/**
	 * URL getter
	 * @return the URL
	 */
	public URI getUrl() {
		return url;
	}

	/**
	 * raw URL getter
	 * @return the raw URL
	 */
	public String getRawUrl() {
		return r_url;
	}

	/**
	 * inetAddress getter
	 * @return the InetAddress
	 */
	public InetAddress getInetAddress() {
		return inetAddress;
	}

	/**
	 * ipAddress getter
	 * @return the ipAddress
	 */
	public String getRawIpAddress() {
		return r_ipAddress;
	}

	/**
	 * archiveDate getter
	 * @return the archiveDate
	 */
	public Date getArchiveDate() {
		return archiveDate;
	}

	/**
	 * rawArchiveDate getter
	 * @return the rawArchiveDate
	 */
	public String getRawArchiveDate() {
		return r_archiveDate;
	}

	/**
	 * contentType getter
	 * @return the contentType
	 */
	public String getContentType() {
		return r_contentType;
	}

	/**
	 * resultCode getter
	 * @return the resultCode
	 */
	public Integer getResultCode() {
		return r_resultCode;
	}
	
	/**
	 * checksum getter
	 * @return the checksum
	 */
	public String getChecksum() {
		return r_checksum;
	}

	/**
	 * location getter
	 * @return the location
	 */
	public String getLocation() {
		return r_location;
	}

	/**
	 * offset getter
	 * @return the offset
	 */
	public Long getOffset() {
		return r_offset;
	}


	/**
	 * fileName getter
	 * @return the fileName
	 */
	public String getFileName() {
		return r_filename;
	}

	/**
	 * length getter
	 * @return the length
	 */
	public Long getLength() {
		return r_length;
	}

	/**
	 * protocol getter
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * isNetworkDocValidated getter
	 * @return the isNetworkDocValidated
	 */
	public boolean isNetworkDocValidated() {
		return isNetworkDocValidated;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(256);
		if (r_url != null) {
		    builder.append("url:" ).append(r_url);
		}
		if (r_ipAddress != null) {
		    builder.append(", ipAddress: ").append(r_ipAddress);
		}
		if (r_archiveDate != null) {
		    builder.append(", archiveDate: ").append(r_archiveDate);
		}
		if (r_contentType != null) {
		    builder.append(", contentType: ").append(r_contentType);
		}
		if (r_resultCode != null) {
		    builder.append(", resultCode: ").append(r_resultCode);
		}
		if (r_checksum != null) {
		    builder.append(", checksum: ").append(r_checksum);
		}
		if (r_location != null) {
		    builder.append(", location: ").append(r_location);
		}
		if (r_offset != null) {
		    builder.append(", offset: ").append(r_offset);
		}
		if (r_filename != null) {
		    builder.append(", fileName: ")
		    	.append(r_filename);
			if (r_length != null) {
			    builder.append(", length: ").append(r_length);
			}
		}
		return builder.toString();
	}
}
