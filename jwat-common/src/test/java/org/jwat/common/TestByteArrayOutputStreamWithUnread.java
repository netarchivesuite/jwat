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
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestByteArrayOutputStreamWithUnread {

    private int min;
    private int max;
    private int runs;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 1024, 2}
        });
    }

    public TestByteArrayOutputStreamWithUnread(int min, int max, int runs) {
        this.min = min;
        this.max = max;
        this.runs = runs;
    }

    @Test
    public void test_bytearrayoutputstreamunread() {
        SecureRandom random = new SecureRandom();

        byte[] srcArr = new byte[ 1 ];
        ByteArrayOutputStreamWithUnread dstOut = new ByteArrayOutputStreamWithUnread();
        byte[] dstArr;

        try {
            dstOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        dstOut = new ByteArrayOutputStreamWithUnread( 1024 );

        try {
            dstOut.reset();
            try {
                dstOut.unread( 'a' );
                Assert.fail( "Exception expected!" );
            } catch (IllegalArgumentException e) {
            }

            dstOut.reset();
            dstOut.write( 'a' );
            dstOut.unread( 'a' );
            try {
                dstOut.unread( 'a' );
                Assert.fail( "Exception expected!" );
            } catch (IllegalArgumentException e) {
            }

            byte[] arr = "a".getBytes();

            dstOut.reset();
            try {
                dstOut.unread(arr);
            } catch (IllegalArgumentException e) {
            }

            dstOut.reset();
            dstOut.write(arr);
            dstOut.unread(arr);
            try {
                dstOut.unread(arr);
            } catch (IllegalArgumentException e) {
            }

            arr = "abcd".getBytes();

            dstOut.reset();
            try {
                dstOut.unread(arr, 0, 1);
            } catch (IllegalArgumentException e) {
            }

            dstOut.reset();
            dstOut.write(arr, 0, arr.length);
            dstOut.unread(arr, 0, 4);
            try {
                dstOut.unread(arr, 0, 1);
            } catch (IllegalArgumentException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail( "Exception not expected!" );
        }

        ByteCountingPushBackInputStream in = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 16 );

        long remaining;
        long consumed;
        byte[] tmpBuf = new byte[ 16 ];
        int read;
        int in_mod;
        int out_mod;

        for ( int r=0; r<runs; ++r) {
            for ( int n=min; n<max; ++n ) {
                srcArr = new byte[ n ];
                random.nextBytes( srcArr );

                try {
                    /*
                     * Unread.
                     */
                    in = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 16 );

                    dstOut.reset();
                    dstArr = new byte[ n ];

                    remaining = srcArr.length;
                    consumed = 0;
                    read = 0;
                    in_mod = 2;
                    out_mod = 0;
                    while ( remaining > 0 && read != -1 ) {
                        switch ( in_mod ) {
                        case 0:
                            dstOut.write(read);
                            dstArr[ (int)consumed ] = (byte)read;
                            --remaining;
                            ++consumed;
                            Assert.assertEquals( consumed, in.consumed );
                            break;
                        case 1:
                        case 2:
                            dstOut.write( tmpBuf, 0, read );
                            System.arraycopy( tmpBuf, 0, dstArr, (int)consumed, read );
                            remaining -= read;
                            consumed += read;
                            Assert.assertEquals( consumed, in.consumed );
                            break;
                        }

                        in_mod = (in_mod + 1) % 3;

                        if (in_mod == 1) {
                            switch ( out_mod ) {
                            case 0:
                                in.unread( dstArr[ (int)consumed - 1 ] );
                                dstOut.unread( dstArr[ (int)consumed - 1 ] );
                                ++remaining;
                                --consumed;
                                Assert.assertEquals( consumed, in.consumed );
                                break;
                            case 1:
                                read = tmpBuf.length;
                                if ( read <= consumed ) {
                                    System.arraycopy(dstArr, (int)consumed - read, tmpBuf, 0, read);
                                    in.unread( tmpBuf );
                                    dstOut.unread( tmpBuf );
                                    remaining += read;
                                    consumed -= read;
                                    Assert.assertEquals( consumed, in.consumed );
                                }
                                break;
                            case 2:
                                read = random.nextInt( 15 ) + 1;
                                if ( read <= consumed ) {
                                    System.arraycopy(dstArr, (int)consumed - read, tmpBuf, 0, read);
                                    in.unread( tmpBuf, 0, read );
                                    dstOut.unread( tmpBuf, 0, read );
                                    remaining += read;
                                    consumed -= read;
                                    Assert.assertEquals( consumed, in.consumed );
                                }
                                break;
                            }
                            out_mod = (out_mod + 1) % 3;
                        }

                        switch ( in_mod ) {
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
                    Assert.assertEquals( n, consumed );
                    Assert.assertEquals( n, in.consumed );
                    Assert.assertEquals( n, in.counter );
                    Assert.assertEquals( in.consumed, in.getConsumed() );
                    Assert.assertEquals( in.counter, in.getCounter() );

                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    dstArr = dstOut.toByteArray();

                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Assert.fail( "Exception not expected!" );
                }
            }
        }

        try {
            dstOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
