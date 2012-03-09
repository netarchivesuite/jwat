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

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestISO8859_1 {

    @Test
    public void test_iso8859_1_encode() {
        ISO8859_1 iso = new ISO8859_1();
        StringBuffer srcSb = new StringBuffer();
        String srcStr;
        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        StringBuffer dstSb = new StringBuffer();
        String dstStr;

        Assert.assertTrue(iso.encode("Fudge", ""));
        Assert.assertArrayEquals("Fudge".getBytes(), iso.encoded);
        Assert.assertFalse(iso.encode("Fudge\n", ""));
        Assert.assertArrayEquals("Fudge".getBytes(), iso.encoded);
        Assert.assertTrue(iso.encode("Fudge\n", "\n"));
        Assert.assertArrayEquals("Fudge\n".getBytes(), iso.encoded);
        Assert.assertFalse(iso.encode("Fudge\r\n", "\n"));
        Assert.assertArrayEquals("Fudge\n".getBytes(), iso.encoded);

        /*
         * 1.
         */

        srcSb.setLength(0);
        for (int i=0; i<256; ++i) {
            srcSb.append((char)i);
        }
        srcStr = srcSb.toString();

        dstOut.reset();
        for (int i=0; i<256; ++i) {
            if (ISO8859_1.validBytes[i] != 0) {
                dstOut.write(i);
            }
        }
        dstArr = dstOut.toByteArray();

        Assert.assertFalse(iso.encode(srcStr, ""));
        Assert.assertArrayEquals(dstArr, iso.encoded);

        dstSb.setLength(0);
        for (int i=0; i<256; ++i) {
            if (ISO8859_1.validBytes[i] != 0) {
                dstSb.append((char)i);
            }
        }
        dstStr = dstSb.toString();

        Assert.assertTrue(iso.decode(iso.encoded, ""));
        Assert.assertEquals(dstStr, iso.decoded);

        /*
         * 2.
         */

        srcSb.setLength(0);
        for (int i=0; i<65536; ++i) {
            srcSb.append((char)i);
        }
        srcStr = srcSb.toString();

        Assert.assertFalse(iso.encode(srcStr, ""));
        Assert.assertArrayEquals(dstArr, iso.encoded);

        Assert.assertTrue(iso.decode(iso.encoded, ""));
        Assert.assertEquals(dstStr, iso.decoded);

        /*
         * 3.
         */

        srcSb.setLength(0);
        for (int i=0; i<256; ++i) {
            srcSb.append((char)i);
        }
        srcStr = srcSb.toString();

        dstOut.reset();
        for (int i=0; i<256; ++i) {
            if (ISO8859_1.validBytes[i] != 0 || i == '\n') {
                dstOut.write(i);
            }
        }
        byte[] dst2Arr = dstOut.toByteArray();

        Assert.assertFalse(iso.encode(srcStr, "\n"));
        Assert.assertArrayEquals(dst2Arr, iso.encoded);

        dstSb.setLength(0);
        for (int i=0; i<256; ++i) {
            if (ISO8859_1.validBytes[i] != 0 || i == '\n') {
                dstSb.append((char)i);
            }
        }
        String dst2Str = dstSb.toString();

        Assert.assertTrue(iso.decode(iso.encoded, "\n"));
        Assert.assertEquals(dst2Str, iso.decoded);

        /*
         * 4.
         */

        srcSb.setLength(0);
        for (int i=0; i<65536; ++i) {
            srcSb.append((char)i);
        }
        srcStr = srcSb.toString();

        Assert.assertFalse(iso.encode(srcStr, "\n"));
        Assert.assertArrayEquals(dst2Arr, iso.encoded);

        Assert.assertTrue(iso.decode(iso.encoded, "\n"));
        Assert.assertEquals(dst2Str, iso.decoded);
    }

    @Test
    public void test_iso8859_1_decode() {
        ISO8859_1 iso = new ISO8859_1();
        ByteArrayOutputStream srcOut = new ByteArrayOutputStream();
        byte[] srcArr;
        StringBuffer dstSb = new StringBuffer();
        String dstStr;

        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        Assert.assertTrue(iso.decode("Fudge".getBytes(), ""));
        Assert.assertEquals("Fudge", iso.decoded);
        Assert.assertFalse(iso.decode("Fudge\n".getBytes(), ""));
        Assert.assertEquals("Fudge", iso.decoded);
        Assert.assertTrue(iso.decode("Fudge\n".getBytes(), "\n"));
        Assert.assertEquals("Fudge\n", iso.decoded);
        Assert.assertFalse(iso.decode("Fudge\r\n".getBytes(), "\n"));
        Assert.assertEquals("Fudge\n", iso.decoded);

        /*
         * 1.
         */

        srcOut.reset();
        for (int i=0; i<256; ++i) {
            srcOut.write(i);
        }
        srcArr = srcOut.toByteArray();

        dstSb.setLength(0);
        for (int i=0; i<256; ++i) {
            if (ISO8859_1.validBytes[i] != 0) {
                dstSb.append((char)i);
            }
        }
        dstStr = dstSb.toString();

        Assert.assertFalse(iso.decode(srcArr, ""));
        Assert.assertEquals(dstStr, iso.decoded);

        dstOut.reset();
        for (int i=0; i<256; ++i) {
            if (ISO8859_1.validBytes[i] != 0) {
                dstOut.write(i);
            }
        }
        dstArr = dstOut.toByteArray();

        Assert.assertTrue(iso.encode(iso.decoded, ""));
        Assert.assertArrayEquals(dstArr, iso.encoded);

        /*
         * 2.
         */

        srcOut.reset();
        for (int i=0; i<256; ++i) {
            srcOut.write(i);
        }
        srcArr = srcOut.toByteArray();

        dstSb.setLength(0);
        for (int i=0; i<256; ++i) {
            if (ISO8859_1.validBytes[i] != 0 || i == '\n') {
                dstSb.append((char)i);
            }
        }
        String dst2Str = dstSb.toString();

        Assert.assertFalse(iso.decode(srcArr, "\n"));
        Assert.assertEquals(dst2Str, iso.decoded);

        dstOut.reset();
        for (int i=0; i<256; ++i) {
            if (ISO8859_1.validBytes[i] != 0 || i == '\n') {
                dstOut.write(i);
            }
        }
        byte[] dst2Arr = dstOut.toByteArray();

        Assert.assertTrue(iso.encode(iso.decoded, "\n"));
        Assert.assertArrayEquals(dst2Arr, iso.encoded);
    }

}
