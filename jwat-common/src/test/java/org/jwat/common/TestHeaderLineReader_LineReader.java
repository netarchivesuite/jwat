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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestHeaderLineReader_LineReader extends TestHeaderLineReaderHelper {

    @Test
    public void test_linereader_lines() {
        byte[] utf8Str;
        byte[] partialUtf8Str = null;
        try {
            utf8Str = "WARCæøå\u1234".getBytes("UTF-8");
            partialUtf8Str = new byte[utf8Str.length - 1];
            System.arraycopy(utf8Str, 0, partialUtf8Str, 0, utf8Str.length - 1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        try {
            commonCases = new Object[][] {
                    {"WARC/1.0".getBytes(),
                        HeaderLine.HLT_RAW, null},
                    {"WARC/1.0\n".getBytes(),
                        HeaderLine.HLT_LINE, "WARC/1.0"},
                    {"WARC\r/1.0\n".getBytes(),
                        HeaderLine.HLT_LINE, "WARC/1.0"},
                    {"WARC/1.0\r\n".getBytes(),
                        HeaderLine.HLT_LINE, "WARC/1.0"},
                    {partialUtf8Str,
                        HeaderLine.HLT_RAW, null}
            };
            /*
             * Raw.
             */
            cases = new Object[][] {
                    {"WARC/1.0\u0001\r\n".getBytes(),
                        HeaderLine.HLT_LINE, "WARC/1.0\u0001"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"),
                        HeaderLine.HLT_LINE, "WARCæøå?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"),
                        HeaderLine.HLT_LINE, "WARCÃ¦Ã¸Ã¥á´/1.0"}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_RAW;
            test_line_cases(commonCases);
            test_line_cases(cases);
            /*
             * US-ASCII.
             */
            cases = new Object[][] {
                    {"WARC/1.0\u0001\r\n".getBytes(),
                        HeaderLine.HLT_LINE, "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"),
                        HeaderLine.HLT_LINE, "WARC?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"),
                        HeaderLine.HLT_LINE, "WARC/1.0"}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_US_ASCII;
            test_line_cases(commonCases);
            test_line_cases(cases);
            /*
             * ISO8859-1.
             */
            cases = new Object[][] {
                    {"WARC/1.0\u0001\r\n".getBytes(),
                        HeaderLine.HLT_LINE, "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"),
                        HeaderLine.HLT_LINE, "WARCæøå?/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"),
                        HeaderLine.HLT_LINE, "WARCÃ¦Ã¸Ã¥á´/1.0"}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_ISO8859_1;
            test_line_cases(commonCases);
            test_line_cases(cases);
            /*
             * UTF-8.
             */
            cases = new Object[][] {
                    {"WARC/1.0\u0001\r\n".getBytes(),
                        HeaderLine.HLT_LINE, "WARC/1.0"},
                    {"WARCæøå/1.0\r\n".getBytes("ISO8859-1"),
                        HeaderLine.HLT_LINE, "WARC1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("ISO8859-1"),
                        HeaderLine.HLT_LINE, "WARC/1.0"},
                    {"WARCæøå\u1234/1.0\r\n".getBytes("UTF-8"),
                        HeaderLine.HLT_LINE, "WARCæøå\u1234/1.0"}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            test_line_cases(commonCases);
            test_line_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_linereader_headerlines() {
        byte[] utf8Str;
        byte[] partialUtf8Str = null;
        try {
            utf8Str = "WARCæøå\u1234".getBytes("UTF-8");
            partialUtf8Str = new byte[utf8Str.length - 1];
            System.arraycopy(utf8Str, 0, partialUtf8Str, 0, utf8Str.length - 1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        try {
            commonCases = new Object[][] {
                    {"content-type: monkeys".getBytes(), new Object[][] {
                        {HeaderLine.HLT_RAW, null, null, null}
                    }},
                    {"content-type: monkeys\r\n and\r\n poo".getBytes(), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null},
                        {HeaderLine.HLT_LINE, " and", null, null},
                        {HeaderLine.HLT_RAW, null, null, null}
                    }},
                    {"content-type: monkeys\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null}
                    }},
                    {"content-type\r: monkeys\r\n and\r\n poo\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null},
                        {HeaderLine.HLT_LINE, " and", null, null},
                        {HeaderLine.HLT_LINE, " poo", null, null}
                    }},
                    {partialUtf8Str, new Object[][] {
                        {HeaderLine.HLT_RAW, null, null, null}
                    }}
            };
            /*
             * Raw.
             */
            cases = new Object[][] {
                    {"content-type: monkeys\u0001\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys\u0001", null, null}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeysæøå?", null, null}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("UTF-8"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeysÃ¦Ã¸Ã¥á´", null, null}
                    }}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_RAW;
            test_headerline_cases(commonCases);
            test_headerline_cases(cases);
            /*
             * US-ASCII.
             */
            cases = new Object[][] {
                    {"content-type: monkeys\u0001\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys?", null, null}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("UTF-8"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null}
                    }}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_US_ASCII;
            test_headerline_cases(commonCases);
            test_headerline_cases(cases);
            /*
             * ISO8859-1.
             */
            cases = new Object[][] {
                    {"content-type: monkeys\u0001\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeysæøå?", null, null}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("UTF-8"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeysÃ¦Ã¸Ã¥á´", null, null}
                    }}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_ISO8859_1;
            test_headerline_cases(commonCases);
            test_headerline_cases(cases);
            /*
             * UTF-8.
             */
            cases = new Object[][] {
                    {"content-type: monkeys\u0001\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null}
                    }},
                    {"content-type: monkeysæøå\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeys", null, null}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("UTF-8"), new Object[][] {
                        {HeaderLine.HLT_LINE, "content-type: monkeysæøå\u1234", null, null}
                    }}
            };
            hlr = HeaderLineReader.getLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            test_headerline_cases(commonCases);
            test_headerline_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_headerlinereader_trim() {
        StringBuffer sb = new StringBuffer();
        sb.setLength(0);
        sb.append("");
        Assert.assertEquals("", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append(" ");
        Assert.assertEquals("", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("  ");
        Assert.assertEquals("", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("   ");
        Assert.assertEquals("", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("text");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append(" text");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("  text");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("   text");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("text ");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("text  ");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("text   ");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append(" text ");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("  text  ");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("   text   ");
        Assert.assertEquals("text", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append(" \u0001text\u0002 ");
        Assert.assertEquals("\u0001text\u0002", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("  \u0001text\u0002  ");
        Assert.assertEquals("\u0001text\u0002", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("   \u0001text\u0002   ");
        Assert.assertEquals("\u0001text\u0002", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("  \u0001 text \u0002  ");
        Assert.assertEquals("\u0001 text \u0002", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append(" \u0001  text  \u0002 ");
        Assert.assertEquals("\u0001  text  \u0002", HeaderLineReader.trim(sb));
        sb.setLength(0);
        sb.append("\u0001   text   \u0002");
        Assert.assertEquals("\u0001   text   \u0002", HeaderLineReader.trim(sb));
    }

}
