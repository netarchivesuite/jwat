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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestAdditionalReadMethods {

    /**
     * Check inputstream with random number of lines with random line length.
     * Each line is trailed by CR+LF.
     */
    @Test
    public void test_inputstream_readline() {
        SecureRandom random = new SecureRandom();
        int length;

        List<String> linesList = new ArrayList<String>();
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        StringBuffer lineSb = new StringBuffer();
        ByteArrayOutputStream linesOut = new ByteArrayOutputStream();
        byte[] srcArr;
        ByteArrayInputStream linesIn;

        try {
            // Test inputstreams with 0-255 linefeeds.
            for (int lines=0; lines<256; ++lines) {
                linesList.clear();
                linesOut.reset();
                // Build lines.
                for (int line=0; line<lines; ++line) {
                    lineOut.reset();
                    length = random.nextInt(256);
                    // Build line.
                    for (int i=0; i<length; ++i) {
                        lineOut.write(random.nextInt(256-32)+32);
                    }
                    lineSb.setLength( 0 );
                    appendBytesToStringBuffer(lineSb, lineOut.toByteArray());
                    linesList.add(lineSb.toString());
                    lineOut.write('\r');
                    lineOut.write('\n');
                    lineOut.writeTo(linesOut);
                }
                srcArr = linesOut.toByteArray();
                linesIn = new ByteArrayInputStream(srcArr);
                ByteCountingPushBackInputStream bcin = new ByteCountingPushBackInputStream(linesIn, 16);
                int lineCnt = 0;
                String tmpStr;
                while ((tmpStr = bcin.readLine()) != null) {
                    Assert.assertEquals(linesList.get(lineCnt), tmpStr);
                    ++lineCnt;
                }
                Assert.assertEquals(lines, lineCnt);
                Assert.assertEquals(srcArr.length, bcin.consumed);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    public void appendBytesToStringBuffer(StringBuffer srcSb, byte[] srcArr) {
        for ( int i=0; i<srcArr.length; ++i ) {
            srcSb.append( (char)(srcArr[ i ] & 255) );
        }
    }

    /**
     * Check inputreader with random number of lines with random line length.
     * Each line is trailed by LF.
     */
    @Test
    public void test_stringreader_readline() {
        SecureRandom random = new SecureRandom();
        int length;

        List<String> linesList = new ArrayList<String>();
        StringBuffer lineSb = new StringBuffer();
        StringBuffer linesSb = new StringBuffer();

        try {
            // Test inputstreams with 0-255 linefeeds.
            for (int lines=0; lines<256; ++lines) {
                linesList.clear();
                linesSb.setLength(0);
                // Build lines.
                for (int line=0; line<lines; ++line) {
                    lineSb.setLength( 0 );
                    length = random.nextInt(256);
                    // Build line.
                    for (int i=0; i<length; ++i) {
                        lineSb.append((char) random.nextInt(256-32)+32);
                    }
                    linesList.add(lineSb.toString());
                    lineSb.append('\r');
                    lineSb.append('\n');
                    linesSb.append(lineSb);
                }
                CharCountingStringReader ccsr = new CharCountingStringReader(linesSb.toString());
                int lineCnt = 0;
                String tmpStr;
                while ((tmpStr = ccsr.readLine()) != null) {
                    Assert.assertEquals(linesList.get(lineCnt), tmpStr);
                    ++lineCnt;
                }
                Assert.assertEquals(lines, lineCnt);
                Assert.assertEquals(linesSb.length(), ccsr.consumed);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    @Test
    public void test_readfully() {
        SecureRandom random = new SecureRandom();

        byte[] srcArr;
        ByteArrayInputStream srcIn;
        ByteCountingPushBackInputStream pbin;
        byte[] tmpArr;
        int read;
        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        try {
            for (int i=0; i<=1024; ++i) {
                srcArr = new byte[i];
                random.nextBytes(srcArr);

                srcIn = new ByteArrayInputStream(srcArr);
                pbin = new ByteCountingPushBackInputStream(srcIn, 16);

                dstArr = new byte[0];
                read = pbin.readFully(dstArr);
                Assert.assertEquals(0, read);

                dstOut.reset();

                boolean b = true;
                while (b) {
                    read = random.nextInt(15) + 1;
                    dstArr = new byte[read];
                    read = pbin.readFully(dstArr);
                    switch (read) {
                    case -1:
                        b = false;
                        break;
                    case 0:
                        read = pbin.read(dstArr);
                        if (read != -1) {
                            if (read == dstArr.length) {
                                Assert.fail("Internal error!");
                            }
                            dstOut.write(dstArr, 0, read);
                        }
                        else {
                            b = false;
                        }
                        break;
                    default:
                        if (read != dstArr.length) {
                            Assert.fail("Internal error!");
                        }
                        dstOut.write(dstArr);
                        break;
                    }
                }
                dstArr = dstOut.toByteArray();
                Assert.assertEquals(srcArr.length, pbin.consumed);
                Assert.assertArrayEquals(srcArr, dstArr);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

}
