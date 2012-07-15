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

import java.net.InetAddress;
import java.net.URI;
import java.util.Date;

import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.IPAddressParser;

/**
 * Separate class containing all the different types of field parser.
 * Including validating parsers for strings, integers, longs,
 * content-types, IP's, URI's, WARC dates, and WARC digests.
 *
 * @author nicl
 */
public class WarcFieldParsers {

    /** Diagnostics used to report diagnoses.
     * Must be set prior to calling the various methods. */
    protected Diagnostics<Diagnosis> diagnostics;

    /**
     * Add an error diagnosis on the given entity stating that it is invalid
     * and something else was expected. The optional information should provide
     * more details and/or format information.
     * @param entity entity examined
     * @param information optional extra information
     */
    protected void addInvalidExpectedError(String entity, String... information) {
        diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED, entity, information));
    }

    /**
     * Add a warning diagnosis on the given entity stating that it is empty.
     * @param entity entity examined
     */
    protected void addEmptyWarning(String entity) {
        diagnostics.addWarning(new Diagnosis(DiagnosisType.EMPTY, entity));
    }

    /**
     * Parses a string.
     * @param str the value to parse
     * @param field field name
     * @return the parsed value
     */
    protected String parseString(String str, String field) {
        if (((str == null) || (str.trim().length() == 0))) {
            addEmptyWarning("'" + field + "' field");
        }
        return str;
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
            } catch (Exception e) {
                // Invalid integer value.
                addInvalidExpectedError("'" + field + "' value",
                        intStr,
                        "Numeric format");
            }
         } else {
             // Missing integer value.
             addEmptyWarning("'" + field + "' field");
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
            } catch (Exception e) {
                // Invalid long value.
                addInvalidExpectedError("'" + field + "' value",
                        longStr,
                        "Numeric format");
            }
         } else {
             // Missing long value.
             addEmptyWarning("'" + field + "' field");
         }
         return lVal;
    }

    /**
     * Parse and validate content-type string with optional parameters.
     * @param contentTypeStr content-type string to parse
     * @param field field name
     * @return content-type wrapper object or null
     */
    protected ContentType parseContentType(String contentTypeStr, String field) {
        ContentType contentType = null;
        if (contentTypeStr != null && contentTypeStr.length() != 0) {
            contentType = ContentType.parseContentType(contentTypeStr);
            if (contentType == null) {
                // Invalid content-type.
                addInvalidExpectedError("'" + field + "' value",
                        contentTypeStr,
                        WarcConstants.CONTENT_TYPE_FORMAT);
            }
        } else {
            // Missing content-type.
            addEmptyWarning("'" + field + "' field");
        }
        return contentType;
    }

    /**
     * Parses WARC record IP address.
     * @param ipAddress the IP address to parse
     * @param field field name
     * @return the IP address
     */
    protected InetAddress parseIpAddress(String ipAddress, String field) {
        InetAddress inetAddr = null;
        if (ipAddress != null && ipAddress.length() > 0) {
            inetAddr = IPAddressParser.getAddress(ipAddress);
            if (inetAddr == null) {
                // Invalid ip address.
                addInvalidExpectedError("'" + field + "' value",
                        ipAddress,
                        "IPv4 or IPv6 format");
            }
        } else {
            // Missing ip address.
            addEmptyWarning("'" + field + "' field");
        }
        return inetAddr;
    }

    /**
     * Returns an URL object holding the value of the specified string.
     * @param uriStr the URL to parse
     * @param field field name
     * @return an URL object holding the value of the specified string
     */
    protected URI parseUri(String uriStr, String field) {
        URI uri = null;
        if (uriStr != null && uriStr.length() != 0) {
            if (uriStr.startsWith("<") && uriStr.endsWith(">")) {
                uriStr = uriStr.substring(1, uriStr.length() - 1);
            }
            try {
                uri = new URI(uriStr);
            } catch (Exception e) {
                // Invalid URI.
                addInvalidExpectedError("'" + field + "' value",
                        uriStr,
                        "URI format");
            }
            if (uri != null) {
                String scheme = uri.getScheme();
                if (scheme == null || scheme.length() == 0) {
                    uri = null;
                    // Relative URI.
                    addInvalidExpectedError("'" + field + "' value",
                            uriStr,
                            "Absolute URI");
                }
            }
        } else {
            // Missing URI.
            addEmptyWarning("'" + field + "' field");
        }
        return uri;
    }

    /**
     * Parses WARC record date.
     * @param dateStr the date to parse.
     * @param field field name
     * @return the formatted date.
     */
    protected Date parseDate(String dateStr, String field) {
        Date date = null;
        if (dateStr != null && dateStr.length() > 0) {
                date = WarcDateParser.getDate(dateStr);
                if (date == null) {
                    // Invalid date.
                    addInvalidExpectedError("'" + field + "' value",
                            dateStr,
                            WarcConstants.WARC_DATE_FORMAT);
                }
        } else {
            // Missing date.
            addEmptyWarning("'" + field + "' field");
        }
        return date;
    }

    /**
     * Parse and validate WARC digest string.
     * @param labelledDigest WARC digest string to parse
     * @param field field name
     * @return digest wrapper object or null
     */
    protected WarcDigest parseDigest(String labelledDigest, String field) {
        WarcDigest digest = null;
        if (labelledDigest != null && labelledDigest.length() > 0) {
                digest = WarcDigest.parseWarcDigest(labelledDigest);
                if (digest == null) {
                    // Invalid digest.
                    addInvalidExpectedError("'" + field + "' value",
                            labelledDigest,
                            WarcConstants.WARC_DIGEST_FORMAT);
                }
        } else {
            // Missing digest.
            addEmptyWarning("'" + field + "' field");
        }
        return digest;
    }

}
