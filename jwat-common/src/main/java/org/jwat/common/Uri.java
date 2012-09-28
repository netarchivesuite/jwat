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
 * This implementation is not a complete substitute for the JDK version.
 * It does not yet implement the following methods.
 * - normalize()
 * - parseServerAuthority()
 * - relativize(URI uri)
 * - resolve(String str)
 * - resolve(URI uri)
 * - toASCIIString()
 *
 * @author nicl
 */
public class Uri implements Comparable<Uri> {

    /** Raw hier-part part ("//" + authority + path). */
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

    /** Profile used to parse URI. */
    protected UriProfile uriProfile = UriProfile.RFC3986;

    /**
     * Constructor for unit-testing.
     */
    protected Uri() {
    }

    /**
     * Creates a URI by parsing the given string using the default RFC3986
     * profile.
     * @param str The string to be parsed into a URI
     * @return The new URI
     * @throws IllegalArgumentException If the given string violates RFC 3986
     */
    public static Uri create(String str) throws IllegalArgumentException {
        try {
            return new Uri(str, UriProfile.RFC3986);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI", e);
        }
    }

    /**
     * Creates a URI by parsing the given string using the requested profile.
     * @param str The string to be parsed into a URI
     * @return The new URI
     * @throws IllegalArgumentException If the given string violates the profile
     */
    public static Uri create(String str, UriProfile uriProfile) throws IllegalArgumentException {
        try {
            return new Uri(str, uriProfile);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI", e);
        }
    }

    /**
     * Constructs a URI by parsing the given string using the default RFC3986
     * profile.
     * @param str The string to be parsed into a URI
     * @throws URISyntaxException If the given string violates RFC 3986
     */
    public Uri(String str) throws URISyntaxException {
        this(str, UriProfile.RFC3986);
    }

    /**
     * Constructs a URI by parsing the given string using the requested
     * profile.
     * @param str The string to be parsed into a URI
     * @throws URISyntaxException If the given string violates the profile
     */
    public Uri(String str, UriProfile uriProfile) throws URISyntaxException {
        this.uriProfile = uriProfile;
        int idx = uriProfile.indexOf(UriProfile.B_GEN_DELIMS, str, 0);
        if (idx != -1 && str.charAt(idx) == ':') {
            // Try validating as an absolute URI.
            scheme = str.substring(0, idx++);
            validate_absoluteUri(str, idx);
            bAbsolute = true;
            if (schemeSpecificPart.length() > 0
                    && !schemeSpecificPart.startsWith("/")) {
                bOpaque = true;
            }
        } else {
            if (uriProfile.bAllowRelativeUris) {
                validate_relativeUri(str, 0);
            } else {
                throw new URISyntaxException(str, "Invalid URI - relative URIs not allowed");
            }
        }
    }

    /**
     * Parse and validate an absolute URI string.
     * @param uriStr URI string
     * @param uIdx index in string after scheme colon character
     * @throws URISyntaxException if the URI is invalid in some way
     */
    protected void validate_absoluteUri(String uriStr, int uIdx) throws URISyntaxException {
        // Scheme validation.
        if (scheme.length() > 0) {
            scheme = scheme.toLowerCase();
            uriProfile.validate_first_follow(scheme, UriProfile.B_SCHEME_FIRST, UriProfile.B_SCHEME_FOLLOW);
        } else {
            throw new URISyntaxException(scheme, "Empty URI scheme component");
        }
        validate_relativeUri(uriStr, uIdx);
    }

