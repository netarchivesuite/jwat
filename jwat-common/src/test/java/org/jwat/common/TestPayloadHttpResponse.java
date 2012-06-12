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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestPayloadHttpResponse implements PayloadOnClosedHandler {

    private int min;
    private int max;
    private int runs;
    private String digestAlgorithm;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 256, 1, null},
                {1, 256, 1, "sha1"}
        });
    }

    public TestPayloadHttpResponse(int min, int max, int runs, String digestAlgorithm) {
        this.min = min;
        this.max = max;
        this.runs = runs;
        this.digestAlgorithm = digestAlgorithm;
    }

    public static byte[] headerArr;

    static {
        String header = "";
        header += "HTTP/1.1 200 OK\r\n";
        header += "Date: Wed, 30 Apr 2008 20:53:30 GMT\r\n";
        header += "Server: Apache/2.0.54 (Ubuntu) PHP/5.0.5-2ubuntu1.4 mod_ssl/2.0.54 OpenSSL/0.9.7g\r\n";
        header += "X-Powered-By: PHP/5.0.5-2ubuntu1.4\r\n";
        header += "Connection: close\r\n";
        header += "Content-Type: text/html; charset=UTF-8\r\n";
        header += "\r\n";
        headerArr = header.getBytes();
    }

    public int closed = 0;

    @Test
    public void test_payload_httpresponse() {
        SecureRandom random = new SecureRandom();

        byte[] payloadArr;
        ByteArrayOutputStream srcOut = new ByteArrayOutputStream();
        byte[] srcArr = new byte[ 0 ];
        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        Payload payload;
        HttpHeader httpHeader;

        InputStream in;
        long remaining;
        byte[] tmpBuf = new byte[ 256 ];
        int read;

        MessageDigest mdPayload = null;
        try {
            mdPayload = MessageDigest.getInstance( "SHA1" );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        MessageDigest mdHttp = null;
        try {
            mdHttp = MessageDigest.getInstance( "SHA1" );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for ( int r=0; r<runs; ++r) {
            for ( int n=min; n<max; ++n ) {
                payloadArr = new byte[ n ];
                random.nextBytes( payloadArr );

                try {
                    srcOut.reset();
                    srcOut.write( headerArr );
                    srcOut.write( payloadArr );
                    srcArr = srcOut.toByteArray();
                    /*
                     * Payload.
                     */
                    // Important pushback buffersize determines the maximum size of the httpheader!
                    payload = Payload.processPayload( new ByteArrayInputStream( srcArr ), srcArr.length, 8192, digestAlgorithm );
                    //payload.setOnClosedHandler( this );
                    Assert.assertEquals(srcArr.length, payload.getTotalLength());
                    /*
                     * HttpHeader.
                     */
                    httpHeader = HttpHeader.processPayload(HttpHeader.HT_RESPONSE,
                            payload.getInputStream(),
                            payload.getTotalLength(),
                            digestAlgorithm);
                    if (httpHeader != null) {
                        payload.setHttpHeader(httpHeader);
                    }
                    Assert.assertTrue(httpHeader.isValid());
                    Assert.assertNotNull(httpHeader);
                    Assert.assertEquals(httpHeader, payload.httpHeader);
                    Assert.assertEquals(httpHeader, payload.getHttpHeader());

                    in = httpHeader.getPayloadInputStream();
                    Assert.assertEquals(in, httpHeader.getPayloadInputStream());
                    Assert.assertEquals( srcArr.length, payload.getTotalLength() );
                    Assert.assertEquals( srcArr.length, httpHeader.getTotalLength() );
                    Assert.assertEquals( 8192, payload.getPushbackSize() );

                    dstOut.reset();

                    remaining = httpHeader.getTotalLength() - httpHeader.getHeader().length;
                    read = 0;
                    while ( remaining > 0 && read != -1 ) {
                        dstOut.write(tmpBuf, 0, read);
                        remaining -= read;

                        // This wont work with buffered streams...
                        //Assert.assertEquals( remaining, payload.getUnavailable() );
                        //Assert.assertEquals( remaining, payload.getRemaining() );

                        read = random.nextInt( 15 ) + 1;
                        read = in.read(tmpBuf, 0, read);
                    }

                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( 0, httpHeader.getUnavailable() );
                    Assert.assertEquals( 0, httpHeader.getRemaining() );
                    Assert.assertEquals( 0, payload.getUnavailable() );
                    Assert.assertEquals( 0, payload.getRemaining() );

                    Assert.assertArrayEquals(headerArr, httpHeader.getHeader());

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( payloadArr.length, dstArr.length );
                    Assert.assertArrayEquals( payloadArr, dstArr );

                    Assert.assertFalse(httpHeader.isClosed());
                    Assert.assertFalse(payload.isClosed());
                    in.close();
                    Assert.assertFalse(httpHeader.isClosed());
                    Assert.assertFalse(payload.isClosed());

                    Assert.assertEquals( "HTTP/1.1", httpHeader.getProtocolVersion() );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMajor );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMinor );
                    Assert.assertEquals( "200", httpHeader.getProtocolStatusCodeStr() );
                    Assert.assertEquals( new Integer(200), httpHeader.getProtocolStatusCode() );
                    Assert.assertEquals( null, httpHeader.method );
                    Assert.assertEquals( null, httpHeader.requestUri);
                    Assert.assertEquals( "text/html; charset=UTF-8", httpHeader.getProtocolContentType() );
                    Assert.assertEquals( n, httpHeader.getPayloadLength() );

                    payload.close();
                    Assert.assertTrue(httpHeader.isClosed());
                    Assert.assertTrue(payload.isClosed());
                    httpHeader.close();

                    Assert.assertNotNull( httpHeader.toString() );

                    in.close();
                    httpHeader.close();
                    payload.close();
                    /*
                     * HttpResponse Payload Digest.
                     */
                    if ( digestAlgorithm != null ) {
                        Assert.assertNotNull( payload.getMessageDigest() );
                        mdPayload.reset();
                        byte[] digest1 = mdPayload.digest( srcArr );
                        byte[] digest2 = payload.getMessageDigest().digest();
                        Assert.assertArrayEquals( digest1, digest2 );

                        Assert.assertNotNull( httpHeader.getMessageDigest() );
                        mdHttp.reset();
                        byte[] digest3 = mdHttp.digest( payloadArr );
                        byte[] digest4 = httpHeader.getMessageDigest().digest();
                        Assert.assertArrayEquals( digest3, digest4 );
                    } else {
                        Assert.assertNull( httpHeader.getMessageDigest() );
                    }
                    /*
                     * Payload.
                     */
                    // Important pushback buffersize determines the maximum size of the httpheader!
                    payload = Payload.processPayload( new ByteArrayInputStream( srcArr ), srcArr.length, 8192, digestAlgorithm );
                    //payload.setOnClosedHandler( this );
                    Assert.assertEquals(srcArr.length, payload.getTotalLength());
                    /*
                     * HttpHeader Complete
                     */
                    httpHeader = HttpHeader.processPayload(HttpHeader.HT_RESPONSE,
                            payload.getInputStream(),
                            payload.getTotalLength(),
                            digestAlgorithm);
                    if (httpHeader != null) {
                        payload.setHttpHeader(httpHeader);
                    }
                    Assert.assertTrue(httpHeader.isValid());
                    Assert.assertNotNull(httpHeader);
                    Assert.assertEquals(httpHeader, payload.httpHeader);
                    Assert.assertEquals(httpHeader, payload.getHttpHeader());

                    in = httpHeader.getInputStreamComplete();
                    Assert.assertEquals(in, httpHeader.getInputStreamComplete());
                    Assert.assertEquals(in, payload.getInputStreamComplete());
                    Assert.assertEquals( srcArr.length, payload.getTotalLength() );
                    Assert.assertEquals( srcArr.length, httpHeader.getTotalLength() );
                    Assert.assertEquals( 8192, payload.getPushbackSize() );

                    dstOut.reset();

                    remaining = httpHeader.getTotalLength();
                    read = 0;
                    while ( remaining > 0 && read != -1 ) {
                        dstOut.write(tmpBuf, 0, read);
                        remaining -= read;

                        // This wont work with buffered streams...
                        //Assert.assertEquals( remaining, payload.getUnavailable() );
                        //Assert.assertEquals( remaining, payload.getRemaining() );

                        read = random.nextInt( 15 ) + 1;
                        read = in.read(tmpBuf, 0, read);
                    }

                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( 0, httpHeader.getUnavailable() );
                    Assert.assertEquals( 0, httpHeader.getRemaining() );
                    Assert.assertEquals( 0, payload.getUnavailable() );
                    Assert.assertEquals( 0, payload.getRemaining() );

                    Assert.assertArrayEquals(headerArr, httpHeader.getHeader());

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    Assert.assertFalse(httpHeader.isClosed());
                    Assert.assertFalse(payload.isClosed());
                    in.close();
                    Assert.assertFalse(httpHeader.isClosed());
                    Assert.assertFalse(payload.isClosed());

                    Assert.assertEquals( "HTTP/1.1", httpHeader.getProtocolVersion() );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMajor );
                    Assert.assertEquals( new Integer(1), httpHeader.httpVersionMinor );
                    Assert.assertEquals( "200", httpHeader.getProtocolStatusCodeStr() );
                    Assert.assertEquals( new Integer(200), httpHeader.getProtocolStatusCode() );
                    Assert.assertEquals( null, httpHeader.method );
                    Assert.assertEquals( null, httpHeader.requestUri);
                    Assert.assertEquals( "text/html; charset=UTF-8", httpHeader.getProtocolContentType() );
                    Assert.assertEquals( n, httpHeader.getPayloadLength() );

                    payload.close();
                    Assert.assertTrue(httpHeader.isClosed());
                    Assert.assertTrue(payload.isClosed());
                    httpHeader.close();

                    Assert.assertNotNull( httpHeader.toString() );

                    in.close();
                    httpHeader.close();
                    payload.close();
                    /*
                     * HttpHeader Payload Digest.
                     */
                    if ( digestAlgorithm != null ) {
                        Assert.assertNotNull( payload.getMessageDigest() );
                        mdPayload.reset();
                        byte[] digest1 = mdPayload.digest( srcArr );
                        byte[] digest2 = payload.getMessageDigest().digest();
                        Assert.assertArrayEquals( digest1, digest2 );

                        Assert.assertNotNull( httpHeader.getMessageDigest() );
                        mdHttp.reset();
                        byte[] digest3 = mdHttp.digest( payloadArr );
                        byte[] digest4 = httpHeader.getMessageDigest().digest();
                        Assert.assertArrayEquals( digest3, digest4 );
                    } else {
                        Assert.assertNull( httpHeader.getMessageDigest() );
                    }
                    /*
                     * Payload.
                     */
                    // Important pushback buffersize determines the maximum size of the httpheader!
                    payload = Payload.processPayload( new ByteArrayInputStream( srcArr ), srcArr.length, 8192, digestAlgorithm );
                    payload.setOnClosedHandler( this );
                    Assert.assertEquals(srcArr.length, payload.getTotalLength());
                    /*
                     * HttpResponse Complete
                     */
                    httpHeader = HttpHeader.processPayload(HttpHeader.HT_RESPONSE,
                            payload.getInputStream(),
                            payload.getTotalLength(),
                            digestAlgorithm);
                    if (httpHeader != null) {
                        payload.setHttpHeader(httpHeader);
                    }
                    Assert.assertTrue(httpHeader.isValid());
                    Assert.assertNotNull(httpHeader);
                    Assert.assertEquals(httpHeader, payload.httpHeader);
                    Assert.assertEquals(httpHeader, payload.getHttpHeader());

                    in = payload.getInputStreamComplete();
                    Assert.assertEquals( httpHeader.getInputStreamComplete(), payload.getInputStreamComplete() );
                    Assert.assertEquals( srcArr.length, payload.getTotalLength() );
                    Assert.assertEquals( srcArr.length, httpHeader.getTotalLength() );
                    Assert.assertEquals( 8192, payload.getPushbackSize() );

                    dstOut.reset();

                    remaining = payload.getTotalLength();
                    read = 0;
                    while ( remaining > 0 && read != -1 ) {
                        dstOut.write(tmpBuf, 0, read);
                        remaining -= read;

                        // This wont work with buffered streams...
                        //Assert.assertEquals( remaining, payload.getUnavailable() );
                        //Assert.assertEquals( remaining, payload.getRemaining() );

                        read = random.nextInt( 15 ) + 1;
                        read = in.read(tmpBuf, 0, read);
                    }
                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( 0, payload.getUnavailable() );
                    Assert.assertEquals( 0, payload.getRemaining() );

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    Assert.assertFalse(payload.isClosed());
                    in.close();
                    Assert.assertFalse(payload.isClosed());
                    payload.close();
                    Assert.assertTrue(payload.isClosed());

                    Assert.assertEquals( n, closed );

                    in.close();
                    payload.close();
                    /*
                     * Digest.
                     */
                    if ( digestAlgorithm != null ) {
                        Assert.assertNotNull( payload.getMessageDigest() );
                        mdPayload.reset();
                        byte[] digest1 = mdPayload.digest( srcArr );
                        byte[] digest2 = payload.getMessageDigest().digest();
                        Assert.assertArrayEquals( digest1, digest2 );

                        Assert.assertNotNull( httpHeader.getMessageDigest() );
                        mdHttp.reset();
                        byte[] digest3 = mdHttp.digest( payloadArr );
                        byte[] digest4 = httpHeader.getMessageDigest().digest();
                        Assert.assertArrayEquals( digest3, digest4 );
                    } else {
                        Assert.assertNull( payload.getMessageDigest() );
                    }
                } catch (IOException e) {
                    Assert.fail( "Exception not expected!" );
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void payloadClosed() throws IOException {
        ++closed;
    }

}
