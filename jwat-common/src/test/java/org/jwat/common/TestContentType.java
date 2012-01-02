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
    public void test() throws IOException {
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

        ct = ContentType.parseContentType("application/warc-fields");
        Assert.assertNotNull(ct);
        //System.out.println(ct.contentType);
        //System.out.println(ct.mediaType);
        Assert.assertEquals("application", ct.contentType);
        Assert.assertEquals("warc-fields", ct.mediaType);

        str = ct.toString();
        Assert.assertEquals("application/warc-fields", str);

        ct.setParameter("quote", ".oOo. \t .oOo.");

        str = ct.toString();
        Assert.assertEquals("application/warc-fields; quote=\".oOo. \t .oOo.\"", str);
    }

}