    /**
     * Parse and validate a relative URI string.
     * This method is also used by the absolute parser after a scheme has
     * been identified.
     * @param uriStr URI string
     * @param uIdx index in string to start parsing from
     * @throws URISyntaxException if the URI is invalid in some way
     */
    protected void validate_relativeUri(String uriStr, int uIdx) throws URISyntaxException {
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
                host = uriProfile.validate_decode(UriProfile.B_REGNAME, "host", hostRaw);
            }
            // Userinfo validation.
            authority = "";
            if (userinfoRaw != null) {
                userinfo = uriProfile.validate_decode(UriProfile.B_USERINFO, "userinfo", userinfoRaw);
                authority += userinfo + '@';
            }
            authority += host;
            // Port validation.
            if (portRaw != null) {
                if (portRaw.length() > 0) {
                    try {
                        port = Integer.parseInt(portRaw);
                        if (port < 1 || port > 65535) {
                            throw new URISyntaxException(portRaw, "Invalid URI port component - port is not within range [1-65535]");
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
                path = uriProfile.validate_decode(UriProfile.B_PATH, "path", pathRaw);
            } else {
                path = "";
            }
            schemeSpecificPart += path;
        } else {
            // Path validation (path-absolute / path-rootless / path-empty).
            if (pathRaw.length() > 0) {
                path = uriProfile.validate_decode(UriProfile.B_PATH, "path", pathRaw);
            } else {
                path = "";
            }
            schemeSpecificPart = path;
        }
        // Query validation.
        if (queryRaw != null) {
            query = uriProfile.validate_decode(UriProfile.B_QUERY, "query", queryRaw);
            schemeSpecificPart += '?' + query;
        }
        // Fragment validation.
        if (fragmentRaw != null) {
            fragment = uriProfile.validate_decode(UriProfile.B_FRAGMENT, "fragment", fragmentRaw);
        }
    }

    /**
     * Tells whether or not this URI is absolute.
     * A URI is absolute if, and only if, it has a scheme component.
     * @return true if, and only if, this URI is absolute
     */
    public boolean isAbsolute() {
        return bAbsolute;
    }

    /**
     * Tells whether or not this URI is opaque.
     * A URI is opaque if, and only if, it is absolute and its scheme-specific
     * part does not begin with a slash character ('/'). An opaque URI has a
     * scheme, a scheme-specific part, and possibly a fragment; all other
     * components are undefined.
     * @return true if, and only if, this URI is opaque
     */
    public boolean isOpaque() {
        return bOpaque;
    }

    /**
     * Returns the scheme component of this URI.
     * The scheme component of a URI, if defined, only contains characters in
     * the alphanum category and in the string "-.+". A scheme always starts
     * with an alpha character.
     * The scheme component of a URI cannot contain escaped octets, hence this
     * method does not perform any decoding.
     * @return The scheme component of this URI, or null if the scheme is
     * undefined
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the raw scheme-specific part of this URI.
     * The scheme-specific part is never undefined, though it may be empty.
     * The scheme-specific part of a URI only contains legal URI characters.
     * @return The raw scheme-specific part of this URI (never null)
     */
    public String getRawSchemeSpecificPart() {
        return schemeSpecificPartRaw;
    }

    /**
     * Returns the decoded scheme-specific part of this URI.
     * The string returned by this method is equal to that returned by the
     * getRawSchemeSpecificPart method except that all sequences of escaped
     * octets are decoded.
     * @return The decoded scheme-specific part of this URI (never null)
     */
    public String getSchemeSpecificPart() {
        return schemeSpecificPart;
    }

    /**
     * Returns the raw authority component of this URI.
     * The authority component of a URI, if defined, only contains the
     * commercial-at character ('@') and characters in the unreserved, punct,
     * escaped, and other categories. If the authority is server-based then it
     * is further constrained to have valid user-information, host, and port
     * components.
     * @return The raw authority component of this URI, or null if the authority
     * is undefined
     */
    public String getRawAuthority() {
        return authorityRaw;
    }

    /**
     * Returns the decoded authority component of this URI.
     * The string returned by this method is equal to that returned by the
     * getRawAuthority method except that all sequences of escaped octets are
     * decoded.
     * @return The decoded authority component of this URI, or null if the
     * authority is undefined
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Returns the raw user-information component of this URI.
     * The user-information component of a URI, if defined, only contains
     * characters in the unreserved, punct, escaped, and other categories.
     * @return The raw user-information component of this URI, or null if the
     * user information is undefined
     */
    public String getRawUserInfo() {
        return userinfoRaw;
    }

    /**
     * Returns the decoded user-information component of this URI.
     * The string returned by this method is equal to that returned by the
     * getRawUserInfo method except that all sequences of escaped octets are
     * decoded.
     * @return The decoded user-information component of this URI, or null if
     * the user information is undefined
     */
    public String getUserInfo() {
        return userinfo;
    }

    /**
     * Returns the host component of this URI.
     * The host component of a URI, if defined, will have one of the following forms:
     * A domain name consisting of one or more labels separated by period characters ('.'), optionally followed by a period character. Each label consists of alphanum characters as well as hyphen characters ('-'), though hyphens never occur as the first or last characters in a label. The rightmost label of a domain name consisting of two or more labels, begins with an alpha character.
     * A dotted-quad IPv4 address of the form digit+.digit+.digit+.digit+, where no digit sequence is longer than three characters and no sequence has a value larger than 255.
     * An IPv6 address enclosed in square brackets ('[' and ']') and consisting of hexadecimal digits, colon characters (':'), and possibly an embedded IPv4 address. The full syntax of IPv6 addresses is specified in RFC 2373: IPv6 Addressing Architecture.
     * The host component of a URI cannot contain escaped octets, hence this method does not perform any decoding.
     * @return The host component of this URI, or null if the host is undefined
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port number of this URI.
     * The port component of a URI, if defined, is a non-negative integer.
     * @return The port component of this URI, or -1 if the port is undefined
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the raw path component of this URI.
     * The path component of a URI, if defined, only contains the slash
     * character ('/'), the commercial-at character ('@'), and characters in
     * the unreserved, punct, escaped, and other categories.
     * @return The path component of this URI, or null if the path is undefined
     */
    public String getRawPath() {
        return pathRaw;
    }

    /**
     * Returns the decoded path component of this URI.
     * The string returned by this method is equal to that returned by the
     * getRawPath method except that all sequences of escaped octets are
     * decoded.
     * @return The decoded path component of this URI, or null if the path is
     * undefined
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the raw query component of this URI.
     * The query component of a URI, if defined, only contains legal URI
     * characters.
     * @return The raw query component of this URI, or null if the query is
     * undefined
     */
    public String getRawQuery() {
        return queryRaw;
    }

    /**
     * Returns the decoded query component of this URI.
     * The string returned by this method is equal to that returned by the
     * getRawQuery method except that all sequences of escaped octets are
     * decoded.
     * @return The decoded query component of this URI, or null if the query is
     * undefined
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the raw fragment component of this URI.
     * The fragment component of a URI, if defined, only contains legal URI
     * characters.
     * @return The raw fragment component of this URI, or null if the fragment
     * is undefined
     */
    public String getRawFragment() {
        return fragmentRaw;
    }

    /**
     * Returns the decoded fragment component of this URI.
     * The string returned by this method is equal to that returned by the
     * getRawFragment method except that all sequences of escaped octets are
     * decoded.
     * @return The decoded fragment component of this URI, or null if the
     * fragment is undefined
     */
    public String getFragment() {
        return fragment;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        if (hostRaw != null) {
            sb.append("//");
            if (userinfoRaw != null) {
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
        if (queryRaw != null) {
            sb.append('?');
            sb.append(queryRaw);
        }
        if (fragmentRaw != null) {
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
        if (!path.equals(uriObj.path)) {
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
        hashCode ^= schemeSpecificPart.hashCode();
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
        res = schemeSpecificPart.compareTo(uri.schemeSpecificPart);
        if (res != 0) {
            return res;
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

}
