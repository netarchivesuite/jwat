package dk.netarkivet.warclib;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jhove2.module.format.arc.ArcErrorType;
import org.jhove2.module.format.arc.ArcValidationError;
import org.jhove2.module.format.arc.ByteCountingInputStream;
import org.jhove2.module.format.arc.IPAddressParser;

public class WarcRecord {

    /** Validation errors */
    protected List<ArcValidationError> errors = null;

    /*
     * Version related fields.
     */

    boolean bMagicIdentified;
	boolean bVersionParsed;

	int major = -1;
	int minor = -1;

	/*
	 * Warc-Field related fields.
	 */

	boolean bMandatoryMissing;

	String warcTypeStr;
	Integer warcTypeIdx;

	// Warcinfo record only
	String warcFilename;

	String warcRecordIdStr;
	URI warcRecordIdUri;

	String warcDateStr;
	Date warcDate;

	String contentLengthStr;
	Long contentLength;

	String contentTypeStr;
	String contentType;

	String warcTruncatedStr;
	Integer warcTruncatedIdx;

	String warcIpAddress;
	InetAddress warcInetAddress;

	String warcConcurrentToStr;
	URI warcConcurrentToUri;

	String warcRefersToStr;
	URI warcRefersToUri;

	String warcTargetUriStr;
	URI warcTargetUriUri;

	String warcWarcinfoIdStr;
	URI warcWarcInfoIdUri;

	String warcBlockDigestStr;
	String warcBlockDigest;

	String warcPayloadDigestStr;
	String warcPayloadDigest;

	String warcIdentifiedPayloadTypeStr;
	String warcIdentifiedPayloadType;

	// revisit record only
	String warcProfileStr;
	Integer warcProfileIdx;

	String warcSegmentNumberStr;
	Integer warcSegmentNumber;

	// continuation record only
	String warcSegmentOriginIdStr;
	URI warcSegmentOriginIdUrl;

	//continuation record only
	String warcSegmentTotalLengthStr;
	Long warcSegmentTotalLength;

	public static WarcRecord parseRecord(ByteCountingInputStream in) {
		WarcRecord wr = new WarcRecord();
		try {
			if (wr.checkMagicVersion(in)) {
				//System.out.println(wr.bMagicIdentified);
				//System.out.println(wr.bVersionParsed);
				//System.out.println(wr.major + "." + wr.minor);

				wr.parseFields(in);

				if (wr.warcTypeIdx != null) {
					// TODO payload processing
				}
				if (wr.contentLength != null) {
					in.skip(wr.contentLength);
				}
			}
			else {
				wr = null;
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wr;
	}

    /**
     * Close resources associated with the WARC record. 
     * Mainly payload stream if any.
     */
	public void close() throws IOException {
	}

	protected boolean checkMagicVersion(ByteCountingInputStream in) throws IOException {
		bMagicIdentified = false;
		bVersionParsed = false;
		String tmpStr;
		boolean bSeekMagic = true;
		while (bSeekMagic) {
			tmpStr = in.readLine();
			if (tmpStr != null) {
				if (tmpStr.length() > 0) {
					if (tmpStr.toUpperCase().startsWith(WarcConstants.WARC_MAGIC_HEADER)) {
						bMagicIdentified = true;
						String versionStr = tmpStr.substring(WarcConstants.WARC_MAGIC_HEADER.length());
						String[] tmpArr = versionStr.split("\\.", -1);		// Slow?
						if (tmpArr.length >= 2 && tmpArr.length <= 4) {
							bVersionParsed = true;
							int[] versionArr = new int[tmpArr.length];
							for (int i=0; i<tmpArr.length; ++i) {
								try {
									versionArr[i] = Integer.parseInt(tmpArr[i]);
								}
								catch (NumberFormatException e) {
									versionArr[i] = -1;
								}
							}
							major = versionArr[0];
							minor = versionArr[1];
						}
						bSeekMagic = false;
					}
					else {
						// Gibberish.
					}
				}
				else {
					// Empty line.
				}
			}
			else {
				// EOF.
				bSeekMagic = false;
			}
		}
		return bMagicIdentified;
	}

	protected void parseFields(ByteCountingInputStream in) throws IOException {
		String tmpStr;
		boolean[] seen = new boolean[WarcConstants.FN_MAX_NUMBER];
		boolean bFields = true;
		while (bFields) {
			tmpStr = in.readLine();
			if (tmpStr != null) {
				while (tmpStr.endsWith("\r")) {
					tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
				}
				if ( tmpStr.length() > 0 ) {
					if (!Character.isWhitespace(tmpStr.charAt(0))) {
						int idx = tmpStr.indexOf(':');
						if (idx != -1) {
							String field = tmpStr.substring(0, idx);
							String value = tmpStr.substring(idx + 1).trim();

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
										warcConcurrentToStr = value;
										warcConcurrentToUri = parseUri(value,
												WarcConstants.FN_WARC_CONCURRENT_TO);
										break;
									case WarcConstants.FN_IDX_WARC_BLOCK_DIGEST:
										// TODO
										warcBlockDigestStr = value;
										warcBlockDigest = parseDigest(value,
												WarcConstants.FN_WARC_BLOCK_DIGEST);
										break;
									case WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST:
										// TODO
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
								}
								else {
									// Duplicate field.
						            addValidationError(ArcErrorType.INVALID, field, value);
								}
							}
							else {
								// Unrecognized field name.
							}
						}
					}
					else {
						// Leading Whitespace.
					}
				}
				else {
					// Empty line.
					bFields = false;
				}
			}
			else {
				// EOF.
				bFields = false;
			}
		}

		bMandatoryMissing = false;

		/*
		 * Unknowns.
		 */

		if (warcTypeIdx != null && warcTypeIdx == WarcConstants.RT_IDX_UNKNOWN) {
			// Warning: Unknown Warc-Type.
            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_WARC_TYPE, warcTypeStr);
		}

