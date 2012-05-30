package org.jwat.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.Base16;
import org.jwat.common.Base2;
import org.jwat.common.Base32;
import org.jwat.common.Base64;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestWarcRecord {

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
				Assert.assertTrue(record.isClosed());
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
				Assert.assertTrue(record.isClosed());
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
				Assert.assertTrue(record.isClosed());
			}
			reader.close();
			Assert.assertFalse(reader.isCompliant());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unexepected exception!");
		}
	}

	@Test
	public void test_warcrecord_digest() {
		Object[][] warcHeaders = null;
		byte[] httpHeaderBytes = null;
		byte[] payloadBytes = null;
    	Object[][] writedata;
    	Object[][] expectedDigests;
		Object[][] expectedDiagnoses;

    	ByteArrayOutputStream out;
    	WarcWriter writer;
		WarcReader reader;
    	WarcRecord record;

    	warcHeaders = new Object[][] {
    			{"WARC-Type", "response"},
    			{"WARC-Target-URI", "http://www.archive.org/robots.txt"},
    			{"WARC-Date", "2008-04-30T20:48:25Z"},
    			//{"WARC-Payload-Digest", "sha1:SUCGMUVXDKVB5CS2NL4R4JABNX7K466U"},
    			{"WARC-IP-Address", "207.241.229.39"},
    			{"WARC-Record-ID", "<urn:uuid:e7c9eff8-f5bc-4aeb-b3d2-9d3df99afb30>"},
    			{"Content-Type", "application/http; msgtype=response"},
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
			Assert.fail("Unexepected exception!");
		}

		Assert.assertEquals(467, payloadBytes.length);
		Assert.assertEquals(782, httpHeaderBytes.length + payloadBytes.length);
		/*
		 * Calculate reference digests.
		 */
		MessageDigest md_md5 = null;
    	try {
    	    md_md5 = MessageDigest.getInstance("MD5");
    	} catch (NoSuchAlgorithmException e) {
    	    e.printStackTrace();
    	}
    	MessageDigest md_sha1 = null;
    	try {
    	    md_sha1 = MessageDigest.getInstance("SHA1");
    	} catch (NoSuchAlgorithmException e) {
    	    e.printStackTrace();
    	}
    	
    	md_md5.reset();
    	md_md5.update(httpHeaderBytes);
    	md_md5.update(payloadBytes);
    	byte[] blockDigestMd5 = md_md5.digest();

    	md_md5.reset();
    	md_md5.update(payloadBytes);
    	byte[] payloadDigestMd5 = md_md5.digest();

    	md_sha1.reset();
    	md_sha1.update(httpHeaderBytes);
    	md_sha1.update(payloadBytes);
    	byte[] blockDigestSha1 = md_sha1.digest();

    	md_sha1.reset();
    	md_sha1.update(payloadBytes);
    	byte[] payloadDigestSha1 = md_sha1.digest();

    	Assert.assertEquals(16, WarcRecord.digestAlgorithmLength("MD5"));
    	Assert.assertEquals(20, WarcRecord.digestAlgorithmLength("SHA1"));
    	Assert.assertEquals(-1, WarcRecord.digestAlgorithmLength("SHAFT1"));

    	try {
	    	writedata = new Object[][] {
	    	    	{httpHeaderBytes, payloadBytes, null, null},
	    	    	{httpHeaderBytes, payloadBytes,
	    	    		new Object[] {"MD5", blockDigestMd5, "base16", Base16.encodeArray(blockDigestMd5)},
	    	    		new Object[] {"MD5", payloadDigestMd5, "base16", Base16.encodeArray(payloadDigestMd5)}
	   	    		},
	    	    	{httpHeaderBytes, payloadBytes,
	   	    			new Object[] {"MD5", blockDigestMd5, "base32", Base32.encodeArray(blockDigestMd5)},
	   	    			new Object[] {"MD5", payloadDigestMd5, "base32", Base32.encodeArray(payloadDigestMd5)}
	   	    		},
	    	    	{httpHeaderBytes, payloadBytes,
	   	    			new Object[] {"MD5", blockDigestMd5, "base64", Base64.encodeArray(blockDigestMd5)},
	   	    			new Object[] {"MD5", payloadDigestMd5, "base64", Base64.encodeArray(payloadDigestMd5)}
	   	    		},
	    	    	{httpHeaderBytes, payloadBytes,
	   	    			new Object[] {"SHA1", blockDigestSha1, "base16", Base16.encodeArray(blockDigestSha1)},
	   	    			new Object[] {"SHA1", payloadDigestSha1, "base16", Base16.encodeArray(payloadDigestSha1)}
	    	    	},
	    	    	{httpHeaderBytes, payloadBytes,
	    	    		new Object[] {"SHA1", blockDigestSha1, "base32", Base32.encodeArray(blockDigestSha1)},
	        	    	new Object[] {"SHA1", payloadDigestSha1, "base32", Base32.encodeArray(payloadDigestSha1)}
	    	    	},
	    	    	{httpHeaderBytes, payloadBytes,
	    	    		new Object[] {"SHA1", blockDigestSha1, "base64", Base64.encodeArray(blockDigestSha1)},
	    	    		new Object[] {"SHA1", payloadDigestSha1, "base64", Base64.encodeArray(payloadDigestSha1)}
	    	    	}
	    	};
	    	out = new ByteArrayOutputStream();
	    	writer = WarcWriterFactory.getWriter(out, false);
	    	writeRecords(writer, warcHeaders, writedata);
	    	writer.close();

	    	// debug
	    	//System.out.println(new String(out.toByteArray()));

	    	/*
			 * Disable digest validation.
			 */
			reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
			reader.setBlockDigestEnabled(false);
			reader.setPayloadDigestEnabled(false);
			while ((record = reader.getNextRecord()) != null) {
				record.close();
				Assert.assertTrue(record.isClosed());
				Assert.assertEquals(0, record.diagnostics.getErrors().size());
				Assert.assertEquals(0, record.diagnostics.getWarnings().size());
				Assert.assertNull(record.computedBlockDigest);
				Assert.assertNull(record.computedPayloadDigest);
				Assert.assertNull(record.isValidBlockDigest);
				Assert.assertNull(record.isValidPayloadDigest);
			}
			reader.close();
			Assert.assertTrue(reader.isCompliant());
			Assert.assertEquals(0, reader.errors);
			Assert.assertEquals(0, reader.warnings);
			/*
			 * Enable digest validation.
			 */
	    	expectedDigests = new Object[][] {
	    			{null, null, null, null},
	    			{"md5", "base16", blockDigestMd5, payloadDigestMd5},
	    			{"md5", "base32", blockDigestMd5, payloadDigestMd5},
	    	    	{"md5", "base64", blockDigestMd5, payloadDigestMd5},
	    	    	{"sha1", "base16", blockDigestSha1, payloadDigestSha1},
	    	    	{"sha1", "base32", blockDigestSha1, payloadDigestSha1},
	    	    	{"sha1", "base64", blockDigestSha1, payloadDigestSha1}
	    	};
			reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
			reader.setBlockDigestEnabled(true);
			reader.setPayloadDigestEnabled(true);
			for (int i=0; i<expectedDigests.length; ++i) {
				record = reader.getNextRecord();
				record.close();
				Assert.assertTrue(record.isClosed());
				String expectedAlgo = (String)expectedDigests[i][0];
				String expectedEnc = (String)expectedDigests[i][1];
				byte[] expectedBlockDigest = (byte[])expectedDigests[i][2];
				byte[] expectedPayloadDigest = (byte[])expectedDigests[i][3];
				if (expectedAlgo == null && expectedEnc == null) {
					Assert.assertNull(record.header.warcBlockDigest);
					Assert.assertNull(record.header.warcPayloadDigest);
					Assert.assertNull(record.computedBlockDigest);
					Assert.assertNull(record.computedPayloadDigest);
				} else {
					Assert.assertEquals(expectedAlgo, record.header.warcBlockDigest.algorithm);
					Assert.assertEquals(expectedAlgo, record.header.warcPayloadDigest.algorithm);
					Assert.assertEquals(expectedAlgo, record.computedBlockDigest.algorithm);
					Assert.assertEquals(expectedAlgo, record.computedPayloadDigest.algorithm);
					Assert.assertEquals(expectedEnc, record.header.warcBlockDigest.encoding);
					Assert.assertEquals(expectedEnc, record.header.warcPayloadDigest.encoding);
					Assert.assertEquals(expectedEnc, record.computedBlockDigest.encoding);
					Assert.assertEquals(expectedEnc, record.computedPayloadDigest.encoding);
					Assert.assertArrayEquals(expectedBlockDigest, record.header.warcBlockDigest.digestBytes);
					Assert.assertArrayEquals(expectedPayloadDigest, record.header.warcPayloadDigest.digestBytes);
					Assert.assertArrayEquals(expectedBlockDigest, record.computedBlockDigest.digestBytes);
					Assert.assertArrayEquals(expectedPayloadDigest, record.computedPayloadDigest.digestBytes);
					Assert.assertTrue(record.isValidBlockDigest);
					Assert.assertTrue(record.isValidPayloadDigest);
				}
				Assert.assertEquals(0, record.diagnostics.getErrors().size());
				Assert.assertEquals(0, record.diagnostics.getWarnings().size());
				/*
				if (record.header.warcBlockDigest != null) {
					System.out.println(record.header.warcBlockDigest.toStringFull());
				}
				if (record.computedBlockDigest != null) {
					System.out.println(record.computedBlockDigest.toStringFull());
				}
				if (record.header.warcPayloadDigest != null) {
					System.out.println(record.header.warcPayloadDigest.toStringFull());
				}
				if (record.computedPayloadDigest != null) {
					System.out.println(record.computedPayloadDigest.toStringFull());
				}
				*/
				if (record.hasPayload()) {
					Assert.assertNotNull(record.payload);
					Assert.assertEquals(record.payload, record.getPayload());
					Assert.assertEquals(record.payload.getInputStream(), record.getPayloadContent());
				} else {
					Assert.assertNull(record.payload);
					Assert.assertNull(record.getPayload());
					Assert.assertNull(record.getPayloadContent());
				}
			}
			Assert.assertNull(reader.getNextRecord());
			reader.close();
			Assert.assertTrue(reader.isCompliant());
			Assert.assertEquals(0, reader.errors);
			Assert.assertEquals(0, reader.warnings);
			/*
			 * Mismatch algorithm and digest.
			 */
	    	writedata = new Object[][] {
	    	    	{httpHeaderBytes, payloadBytes, null, null},
	    	    	{httpHeaderBytes, payloadBytes,
	    	    		new Object[] {"SHA1", blockDigestMd5, "base16", Base16.encodeArray(blockDigestMd5)},
	    	    		new Object[] {"SHA1", payloadDigestMd5, "base16", Base16.encodeArray(payloadDigestMd5)}
	   	    		},
	    	    	{httpHeaderBytes, payloadBytes,
	   	    			new Object[] {"SHA1", blockDigestMd5, "base32", Base32.encodeArray(blockDigestMd5)},
	   	    			new Object[] {"SHA1", payloadDigestMd5, "base32", Base32.encodeArray(payloadDigestMd5)}
	   	    		},
	    	    	{httpHeaderBytes, payloadBytes,
	   	    			new Object[] {"SHA1", blockDigestMd5, "base64", Base64.encodeArray(blockDigestMd5)},
	   	    			new Object[] {"SHA1", payloadDigestMd5, "base64", Base64.encodeArray(payloadDigestMd5)}
	   	    		},
	    	    	{httpHeaderBytes, payloadBytes,
	   	    			new Object[] {"MD5", blockDigestSha1, "base16", Base16.encodeArray(blockDigestSha1)},
	   	    			new Object[] {"MD5", payloadDigestSha1, "base16", Base16.encodeArray(payloadDigestSha1)}
	    	    	},
	    	    	{httpHeaderBytes, payloadBytes,
	    	    		new Object[] {"MD5", blockDigestSha1, "base32", Base32.encodeArray(blockDigestSha1)},
	        	    	new Object[] {"MD5", payloadDigestSha1, "base32", Base32.encodeArray(payloadDigestSha1)}
	    	    	},
	    	    	{httpHeaderBytes, payloadBytes,
	    	    		new Object[] {"MD5", blockDigestSha1, "base64", Base64.encodeArray(blockDigestSha1)},
	    	    		new Object[] {"MD5", payloadDigestSha1, "base64", Base64.encodeArray(payloadDigestSha1)}
	    	    	}
	    	};
	    	out = new ByteArrayOutputStream();
	    	writer = WarcWriterFactory.getWriter(out, false);
	    	writeRecords(writer, warcHeaders, writedata);
	    	writer.close();
	    	/*
			 * Disable digest validation.
			 */
	    	expectedDigests = new Object[][] {
	    			{null, null, null, null},
	    			{"sha1", null, null, null},
	    			{"sha1", null, null, null},
	    	    	{"sha1", null, null, null},
	    	    	{"md5", null, null, null},
	    	    	{"md5", null, null, null},
	    	    	{"md5", null, null, null}
	    	};
			reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
			reader.setBlockDigestEnabled(false);
			reader.setPayloadDigestEnabled(false);
			int recordNumber = 0;
			for (int i=0; i<expectedDigests.length; ++i) {
				record = reader.getNextRecord();
				record.close();
				++recordNumber;
				Assert.assertTrue(record.isClosed());
				String expectedAlgo = (String)expectedDigests[i][0];
				String expectedEnc = (String)expectedDigests[i][1];
				byte[] expectedBlockDigest = (byte[])expectedDigests[i][2];
				byte[] expectedPayloadDigest = (byte[])expectedDigests[i][3];
				if (expectedAlgo == null && expectedEnc == null) {
					Assert.assertEquals(0, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					Assert.assertNull(record.header.warcBlockDigest);
					Assert.assertNull(record.header.warcPayloadDigest);
					Assert.assertNull(record.computedBlockDigest);
					Assert.assertNull(record.computedPayloadDigest);
				} else {
					Assert.assertEquals(2, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.UNKNOWN, "Block digest encoding scheme", 1},
							{DiagnosisType.UNKNOWN, "Block digest encoding scheme", 1}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertEquals(expectedAlgo, record.header.warcBlockDigest.algorithm);
					Assert.assertEquals(expectedAlgo, record.header.warcPayloadDigest.algorithm);
					Assert.assertEquals(expectedEnc, record.header.warcBlockDigest.encoding);
					Assert.assertEquals(expectedEnc, record.header.warcPayloadDigest.encoding);
					Assert.assertArrayEquals(expectedBlockDigest, record.header.warcBlockDigest.digestBytes);
					Assert.assertArrayEquals(expectedPayloadDigest, record.header.warcPayloadDigest.digestBytes);
				}
				Assert.assertNull(record.computedBlockDigest);
				Assert.assertNull(record.computedPayloadDigest);
				Assert.assertNull(record.isValidBlockDigest);
				Assert.assertNull(record.isValidPayloadDigest);
			}
			reader.close();
			Assert.assertFalse(reader.isCompliant());
			Assert.assertEquals(12, reader.errors);
			Assert.assertEquals(0, reader.warnings);
			/*
			 * Enable digest validation.
			 */
	    	expectedDigests = new Object[][] {
	    			{null, null, null, null},
	    			{"sha1", null, blockDigestSha1, payloadDigestSha1},
	    			{"sha1", null, blockDigestSha1, payloadDigestSha1},
	    	    	{"sha1", null, blockDigestSha1, payloadDigestSha1},
	    	    	{"md5", null, blockDigestMd5, payloadDigestMd5},
	    	    	{"md5", null, blockDigestMd5, payloadDigestMd5},
	    	    	{"md5", null, blockDigestMd5, payloadDigestMd5}
	    	};
			reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
			reader.setBlockDigestEnabled(true);
			reader.setPayloadDigestEnabled(true);
			for (int i=0; i<expectedDigests.length; ++i) {
				record = reader.getNextRecord();
				record.close();
				Assert.assertTrue(record.isClosed());
				String expectedAlgo = (String)expectedDigests[i][0];
				String expectedEnc = (String)expectedDigests[i][1];
				byte[] expectedBlockDigest = (byte[])expectedDigests[i][2];
				byte[] expectedPayloadDigest = (byte[])expectedDigests[i][3];
				if (expectedAlgo == null && expectedEnc == null) {
					Assert.assertEquals(0, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					Assert.assertNull(record.header.warcBlockDigest);
					Assert.assertNull(record.header.warcPayloadDigest);
					Assert.assertNull(record.computedBlockDigest);
					Assert.assertNull(record.computedPayloadDigest);
				} else {
					Assert.assertEquals(2, record.diagnostics.getErrors().size());
					Assert.assertEquals(0, record.diagnostics.getWarnings().size());
					expectedDiagnoses = new Object[][] {
							{DiagnosisType.UNKNOWN, "Block digest encoding scheme", 1},
							{DiagnosisType.UNKNOWN, "Block digest encoding scheme", 1}
					};
					compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
					Assert.assertEquals(expectedAlgo, record.header.warcBlockDigest.algorithm);
					Assert.assertEquals(expectedAlgo, record.header.warcPayloadDigest.algorithm);
					Assert.assertEquals(expectedAlgo, record.computedBlockDigest.algorithm);
					Assert.assertEquals(expectedAlgo, record.computedPayloadDigest.algorithm);
					Assert.assertEquals(expectedEnc, record.header.warcBlockDigest.encoding);
					Assert.assertEquals(expectedEnc, record.header.warcPayloadDigest.encoding);
					Assert.assertEquals(reader.blockDigestEncoding, record.computedBlockDigest.encoding);
					Assert.assertEquals(reader.payloadDigestEncoding, record.computedPayloadDigest.encoding);
					Assert.assertNull(record.header.warcBlockDigest.digestBytes);
					Assert.assertNull(record.header.warcPayloadDigest.digestBytes);
					Assert.assertArrayEquals(expectedBlockDigest, record.computedBlockDigest.digestBytes);
					Assert.assertArrayEquals(expectedPayloadDigest, record.computedPayloadDigest.digestBytes);
					Assert.assertFalse(record.isValidBlockDigest);
					Assert.assertFalse(record.isValidPayloadDigest);
				}
				/*
				if (record.header.warcBlockDigest != null) {
					System.out.println(record.header.warcBlockDigest.toStringFull());
				}
				if (record.computedBlockDigest != null) {
					System.out.println(record.computedBlockDigest.toStringFull());
				}
				if (record.header.warcPayloadDigest != null) {
					System.out.println(record.header.warcPayloadDigest.toStringFull());
				}
				if (record.computedPayloadDigest != null) {
					System.out.println(record.computedPayloadDigest.toStringFull());
				}
				*/
				if (record.hasPayload()) {
					Assert.assertNotNull(record.payload);
					Assert.assertEquals(record.payload, record.getPayload());
					Assert.assertEquals(record.payload.getInputStream(), record.getPayloadContent());
				} else {
					Assert.assertNull(record.payload);
					Assert.assertNull(record.getPayload());
					Assert.assertNull(record.getPayloadContent());
				}
			}
			Assert.assertNull(reader.getNextRecord());
			reader.close();
			Assert.assertFalse(reader.isCompliant());
			Assert.assertEquals(12, reader.errors);
			Assert.assertEquals(0, reader.warnings);
			/*
			 * Unknown encoding.
			 */
	    	writedata = new Object[][] {
	    	    	{httpHeaderBytes, payloadBytes,
	   	    			new Object[] {"MD5", blockDigestSha1, "base2", Base2.encodeArray(blockDigestSha1)},
	   	    			new Object[] {"MD5", payloadDigestSha1, "base2", Base2.encodeArray(payloadDigestSha1)}
	    	    	},
	    	    	{httpHeaderBytes, payloadBytes,
	    	    		new Object[] {"SHA1", blockDigestMd5, "base2", Base2.encodeArray(blockDigestMd5)},
	    	    		new Object[] {"SHA1", payloadDigestMd5, "base2", Base2.encodeArray(payloadDigestMd5)}
	   	    		}
	    	};
	    	out = new ByteArrayOutputStream();
	    	writer = WarcWriterFactory.getWriter(out, false);
	    	writeRecords(writer, warcHeaders, writedata);
	    	writer.close();
    	} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unexepected exception!");
		}
	}

	public void compareDiagnoses(Object[][] expectedDiagnoses, List<Diagnosis> diagnosisList) {
		Assert.assertEquals(expectedDiagnoses.length, diagnosisList.size());
		Diagnosis diagnosis;
		for (int i=0; i<expectedDiagnoses.length; ++i) {
			diagnosis = diagnosisList.get(i);
			Assert.assertEquals(expectedDiagnoses[i][0], diagnosis.type);
			Assert.assertEquals(expectedDiagnoses[i][1], diagnosis.entity);
			Assert.assertEquals(expectedDiagnoses[i][2], diagnosis.information.length);
		}
	}

	public WarcRecord createRecord(WarcWriter writer, Object[][] warcHeaders, WarcDigest blockDigest, WarcDigest payloadDigest) {
    	WarcRecord record = WarcRecord.createRecord(writer);
    	for (int i=0; i<warcHeaders.length; ++i) {
    		String fieldName = (String)warcHeaders[i][0];
    		String fieldValue = (String)warcHeaders[i][1];
    		record.header.addHeader(fieldName, fieldValue);
    	}
    	record.header.warcBlockDigest = blockDigest;
    	record.header.warcPayloadDigest = payloadDigest;
    	return record;
	}

	public long writeRecord(WarcWriter writer, WarcRecord record, byte[] httpHeaderBytes, byte[] payloadBytes) {
    	long written = 0;
		try {
			writer.writeHeader(record);
			if (httpHeaderBytes != null) {
				written += writer.streamPayload(new ByteArrayInputStream(httpHeaderBytes), 0);
			}
			if (payloadBytes != null) {
				written += writer.streamPayload(new ByteArrayInputStream(payloadBytes), 0);
			}
			writer.closeRecord();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unexepected exception!");
		}
		return written;
	}

	public WarcDigest createWarcDigest(Object[] digestParams) {
		WarcDigest warcDigest = null;
		if (digestParams != null) {
			String algorithm = (String)digestParams[0];
			byte[] digest = (byte[])digestParams[1];
			String encoding = (String)digestParams[2];
			String digestString = (String)digestParams[3];
        	warcDigest = WarcDigest.createWarcDigest(algorithm, digest, encoding, digestString);
		}
		return warcDigest;
	}

	public void writeRecords(WarcWriter writer, Object[][] warcHeaders, Object[][] writedata) {
		byte[] httpHeaderBytes = null;
		byte[] payloadBytes = null;
		Object[] blockDigestParams;
		Object[] payloadDigestParams;
    	WarcDigest blockDigest;
    	WarcDigest payloadDigest;
    	WarcRecord record;
    	for (int i=0; i<writedata.length; ++i) {
    		httpHeaderBytes = (byte[])writedata[i][0];
    		payloadBytes = (byte[])writedata[i][1];
    		blockDigestParams = (Object[])writedata[i][2];
    		payloadDigestParams = (Object[])writedata[i][3];
        	blockDigest = createWarcDigest(blockDigestParams);
        	payloadDigest = createWarcDigest(payloadDigestParams);
        	record = createRecord(writer, warcHeaders, blockDigest, payloadDigest);
        	writeRecord(writer, record, httpHeaderBytes, payloadBytes);
    	}
	}

}
