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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to work with numbers and their string representation.
 *
 * @author nicl
 */
public class Numbers {

    /** Integer array for integer string length binary search. */
    protected static int[] islArr;

    /** Long array for long string length binary search. */
    protected static long[] lslArr;

    /*
     * Initialize the internal comparison arrays for binary search.
     */
    static {
        boolean b;
        List<Integer> islList = new ArrayList<Integer>();
        islList.add(0);
        islList.add(0);
        int i = 1;
        int li;
        int lm = Integer.MAX_VALUE;
        b = true;
        while (b) {
            if (i < lm) {
                li = i;
                i = (i << 3) + (i << 1);
                if (i / 10 == li) {
                    islList.add(i);
                }
                else {
                    b = false;
                }
            }
            else {
                b = false;
            }
        }
        islArr = new int[islList.size()];
        for (i=0; i<islList.size(); ++i) {
            islArr[i] = islList.get(i);
            //System.out.println(islList.get(i));
        }
        List<Long> lslList = new ArrayList<Long>();
        lslList.add(0L);
        lslList.add(0L);
        long l = 1;
        long ll;
        long ml = Long.MAX_VALUE;
        b = true;
        while (b) {
            if (l < ml) {
                ll = l;
                l = (l << 3) + (l << 1);
                if (l / 10 == ll) {
                    lslList.add(l);
                }
                else {
                    b = false;
                }
            }
            else {
                b = false;
            }
        }
        lslArr = new long[lslList.size()];
        for (i=0; i<lslList.size(); ++i) {
            lslArr[i] = lslList.get(i);
            //System.out.println(lslList.get(i));
        }
    }

    /**
     * Find the string length of the integer without leading zeroes.
     * @param i integer value
     * @return string length of the integer
     */
    public static int intStrLen(int i) {
        int min = 1;
        int max = 10;
        int idx = 0;
        int c;
        boolean bLoop = true;
        while (bLoop) {
            idx = min + ((max - min + 1) >> 1);
            //System.out.println(l + " " + min + " " + max + " " + islArr[min] + " " + i + " " + islArr[max]);
            c = islArr[idx];
            if (i < c) {
                max = --idx;
            }
            else {
                min = idx;
            }
            bLoop = (max - min) != 0;
            //System.out.println(l + " " + min + " " + max + " " + islArr[min] + " " + i + " " + islArr[max]);
        }
        return idx;
    }

    /**
     * Find the string length of the long without leading zeroes.
     * @param l long value
     * @return string length of the long
     */
    public static int longStrLen(long l) {
        int min = 1;
        int max = 19;
        int idx = 0;
        long c;
        boolean bLoop = true;
        while (bLoop) {
            idx = min + ((max - min + 1) >> 1);
            //System.out.println(l + " " + min + " " + max + " " + lslArr[min] + " " + i + " " + lslArr[max]);
            c = lslArr[idx];
            if (l < c) {
                max = --idx;
            }
            else {
                min = idx;
            }
            bLoop = (max - min) != 0;
            //System.out.println(l + " " + min + " " + max + " " + lslArr[min] + " " + i + " " + lslArr[max]);
        }
        return idx;
    }

    /**
     * Returns the number of trailing zeroes in an integer, or zero if the number is 0.
     * @param i number to count trailing zeroes in
     * @return the number of trailing zeroes in an integer, or zero if the number is 0
     */
    public static int intTrailingZeros(int i) {
        int cnt = 0;
        if (i != 0) {
            while (i % 10 == 0) {
                i = i / 10;
                ++cnt;
            }
        }
        return cnt;
    }

    /**
     * Divu10 from Hackers Delight.
     * @param n integer to divide by 10
     * @return integer divided by 10
     */
    public static int divu10hd(int n) {
        int q, r;
        q = (n >> 1) + (n >> 2);        // q=n/2+n/4 = 3n/4
        q = q + (q >> 4);               // q=3n/4+(3n/4)/16 = 3n/4+3n/64 = 51n/64
        q = q + (q >> 8);               // q=51n/64+(51n/64)/256 = 51n/64 + 51n/16384 = 13107n/16384
        q = q + (q >> 16);              // q= 13107n/16384+(13107n/16384)/65536=13107n/16348+13107n/1073741824=858993458n/1073741824
        // note: q is now roughly 0.8n
        q = q >> 3;                     // q=n/8 = (about 0.1n or n/10)
        r = n - (((q << 2) + q) << 1);  // rounding: r= n-2*(n/10*4+n/10)=n-2*5n/10=n-10n/10
        return q + ((r > 9) ? 1 : 0);   // adjust answer by error term
    }

    /**
     * Divu10 from GNU GCC.
     * @param n integer to divide by 10
     * @return integer divided by 10
     */
    public static int divu10gcc(int n) {
        return (int)((((long)n) * 0xcccccccdL) >>> 35);
    }

    /* This does not seem to work in Java, most likely dues to missing unsigned multiplication.
    public static long divu10(long l) {
        return (l * 0xcccccccccccccccdL) >>> 3;
    }
    */

}
