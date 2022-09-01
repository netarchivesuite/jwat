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

import java.net.URISyntaxException;

/**
 * Implementation of an URI profile. A profile can be used to customize which
 * characters and features are acceptable when a certain profile is used.
 *
 * An array of integers is used to defined which categories the first
 * 8-bit characters belong to.
 *
 * @author nicl
 */
public class UriProfile {

    /** Bit to categorize a char as an alpha. */
    public static final int B_ALPHAS = 1 << 0;
    /** Bit to categorize a char as a digit. */
    public static final int B_DIGITS = 1 << 1;
    /** Bit to categorize a char as first in scheme. */
    public static final int B_SCHEME_FIRST = 1 << 2;
    /** Bit to categorize a char as following in scheme. */
    public static final int B_SCHEME_FOLLOW = 1 << 3;
    /** Bit to categorize a char as UNRESERVED in RFC3986. */
    public static final int B_UNRESERVED = 1 << 4;
    /** Bit to categorize a char as GEN-DELIMS in RFC3986. */
    public static final int B_GEN_DELIMS = 1 << 5;
    /** Bit to categorize a char as SUB-DELIMS in RFC3986. */
    public static final int B_SUB_DELIMS = 1 << 6;
    /** Bit to categorize a char as RESERVED in RFC3986. */
    public static final int B_RESERVED = 1 << 7;
    /** Bit to categorize a char as PCHAR in RFC3986. */
    public static final int B_PCHAR = 1 << 8;
    /** Bit to categorize a char as USERINFO in RFC3986. */
    public static final int B_USERINFO = 1 << 9;
    /** Bit to categorize a char as REGNAME in RFC3986. */
    public static final int B_REGNAME = 1 << 10;
    /** Bit to categorize a char as SEGMENT in RFC3986. */
    public static final int B_SEGMENT = 1 << 11;
    /** Bit to categorize a char as SEGMENT-NZ in RFC3986. */
    public static final int B_SEGMENT_NZ = 1 << 12;
    /** Bit to categorize a char as SEGMENT-NZ-NC in RFC3986. */
    public static final int B_SEGMENT_NZ_NC = 1 << 13;
    /** Bit to categorize a char as PATH in RFC3986. */
    public static final int B_PATH = 1 << 14;
    /** Bit to categorize a char as QUERY in RFC3986. */
    public static final int B_QUERY = 1 << 15;
    /** Bit to categorize a char as FRAGMENT in RFC3986. */
    public static final int B_FRAGMENT = 1 << 16;

    /** Array of integers used to categorize all 8bit chars. */
    protected final int[] charTypeMap = new int[256];

    /** Does profile allow relative URIs. */
    public boolean bAllowRelativeUris;

    /** Does profile allow 16-bit percent encoding. */
    public boolean bAllow16bitPercentEncoding;

    /** Does profile allow invalid percent encoding. */
    public boolean bAllowInvalidPercentEncoding;

    /**
     * Construct an <code>UriProfile</code> initialized with RFC3986
     * rules.
     */
    public UriProfile() {
        for (int i=0; i<defaultCharTypeMap.length; ++i) {
            charTypeMap[i] = defaultCharTypeMap[i];
        }
        bAllowRelativeUris = true;
        bAllow16bitPercentEncoding = false;
        bAllowInvalidPercentEncoding = false;
    }

    /**
     * Construct an <code>UriProfile</code> initialized from another profile.
     * @param uriProfile URI profile to base a new profile on
     */
    public UriProfile(UriProfile uriProfile) {
        for (int i=0; i<charTypeMap.length; ++i) {
            charTypeMap[i] = uriProfile.charTypeMap[i];
        }
        bAllowRelativeUris = uriProfile.bAllowRelativeUris;
        bAllow16bitPercentEncoding = uriProfile.bAllow16bitPercentEncoding;
        bAllowInvalidPercentEncoding = uriProfile.bAllowInvalidPercentEncoding;
    }

