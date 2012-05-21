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
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.HeaderLine;

@RunWith(JUnit4.class)
public class TestWarcHeader {

    @Test
    public void test_warcheader_version_parser() {
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

	String[] headers;
	Object[][] cases;
	WarcHeader header;
	HeaderLine headerLine;
	List<Diagnosis> errors;
	List<Diagnosis> warnings;
	Diagnosis diagnosis;

	@Test
    public void test_warcheader_addheader() {
    	headers = new String[] {
    	    	"WARC-Type",
    	        "WARC-Record-ID",
    	        "WARC-Date",
    	        "Content-Length",
    	        "Content-Type",
    	        "WARC-Concurrent-To",
    	        "WARC-Block-Digest",
    	        "WARC-Payload-Digest",
    	        "WARC-IP-Address",
    	        "WARC-Refers-To",
    	        "WARC-Target-URI",
    	        "WARC-Truncated",
    	        "WARC-Warcinfo-ID",
    	        "WARC-Filename",
    	        "WARC-Profile",
    	        "WARC-Identified-Payload-Type",
    	        "WARC-Segment-Origin-ID",
    	        "WARC-Segment-Number",
    	        "WARC-Segment-Total-Length"
    	};
    	testEmptyHeader(headers, null);
    	testEmptyHeader(headers, "");

    	String fieldName;
    	String fieldValue;

    	fieldName = "WARC-Record-ID";
    	fieldValue = "http://hello_kitty$";
    	testHeader(fieldName, fieldValue, 0, 0);
    	/*
        "WARC-Concurrent-To",
        "WARC-Refers-To",
        "WARC-Target-URI",
        "WARC-Warcinfo-ID",
        "WARC-Segment-Origin-ID",
    	 */

    	/*
    	 * Add header.
    	 */

    	cases = new Object[][] {
    			{"WARC-Type", new Object[][] {
    					{"hello_kitty", null, null, new TestHeaderCallback() {
    	    		    	public void callback(WarcHeader header) {
    	    	    	    	Assert.assertEquals(new Integer(WarcConstants.RT_IDX_UNKNOWN), header.warcTypeIdx);
    	    		    	}
    					}}
    			}},
    			{"WARC-Concurrent-To", new Object[][] {
    	    			{"hello_kitty", null, null, new TestHeaderCallback() {
    	    		    	public void callback(WarcHeader header) {
    	    		        	Assert.assertEquals(1, header.warcConcurrentToList.size());
    	    		    	}
    	    			}},
    	    			{"hello_kitty2", null, null, new TestHeaderCallback() {
    	    		    	public void callback(WarcHeader header) {
    	    		        	Assert.assertEquals(2, header.warcConcurrentToList.size());
    	    		    	}
    	    			}}
    	    	}},
    	    	{"WARC-Date", new Object[][] {
    	    	    	{"hello_kitty", new Object[][] {
    	    	    			{DiagnosisType.INVALID_EXPECTED, "'WARC-Date' value", 2}
    	    	    	}, null, null}
    	    	}},
    	    	{"Content-Length", new Object[][] {
    	    			{"hello_kitty", new Object[][] {
    	        	        	{DiagnosisType.INVALID_EXPECTED, "'Content-Length' value", 2}
    	    			}, null, null}
    	    	}},
    	    	{"Content-Type", new Object[][] {
    	    			{"hello_kitty", new Object[][] {
    	    					{DiagnosisType.INVALID_EXPECTED, "'Content-Type' value", 2}
   	    					
    	    			}, null, null}
    	    	}},
    	    	{"WARC-Block-Digest", new Object[][] {
    	    			{"hello_kitty", new Object[][] {
    	    	    			{DiagnosisType.INVALID_EXPECTED, "'WARC-Block-Digest' value", 2}
    	    			}, null, null}
    	    	}},
    	    	{"WARC-Payload-Digest", new Object[][] {
    	    			{"hello_kitty", new Object[][] {
    	    					{DiagnosisType.INVALID_EXPECTED, "'WARC-Payload-Digest' value", 2}
    	    			}, null, null}
    	    	}},
    	    	{"WARC-IP-Address", new Object[][] {
    	    			{"hello_kitty", new Object[][] {
    	    					{DiagnosisType.INVALID_EXPECTED, "'WARC-IP-Address' value", 2}
    	    			}, null, null}
    	    	}},
    	    	{"WARC-Segment-Number", new Object[][] {
    	    			{"hello_kitty", new Object[][] {
    	    					{DiagnosisType.INVALID_EXPECTED, "'WARC-Segment-Number' value", 2}
    	    			}, null, null}
    	    	}},
    	    	{"WARC-Segment-Total-Length", new Object[][] {
    	    			{"hello_kitty", new Object[][] {
    	    					{DiagnosisType.INVALID_EXPECTED, "'WARC-Segment-Total-Length' value", 2}
    	    			}, null, null}
    	    	}},
    	    	{"WARC-Truncated", new Object[][] {
    	    			{"hello_kitty", null, null, new TestHeaderCallback() {
    	    		    	public void callback(WarcHeader header) {
    	    		        	Assert.assertEquals(new Integer(WarcConstants.TT_IDX_FUTURE_REASON), header.warcTruncatedIdx);
    	    		    	}
    	    			}}
    	    	}},
    	    	{"WARC-Filename", new Object[][] {
    	    			{"hello_kitty", null, null, null}
    	    	}},
    	    	{"WARC-Profile", new Object[][] {
    	    			{"hello_kitty", null, null, new TestHeaderCallback() {
    	    		    	public void callback(WarcHeader header) {
    	    		        	Assert.assertEquals(new Integer(WarcConstants.PROFILE_IDX_UNKNOWN), header.warcProfileIdx);
    	    		    	}
    	    			}}
    	    	}},
    	    	{"WARC-Identified-Payload-Type", new Object[][] {
    	    			{"hello_kitty", new Object[][] {
    	    					{DiagnosisType.INVALID_EXPECTED, "'WARC-Identified-Payload-Type' value", 2}
    	    			}, null, null}
    	    	}}
    	};
    	test_headeradd_cases(cases);

		/*
		 * Duplicate headers.
		 */

		header = getTestHeader();
    	headerLine = header.addHeader("WARC-Type", "warcinfo");
		Assert.assertNotNull(headerLine);
		Assert.assertEquals("WARC-Type", headerLine.name);
		Assert.assertEquals("warcinfo", headerLine.value);
		errors = header.diagnostics.getErrors();
		warnings = header.diagnostics.getWarnings();
		Assert.assertEquals(0, errors.size());
		Assert.assertEquals(0, warnings.size());
		headerLine = header.addHeader("WARC-Type", "warcinfo");
		Assert.assertNotNull(headerLine);
		Assert.assertEquals("WARC-Type", headerLine.name);
		Assert.assertEquals("warcinfo", headerLine.value);
		errors = header.diagnostics.getErrors();
		warnings = header.diagnostics.getWarnings();
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals(0, warnings.size());
		diagnosis = errors.get(0);
        Assert.assertEquals(DiagnosisType.DUPLICATE, diagnosis.type);
        Assert.assertEquals("'WARC-Type' header", diagnosis.entity);
        Assert.assertEquals(1, diagnosis.information.length);

    	/*
    	 * Non warc headers.
    	 */

    	header = getTestHeader();
    	headerLine = header.addHeader("X-Header1", "Hello");
		Assert.assertNotNull(headerLine);
		Assert.assertEquals("X-Header1", headerLine.name);
		Assert.assertEquals("Hello", headerLine.value);
    	headerLine = header.addHeader("X-Header2", "Kitty");
		Assert.assertNotNull(headerLine);
		Assert.assertEquals("X-Header2", headerLine.name);
		Assert.assertEquals("Kitty", headerLine.value);

		/*
		 * Date add header.
		 */

		String warcDateStr1 = "2012-05-17T00:14:47Z";
        Date warcDate1 = WarcDateParser.getDate(warcDateStr1);

        cases = new Object[][] {
        		{"WARC-Date", null, null,
        			null, null, null, null},
        		{"WARC-Date", warcDate1, null,
        			warcDateStr1, null, null, null},
        		{"WARC-Date", null, warcDateStr1,
        			warcDateStr1, null, null, null},
        		{"WARC-Date", warcDate1, warcDateStr1,
        			warcDateStr1, null, null, null},
        		{"WARC-Filename", null, null,
        			null, null, null, null},
        		{"WARC-Filename", warcDate1, null,
        			warcDateStr1, null, null, null},
        		{"WARC-Filename", null, warcDateStr1,
        			warcDateStr1, null, null, null},
        		{"WARC-Filename", warcDate1, warcDateStr1,
        			warcDateStr1, null, null, null}
        };
        test_headeradd_date_cases(cases);

        /*
         * Non warc headers.
         */

        header = getTestHeader();
    	headerLine = header.addHeader("X-Header1", warcDate1, warcDateStr1);
		Assert.assertNotNull(headerLine);
		Assert.assertEquals("X-Header1", headerLine.name);
		Assert.assertEquals(warcDateStr1, headerLine.value);
    	headerLine = header.addHeader("X-Header2", warcDate1, warcDateStr1);
		Assert.assertNotNull(headerLine);
		Assert.assertEquals("X-Header2", headerLine.name);
		Assert.assertEquals(warcDateStr1, headerLine.value);

		/*
		 * Duplicate date headers.
		 */

		header = getTestHeader();
    	headerLine = header.addHeader("WARC-Date", warcDate1, warcDateStr1);
		Assert.assertNotNull(headerLine);
		Assert.assertEquals("WARC-Date", headerLine.name);
		Assert.assertEquals(warcDateStr1, headerLine.value);
		errors = header.diagnostics.getErrors();
		warnings = header.diagnostics.getWarnings();
		Assert.assertEquals(0, errors.size());
		Assert.assertEquals(0, warnings.size());
		headerLine = header.addHeader("WARC-Date", warcDate1, warcDateStr1);
		Assert.assertNotNull(headerLine);
		Assert.assertEquals("WARC-Date", headerLine.name);
		Assert.assertEquals(warcDateStr1, headerLine.value);
		errors = header.diagnostics.getErrors();
		warnings = header.diagnostics.getWarnings();
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals(0, warnings.size());
		diagnosis = errors.get(0);
        Assert.assertEquals(DiagnosisType.DUPLICATE, diagnosis.type);
        Assert.assertEquals("'WARC-Date' header", diagnosis.entity);
        Assert.assertEquals(1, diagnosis.information.length);

        /*
         * Check fields.
         */

        cases = new Object[][] {
			{new Object[][] {
					{"WARC-Type", "hello_kitty"}
					}, new Object[][] {
						{DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_RECORD_ID + "' header", 1},
						{DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_DATE + "' header", 1},
						{DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_CONTENT_LENGTH + "' header", 1}
					}, new Object[][] {
				    	{DiagnosisType.UNKNOWN, "'" + WarcConstants.FN_WARC_TYPE + "' value", 1}
					}, null
			},
			{new Object[][] {
					{"WARC-Type", "warcinfo"},
					{"WARC-Record-ID", "<urn:uuid:0d5d5e9f-2222-4780-b5a4-bbcb3f28431f>"},
					{"WARC-Date", "2012-05-17T00:14:47Z"},
					{"Content-Length", "0"},
					{"Content-Type", "application/warc-fields"},
					{"WARC-Filename", "converted-IAH-20080430204825-00000-blackbook.warc"}
					}, null, null, null
			},
			{new Object[][] {
					{"WARC-Type", "revisit"},
					{"WARC-Target-URI", "http://state.tn.us/sos/rules/0240/0240-04/0240-04.htm"},
					{"WARC-Date", "2010-07-07T13:29:44Z"},
					{"WARC-Payload-Digest", "sha1:TTINQ47F6426Y5KPEZOUYEKE76BBVT56"},
					{"WARC-IP-Address", "170.143.36.24"},
					{"WARC-Profile", "http://netpreserve.org/warc/1.0/revisit/identical-payload-digest"},
					{"WARC-Truncated", "length"},
					{"WARC-Record-ID", "<urn:uuid:4b09f3c5-6aaa-460c-ba2c-b3c449394a1c>"},
					{"Content-Type", "application/http; msgtype=response"},
					{"Content-Length", "206"},
					}, null, null, null
			},
			{new Object[][] {
					{"WARC-Type", "revisit"},
					{"WARC-Target-URI", "http://state.tn.us/sos/rules/0240/0240-04/0240-04.htm"},
					{"WARC-Date", "2010-07-07T13:29:44Z"},
					{"WARC-Payload-Digest", "sha1:TTINQ47F6426Y5KPEZOUYEKE76BBVT56"},
					{"WARC-IP-Address", "170.143.36.24"},
					{"WARC-Profile", "hello kitty"},
					{"WARC-Truncated", "length"},
					{"WARC-Record-ID", "<urn:uuid:4b09f3c5-6aaa-460c-ba2c-b3c449394a1c>"},
					{"Content-Type", "application/http; msgtype=response"},
					{"Content-Length", "206"},
					}, null, new Object[][] {
						{DiagnosisType.UNKNOWN, "'" + WarcConstants.FN_WARC_PROFILE + "' value", 1}
					}, null
			},
			{new Object[][] {
					{"WARC-Type", "warcinfo"},
					{"WARC-Date", "2008-04-30T20:48:25Z"},
					{"WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz"},
					{"WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>"},
					//{"Content-Type", "application/warc-fields"},
					{"Content-Length", "483"},
					}, null, new Object[][] {
	                	{DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' header", 0}
					}, null
			},
			{new Object[][] {
					{"WARC-Type", "continuation"},
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"WARC-Segment-Number", "42"},
					{"WARC-Segment-Total-Length", "1234"},
					{"WARC-Segment-Origin-ID", "<urn:uuid:ff728363-2d5f-4f5f-b832-9552de1a6037>"},
					{"Content-Length", "693"},
					{"WARC-Truncated", "length"}
					}, null, null, null
			},
			{new Object[][] {
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"Content-Length", "693"}
					}, new Object[][] {
						{DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_TYPE + "' header", 1}
					}, new Object[][] {
						{DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' header", 0}
					}, null
			},
			{new Object[][] {
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"Content-Length", "693"},
					{"Content-Type", ""},
					}, new Object[][] {
						{DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_TYPE + "' header", 1}
					}, new Object[][] {
						{DiagnosisType.EMPTY, "'" + WarcConstants.FN_CONTENT_TYPE + "' field", 0},
						{DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' header", 0}
					}, null
			},
			{new Object[][] {
					{"WARC-Type", "warcinfo"},
					{"WARC-Record-ID", "<urn:uuid:0d5d5e9f-2222-4780-b5a4-bbcb3f28431f>"},
					{"WARC-Date", "2012-05-17T00:14:47Z"},
					{"Content-Length", "0"},
					{"Content-Type", "hello/warc-fields"},
					{"WARC-Filename", "converted-IAH-20080430204825-00000-blackbook.warc"}
					}, null, new Object[][] {
						{DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' value", 2}
					}, null
			},
			{new Object[][] {
					{"WARC-Type", "warcinfo"},
					{"WARC-Record-ID", "<urn:uuid:0d5d5e9f-2222-4780-b5a4-bbcb3f28431f>"},
					{"WARC-Date", "2012-05-17T00:14:47Z"},
					{"Content-Length", "0"},
					{"Content-Type", "application/kitty"},
					{"WARC-Filename", "converted-IAH-20080430204825-00000-blackbook.warc"}
					}, null, new Object[][] {
						{DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' value", 2}
					}, null
			},
			{new Object[][] {
					{"WARC-Type", "response"},
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
					{"WARC-IP-Address", "207.241.229.39"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"Content-Type", "application/http; msgtype=response"},
					{"Content-Length", "693"}
					}, null, null, null
			},
			{new Object[][] {
					{"WARC-Type", "response"},
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
					{"WARC-IP-Address", "207.241.229.39"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"WARC-Segment-Number", "1"},
					{"Content-Type", "application/http; msgtype=response"},
					{"Content-Length", "693"}
					}, null, null, null
			},
			{new Object[][] {
					{"WARC-Type", "response"},
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
					{"WARC-IP-Address", "207.241.229.39"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"WARC-Segment-Number", "2"},
					{"Content-Type", "application/http; msgtype=response"},
					{"Content-Length", "693"}
					}, new Object[][] {
						{DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER + "' value", 2}
					}, null, null
			},
			{new Object[][] {
					{"WARC-Type", "continuation"},
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"WARC-Segment-Origin-ID", "<urn:uuid:ff728363-2d5f-4f5f-b832-9552de1a6037>"},
					{"Content-Length", "693"}
					}, new Object[][] {
						{DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER + "' value", 1}
					}, null, null
			},
			{new Object[][] {
					{"WARC-Type", "continuation"},
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"WARC-Segment-Number", "1"},
					{"WARC-Segment-Origin-ID", "<urn:uuid:ff728363-2d5f-4f5f-b832-9552de1a6037>"},
					{"Content-Length", "693"}
					}, new Object[][] {
						{DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER + "' value", 2}
					}, null, null
			},
			{new Object[][] {
					{"WARC-Type", "continuation"},
					{"WARC-Target-URI", "http://www.archive.org/"},
					{"WARC-Date", "2008-04-30T20:48:26Z"},
					{"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
					{"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
					{"WARC-Segment-Number", "2"},
					{"WARC-Segment-Origin-ID", "<urn:uuid:ff728363-2d5f-4f5f-b832-9552de1a6037>"},
					{"Content-Length", "693"}
					}, null, null, null
			},
			{new Object[][] {
					{"WARC-Type", "metadata"},
					{"WARC-Date", "2008-04-30T20:48:25Z"},
					{"WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-1>"},
					{"Content-Length", "483"},
					{"Content-Type", "application/warc-fields"},
					{"WARC-Concurrent-To", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-2>"},
					{"WARC-Concurrent-To", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-3>"},
					{"WARC-Concurrent-To", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-4>"}
					}, null, null, new TestHeaderCallback() {
	    		    	public void callback(WarcHeader header) {
	    		    		Assert.assertEquals(3, header.warcConcurrentToList.size());
	    		    		WarcConcurrentTo concurrentTo;
	    		    		concurrentTo = header.warcConcurrentToList.get(0);
	    		    		Assert.assertEquals("<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-2>", concurrentTo.warcConcurrentToStr);
	    		    		concurrentTo = header.warcConcurrentToList.get(1);
	    		    		Assert.assertEquals("<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-3>", concurrentTo.warcConcurrentToStr);
	    		    		concurrentTo = header.warcConcurrentToList.get(2);
	    		    		Assert.assertEquals("<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-4>", concurrentTo.warcConcurrentToStr);
	    		    	}
	    			}
			}
    	};
    	test_checkfields_cases(cases);

        /*
         * Check fields policy.
         */

    	for (int rtype=1; rtype<WarcConstants.field_policy.length; ++rtype) {
    		for (int ftype=1; ftype<WarcConstants.field_policy[rtype].length; ++ftype) {
    			header = getTestHeader();
    			header.checkFieldPolicy(rtype, ftype, null, null);
	    		errors = header.diagnostics.getErrors();
	    		warnings = header.diagnostics.getWarnings();
    			switch (WarcConstants.field_policy[rtype][ftype]) {
    			case WarcConstants.POLICY_MANDATORY:
    				Assert.assertEquals(1, errors.size());
    				Assert.assertEquals(0, warnings.size());
    				diagnosis = errors.get(0);
                    Assert.assertEquals(DiagnosisType.REQUIRED_INVALID, diagnosis.type);
                    Assert.assertEquals("'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value", diagnosis.entity);
                    Assert.assertEquals(1, diagnosis.information.length);
                    break;
    			case WarcConstants.POLICY_SHALL:
    				Assert.assertEquals(0, errors.size());
    				Assert.assertEquals(1, warnings.size());
    				diagnosis = warnings.get(0);
                    Assert.assertEquals(DiagnosisType.REQUIRED_INVALID, diagnosis.type);
                    Assert.assertEquals("'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value", diagnosis.entity);
                    Assert.assertEquals(1, diagnosis.information.length);
                    break;
    			case WarcConstants.POLICY_SHALL_NOT:
    			case WarcConstants.POLICY_MAY_NOT:
    			case WarcConstants.POLICY_MAY:
    			case WarcConstants.POLICY_IGNORE:
    				Assert.assertEquals(0, errors.size());
    				Assert.assertEquals(0, warnings.size());
    				break;
    			}
    			header = getTestHeader();
    			header.checkFieldPolicy(rtype, ftype, new Object(), null);
	    		errors = header.diagnostics.getErrors();
	    		warnings = header.diagnostics.getWarnings();
    			switch (WarcConstants.field_policy[rtype][ftype]) {
    			case WarcConstants.POLICY_SHALL_NOT:
    				Assert.assertEquals(1, errors.size());
    				Assert.assertEquals(0, warnings.size());
    				diagnosis = errors.get(0);
                    Assert.assertEquals(DiagnosisType.UNDESIRED_DATA, diagnosis.type);
                    Assert.assertEquals("'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value", diagnosis.entity);
                    Assert.assertEquals(1, diagnosis.information.length);
                    break;
    			case WarcConstants.POLICY_MAY_NOT:
    				Assert.assertEquals(0, errors.size());
    				Assert.assertEquals(1, warnings.size());
    				diagnosis = warnings.get(0);
                    Assert.assertEquals(DiagnosisType.UNDESIRED_DATA, diagnosis.type);
                    Assert.assertEquals("'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value", diagnosis.entity);
                    Assert.assertEquals(1, diagnosis.information.length);
    				break;
    			case WarcConstants.POLICY_MANDATORY:
    			case WarcConstants.POLICY_SHALL:
    			case WarcConstants.POLICY_MAY:
    			case WarcConstants.POLICY_IGNORE:
    				Assert.assertEquals(0, errors.size());
    				Assert.assertEquals(0, warnings.size());
    				break;
    			}
    		}
    	}
	}

	public void test_headeradd_cases(Object[][] cases) {
		for (int i=0; i<cases.length; ++i) {
			String fieldName = (String)cases[i][0];
			Object[][] values = (Object[][])cases[i][1];
	    	header = getTestHeader();
			for (int j=0; j<values.length; ++j) {
				String fieldValue = (String)values[j][0];
				Object[][] expectedErrors = (Object[][])values[j][1];
				Object[][] expectedWarnings = (Object[][])values[j][2];
				TestHeaderCallback callback = (TestHeaderCallback)values[j][3];
				headerLine = header.addHeader(fieldName, fieldValue);
				Assert.assertNotNull(headerLine);
				Assert.assertEquals(fieldName, headerLine.name);
				Assert.assertEquals(fieldValue, headerLine.value);
				test_result(expectedErrors, expectedWarnings, callback);
			}
		}
	}

	public void test_headeradd_date_cases(Object[][] cases) {
		for (int i=0; i<cases.length; ++i) {
			String fieldName = (String)cases[i][0];
			Date dateFieldValue = (Date)cases[i][1];
			String fieldValueStr = (String)cases[i][2];
			String expectedFieldValueStr = (String)cases[i][3];
			Object[][] expectedErrors = (Object[][])cases[i][4];
			Object[][] expectedWarnings = (Object[][])cases[i][5];
			TestHeaderCallback callback = (TestHeaderCallback)cases[i][6];
	    	header = getTestHeader();
	    	headerLine = header.addHeader(fieldName, dateFieldValue, fieldValueStr);
			Assert.assertNotNull(headerLine);
			Assert.assertEquals(fieldName, headerLine.name);
			if (dateFieldValue != null || fieldValueStr != null) {
				Assert.assertEquals(expectedFieldValueStr, headerLine.value);
			} else {
				Assert.assertNull(headerLine.value);
			}
			test_result(expectedErrors, expectedWarnings, callback);
		}
	}

	public void test_checkfields_cases(Object[][] cases) {
		for (int i=0; i<cases.length; ++i) {
			Object[][] headers = (Object[][])cases[i][0];
			Object[][] expectedErrors = (Object[][])cases[i][1];
			Object[][] expectedWarnings = (Object[][])cases[i][2];
			TestHeaderCallback callback = (TestHeaderCallback)cases[i][3];
	    	header = getTestHeader();
			for (int j=0; j<headers.length; ++j) {
				String fieldName = (String)headers[j][0];
				String fieldValue = (String)headers[j][1];
				headerLine = header.addHeader(fieldName, fieldValue);
				Assert.assertNotNull(headerLine);
				Assert.assertEquals(fieldName, headerLine.name);
				Assert.assertEquals(fieldValue, headerLine.value);
			}
			header.checkFields();
			test_result(expectedErrors, expectedWarnings, callback);
		}
	}

	public void test_result(Object[][] expectedErrors, Object[][] expectedWarnings, TestHeaderCallback callback) {
		errors = header.diagnostics.getErrors();
		if (expectedErrors != null) {
			Assert.assertEquals(expectedErrors.length, errors.size());
			for (int k=0; k<expectedErrors.length; ++k) {
				diagnosis = errors.get(k);
				Assert.assertEquals((DiagnosisType)expectedErrors[k][0], diagnosis.type);
				Assert.assertEquals((String)expectedErrors[k][1], diagnosis.entity);
				Assert.assertEquals((Integer)expectedErrors[k][2], new Integer(diagnosis.information.length));
			}
		} else {
			Assert.assertEquals(0, errors.size());
		}
		warnings = header.diagnostics.getWarnings();
		if (expectedWarnings != null) {
			Assert.assertEquals(expectedWarnings.length, warnings.size());
			for (int k=0; k<expectedWarnings.length; ++k) {
				diagnosis = warnings.get(k);
				Assert.assertEquals((DiagnosisType)expectedWarnings[k][0], diagnosis.type);
				Assert.assertEquals((String)expectedWarnings[k][1], diagnosis.entity);
				Assert.assertEquals((Integer)expectedWarnings[k][2], new Integer(diagnosis.information.length));
			}
		} else {
			Assert.assertEquals(0, warnings.size());
		}
		if (callback != null) {
			callback.callback(header);
		}
	}

	public WarcHeader testHeader(String fieldName, String fieldValue, int expectedErrors, int expectedWarnings) {
    	header = getTestHeader();
		headerLine = header.addHeader(fieldName, fieldValue);
		Assert.assertNotNull(headerLine);
		Assert.assertEquals(fieldName, headerLine.name);
		Assert.assertEquals(fieldValue, headerLine.value);
		errors = header.diagnostics.getErrors();
		warnings = header.diagnostics.getWarnings();
		Assert.assertEquals(expectedErrors, errors.size());
		Assert.assertEquals(expectedWarnings, warnings.size());
		return header;
	}

	public void testEmptyHeader(String[] headers, String fieldValue) {
    	for (int i=0; i<headers.length; ++i) {
    		// debug
    		//System.out.println(headers[i]);
    		header = getTestHeader();
    		headerLine = header.addHeader(headers[i], fieldValue);
    		Assert.assertNotNull(headerLine);
    		Assert.assertEquals(headers[i], headerLine.name);
    		Assert.assertEquals(fieldValue, headerLine.value);
    		errors = header.diagnostics.getErrors();
    		warnings = header.diagnostics.getWarnings();
    		Assert.assertEquals(0, errors.size());
    		Assert.assertEquals(1, warnings.size());
    		diagnosis = warnings.get(0);
    		Assert.assertEquals(DiagnosisType.EMPTY, diagnosis.type);
    		Assert.assertEquals("'" + headers[i] + "' field", diagnosis.entity);
    		Assert.assertEquals(0, diagnosis.information.length);
    	}
    }

    public WarcHeader getTestHeader() {
    	WarcHeader header = new WarcHeader();
    	header.fieldParser = new WarcFieldParsers();
    	header.warcDateFormat = WarcDateParser.getWarcDateFormat();
    	header.diagnostics = new Diagnostics<Diagnosis>();
    	header.fieldParser.diagnostics = header.diagnostics;
    	return header;
    }

    public abstract class TestHeaderCallback {
    	public abstract void callback(WarcHeader header);
    } 

}
