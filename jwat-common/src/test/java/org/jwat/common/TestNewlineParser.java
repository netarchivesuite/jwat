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
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestNewlineParser {

    /**
     * Test newlines parser used to check for trailing CR-LF pairs.
     */
    @Test
    public void test_parsenewlines_crlf() {
        NewlineParser nlp = new NewlineParser();
        Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();
        byte[] bytes;
        int expectedNewlines;
        boolean expectedMissingCr;
        boolean expectedMissingLf;
        boolean expectedMisplacedCr;
        boolean expectedMisplacedLf;
        String expectedRemaining;
        ByteArrayInputStream in;
        ByteCountingPushBackInputStream pbin;
        int newlines;
        byte[] remainingBytes = new byte[16];
        int remaining;
        String remainingStr;

        Object[][] cases = {
                {"".getBytes(), 0, false, false, false, false, ""},
                {"a".getBytes(), 0, false, false, false, false, "a"},
                {"a\r".getBytes(), 0, false, false, false, false, "a\r"},
                {"a\n".getBytes(), 0, false, false, false, false, "a\n"},
                {"a\r\n".getBytes(), 0, false, false, false, false, "a\r\n"},
                {"a\n\r".getBytes(), 0, false, false, false, false, "a\n\r"},
                {"\n".getBytes(), 1, true, false, false, false, ""},
                {"\r".getBytes(), 1, false, true, false, false, ""},
                {"\r\n".getBytes(), 1, false, false, false, false, ""},
                {"\ra".getBytes(), 1, false, true, false, false, "a"},
                {"\r\n\n".getBytes(), 2, true, false, false, false, ""},
                {"\n\r\n".getBytes(), 2, true, false, true, true, ""},
                {"\r\n\r\n".getBytes(), 2, false, false, false, false, ""},
                {"\r\n\na".getBytes(), 2, true, false, false, false, "a"},
                {"\n\r\na".getBytes(), 2, true, false, true, true, "a"},
                {"\r\n\r\na".getBytes(), 2, false, false, false, false, "a"},
                {"\n\r\n\ra".getBytes(), 2, false, false, true, true, "a"}
        };

        try {
            for (int i=0; i<cases.length; ++i) {
                bytes = (byte[])cases[i][0];
                expectedNewlines = (Integer)cases[i][1];
                expectedMissingCr = (Boolean)cases[i][2];
                expectedMissingLf = (Boolean)cases[i][3];
                expectedMisplacedCr = (Boolean)cases[i][4];
                expectedMisplacedLf = (Boolean)cases[i][5];
                expectedRemaining = (String)cases[i][6];
                // debug
                //System.out.println(Base16.encodeArray(bytes));
                in = new ByteArrayInputStream(bytes);
                pbin = new ByteCountingPushBackInputStream(in, 16);
                newlines = nlp.parseCRLFs(pbin, diagnostics);
                Assert.assertEquals(expectedNewlines, newlines);
                Assert.assertEquals(expectedMissingCr, nlp.bMissingCr);
                Assert.assertEquals(expectedMissingLf, nlp.bMissingLf);
                Assert.assertEquals(expectedMisplacedCr, nlp.bMisplacedCr);
                Assert.assertEquals(expectedMisplacedLf, nlp.bMisplacedLf);
                remaining = pbin.read(remainingBytes);
                if (remaining == -1) {
                    remaining = 0;
                }
                remainingStr = new String(remainingBytes, 0, remaining);
                Assert.assertEquals(expectedRemaining, remainingStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }
    }

    /**
     * Test newlines parser used to check for trailing LF characters.
     */
    @Test
    public void test_parsenewlines_lf() {
        NewlineParser nlp = new NewlineParser();
        Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();
        byte[] bytes;
        int expectedNewlines;
        boolean expectedMissingCr;
        boolean expectedMissingLf;
        boolean expectedMisplacedCr;
        boolean expectedMisplacedLf;
        String expectedRemaining;
        ByteArrayInputStream in;
        ByteCountingPushBackInputStream pbin;
        int newlines;
        byte[] remainingBytes = new byte[16];
        int remaining;
        String remainingStr;

        Object[][] cases = {
                {"".getBytes(), 0, false, false, false, false, ""},
                {"a".getBytes(), 0, false, false, false, false, "a"},
                {"a\r".getBytes(), 0, false, false, false, false, "a\r"},
                {"a\n".getBytes(), 0, false, false, false, false, "a\n"},
                {"a\r\n".getBytes(), 0, false, false, false, false, "a\r\n"},
                {"a\n\r".getBytes(), 0, false, false, false, false, "a\n\r"},
                {"\n".getBytes(), 1, false, false, false, false, ""},
                {"\n\n".getBytes(), 2, false, false, false, false, ""},
                {"\n\n\n".getBytes(), 3, false, false, false, false, ""},
                {"\r".getBytes(), 1, false, true, true, false, ""},
                {"\r\n".getBytes(), 1, false, false, true, true, ""},
                {"\ra".getBytes(), 1, false, true, true, false, "a"},
                {"\r\n\n".getBytes(), 2, false, false, true, true, ""},
                {"\n\r\n".getBytes(), 2, false, false, true, false, ""},
                {"\r\n\r\n".getBytes(), 2, false, false, true, true, ""},
                {"\r\n\na".getBytes(), 2, false, false, true, true, "a"},
                {"\n\r\na".getBytes(), 2, false, false, true, false, "a"},
                {"\r\n\r\na".getBytes(), 2, false, false, true, true, "a"},
                {"\n\r\n\ra".getBytes(), 2, false, false, true, false, "a"}
        };

        try {
            for (int i=0; i<cases.length; ++i) {
                bytes = (byte[])cases[i][0];
                expectedNewlines = (Integer)cases[i][1];
                expectedMissingCr = (Boolean)cases[i][2];
                expectedMissingLf = (Boolean)cases[i][3];
                expectedMisplacedCr = (Boolean)cases[i][4];
                expectedMisplacedLf = (Boolean)cases[i][5];
                expectedRemaining = (String)cases[i][6];
                // debug
                //System.out.println(Base16.encodeArray(bytes));
                in = new ByteArrayInputStream(bytes);
                pbin = new ByteCountingPushBackInputStream(in, 16);
                newlines = nlp.parseLFs(pbin, diagnostics);
                Assert.assertEquals(expectedNewlines, newlines);
                Assert.assertEquals(expectedMissingCr, nlp.bMissingCr);
                Assert.assertEquals(expectedMissingLf, nlp.bMissingLf);
                Assert.assertEquals(expectedMisplacedCr, nlp.bMisplacedCr);
                Assert.assertEquals(expectedMisplacedLf, nlp.bMisplacedLf);
                remaining = pbin.read(remainingBytes);
                if (remaining == -1) {
                    remaining = 0;
                }
                remainingStr = new String(remainingBytes, 0, remaining);
                Assert.assertEquals(expectedRemaining, remainingStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }
    }

}
