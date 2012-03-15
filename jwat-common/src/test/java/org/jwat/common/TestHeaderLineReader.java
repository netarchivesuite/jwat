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
import java.io.PushbackInputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestHeaderLineReader {

    Object[][] cases;
    byte[] bytes;
    ByteArrayInputStream in;
    PushbackInputStream pbin;
    HeaderLineReader hlr;
    HeaderLine line;
    String expected;

    @Test
    public void test_line_linereader() {
        try {
            /*
             * Raw.
             */
            cases = new Object[][] {
                    {"WARC/1.0".getBytes(), null},
                    {"WARC/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(), "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"), "WARCæøå?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"), "WARCÃ¦Ã¸Ã¥á´/1.0"}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_RAW;
            test_line_cases(cases);
            /*
             * US-ASCII.
             */
            cases = new Object[][] {
                    {"WARC/1.0".getBytes(), null},
                    {"WARC/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(), "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"), "WARC?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"), "WARC/1.0"}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_US_ASCII;
            test_line_cases(cases);
            /*
             * ISO8859-1.
             */
            cases = new Object[][] {
                    {"WARC/1.0".getBytes(), null},
                    {"WARC/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(), "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"), "WARCæøå?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"), "WARCÃ¦Ã¸Ã¥á´/1.0"}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_ISO8859_1;
            test_line_cases(cases);
            /*
             * UTF-8.
             */
            cases = new Object[][] {
                    {"WARC/1.0".getBytes(), null},
                    {"WARC/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(), "WARC/1.0"},
                    {"WARCæøå/1.0\r\n".getBytes("ISO8859-1"), "WARC1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"), "WARCæøå\u1234/1.0"}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            test_line_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_line_headerlinereader() {
        try {
            /*
             * Raw.
             */
            cases = new Object[][] {
                    {"WARC/1.0".getBytes(), null},
                    {"WARC/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(), "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"), "WARCæøå?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"), "WARCÃ¦Ã¸Ã¥á´/1.0"}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_RAW;
            test_line_cases(cases);
            /*
             * US-ASCII.
             */
            cases = new Object[][] {
                    {"WARC/1.0".getBytes(), null},
                    {"WARC/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(), "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"), "WARC?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"), "WARC/1.0"}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_US_ASCII;
            test_line_cases(cases);
            /*
             * ISO8859-1.
             */
            cases = new Object[][] {
                    {"WARC/1.0".getBytes(), null},
                    {"WARC/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(), "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"), "WARCæøå?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"), "WARCÃ¦Ã¸Ã¥á´/1.0"}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_ISO8859_1;
            test_line_cases(cases);
            /*
             * UTF-8.
             */
            cases = new Object[][] {
                    {"WARC/1.0".getBytes(), null},
                    {"WARC/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(), "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(), "WARC/1.0"},
                    {"WARCæøå/1.0\r\n".getBytes("ISO8859-1"), "WARC1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"), "WARCæøå\u1234/1.0"}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            test_line_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void test_line_cases(Object[][] cases) throws IOException {
        for (int i=0; i<cases.length; ++i) {
            bytes = (byte[])cases[i][0];
            expected = (String)cases[i][1];
            in = new ByteArrayInputStream(bytes);
            pbin = new PushbackInputStream(in, 16);
            line = hlr.readLine(new PushbackInputStream(in, 16));
            //System.out.println(expected);
            if (expected == null) {
                Assert.assertNull(line);
            } else {
                //System.out.println(Base2.delimit(Base2.encodeArray(bytes), 8, '.'));
                //System.out.println(Base16.encodeArray(bytes));
                //System.out.println(Base2.delimit(Base2.encodeString(line.line), 8, '.'));
                //System.out.println(Base16.encodeString(line.line));
                Assert.assertEquals(expected, line.line);
            }
        }
    }

    @Test
    public void test_headerline_linereader() {
        try {
            /*
             * Raw.
             */
            cases = new Object[][] {
                    {"content-type: monkeys".getBytes(), null},
                    {"content-type: monkeys\r\n and\r\n poo".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null},
                        {" and", null, null}
                    }},
                    {"content-type: monkeys\r\n".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null}
                    }},
                    {"content-type\r: monkeys\r\n and\r\n poo\n".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null},
                        {" and", null, null},
                        {" poo", null, null}
                    }}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_RAW;
            test_headerline_cases(cases);
            /*
             * US-ASCII.
             */
            cases = new Object[][] {
                    {"content-type: monkeys".getBytes(), null},
                    {"content-type: monkeys\r\n and\r\n poo".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null},
                        {" and", null, null}
                    }},
                    {"content-type: monkeys\r\n".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null}
                    }},
                    {"content-type\r: monkeys\r\n and\r\n poo\n".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null},
                        {" and", null, null},
                        {" poo", null, null}
                    }}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_US_ASCII;
            test_headerline_cases(cases);
            /*
             * ISO8859-1.
             */
            cases = new Object[][] {
                    {"content-type: monkeys".getBytes(), null},
                    {"content-type: monkeys\r\n and\r\n poo".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null},
                        {" and", null, null}
                    }},
                    {"content-type: monkeys\r\n".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null}
                    }},
                    {"content-type\r: monkeys\r\n and\r\n poo\n".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null},
                        {" and", null, null},
                        {" poo", null, null}
                    }}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_ISO8859_1;
            test_headerline_cases(cases);
            /*
             * UTF-8.
             */
            cases = new Object[][] {
                    {"content-type: monkeys".getBytes(), null},
                    {"content-type: monkeys\r\n and\r\n poo".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null},
                        {" and", null, null}
                    }},
                    {"content-type: monkeys\r\n".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null}
                    }},
                    {"content-type\r: monkeys\r\n and\r\n poo\n".getBytes(), new Object[][] {
                        {"content-type: monkeys", null, null},
                        {" and", null, null},
                        {" poo", null, null}
                    }}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            test_headerline_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_headerline_headerlinereader() {
        try {
            /*
             * Raw.
             */
            cases = new Object[][] {
                    {"content-type: monkeys".getBytes(), null},
                    {"content-type: monkeys\r\n and\r\n poo".getBytes(), null},
                    {"content-type: monkeys\r\n".getBytes(), new Object[][] {
                        {null, "content-type", "monkeys"}
                    }},
                    {"content-type\r: monkeys\r\n and\r\n poo\n".getBytes(), new Object[][] {
                        {null, "content-type", "monkeys and poo"}
                    }},
                    {"content-type:monkeys\r\ncontent-length:  1 2 3 \r\n".getBytes(), new Object[][] {
                        {null, "content-type", "monkeys"},
                        {null, "content-length", "1 2 3"}
                       
                    }},
                    {("Set-Cookie: a9locale=en_US; Domain=.a9.com; Path=/\r\n"
                        + "Set-Cookie: a9Temp=\"{\\\"w\\\":\\\"g\\\"}\"; Version=1; Domain=.a9.com; Path=/\r\n"
                        + "Vary: Accept-Encoding,User-Agent\r\n"
                        + "Connection: close\r\n").getBytes(), new Object[][] {
                        {null, "Set-Cookie", "a9locale=en_US; Domain=.a9.com; Path=/"},
                        {null, "Set-Cookie", "a9Temp=\"{\\\"w\\\":\\\"g\\\"}\"; Version=1; Domain=.a9.com; Path=/"},
                        {null, "Vary", "Accept-Encoding,User-Agent"},
                        {null, "Connection", "close"}
                    }},
                    {("HTTP/1.1 200 OK\n"
                        + "Date: Wed, 30 Apr 2008 20:48:25 GMT\n"
                        + "Server: Apache/2.0.54 (Ubuntu) PHP/5.0.5-2ubuntu1.4 mod_ssl/2.0.54 OpenSSL/0.9.7g\n"
                        + "Last-Modified: Wed, 09 Jan 2008 23:18:29 GMT\n"
                        + "ETag: \"47ac-16e-4f9e5b40\"\n"
                        + "Accept-Ranges: bytes\n"
                        + "Content-Length: 366\n"
                        + "Connection: close\n"
                        + "Content-Type: text/html; charset=UTF-8\n").getBytes(), new Object[][] {
                            {"HTTP/1.1 200 OK", null, null},
                            {null, "Date", "Wed, 30 Apr 2008 20:48:25 GMT"},
                            {null, "Server", "Apache/2.0.54 (Ubuntu) PHP/5.0.5-2ubuntu1.4 mod_ssl/2.0.54 OpenSSL/0.9.7g"},
                            {null, "Last-Modified", "Wed, 09 Jan 2008 23:18:29 GMT"},
                            {null, "ETag", "\"47ac-16e-4f9e5b40\""},
                            {null, "Accept-Ranges", "bytes"},
                            {null, "Content-Length", "366"},
                            {null, "Connection", "close"},
                            {null, "Content-Type", "text/html; charset=UTF-8"}
                    }}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_ISO8859_1;
            test_headerline_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void test_headerline_cases(Object[][] cases) throws IOException {
        for (int i=0; i<cases.length; ++i) {
            bytes = (byte[])cases[i][0];
            Object[][] expectedLines = (Object[][])cases[i][1];
            in = new ByteArrayInputStream(bytes);
            pbin = new PushbackInputStream(in, 16);
            if (expectedLines == null) {
                line = hlr.readLine(pbin);
                Assert.assertNull(line);
            } else {
                for (int j=0; j<expectedLines.length; ++j) {
                    line = hlr.readLine(pbin);
                    if (expectedLines[j][0] == null) {
                        Assert.assertNull(line.line);
                    } else {
                        Assert.assertEquals(expectedLines[j][0], line.line);
                    }
                    if (expectedLines[j][1] == null) {
                        Assert.assertNull(line.name);
                    } else {
                        Assert.assertEquals(expectedLines[j][1], line.name);
                    }
                    if (expectedLines[j][2] == null) {
                        Assert.assertNull(line.value);
                    } else {
                        Assert.assertEquals(expectedLines[j][2], line.value);
                    }
                }
            }
        }
    }

}
