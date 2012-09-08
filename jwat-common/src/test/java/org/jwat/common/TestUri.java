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

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestUri {

    @Test
    public void test_uri() {
        String[] uris = {
                //"http://",
                "http://www.a1ie.com/news/index_0_7.html?tags=%u4E8B%u4EF6%u8425%u9500#hello_world",
                "http://www.a1ie.com",
                //"Last-Modified:",
                "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88",
                "ftp://ftp.is.co.za/rfc/rfc1808.txt",
                "http://www.ietf.org/rfc/rfc2396.txt",
                "ldap://[2001:db8::7]/c=GB?objectClass?one",
                "mailto:John.Doe@example.com",
                "news:comp.infosystems.www.servers.unix",
                "tel:+1-816-555-1212",
                "telnet://192.0.2.16:80/",
                "urn:oasis:names:specification:docbook:dtd:xml:4.1.2"
        };
        for (int i=0; i<uris.length; ++i) {
            try {
                Uri uri = Uri.create(uris[i]);
                Assert.assertNotNull(uri);
                System.out.println(uri);
                System.out.println("--------");
                if (uri != null) {
                    System.out.println("    Scheme: " + uri.scheme);
                    System.out.println("SchemeSpec: " + uri.schemeSpecificPart);
                    System.out.println(" Authority: " + uri.authority);
                    System.out.println("  Userinfo: " + uri.userinfo);
                    System.out.println("      Host: " + uri.host);
                    System.out.println("      Port: " + uri.port);
                    System.out.println("      Path: " + uri.path);
                    System.out.println("     Query: " + uri.query);
                    System.out.println("  Fragment: " + uri.fragment);
                }
                Assert.assertEquals(uris[i], uri.toString());

                try {
                    URI jdkuri = new URI(uris[i]);
                    System.out.println("      Scheme: " + jdkuri.getScheme());
                    System.out.println("  SchemeSpec: " + jdkuri.getSchemeSpecificPart());
                    System.out.println("   Authority: " + jdkuri.getAuthority());
                    System.out.println("    Userinfo: " + jdkuri.getUserInfo());
                    System.out.println("        Host: " + jdkuri.getHost());
                    System.out.println("        Port: " + jdkuri.getPort());
                    System.out.println("        Path: " + jdkuri.getPath());
                    System.out.println("       Query: " + jdkuri.getQuery());
                    System.out.println("    Fragment: " + jdkuri.getFragment());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                System.out.println("--------");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Assert.fail("Unexpected exception!");
            }
        }
    }

}
