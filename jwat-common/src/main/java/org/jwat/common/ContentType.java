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
package org.jwat.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class represents a content-type string parsed, validated and decomposed
 * into its separate components. It's based on the rfc2616 text and accordingly
 * fairly strict concerning whitespaces. Whitespace is only permissible after
 * a ';' and before and after the whole content-type string.
 * ContentType, MediaType and parameter names are all converted to lower case.
 *
 * @author nicl
 */
public class ContentType {

    /** Beginning state. */
    protected static final int S_START = 0;
    /** Parsing content-type state. */
    protected static final int S_CONTENTTYPE = 1;
    /** Parsing media-type state. */
    protected static final int S_MEDIATYPE = 2;
    /** Lenient parsing of trailing whitespace after media-type state. */
    protected static final int S_MEDIATYPE_WHITESPACE = 3;
    /** Parsing after a ';' character state. */
    protected static final int S_SEMICOLON = 4;
    /** Parsing the parameter name state. */
    protected static final int S_PARAM_NAME = 5;
    /** Parsing after a '=' character state. */
    protected static final int S_PARAM_EQ = 6;
    /** Parsing a parameter value token state. */
    protected static final int S_PARAM_VALUE = 7;
    /** Parsing a quote parameter value state. */
    protected static final int S_PARAM_QUOTED_VALUE = 8;
    /** Parsing a quoted pair character state. */
    protected static final int S_PARAM_QUOTED_PAIR = 9;
    /** Lenient parsing of trailing whitespace after argument value state. */
    protected static final int S_PARAM_VALUE_WHITESPACE = 10;

    /** Control character characteristic. */
    protected static final int CC_CONTROL = 1;
    /** Separator character characteristic. */
    protected static final int CC_SEPARATOR_WS = 2;

    /** Parsed Content-type. */
    public String contentType;

    /** Parsed Media-type. */
    public String mediaType;

    /** Optional <code>Map</code> of parameters. */
    public Map<String, String> parameters;

    /** rfc2616 separator minus space and tab. */
    protected static final String separators = "()<>@,;:\\\"/[]?={} \t";

    /** Table of separator and control characters. */
    protected static final byte[] charCharacteristicsTab = new byte[256];

    /*
     * Populate table with separator and control characters.
     */
    static {
        for (int i=0; i<separators.length(); ++i) {
            charCharacteristicsTab[separators.charAt(i)] = CC_SEPARATOR_WS;
        }
        for (int i=0; i<32; ++i) {
            if (i != '\t') {
                charCharacteristicsTab[i] = CC_CONTROL;
            }
        }
    }

    /**
     * Check whether character is a valid token.
     * @param c character to check
     * @return boolean indicating whether character is a valid token
     */
    public static boolean isTokenCharacter(int c) {
        return (c >= 0 && c < 256 && charCharacteristicsTab[c] == 0) || c >= 256;
    }

