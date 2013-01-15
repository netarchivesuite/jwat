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
public class TestHeaderLine {

    @Test
    public void test_headerline_readline() {
        SecureRandom random = new SecureRandom();
        int length;

        List<String> linesList = new ArrayList<String>();
        List<byte[]> rawsList = new ArrayList<byte[]>();
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        StringBuffer lineSb = new StringBuffer();
        ByteArrayOutputStream linesOut = new ByteArrayOutputStream();
        byte[] srcArr;
        ByteArrayInputStream linesIn;
        ByteCountingInputStream bcin;

        try {
            // Test inputstreams with 0-255 linefeeds.
            for (int lines=0; lines<256; ++lines) {
                linesList.clear();
                rawsList.clear();
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
                    rawsList.add(lineOut.toByteArray());
                    lineOut.writeTo(linesOut);
                }
                srcArr = linesOut.toByteArray();
                linesIn = new ByteArrayInputStream(srcArr);
                bcin = new ByteCountingInputStream(linesIn);
                int lineCnt = 0;
                HeaderLine headerLine;
                while ((headerLine = HeaderLine.readLine(bcin)) != null) {
                    Assert.assertEquals(linesList.get(lineCnt), headerLine.line);
                    Assert.assertArrayEquals(rawsList.get(lineCnt), headerLine.raw);
                    ++lineCnt;
                }
                Assert.assertEquals(lines, lineCnt);
                Assert.assertEquals(srcArr.length, bcin.consumed);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    public void appendBytesToStringBuffer(StringBuffer srcSb, byte[] srcArr) {
        for ( int i=0; i<srcArr.length; ++i ) {
            srcSb.append( (char)(srcArr[ i ] & 255) );
        }
    }

}
