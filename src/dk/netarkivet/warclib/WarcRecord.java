package dk.netarkivet.warclib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jhove2.module.format.arc.IPAddressParser;

public class WarcRecord {

    /** Validation errors */
    protected List<WarcValidationError> errors = null;

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

	List<String> warcConcurrentToStrList;
	List<URI> warcConcurrentToUriList;

	String warcRefersToStr;
	URI warcRefersToUri;

	String warcTargetUriStr;
	URI warcTargetUriUri;

	String warcWarcinfoIdStr;
	URI warcWarcInfoIdUri;

	String warcBlockDigestStr;
	WarcDigest warcBlockDigest;

	String warcPayloadDigestStr;
	WarcDigest warcPayloadDigest;

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

	public static WarcRecord parseRecord(WarcInputStream in) {
		WarcRecord wr = new WarcRecord();
		try {
			if (wr.parseVersion(in)) {
				//System.out.println(wr.bMagicIdentified);
				//System.out.println(wr.bVersionParsed);
				//System.out.println(wr.major + "." + wr.minor);

				wr.parseFields(in);
				wr.checkFields();

				if (wr.warcTypeIdx != null) {
					// TODO payload processing
				}
				if (wr.contentLength != null) {
					long skipped = in.skip(wr.contentLength);
					if (skipped != wr.contentLength) {
	                    wr.addValidationError(WarcErrorType.INVALID, "Payload Length", wr.contentLength.toString());
					}
				}
				int newlines = wr.parseNewLines(in);
				if (newlines != 2) {
                    wr.addValidationError(WarcErrorType.INVALID, "Traling newlines", Integer.toString(newlines));
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

	protected int parseNewLines(WarcInputStream in) throws IOException {
		int newlines = 0;
		byte[] buffer = new byte[2];
		boolean b = true;
		while (b) {
			int read = in.read(buffer);
			switch (read) {
			case 1:
				if (buffer[0] == '\n') {
					++newlines;
				}
				else {
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
				}
				else {
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

	protected boolean parseVersion(WarcInputStream in) throws IOException {
		bMagicIdentified = false;
		bVersionParsed = false;
		String tmpStr;
		boolean bSeekMagic = true;
		while (bSeekMagic) {
			tmpStr = readLine(in);
			if (tmpStr != null) {
				System.out.println(tmpStr);
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
	                    addValidationError(WarcErrorType.INVALID, "Data", null);
					}
				}
				else {
					// Empty line.
                    addValidationError(WarcErrorType.INVALID, "Empty lines", null);
				}
			}
			else {
				// EOF.
				bSeekMagic = false;
			}
		}
		return bMagicIdentified;
	}

	protected void parseFields(WarcInputStream in) throws IOException {
		WarcHeader warcHeader;
		boolean[] seen = new boolean[WarcConstants.FN_MAX_NUMBER];
		boolean bFields = true;
		while (bFields) {
			warcHeader = readHeaderLine(in);
			if (warcHeader != null) {
				if (warcHeader.line == null) {
					if (warcHeader.name != null && warcHeader.name.length() > 0) {
						System.out.println(warcHeader.name);
						System.out.println(warcHeader.value);

						parseField(warcHeader.name, warcHeader.value, seen);
					}
					else {
						// Empty field name.
					}
				}
				else {
					if (warcHeader.line.length() == 0) {
						// Empty line.
						bFields = false;
					}
					else {
						// Unknown header line.
					}
				}
			}
			else {
				// EOF.
				bFields = false;
			}
		}
	}

	protected void parseField(String field, String value, boolean[] seen) {
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
	            addValidationError(WarcErrorType.DUPLICATE, field, value);
			}
		}
		else {
			// Unrecognized field name.
		}
	}

	protected void checkFields() {
		bMandatoryMissing = false;

		/*
		 * Unknown Warc-Type and/or Warc-Profile.
		 */

		if (warcTypeIdx != null && warcTypeIdx == WarcConstants.RT_IDX_UNKNOWN) {
			// Warning: Unknown Warc-Type.
            addValidationError(WarcErrorType.UNKNOWN, WarcConstants.FN_WARC_TYPE, warcTypeStr);
		}

		if (warcProfileIdx != null && warcProfileIdx == WarcConstants.PROFILE_IDX_UNKNOWN) {
			// Warning: Unknown Warc-Profile.
            addValidationError(WarcErrorType.UNKNOWN, WarcConstants.FN_WARC_PROFILE, warcProfileStr);
		}

		/*
		 * Mandatory fields.
		 */

		if (warcTypeIdx == null) {
			// Mandatory valid Warc-Type missing.
            addValidationError(WarcErrorType.WANTED, WarcConstants.FN_WARC_TYPE, warcTypeStr);
            bMandatoryMissing = true;
		}
		if (warcRecordIdUri == null) {
			// Mandatory valid Warc-Record-Id missing.
            addValidationError(WarcErrorType.WANTED, WarcConstants.FN_WARC_RECORD_ID, warcRecordIdStr);
            bMandatoryMissing = true;
		}
		if (warcDate == null) {
			// Mandatory valid Warc-Date missing.
            addValidationError(WarcErrorType.WANTED, WarcConstants.FN_WARC_DATE, warcDateStr);
            bMandatoryMissing = true;
		}
		if (contentLength == null) {
			// Mandatory valid Content-Length missing.
            addValidationError(WarcErrorType.WANTED, WarcConstants.FN_CONTENT_LENGTH, contentLengthStr);
            bMandatoryMissing = true;
		}

		/*
		 * Content-Type should be present if Content-Length > 0.
		 * Exception for continuation records.
		 */

		if (contentLength != null && contentLength.longValue() > 0L &&
						(contentType == null || contentType.length() == 0)) {
			if (warcTypeIdx == null || warcTypeIdx != WarcConstants.RT_IDX_CONTINUATION) {
	            addValidationError(WarcErrorType.RECOMMENDED, WarcConstants.FN_CONTENT_TYPE, contentType);
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
				if (contentType != null && !WarcConstants.CT_APP_WARC_FIELDS.equalsIgnoreCase(contentType)) {
		            addValidationError(WarcErrorType.RECOMMENDED, WarcConstants.FN_CONTENT_TYPE, "application/warc-fields");
				}
			}

			if (warcTypeIdx == WarcConstants.RT_IDX_RESPONSE) {
				if (warcSegmentNumber != null && warcSegmentNumber != 1) {
		            addValidationError(WarcErrorType.INVALID, WarcConstants.FN_WARC_SEGMENT_NUMBER, warcSegmentNumber.toString());
				}
			}

			if (warcTypeIdx == WarcConstants.RT_IDX_CONTINUATION) {
				if (warcSegmentNumber != null && warcSegmentNumber < 2) {
		            addValidationError(WarcErrorType.INVALID, WarcConstants.FN_WARC_SEGMENT_NUMBER, warcSegmentNumber.toString());
				}
			}

			/*
			 * Check
			 */

			if (warcTypeIdx  > 0) {
				checkFieldPolicy(warcTypeIdx, WarcConstants.FN_IDX_CONTENT_TYPE, contentType, contentType);
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

	protected void checkFieldPolicy(int rtype, int ftype, Object fieldObj, String valueStr) {
		int policy = WarcConstants.field_policy[rtype][ftype];
		switch (policy) {
		case WarcConstants.POLICY_MANDATORY:
			if (fieldObj == null) {
	            addValidationError(WarcErrorType.WANTED,
	            			WarcConstants.FN_IDX_STRINGS[ftype], valueStr);
			}
            break;
		case WarcConstants.POLICY_SHALL:
			if (fieldObj == null) {
	            addValidationError(WarcErrorType.WANTED,
	            			WarcConstants.FN_IDX_STRINGS[ftype], valueStr);
			}
            break;
		case WarcConstants.POLICY_MAY:
			break;
		case WarcConstants.POLICY_MAY_NOT:
			if (fieldObj != null) {
	            addValidationError(WarcErrorType.UNWANTED,
	            			WarcConstants.FN_IDX_STRINGS[ftype], valueStr);
			}
			break;
		case WarcConstants.POLICY_SHALL_NOT:
			if (fieldObj != null) {
	            addValidationError(WarcErrorType.UNWANTED,
	            			WarcConstants.FN_IDX_STRINGS[ftype], valueStr);
			}
			break;
		case WarcConstants.POLICY_IGNORE:
		default:
			break;
		}
	}

	protected void checkFieldPolicy(int rtype, int ftype, List<?> fieldObj, List<String> valueList) {
		String valueStr = null;
		int policy = WarcConstants.field_policy[rtype][ftype];
		switch (policy) {
		case WarcConstants.POLICY_MANDATORY:
			if (fieldObj == null) {
				valueStr = listToStr(valueList);
	            addValidationError(WarcErrorType.WANTED,
	            			WarcConstants.FN_IDX_STRINGS[ftype], valueStr);
			}
            break;
		case WarcConstants.POLICY_SHALL:
			if (fieldObj == null) {
				valueStr = listToStr(valueList);
	            addValidationError(WarcErrorType.WANTED,
	            			WarcConstants.FN_IDX_STRINGS[ftype], valueStr);
			}
            break;
		case WarcConstants.POLICY_MAY:
			break;
		case WarcConstants.POLICY_MAY_NOT:
			if (fieldObj != null) {
				valueStr = listToStr(valueList);
	            addValidationError(WarcErrorType.UNWANTED,
	            			WarcConstants.FN_IDX_STRINGS[ftype], valueStr);
			}
			break;
		case WarcConstants.POLICY_SHALL_NOT:
			if (fieldObj != null) {
				valueStr = listToStr(valueList);
	            addValidationError(WarcErrorType.UNWANTED,
	            			WarcConstants.FN_IDX_STRINGS[ftype], valueStr);
			}
			break;
		case WarcConstants.POLICY_IGNORE:
		default:
			break;
		}
	}

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
    public Collection<WarcValidationError> getValidationErrors() {
        return (hasErrors())? Collections.unmodifiableList(errors) : null;
    }

    /**
     * Add validation error.
     * @param errorType the error type {@link WarcErrorType}.
     * @param field the field name
     * @param value the error value
     */
    protected void addValidationError(WarcErrorType errorType,
                                      String field, String value) {
        if (errors == null) {
            errors = new LinkedList<WarcValidationError>();
        }
        errors.add(new WarcValidationError(errorType, field, value));
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
                this.addValidationError(WarcErrorType.INVALID, field, intStr);
            }
         }
         else {
             // Missing integer value.
             addValidationError(WarcErrorType.EMPTY, field, intStr);
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
                this.addValidationError(WarcErrorType.INVALID, field, longStr);
            }
         }
         else {
             // Missing long value.
             addValidationError(WarcErrorType.EMPTY, field, longStr);
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
            this.addValidationError(WarcErrorType.EMPTY, field, str);
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
                    addValidationError(WarcErrorType.INVALID, field, dateStr);
                }
        }
        else {
            // Missing date.
            addValidationError(WarcErrorType.EMPTY, field, dateStr);
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
                addValidationError(WarcErrorType.INVALID, field, ipAddress);
            }
        }
        else {
            // Missing ip address.
            addValidationError(WarcErrorType.EMPTY, field, ipAddress);
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
                addValidationError(WarcErrorType.INVALID, field, uriStr);
            }
        }
        else {
            // Missing URI.
            addValidationError(WarcErrorType.EMPTY, field, uriStr);
        }
        return uri;
    }

    protected String parseContentType(String contentType, String field) {
    	return parseString(contentType, field);
    }

    protected WarcDigest parseDigest(String labelledDigest, String field) {
        WarcDigest digest = null;
        if (labelledDigest != null && labelledDigest.length() > 0) {
                digest = WarcDigest.parseDigest(labelledDigest);
                if (digest == null) {
                    // Invalid digest.
                    addValidationError(WarcErrorType.INVALID, field, labelledDigest);
                }
        }
        else {
            // Missing date.
            addValidationError(WarcErrorType.EMPTY, field, labelledDigest);
        }
        return digest;
    }

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

    private static final int S_START = 0;
    private static final int S_LINE = 1;
    private static final int S_NAME = 2;
    private static final int S_VALUE = 3;
    private static final int S_LWS = 4;
    private static final int S_QUOTED_TEXT = 5;
    private static final int S_QUOTED_PAIR = 6;
    private static final int S_QUOTED_LWS = 7;

    protected static boolean[] separator = new boolean[256];

    static {
        String separators = "()<>@,;:\\\"/[]?={} \t";
        for (int i=0; i<separators.length(); ++i) {
        	separator[separators.charAt(i)] = true;
        }
    }

    protected WarcHeader readHeaderLine(PushbackInputStream in) throws IOException {
    	WarcHeader warcHeader = null;
    	ByteArrayOutputStream bos = new ByteArrayOutputStream(32);
    	StringBuilder sb = new StringBuilder(128);
    	int state = S_START;
    	int c;
    	int utf8_c;
    	byte utf8_read;
    	byte utf8_octets;
    	boolean bCr = false;
    	boolean bLoop = true;
    	boolean bOctets;
    	while (bLoop) {
    		c = in.read();
    		if (c == -1) {
    			// EOF.
    			return null;
    		}
    		switch (state) {
    		case S_START:
    			switch (c) {
    			case '\r':
    				bCr = true;
    				break;
    			case '\n':
    				warcHeader = new WarcHeader();
    				warcHeader.line = bos.toString();
    				if (!bCr) {
    					// Missing CR.
    					bCr = false;
    				}
    				bLoop = false;
    				break;
    			default:
    				if (!bCr) {
    					// Misplaced CR.
    					bCr = false;
    				}
    				if (!Character.isWhitespace(c)) {
    					in.unread(c);
    					state = S_NAME;
    				} else {
    					bos.write(c);
    					state = S_LINE;
    				}
    				break;
    			}
    			break;
    		case S_LINE:
    			switch (c) {
    			case '\r':
    				bCr = true;
    				break;
    			case '\n':
    				warcHeader = new WarcHeader();
    				warcHeader.line = bos.toString();
    				if (!bCr) {
    					// Missing CR.
    					bCr = false;
    				}
    				bLoop = false;
    				break;
    			default:
    				if (!bCr) {
    					// Misplaced CR.
    					bCr = false;
    				}
   					bos.write(c);
    				break;
    			}
    			break;
    		case S_NAME:
    			switch (c) {
    			case '\r':
    				bCr = true;
    				break;
    			case '\n':
    				warcHeader = new WarcHeader();
    				warcHeader.line = bos.toString();
    				if (!bCr) {
    					// Missing CR.
        				bCr = false;
    				}
    				bLoop = false;
    				break;
    			case ':':
    				warcHeader = new WarcHeader();
    				warcHeader.name = bos.toString("US-ASCII");
    				if (bCr) {
    					// Misplaced CR.
        				bCr = false;
    				}
    				state = S_VALUE;
    				break;
    			default:
    				if (bCr) {
    					// Misplaced CR.
        				bCr = false;
    				}
    				if (c < 32 && c>126) {
    					// Controls.
    				} else {
    					if (!separator[c]) {
        	   				bos.write(c);
    					} else {
    						// Separator.
    					}
    				}
    				break;
    			}
    			break;
    		case S_VALUE:
    			switch (c) {
    			case '\r':
    				bCr = true;
    				break;
    			case '\n':
    				if (!bCr) {
    					// Missing CR.
        				bCr = false;
    				}
    				state = S_LWS;
    				break;
   				default:
    				if (bCr) {
    					// Misplaced CR.
        				bCr = false;
    				}
    				if ((c & 0x80) == 0x00) {
    					// US-ASCII/UTF-8: 0000 0000-0000 007F | 0xxxxxxx
        				if (c < 32 && c>126) {
        					// Controls.
        				} else {
        					switch (c) {
        					case '\"':
        						state = S_QUOTED_TEXT;
        						break;
        					case '=':
        						break;
        					default:
                				sb.append((char)c);
                				break;
        					}
        				}
    				} else {
    					// UTF-8
    					utf8_read = 1;
    					bOctets = true;
    					if ((c & 0xE0) == 0xC0) {
        					// UTF-8: 0000 0080-0000 07FF | 110xxxxx 10xxxxxx
        					utf8_c = c & 0x1F;
        					utf8_octets = 2;
        				} else if ((c & 0xF0) == 0xE0) {
        					// UTF-8: 0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
        					utf8_c = c & 0x0F;
        					utf8_octets = 3;
        				} else if ((c & 0xF8) == 0xF0) {
        					// UTF-8: 0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        					utf8_c = c & 0x07;
        					utf8_octets = 4;
        				} else {
        					// Invalid UTF-8 octet.
        					utf8_c = 0;
        					utf8_read = 0;
        					utf8_octets = 0;
        					bOctets = false;
        				}
    					// Read the remaning octets.
    					while (bOctets) {
    						if (utf8_read < utf8_octets) {
        						c = in.read();
        						if (c == -1) {
        			    			// EOF.
        							bOctets = false;
        							bLoop = false;
        						} else if ((c & 0xC0) == 0x80) {
        							utf8_c = (utf8_c << 6) | (c & 0x3F);
            						++utf8_read;
        						} else {
                					// Invalid UTF-8 octet.
        							bOctets = false;
        						}
    						} else {
    							bOctets = false;
    						}
    					}
    					// Correctly encoded.
    					if (utf8_read == utf8_octets) {
        					switch (utf8_octets) {
        					case 2:
            					if (utf8_c < 0x00000080 || utf8_c > 0x000007FF) {
            						// Incorrectly encoded value.
            					}
        						break;
        					case 3:
        						if (utf8_c < 0x00000800 || utf8_c > 0x0000FFFF) {
            						// Incorrectly encoded value.
        						}
        						break;
        					case 4:
        						if (utf8_c < 0x00010000 || utf8_c > 0x0010FFFF) {
            						// Incorrectly encoded value.
        						}
        						break;
        					}
        					sb.append((char) utf8_c);
    					}
    				}
    				/*
    				Char. number range  |        UTF-8 octet sequence
    				      (hexadecimal)    |              (binary)
    				   --------------------+---------------------------------------------
    				   0000 0000-0000 007F | 0xxxxxxx
    				   0000 0080-0000 07FF | 110xxxxx 10xxxxxx
    				   0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
    				   0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
    				*/
   					break;
    			}
    			break;
    		case S_LWS:
    			if (c == ' ' || c == '\t') {
    				sb.append(" ");
    				state = S_VALUE;
    			} else {
    				in.unread(c);
    				warcHeader.value = sb.toString().trim();
    				bLoop = false;
    			}
    			break;
    		case S_QUOTED_TEXT:
    			switch (c) {
    			case '\"':
    				if (bCr) {
    					// Misplaced CR.
        				bCr = false;
    				}
    				state = S_VALUE;
    				break;
    			case '\\':
    				if (bCr) {
    					// Misplaced CR.
        				bCr = false;
    				}
    				state = S_QUOTED_PAIR;
    				break;
    			case '\r':
    				bCr = true;
    				break;
    			case '\n':
    				if (!bCr) {
    					// Missing CR.
        				bCr = false;
    				}
    				state = S_LWS;
    				break;
    			default:
    				break;
    			}
    			break;
    		case S_QUOTED_PAIR:
    			break;
    		case S_QUOTED_LWS:
    			if (c == ' ' || c == '\t') {
    				sb.append(" ");
    				state = S_QUOTED_TEXT;
    			} else {
    				// Non LWS force end of quoted text parsing and header line.
    				in.unread(c);
    				warcHeader.value = sb.toString().trim();
    				bLoop = false;
    			}
    			break;
    		}
    	}
    	return warcHeader;
    }
}
