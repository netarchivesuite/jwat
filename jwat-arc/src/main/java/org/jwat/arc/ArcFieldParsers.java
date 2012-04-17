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
 * content-types, IP's, URI's and ARC dates.
 *
 * @author nicl
 */
public class ArcFieldParsers {

    /** Diagnostics used to report diagnoses.
     * Must be set prior to calling the various methods. */
    protected Diagnostics<Diagnosis> diagnostics;

    /**
     * Add a warning diagnosis on the given entity stating that it is empty.
     * @param entity entity examined
     */
    protected void addEmptyWarning(String entity) {
        diagnostics.addWarning(new Diagnosis(DiagnosisType.EMPTY, entity));
    }

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
     * Parses a string.
     * @param str the value to parse
     * @param field field name
     * @return the parsed value
     */
    protected String parseString(String str, String field) {
        return parseString(str, field, false);
    }

    /**
     * Parses a string.
     * @param str the value to parse
     * @param field field name
     * @param optional specifies if the value is optional or not
     * @return the parsed value
     */
    protected String parseString(String str, String field, boolean optional) {
        if (((str == null) || (str.trim().length() == 0))
            && (!optional)) {
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
         }
         return iVal;
    }

    /**
     * Returns an Integer object holding the value of the specified string.
     * @param intStr the value to parse.
     * @param field field name
     * @param optional specifies if the value is optional or not
     * @return an integer object holding the value of the specified string
     */
    protected Integer parseInteger(String intStr, String field,
                                   boolean optional) {
        Integer result = this.parseInteger(intStr, field);
        if((result == null) && (!optional)){
            // Missing integer value.
            addEmptyWarning("'" + field + "' field");
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
            } catch (Exception e) {
                // Invalid long value.
                addInvalidExpectedError("'" + field + "' value",
                        longStr,
                        "Numeric format");
            }
         } else {
             // Missing mandatory value.
             addEmptyWarning("'" + field + "' field");
         }
         return lVal;
    }

    /**
     * Parses ARC record content type.
     * @param contentTypeStr ARC record content type
     * @param field field name
     * @return ARC record content type
     */
    protected ContentType parseContentType(String contentTypeStr, String field) {
        ContentType contentType = null;
        if (contentTypeStr != null && contentTypeStr.length() != 0) {
            contentType = ContentType.parseContentType(contentTypeStr);
            if (contentType == null) {
                // Invalid content-type.
                addInvalidExpectedError("'" + field + "' value",
                        contentTypeStr,
                        ArcConstants.CONTENT_TYPE_FORMAT);
            }
        } else {
            // Missing content-type.
            addEmptyWarning("'" + field + "' field");
        }
        return contentType;
    }

    /**
     * Parses ARC record IP address.
     * @param ipAddress the IP address to parse
     * @param field field name
     * @return the IP address
     */
    protected InetAddress parseIpAddress(String ipAddress, String field) {
        InetAddress inetAddr = null;
        if (ipAddress != null && ipAddress.length() > 0) {
            inetAddr = IPAddressParser.getAddress(ipAddress);
            if (inetAddr == null) {
                // Invalid date.
                addInvalidExpectedError("'" + field + "' value",
                        ipAddress,
                        "IPv4 or IPv6 format");
            }
        } else {
            // Missing mandatory value.
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
        if ((uriStr != null) && (uriStr.length() != 0)) {
            try {
                uri = new URI(uriStr);
            } catch (Exception e) {
                // Invalid URI.
                addInvalidExpectedError("'" + field + "' value",
                        uriStr,
                        "URI format");
            }
        } else {
            // Missing mandatory value.
            addEmptyWarning("'" + field + "' field");
        }
        return uri;
    }

    /**
     * Parses ARC record date.
     * @param dateStr the date to parse.
     * @param field field name
     * @return the formatted date.
     */
    protected Date parseDate(String dateStr, String field) {
        Date date = null;
        if (dateStr != null && dateStr.length() > 0) {
                date = ArcDateParser.getDate(dateStr);
                if (date == null) {
                    // Invalid date.
                    addInvalidExpectedError("'" + field + "' value",
                            dateStr,
                            ArcConstants.ARC_DATE_FORMAT);
                }
        } else {
            // Missing mandatory value.
            addEmptyWarning("'" + field + "' field");
        }
        return date;
    }

}
