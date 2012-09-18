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
 * Custom URI parser/validation based on rfc3986. An URI is split into sub
 * components which are in turn validated. The overall reason for implementing
 * this class is the lack of support for %uxxxx encoding which is not part of
 * the specification but nevertheless needs to be validated since they are in
 * use. Other non standard characters are also used in the wild.
 *
 * Suitable for this package, but not yet suitable as an URI substitute (yet).
 *
 * @author nicl
 */
public class Uri implements Comparable<Uri> {

    /** Raw hier part. */
    protected String hierPartRaw;
    /** Raw scheme specific part, if present. */
    protected String schemeSpecificPartRaw;
    /** Raw authority components, if present. */
    protected String authorityRaw;
    /** Raw userinfo authority component, if present. */
    protected String userinfoRaw;
    /** Host authority component, if present. */
    protected String hostRaw;
    /** Raw port authority component, if present. */
    protected String portRaw;
    /** Raw path component. */
    protected String pathRaw;
    /** Raw query component, if present. */
    protected String queryRaw;
    /** Raw fragment component, if present. */
    protected String fragmentRaw;

    /** Scheme component, if present. */
    protected String scheme;
    /** Scheme specific part decoded. */
    protected String schemeSpecificPart;
    /** Authority components decoded, if present. */
    protected String authority;
    /** Userinfo authority component decoded, if present. */
    protected String userinfo;
    /** Host authority component decoded, if present. */
    protected String host;
    /** Port authority component, if present. */
    protected int port = -1;
    /** Path component decoded. */
    protected String path;
    /** Query component decoded, if present. */
    protected String query;
    /** Fragment component decoded, if present. */
    protected String fragment;

    /** Boolean indicating whether this URI is absolute. */
    protected boolean bAbsolute;

    /** Boolean indicating whether this URI is opaque. */
    protected boolean bOpaque;

    /**
     * Creates a URI by parsing the given string.
     * @param str The string to be parsed into a URI
     * @return The new URI
     * @throws IllegalArgumentException If the given string violates RFC 3986
     */
    public static Uri create(String str) throws IllegalArgumentException {
        try {
            return new Uri(str);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI", e);
        }
    }

    /**
     * Constructor for unit-testing.
     */
    protected Uri() {
    }

