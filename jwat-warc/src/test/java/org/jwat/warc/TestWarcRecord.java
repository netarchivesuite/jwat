package org.jwat.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestWarcRecord extends TestWarcRecordHelper {

	/**
	 * Test newlines parser used to check for required newlines after each
	 * WARC record.
	 */
	@Test
	public void test_warcrecord_parsenewlines() {
		WarcRecord record = new WarcRecord();
		byte[] bytes;
		int expectedNewlines;
		String expectedRemaining;
		ByteArrayInputStream in;
		ByteCountingPushBackInputStream pbin;
		int newlines;
		byte[] remainingBytes = new byte[16];
		int remaining;
		String remainingStr;

		Object[][] cases = {
				{"".getBytes(), 0, ""},
				{"\n".getBytes(), 1, ""},
				{"a".getBytes(), 0, "a"},
				{"\r\n".getBytes(), 1, ""},
				{"\ra".getBytes(), 0, "\ra"},
				{"\r\n\n".getBytes(), 2, ""},
				{"\n\r\n".getBytes(), 2, ""},
				{"\r\n\r\n".getBytes(), 2, ""},
				{"\r\n\na".getBytes(), 2, "a"},
				{"\n\r\na".getBytes(), 2, "a"},
				{"\r\n\r\na".getBytes(), 2, "a"}
		};

		try {
			for (int i=0; i<cases.length; ++i) {
				bytes = (byte[])cases[i][0];
				expectedNewlines = (Integer)cases[i][1];
				expectedRemaining = (String)cases[i][2];
				// debug
				//System.out.println(Base16.encodeArray(bytes));
				in = new ByteArrayInputStream(bytes);
				pbin = new ByteCountingPushBackInputStream(in, 16);
				newlines = record.parseNewLines(pbin);
				Assert.assertEquals(expectedNewlines, newlines);
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
	 * Test various combinations with and without content-length and
	 * content-type headers.
	 */
	@Test
	public void test_warcrecord_nopayload() {
		Object[][] warcMetainfoHeaders = null;
		Object[][] warcResourceHeaders = null;
		byte[] payloadBytes = null;
		Object[][] expectedDiagnoses;

		warcMetainfoHeaders = new Object[][] {
				{"WARC-Type", "warcinfo"},
				{"WARC-Date", "2008-04-30T20:48:25Z"},
				{"WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz"},
				{"WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>"}
		};

		warcResourceHeaders = new Object[][] {
				{"WARC-Type", "warcinfo"},
				{"WARC-Date", "2008-04-30T20:48:25Z"},
				{"WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz"},
				{"WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>"}
		};

		String payload = 
				"software: Heritrix/@VERSION@ http://crawler.archive.org\r\n"
				+ "ip: 192.168.1.13\r\n"
				+ "hostname: blackbook\r\n"
				+ "format: WARC File Format 0.17\r\n"
				+ "conformsTo: http://crawler.archive.org/warc/0.17/WARC0.17ISO.doc\r\n"
				+ "operator: Admin\r\n"
				+ "isPartOf: archive.org-shallow\r\n"
				+ "created: 2008-04-30T20:48:24Z\r\n"
				+ "description: archive.org shallow\r\n"
				+ "robots: classic\r\n"
				+ "http-header-user-agent: Mozilla/5.0 (compatible; heritrix/1.14.0 +http://crawler.archive.org)\r\n"
				+ "http-header-from: archive-crawler-agent@lists.sourceforge.net\r\n"
				+ "\r\n";

		try {
	    	payloadBytes = payload.getBytes("ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(483, payloadBytes.length);

		WarcReader reader;
		WarcRecord record;

		try {
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
	    	WarcWriter writer = WarcWriterFactory.getWriter(out, false);

	    	record = createRecord(writer, warcMetainfoHeaders, null, null);
	    	writeRecord(writer, record, null, null);
			writer.closeRecord();

	    	record = createRecord(writer, warcMetainfoHeaders, null, null);
	    	record.header.addHeader("Content-Length", "0");
	    	writeRecord(writer, record, null, null);
			writer.closeRecord();

			record = createRecord(writer, warcMetainfoHeaders, null, null);
	    	writeRecord(writer, record, null, payloadBytes);
			writer.closeRecord();

			record = createRecord(writer, warcMetainfoHeaders, null, null);
	    	record.header.addHeader("Content-Type", "application/warc-fields");
			record.header.addHeader("Content-Length", "483");
	    	writeRecord(writer, record, null, payloadBytes);
			writer.closeRecord();

			record = createRecord(writer, warcMetainfoHeaders, null, null);
			record.header.addHeader("Content-Length", "483");
	    	writeRecord(writer, record, null, payloadBytes);
			writer.closeRecord();

			record = createRecord(writer, warcResourceHeaders, null, null);
	    	record.header.addHeader("Content-Type", "text/plain");
			record.header.addHeader("Content-Length", "483");
	    	writeRecord(writer, record, null, payloadBytes);
			writer.closeRecord();

			writer.close();

			// debug
	    	//System.out.println(new String(out.toByteArray()));

			reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
			reader.setBlockDigestEnabled(true);
			reader.setPayloadDigestEnabled(true);
			int recordNumber = 0;
			while ((record = reader.getNextRecord()) != null) {
				record.close();
				++recordNumber;
				Assert.assertTrue(record.isClosed());
				switch (recordNumber) {
				case 1:
					Assert.assertEquals(2, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.REQUIRED_INVALID, "'Content-Length' header", 1},
							// FIXME !
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertNull(record.payload);
					Assert.assertNull(record.httpResponse);
					break;
				case 2:
					Assert.assertEquals(1, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							// FIXME !
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertNull(record.payload);
					Assert.assertNull(record.httpResponse);
					break;
				case 3:
					Assert.assertEquals(2, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.REQUIRED_INVALID, "'Content-Length' header", 1},
							// FIXME !
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertNull(record.payload);
					Assert.assertNull(record.httpResponse);
					break;
				case 4:
					Assert.assertEquals(3, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							// FIXME !
							{DiagnosisType.INVALID, "Data before WARC version", 0},
							{DiagnosisType.INVALID, "Empty lines before WARC version", 0},
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertNotNull(record.payload);
					Assert.assertNull(record.httpResponse);
					break;
				case 5:
					Assert.assertEquals(1, record.diagnostics.getErrors().size());
					Assert.assertEquals(1, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							// FIXME !
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.RECOMMENDED, "'Content-Type' header", 0}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());
					Assert.assertNotNull(record.payload);
					Assert.assertNull(record.httpResponse);
					break;
				case 6:
					Assert.assertEquals(1, record.diagnostics.getErrors().size());
					Assert.assertEquals(1, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							// FIXME !
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.RECOMMENDED, "'Content-Type' value", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());
					Assert.assertNotNull(record.payload);
					Assert.assertNull(record.httpResponse);
					break;
				}
				Assert.assertNull(record.computedBlockDigest);
				Assert.assertNull(record.computedPayloadDigest);
				Assert.assertNull(record.isValidBlockDigest);
				Assert.assertNull(record.isValidPayloadDigest);
				if (record.hasPayload()) {
					Assert.assertNotNull(record.payload);
					Assert.assertEquals(record.payload, record.getPayload());
					Assert.assertEquals(record.payload.getInputStream(), record.getPayloadContent());
				} else {
					Assert.assertNull(record.payload);
					Assert.assertNull(record.getPayload());
					Assert.assertNull(record.getPayloadContent());
				}
				if (record.diagnostics.getErrors().size() == 0 && record.diagnostics.getWarnings().size() == 0) {
					Assert.assertTrue(record.isCompliant());
				} else {
					Assert.assertFalse(record.isCompliant());
				}
			}
			reader.close();
			Assert.assertFalse(reader.isCompliant());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unexepected exception!");
		}
	}

	/**
	 * Test for valid and invalid http headers. In this case by setting the
	 * http header so low the http header can not be fully read.
	 */
	@Test
	public void test_warcrecord_httpresponse() {
		Object[][] warcResponseHeaders = null;
		byte[] httpHeaderBytes = null;
		byte[] payloadBytes = null;
		Object[][] expectedDiagnoses;

		warcResponseHeaders = new Object[][] {
    			{"WARC-Type", "response"},
    			{"WARC-Target-URI", "http://www.archive.org/robots.txt"},
    			{"WARC-Date", "2008-04-30T20:48:25Z"},
    			{"WARC-IP-Address", "207.241.229.39"},
    			{"WARC-Record-ID", "<urn:uuid:e7c9eff8-f5bc-4aeb-b3d2-9d3df99afb30>"},
    			{"Content-Length", "782"}
    	};

    	String httpHeader =
    			"HTTP/1.1 200 OK\r\n"
    			+ "Date: Wed, 30 Apr 2008 20:48:24 GMT\r\n"
    			+ "Server: Apache/2.0.54 (Ubuntu) PHP/5.0.5-2ubuntu1.4 mod_ssl/2.0.54 OpenSSL/0.9.7g\r\n"
    			+ "Last-Modified: Sat, 02 Feb 2008 19:40:44 GMT\r\n"
    			+ "ETag: \"47c3-1d3-11134700\"\r\n"
    			+ "Accept-Ranges: bytes\r\n"
    			+ "Content-Length: 467\r\n"
    			+ "Connection: close\r\n"
    			+ "Content-Type: text/plain; charset=UTF-8\r\n"
    			+ "\r\n";

    	String payload =
    			"##############################################\n"
    			+"#\n"
				+ "# Welcome to the Archive!\n"
				+ "#\n"
				+ "##############################################\n"
				+ "# Please crawl our files.\n"
				+ "# We appreciate if you can crawl responsibly.\n"
				+ "# Stay open!\n"
				+ "##############################################\n"
				+ "User-agent: *\n"
				+ "Disallow: /nothing---please-crawl-us--\n"
				+ "\n"
				+ "# slow down the ask jeeves crawler which was hitting our SE a little too fast\n"
				+ "# via collection pages.   --Feb2008 tracey--\n"
				+ "User-agent: Teoma\n"
				+ "Crawl-Delay: 10\n";

		try {
			httpHeaderBytes = httpHeader.getBytes("ISO8859-1");
	    	payloadBytes = payload.getBytes("ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(467, payloadBytes.length);
		Assert.assertEquals(782, httpHeaderBytes.length + payloadBytes.length);

		WarcReader reader;
		WarcRecord record;

		try {
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
	    	WarcWriter writer = WarcWriterFactory.getWriter(out, false);

	    	record = createRecord(writer, warcResponseHeaders, null, null);
			record.header.addHeader("Content-Type", "application/http; msgtype=custom");
	    	writeRecord(writer, record, httpHeaderBytes, payloadBytes);
			writer.closeRecord();

	    	record = createRecord(writer, warcResponseHeaders, null, null);
			record.header.addHeader("Content-Type", "application/http; msgtype=response");
	    	writeRecord(writer, record, httpHeaderBytes, payloadBytes);
			writer.closeRecord();

			writer.close();

			// debug
	    	//System.out.println(new String(out.toByteArray()));

			reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
			reader.setBlockDigestEnabled(false);
			reader.setPayloadDigestEnabled(false);
			int recordNumber = 0;
			while ((record = reader.getNextRecord()) != null) {
				record.close();
				++recordNumber;
				Assert.assertTrue(record.isClosed());
				switch (recordNumber) {
				case 1:
					Assert.assertEquals(1, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertNotNull(record.payload);
					Assert.assertNull(record.httpResponse);
					break;
				case 2:
					Assert.assertEquals(1, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertNotNull(record.payload);
					Assert.assertNotNull(record.httpResponse);
					Assert.assertTrue(record.httpResponse.isValid());
					break;
				}
				Assert.assertNull(record.computedBlockDigest);
				Assert.assertNull(record.computedPayloadDigest);
				Assert.assertNull(record.isValidBlockDigest);
				Assert.assertNull(record.isValidPayloadDigest);
				if (record.hasPayload()) {
					Assert.assertNotNull(record.payload);
					Assert.assertEquals(record.payload, record.getPayload());
					Assert.assertEquals(record.payload.getInputStream(), record.getPayloadContent());
				} else {
					Assert.assertNull(record.payload);
					Assert.assertNull(record.getPayload());
					Assert.assertNull(record.getPayloadContent());
				}
				if (record.diagnostics.getErrors().size() == 0 && record.diagnostics.getWarnings().size() == 0) {
					Assert.assertTrue(record.isCompliant());
				} else {
					Assert.assertFalse(record.isCompliant());
				}
			}
			reader.close();
			Assert.assertFalse(reader.isCompliant());

			reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
			reader.setBlockDigestEnabled(false);
			reader.setPayloadDigestEnabled(false);
			reader.payloadHeaderMaxSize = 32;
			recordNumber = 0;
			while ((record = reader.getNextRecord()) != null) {
				record.close();
				++recordNumber;
				Assert.assertTrue(record.isClosed());
				switch (recordNumber) {
				case 1:
					Assert.assertEquals(1, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertNotNull(record.payload);
					Assert.assertNull(record.httpResponse);
					break;
				case 2:
					Assert.assertEquals(2, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.ERROR, "http response", 1},
							{DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertNotNull(record.payload);
					Assert.assertNotNull(record.httpResponse);
					Assert.assertFalse(record.httpResponse.isValid());
					break;
				}
				Assert.assertNull(record.computedBlockDigest);
				Assert.assertNull(record.computedPayloadDigest);
				Assert.assertNull(record.isValidBlockDigest);
				Assert.assertNull(record.isValidPayloadDigest);
				if (record.hasPayload()) {
					Assert.assertNotNull(record.payload);
					Assert.assertEquals(record.payload, record.getPayload());
					Assert.assertEquals(record.payload.getInputStream(), record.getPayloadContent());
				} else {
					Assert.assertNull(record.payload);
					Assert.assertNull(record.getPayload());
					Assert.assertNull(record.getPayloadContent());
				}
				if (record.diagnostics.getErrors().size() == 0 && record.diagnostics.getWarnings().size() == 0) {
					Assert.assertTrue(record.isCompliant());
				} else {
					Assert.assertFalse(record.isCompliant());
				}
			}
			reader.close();
			Assert.assertFalse(reader.isCompliant());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unexepected exception!");
		}
	}

}
