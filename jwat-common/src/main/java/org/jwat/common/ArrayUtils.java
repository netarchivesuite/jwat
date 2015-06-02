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

import java.util.LinkedList;
import java.util.List;

/**
 * Common byte array helper methods.
 *
 * @author nicl
 */
public class ArrayUtils {

    /** Case sensitive array where [i] == i. */
    protected static final byte[] CASE_SENSITIVE = new byte[256];
    /** Case insensitive array where [i] == toLowerCase(i). */
    protected static final byte[] CASE_INSENSITIVE = new byte[256];

    /** Array where the character equal to the index is 1 only for whitespace characters. **/
    public static final byte[] SKIP_WHITESPACE = new byte[256];
    /** Array where the character equal to the index is 1 only for non whitespace characters. **/
    public static final byte[] SKIP_NONWHITESPACE = new byte[256];

    /** Zero length byte array. */
    protected static final byte[] zeroArr = new byte[0];

    /**
     * Initialize static arrays.
     */
    static {
        for (int i=0; i<256; ++i) {
            CASE_SENSITIVE[i] = (byte)i;
            CASE_INSENSITIVE[i] = (byte)Character.toLowerCase(i);
            SKIP_WHITESPACE[i] = 0;
            SKIP_NONWHITESPACE[i] = 1;
        }
        SKIP_WHITESPACE[' '] = 1;
        SKIP_WHITESPACE['\t'] = 1;
        SKIP_WHITESPACE['\r'] = 1;
        SKIP_WHITESPACE['\n'] = 1;
        SKIP_NONWHITESPACE[' '] = 0;
        SKIP_NONWHITESPACE['\t'] = 0;
        SKIP_NONWHITESPACE['\r'] = 0;
        SKIP_NONWHITESPACE['\n'] = 0;
    }

    /**
     * Purely for unit testing.
     */
    protected ArrayUtils() {
    }

    /**
     * Move index forward according to the values in the byte array and the skip byte array which decides which byte values must be skipped.
     * @param skip byte array deciding which byte values must be skipped
     * @param arr byte array to skip in
     * @param fIdx byte array index
     * @return updated index depending of how many bytes where skipped
     */
    public static int skip(byte[] skip, byte[] arr, int fIdx) {
        int arrLen = arr.length;
        while (fIdx < arrLen && skip[arr[fIdx] & 255] == 1) {
            ++fIdx;
        }
        return fIdx;
    }

    /**
     * Check if a byte array starts with a specified case sensitive sub byte array.
     * @param subArr case sensitive sub byte array to compare for
     * @param arr byte array to look in
     * @return boolean indicating if there was a match
     */
    public static boolean startsWith(byte[] subArr, byte[] arr) {
        boolean bRes = false;
        int lIdx = subArr.length - 1;
        if (lIdx < arr.length) {
            if (subArr[0] == arr[0]) {
                while (lIdx > 0 && subArr[lIdx] == arr[lIdx]) {
                    --lIdx;
                }
                bRes = (lIdx == 0);
            }
        }
        return bRes;
    }

    /**
     * Check if a byte array starts with a specified case insensitive sub byte array.
     * @param subArr case insensitive sub byte array to compare for
     * @param arr byte array to look in
     * @return boolean indicating if there was a match
     */
    public static boolean startsWithIgnoreCase(byte[] subArr, byte[] arr) {
        boolean bRes = false;
        int lIdx = subArr.length - 1;
        if (lIdx < arr.length) {
            if (CASE_INSENSITIVE[subArr[0]] == CASE_INSENSITIVE[arr[0]]) {
                while (lIdx > 0 && CASE_INSENSITIVE[subArr[lIdx]] == CASE_INSENSITIVE[arr[lIdx]]) {
                    --lIdx;
                }
                bRes = (lIdx == 0);
            }
        }
        return bRes;
    }

    /**
     * Check if a byte array matches a specified case sensitive sub byte array at a certain index.
     * @param subArr case sensitive sub byte array to compare against
     * @param arr byte array to look in
     * @return boolean indicating if there was a match
     */
    public static boolean equalsAt(byte[] subArr, byte[] arr, int fIdx) {
        boolean bRes = false;
        int lIdx = subArr.length - 1;
        int tIdx = fIdx + lIdx;
        if (tIdx < arr.length) {
            if (subArr[0] == arr[fIdx]) {
                while (lIdx > 0 && subArr[lIdx] == arr[tIdx]) {
                    --lIdx;
                    --tIdx;
                }
                bRes = (lIdx == 0);
            }
        }
        return bRes;
    }

    /**
     * Check if a byte array matches a specified case insensitive sub byte array at a certain index.
     * @param subArr case sensitive sub byte array to compare against
     * @param arr byte array to look in
     * @return boolean indicating if there was a match
     */
    public static boolean equalsAtIgnoreCase(byte[] subArr, byte[] arr, int fIdx) {
        boolean bRes = false;
        int lIdx = subArr.length - 1;
        int tIdx = fIdx + lIdx;
        if (tIdx < arr.length) {
            if (CASE_INSENSITIVE[subArr[0] & 255] == CASE_INSENSITIVE[arr[fIdx] & 255]) {
                while (lIdx > 0 && CASE_INSENSITIVE[subArr[lIdx] & 255] == CASE_INSENSITIVE[arr[tIdx] & 255]) {
                    --lIdx;
                    --tIdx;
                }
                bRes = (lIdx == 0);
            }
        }
        return bRes;
    }

