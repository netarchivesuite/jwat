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
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.junit.Test;

public class TestHeaderLineReader_HeaderLineReader extends TestHeaderLineReaderHelper {

    @Test
    public void test_headerlinereader_lines() {
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
            hlr = HeaderLineReader.getHeaderLineReader();
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
            hlr = HeaderLineReader.getHeaderLineReader();
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
            hlr = HeaderLineReader.getHeaderLineReader();
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
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            test_line_cases(commonCases);
            test_line_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    /*
    From: =?US-ASCII?Q?Keith_Moore?= <moore@cs.utk.edu>
    To: =?ISO-8859-1?Q?Keld_J=F8rn_Simonsen?= <keld@dkuug.dk>
    CC: =?ISO-8859-1?Q?Andr=E9?= Pirard <PIRARD@vm1.ulg.ac.be>
    Subject: =?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?=
     =?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=
    */

    @Test
    public void test_headerlinereader_headerlines() {
        byte[] utf8Str;
        byte[] partialUtf8Str = null;
        try {
            utf8Str = "content-type: monkeysæøå\u1234".getBytes("UTF-8");
            partialUtf8Str = new byte[utf8Str.length - 1];
            System.arraycopy(utf8Str, 0, partialUtf8Str, 0, utf8Str.length - 1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        try {
            commonCases = new Object[][] {
                    {"content-type: monkeys".getBytes(), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: monkeys\r\n and\r\n poo".getBytes(), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: monkeys\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"}
                    }},
                    {"content-type\r: monkeys\r\n and\r\n poo\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys and poo"}
                    }},
                    {"content-type:monkeys\r\ncontent-length:  1 2 3 \r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"},
                        {HeaderLine.HLT_HEADERLINE, null, "content-length", "1 2 3"}
                       
                    }},
                    {("Set-Cookie: a9locale=en_US; Domain=.a9.com; Path=/\r\n"
                        + "Set-Cookie: a9Temp=\"{\\\"w\\\":\\\"g\\\"}\"; Version=1; Domain=.a9.com; Path=/\r\n"
                        + "Vary: Accept-Encoding,User-Agent\r\n"
                        + "Connection: close\r\n").getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "Set-Cookie", "a9locale=en_US; Domain=.a9.com; Path=/"},
                        {HeaderLine.HLT_HEADERLINE, null, "Set-Cookie", "a9Temp=\"{\\\"w\\\":\\\"g\\\"}\"; Version=1; Domain=.a9.com; Path=/"},
                        {HeaderLine.HLT_HEADERLINE, null, "Vary", "Accept-Encoding,User-Agent"},
                        {HeaderLine.HLT_HEADERLINE, null, "Connection", "close"}
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
                            {HeaderLine.HLT_LINE, "HTTP/1.1 200 OK", null, null},
                            {HeaderLine.HLT_HEADERLINE, null, "Date", "Wed, 30 Apr 2008 20:48:25 GMT"},
                            {HeaderLine.HLT_HEADERLINE, null, "Server", "Apache/2.0.54 (Ubuntu) PHP/5.0.5-2ubuntu1.4 mod_ssl/2.0.54 OpenSSL/0.9.7g"},
                            {HeaderLine.HLT_HEADERLINE, null, "Last-Modified", "Wed, 09 Jan 2008 23:18:29 GMT"},
                            {HeaderLine.HLT_HEADERLINE, null, "ETag", "\"47ac-16e-4f9e5b40\""},
                            {HeaderLine.HLT_HEADERLINE, null, "Accept-Ranges", "bytes"},
                            {HeaderLine.HLT_HEADERLINE, null, "Content-Length", "366"},
                            {HeaderLine.HLT_HEADERLINE, null, "Connection", "close"},
                            {HeaderLine.HLT_HEADERLINE, null, "Content-Type", "text/html; charset=UTF-8"}
                    }},
                    /*
                    {partialUtf8Str, null},
                    {"From: =?US-ASCII?Q?Keith_Moore?= <moore@cs.utk.edu>\r\n".getBytes(), new Object[][] {
                            {null, "From", "=?US-ASCII?Q?Keith_Moore?= <moore@cs.utk.edu>"}
                    }}
                    */
            };
            /*
             * Raw.
             */
            cases = new Object[][] {
                    {"content-type: monkeys\u0001\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys\u0001"}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeysæøå?"}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("UTF-8"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeysÃ¦Ã¸Ã¥á´"}
                    }}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_RAW;
            test_headerline_cases(commonCases);
            test_headerline_cases(cases);
            /*
             * US-ASCII.
             */
            cases = new Object[][] {
                    {"content-type: monkeys\u0001\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys?"}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("UTF-8"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"}
                    }}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_US_ASCII;
            test_headerline_cases(commonCases);
            test_headerline_cases(cases);
            /*
             * ISO8859-1.
             */
            cases = new Object[][] {
                    {"content-type: monkeys\u0001\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeysæøå?"}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("UTF-8"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeysÃ¦Ã¸Ã¥á´"}
                    }}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_ISO8859_1;
            test_headerline_cases(commonCases);
            test_headerline_cases(cases);
            /*
             * UTF-8.
             */
            cases = new Object[][] {
                    {"content-type: monkeys\u0001\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"}
                    }},
                    {"content-type: monkeysæøå\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"}
                    }},
                    {"content-type: monkeysæøå\u1234\r\n".getBytes("UTF-8"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeysæøå\u1234"}
                    }}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            test_headerline_cases(commonCases);
            test_headerline_cases(cases);

            byte[] bytes;
            ByteArrayInputStream in;
            ByteCountingPushBackInputStream pbin;
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_ISO8859_1;

            bytes = "WARC-Target-URI: http://www.10paint.com/search/searchproduct.asp?keywords=?%C2%BE%C3%A5".getBytes("ISO8859-1");
            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);
            hlr.readLine(pbin);

            bytes = "Content-Type: application/http; msgtype=response".getBytes("ISO8859-1");
            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);
            hlr.readLine(pbin);

            bytes = "WARC-Target-URI: http://www.chsima.com/search/news.asp?page=3&keyword=?%C3%83%C2%82%C3%82%C2%B4%C3%83%C2%83%C3%82%C2%A6?%C3%83%C2%83%C3%82%C2%A4%C3%83%C2%82%C3%82%C2%BB%C3%83%C2%82%C3%82%C2%A2%C3%83%C2%83%C3%82%C2%A9?%C3%83%C2%83%C3%82%C2%A6%C3%83%C2%82%C3%82%C2%A5??%2011?%C3%83%C2%82%C3%82%C2%B4%C3%83%C2%83%C3%82%C2%A6?%C3%83%C2%83%C3%82%C2%A4%C3%83%C2%82%C3%82%C2%BC??%C3%83%C2%83%C3%82%C2%A6%C3%83%C2%82%C3%82%C2%AC??%202009&flag=0".getBytes("ISO8859-1");
            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);
            hlr.readLine(pbin);

            bytes = "WARC-Target-URI: http://www.chsima.com/search/news.asp?page=2&keyword=??????".getBytes("ISO8859-1");
            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);
            hlr.readLine(pbin);

            bytes = "WARC-Target-URI: http://www.chsima.com/search/news.asp?page=1&keyword=??????%2011??????%202009&flag=0".getBytes("ISO8859-1");
            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);
            hlr.readLine(pbin);

            bytes = "WARC-Target-URI: http://www.chsima.com/search/news.asp?page=7&keyword=??????%2011??????%202009&flag=0".getBytes("ISO8859-1");
            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);
            hlr.readLine(pbin);

            bytes = "WARC-Target-URI: http://www.10paint.com/search/searchall.asp?keywords=?%C3%A5%C2%9E".getBytes("ISO8859-1");
            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);
            hlr.readLine(pbin);

        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_headerlinereader_headerlines_lws() {
        try {
            cases = new Object[][] {
                    {"content-type: monkeys".getBytes(), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: monkeys\r\n and\r\n poo".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"},
                        {HeaderLine.HLT_LINE, " and", null, null},
                        // TODO Maybe the line should be returned at least?
                        {HeaderLine.HLT_RAW, null, null, null}
                    }},
                    {"content-type: monkeys\r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"}
                    }},
                    {"content-type\r: monkeys\r\n and\r\n poo\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"},
                        {HeaderLine.HLT_LINE, " and", null, null},
                        {HeaderLine.HLT_LINE, " poo", null, null}
                    }},
                    {"content-type:monkeys\r\ncontent-length:  1 2 3 \r\n".getBytes(), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "monkeys"},
                        {HeaderLine.HLT_HEADERLINE, null, "content-length", "1 2 3"}
                       
                    }}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            hlr.bLWS = false;
            test_headerline_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_headerlinereader_decode_eof() {
        try {
            cases = new Object[][] {
                    {("content-type: monkeys" + (char)(0xC0)).getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            hlr.bLWS = false;
            test_headerline_cases(cases);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_headerlinereader_quotedtext() {
        try {
            cases = new Object[][] {
                    {"content-type: \"monkeys\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "\"monkeys\""}
                    }},
                    {"content-type: \"hello\rmonkeys\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "\"hellomonkeys\""}
                    }},
                    {"content-type: \"monkeys\r\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "\"monkeys\""}
                    }},
                    {"content-type: \"monkeys\r\\\"\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "\"monkeys\\\"\""}
                    }},
                    {"content-type: \"monkeys\r\n invasion!\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "\"monkeys invasion!\""}
                    }},
                    {"content-type: \"monkeys\r\n\tinvasion!\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "\"monkeys invasion!\""}
                    }},
                    {("content-type: \"monkeys" + (char)(0xC0)).getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    // Covered quoted-text and not quoted pair
                    {("content-type: \"monkeys" + (char)(0xC0) + (char)(0x01)).getBytes("ISO8859-1"), new Object[][] {
                            {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    /*
                    {"content-type: \"monkeys\u1234".getBytes("ISO8859-1"), new Object[][] {
                            {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: \"monkeys\t".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    */
                    {("content-type: \"monkeys" + (char)(0x01)).getBytes("ISO8859-1"), new Object[][] {
                            {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: \"monkeys\\".getBytes("ISO8859-1"), new Object[][] {
                            {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {("content-type: \"monkeys\\" + (char)(0x01)).getBytes("ISO8859-1"), new Object[][] {
                            {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {("content-type: \"monkeys\\" + (char)(0xC0)).getBytes("ISO8859-1"), new Object[][] {
                            {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: \"monkeys\r\n invasion!".getBytes("ISO8859-1"), new Object[][] {
                            {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: \"monkeys\r\n invasion!\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: \"monkeys\r\n\tinvasion!\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: \"monkeys\r\n invasion!\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "\"monkeys invasion!\""}
                    }},
                    {"content-type: \"monkeys\r\n\tinvasion!\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "\"monkeys invasion!\""}
                    }},
                    {"content-type: \"monkeys\r\n\tinvasion!\r\ntest line\"\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null},
                        {HeaderLine.HLT_LINE, "test line\"", null, null}
                    }},

                    {"content-type: =".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_RAW, null, "content-type", null}
                    }},
                    {"content-type: =?iso-8859-1?q?this=20is=20some=20text?=\r\n".getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "content-type", "=?iso-8859-1?q?this=20is=20some=20text?="}
                    }},

            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            hlr.bLWS = true;
            test_headerline_cases(cases);

            /*
            String httpHeader = 
            		//"HTTP/1.1 200 OK\r\n" +
            		"Cache-Control: public\r\n" +
		            "Content-Length: 108388\r\n" +
		            "Content-Type: image/png\r\n" +
		            "Last-Modified: Tue, 22 Feb 2011 21:13:52 GMT\r\n" +
		            "Server: Microsoft-IIS/7.0\r\n" +
		            "Content-Disposition: inline; filename=march-GEO.png\"\r\n" +
		            "X-AspNet-Version: 2.0.50727\r\n" +
		            "X-Powered-By: ASP.NET\r\n" +
		            "Date: Fri, 25 Feb 2011 22:06:45 GMT\r\n" +
		            "Connection: close\r\n" +
		            "\r\n";
            cases = new Object[][] {
                    {httpHeader.getBytes("ISO8859-1"), new Object[][] {
                        {HeaderLine.HLT_HEADERLINE, null, "Cache-Control", "public"},
                		{HeaderLine.HLT_HEADERLINE, null, "Content-Length", "108388"},
        				{HeaderLine.HLT_HEADERLINE, null, "Content-Type", "image/png"},
						{HeaderLine.HLT_HEADERLINE, null, "Last-Modified", "Tue, 22 Feb 2011 21:13:52 GMT"},
						{HeaderLine.HLT_HEADERLINE, null, "Server", "Microsoft-IIS/7.0"},
						{HeaderLine.HLT_HEADERLINE, null, "Content-Disposition", "inline; filename=march-GEO.png\""},
						{HeaderLine.HLT_HEADERLINE, null, "X-AspNet-Version", "2.0.50727"},
						{HeaderLine.HLT_HEADERLINE, null, "X-Powered-By", "ASP.NET"},
						{HeaderLine.HLT_HEADERLINE, null, "Date", "Fri, 25 Feb 2011 22:06:45 GMT"},
						{HeaderLine.HLT_HEADERLINE, null, "Connection", "close"}
						//{HeaderLine.HLT_LINE, null, "Connection: close"}
                    }}
            };
            hlr = HeaderLineReader.getHeaderLineReader();
            hlr.encoding = HeaderLineReader.ENC_UTF8;
            hlr.bLWS = false;
            test_headerline_cases(cases);
            */
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_headerlinereader_headerline_encoded_words() {
    }

}
