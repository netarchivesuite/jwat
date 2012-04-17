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
package org.jwat.warc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestWarcHeader {

    @Test
    public void test_warcparser_version() {
        String header;
        ByteArrayInputStream in;
        ByteCountingPushBackInputStream pbin;
        WarcReader reader;
        WarcRecord record;
        Object[][] cases;
        boolean bValidVersion;

        try {
            cases = new Object[][] {
                    {true, "WARC/0.16\r\n", true, true, false, 0, 16},
                    {true, "WARC/0.17\r\n", true, true, true, 0, 17},
                    {true, "WARC/0.18\r\n", true, true, true, 0, 18},
                    {true, "WARC/0.19\r\n", true, true, false, 0, 19},
                    {true, "WARC/0.99\r\n", true, true, false, 0, 99},
                    {true, "WARC/1.0\r\n", true, true, true, 1, 0},
                    {true, "WARC/1.1\r\n", true, true, false, 1, 1},
                    {true, "WARC/2.0\r\n", true, true, false, 2, 0},
                    {true, "WARC/x.x\r\n", true, true, false, -1, -1},
                    {true, "WARC/1.0.0\r\n", true, true, false, 1, 0},
                    {true, "WARC/1.0.1\r\n", true, true, false, 1, 0},
                    {true, "WARC/1\r\n", true, false, false, -1, -1},
                    {true, "WARC/1.2.3.4.5\r\n", true, false, false, -1, -1},
                    {true, "WARC/\r\n", true, false, false, -1, -1},
                    {true, "WARC/WARC\r\n", true, false, false, -1, -1},
                    {false, "WARC\r\n", false, false, false, -1, -1},
                    {false, "WARC", false, false, false, -1, -1},
                    {false, "", false, false, false, -1, -1},
                    {false, "WARC-Type: resource\r\n", false, false, false, -1, -1},
                    {false, "WARC-Type: resource\r\nWARC", false, false, false, -1, -1},
                    {false, "\r\n", false, false, false, -1, -1},
            };

            for (int i=0; i<cases.length; ++i) {
                bValidVersion = (Boolean)cases[i][0];
                header = (String)cases[i][1];
                // debug
                //System.out.println(header);
                in = new ByteArrayInputStream(header.getBytes("ISO8859-1"));
                pbin = new ByteCountingPushBackInputStream(in, 16);
                reader = WarcReaderFactory.getReader(pbin);
                record = reader.getNextRecord();
                if (bValidVersion) {
                    Assert.assertNotNull(record);
                    Assert.assertNotNull(record.header);
                    Assert.assertEquals(cases[i][2], record.header.bMagicIdentified);
                    Assert.assertEquals(cases[i][3], record.header.bVersionParsed);
                    Assert.assertEquals(cases[i][4], record.header.bValidVersion);
                    Assert.assertEquals(cases[i][5], record.header.major);
                    Assert.assertEquals(cases[i][6], record.header.minor);
                } else {
                    Assert.assertNull(record);
                }
            }

            cases = new Object[][] {
                    {"WARC/1.0\r\n\r\n"},
                    {"WARC/1.0\r\nWARC-Type: resource"},
                    {"WARC/1.0\r\nWARC-Type: resource\r\n"},
                    {"WARC/1.0\r\nWARC-Type resource\r\n"},
                    {"WARC/1.0\r\n: resource\r\n"}
            };

            for (int i=0; i<cases.length; ++i) {
                //bValidVersion = (Boolean)cases[i][0];
                bValidVersion = true;
                header = (String)cases[i][0];
                // debug
                //System.out.println(header);
                in = new ByteArrayInputStream(header.getBytes("ISO8859-1"));
                pbin = new ByteCountingPushBackInputStream(in, 16);
                reader = WarcReaderFactory.getReader(pbin);
                record = reader.getNextRecord();
                if (bValidVersion) {
                    Assert.assertNotNull(record);
                    Assert.assertNotNull(record.header);
                    Assert.assertEquals(true, record.header.bMagicIdentified);
                    Assert.assertEquals(true, record.header.bVersionParsed);
                    Assert.assertEquals(true, record.header.bValidVersion);
                    Assert.assertEquals(1, record.header.major);
                    Assert.assertEquals(0, record.header.minor);
                } else {
                    Assert.assertNull(record);
                }
            }

            header = "WARC/1.0\r\nWARC-Type: resource\r\n";
            in = new ByteArrayInputStream(header.getBytes("ISO8859-1"));
            pbin = new ByteCountingPushBackInputStream(in, 16);

            reader = WarcReaderFactory.getReader(pbin);
            record = reader.getNextRecord();

            Assert.assertNotNull(record);
            Assert.assertNotNull(record.header);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