    /**
     * Find the next case sensitive occurrence of a byte sub array in a larger byte array beginning from a specific index.
     * @param subArr case sensitive byte sub array to compare against
     * @param arr byte array to look in
     * @param fIdx index to starting looking from
     * @return next occurrence or -1, if none was found
     */
    public static int indexOf(byte[] subArr, byte[] arr, int fIdx) {
        int csIdx;
        int caIdx;
        int idx = -1;
        int subArrLast = subArr.length - 1;
        int arrLen = arr.length;
        int lIdx = fIdx + subArrLast;
        if (subArrLast > 0) {
            while (lIdx < arrLen && idx == -1) {
                if (subArr[0] == arr[fIdx]) {
                    csIdx = subArrLast;
                    caIdx = lIdx;
                    while (csIdx > 0 && subArr[csIdx] == arr[caIdx]) {
                        --csIdx;
                        --caIdx;
                    }
                    if (csIdx == 0) {
                        idx = fIdx;
                    }
                }
                ++fIdx;
                ++lIdx;
            }
        }
        else if (subArrLast == 0) {
            while (fIdx < arrLen && idx == -1) {
                if (subArr[0] == arr[fIdx]) {
                    idx = fIdx;
                }
                ++fIdx;
            }
        }
        return idx;
    }

    /**
     * Find the next case insensitive occurrence of a byte sub array in a larger byte array beginning from a specific index.
     * @param subArr case insensitive byte sub array to compare against
     * @param arr byte array to look in
     * @param fIdx index to starting looking from
     * @return next occurrence or -1, if none was found
     */
    public static int indexOfIgnoreCase(byte[] subArr, byte[] arr, int fIdx) {
        int csIdx;
        int caIdx;
        int idx = -1;
        int subArrLast = subArr.length - 1;
        int arrLen = arr.length;
        int lIdx = fIdx + subArrLast;
        if (subArrLast > 0) {
            while (lIdx < arrLen && idx == -1) {
                if (CASE_INSENSITIVE[subArr[0]] == CASE_INSENSITIVE[arr[fIdx]]) {
                    csIdx = subArrLast;
                    caIdx = lIdx;
                    while (csIdx > 0 && CASE_INSENSITIVE[subArr[csIdx]] == CASE_INSENSITIVE[arr[caIdx]]) {
                        --csIdx;
                        --caIdx;
                    }
                    if (csIdx == 0) {
                        idx = fIdx;
                    }
                }
                ++fIdx;
                ++lIdx;
            }
        }
        else if (subArrLast == 0) {
            while (fIdx < arrLen && idx == -1) {
                if (CASE_INSENSITIVE[subArr[0]] == CASE_INSENSITIVE[arr[fIdx]]) {
                    idx = fIdx;
                }
                ++fIdx;
            }
        }
        return idx;
    }

    /**
     * Case sensitive split a byte array into a list of byte arrays.
     * @param arr byte array to split
     * @param subArr case sensitive byte sub array to split around
     * @param fIdx from index to start from when splitting
     * @param tIdx to index to end splitting
     * @return a list of byte arrays
     */
    public static List<byte[]> split(byte[] arr, byte[] subArr, int fIdx, int tIdx) {
        List<byte[]> list = new LinkedList<byte[]>();
        byte[] tmpArr;
        int csIdx;
        int caIdx;
        int subArrLen = subArr.length;
        int subArrLast = subArrLen - 1;
        if (arr.length < tIdx) {
            tIdx = arr.length;
        }
        if (fIdx > tIdx) {
            throw new IllegalArgumentException("Reverse interval!");
        }
        int lIdx = fIdx + subArrLast;
        int pIdx = fIdx;
        if (subArrLast > 0) {
            while (lIdx < tIdx) {
                if (subArr[0] == arr[fIdx]) {
                    csIdx = subArrLast;
                    caIdx = lIdx;
                    while (csIdx > 0 && subArr[csIdx] == arr[caIdx]) {
                        --csIdx;
                        --caIdx;
                    }
                    if (csIdx == 0) {
                        tmpArr = new byte[fIdx - pIdx];
                        System.arraycopy(arr, pIdx, tmpArr, 0, tmpArr.length);
                        list.add(tmpArr);
                        fIdx += subArrLen;
                        lIdx += subArrLen;
                        pIdx = fIdx;
                    }
                    else {
                        ++fIdx;
                        ++lIdx;
                    }
                }
                else {
                    ++fIdx;
                    ++lIdx;
                }
            }
            if (pIdx < tIdx) {
                tmpArr = new byte[tIdx - pIdx];
                System.arraycopy(arr, pIdx, tmpArr, 0, tmpArr.length);
                list.add(tmpArr);
            }
        }
        else {
            while (fIdx < tIdx) {
                if (subArr[0] == arr[fIdx]) {
                    tmpArr = new byte[fIdx - pIdx];
                    System.arraycopy(arr, pIdx, tmpArr, 0, tmpArr.length);
                    list.add(tmpArr);
                    pIdx = ++fIdx;
                }
                else {
                    ++fIdx;
                }
            }
            if (pIdx < tIdx) {
                tmpArr = new byte[tIdx - pIdx];
                System.arraycopy(arr, pIdx, tmpArr, 0, tmpArr.length);
                list.add(tmpArr);
            }
        }
        if (pIdx == fIdx) {
            list.add(zeroArr);
        }
        return list;
    }

}
