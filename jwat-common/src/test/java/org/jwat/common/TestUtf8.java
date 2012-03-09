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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestUtf8 {

    String utf8File = "test-utf8.warc";

    @Test
    public void test_utf8_decode_encode() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;
        byte[] org;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] tmpBytes = new byte[256];
        ByteArrayInputStream bin;
        StringBuffer sb = new StringBuffer();
        byte[] copy;
        int read;

        int c;
        UTF8 utf8 = new UTF8();

        try {
            if (bDebugOutput) {
            }

            in = this.getClass().getClassLoader().getResourceAsStream(utf8File);
            while ((read = in.read(tmpBytes, 0, tmpBytes.length)) != -1) {
                out.write(tmpBytes, 0, read);
            }
            org = out.toByteArray();
            out.close();
            out.reset();

            bin = new ByteArrayInputStream(org);
            while ((c = bin.read()) != -1) {
                c = utf8.readUtf8(c, bin);
                if (c == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                if (!utf8.bValidChar) {
                    Assert.fail("Unexpected invalid utf8 char!");
                }
                sb.append((char)c);
            }

            String str = sb.toString();

            for (int i=0; i<str.length(); ++i) {
                c = str.charAt(i);
                utf8.writeUtf8(c, out);
            }
            copy = out.toByteArray();
            out.close();
            out.reset();

            Assert.assertArrayEquals(org, copy);
        } catch (IOException e) {
        }
    }

    @Test
    public void test_utf8_encode_decode() {
        StringBuffer sb = new StringBuffer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream bin;
        byte[] org;
        byte[] org2;

        int c;
        UTF8 utf8 = new UTF8();

        try {
            for (int i=0; i<0x00110000; ++i) {
                sb.append((char)i);
            }

            String str = sb.toString();

            for (int i=0; i<str.length(); ++i) {
                c = str.charAt(i);
                utf8.writeUtf8(c, out);
            }
            org = out.toByteArray();
            out.close();
            out.reset();

            bin = new ByteArrayInputStream(org);
            sb.setLength(0);
            while ((c = bin.read()) != -1) {
                c = utf8.readUtf8(c, bin);
                if (c == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                if (!utf8.bValidChar) {
                    Assert.fail("Unexpected invalid utf8 char!");
                }
                sb.append((char)c);
            }

            String copy = sb.toString();

            Assert.assertEquals(str, copy);

            int[] ints = new int[0x00110000];
            for (int i=0; i<0x00110000; ++i) {
                ints[i] = i;
            }

            for (int i=0; i<ints.length; ++i) {
                c = ints[i];
                utf8.writeUtf8(c, out);
            }
            org2 = out.toByteArray();
            out.close();
            out.reset();

            bin = new ByteArrayInputStream(org2);
            int[] ints2 = new int[0x00110000];
            int idx = 0;
            while ((c = bin.read()) != -1) {
                c = utf8.readUtf8(c, bin);
                if (c == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                if (!utf8.bValidChar) {
                    Assert.fail("Unexpected invalid utf8 char!");
                }
                ints2[idx++] = c;
            }

            Assert.assertEquals(ints2.length, idx);
            Assert.assertArrayEquals(ints, ints2);

        } catch (IOException e) {
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_utf8_invalid() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] arr;
        ByteArrayInputStream bin;

        int c;
        UTF8 utf8 = new UTF8();

        try {
            try {
                utf8.writeUtf8(0x00110001, out);
                Assert.fail("Unexpected exception!");
            } catch (IOException e) {
            }

            out.close();
            out.reset();

            utf8.writeUtf8(0x1F, out);

            arr = out.toByteArray();
            Assert.assertEquals(1, arr.length);

            bin = new ByteArrayInputStream(arr);
            c = bin.read();
            Assert.assertThat(-1, is(not(equalTo(c))));
            c = utf8.readUtf8(c, bin);
            Assert.assertTrue(utf8.bValidChar);
            Assert.assertEquals(0x1F, c);
            c = bin.read();
            Assert.assertEquals(-1, c);

            out.close();
            out.reset();

            arr[0] |= 0x80;

            bin = new ByteArrayInputStream(arr);
            c = bin.read();
            Assert.assertThat(-1, is(not(equalTo(c))));
            c = utf8.readUtf8(c, bin);
            Assert.assertFalse(utf8.bValidChar);
            Assert.assertEquals(0, c);
            c = bin.read();
            Assert.assertEquals(-1, c);

            utf8.writeUtf8(0x00012345, out);

            arr = out.toByteArray();
            Assert.assertEquals(4, arr.length);

            bin = new ByteArrayInputStream(arr);
            c = bin.read();
            Assert.assertThat(-1, is(not(equalTo(c))));
            c = utf8.readUtf8(c, bin);
            Assert.assertTrue(utf8.bValidChar);
            Assert.assertEquals(0x00012345, c);
            c = bin.read();
            Assert.assertEquals(-1, c);

            out.close();
            out.reset();

            arr[3] |= 0xC0;

            bin = new ByteArrayInputStream(arr);
            c = bin.read();
            Assert.assertThat(-1, is(not(equalTo(c))));
            c = utf8.readUtf8(c, bin);
            Assert.assertFalse(utf8.bValidChar);
            //Assert.assertEquals(0, c);
            c = bin.read();
            Assert.assertEquals(-1, c);

            byte[] arr2 = new byte[3];
            System.arraycopy(arr, 0, arr2, 0, 3);

            bin = new ByteArrayInputStream(arr2);
            c = bin.read();
            Assert.assertThat(-1, is(not(equalTo(c))));
            c = utf8.readUtf8(c, bin);
            Assert.assertFalse(utf8.bValidChar);
            Assert.assertEquals(-1, c);

            arr = new byte[2];
            arr[0] = (byte)0xC0;
            arr[1] = (byte)(42 | 0x80);

            bin = new ByteArrayInputStream(arr);
            c = bin.read();
            Assert.assertThat(-1, is(not(equalTo(c))));
            c = utf8.readUtf8(c, bin);
            Assert.assertFalse(utf8.bValidChar);
            Assert.assertEquals(42, c);
            c = bin.read();
            Assert.assertEquals(-1, c);

            arr = new byte[3];
            arr[0] = (byte)0xE0;
            arr[1] = (byte)0x80;
            arr[2] = (byte)(42 | 0x80);

            bin = new ByteArrayInputStream(arr);
            c = bin.read();
            Assert.assertThat(-1, is(not(equalTo(c))));
            c = utf8.readUtf8(c, bin);
            Assert.assertFalse(utf8.bValidChar);
            Assert.assertEquals(42, c);
            c = bin.read();
            Assert.assertEquals(-1, c);

            arr = new byte[4];
            arr[0] = (byte)0xF0;
            arr[1] = (byte)0x80;
            arr[2] = (byte)0x80;
            arr[3] = (byte)(42 | 0x80);

            bin = new ByteArrayInputStream(arr);
            c = bin.read();
            Assert.assertThat(-1, is(not(equalTo(c))));
            c = utf8.readUtf8(c, bin);
            Assert.assertFalse(utf8.bValidChar);
            Assert.assertEquals(42, c);
            c = bin.read();
            Assert.assertEquals(-1, c);
        } catch (IOException e) {
            Assert.fail("Unexpected exception!");
        }
    }

}
