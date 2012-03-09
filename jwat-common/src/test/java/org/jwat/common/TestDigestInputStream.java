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
public class TestDigestInputStream {

    private int min;
    private int max;
    private int runs;
    private String digestAlgorithm;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 1024, 2, "sha1"}
        });
    }

    public TestDigestInputStream(int min, int max, int runs, String digestAlgorithm) {
        this.min = min;
        this.max = max;
        this.runs = runs;
        this.digestAlgorithm = digestAlgorithm;
    }

    @Test
    public void test_inputstream_digest() {
        SecureRandom random = new SecureRandom();

        byte[] srcArr = new byte[ 0 ];
        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance( digestAlgorithm );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        DigestInputStreamNoSkip in;

        in = new DigestInputStreamNoSkip( null, null );
        Assert.assertNotNull( in );

        in = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), null );
        Assert.assertNotNull( in );

        in = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), md );
        Assert.assertNotNull( in );

        in = new DigestInputStreamNoSkip( null, md );
        Assert.assertNotNull( in );

        Assert.assertFalse( in.markSupported() );
        in.mark( 1 );
        try {
            in.reset();
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            Assert.fail( "Exception expected!" );
        } catch (UnsupportedOperationException e) {
        }

        long remaining;
        byte[] tmpBuf = new byte[ 16 ];
        int read;
        int mod;

        for ( int r=0; r<runs; ++r) {
            for ( int n=min; n<max; ++n ) {
                srcArr = new byte[ n ];
                random.nextBytes( srcArr );

                try {
                    /*
                     * Read.
                     */
                    md.reset();
                    byte[] digest1 = md.digest( srcArr );

                    md.reset();
                    in = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), md );

                    dstOut.reset();

                    remaining = srcArr.length;
                    read = 0;
                    mod = 2;
                    while ( remaining > 0 && read != -1 ) {
                        switch ( mod ) {
                        case 0:
                            dstOut.write( read );
                            --remaining;
                            break;
                        case 1:
                        case 2:
                            dstOut.write( tmpBuf, 0, read );
                            remaining -= read;
                            break;
                        }

                        mod = (mod + 1) % 3;

                        switch ( mod ) {
                        case 0:
                            read = in.read();
                            break;
                        case 1:
                            read = in.read( tmpBuf );
                            break;
                        case 2:
                            read = random.nextInt( 15 ) + 1;
                            read = in.read( tmpBuf, 0, read );
                            break;
                        }
                    }

                    Assert.assertEquals( 0, remaining );

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    in.close();

                    byte[] digest2 = md.digest();

                    Assert.assertArrayEquals( digest1, digest2 );

                    md.reset();
                    byte[] digest3 = md.digest( dstArr );

                    Assert.assertArrayEquals( digest1, digest3 );
                    /*
                     * Skip.
                     */
                    md.reset();
                    in = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), md );

                    dstOut.reset();

                    remaining = srcArr.length;
                    read = 0;
                    mod = 3;
                    int skipped = 0;
                    while ( remaining > 0 && read != -1 ) {
                        switch ( mod ) {
                        case 0:
                            dstOut.write( read );
                            --remaining;
                            break;
                        case 1:
                        case 2:
                            dstOut.write( tmpBuf, 0, read );
                            remaining -= read;
                            break;
                        case 3:
                            remaining -= read;
                            skipped += read;
                            break;
                        }

                        mod = (mod + 1) % 4;

                        switch ( mod ) {
                        case 0:
                            read = in.read();
                            break;
                        case 1:
                            read = in.read( tmpBuf );
                            break;
                        case 2:
                            read = random.nextInt( 15 ) + 1;
                            read = in.read( tmpBuf, 0, read );
                            break;
                        case 3:
                            read = random.nextInt( 15 ) + 1;
                            read = (int)in.skip( read );
                            break;
                        }
                    }

                    Assert.assertEquals( 0, remaining );

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length + skipped );

                    in.close();

                    byte[] digest4 = md.digest();

                    Assert.assertArrayEquals( digest1, digest4 );
                } catch (IOException e) {
                    Assert.fail( "Exception not expected!" );
                    e.printStackTrace();
                }
            }
        }
    }

}
