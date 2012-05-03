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
import java.io.PushbackInputStream;

import org.junit.Assert;

public class TestHeaderLineReaderHelper {

    Object[][] commonCases;
    Object[][] cases;
    byte[] bytes;
    ByteArrayInputStream in;
    PushbackInputStream pbin;
    HeaderLineReader hlr;
    HeaderLine line;
    byte expectedType;
    String expected;

    public void test_line_cases(Object[][] cases) throws IOException {
        for (int i=0; i<cases.length; ++i) {
            bytes = (byte[])cases[i][0];
            expectedType = (Byte)cases[i][1];
            expected = (String)cases[i][2];
            in = new ByteArrayInputStream(bytes);
            pbin = new PushbackInputStream(in, 16);
            // The puahback size should be equals to the maximum allowed header size.
            line = hlr.readLine(new PushbackInputStream(in, 8192));
            //System.out.println(expected);
            Assert.assertNotNull(line);
            Assert.assertEquals(expectedType, line.type);
            Assert.assertArrayEquals(bytes, line.raw);
            if (line.type == HeaderLine.HLT_LINE) {
                //System.out.println(Base2.delimit(Base2.encodeArray(bytes), 8, '.'));
                //System.out.println(Base16.encodeArray(bytes));
                //System.out.println(Base2.delimit(Base2.encodeString(line.line), 8, '.'));
                //System.out.println(Base16.encodeString(line.line));
                Assert.assertEquals(expected, line.line);
            }
        }
    }

    public void test_headerline_cases(Object[][] cases) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        for (int i=0; i<cases.length; ++i) {
            bytes = (byte[])cases[i][0];
            System.out.println("org: " + new String(bytes));
            Object[][] expectedLines = (Object[][])cases[i][1];
            bytesOut.reset();
            in = new ByteArrayInputStream(bytes);
            // The puahback size should be equals to the maximum allowed header size.
            pbin = new PushbackInputStream(in, 8192);
            for (int j=0; j<expectedLines.length; ++j) {
                line = hlr.readLine(pbin);
                Assert.assertNotNull(line);
                expectedType = (Byte)expectedLines[j][0];
                Assert.assertEquals(expectedType, line.type);
                if (expectedLines[j][1] == null) {
                    Assert.assertNull(line.line);
                } else {
                    Assert.assertEquals(expectedLines[j][1], line.line);
                }
                if (expectedLines[j][2] == null) {
                    Assert.assertNull(line.name);
                } else {
                    Assert.assertEquals(expectedLines[j][2], line.name);
                }
                if (expectedLines[j][3] == null) {
                    Assert.assertNull(line.value);
                } else {
                    Assert.assertEquals(expectedLines[j][3], line.value);
                }
                bytesOut.write(line.raw);
            }
            System.out.println("dst: " + new String(bytesOut.toByteArray()));
            Assert.assertArrayEquals(bytes, bytesOut.toByteArray());
        }
    }

}
