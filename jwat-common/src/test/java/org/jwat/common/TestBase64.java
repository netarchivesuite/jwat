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
import org.jwat.common.Base64;

/**
 * Tests the Base64 encoder/decoder with loads of random data of various
 * lengths to cover all of the code.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestBase64 {

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

    public TestBase64(int min, int max, int runs, boolean bStrict) {
        this.min = min;
        this.max = max;
        this.runs = runs;
        this.bStrict = bStrict;
    }

    @Test
    public void test_base64() {
        SecureRandom random = new SecureRandom();

        byte[] srcArr;
        StringBuffer srcSb = new StringBuffer( 256 );
        String srcStr;

        String base64a;
        String base64s;

        byte[] dstArr;
        String dstStr;

        String base16sa;
        String base16ss;
        String base16da;
        String base16ds;

        Base64 b64 = new Base64();
        Assert.assertNotNull(b64);

        base64a = Base64.encodeArray( null );
        Assert.assertNull( base64a );
        base64a = Base64.encodeArray( new byte[ 0 ] );
        Assert.assertEquals( "", base64a );

        base64s = Base64.encodeString( null );
        Assert.assertNull( base64s );
        base64s = Base64.encodeString( "" );
        Assert.assertEquals( "", base64s );

        dstArr = Base64.decodeToArray( null, bStrict );
        Assert.assertNull( dstArr );
        dstArr = Base64.decodeToArray( "", bStrict );
        Assert.assertArrayEquals( new byte[0], dstArr );

        dstStr = Base64.decodeToString( null, bStrict );
        Assert.assertNull( dstStr );
        dstStr = Base64.decodeToString( "", bStrict );
        Assert.assertEquals( "", dstStr );

        for ( int r=0; r<runs; ++r) {
            for ( int n=min; n<max; ++n ) {
                srcArr = new byte[ n ];
                random.nextBytes( srcArr );

                srcSb.setLength( 0 );
                for ( int i=0; i<srcArr.length; ++i ) {
                    srcSb.append( (char)(srcArr[ i ] & 255) );
                }
                srcStr = srcSb.toString();

                base64a = Base64.encodeArray( srcArr );
                base64s = Base64.encodeString( srcStr );

                dstArr = Base64.decodeToArray( base64a, bStrict );
                dstStr = Base64.decodeToString( base64s, bStrict );

                base16sa = Base16.encodeArray( srcArr );
                base16ss = Base16.encodeString( srcStr );

                base16da = Base16.encodeArray( dstArr );
                base16ds = Base16.encodeString( dstStr );

                /*
                System.out.println( base16sa );
                System.out.println( base16ss );
                System.out.println( base64a );
                System.out.println( base64s );
                System.out.println( base16da );
                System.out.println( base16ds );
                */

                Assert.assertArrayEquals( srcArr, dstArr );
                Assert.assertEquals( base64a, base64s );
                Assert.assertEquals( srcStr, dstStr );
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
        base64s = Base64.encodeString( srcStr );
        Assert.assertNull( base64s );

        /*
         * decodeToArray
         */

        boolean bValid;
        String base64;
        boolean bStrict;
        Object[][] cases;
        cases = new Object[][] {
                {true, "aaaa", true},
                {true, "aaa=", true},
                {true, "aa==", true},
                {false, "a===", true},
                {false, "####", true},
                {false, "###=", true},
                {false, "##==", true},
                {false, "#===", true},
                {true, "aaaa", false},
                {true, "aaa=", false},
                {true, "aa==", false},
                {false, "a===", false},
                {false, "####", false},
                {false, "###=", false},
                {false, "##==", false},
                {false, "#===", false},
                {false, "aaa", true},
                {false, "aa", true},
                {false, "a", true},
                {false, "aa=", true},
                {false, "a==", true},
                {false, "a=", true},
                {false, "###", true},
                {false, "##", true},
                {false, "#", true},
                {false, "##=", true},
                {false, "#==", true},
                {false, "#=", true},
                {true, "aaa", false},
                {true, "aa", false},
                {false, "a", false},
                {true, "aa=", false},
                {false, "a==", false},
                {false, "a=", false},
                {false, "###", false},
                {false, "##", false},
                {false, "#", false},
                {false, "##=", false},
                {false, "#==", false},
                {false, "#=", false}
        };
        for (int i=0; i<cases.length; ++i) {
            bValid = (Boolean)cases[i][0];
            base64 = (String)cases[i][1];
            bStrict = (Boolean)cases[i][2];
            dstArr = Base64.decodeToArray(base64, bStrict);
            //System.out.println((dstArr != null) + " - " + base64 + " - " +  bStrict);
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
                {true, "aaaa", true},
                {true, "aaa=", true},
                {true, "aa==", true},
                {false, "a===", true},
                {false, "####", true},
                {false, "###=", true},
                {false, "##==", true},
                {false, "#===", true},
                {true, "aaaa", false},
                {true, "aaa=", false},
                {true, "aa==", false},
                {false, "a===", false},
                {false, "####", false},
                {false, "###=", false},
                {false, "##==", false},
                {false, "#===", false},
                {false, "aaa", true},
                {false, "aa", true},
                {false, "a", true},
                {false, "aa=", true},
                {false, "a==", true},
                {false, "a=", true},
                {false, "###", true},
                {false, "##", true},
                {false, "#", true},
                {false, "##=", true},
                {false, "#==", true},
                {false, "#=", true},
                {true, "aaa", false},
                {true, "aa", false},
                {false, "a", false},
                {true, "aa=", false},
                {false, "a==", false},
                {false, "a=", false},
                {false, "###", false},
                {false, "##", false},
                {false, "#", false},
                {false, "##=", false},
                {false, "#==", false},
                {false, "#=", false}
        };
        for (int i=0; i<cases.length; ++i) {
            bValid = (Boolean)cases[i][0];
            base64 = (String)cases[i][1];
            bStrict =  (Boolean)cases[i][2];
            dstStr = Base64.decodeToString(base64, bStrict);
            //System.out.println((dstStr != null) + " - " + base64 + " - " + bStrict);
            if (bValid) {
                Assert.assertNotNull( dstStr );
            } else {
                Assert.assertNull( dstStr );
            }
        }
    }

}
