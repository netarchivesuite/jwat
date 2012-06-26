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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test ContentType parser with legal and illegal content-types with and without
 * optional parameters.
 *
 * @author nicl
 */
@RunWith(JUnit4.class)
public class TestContentType {

    @Test
    public void test_contenttype() throws IOException {
        Object[][] cases;
        ContentType ct;
        String value;
        String str;

        /*
         * Test invalid Content-type/Media-type.
         */

        cases = new Object[][] {
                {false, null},
                {false, ""},
                {false, " "},
                {false, "  "},
                {false, "text"},
                {false, " text"},
                {false, "  text"},
                {false, "text;"},
                {false, " text;"},
                {false, "  text;"},
                {false, "text/"},
                {false, " text/"},
                {false, "  text/"},
                {false, "  text/  "},
                {false, "  text/\t"},
                {false, "text/;"},
                {false, " text/;"},
                {false, "  text/;"},
                {false, "  -"},
                {false, "  text-"},
                {false, "  text/:"},
                {false, "  text/:;"},
                {false, "  text/plain ;"},
                {false, " text/plain a"},
                {false, "   text/plain   a"},
                {false, "text/plain;a"},
                {false, "text/plain; a"},
                {false, "text/plain;  a"}
        };
        test_cases(cases);

        /*
         * Test simple Content-type/Media-type.
         */

        cases = new Object[][] {
                {true, "text/plain",
                    "text", "plain", null
                },
                {true, " text/plain ",
                    "text", "plain", null
                },
                {true, "   text/plain   ",
                    "text", "plain", null
                },
                {true, " text/plain \t",
                    "text", "plain", null
                },
                {true, "text/plain;",
                    "text", "plain", null
                },
                {true, " text/plain; ",
                    "text", "plain", null
                },
                {true, " text/plain;\t",
                    "text", "plain", null
                }
        };
        test_cases(cases);

        /*
         * Test invalid Parameters.
         */

        cases = new Object[][] {
                {false, "application/http;=request"},
                {false, "application/http;= request"},
                {false, "application/http;=  request"},
                {false, "application/http;msgtype =request"},
                {false, "application/http;msgtype= request"},
                {false, "application/http;msgtype=request a"},
                {false, "application/http;msgtype=request  a"},
                {false, "application/http;msgtype=request:"},
                {false, "application/http;msgtype=\"request"}
        };
        test_cases(cases);

        /*
         * Test Parameters.
         */

        cases = new Object[][] {
                {true, "application/http;msgtype=request",
                "application", "http", new Object[][] {
                        {"msgtype", "request"}
                }},
                {true, " application/http; msgtype=request ",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"}
                }},
                {true, "\tapplication/http; msgtype=request ",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"}
                }},
                {true, "  application/http;  msgtype=request  ",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"}
                }},
                {true, "application/http;msgtype=\"request\"",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"}
                }},
                {true, " application/http; msgtype=\"request\" ",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"}
                }},
                {true, "  application/http;  msgtype=\"request\"  ",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"}
                }},
                {true, " application/http; msgtype=\"request\"\t",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"}
                }},
                {true, "application/http;msgtype=request;charset=utf8 ",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"},
                        {"charset", "utf8"}
                }},
                {true, "application/http;msgtype=request;charset=utf8 ; ",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"},
                        {"charset", "utf8"}
                }},
                {true, "application/http;msgtype=request;charset=utf8\t; ",
                    "application", "http", new Object[][] {
                        {"msgtype", "request"},
                        {"charset", "utf8"}
                }}
        };
        test_cases(cases);

        ct = ContentType.parseContentType("application/http; msgtype=request; charset=utf8");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);
        value = ct.getParameter("charset");
        Assert.assertNotNull(value);
        Assert.assertEquals("utf8", value);

        str = ct.toString();
        Assert.assertEquals("application/http; msgtype=request; charset=utf8", str);
        str = ct.toStringShort();
        Assert.assertEquals("application/http", str);

        ct = ContentType.parseContentType("Application/Warc-Fields");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("warc-fields", ct.mediaType);

        str = ct.toString();
        Assert.assertEquals("application/warc-fields", str);

        ct.setParameter("Quote", ".oOo. \t .oOo.");

        str = ct.getParameter("qUOTE");
        Assert.assertEquals(".oOo. \t .oOo.", str);

        str = ct.toString();
        Assert.assertEquals("application/warc-fields; quote=\".oOo. \t .oOo.\"", str);

        str = ct.toStringShort();
        Assert.assertEquals("application/warc-fields", str);

        ct.setParameter(null, "utf-8");
        ct.setParameter("", "utf-8");
        ct.setParameter("charset", null);

        str = ct.getParameter(null);
        Assert.assertNull(str);
        str = ct.getParameter("");
        Assert.assertNull(str);
        str = ct.getParameter("charset");
        Assert.assertNull(str);

        ct.setParameter("one", "One");
        ct.setParameter("two", "Two");
        str = ct.getParameter("one");
        Assert.assertEquals("One", str);
        str = ct.getParameter("two");
        Assert.assertEquals("Two", str);

        ct = ContentType.parseContentType(" application/http; msgtype=req\\uest");
        Assert.assertNull(ct);

        ct = ContentType.parseContentType(" application/http; msgtype=\"req\\\"uest\"");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("req\"uest", value);

        ct = ContentType.parseContentType(" application/http; msgtype=\"request\\");
        Assert.assertNull(ct);

        /*
         * isTokenCharacter.
         */

        StringBuffer sb = new StringBuffer();

        for (int i=0; i<256; ++i) {
            if (i < 32 || ContentType.separators.indexOf(i) != -1) {
                Assert.assertFalse(ContentType.isTokenCharacter(i));
            } else {
                Assert.assertTrue(ContentType.isTokenCharacter(i));
                if (i != ' ' && i != '\t' && i != '"') {
                    sb.append((char) i);
                }
            }
        }
        Assert.assertTrue(ContentType.isTokenCharacter(256));

        /*
         * Quote.
         */

        Assert.assertFalse(ContentType.quote(null));
        Assert.assertFalse(ContentType.quote(""));
        Assert.assertFalse(ContentType.quote(sb.toString()));
        Assert.assertFalse(ContentType.quote(sb.toString() + "\u0100"));
        Assert.assertTrue(ContentType.quote(sb.toString() + " "));
        Assert.assertTrue(ContentType.quote(sb.toString() + "\t"));
        Assert.assertTrue(ContentType.quote(sb.toString() + "\""));
    }

    public void test_cases(Object[][] cases) {
        boolean bIsValid;
        String inStr;
        ContentType ct;
        Object[][] parameters;
        String name;
        String value;
        for (int i=0; i<cases.length; ++i) {
            bIsValid = (Boolean)cases[i][0];
            inStr = (String)cases[i][1];
            ct = ContentType.parseContentType(inStr);
            if (!bIsValid) {
                Assert.assertNull(ct);
            } else {
                Assert.assertNotNull(ct);
                //System.out.println(ct.contentType);
                //System.out.println(ct.mediaType);
                Assert.assertEquals(cases[i][2], ct.contentType);
                Assert.assertEquals(cases[i][3], ct.mediaType);
                parameters = (Object[][])cases[i][4];
                if (parameters != null) {
                    for (int j=0; j<parameters.length; ++j) {
                        name = (String)parameters[j][0];
                        value = ct.getParameter(name);
                        Assert.assertNotNull(value);
                        Assert.assertEquals(parameters[j][1], value);
                    }
                } else {
                    value = ct.getParameter("msgtype");
                    Assert.assertNull(value);
                }
                value = ct.getParameter(null);
                Assert.assertNull(value);
                value = ct.getParameter("");
                Assert.assertNull(value);
            }
        }
    }

    @Test
    public void test_contenttype_equals_hashcode() {
        ContentType ct1;
        ContentType ct2;

        /*
         * Nulls.
         */

        ct1 = new ContentType();
        ct1.contentType = null;
        ct1.mediaType = null;
        ct1.parameters = null;

        ct2 = new ContentType();
        ct2.contentType = null;
        ct2.mediaType = null;
        ct2.parameters = null;

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        Assert.assertFalse(ct1.equals(null));
        Assert.assertFalse(ct2.equals(null));

        /*
         * Null vs. partial.
         */

        ct2.contentType = "application";
        ct2.mediaType = null;
        ct2.parameters = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = null;
        ct2.mediaType = "http";
        ct2.parameters = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = null;
        ct2.mediaType = null;
        ct2.parameters = new HashMap<String, String>();

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = null;
        ct2.mediaType = null;
        ct2.parameters.put("msgtype", null);

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = null;
        ct2.mediaType = null;
        ct2.parameters.put("msgtype", "response");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        /*
         * Full vs. partial.
         */

        ct1.contentType = "application";
        ct1.mediaType = "http";
        ct1.parameters = new HashMap<String, String>();
        ct1.parameters.put("msgtype", "response");

        ct2.contentType = null;
        ct2.mediaType = null;
        ct2.parameters = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = "application";
        ct2.mediaType = null;
        ct2.parameters = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = "application";
        ct2.mediaType = "http";
        ct2.parameters = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = "application";
        ct2.mediaType = "http";
        ct2.parameters = new HashMap<String, String>();

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = "application";
        ct2.mediaType = "http";
        ct2.parameters.put("msgtype", null);

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = "application";
        ct2.mediaType = "http";
        ct2.parameters.put("response", "msgtype");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.contentType = "application";
        ct2.mediaType = "http";
        ct2.parameters.put("msgtype", "response");
        ct2.parameters.remove("response");

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        /*
         * Parameter variations.
         */

        ct1.parameters.clear();
        ct2.parameters.clear();

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        ct1.parameters.put("msgtype", "response");
        ct2.parameters.put("response", "msgtype");

        Assert.assertFalse(ct1.equals(ct2));
        // Technicality.
        //Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        ct1.parameters.clear();
        ct1.parameters.put("msgtype", null);
        ct2.parameters.clear();
        ct2.parameters.put("msgtype", "response");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct1.parameters.clear();
        ct1.parameters.put("msgtype", "response");
        ct2.parameters.clear();
        ct2.parameters.put("msgtype", null);

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct1.parameters.clear();
        ct1.parameters.put("msgtype", null);
        ct2.parameters.clear();
        ct2.parameters.put("msgtype", null);

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        /*
         * More parameters.
         */

        ct1.parameters.clear();
        ct2.parameters.clear();
        ct1.parameters.put("msgtype", "response");
        ct1.parameters.put("response", "msgtype");
        ct2.parameters.put("response", "msgtype");
        ct2.parameters.put("msgtype", "response");

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        ct1.parameters.clear();
        ct2.parameters.clear();
        ct1.parameters.put("msgtype", "response");
        ct1.parameters.put("response", "msgtype");
        ct2.parameters.put("response", "msgtype");
        ct2.parameters.put("msgtype", "null");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct1.parameters.clear();
        ct2.parameters.clear();
        ct1.parameters.put("msgtype", "response");
        ct1.parameters.put("response", "msgtype");
        ct2.parameters.put("response", "msgtype");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct1.parameters.clear();
        ct2.parameters.clear();
        ct1.parameters.put("response", "msgtype");
        ct2.parameters.put("response", "msgtype");
        ct2.parameters.put("msgtype", "null");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));
    }

}
