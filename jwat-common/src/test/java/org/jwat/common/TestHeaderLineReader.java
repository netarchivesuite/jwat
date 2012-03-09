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
        	hlr = HeaderLineReader.getHeaderLineReader();
        	hlr.encoding = HeaderLineReader.ENC_ISO8859_1;

        	in = new ByteArrayInputStream("content-type: monkeys".getBytes());
            line = hlr.readLine(new PushbackInputStream(in, 16));
            Assert.assertNull(line);

        	in = new ByteArrayInputStream("content-type: monkeys\r\n and\r\n poo".getBytes());
            line = hlr.readLine(new PushbackInputStream(in, 16));
            Assert.assertNull(line);

            in = new ByteArrayInputStream("content-type: monkeys\r\n".getBytes());
            line = hlr.readLine(new PushbackInputStream(in, 16));
            Assert.assertNotNull(line);
            Assert.assertNull(line.line);
            Assert.assertEquals("content-type", line.name);
            Assert.assertEquals("monkeys", line.value);

        	in = new ByteArrayInputStream("content-type\r: monkeys\r\n and\r\n poo\n".getBytes());
            line = hlr.readLine(new PushbackInputStream(in, 16));
            Assert.assertNotNull(line);
            Assert.assertNull(line.line);
            Assert.assertEquals("content-type", line.name);
            Assert.assertEquals("monkeys and poo", line.value);

            in = new ByteArrayInputStream("content-type:monkeys\r\ncontent-length:  1 2 3 \r\n".getBytes());
            pbin = new PushbackInputStream(in, 16);
            line = hlr.readLine(pbin);
            Assert.assertNotNull(line);
            Assert.assertNull(line.line);
            Assert.assertEquals("content-type", line.name);
            Assert.assertEquals("monkeys", line.value);
            line = hlr.readLine(pbin);
            Assert.assertNotNull(line);
            Assert.assertNull(line.line);
            Assert.assertEquals("content-length", line.name);
            Assert.assertEquals("1 2 3", line.value);
        } catch (IOException e) {
        	e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