    /**
     * Helper method to populate the profiles integer array with character
     * categories.
     * @param chars characters to categorize or null
     * @param bw_and character categories to include in new category
     * @param bw_or new category/categories
     */
    public void charTypeAddAndOr(String chars, int bw_and, int bw_or) {
        UriProfile.charTypeAddAndOr(charTypeMap, chars, bw_and, bw_or);
    }

    /**
     * Given a collection of characters find the index of the first occurrence
     * of any of the characters in the supplied string starting from a certain
     * position.
     * @param bw_and bits identifying one or more character categories
     * @param str string to search through
     * @param pos position to start searching from
     * @return index of the first character in the string present in the
     * character collection(s)
     * @throws URISyntaxException if a character with a value larger that 255
     * is encountered
     */
    public int indexOf(int bw_and, String str, int pos) throws URISyntaxException {
        int limit = str.length();
        char c;
        while (pos < limit) {
            c = str.charAt(pos);
            if (c < 256) {
                if ((charTypeMap[c] & bw_and) != 0) {
                    return pos;
                }
                ++pos;
            } else {
                throw new URISyntaxException(str, "Invalid URI character '" + (Character.isISOControl(c)?String.format("0x%02x", (int)c):c) + "'");
            }
        }
        return -1;
    }

    /**
     * Validate an URI component using two character categories. One category
     * for the first character and another for the following characters.
     * @param str URI component string
     * @param bw_and_first bits identifying first character categories
     * @param bw_and_follow bits identifying following character categories
     * @throws URISyntaxException if an error occurs parsing component
     */
    public void validate_first_follow(String str, int bw_and_first, int bw_and_follow) throws URISyntaxException {
        int pos = 0;
        int limit = str.length();
        char c;
        while (pos < limit) {
            c = str.charAt(pos);
            // indexOf has ensured that the scheme does not have character values > 255.
            if (pos == 0 && ((charTypeMap[c] & UriProfile.B_SCHEME_FIRST) == 0)) {
                throw new URISyntaxException(str, "Invalid URI scheme component");
            } else if ((charTypeMap[c] & UriProfile.B_SCHEME_FOLLOW) == 0) {
                throw new URISyntaxException(str, "Invalid URI scheme component");
            }
            ++pos;
        }
    }

    /**
     * Validates an URI component according to the supplied character category
     * bitfield.
     * @param bw_and bits identifying one or more character categories
     * @param componentName URI component name
     * @param str URI component string
     * @return decoded and validated string
     * @throws URISyntaxException if an error occurs parsing/validating component
     */
    public String validate_decode(int bw_and, String componentName, String str) throws URISyntaxException {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        int ppos;
        int limit = str.length();
        char c;
        int decode = 0;
        int tmpC;
        char decodedC;
        boolean bValid;
        while (pos < limit) {
            c = str.charAt(pos++);
            if (c < 256) {
                if ((charTypeMap[c] & bw_and) == 0) {
                    if (c == '%') {
                        ppos = pos - 1;
                        if (pos < limit) {
                            c = str.charAt(pos);
                            if (c == 'u' || c == 'U') {
                                if (!bAllow16bitPercentEncoding) {
                                    if (!bAllowInvalidPercentEncoding) {
                                        throw new URISyntaxException(str, "Invalid URI " + componentName + " component - 16-bit percent encoding not allowed");
                                    } else {
                                        bValid = false;
                                    }
                                } else {
                                    ++pos;
                                    decode = 4;
                                    bValid = true;
                                }
                            } else {
                                decode = 2;
                                bValid = true;
                            }
                            decodedC = 0;
                            while (bValid && decode > 0) {
                                if (pos < limit) {
                                    c = str.charAt(pos++);
                                    decodedC <<= 4;
                                    if (c < 256) {
                                        tmpC = asciiHexTab[c];
                                        if (tmpC != -1) {
                                            decodedC |= tmpC;
                                            --decode;
                                        } else {
                                            bValid = false;
                                        }
                                    } else {
                                        bValid = false;
                                    }
                                } else {
                                    bValid = false;
                                }
                            }
                            if (!bValid && !bAllowInvalidPercentEncoding) {
                                throw new URISyntaxException(str, "Invalid URI " + componentName + " component - invalid percent encoding");
                            }
                            sb.append((char) decodedC);
                        } else {
                            if (!bAllowInvalidPercentEncoding) {
                                throw new URISyntaxException(str, "Invalid URI " + componentName + " component - incomplete percent encoding");
                            } else {
                                bValid = false;
                            }
                        }
                        if (!bValid) {
                            while (ppos < pos) {
                                sb.append(str.charAt(ppos++));
                            }
                        }
                    } else {
                        throw new URISyntaxException(str, "Invalid URI " + componentName + " component - invalid character '" + (Character.isISOControl(c)?String.format("0x%02x", (int)c):c) + "'");
                    }
                } else {
                    sb.append(c);
                }
            } else {
                throw new URISyntaxException(str, "Invalid URI " + componentName + " component - invalid character '" + (Character.isISOControl(c)?String.format("0x%02x", (int)c):c) + "'");
            }
        }
        return sb.toString();
    }

