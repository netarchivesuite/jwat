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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestHttpHeader {

    private int min;
    private int max;
    private int runs;
    private String digestAlgorithm;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 256, 1, null},
                {1, 256, 1, "sha1"},
                {1, 2, 1, "shaft"}
        });
    }

    public TestHttpHeader(int min, int max, int runs, String digestAlgorithm) {
        this.min = min;
        this.max = max;
        this.runs = runs;
        this.digestAlgorithm = digestAlgorithm;
    }

    public static byte[] responseHeaderArr;
    public static byte[] requestHeaderArr;

    static {
        String responseHeader = "";
        responseHeader += "HTTP/1.1 200 OK\r\n";
        responseHeader += "Date: Wed, 30 Apr 2008 20:53:30 GMT\r\n";
        responseHeader += "Server: Apache/2.0.54 (Ubuntu) PHP/5.0.5-2ubuntu1.4 mod_ssl/2.0.54 OpenSSL/0.9.7g\r\n";
        responseHeader += "X-Powered-By: PHP/5.0.5-2ubuntu1.4\r\n";
        responseHeader += "Connection: close\r\n";
        responseHeader += "Content-Type: text/html; charset=UTF-8\r\n";
        responseHeader += "\r\n";
        responseHeaderArr = responseHeader.getBytes();

        String requestHeader = "";
        requestHeader += "GET /robots.txt HTTP/1.0\r\n";
        requestHeader += "User-Agent: Mozilla/5.0 (compatible; heritrix/1.14.0 +http://crawler.archive.org)\r\n";
        requestHeader += "From: archive-crawler-agent@lists.sourceforge.net\r\n";
        requestHeader += "Connection: close\r\n";
        requestHeader += "Referer: http://www.archive.org/\r\n";
        requestHeader += "Host: www.archive.org\r\n";
        requestHeader += "Content-Type: application/binary\r\n";
        requestHeader += "\r\n";
        requestHeaderArr = requestHeader.getBytes();
    }

    @Test
    public void test_httpheader() {
        byte[] srcArr = new byte[ 0 ];

        ByteCountingPushBackInputStream pbin;
        HttpHeader httpHeader;

        httpHeader = new HttpHeader();
        String tmpStr = httpHeader.toString();
        Assert.assertNotNull(tmpStr);

        Assert.assertTrue( HttpHeader.isSupported( "http" ) );
        Assert.assertTrue( HttpHeader.isSupported( "https" ) );
        Assert.assertTrue( HttpHeader.isSupported( "Http" ) );
        Assert.assertTrue( HttpHeader.isSupported( "Https" ) );

        Assert.assertFalse( HttpHeader.isSupported( "httpss" ) );
        Assert.assertFalse( HttpHeader.isSupported( "ftp" ) );
        Assert.assertFalse( HttpHeader.isSupported( "ftps" ) );

        try {
            httpHeader = HttpHeader.processPayload( 0, null, 0, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            httpHeader = HttpHeader.processPayload( HttpHeader.HT_RESPONSE, null, 0, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
            httpHeader = HttpHeader.processPayload( HttpHeader.HT_RESPONSE, pbin, -1, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            pbin = new  ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
            httpHeader = HttpHeader.processPayload( HttpHeader.HT_RESPONSE, pbin, 0, "shit1" );
            Assert.assertFalse(httpHeader.isValid());
            Assert.assertNull( httpHeader.getMessageDigest() );
            try {
                httpHeader.getPayloadLength();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getTotalLength();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getUnavailable();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getInputStreamComplete();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getPayloadInputStream();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getRemaining();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test http response parsing.
     */
    @Test
    public void test_httpheader_response() {
        SecureRandom random = new SecureRandom();

        byte[] payloadArr;
        ByteArrayOutputStream srcOut = new ByteArrayOutputStream();
        byte[] srcArr = new byte[ 0 ];
        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        ByteCountingPushBackInputStream pbin;
        HttpHeader httpHeader;

        InputStream in;
        long remaining;
        byte[] tmpBuf = new byte[ 256 ];
        int read;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance( "SHA1" );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for ( int r=0; r<runs; ++r) {
            for ( int n=min; n<max; ++n ) {
                payloadArr = new byte[ n ];
                random.nextBytes( payloadArr );

                try {
                    srcOut.reset();
                    srcOut.write( responseHeaderArr );
                    srcOut.write( payloadArr );
                    srcArr = srcOut.toByteArray();
                    /*
                     * HttpHeader Payload.
                     */
                    pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
                    httpHeader = HttpHeader.processPayload( HttpHeader.HT_RESPONSE, pbin, srcArr.length, digestAlgorithm );

                    Assert.assertTrue(httpHeader.isValid());
                    Assert.assertEquals(HttpHeader.HT_RESPONSE, httpHeader.headerType);

                    in = httpHeader.getPayloadInputStream();
                    Assert.assertEquals(in, httpHeader.getPayloadInputStream());
                    Assert.assertEquals( srcArr.length, httpHeader.getTotalLength() );

                    dstOut.reset();

                    remaining = httpHeader.getTotalLength() - httpHeader.getHeader().length;
                    read = 0;
                    while ( remaining > 0 && read != -1 ) {
                        dstOut.write(tmpBuf, 0, read);
                        remaining -= read;

                        read = random.nextInt( 15 ) + 1;
                        read = in.read(tmpBuf, 0, read);
                    }

                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( 0, httpHeader.getUnavailable() );
                    Assert.assertEquals( 0, httpHeader.getRemaining() );

                    Assert.assertArrayEquals(responseHeaderArr, httpHeader.getHeader());

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( payloadArr.length, dstArr.length );
                    Assert.assertArrayEquals( payloadArr, dstArr );

                    in.close();

                    Assert.assertEquals( "HTTP/1.1", httpHeader.getProtocolVersion() );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMajor );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMinor );
                    Assert.assertEquals( "200", httpHeader.getProtocolStatusCodeStr() );
                    Assert.assertEquals( new Integer(200), httpHeader.getProtocolStatusCode() );
                    Assert.assertEquals( null, httpHeader.method );
                    Assert.assertEquals( null, httpHeader.requestUri);
                    Assert.assertEquals( "text/html; charset=UTF-8", httpHeader.getProtocolContentType() );
                    Assert.assertEquals( n, httpHeader.getPayloadLength() );

                    httpHeader.close();

                    Assert.assertNotNull( httpHeader.toString() );

                    String[][] headerLines = new String[][] {
                            {"Date", "Wed, 30 Apr 2008 20:53:30 GMT"},
                            {"Server", "Apache/2.0.54 (Ubuntu) PHP/5.0.5-2ubuntu1.4 mod_ssl/2.0.54 OpenSSL/0.9.7g"},
                            {"X-Powered-By", "PHP/5.0.5-2ubuntu1.4"},
                            {"Connection", "close"},
                            {"Content-Type", "text/html; charset=UTF-8"}
                    };

                    List<HeaderLine> headerList = httpHeader.getHeaderList();
                    Assert.assertEquals(headerLines.length, headerList.size());
                    Assert.assertNull(httpHeader.getHeader(null));
                    Assert.assertNull(httpHeader.getHeader(""));
                    for (int i=0; i<headerLines.length; ++i) {
                        HeaderLine hl = headerList.get(i);
                        Assert.assertEquals(headerLines[i][0], hl.name);
                        Assert.assertEquals(headerLines[i][1], hl.value);
                        Assert.assertEquals(hl, httpHeader.getHeader(headerLines[i][0].toUpperCase()));
                    }

                    /*
                     * HttpResponse Payload Digest.
                     */
                    if ( digestAlgorithm != null ) {
                        if ("sha1".equals( digestAlgorithm )) {
                            Assert.assertFalse( httpHeader.bNoSuchAlgorithmException );
                            Assert.assertNotNull( httpHeader.getMessageDigest() );

                            md.reset();
                            byte[] digest1 = md.digest( payloadArr );

                            byte[] digest2 = httpHeader.getMessageDigest().digest();

                            Assert.assertArrayEquals( digest1, digest2 );
                        } else {
                            Assert.assertTrue( httpHeader.bNoSuchAlgorithmException );
                            Assert.assertNull( httpHeader.getMessageDigest() );
                        }
                    } else {
                        Assert.assertNull( httpHeader.getMessageDigest() );
                    }
                    /*
                     * HttpHeader Complete
                     */
                    pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
                    httpHeader = HttpHeader.processPayload( HttpHeader.HT_RESPONSE, pbin, srcArr.length, digestAlgorithm );

                    Assert.assertTrue(httpHeader.isValid());
                    Assert.assertEquals(HttpHeader.HT_RESPONSE, httpHeader.headerType);

                    in = httpHeader.getInputStreamComplete();
                    Assert.assertEquals(in, httpHeader.getInputStreamComplete());
                    Assert.assertEquals( srcArr.length, httpHeader.getTotalLength() );

                    dstOut.reset();

                    remaining = httpHeader.getTotalLength();
                    read = 0;
                    while ( remaining > 0 && read != -1 ) {
                        dstOut.write(tmpBuf, 0, read);
                        remaining -= read;

                        read = random.nextInt( 15 ) + 1;
                        read = in.read(tmpBuf, 0, read);
                    }

                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( 0, httpHeader.getUnavailable() );
                    Assert.assertEquals( 0, httpHeader.getRemaining() );

                    Assert.assertArrayEquals(responseHeaderArr, httpHeader.getHeader());

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    Assert.assertFalse(httpHeader.isClosed());
                    in.close();
                    Assert.assertFalse(httpHeader.isClosed());

                    Assert.assertEquals( "HTTP/1.1", httpHeader.getProtocolVersion() );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMajor );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMinor );
                    Assert.assertEquals( "200", httpHeader.getProtocolStatusCodeStr() );
                    Assert.assertEquals( new Integer(200), httpHeader.getProtocolStatusCode() );
                    Assert.assertEquals( null, httpHeader.method );
                    Assert.assertEquals( null, httpHeader.requestUri);
                    Assert.assertEquals( "text/html; charset=UTF-8", httpHeader.getProtocolContentType() );
                    Assert.assertEquals( n, httpHeader.getPayloadLength() );

                    httpHeader.close();
                    Assert.assertTrue(httpHeader.isClosed());

                    Assert.assertNotNull( httpHeader.toString() );

                    in.close();
                    httpHeader.close();

                    headerList = httpHeader.getHeaderList();
                    Assert.assertEquals(headerLines.length, headerList.size());
                    Assert.assertNull(httpHeader.getHeader(null));
                    Assert.assertNull(httpHeader.getHeader(""));
                    for (int i=0; i<headerLines.length; ++i) {
                        HeaderLine hl = headerList.get(i);
                        Assert.assertEquals(headerLines[i][0], hl.name);
                        Assert.assertEquals(headerLines[i][1], hl.value);
                        Assert.assertEquals(hl, httpHeader.getHeader(headerLines[i][0].toUpperCase()));
                    }

                    /*
                     * HttpHeader Payload Digest.
                     */
                    if ( digestAlgorithm != null ) {
                        if ("sha1".equals(digestAlgorithm)) {
                            Assert.assertFalse( httpHeader.bNoSuchAlgorithmException );
                            Assert.assertNotNull( httpHeader.getMessageDigest() );

                            md.reset();
                            byte[] digest1 = md.digest( payloadArr );

                            byte[] digest2 = httpHeader.getMessageDigest().digest();

                            Assert.assertArrayEquals( digest1, digest2 );
                        } else {
                            Assert.assertTrue( httpHeader.bNoSuchAlgorithmException );
                            Assert.assertNull( httpHeader.getMessageDigest() );
                        }
                    } else {
                        Assert.assertNull( httpHeader.getMessageDigest() );
                    }
                } catch (IOException e) {
                    Assert.fail( "Exception not expected!" );
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Test http request parsing
     */
    @Test
    public void test_httpheader_request() {
        SecureRandom random = new SecureRandom();

        byte[] payloadArr;
        ByteArrayOutputStream srcOut = new ByteArrayOutputStream();
        byte[] srcArr = new byte[ 0 ];
        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        ByteCountingPushBackInputStream pbin;
        HttpHeader httpHeader;

        InputStream in;
        long remaining;
        byte[] tmpBuf = new byte[ 256 ];
        int read;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance( "SHA1" );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for ( int r=0; r<runs; ++r) {
            for ( int n=min; n<max; ++n ) {
                payloadArr = new byte[ n ];
                random.nextBytes( payloadArr );

                try {
                    srcOut.reset();
                    srcOut.write( requestHeaderArr );
                    srcOut.write( payloadArr );
                    srcArr = srcOut.toByteArray();
                    /*
                     * HttpHeader Payload.
                     */
                    pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
                    httpHeader = HttpHeader.processPayload( HttpHeader.HT_REQUEST, pbin, srcArr.length, digestAlgorithm );

                    Assert.assertTrue(httpHeader.isValid());
                    Assert.assertEquals(HttpHeader.HT_REQUEST, httpHeader.headerType);

                    in = httpHeader.getPayloadInputStream();
                    Assert.assertEquals(in, httpHeader.getPayloadInputStream());
                    Assert.assertEquals( srcArr.length, httpHeader.getTotalLength() );

                    dstOut.reset();

                    remaining = httpHeader.getTotalLength() - httpHeader.getHeader().length;
                    read = 0;
                    while ( remaining > 0 && read != -1 ) {
                        dstOut.write(tmpBuf, 0, read);
                        remaining -= read;

                        read = random.nextInt( 15 ) + 1;
                        read = in.read(tmpBuf, 0, read);
                    }

                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( 0, httpHeader.getUnavailable() );
                    Assert.assertEquals( 0, httpHeader.getRemaining() );

                    Assert.assertArrayEquals(requestHeaderArr, httpHeader.getHeader());

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( payloadArr.length, dstArr.length );
                    Assert.assertArrayEquals( payloadArr, dstArr );

                    in.close();

                    Assert.assertEquals( "HTTP/1.0", httpHeader.getProtocolVersion() );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMajor );
                    Assert.assertEquals( new Integer(0), httpHeader.httpVersionMinor );
                    Assert.assertEquals( "GET", httpHeader.method );
                    Assert.assertEquals( "/robots.txt", httpHeader.requestUri);
                    Assert.assertEquals( null, httpHeader.getProtocolStatusCodeStr() );
                    Assert.assertEquals( null, httpHeader.getProtocolStatusCode() );
                    Assert.assertEquals( "application/binary", httpHeader.getProtocolContentType() );
                    Assert.assertEquals( n, httpHeader.getPayloadLength() );
                    /*

                    httpHeader.close();
                    */

                    Assert.assertNotNull( httpHeader.toString() );

                    String[][] headerLines = new String[][] {
                            {"User-Agent", "Mozilla/5.0 (compatible; heritrix/1.14.0 +http://crawler.archive.org)"},
                            {"From", "archive-crawler-agent@lists.sourceforge.net"},
                            {"Connection", "close"},
                            {"Referer", "http://www.archive.org/"},
                            {"Host", "www.archive.org"},
                            {"Content-Type", "application/binary"}
                    };

                    List<HeaderLine> headerList = httpHeader.getHeaderList();
                    Assert.assertEquals(headerLines.length, headerList.size());
                    Assert.assertNull(httpHeader.getHeader(null));
                    Assert.assertNull(httpHeader.getHeader(""));
                    for (int i=0; i<headerLines.length; ++i) {
                        HeaderLine hl = headerList.get(i);
                        Assert.assertEquals(headerLines[i][0], hl.name);
                        Assert.assertEquals(headerLines[i][1], hl.value);
                        Assert.assertEquals(hl, httpHeader.getHeader(headerLines[i][0].toUpperCase()));
                    }

                    /*
                     * HttpResponse Payload Digest.
                     */
                    if ( digestAlgorithm != null ) {
                        if ("sha1".equals(digestAlgorithm)) {
                            Assert.assertFalse( httpHeader.bNoSuchAlgorithmException );
                            Assert.assertNotNull( httpHeader.getMessageDigest() );

                            md.reset();
                            byte[] digest1 = md.digest( payloadArr );

                            byte[] digest2 = httpHeader.getMessageDigest().digest();

                            Assert.assertArrayEquals( digest1, digest2 );
                        } else {
                            Assert.assertTrue( httpHeader.bNoSuchAlgorithmException );
                            Assert.assertNull( httpHeader.getMessageDigest() );
                        }
                    } else {
                        Assert.assertNull( httpHeader.getMessageDigest() );
                    }
                    /*
                     * HttpHeader Complete
                     */
                    pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
                    httpHeader = HttpHeader.processPayload( HttpHeader.HT_REQUEST, pbin, srcArr.length, digestAlgorithm );

                    Assert.assertTrue(httpHeader.isValid());
                    Assert.assertEquals(HttpHeader.HT_REQUEST, httpHeader.headerType);

                    in = httpHeader.getInputStreamComplete();
                    Assert.assertEquals(in, httpHeader.getInputStreamComplete());
                    Assert.assertEquals( srcArr.length, httpHeader.getTotalLength() );

                    dstOut.reset();

                    remaining = httpHeader.getTotalLength();
                    read = 0;
                    while ( remaining > 0 && read != -1 ) {
                        dstOut.write(tmpBuf, 0, read);
                        remaining -= read;

                        read = random.nextInt( 15 ) + 1;
                        read = in.read(tmpBuf, 0, read);
                    }

                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( 0, httpHeader.getUnavailable() );
                    Assert.assertEquals( 0, httpHeader.getRemaining() );

                    Assert.assertArrayEquals(requestHeaderArr, httpHeader.getHeader());

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    Assert.assertFalse(httpHeader.isClosed());
                    in.close();
                    Assert.assertFalse(httpHeader.isClosed());

                    Assert.assertEquals( "HTTP/1.0", httpHeader.getProtocolVersion() );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMajor );
                    Assert.assertEquals( new Integer(0), httpHeader.httpVersionMinor );
                    Assert.assertEquals( "GET", httpHeader.method );
                    Assert.assertEquals( "/robots.txt", httpHeader.requestUri);
                    Assert.assertEquals( null, httpHeader.getProtocolStatusCodeStr() );
                    Assert.assertEquals( null, httpHeader.getProtocolStatusCode() );
                    Assert.assertEquals( "application/binary", httpHeader.getProtocolContentType() );
                    Assert.assertEquals( n, httpHeader.getPayloadLength() );

                    httpHeader.close();
                    Assert.assertTrue(httpHeader.isClosed());

                    Assert.assertNotNull( httpHeader.toString() );

                    in.close();
                    httpHeader.close();
                    /*
                     * HttpHeader Payload Digest.
                     */
                    if ( digestAlgorithm != null ) {
                        if ("sha1".equals(digestAlgorithm)) {
                            Assert.assertFalse( httpHeader.bNoSuchAlgorithmException );
                            Assert.assertNotNull( httpHeader.getMessageDigest() );

                            md.reset();
                            byte[] digest1 = md.digest( payloadArr );

                            byte[] digest2 = httpHeader.getMessageDigest().digest();

                            Assert.assertArrayEquals( digest1, digest2 );
                        } else {
                            Assert.assertTrue( httpHeader.bNoSuchAlgorithmException );
                            Assert.assertNull( httpHeader.getMessageDigest() );
                        }
                    } else {
                        Assert.assertNull( httpHeader.getMessageDigest() );
                    }
                } catch (IOException e) {
                    Assert.fail( "Exception not expected!" );
                    e.printStackTrace();
                }
            }
        }
    }

}
