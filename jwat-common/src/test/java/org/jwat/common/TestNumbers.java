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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestNumbers {

    @Test
    public void test_numbers() {
        //System.out.println(Integer.MAX_VALUE);
        //System.out.println(Long.MAX_VALUE);
        int[] islArrCases = new int[] {
                0,
                0,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000
        };
        //      2147483647
        Assert.assertEquals(islArrCases.length, Numbers.islArr.length);
        for (int i=0; i<Numbers.islArr.length; ++i) {
            Assert.assertEquals(islArrCases[i], Numbers.islArr[i]);
        }
        int[][] islCases = new int[][] {
            {0, 0},
            {8, 1},
            {8, 10},
            {8, 100},
            {8, 1000},
            {8, 10000},
            {8, 100000},
            {8, 1000000},
            {8, 10000000},
            {8, 100000000},
            {1, 1000000000}
        };
        //      2147483647
        Assert.assertEquals(1, Numbers.intStrLen(0));
        int iv;
        int it;
        for (int i=1; i<islCases.length; ++i) {
            it = islCases[i][0];
            iv = islCases[i][1];
            // Debug
            //System.out.println(iv);
            Assert.assertEquals(i, Numbers.intStrLen(iv));
            while (it > 0) {
                iv += islCases[i][1];
                // Debug
                //System.out.println(iv);
                Assert.assertEquals(i, Numbers.intStrLen(iv));
                --it;
            }
        }
        long[] lslArrCases = new long[] {
                0,
                0,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000,
                10000000000L,
                100000000000L,
                1000000000000L,
                10000000000000L,
                100000000000000L,
                1000000000000000L,
                10000000000000000L,
                100000000000000000L,
                1000000000000000000L
        };
        //        9223372036854775807
        Assert.assertEquals(lslArrCases.length, Numbers.lslArr.length);
        for (int i=0; i<Numbers.lslArr.length; ++i) {
            Assert.assertEquals(lslArrCases[i], Numbers.lslArr[i]);
        }
        Assert.assertEquals(1, Numbers.intStrLen(0));
        long lv;
        for (int i=1; i<lslArrCases.length; ++i) {
            it = 8;
            lv = lslArrCases[i];
            // Debug
            //System.out.println(lv);
            Assert.assertEquals(i, Numbers.longStrLen(lv));
            while (it > 0) {
                lv += lslArrCases[i];
                // Debug
                //System.out.println(lv);
                Assert.assertEquals(i, Numbers.longStrLen(lv));
                --it;
            }
        }

        int[][] tzCases = new int[][] {
            {0, 0},
            {0, 1},
            {1, 10},
            {2, 100},
            {3, 1000},
            {4, 10000},
            {5, 100000},
            {6, 1000000},
            {7, 10000000},
            {8, 100000000},
            {9, 1000000000}
        };
        for (int i=0; i<tzCases.length; ++i) {
            int tz = (int)tzCases[i][0];
            int intVal = (int)tzCases[i][1];
            Assert.assertEquals(tz, Numbers.intTrailingZeros(intVal));
        }
    }

    @Test
    public void test_number_string_length() {
        Assert.assertEquals(11, Numbers.islArr.length);
        Assert.assertEquals(20, Numbers.lslArr.length);
        for (int i=0; i<10; ++i) {
        }
    }

    @Test
    @Ignore
    public void test_divu10_hack() {
        int q, r;
        int res;
        for (int n=0; n<100000000; ++n) {
            q = (n >> 1) + (n >> 2);        // q=n/2+n/4 = 3n/4
            q = q + (q >> 4);               // q=3n/4+(3n/4)/16 = 3n/4+3n/64 = 51n/64
            q = q + (q >> 8);               // q=51n/64+(51n/64)/256 = 51n/64 + 51n/16384 = 13107n/16384
            q = q + (q >> 16);              // q= 13107n/16384+(13107n/16384)/65536=13107n/16348+13107n/1073741824=858993458n/1073741824
            // note: q is now roughly 0.8n
            q = q >> 3;                     // q=n/8 = (about 0.1n or n/10)
            r = n - (((q << 2) + q) << 1);  // rounding: r= n-2*(n/10*4+n/10)=n-2*5n/10=n-10n/10
            res = q + ((r > 9) ? 1 : 0);    // adjust answer by error term
            if (res != n / 10) {
                Assert.fail("Precision fail!");
            }
            if (Numbers.divu10gcc(n) != n / 10) {
                Assert.fail("Precision fail!");
            }
        }

        long ctm1 = System.currentTimeMillis();
        int r2 = 0;
        for (int n=0; n<100000000; ++n) {
            q = n / 10;
            r2 += q;
        }
        long ctm2 = System.currentTimeMillis();
        int r1 = 0;
        for (int n=0; n<100000000; ++n) {
            q = (n >> 1) + (n >> 2);        // q=n/2+n/4 = 3n/4
            q = q + (q >> 4);               // q=3n/4+(3n/4)/16 = 3n/4+3n/64 = 51n/64
            q = q + (q >> 8);               // q=51n/64+(51n/64)/256 = 51n/64 + 51n/16384 = 13107n/16384
            q = q + (q >> 16);              // q= 13107n/16384+(13107n/16384)/65536=13107n/16348+13107n/1073741824=858993458n/1073741824
            // note: q is now roughly 0.8n
            q = q >> 3;                     // q=n/8 = (about 0.1n or n/10)
            r = n - (((q << 2) + q) << 1);  // rounding: r= n-2*(n/10*4+n/10)=n-2*5n/10=n-10n/10
            r1 += q + ((r > 9) ? 1 : 0);    // adjust answer by error term
        }
        long ctm3 = System.currentTimeMillis();
        int r3 = 0;
        for (int n=0; n<100000000; ++n) {
            q = Numbers.divu10hd(n);
            r3 += q;
        }
        long ctm4 = System.currentTimeMillis();
        int r4 = 0;
        for (int n=0; n<100000000; ++n) {
            q = Numbers.divu10gcc(n);
            r4 += q;
        }
        long ctm5 = System.currentTimeMillis();
        System.out.println(ctm2 - ctm1);
        System.out.println(ctm3 - ctm2);
        System.out.println(ctm4 - ctm3);
        System.out.println(ctm5 - ctm4);
        System.out.println(r1);
        System.out.println(r2);
        System.out.println(r3);
        System.out.println(r4);

        //ctm = System.currentTimeMillis();
        int n = 12340000;
        q = (n >> 1) + (n >> 2);        // q=n/2+n/4 = 3n/4
        q = q + (q >> 4);               // q=3n/4+(3n/4)/16 = 3n/4+3n/64 = 51n/64
        q = q + (q >> 8);               // q=51n/64+(51n/64)/256 = 51n/64 + 51n/16384 = 13107n/16384
        q = q + (q >> 16);              // q= 13107n/16384+(13107n/16384)/65536=13107n/16348+13107n/1073741824=858993458n/1073741824
        // note: q is now roughly 0.8n
        q = q >> 3;                     // q=n/8 = (about 0.1n or n/10)
        r = n - (((q << 2) + q) << 1);  // rounding: r= n-2*(n/10*4+n/10)=n-2*5n/10=n-10n/10
        res = q + ((r > 9) ? 1 : 0);    // adjust answer by error term
        System.out.println(res);
    }

}