    /** Hex char to integer conversion table. */
    public static int[] asciiHexTab = new int[256];

    /** Integer to hex char conversion table. */
    public static char[] hexTab = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /*
     * Initialize ASCII hex table.
     */
    static {
        String hex = "0123456789abcdef";
        for (int i=0; i<asciiHexTab.length; ++i) {
            asciiHexTab[i] = hex.indexOf(i);
        }
        hex = hex.toUpperCase();
        for (int i=0; i<hex.length(); ++i) {
            asciiHexTab[hex.charAt(i)] = i;
        }
    }

    /**
     * Helper method to populate an integer array with character categories.
     * @param charTypeMap integer array with character categories
     * @param chars characters to categorize or null
     * @param bw_and character categories to include in new category
     * @param bw_or new category/categories
     */
    public static void charTypeAddAndOr(int[] charTypeMap, String chars, int bw_and, int bw_or) {
        if (chars != null) {
            for (int i=0; i<chars.length(); ++i) {
                charTypeMap[chars.charAt(i)] |= bw_or;
            }
        }
        if (bw_and != 0) {
            for (int i=0; i<charTypeMap.length; ++i) {
                if ((charTypeMap[i] & bw_and) != 0) {
                    charTypeMap[i] |= bw_or;
                }
            }
        }
    }

    /** Default array of integers used to categorize all 8bit chars. */
    protected static int[] defaultCharTypeMap = new int[256];

