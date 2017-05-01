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

import java.net.InetAddress;
import java.util.Date;

import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.IPAddressParser;
import org.jwat.common.Uri;
import org.jwat.common.UriProfile;

/**
 * Separate class containing all the different types of field parser.
 * Including validating parsers for strings, integers, longs,
 * content-types, IP's, URI's and ARC dates.
 *
 * @author nicl
 */
public class ArcFieldParsers {

    /** Diagnostics used to report diagnoses. Must be set prior to calling the various methods. */
    public Diagnostics<Diagnosis> diagnostics;

    /**
     * Add an error diagnosis on the given entity stating that it is invalid
     * and something else was expected. The optional information should provide
     * more details and/or format information.
     * @param entity entity examined
     * @param information optional extra information
     */
    public void addInvalidExpectedError(String entity, String... information) {
        diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED, entity, information));
    }

    /**
     * Add an error diagnosis on the given entity stating that it is required
     * but is missing.
     * @param entity entity examined
     */
    public void addRequiredMissingError(String entity) {
        diagnostics.addError(new Diagnosis(DiagnosisType.REQUIRED_MISSING, entity));
    }

    /**
     * Validates that the string is not null but only if nullable is false.
     * @param str the value to validate
     * @param field field name
     * @param nullable allow empty or null value
     * @return the original value
     */
    public String parseString(String str, String field, boolean nullable) {
        if ((!nullable) && ((str == null) || (str.trim().length() == 0))) {
            addRequiredMissingError("'" + field + "' value");
        }
        return str;
    }

    /**
     * Returns an Integer object holding the value of the specified string.
     * @param intStr the value to parse.
     * @param field field name
     * @param nullable allow empty or null value
     * @return an integer object holding the value of the specified string or null, if unable to parse the value as an integer
     */
    public Integer parseInteger(String intStr, String field, boolean nullable) {
         Integer iVal = null;
         if (intStr != null && intStr.length() > 0) {
            try {
                iVal = Integer.valueOf(intStr);
            } catch (Exception e) {
                // Invalid integer value.
                addInvalidExpectedError("'" + field + "' value", intStr, "Numeric format");
            }
         } else if (!nullable) {
             // Missing mandatory value.
             addRequiredMissingError("'" + field + "' value");
         }
         return iVal;
    }

    /**
     * Returns a Long object holding the value of the specified string.
     * @param longStr the value to parse.
     * @param field field name
     * @param nullable allow empty or null value
     * @return a long object holding the value of the specified string or null, if unable to parse the value as a Long
     */
    public Long parseLong(String longStr, String field, boolean nullable) {
        Long lVal = null;
        if (longStr != null && longStr.length() > 0) {
            try {
                lVal = Long.valueOf(longStr);
            } catch (Exception e) {
                // Invalid long value.
                addInvalidExpectedError("'" + field + "' value", longStr, "Numeric format");
            }
        } else if (!nullable) {
             // Missing mandatory value.
            addRequiredMissingError("'" + field + "' value");
        }
        return lVal;
    }

    /**
     * Parse and validate a content type.
     * @param contentTypeStr ARC record content type
     * @param field field name
     * @param nullable allow empty or null value
     * @return content type or null, if unable to extract the content-type
     */
    public ContentType parseContentType(String contentTypeStr, String field, boolean nullable) {
        ContentType contentType = null;
        if (contentTypeStr != null && contentTypeStr.length() != 0) {
            contentType = ContentType.parseContentType(contentTypeStr);
            if (contentType == null) {
                // Invalid content-type.
                addInvalidExpectedError("'" + field + "' value", contentTypeStr, ArcConstants.CONTENT_TYPE_FORMAT);
            }
        } else if (!nullable) {
            // Missing content-type.
            addRequiredMissingError("'" + field + "' value");
        }
        return contentType;
    }

    /**
     * Parse and validate an IP address.
     * @param ipAddress the IP address to parse
     * @param field field name
     * @param nullable allow empty or null value
     * @return the IP address or null, if unable to parse the value as an IP-address
     */
    public InetAddress parseIpAddress(String ipAddress, String field, boolean nullable) {
        InetAddress inetAddr = null;
        if (ipAddress != null && ipAddress.length() > 0) {
            inetAddr = IPAddressParser.getAddress(ipAddress);
            if (inetAddr == null) {
                // Invalid date.
                addInvalidExpectedError("'" + field + "' value", ipAddress, "IPv4 or IPv6 format");
            }
        } else if (!nullable) {
            // Missing mandatory value.
            addRequiredMissingError("'" + field + "' value");
        }
        return inetAddr;
    }

    /**
     * Returns an URL object holding the value of the specified string.
     * @param uriStr the URL to parse
     * @param uriProfile profile used to validate URI
     * @param field field name
     * @param nullable allow empty or null value
     * @return an URL object holding the value of the specified string or null, if unable to parse the value as an URL object
     */
    public Uri parseUri(String uriStr, UriProfile uriProfile, String field, boolean nullable) {
        Uri uri = null;
        if ((uriStr != null) && (uriStr.length() != 0)) {
            try {
                uri = new Uri(uriStr, uriProfile);
            } catch (Exception e) {
                // Invalid URI.
                addInvalidExpectedError("'" + field + "' value", uriStr, e.getMessage());
            }
            if (uri != null) {
                String scheme = uri.getScheme();
                if (scheme == null) {
                    uri = null;
                    // Relative URI.
                    addInvalidExpectedError("'" + field + "' value", uriStr, "Absolute URI");
                } else {
                    scheme = scheme.toLowerCase();
                }
            }
        } else if (!nullable) {
            // Missing mandatory value.
            addRequiredMissingError("'" + field + "' value");
        }
        return uri;
    }

    /**
     * Parses ARC record date.
     * @param dateStr the date to parse.
     * @param field field name
     * @param nullable allow empty or null value
     * @return the formatted date or null, if unable to parse the value as an ARC record date
     */
    public Date parseDate(String dateStr, String field, boolean nullable) {
        Date date = null;
        if (dateStr != null && dateStr.length() > 0) {
                date = ArcDateParser.getDate(dateStr);
                if (date == null) {
                    // Invalid date.
                    addInvalidExpectedError("'" + field + "' value", dateStr, ArcConstants.ARC_DATE_FORMAT);
                }
        } else if (!nullable) {
            // Missing mandatory value.
            addRequiredMissingError("'" + field + "' value");
        }
        return date;
    }

}
