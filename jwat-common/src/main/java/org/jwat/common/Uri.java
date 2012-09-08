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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom URI parser/validation based on rfc3986. The parser uses a series
 * of regular expressions to split an URI into sub parts with can then be
 * validated. The overall reason for implementing this class is the lack of
 * support for %uxxxx encoding which is not part of the specification but
 * nevertheless needs to be validated since they are used.
 *
 * Suitable for this package, but not yet suitable as an URI substitute.
 *
 * @author nicl
 */
public class Uri implements Comparable<Uri> {

    protected static String reUri =
            "^(?:([^:/?#]+):)?"
            + "(?://([^/?#]*))?"
            + "([^?#]*)"
            + "(?:\\?([^#]*))?"
            + "(?:#(.*))?";
    protected static Pattern patternUri = Pattern.compile(reUri);

    protected static String reScheme = "^[a-z0-9+.-]+";
    protected static Pattern patternScheme = Pattern.compile(reScheme);

    protected static String reAuthority =
            "^(?:((?:[a-zA-Z0-9-._~!$&'()*+,;=:]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*)@)?"
            + "((?:\\[[a-zA-Z0-9.:]+\\])|(?:[a-zA-Z0-9-._~!$&'()*+,;=]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})+)"
            + "(?::(\\d*))?";
    protected static Pattern patternAuthority = Pattern.compile(reAuthority);

    protected static String rePathAuth = "^(?:/(?:[a-zA-Z0-9-._~!$&'()*+,;=:@/]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*)?";
    protected static Pattern patternPathAuth = Pattern.compile(rePathAuth);

    protected static String rePathNoAut = "^(?:/?(?:[a-zA-Z0-9-._~!$&'()*+,;=:@]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})+(?:[a-zA-Z0-9-._~!$&'()*+,;=:@/]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*)?";
    protected static Pattern patternPathNoAuth = Pattern.compile(rePathNoAut);

    protected static String reQuery = "^(?:[a-zA-Z0-9-._~!$&'()*+,;=:/?@]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*";
    protected static Pattern patternQuery = Pattern.compile(reQuery);

    protected static String reFragment = "^(?:[a-zA-Z0-9-._~!$&'()*+,;=:/?@]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*";
    protected static Pattern patternFragment = Pattern.compile(reFragment);

    //protected String uriRaw;
    //protected String uri;

    protected String schemeSpecificPartRaw;
    protected String authorityRaw;
    protected String userinfoRaw;
    protected String pathRaw;
    protected String queryRaw;
    protected String fragmentRaw;

    protected String scheme;
    protected String schemeSpecificPart;
    protected String authority;
    protected String userinfo;
    protected String host;
    protected int port = -1;
    protected String path;
    protected String query;
    protected String fragment;

    public static Uri create(String str) throws IllegalArgumentException {
        try {
            return new Uri(str);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URI", e);
        }
    }

    public Uri(String str) throws URISyntaxException {
        Matcher matcher = patternUri.matcher(str);
        if (matcher.matches()) {
            validate(matcher);
        } else {
            throw new URISyntaxException(str, "Invalid URI composition");
        }
    }

    protected void validate(Matcher matcher) throws URISyntaxException {
        scheme = matcher.group(1);
        authorityRaw = matcher.group(2);
        pathRaw = matcher.group(3);
        queryRaw = matcher.group(4);
        fragmentRaw = matcher.group(5);
        if (scheme != null && scheme.length() > 0) {
            scheme = scheme.toLowerCase();
            matcher = patternScheme.matcher(scheme);
            if (!matcher.matches()) {
                throw new URISyntaxException(scheme, "Invalid URI scheme component");
            }
        }
        if (authorityRaw != null && authorityRaw.length() > 0) {
            matcher = patternAuthority.matcher(authorityRaw);
            if (!matcher.matches()) {
                throw new URISyntaxException(authorityRaw, "Invalid URI authority component");
            }
            authority = "//";
            userinfoRaw = matcher.group(1);
            host = matcher.group(2);
            String tmpPort = matcher.group(3);
            if (userinfoRaw != null && userinfoRaw.length() > 0) {
                userinfo = decode(userinfoRaw);
                authority += userinfo + '@';
            }
            authority += host;
            if (tmpPort != null && tmpPort.length() > 0) {
                try {
                    port = Integer.parseInt(tmpPort);
                    if (port < 1 || port > 65535) {
                        throw new URISyntaxException(tmpPort, "Invalid URI port component");
                    }
                    authority += ':' + tmpPort;
                } catch (NumberFormatException e) {
                    throw new URISyntaxException(tmpPort, "Invalid URI port component");
                }
            }
            schemeSpecificPart = authority;
            if (pathRaw != null && pathRaw.length() > 0) {
                matcher = patternPathAuth.matcher(pathRaw);
                if (!matcher.matches()) {
                    throw new URISyntaxException(pathRaw, "Invalid URI path component");
                }
                path = decode(pathRaw);
                schemeSpecificPart += path;
            }
        } else {
            if (pathRaw != null && pathRaw.length() > 0) {
                matcher = patternPathNoAuth.matcher(pathRaw);
                if (!matcher.matches()) {
                    throw new URISyntaxException(pathRaw, "Invalid URI path component");
                }
                schemeSpecificPartRaw = pathRaw;
                path = decode(pathRaw);
                schemeSpecificPart = path;
            }
        }
        if (queryRaw != null && queryRaw.length() > 0) {
            matcher = patternQuery.matcher(queryRaw);
            if (!matcher.matches()) {
                throw new URISyntaxException(queryRaw, "Invalid URI query component");
            }
            query = decode(queryRaw);
            schemeSpecificPart += query;
        }
        if (fragmentRaw != null && fragmentRaw.length() > 0) {
            matcher = patternFragment.matcher(fragmentRaw);
            if (!matcher.matches()) {
                throw new URISyntaxException(fragmentRaw, "Invalid URI fragment component");
            }
            fragment = decode(fragmentRaw);
        }
    }

    protected String decode(String str) {
        return str;
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        if (userinfo != null || host != null || port != -1) {
            sb.append("//");
            if (userinfo != null) {
                sb.append(userinfo);
                sb.append('@');
            }
            if (host != null) {
                sb.append(host);
            }
            if (port != -1) {
                sb.append(':');
                sb.append(port);
            }
        }
        if (path != null) {
            sb.append(path);
        }
        if (query != null) {
            sb.append('?');
            sb.append(query);
        }
        if (fragment != null) {
            sb.append('#');
            sb.append(fragment);
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

}
