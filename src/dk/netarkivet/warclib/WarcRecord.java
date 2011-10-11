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

    boolean bMagic;
	boolean bVersion;

	int major = -1;
	int minor = -1;

	String warcType;

	// warcinfo only
	String warcFilename;

	String warcRecordId;
	URI warcRecordIdUri;

	String warcDate;
	Date warcDateDate;

	Long contentLength;

	String contentType;

	String warcTruncated;

	String warcIpAddress;
	InetAddress warcInetAddress;

	String warcConcurrentTo;
	URI warcConcurrentToUri;

	String warcRefersTo;
	URI warcRefersToUri;

	String warcTargetUri;
	URI warcTargetUriUri;

	String warcWarcinfoId;
	URI warcWarcInfoIdUri;

	String warcBlockDigest;

	String warcPayloadDigest;

	String warcIdentifiedPayloadType;

	// revisit only
	String warcProfile;

	Integer warcSegmentNumber;

	// continuation only
	String warcSegmentOriginId;
	URI warcSegmentOriginIdUrl;

	//continuation only
	Long warcSegmentTotalLength;

	public static WarcRecord parseRecord(ByteCountingInputStream in) {
		WarcRecord wr = new WarcRecord();
		try {
			if (wr.checkMagicVersion(in)) {
				System.out.println(wr.bMagic);
				System.out.println(wr.bVersion);
				System.out.println(wr.major + "." + wr.minor);

				wr.parseFields(in);

				if (wr.warcType != null) {
					Integer rt_idx = WarcConstants.recordTypeIdxMap.get(wr.warcType);
					if (rt_idx != null) {
						System.out.println("WARC-Type-Idx: " + rt_idx.intValue());
					}
					System.out.println("WARC-Type: " + wr.warcType);
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

	public boolean checkMagicVersion(ByteCountingInputStream in) throws IOException {
		bMagic = false;
		bVersion = false;
		String tmpStr;
		boolean bSeekMagic = true;
		while (bSeekMagic) {
			tmpStr = in.readLine();
			if (tmpStr != null) {
				if (tmpStr.length() > 0) {
					if (tmpStr.startsWith(WarcConstants.WARC_MAGIC_HEADER)) {
						bMagic = true;
						String versionStr = tmpStr.substring(WarcConstants.WARC_MAGIC_HEADER.length());
						String[] tmpArr = versionStr.split("\\.", -1);		// Slow?
						if (tmpArr.length >= 2 && tmpArr.length <= 4) {
							bVersion = true;
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
		return bMagic;
	}

	public void parseFields(ByteCountingInputStream in) throws IOException {
		String tmpStr;
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

							Integer fn_idx = WarcConstants.fieldNameIdxMap.get(field.toUpperCase());
							if (fn_idx != null) {
								switch (fn_idx.intValue()) {
								case WarcConstants.FN_IDX_WARC_TYPE:
									// TODO
									warcType = value;
									break;
								case WarcConstants.FN_IDX_WARC_RECORD_ID:
									warcRecordId = value;
									warcRecordIdUri = parseUri(value,
											WarcConstants.FN_WARC_RECORD_ID);
									break;
								case WarcConstants.FN_IDX_WARC_DATE:
									warcDate = value;
									warcDateDate = parseDate(value,
											WarcConstants.FN_WARC_DATE);
									break;
								case WarcConstants.FN_IDX_CONTENT_LENGTH:
									contentLength = parseLong(value,
											WarcConstants.FN_CONTENT_LENGTH);
									break;
								case WarcConstants.FN_IDX_CONTENT_TYPE:
									// TODO
									contentType = value;
									break;
								case WarcConstants.FN_IDX_WARC_CONCURRENT_TO:
									warcConcurrentTo = value;
									warcConcurrentToUri = parseUri(value,
											WarcConstants.FN_WARC_CONCURRENT_TO);
									break;
								case WarcConstants.FN_IDX_WARC_BLOCK_DIGEST:
									// TODO
									warcBlockDigest = value;
									break;
								case WarcConstants.FN_IDX_WARC_PAYLOAD_DIGEST:
									// TODO
									warcPayloadDigest = value;
									break;
								case WarcConstants.FN_IDX_WARC_IP_ADDRESS:
									warcIpAddress = value;
									warcInetAddress = parseIpAddress(value,
											WarcConstants.FN_WARC_IP_ADDRESS);
									break;
								case WarcConstants.FN_IDX_WARC_REFERS_TO:
									warcRefersTo = value;
									warcRefersToUri = parseUri(value,
											WarcConstants.FN_WARC_REFERS_TO);
									break;
								case WarcConstants.FN_IDX_WARC_TARGET_URI:
									warcTargetUri = value;
									warcTargetUriUri = parseUri(value,
											WarcConstants.FN_WARC_TARGET_URI);
									break;
								case WarcConstants.FN_IDX_WARC_TRUNCATED:
									// TODO
									warcTruncated = value;
									break;
								case WarcConstants.FN_IDX_WARC_WARCINFO_ID:
									warcWarcinfoId = value;
									warcWarcInfoIdUri = parseUri(value,
											WarcConstants.FN_WARC_WARCINFO_ID);
									break;
								case WarcConstants.FN_IDX_WARC_FILENAME:
									// TODO
									warcFilename = value;
									break;
								case WarcConstants.FN_IDX_WARC_PROFILE:
									// TODO
									warcProfile = value;
									break;
								case WarcConstants.FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE:
									// TODO
									warcIdentifiedPayloadType = value;
									break;
								case WarcConstants.FN_IDX_WARC_SEGMENT_ORIGIN_ID:
									warcSegmentOriginId = value;
									warcSegmentOriginIdUrl = parseUri(value,
											WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID);
									break;
								case WarcConstants.FN_IDX_WARC_SEGMENT_NUMBER:
									warcSegmentNumber = parseInteger(value,
											WarcConstants.FN_WARC_SEGMENT_NUMBER);
									break;
								case WarcConstants.FN_IDX_WARC_SEGMENT_TOTAL_LENGTH:
									warcSegmentTotalLength = parseLong(value,
											WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH);
									break;
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

}
