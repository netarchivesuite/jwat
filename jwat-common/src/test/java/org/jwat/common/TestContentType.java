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

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ContentType;

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
        ContentType ct;
        String value;
        String str;

        ct = ContentType.parseContentType(null);
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType(" ");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  ");
        Assert.assertNull(ct);

        ct = ContentType.parseContentType("text");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType(" text");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("text;");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType(" text;");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text;");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("text/");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType(" text/");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text/");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text/  ");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text/\t");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("text/;");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType(" text/;");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text/;");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  -");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text-");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text/:");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text/:;");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("  text/plain ;");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType(" text/plain a");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("   text/plain   a");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("text/plain;a");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("text/plain; a");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("text/plain;  a");
        Assert.assertNull(ct);

        /*
         * Content-type/Media-type.
         */

        ct = ContentType.parseContentType("text/plain");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("text", ct.contentType);
        Assert.assertEquals("plain", ct.mediaType);

        value = ct.getParameter(null);
        Assert.assertNull(value);
        value = ct.getParameter("");
        Assert.assertNull(value);
        value = ct.getParameter("msgtype");
        Assert.assertNull(value);

        ct = ContentType.parseContentType(" text/plain ");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("text", ct.contentType);
        Assert.assertEquals("plain", ct.mediaType);

        ct = ContentType.parseContentType("   text/plain   ");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("text", ct.contentType);
        Assert.assertEquals("plain", ct.mediaType);

        ct = ContentType.parseContentType(" text/plain \t");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("text", ct.contentType);
        Assert.assertEquals("plain", ct.mediaType);

        ct = ContentType.parseContentType("text/plain;");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("text", ct.contentType);
        Assert.assertEquals("plain", ct.mediaType);

        ct = ContentType.parseContentType(" text/plain; ");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("text", ct.contentType);
        Assert.assertEquals("plain", ct.mediaType);

        ct = ContentType.parseContentType(" text/plain;\t");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("text", ct.contentType);
        Assert.assertEquals("plain", ct.mediaType);

        /*
         * Parameters.
         */

        ct = ContentType.parseContentType("application/http;=request");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("application/http;= request");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("application/http;=  request");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("application/http;msgtype =request");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("application/http;msgtype= request");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("application/http;msgtype=request a");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("application/http;msgtype=request  a");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("application/http;msgtype=request:");
        Assert.assertNull(ct);
        ct = ContentType.parseContentType("application/http;msgtype=\"request");
        Assert.assertNull(ct);

        ct = ContentType.parseContentType("application/http;msgtype=request");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);

        value = ct.getParameter(null);
        Assert.assertNull(value);
        value = ct.getParameter("");
        Assert.assertNull(value);

        ct = ContentType.parseContentType(" application/http; msgtype=request ");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);

        ct = ContentType.parseContentType("\tapplication/http; msgtype=request ");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);

        ct = ContentType.parseContentType("  application/http;  msgtype=request  ");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);

        ct = ContentType.parseContentType("application/http;msgtype=\"request\"");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);

        ct = ContentType.parseContentType(" application/http; msgtype=\"request\" ");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);

        ct = ContentType.parseContentType("  application/http;  msgtype=\"request\"  ");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);

        ct = ContentType.parseContentType(" application/http; msgtype=\"request\"\t");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("http", ct.mediaType);
        value = ct.getParameter("msgtype");
        Assert.assertNotNull(value);
        Assert.assertEquals("request", value);

        ct = ContentType.parseContentType("application/http;msgtype=request;charset=utf8 ");
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

        ct = ContentType.parseContentType("application/http;msgtype=request;charset=utf8 ; ");
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

        ct = ContentType.parseContentType("application/http;msgtype=request;charset=utf8\t; ");
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

        Assert.assertFalse(ContentType.quote(null));
        Assert.assertFalse(ContentType.quote(""));
        Assert.assertFalse(ContentType.quote(sb.toString()));
        Assert.assertFalse(ContentType.quote(sb.toString() + "\u0100"));
        Assert.assertTrue(ContentType.quote(sb.toString() + " "));
        Assert.assertTrue(ContentType.quote(sb.toString() + "\t"));
        Assert.assertTrue(ContentType.quote(sb.toString() + "\""));

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
    }

}
