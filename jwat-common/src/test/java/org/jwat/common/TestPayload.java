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
import org.jwat.common.Payload;
import org.jwat.common.PayloadOnClosedHandler;

@RunWith(Parameterized.class)
public class TestPayload implements PayloadOnClosedHandler {

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

    public TestPayload(int min, int max, int runs, String digestAlgorithm) {
        this.min = min;
        this.max = max;
        this.runs = runs;
        this.digestAlgorithm = digestAlgorithm;
    }

    public int closed = 0;

    @Test
    public void test_payload() {
        SecureRandom random = new SecureRandom();

        byte[] srcArr = new byte[ 0 ];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] dstArr;

        Payload payload;

        try {
            payload = Payload.processPayload( null, 0, 16, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            payload = Payload.processPayload( new ByteArrayInputStream( srcArr ), -1, 16, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            payload = Payload.processPayload( new ByteArrayInputStream( srcArr ), 0, -1, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            payload = Payload.processPayload( new ByteArrayInputStream( srcArr ), 0, 0, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            payload = Payload.processPayload( new ByteArrayInputStream( srcArr ), 0, 16, "shit1" );
            Assert.assertNull( payload.getMessageDigest() );
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            closed = 0;
            for ( int n=min; n<max; ++n ) {
                srcArr = new byte[ n ];
                random.nextBytes( srcArr );

                out.reset();

                try {
                    /*
                     * Payload.
                     */
                    payload = Payload.processPayload( new ByteArrayInputStream( srcArr ), srcArr.length, 16, digestAlgorithm );
                    payload.setOnClosedHandler( this );

                    Assert.assertNull( payload.getHttpResponse() );
                    payload.setHttpResponse( null );
                    Assert.assertNull( payload.getHttpResponse() );

                    in = payload.getInputStream();
                    Assert.assertEquals( payload.getInputStreamComplete(), payload.getInputStream() );
                    Assert.assertEquals( srcArr.length, payload.getTotalLength() );
                    Assert.assertEquals( 16, payload.getPushbackSize() );

                    remaining = payload.getTotalLength();
                    read = 0;
                    while ( remaining > 0 && read != -1 ) {
                        out.write(tmpBuf, 0, read);
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

                    dstArr = out.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    payload.close();
                    Assert.assertEquals( n, closed );
                    /*
                     * Digest.
                     */
                    if ( digestAlgorithm != null ) {
                        Assert.assertNotNull( payload.getMessageDigest() );

                        byte[] digest1 = payload.getMessageDigest().digest();

                        md.reset();
                        byte[] digest2 = md.digest( srcArr );

                        Assert.assertArrayEquals( digest1, digest2 );
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
