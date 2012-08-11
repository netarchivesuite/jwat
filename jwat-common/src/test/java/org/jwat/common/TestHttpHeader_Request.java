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
public class TestHttpHeader_Request {

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

    public TestHttpHeader_Request(int min, int max, int runs, String digestAlgorithm) {
        this.min = min;
        this.max = max;
        this.runs = runs;
        this.digestAlgorithm = digestAlgorithm;
    }

    /**
     * Test http request parsing
     */
    @Test
    public void test_httpheader_request() {
        Object[][] test_cases = new Object[][] {
                {("GET /robots.txt HTTP/1.0\r\n"
                        + "User-Agent: Mozilla/5.0 (compatible; heritrix/1.14.0 +http://crawler.archive.org)\r\n"
                        + "From: archive-crawler-agent@lists.sourceforge.net\r\n"
                        + "Connection: close\r\n"
                        + "Referer: http://www.archive.org/\r\n"
                        + "Host: www.archive.org\r\n"
                        + "Content-Type: application/binary\r\n"
                        + "\r\n"
                ).getBytes(), new Object[] {
                        "HTTP/1.0", new Integer(1), new Integer(0), "GET", "/robots.txt", "application/binary"
                }, new String[][] {
                        {"User-Agent", "Mozilla/5.0 (compatible; heritrix/1.14.0 +http://crawler.archive.org)"},
                        {"From", "archive-crawler-agent@lists.sourceforge.net"},
                        {"Connection", "close"},
                        {"Referer", "http://www.archive.org/"},
                        {"Host", "www.archive.org"},
                        {"Content-Type", "application/binary"}
                }},
                {("GET http://bits.wikimedia.org/en.wikipedia.org/load.php?debug=false&lang=en&modules=ext.UserBuckets%2CmarkAsHelpful%7Cext.UserBuckets.AccountCreationUserBucket%7Cext.articleFeedback.startup%7Cext.articleFeedbackv5.startup%7Cext.gadget.wmfFR2011Style%7Cjquery.autoEllipsis%2CcheckboxShiftClick%2CclickTracking%2CcollapsibleTabs%2Ccookie%2CdelayedBind%2ChighlightText%2Cjson%2CmakeCollapsible%2CmessageBox%2CmwPrototypes%2Cplaceholder%2Csuggestions%2CtabIndex%7Cmediawiki.language%2Cuser%2Cutil%7Cmediawiki.legacy.ajax%2Cmwsuggest%2Cwikibits%7Cmediawiki.page.ready&skin=vector&version=20120118T020454Z&* HTTP/1.1\r\n"
                        + "User-Agent: Wget/1.12-2507-dirty (darwin11.2.0)\r\n"
                        + "Accept: */*\r\n"
                        + "Host: bits.wikimedia.org\r\n"
                        + "Connection: Close\r\n"
                        + "Proxy-Connection: Keep-Alive\r\n"
                        + "\r\n"
                ).getBytes(), new Object[] {
                        "HTTP/1.1", new Integer(1), new Integer(1), "GET", "http://bits.wikimedia.org/en.wikipedia.org/load.php?debug=false&lang=en&modules=ext.UserBuckets%2CmarkAsHelpful%7Cext.UserBuckets.AccountCreationUserBucket%7Cext.articleFeedback.startup%7Cext.articleFeedbackv5.startup%7Cext.gadget.wmfFR2011Style%7Cjquery.autoEllipsis%2CcheckboxShiftClick%2CclickTracking%2CcollapsibleTabs%2Ccookie%2CdelayedBind%2ChighlightText%2Cjson%2CmakeCollapsible%2CmessageBox%2CmwPrototypes%2Cplaceholder%2Csuggestions%2CtabIndex%7Cmediawiki.language%2Cuser%2Cutil%7Cmediawiki.legacy.ajax%2Cmwsuggest%2Cwikibits%7Cmediawiki.page.ready&skin=vector&version=20120118T020454Z&*", null
                }, new String[][] {
                        {"User-Agent", "Wget/1.12-2507-dirty (darwin11.2.0)"},
                        {"Accept", "*/*"},
                        {"Host", "bits.wikimedia.org"},
                        {"Connection", "Close"},
                        {"Proxy-Connection", "Keep-Alive"}
                }}
        };

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

        for ( int c=0; c<test_cases.length; ++c) {
            byte[] requestHeaderArr = (byte[])test_cases[c][0];
            Object[] requestHeaderValues = (Object[])test_cases[c][1];
            String[][] headerLines = (String[][])test_cases[c][2];

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

                        Assert.assertEquals( requestHeaderValues[0], httpHeader.getProtocolVersion() );
                        Assert.assertEquals( requestHeaderValues[1], httpHeader.httpVersionMajor );
                        Assert.assertEquals( requestHeaderValues[2], httpHeader.httpVersionMinor );
                        Assert.assertEquals( requestHeaderValues[3], httpHeader.method );
                        Assert.assertEquals( requestHeaderValues[4], httpHeader.requestUri);
                        Assert.assertEquals( requestHeaderValues[5], httpHeader.getProtocolContentType() );
                        Assert.assertEquals( null, httpHeader.getProtocolStatusCodeStr() );
                        Assert.assertEquals( null, httpHeader.getProtocolStatusCode() );
                        Assert.assertEquals( n, httpHeader.getPayloadLength() );
                        /*

                        httpHeader.close();
                        */

                        Assert.assertNotNull( httpHeader.toString() );

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
                                Assert.assertNotNull( httpHeader.getDigest() );

                                md.reset();
                                byte[] digest1 = md.digest( payloadArr );

                                byte[] digest2 = httpHeader.getDigest();

                                Assert.assertArrayEquals( digest1, digest2 );
                            } else {
                                Assert.assertTrue( httpHeader.bNoSuchAlgorithmException );
                                Assert.assertNull( httpHeader.getDigest() );
                            }
                        } else {
                            Assert.assertNull( httpHeader.getDigest() );
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

                        Assert.assertEquals( requestHeaderValues[0], httpHeader.getProtocolVersion() );
                        Assert.assertEquals( requestHeaderValues[1], httpHeader.httpVersionMajor );
                        Assert.assertEquals( requestHeaderValues[2], httpHeader.httpVersionMinor );
                        Assert.assertEquals( requestHeaderValues[3], httpHeader.method );
                        Assert.assertEquals( requestHeaderValues[4], httpHeader.requestUri);
                        Assert.assertEquals( requestHeaderValues[5], httpHeader.getProtocolContentType() );
                        Assert.assertEquals( null, httpHeader.getProtocolStatusCodeStr() );
                        Assert.assertEquals( null, httpHeader.getProtocolStatusCode() );
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
                                Assert.assertNotNull( httpHeader.getDigest() );

                                md.reset();
                                byte[] digest1 = md.digest( payloadArr );

                                byte[] digest2 = httpHeader.getDigest();

                                Assert.assertArrayEquals( digest1, digest2 );
                            } else {
                                Assert.assertTrue( httpHeader.bNoSuchAlgorithmException );
                                Assert.assertNull( httpHeader.getDigest() );
                            }
                        } else {
                            Assert.assertNull( httpHeader.getDigest() );
                        }
                    } catch (IOException e) {
                        Assert.fail( "Exception not expected!" );
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
