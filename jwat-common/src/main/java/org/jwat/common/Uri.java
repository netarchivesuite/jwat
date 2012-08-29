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
 * @author nicl
 */
public class Uri {

    protected static String reUri =
            "^(([^:/?#]+):)?"
            + "(//([^/?#]*))?"
            + "([^?#]*)"
            + "(\\?([^#]*))?"
            + "(#(.*))?";
    protected static Pattern patternUri = Pattern.compile(reUri);

    protected static String reScheme = "[a-z0-9+.-]+";
    protected static Pattern patternScheme = Pattern.compile(reScheme);

    protected static String reAuthority =
            "(?:((?:[a-zA-Z0-9-._~!$&'()*+,;=:]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*)@)?"
            + "((?:\\[[a-zA-Z0-9.:]+\\])|(?:[a-zA-Z0-9-._~!$&'()*+,;=]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*)"
            + "(?::(\\d*))?";
    protected static Pattern patternAuthority = Pattern.compile(reAuthority);

    protected static String rePathAuth = "(?:/(?:[a-zA-Z0-9-._~!$&'()*+,;=:@/]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*)?";
    protected static Pattern patternPathAuth = Pattern.compile(rePathAuth);

    protected static String rePathNoAut = "(?:/?(?:[a-zA-Z0-9-._~!$&'()*+,;=:@]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})+(?:[a-zA-Z0-9-._~!$&'()*+,;=:@/]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*)?";
    protected static Pattern patternPathNoAuth = Pattern.compile(rePathNoAut);

    protected static String reQuery = "(?:[a-zA-Z0-9-._~!$&'()*+,;=:/?@]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*";
    protected static Pattern patternQuery = Pattern.compile(reQuery);

    protected static String reFragment = "(?:[a-zA-Z0-9-._~!$&'()*+,;=:/?@]|%[0-9a-fA-F]{2}|%u[0-9a-fA-F]{4})*";
    protected static Pattern patternFragment = Pattern.compile(reFragment);

    public String schemeFull;
    public String scheme;
    public String authorityFull;
    public String authority;
    public String userinfo;
    public String host;
    public String port;
    public String path;
    public String queryFull;
    public String query;
    public String fragmentFull;
    public String fragment;

    public static Uri create(String uriStr) throws URISyntaxException {
        Uri uri = null;
        Matcher matcher = patternUri.matcher(uriStr);
        if (matcher.matches()) {
            uri = new Uri();
            uri.validate(matcher);
        } else {
            throw new URISyntaxException(uriStr, "Invalid URI composition");
        }
        return uri;
    }

    protected void validate(Matcher matcher) throws URISyntaxException {
        schemeFull = matcher.group(1);
        scheme = matcher.group(2);
        authorityFull = matcher.group(3);
        authority = matcher.group(4);
        path = matcher.group(5);
        queryFull = matcher.group(6);
        query = matcher.group(7);
        fragmentFull = matcher.group(8);
        fragment = matcher.group(9);
        if (scheme != null && scheme.length() > 0) {
            matcher = patternScheme.matcher(scheme);
            if (!matcher.matches()) {
                throw new URISyntaxException(scheme, "Invalid URI scheme part");
            }
        }
        if (authority != null && authority.length() > 0) {
            matcher = patternAuthority.matcher(authority);
            if (!matcher.matches()) {
                throw new URISyntaxException(authority, "Invalid URI authority part");
            }
            userinfo = matcher.group(1);
            host = matcher.group(2);
            port = matcher.group(3);
            if (path != null && path.length() > 0) {
                matcher = patternPathAuth.matcher(path);
                if (!matcher.matches()) {
                    throw new URISyntaxException(path, "Invalid URI path part");
                }
            }
        } else {
            if (path != null && path.length() > 0) {
                matcher = patternPathNoAuth.matcher(path);
                if (!matcher.matches()) {
                    throw new URISyntaxException(path, "Invalid URI path part");
                }
            }
        }
        if (query != null && query.length() > 0) {
            matcher = patternQuery.matcher(query);
            if (!matcher.matches()) {
                throw new URISyntaxException(query, "Invalid URI query part");
            }
        }
        if (fragment != null && fragment.length() > 0) {
            matcher = patternFragment.matcher(fragment);
            if (!matcher.matches()) {
                throw new URISyntaxException(fragment, "Invalid URI fragment part");
            }
        }
    }

}