    /**
     * Tries to parse and validate the given content-type string. Also tries
     * to parse and validate any optional parameters present in the string.
     * @param contentTypeStr content-type string
     * @return a content-type object with all information in separate
     * structures.
     */
    public static ContentType parseContentType(String contentTypeStr) {
        if (contentTypeStr == null || contentTypeStr.length() == 0) {
            return null;
        }
        ContentType ct = new ContentType();
        StringBuffer nameSb = new StringBuffer();
        StringBuffer valueSb = null;
        int state = S_START;
        int idx = 0;
        int c;
        boolean bLoop = true;
        while (bLoop) {
            if (idx < contentTypeStr.length()) {
                c = contentTypeStr.charAt(idx);
            } else {
                c = -1;
                bLoop = false;
            }
            switch (state) {
            case S_START:
                if (c == ' ' || c == '\t') {
                    ++idx;
                } else if (isTokenCharacter(c)) {
                    nameSb.setLength(0);
                    nameSb.append((char) c);
                    ++idx;
                    state = S_CONTENTTYPE;
                } else {
                    // /;= (-1) etc.
                    return null;
                }
                break;
            case S_CONTENTTYPE:
                if (isTokenCharacter(c)) {
                    nameSb.append((char) c);
                    ++idx;
                } else if (c == '/') {
                    // Name always at least one.
                    ct.contentType = nameSb.toString().toLowerCase();
                    ++idx;
                    nameSb.setLength(0);
                    state = S_MEDIATYPE;
                } else {
                    // ;= (-1) etc.
                    return null;
                }
                break;
            case S_MEDIATYPE:
                if (isTokenCharacter(c)) {
                    nameSb.append((char) c);
                    ++idx;
                } else if (c == -1) {
                    if (nameSb.length() == 0) {
                        return null;
                    }
                    ct.mediaType = nameSb.toString().toLowerCase();
                } else if (c == ';') {
                    if (nameSb.length() == 0) {
                        return null;
                    }
                    ct.mediaType = nameSb.toString().toLowerCase();
                    ++idx;
                    valueSb = new StringBuffer();
                    ct.parameters = new HashMap<String, String>();
                    state = S_SEMICOLON;
                } else if (c == ' ' || c == '\t') {
                    if (nameSb.length() == 0) {
                        return null;
                    }
                    ct.mediaType = nameSb.toString().toLowerCase();
                    ++idx;
                    state = S_MEDIATYPE_WHITESPACE;
                } else {
                    return null;
                }
                break;
            case S_MEDIATYPE_WHITESPACE:
                if (c == ' ' || c == '\t') {
                    ++idx;
                } else if (c != -1) {
                    return null;
                }
                break;
            case S_SEMICOLON:
                if (c == ' ' || c == '\t') {
                    ++idx;
                } else if (isTokenCharacter(c)) {
                    nameSb.setLength(0);
                    valueSb.setLength(0);
                    nameSb.append((char) c);
                    ++idx;
                    state = S_PARAM_NAME;
                } else if (c == -1) {
                    // Allow contenttype/mediatype; and optional parameters
                } else {
                    // /;= etc.
                    return null;
                }
                break;
            case S_PARAM_NAME:
                if (isTokenCharacter(c)) {
                    nameSb.append((char) c);
                    ++idx;
                } else if (c == '=') {
                    // Name always at least one.
                    ++idx;
                    state = S_PARAM_EQ;
                } else {
                    // (-1) etc.
                    return null;
                }
                break;
            case S_PARAM_EQ:
                if (isTokenCharacter(c)) {
                    valueSb.append((char) c);
                    ++idx;
                    state = S_PARAM_VALUE;
                } else if (c == '"') {
                    ++idx;
                    state = S_PARAM_QUOTED_VALUE;
                }
                else {
                    // (-1) etc.
                    return null;
                }
                break;
            case S_PARAM_VALUE:
                if (isTokenCharacter(c)) {
                    valueSb.append((char) c);
                    ++idx;
                } else if (c == -1) {
                    ct.parameters.put(nameSb.toString().toLowerCase(),
                            valueSb.toString());
                } else if (c == ';') {
                    ct.parameters.put(nameSb.toString().toLowerCase(),
                            valueSb.toString());
                    ++idx;
                    state = S_SEMICOLON;
                } else if (c == ' ' || c == '\t') {
                    ct.parameters.put(nameSb.toString().toLowerCase(),
                            valueSb.toString());
                    ++idx;
                    state = S_PARAM_VALUE_WHITESPACE;
                } else {
                    // etc.
                    return null;
                }
                break;
            case S_PARAM_QUOTED_VALUE:
                if (c == '"') {
                    ct.parameters.put(nameSb.toString().toLowerCase(),
                            valueSb.toString());
                    ++idx;
                    state = S_PARAM_VALUE_WHITESPACE;
                } else if (c == '\\') {
                    ++idx;
                    state = S_PARAM_QUOTED_PAIR;
                } else if (c != -1) {
                    valueSb.append((char) c);
                    ++idx;
                } else {
                    // (-1)
                    return null;
                }
                break;
            case S_PARAM_QUOTED_PAIR:
                if (c != -1) {
                    valueSb.append((char) c);
                    ++idx;
                    state = S_PARAM_QUOTED_VALUE;
                } else {
                    // (-1)
                    return null;
                }
                break;
            case S_PARAM_VALUE_WHITESPACE:
                if (c == ' ' || c == '\t') {
                    ++idx;
                } else if (c == ';') {
                    ++idx;
                    state = S_SEMICOLON;
                } else if (c != -1) {
                    return null;
                }
                break;
            }
        }
        return ct;
    }

    /**
     * Return parameter value associated with supplied parameter name.
     * @param name parameter name
     * @return parameter value or null
     */
    public String getParameter(String name) {
        if (name == null || name.length() == 0 || parameters == null) {
            return null;
        }
        return parameters.get(name.toLowerCase());
    }

    /**
     * Set a parameter with the given (name, value) pair.
     * @param name parameter name
     * @param value parameter value
     */
    public void setParameter(String name, String value) {
        if (name != null && name.length() > 0 && value != null) {
            if (parameters == null) {
                parameters = new HashMap<String, String>();
            }
            parameters.put(name.toLowerCase(), value);
        }
    }

    /**
     * Determines whether a string should be quoted by examining if the string
     * contains tabs and/or spaces.
     * @param str input string
     * @return boolean indicating if the string should be quoted
     */
    public static boolean quote(String str) {
        boolean quote = false;
        if (str != null && str.length() > 0) {
            int idx = 0;
            char c;
            while (idx<str.length() && !quote) {
                c = str.charAt(idx++);
                if (c < 256 && (charCharacteristicsTab[c] & CC_SEPARATOR_WS) != 0) {
                    quote = true;
                }
            }
        }
        return quote;
    }

    /**
     * Returns the content-type omitting any parameters present.
     * @return content-type without any parameters included
     */
    public String toStringShort() {
        StringBuffer sb = new StringBuffer();
        sb.append(contentType);
        sb.append('/');
        sb.append(mediaType);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(contentType);
        sb.append('/');
        sb.append(mediaType);
        if (parameters != null) {
            Iterator<Entry<String, String>> iter = parameters.entrySet().iterator();
            Entry<String, String> entry;
            while (iter.hasNext()) {
                sb.append("; ");
                entry = iter.next();
                sb.append(entry.getKey());
                sb.append('=');
                if (quote(entry.getValue())) {
                    sb.append('"');
                    sb.append(entry.getValue());
                    sb.append('"');
                }
                else {
                    sb.append(entry.getValue());
                }
            }
        }
        return sb.toString();
    }

}