    /*
     * Initialize default char type map.
     */
    static {
        // Alphas.
        String alphas = "abcdefghijklmnopqrstuvwxyz";
        charTypeAddAndOr(defaultCharTypeMap, alphas, 0, B_ALPHAS);
        charTypeAddAndOr(defaultCharTypeMap, alphas.toUpperCase(), 0, B_ALPHAS);

        // Digits.
        String digits = "1234567890";
        charTypeAddAndOr(defaultCharTypeMap, digits, 0, B_DIGITS);

        // scheme first/follow.
        // scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
        String scheme = "+-.";
        charTypeAddAndOr(defaultCharTypeMap, null, B_ALPHAS, B_SCHEME_FIRST | B_SCHEME_FOLLOW);
        charTypeAddAndOr(defaultCharTypeMap, scheme, B_DIGITS, B_SCHEME_FOLLOW);

        // unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
        String unreserved = "-._~";
        charTypeAddAndOr(defaultCharTypeMap, unreserved, B_ALPHAS | B_DIGITS, B_UNRESERVED);

        // gen-delims (reserved)
        String genDelims = ":/?#[]@";
        charTypeAddAndOr(defaultCharTypeMap, genDelims, 0, B_GEN_DELIMS | B_RESERVED);

        // sub-delims (reserved)
        String subDelims  = "!$&'()*+,;=";
        charTypeAddAndOr(defaultCharTypeMap, subDelims, 0, B_SUB_DELIMS | B_RESERVED);

        // pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
        String pchar = ":@";
        charTypeAddAndOr(defaultCharTypeMap, pchar, B_UNRESERVED | B_SUB_DELIMS, B_PCHAR);

        // userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
        String userinfo = ":";
        charTypeAddAndOr(defaultCharTypeMap, userinfo, B_UNRESERVED | B_SUB_DELIMS, B_USERINFO);

        // reg-name = *( unreserved / pct-encoded / sub-delims )
        charTypeAddAndOr(defaultCharTypeMap, null, B_UNRESERVED | B_SUB_DELIMS, B_REGNAME);

        // segment = *pchar
        // segment-nz = 1*pchar
        charTypeAddAndOr(defaultCharTypeMap, null, B_PCHAR, B_SEGMENT);
        charTypeAddAndOr(defaultCharTypeMap, null, B_PCHAR, B_SEGMENT_NZ);

        // segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )
        // non-zero-length segment without any colon ":"
        String segment_nz_nc = "@";
        charTypeAddAndOr(defaultCharTypeMap, segment_nz_nc, B_UNRESERVED | B_SUB_DELIMS, B_SEGMENT_NZ_NC);

        // path
        String path = "/";
        charTypeAddAndOr(defaultCharTypeMap, path, B_PCHAR, B_PATH);

        // query = *( pchar / "/" / "?" )
        String query =  "/?";
        charTypeAddAndOr(defaultCharTypeMap, query, B_PCHAR, B_QUERY);

        // fragment = *( pchar / "/" / "?" )
        String fragment = "/?";
        charTypeAddAndOr(defaultCharTypeMap, fragment, B_PCHAR, B_FRAGMENT);
    }

    /** RFC3986 compliant URI profile. */
    public static final UriProfile RFC3986;

    /** Modified RFC3986 URI profile which disallows relative URIs
     *  and allows 16-bit percent encoding. */
    public static final UriProfile RFC3986_ABS_16BIT;

    /** Relaxed RFC3986 URI profile which disallows relative URIs,
     *  allows 16-bit percent encoding, allow invalid percent encoding
     *  and allows most 8-bit characters. */
    public static final UriProfile RFC3986_ABS_16BIT_LAX;

    /*
     * Initialize default profile.
     */
    static {
        RFC3986 = new UriProfile();

        RFC3986_ABS_16BIT = new UriProfile();
        RFC3986_ABS_16BIT.bAllowRelativeUris = false;
        RFC3986_ABS_16BIT.bAllow16bitPercentEncoding = true;
        RFC3986_ABS_16BIT.bAllowInvalidPercentEncoding = false;

        StringBuilder sb = new StringBuilder("[]");
        for (int i=33; i<127; ++i) {
            if ((defaultCharTypeMap[i] & (B_FRAGMENT | B_RESERVED)) == 0 && i != '%') {
                sb.append((char) i);
            }
        }
        for (int i=161; i<255; ++i) {
            sb.append((char) i);
        }
        // The following characters are accepted in the relaxed mode. And NOT in the official RFC.
        // []"<>\^`{|}¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþ
        RFC3986_ABS_16BIT_LAX = new UriProfile();
        RFC3986_ABS_16BIT_LAX.bAllowRelativeUris = false;
        RFC3986_ABS_16BIT_LAX.bAllow16bitPercentEncoding = true;
        RFC3986_ABS_16BIT_LAX.bAllowInvalidPercentEncoding = true;
        RFC3986_ABS_16BIT_LAX.charTypeAddAndOr(sb.toString(), 0, B_PATH | B_QUERY | B_FRAGMENT);
        RFC3986_ABS_16BIT_LAX.charTypeAddAndOr("#", 0, B_FRAGMENT);
    }

}
