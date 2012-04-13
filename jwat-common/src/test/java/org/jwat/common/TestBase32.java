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

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.Base16;
import org.jwat.common.Base32;

/**
 * Tests the Base32 encoder/decoder with loads of random data of various
 * lengths to cover all of the code.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestBase32 {

    private int min;
    private int max;
    private int runs;
    private boolean bStrict;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 256, 10, false},
                {1, 256, 10, true}
        });
    }

    public TestBase32(int min, int max, int runs, boolean bStrict) {
        this.min = min;
        this.max = max;
        this.runs = runs;
        this.bStrict = bStrict;
    }

    @Test
    public void test_base32() {
        SecureRandom random = new SecureRandom();

        byte[] srcArr;
        StringBuffer srcSb = new StringBuffer( 256 );
        String srcStr;

        String base32a;
        String base32s;

        byte[] dstArr;
        String dstStr;

        byte[] dstArrLc;
        String dstStrLc;

        String base16sa;
        String base16ss;
        String base16da;
        String base16ds;

        Base32 b32 = new Base32();
        Assert.assertNotNull(b32);

        base32a = Base32.encodeArray( null );
        Assert.assertNull( base32a );
        base32a = Base32.encodeArray( new byte[ 0 ] );
        Assert.assertEquals( "", base32a );

        base32s = Base32.encodeString( null );
        Assert.assertNull( base32s );
        base32s = Base32.encodeString( "" );
        Assert.assertEquals( "", base32s );

        dstArr = Base32.decodeToArray( null, bStrict );
        Assert.assertNull( dstArr );
        dstArr = Base32.decodeToArray( "", bStrict );
        Assert.assertArrayEquals( new byte[0], dstArr );

        dstStr = Base32.decodeToString( null, bStrict );
        Assert.assertNull( dstStr );
        dstStr = Base32.decodeToString( "", bStrict );
        Assert.assertEquals( "", dstStr );

        for ( int r=0; r<runs; ++r ) {
            for ( int n=min; n<max; ++n ) {
                srcArr = new byte[ n ];
                random.nextBytes( srcArr );

                srcSb.setLength( 0 );
                for ( int i=0; i<srcArr.length; ++i ) {
                    srcSb.append( (char)(srcArr[ i ] & 255) );
                }
                srcStr = srcSb.toString();

                base32a = Base32.encodeArray( srcArr );
                base32s = Base32.encodeString( srcStr );

                dstArr = Base32.decodeToArray( base32a, bStrict );
                dstStr = Base32.decodeToString( base32s, bStrict );

                dstArrLc = Base32.decodeToArray( base32a.toLowerCase(), bStrict );
                dstStrLc = Base32.decodeToString( base32s.toLowerCase(), bStrict );

                base16sa = Base16.encodeArray( srcArr );
                base16ss = Base16.encodeString( srcStr );

                base16da = Base16.encodeArray( dstArr );
                base16ds = Base16.encodeString( dstStr );

                /*
                System.out.println( base16sa );
                System.out.println( base16ss );
                System.out.println( base32a );
                System.out.println( base32s );
                System.out.println( base16da );
                System.out.println( base16ds );
                */

                Assert.assertArrayEquals( srcArr, dstArr );
                Assert.assertEquals( base32a, base32s );
                Assert.assertEquals( srcStr, dstStr );
                Assert.assertArrayEquals( dstArr, dstArrLc );
                Assert.assertEquals( dstStr, dstStrLc );
                Assert.assertEquals( base16sa, base16ss );
                Assert.assertEquals( base16da, base16ds );
            }
        }

        /*
         * encode(String)
         */

        srcSb.setLength( 0 );
        srcSb.append( (char)0x100 );
        srcStr = srcSb.toString();
        base32s = Base32.encodeString( srcStr );
        Assert.assertNull( base32s );

        /*
         * decodeToArray
         */

        boolean bValid;
        String base32;
        boolean bStrict;
        Object[][] cases;
        cases = new Object[][] {
                {true, "aaaaaaaa", true},
                {true, "aaaaaaa=", true},
                {false, "aaaaaa==", true},
                {true, "aaaaa===", true},
                {true, "aaaa====", true},
                {false, "aaa=====", true},
                {true, "aa======", true},
                {false, "a=======", true},
                {false, "########", true},
                {false, "#######=", true},
                {false, "######==", true},
                {false, "#####===", true},
                {false, "####====", true},
                {false, "###=====", true},
                {false, "##======", true},
                {false, "#=======", true},

                {true, "aaaaaaaa", false},
                {true, "aaaaaaa=", false},
                {false, "aaaaaa==", false},
                {true, "aaaaa===", false},
                {true, "aaaa====", false},
                {false, "aaa=====", false},
                {true, "aa======", false},
                {false, "a=======", false},
                {false, "########", false},
                {false, "#######=", false},
                {false, "######==", false},
                {false, "#####===", false},
                {false, "####====", false},
                {false, "###=====", false},
                {false, "##======", false},
                {false, "#=======", false},

                {false, "aaaaaaa", true},
                {false, "aaaaaa", true},
                {false, "aaaaa", true},
                {false, "aaaa", true},
                {false, "aaa", true},
                {false, "aa", true},
                {false, "a", true},
                {false, "aaaaaa=", true},
                {false, "aaaaa==", true},
                {false, "aaaaa=", true},
                {false, "aaaa===", true},
                {false, "aaaa==", true},
                {false, "aaaa=", true},
                {false, "aaa====", true},
                {false, "aaa===", true},
                {false, "aaa==", true},
                {false, "aaa=", true},
                {false, "aa=====", true},
                {false, "aa====", true},
                {false, "aa===", true},
                {false, "aa==", true},
                {false, "aa=", true},
                {false, "a======", true},
                {false, "a=====", true},
                {false, "a====", true},
                {false, "a===", true},
                {false, "a==", true},
                {false, "a=", true},
                {false, "#######", true},
                {false, "######", true},
                {false, "#####", true},
                {false, "####", true},
                {false, "###", true},
                {false, "##", true},
                {false, "#", true},
                {false, "######=", true},
                {false, "#####==", true},
                {false, "#####=", true},
                {false, "####===", true},
                {false, "####==", true},
                {false, "####=", true},
                {false, "###====", true},
                {false, "###===", true},
                {false, "###==", true},
                {false, "###=", true},
                {false, "##=====", true},
                {false, "##====", true},
                {false, "##===", true},
                {false, "##==", true},
                {false, "##=", true},
                {false, "#======", true},
                {false, "#=====", true},
                {false, "#====", true},
                {false, "#===", true},
                {false, "#==", true},
                {false, "#=", true},

                {true, "aaaaaaa", false},
                {false, "aaaaaa", false},
                {true, "aaaaa", false},
                {true, "aaaa", false},
                {false, "aaa", false},
                {true, "aa", false},
                {false, "a", false},
                {false, "aaaaaa=", false},
                {true, "aaaaa==", false},
                {true, "aaaaa=", false},
                {true, "aaaa===", false},
                {true, "aaaa==", false},
                {true, "aaaa=", false},
                {false, "aaa====", false},
                {false, "aaa===", false},
                {false, "aaa==", false},
                {false, "aaa=", false},
                {true, "aa=====", false},
                {true, "aa====", false},
                {true, "aa===", false},
                {true, "aa==", false},
                {true, "aa=", false},
                {false, "a======", false},
                {false, "a=====", false},
                {false, "a====", false},
                {false, "a===", false},
                {false, "a==", false},
                {false, "a=", false},
                {false, "#######", false},
                {false, "######", false},
                {false, "#####", false},
                {false, "####", false},
                {false, "###", false},
                {false, "##", false},
                {false, "#", false},
                {false, "######=", false},
                {false, "#####==", false},
                {false, "#####=", false},
                {false, "####===", false},
                {false, "####==", false},
                {false, "####=", false},
                {false, "###====", false},
                {false, "###===", false},
                {false, "###==", false},
                {false, "###=", false},
                {false, "##=====", false},
                {false, "##====", false},
                {false, "##===", false},
                {false, "##==", false},
                {false, "##=", false},
                {false, "#======", false},
                {false, "#=====", false},
                {false, "#====", false},
                {false, "#===", false},
                {false, "#==", false},
                {false, "#=", false}
        };
        for (int i=0; i<cases.length; ++i) {
            bValid = (Boolean)cases[i][0];
            base32 = (String)cases[i][1];
            bStrict = (Boolean)cases[i][2];
            dstArr = Base32.decodeToArray( base32, bStrict );
            // debug
            //System.out.println((dstArr != null) + " - " + base32 + " - " +  bStrict);
            if (bValid) {
                Assert.assertNotNull( dstArr );
            } else {
                Assert.assertNull( dstArr );
            }
        }

        /*
         * decodeToArray
         */

        cases = new Object[][] {
                {true, "aaaaaaaa", true},
                {true, "aaaaaaa=", true},
                {false, "aaaaaa==", true},
                {true, "aaaaa===", true},
                {true, "aaaa====", true},
                {false, "aaa=====", true},
                {true, "aa======", true},
                {false, "a=======", true},
                {false, "########", true},
                {false, "#######=", true},
                {false, "######==", true},
                {false, "#####===", true},
                {false, "####====", true},
                {false, "###=====", true},
                {false, "##======", true},
                {false, "#=======", true},

                {true, "aaaaaaaa", false},
                {true, "aaaaaaa=", false},
                {false, "aaaaaa==", false},
                {true, "aaaaa===", false},
                {true, "aaaa====", false},
                {false, "aaa=====", false},
                {true, "aa======", false},
                {false, "a=======", false},
                {false, "########", false},
                {false, "#######=", false},
                {false, "######==", false},
                {false, "#####===", false},
                {false, "####====", false},
                {false, "###=====", false},
                {false, "##======", false},
                {false, "#=======", false},

                {false, "aaaaaaa", true},
                {false, "aaaaaa", true},
                {false, "aaaaa", true},
                {false, "aaaa", true},
                {false, "aaa", true},
                {false, "aa", true},
                {false, "a", true},
                {false, "aaaaaa=", true},
                {false, "aaaaa==", true},
                {false, "aaaaa=", true},
                {false, "aaaa===", true},
                {false, "aaaa==", true},
                {false, "aaaa=", true},
                {false, "aaa====", true},
                {false, "aaa===", true},
                {false, "aaa==", true},
                {false, "aaa=", true},
                {false, "aa=====", true},
                {false, "aa====", true},
                {false, "aa===", true},
                {false, "aa==", true},
                {false, "aa=", true},
                {false, "a======", true},
                {false, "a=====", true},
                {false, "a====", true},
                {false, "a===", true},
                {false, "a==", true},
                {false, "a=", true},
                {false, "#######", true},
                {false, "######", true},
                {false, "#####", true},
                {false, "####", true},
                {false, "###", true},
                {false, "##", true},
                {false, "#", true},
                {false, "######=", true},
                {false, "#####==", true},
                {false, "#####=", true},
                {false, "####===", true},
                {false, "####==", true},
                {false, "####=", true},
                {false, "###====", true},
                {false, "###===", true},
                {false, "###==", true},
                {false, "###=", true},
                {false, "##=====", true},
                {false, "##====", true},
                {false, "##===", true},
                {false, "##==", true},
                {false, "##=", true},
                {false, "#======", true},
                {false, "#=====", true},
                {false, "#====", true},
                {false, "#===", true},
                {false, "#==", true},
                {false, "#=", true},

                {true, "aaaaaaa", false},
                {false, "aaaaaa", false},
                {true, "aaaaa", false},
                {true, "aaaa", false},
                {false, "aaa", false},
                {true, "aa", false},
                {false, "a", false},
                {false, "aaaaaa=", false},
                {true, "aaaaa==", false},
                {true, "aaaaa=", false},
                {true, "aaaa===", false},
                {true, "aaaa==", false},
                {true, "aaaa=", false},
                {false, "aaa====", false},
                {false, "aaa===", false},
                {false, "aaa==", false},
                {false, "aaa=", false},
                {true, "aa=====", false},
                {true, "aa====", false},
                {true, "aa===", false},
                {true, "aa==", false},
                {true, "aa=", false},
                {false, "a======", false},
                {false, "a=====", false},
                {false, "a====", false},
                {false, "a===", false},
                {false, "a==", false},
                {false, "a=", false},
                {false, "#######", false},
                {false, "######", false},
                {false, "#####", false},
                {false, "####", false},
                {false, "###", false},
                {false, "##", false},
                {false, "#", false},
                {false, "######=", false},
                {false, "#####==", false},
                {false, "#####=", false},
                {false, "####===", false},
                {false, "####==", false},
                {false, "####=", false},
                {false, "###====", false},
                {false, "###===", false},
                {false, "###==", false},
                {false, "###=", false},
                {false, "##=====", false},
                {false, "##====", false},
                {false, "##===", false},
                {false, "##==", false},
                {false, "##=", false},
                {false, "#======", false},
                {false, "#=====", false},
                {false, "#====", false},
                {false, "#===", false},
                {false, "#==", false},
                {false, "#=", false}
        };
        for (int i=0; i<cases.length; ++i) {
            bValid = (Boolean)cases[i][0];
            base32 = (String)cases[i][1];
            bStrict =  (Boolean)cases[i][2];
            dstStr = Base32.decodeToString( base32, bStrict );
            // debug
            // System.out.println((dstStr != null) + " - " + base32 + " - " + bStrict);
            if (bValid) {
                Assert.assertNotNull( dstStr );
            } else {
                Assert.assertNull( dstStr );
            }
        }
    }

}