    /**
     * Constructs a URI by parsing the given string.
     * @param str The string to be parsed into a URI
     * @throws URISyntaxException If the given string violates RFC 3986
     */
    public Uri(String str) throws URISyntaxException {
        /*
        try {
            RandomAccessFile raf = new RandomAccessFile("uris.txt", "rw");
            raf.seek(raf.length());
            raf.write(str.getBytes("ISO8859-1"));
            raf.write("\r\n".getBytes());
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        int idx = indexOf(B_GEN_DELIMS, str, 0);
        if (idx != -1 && str.charAt(idx) == ':') {
            scheme = str.substring(0, idx++);
            validate_absoluteUri(str, idx);
            bAbsolute = true;
            if (schemeSpecificPart.length() > 0
                    && !schemeSpecificPart.startsWith("/")) {
                bOpaque = true;
            }
        } else {
            validate_relativeUri(str);
        }
    }

    /**
     *
     * @param bw_and
     * @param str
     * @param pos
     * @return
     * @throws URISyntaxException
     */
    protected int indexOf(int bw_and, String str, int pos) throws URISyntaxException {
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
                throw new URISyntaxException(str, "Invalid URI character '" + c + "'");
            }
        }
        return -1;
    }

    protected void validate_absoluteUri(String uriStr, int uIdx) throws URISyntaxException {
        char c;
        // Scheme validation.
        if (scheme.length() > 0) {
            scheme = scheme.toLowerCase();
            int pos = 0;
            int limit = scheme.length();
            while (pos < limit) {
                c = scheme.charAt(pos);
                // indexOf has ensured that the scheme does not have character values > 255.
                if (pos == 0 && ((charTypeMap[c] & B_SCHEME_FIRST) == 0)) {
                    throw new URISyntaxException(scheme, "Invalid URI scheme component");
                } else if ((charTypeMap[c] & B_SCHEME_FOLLOW) == 0) {
                    throw new URISyntaxException(scheme, "Invalid URI scheme component");
                }
                ++pos;
            }
        } else {
            throw new URISyntaxException(scheme, "Empty URI scheme component");
        }
        // QueryRaw.
        int qfIdx = uriStr.length();
        int qIdx = uriStr.indexOf('?', uIdx);
        int fIdx;
        if (qIdx != -1) {
            qfIdx = qIdx++;
            fIdx = uriStr.indexOf('#', qIdx);
            if (fIdx != -1) {
                queryRaw = uriStr.substring(qIdx, fIdx);
            } else {
                queryRaw = uriStr.substring(qIdx);
            }
        } else {
            fIdx = uriStr.indexOf('#', uIdx);
        }
        // FragmentRaw.
        if (fIdx != -1) {
            if (fIdx < qfIdx) {
                qfIdx = fIdx;
            }
            ++fIdx;
            fragmentRaw = uriStr.substring(fIdx);
        }
        // HierPartRaw / AuthorityRaw / PathRaw.
        hierPartRaw = uriStr.substring(uIdx, qfIdx);
        if (hierPartRaw.startsWith("//")) {
            int pIdx = hierPartRaw.indexOf('/', 2);
            if (pIdx != -1) {
                authorityRaw = hierPartRaw.substring(2, pIdx);
                pathRaw = hierPartRaw.substring(pIdx);
            } else {
                authorityRaw = hierPartRaw.substring(2);
                pathRaw = "";
            }
        } else {
            pathRaw = hierPartRaw;
        }
        // SchemeSpecificPartRaw.
        if (queryRaw != null) {
            schemeSpecificPartRaw = hierPartRaw + '?' + queryRaw;
        } else {
            schemeSpecificPartRaw = hierPartRaw;
        }
        // Authority
        if (authorityRaw != null) {
            // UserinfoRaw.
            int aIdx = authorityRaw.indexOf('@');
            if (aIdx != -1) {
                userinfoRaw = authorityRaw.substring(0, aIdx++);
            } else {
                aIdx = 0;
            }
            // Host / PortRaw.
            if (aIdx < authorityRaw.length() && authorityRaw.charAt(aIdx) == '[') {
                // ipv6 or new address type.
                int bIdx = authorityRaw.indexOf(']', aIdx);
                if (bIdx != -1) {
                    ++bIdx;
                    hostRaw = authorityRaw.substring(aIdx, bIdx);
                    host = hostRaw;
                    if (bIdx < authorityRaw.length()) {
                        if (authorityRaw.charAt(bIdx++) == ':') {
                            portRaw = authorityRaw.substring(bIdx);
                        } else {
                            throw new URISyntaxException(authorityRaw, "Invalid URI authority/port component - expected a ':'");
                        }
                    }
                } else {
                    throw new URISyntaxException(authorityRaw, "Invalid URI authority/host component - missing ']'");
                }
            } else {
                // ipv4 or hostname.
                int pIdx = authorityRaw.indexOf(':', aIdx);
                if (pIdx != -1) {
                    hostRaw = authorityRaw.substring(aIdx, pIdx++);
                    portRaw = authorityRaw.substring(pIdx);
                } else {
                    hostRaw = authorityRaw.substring(aIdx);
                    //portRaw = null;
                }
                host = validecode(B_REGNAME, "host", hostRaw);
            }
            // Userinfo validation.
            authority = "";
            if (userinfoRaw != null) {
                userinfo = validecode(B_USERINFO, "userinfo", userinfoRaw);
                authority += userinfo + '@';
            }
            authority += host;
            // Port validation.
            if (portRaw != null) {
                if (portRaw.length() > 0) {
                    try {
                        port = Integer.parseInt(portRaw);
                        if (port < 1 || port > 65535) {
                            throw new URISyntaxException(portRaw, "Invalid URI port component");
                        }
                        authority += ':' + portRaw;
                    } catch (NumberFormatException e) {
                        throw new URISyntaxException(portRaw, "Invalid URI port component");
                    }
                } else {
                    authority += ':';
                }
            }
            schemeSpecificPart = "//" + authority;
            // Path validation (path-abempty).
            if (pathRaw.length() > 0) {
                if (!pathRaw.startsWith("/")) {
                    throw new URISyntaxException(pathRaw, "Invalid URI path component - must begin with '/' when authority is present");
                }
                path = validecode(B_PATH, "path", pathRaw);
            } else {
                path = "";
            }
            schemeSpecificPart += path;
        } else {
            // Path validation (path-absolute / path-rootless / path-empty).
            if (pathRaw.length() > 0) {
                path = validecode(B_PATH, "path", pathRaw);
            } else {
                path = "";
            }
            schemeSpecificPart = path;
        }
        // Query validation.
        if (queryRaw != null) {
            query = validecode(B_QUERY, "query", queryRaw);
            schemeSpecificPart += '?' + query;
        }
        // Fragment validation.
        if (fragmentRaw != null) {
            fragment = validecode(B_FRAGMENT, "fragment", fragmentRaw);
        }
    }

    protected void validate_relativeUri(String uriStr) throws URISyntaxException {
    }

    protected String validecode(int bw_and, String componentName, String str) throws URISyntaxException {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        int limit = str.length();
        char c;
        int decode;
        int tmpC;
        char decodedC;
        while (pos < limit) {
            c = str.charAt(pos++);
            if (c < 256) {
                if ((charTypeMap[c] & bw_and) == 0) {
                    if (c == '%') {
                        if (pos < limit) {
                            c = str.charAt(pos);
                            if (c == 'u' || c == 'U') {
                                ++pos;
                                decode = 4;
                            } else {
                                decode = 2;
                            }
                            decodedC = 0;
                            while (decode > 0 && pos < limit) {
                                c = str.charAt(pos++);
                                decodedC <<= 4;
                                if (c < 256) {
                                    tmpC = asciiHexTab[c];
                                    if (tmpC != -1) {
                                        decodedC |= tmpC;
                                    } else {
                                        throw new URISyntaxException(str, "Invalid URI " + componentName + " component - incomplete percent encoding character '" + c + "'");
                                    }
                                } else {
                                    throw new URISyntaxException(str, "Invalid URI " + componentName + " component - incomplete percent encoding character '" + c + "'");
                                }
                                --decode;
                            }
                            sb.append((char) decodedC);
                        } else {
                            throw new URISyntaxException(str, "Invalid URI " + componentName + " component - incomplete percent encoding");
                        }
                    } else {
                        throw new URISyntaxException(str, "Invalid URI " + componentName + " component - invalid character '" + c + "'");
                    }
                } else {
                    sb.append(c);
                }
            } else {
                throw new URISyntaxException(str, "Invalid URI " + componentName + " component - invalid character '" + c + "'");
            }
        }
        return sb.toString();
    }

    public String getScheme() {
        return scheme;
    }

    public String getSchemeSpecificPart() {
        return schemeSpecificPart;
    }

    public String getAuthority() {
        return authority;
    }

    public String getUserInfo() {
        return userinfo;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getFragment() {
        return fragment;
    }

    public String getRawSchemeSpecificPart() {
        return schemeSpecificPartRaw;
    }

    public String getRawAuthority() {
        return authorityRaw;
    }

    public String getRawUserInfo() {
        return userinfoRaw;
    }

    public String getRawPath() {
        return pathRaw;
    }

    public String getRawQuery() {
        return queryRaw;
    }

    public String getRawFragment() {
        return fragmentRaw;
    }

    public boolean isAbsolute() {
        return bAbsolute;
    }

    public boolean isOpaque() {
        return bOpaque;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        if (userinfo != null || hostRaw != null || port != -1) {
            sb.append("//");
            if (userinfo != null) {
                sb.append(userinfoRaw);
                sb.append('@');
            }
            sb.append(hostRaw);
            if (portRaw != null) {
                sb.append(':');
                sb.append(portRaw);
            }
        }
        sb.append(pathRaw);
        if (query != null) {
            sb.append('?');
            sb.append(queryRaw);
        }
        if (fragment != null) {
            sb.append('#');
            sb.append(fragmentRaw);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Uri)) {
            return false;
        }
        Uri uriObj = (Uri)obj;
        if (scheme != null) {
            if (!scheme.equals(uriObj.scheme)) {
                return false;
            }
        } else if (uriObj.scheme != null) {
            return false;
        }
        if (userinfo != null) {
            if (!userinfo.equals(uriObj.userinfo)) {
                return false;
            }
        } else if (uriObj.userinfo != null) {
            return false;
        }
        if (host != null) {
            if (!host.equals(uriObj.host)) {
                return false;
            }
        } else if (uriObj.host != null) {
            return false;
        }
        if (port != uriObj.port) {
            return false;
        }
        if (path != null) {
            if (!path.equals(uriObj.path)) {
                return false;
            }
        } else if (uriObj.path != null) {
            return false;
        }
        if (query != null) {
            if (!query.equals(uriObj.query)) {
                return false;
            }
        } else if (uriObj.query != null) {
            return false;
        }
        if (fragment != null) {
            if (!fragment.equals(uriObj.fragment)) {
                return false;
            }
        } else if (uriObj.fragment != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (scheme != null) {
            hashCode ^= scheme.hashCode();
        }
        if (schemeSpecificPart != null) {
            hashCode ^= schemeSpecificPart.hashCode();
        }
        if (fragment != null) {
            hashCode ^= fragment.hashCode();
        }
        return hashCode;
    }

    @Override
    public int compareTo(Uri uri) {
        if (uri == null) {
            return 1;
        }
        int res = 0;
        if (scheme != null) {
            if (uri.scheme != null) {
                res = scheme.compareTo(uri.scheme);
                if (res != 0) {
                    return res;
                }
            } else {
                return 1;
            }
        } else if (uri.scheme != null) {
            return -1;
        }
        if (schemeSpecificPart != null) {
            if (uri.schemeSpecificPart != null) {
                res = schemeSpecificPart.compareTo(uri.schemeSpecificPart);
                if (res != 0) {
                    return res;
                }
            } else {
                return 1;
            }
        } else if (uri.schemeSpecificPart != null) {
            return -1;
        }
        if (fragment != null) {
            if (uri.fragment != null) {
                res = fragment.compareTo(uri.fragment);
                if (res != 0) {
                    return res;
                }
            } else {
                return 1;
            }
        } else if (uri.fragment != null) {
            return -1;
        }
        return res;
    }

    public static int[] asciiHexTab = new int[256];

    public static char[] hexTab = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

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

    public static int[] charTypeMap = new int[256];

    protected static void charTypeAddAndOr(String chars, int bw_and, int bw_or) {
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

    public static final int B_ALPHAS = 1 << 0;
    public static final int B_DIGITS = 1 << 1;
    public static final int B_SCHEME_FIRST = 1 << 2;
    public static final int B_SCHEME_FOLLOW = 1 << 3;
    public static final int B_UNRESERVED = 1 << 4;
    public static final int B_GEN_DELIMS = 1 << 5;
    public static final int B_SUB_DELIMS = 1 << 6;
    public static final int B_RESERVED = 1 << 7;
    public static final int B_PCHAR = 1 << 8;
    public static final int B_USERINFO = 1 << 9;
    public static final int B_REGNAME = 1 << 10;
    public static final int B_SEGMENT = 1 << 11;
    public static final int B_SEGMENT_NZ = 1 << 12;
    public static final int B_SEGMENT_NZ_NC = 1 << 13;
    public static final int B_PATH = 1 << 14;
    public static final int B_QUERY = 1 << 15;
    public static final int B_FRAGMENT = 1 << 16;

    static {
        // Alphas.
        String alphas = "abcdefghijklmnopqrstuvwxyz";
        charTypeAddAndOr(alphas, 0, B_ALPHAS);
        charTypeAddAndOr(alphas.toUpperCase(), 0, B_ALPHAS);

        // Digits.
        String digits = "1234567890";
        charTypeAddAndOr(digits, 0, B_DIGITS);

        // scheme first/follow.
        // scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
        String scheme = "+-.";
        charTypeAddAndOr(null, B_ALPHAS, B_SCHEME_FIRST | B_SCHEME_FOLLOW);
        charTypeAddAndOr(scheme, B_DIGITS, B_SCHEME_FOLLOW);

        // unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
        String unreserved = "-._~";
        charTypeAddAndOr(unreserved, B_ALPHAS | B_DIGITS, B_UNRESERVED);

        // gen-delims (reserved)
        String genDelims = ":/?#[]@";
        charTypeAddAndOr(genDelims, 0, B_GEN_DELIMS | B_RESERVED);

        // sub-delims (reserved)
        String subDelims  = "!$&'()*+,;=";
        charTypeAddAndOr(subDelims, 0, B_SUB_DELIMS | B_RESERVED);

        // pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
        String pchar = ":@";
        charTypeAddAndOr(pchar, B_UNRESERVED | B_SUB_DELIMS, B_PCHAR);

        // userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
        String userinfo = ":";
        charTypeAddAndOr(userinfo, B_UNRESERVED | B_SUB_DELIMS, B_USERINFO);

        // reg-name = *( unreserved / pct-encoded / sub-delims )
        charTypeAddAndOr(null, B_UNRESERVED | B_SUB_DELIMS, B_REGNAME);

        // segment = *pchar
        // segment-nz = 1*pchar
        charTypeAddAndOr(null, B_PCHAR, B_SEGMENT);
        charTypeAddAndOr(null, B_PCHAR, B_SEGMENT_NZ);

        // segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )
        // non-zero-length segment without any colon ":"
        String segment_nz_nc = "@";
        charTypeAddAndOr(segment_nz_nc, B_UNRESERVED | B_SUB_DELIMS, B_SEGMENT_NZ_NC);

        // path
        String path = "/";
        charTypeAddAndOr(path, B_PCHAR, B_PATH);

        // query = *( pchar / "/" / "?" )
        String query =  "/?";
        charTypeAddAndOr(query, B_PCHAR, B_QUERY);

        // fragment = *( pchar / "/" / "?" )
        String fragment = "/?";
        charTypeAddAndOr(fragment, B_PCHAR, B_FRAGMENT);
    }

}