		if (warcProfileIdx != null && warcProfileIdx == WarcConstants.PROFILE_IDX_UNKNOWN) {
			// Warning: Unknown Warc-Profile.
            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_WARC_PROFILE, warcProfileStr);
		}

		/*
		 * Mandatory fields.
		 */

		if (warcTypeIdx == null) {
			// Mandatory valid Warc-Type missing.
            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_WARC_TYPE, warcTypeStr);
            bMandatoryMissing = true;
		}
		if (warcRecordIdUri == null) {
			// Mandatory valid Warc-Record-Id missing.
            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_WARC_RECORD_ID, warcRecordIdStr);
            bMandatoryMissing = true;
		}
		if (warcDate == null) {
			// Mandatory valid Warc-Date missing.
            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_WARC_DATE, warcDateStr);
            bMandatoryMissing = true;
		}
		if (contentLength == null) {
			// Mandatory valid Content-Length missing.
            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_CONTENT_LENGTH, contentLengthStr);
            bMandatoryMissing = true;
		}

		if (warcTypeIdx != null && warcTypeIdx == 0) {
			// Warc-Type not recognized.
            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_WARC_TYPE, warcTypeStr);
		}

		/*
		 * Content-Type should be present if Content-Length > 0
		 */

		if (contentLength != null && contentLength.longValue() > 0L &&
						(contentType == null || contentType.length() == 0)) {
            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_CONTENT_TYPE, contentType);
		}

		/*
		 * Warc record type dependent policies. 
		 */

		if (warcTypeIdx != null) {
			/*
			 * Warcinfo record should have "application/warc-fields" content-type.
			 */

			if (warcTypeIdx == WarcConstants.RT_IDX_WARCINFO) {
				if (!WarcConstants.CT_APP_WARC_FIELDS.equalsIgnoreCase(contentType)) {
					// Warning: Recommended content-type is "application/warc-fields".
				}
			}

			if (warcTypeIdx  > 0) {
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_CONTENT_TYPE, contentType, contentType);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_IP_ADDRESS, warcInetAddress, warcIpAddress);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_CONCURRENT_TO, warcConcurrentToUri, warcConcurrentToStr);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_REFERS_TO, warcRefersToUri, warcRefersToStr);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_TARGET_URI, warcTargetUriUri, warcTargetUriStr);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_WARCINFO_ID, warcWarcInfoIdUri, warcWarcinfoIdStr);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_FILENAME, warcFilename, warcFilename);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_PROFILE, warcProfileStr, warcProfileStr);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER, warcSegmentNumber, warcSegmentNumberStr);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID, warcSegmentOriginIdUrl, warcSegmentOriginIdStr);
				check_field_policy(warcTypeIdx, WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH, warcSegmentTotalLength, warcSegmentTotalLengthStr);
			}
		}

	}

	protected void check_field_policy(int rtype, int ftype, Object fieldObj, String fieldStr) {
		int policy = WarcConstants.field_policy[rtype][ftype];
		switch (policy) {
		case WarcConstants.POLICY_MANDATORY:
			if (fieldObj == null) {
	            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_IDX_STRINGS[ftype], fieldStr);
			}
            break;
		case WarcConstants.POLICY_SHALL:
			if (fieldObj == null) {
	            addValidationError(ArcErrorType.MISSING, WarcConstants.FN_IDX_STRINGS[ftype], fieldStr);
			}
            break;
		case WarcConstants.POLICY_MAY:
			break;
		case WarcConstants.POLICY_MAY_NOT:
			if (fieldObj != null) {
	            addValidationError(ArcErrorType.INVALID, WarcConstants.FN_IDX_STRINGS[ftype], fieldStr);
			}
			break;
		case WarcConstants.POLICY_SHALL_NOT:
			if (fieldObj != null) {
	            addValidationError(ArcErrorType.INVALID, WarcConstants.FN_IDX_STRINGS[ftype], fieldStr);
			}
			break;
		case WarcConstants.POLICY_IGNORE:
		default:
			break;
		}
	}

	/**
     * Checks if the ARC record has errors.
     * @return true/false based on whether the ARC record is valid or not
     */
    public boolean hasErrors() {
        return ((errors != null) && (!errors.isEmpty()));
    }

    /**
     * Validation errors getter.
     * @return validation errors list
     */
    public Collection<ArcValidationError> getValidationErrors() {
        return (hasErrors())? Collections.unmodifiableList(errors) : null;
    }

    /**
     * Add validation error.
     * @param errorType the error type {@link ArcErrorType}.
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
                // Invalid integer value.
                this.addValidationError(ArcErrorType.INVALID, field, intStr);
            }
         }
         else {
             // Missing integer value.
             addValidationError(ArcErrorType.MISSING, field, intStr);
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
            }
            catch (Exception e) {
                // Invalid long value.
                this.addValidationError(ArcErrorType.INVALID, field, longStr);
            }
         }
         else {
             // Missing long value.
             addValidationError(ArcErrorType.MISSING, field, longStr);
         }
         return lVal;
    }

    /**
     * Parses a string.
     * @param str the value to parse
     * @param field field name
     * @param optional specifies if the value is optional or not
     * @return the parsed value
     */
    protected String parseString(String str, String field) {
        if (((str == null) || (str.trim().length() == 0))) {
            this.addValidationError(ArcErrorType.MISSING, field, str);
        }
        return str;
    }

    /**
     * Parses ARC record date.
     * @param dateStr the date to parse.
     * @return the formatted date.
     */
    protected Date parseDate(String dateStr, String field) {
        Date date = null;
        if (dateStr != null && dateStr.length() > 0) {
                date = WarcDateParser.getDate(dateStr);
                if (date == null) {
                    // Invalid date.
                    addValidationError(ArcErrorType.INVALID, field, dateStr);
                }
        }
        else {
            // Missing date.
            addValidationError(ArcErrorType.MISSING, field, dateStr);
        }
        return date;
    }

    /**
     * Parses ARC record IP address.
     * @param ipAddress the IP address to parse
     * @return the IP address
     */
    protected InetAddress parseIpAddress(String ipAddress, String field) {
        InetAddress inetAddr = null;
        if (ipAddress != null && ipAddress.length() > 0) {
            inetAddr = IPAddressParser.getAddress(ipAddress);
            if (inetAddr == null) {
                // Invalid ip address.
                addValidationError(ArcErrorType.INVALID, field, ipAddress);
            }
        }
        else {
            // Missing ip address.
            addValidationError(ArcErrorType.MISSING, field, ipAddress);
        }
        return inetAddr;
    }

    /**
     * Returns an URL object holding the value of the specified string.
     * @param uriStr the URL to parse
     * @return an URL object holding the value of the specified string
     */
    protected URI parseUri(String uriStr, String field) {
        URI uri = null;
        if ((uriStr != null) && (uriStr.length() != 0)) {
        	if (uriStr.startsWith("<") && uriStr.endsWith(">")) {
        		uriStr = uriStr.substring(1, uriStr.length() - 1);
        	}
            try {
                uri = new URI(uriStr);
            }
            catch (Exception e) {
                // Invalid URI.
                addValidationError(ArcErrorType.INVALID, field, uriStr);
            }
        }
        else {
            // Missing URI.
            addValidationError(ArcErrorType.MISSING, field, uriStr);
        }
        return uri;
    }

    protected String parseContentType(String contentType, String field) {
    	return parseString(contentType, field);
    }

    protected String parseDigest(String digest, String field) {
    	return parseString(digest, field);
    }

}
