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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.DiagnosisType;
import org.jwat.common.HttpHeader;

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
                {"\n".getBytes(), 1, true, false, false, false, ""},
                {"\r".getBytes(), 1, false, true, false, false, ""},
                {"a".getBytes(), 0, false, false, false, false, "a"},
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
                newlines = record.parseNewLines(pbin);
                Assert.assertEquals(expectedNewlines, newlines);
                Assert.assertEquals(expectedMissingCr, record.bMissingCr);
                Assert.assertEquals(expectedMissingLf, record.bMissingLf);
                Assert.assertEquals(expectedMisplacedCr, record.bMisplacedCr);
                Assert.assertEquals(expectedMisplacedLf, record.bMisplacedLf);
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
            Assert.assertTrue(writer.bExceptionOnContentLengthMismatch);
            writer.setExceptionOnContentLengthMismatch(false);
            Assert.assertFalse(writer.bExceptionOnContentLengthMismatch);

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
                    Assert.assertEquals(1, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    expectedDiagnoses = new Object[][] {
                            {DiagnosisType.REQUIRED_INVALID, "'Content-Length' header", 1}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
                    Assert.assertNull(record.payload);
                    Assert.assertNull(record.httpHeader);
                    break;
                case 2:
                    Assert.assertEquals(0, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    Assert.assertNull(record.payload);
                    Assert.assertNull(record.httpHeader);
                    break;
                case 3:
                    Assert.assertEquals(2, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    expectedDiagnoses = new Object[][] {
                            {DiagnosisType.REQUIRED_INVALID, "'Content-Length' header", 1},
                            {DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
                    Assert.assertNull(record.payload);
                    Assert.assertNull(record.httpHeader);
                    break;
                case 4:
                    Assert.assertEquals(2, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    expectedDiagnoses = new Object[][] {
                            {DiagnosisType.INVALID, "Data before WARC version", 0},
                            {DiagnosisType.INVALID, "Empty lines before WARC version", 0}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
                    Assert.assertNotNull(record.payload);
                    Assert.assertNull(record.httpHeader);
                    break;
                case 5:
                    Assert.assertEquals(0, record.diagnostics.getErrors().size());
                    Assert.assertEquals(1, record.diagnostics.getWarnings().size());
                    expectedDiagnoses = new Object[][] {
                            {DiagnosisType.RECOMMENDED, "'Content-Type' header", 0}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());
                    Assert.assertNotNull(record.payload);
                    Assert.assertNull(record.httpHeader);
                    break;
                case 6:
                    Assert.assertEquals(0, record.diagnostics.getErrors().size());
                    Assert.assertEquals(1, record.diagnostics.getWarnings().size());
                    expectedDiagnoses = new Object[][] {
                            {DiagnosisType.RECOMMENDED, "'Content-Type' value", 2}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());
                    Assert.assertNotNull(record.payload);
                    Assert.assertNull(record.httpHeader);
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
    public void test_warcrecord_httpheader() {
        Object[][] warcResponseHeaders = null;
        byte[] httpResponseHeaderBytes = null;
        byte[] responsePayloadBytes = null;
        Object[][] warcRequestHeaders = null;
        byte[] httpRequestHeaderBytes = null;
        Object[][] expectedDiagnoses;

        warcResponseHeaders = new Object[][] {
                {"WARC-Type", "response"},
                {"WARC-Target-URI", "http://www.archive.org/robots.txt"},
                {"WARC-Date", "2008-04-30T20:48:25Z"},
                {"WARC-IP-Address", "207.241.229.39"},
                {"WARC-Record-ID", "<urn:uuid:e7c9eff8-f5bc-4aeb-b3d2-9d3df99afb30>"},
                {"Content-Length", "782"}
        };

        String httpResponseHeader =
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

        String responsePayload =
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

        warcRequestHeaders = new Object[][] {
                {"WARC-Type", "request"},
                {"WARC-Target-URI", "http://www.archive.org/robots.txt"},
                {"WARC-Date", "2008-04-30T20:48:25Z"},
                {"WARC-Concurrent-To", "<urn:uuid:e7c9eff8-f5bc-4aeb-b3d2-9d3df99afb30>"},
                {"WARC-Record-ID", "<urn:uuid:fe11aa54-f8a7-4795-8cba-595f689a688f>"},
                {"Content-Length", "238"}
        };

        String httpRequestHeader =
                "GET /robots.txt HTTP/1.0\r\n"
                + "User-Agent: Mozilla/5.0 (compatible; heritrix/1.14.0 +http://crawler.archive.org)\r\n"
                + "From: archive-crawler-agent@lists.sourceforge.net\r\n"
                + "Connection: close\r\n"
                + "Referer: http://www.archive.org/\r\n"
                + "Host: www.archive.org\r\n"
                + "\r\n";

        try {
            httpResponseHeaderBytes = httpResponseHeader.getBytes("ISO8859-1");
            responsePayloadBytes = responsePayload.getBytes("ISO8859-1");
            httpRequestHeaderBytes = httpRequestHeader.getBytes("ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(467, responsePayloadBytes.length);
        Assert.assertEquals(782, httpResponseHeaderBytes.length + responsePayloadBytes.length);
        Assert.assertEquals(238, httpRequestHeaderBytes.length);

        WarcReader reader;
        WarcRecord record;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WarcWriter writer = WarcWriterFactory.getWriter(out, false);

            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=custom");
            writeRecord(writer, record, httpResponseHeaderBytes, responsePayloadBytes);
            writer.closeRecord();

            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=request");
            writeRecord(writer, record, httpResponseHeaderBytes, responsePayloadBytes);
            writer.closeRecord();

            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=response");
            writeRecord(writer, record, httpResponseHeaderBytes, responsePayloadBytes);
            writer.closeRecord();

            record = createRecord(writer, warcRequestHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=custom");
            writeRecord(writer, record, httpRequestHeaderBytes, null);
            writer.closeRecord();

            record = createRecord(writer, warcRequestHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=response");
            writeRecord(writer, record, httpRequestHeaderBytes, null);
            writer.closeRecord();

            record = createRecord(writer, warcRequestHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=request");
            writeRecord(writer, record, httpRequestHeaderBytes, null);
            writer.closeRecord();

            writer.close();

            // debug
            //System.out.println(new String(out.toByteArray()));

            Integer[] expectedHeaderTypes = {
                    0, HttpHeader.HT_REQUEST, HttpHeader.HT_RESPONSE,
                    0, HttpHeader.HT_RESPONSE, HttpHeader.HT_REQUEST
            };

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
                case 4:
                    Assert.assertEquals(0, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    Assert.assertNotNull(record.payload);
                    Assert.assertNull(record.httpHeader);
                    break;
                case 2:
                case 5:
                    Assert.assertEquals(1, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    expectedDiagnoses = new Object[][] {
                            {DiagnosisType.ERROR, "http header", 1}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
                    Assert.assertNotNull(record.payload);
                    Assert.assertNotNull(record.httpHeader);
                    Assert.assertFalse(record.httpHeader.isValid());
                    Assert.assertEquals(expectedHeaderTypes[recordNumber - 1], new Integer(record.httpHeader.headerType));
                    break;
                case 3:
                case 6:
                    Assert.assertEquals(0, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    Assert.assertNotNull(record.payload);
                    Assert.assertNotNull(record.httpHeader);
                    Assert.assertTrue(record.httpHeader.isValid());
                    Assert.assertEquals(expectedHeaderTypes[recordNumber - 1], new Integer(record.httpHeader.headerType));
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

            Assert.assertEquals(8192, reader.warcHeaderMaxSize);
            Assert.assertEquals(32768, reader.payloadHeaderMaxSize);
            Assert.assertEquals(8192, reader.getWarcHeaderMaxSize());
            Assert.assertEquals(32768, reader.getPayloadHeaderMaxSize());
            reader.setWarcHeaderMaxSize(1024);
            reader.setPayloadHeaderMaxSize(4096);
            Assert.assertEquals(1024, reader.warcHeaderMaxSize);
            Assert.assertEquals(4096, reader.payloadHeaderMaxSize);
            Assert.assertEquals(1024, reader.getWarcHeaderMaxSize());
            Assert.assertEquals(4096, reader.getPayloadHeaderMaxSize());

            reader.warcHeaderMaxSize = 8192;
            reader.payloadHeaderMaxSize = 32;
            recordNumber = 0;
            while ((record = reader.getNextRecord()) != null) {
                record.close();
                ++recordNumber;
                Assert.assertTrue(record.isClosed());
                switch (recordNumber) {
                case 1:
                case 4:
                    Assert.assertEquals(0, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    Assert.assertNotNull(record.payload);
                    Assert.assertNull(record.httpHeader);
                    break;
                case 2:
                case 3:
                case 5:
                case 6:
                    Assert.assertEquals(1, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    expectedDiagnoses = new Object[][] {
                            {DiagnosisType.ERROR, "http header", 1}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
                    Assert.assertNotNull(record.payload);
                    Assert.assertNotNull(record.httpHeader);
                    Assert.assertFalse(record.httpHeader.isValid());
                    Assert.assertEquals(expectedHeaderTypes[recordNumber - 1], new Integer(record.httpHeader.headerType));
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

    @Test
    public void test_warcrecord_truncated() {
        Object[][] warcResponseHeaders = null;
        byte[] httpHeaderBytes = null;
        byte[] payloadBytes = null;
        Object[][] expectedRecords;
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

        byte[] payloadSmallerBytes = new byte[payloadBytes.length - 100];
        byte[] payloadLargerBytes = new byte[payloadBytes.length + 100];

        // Fill larger and smaller payloads.
        System.arraycopy(payloadBytes, 0, payloadSmallerBytes, 0, payloadSmallerBytes.length);
        System.arraycopy(payloadBytes, 0, payloadLargerBytes, payloadLargerBytes.length - payloadBytes.length, payloadBytes.length);
        System.arraycopy(payloadBytes, 0, payloadLargerBytes, 0, payloadBytes.length);

        MessageDigest md_sha1 = null;
        try {
            md_sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md_sha1.reset();
        md_sha1.update(httpHeaderBytes);
        md_sha1.update(payloadBytes);
        byte[] blockDigestSha1 = md_sha1.digest();
        md_sha1.reset();
        md_sha1.update(payloadBytes);
        byte[] payloadDigestSha1 = md_sha1.digest();

        WarcReader reader;
        WarcRecord record;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WarcWriter writer = WarcWriterFactory.getWriter(out, false);
            Assert.assertTrue(writer.bExceptionOnContentLengthMismatch);
            writer.setExceptionOnContentLengthMismatch(false);
            Assert.assertFalse(writer.bExceptionOnContentLengthMismatch);

            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=response");
            writeRecord(writer, record, httpHeaderBytes, payloadBytes);
            writer.closeRecord();

            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=response");
            writeRecord(writer, record, httpHeaderBytes, payloadSmallerBytes);
            writer.closeRecord();

            // This record will be skipped.
            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=response");
            writeRecord(writer, record, httpHeaderBytes, payloadBytes);
            writer.closeRecord();

            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=response");
            writeRecord(writer, record, httpHeaderBytes, payloadBytes);
            writer.closeRecord();

            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=response");
            writeRecord(writer, record, httpHeaderBytes, payloadLargerBytes);
            writer.closeRecord();

            record = createRecord(writer, warcResponseHeaders, null, null);
            record.header.addHeader("Content-Type", "application/http; msgtype=response");
            writeRecord(writer, record, httpHeaderBytes, payloadSmallerBytes);
            writer.closeRecord();

            writer.close();

            // debug
            //System.out.println(new String(out.toByteArray()));

            md_sha1.reset();
            md_sha1.update(httpHeaderBytes);
            md_sha1.update(payloadSmallerBytes);
            md_sha1.update(WarcConstants.endMark);
            md_sha1.update(warcHeaderBytes, 0, 100-4);
            byte[] smallerBlockDigestSha1 = md_sha1.digest();
            md_sha1.reset();
            md_sha1.update(payloadSmallerBytes);
            md_sha1.update(WarcConstants.endMark);
            md_sha1.update(warcHeaderBytes, 0, 100-4);
            byte[] smallerPayloadDigestSha1 = md_sha1.digest();

            md_sha1.reset();
            md_sha1.update(httpHeaderBytes);
            md_sha1.update(payloadSmallerBytes);
            md_sha1.update(WarcConstants.endMark);
            byte[] truncatedBlockDigestSha1 = md_sha1.digest();
            md_sha1.reset();
            md_sha1.update(payloadSmallerBytes);
            md_sha1.update(WarcConstants.endMark);
            byte[] truncatedPayloadDigestSha1 = md_sha1.digest();

            /*
             *
             */
            expectedRecords = new Object[][] {
                    {"sha1", "base32", blockDigestSha1, payloadDigestSha1, new Object[][] {
                    }, new Object[][] {
                    }},
                    {"sha1", "base32", smallerBlockDigestSha1, smallerPayloadDigestSha1, new Object[][] {
                            {DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
                    }, new Object[][] {
                    }},
                    {"sha1", "base32", blockDigestSha1, payloadDigestSha1, new Object[][] {
                            {DiagnosisType.INVALID, "Data before WARC version", 0},
                            {DiagnosisType.INVALID, "Empty lines before WARC version", 0}
                    }, new Object[][] {
                    }},
                    {"sha1", "base32", blockDigestSha1, payloadDigestSha1, new Object[][] {
                            {DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
                    }, new Object[][] {
                    }},
                    {"sha1", "base32", truncatedBlockDigestSha1, truncatedPayloadDigestSha1, new Object[][] {
                            {DiagnosisType.INVALID, "Data before WARC version", 0},
                            {DiagnosisType.INVALID, "Empty lines before WARC version", 0},
                            {DiagnosisType.INVALID_DATA, "Payload length mismatch", 1},
                            {DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
                    }, new Object[][] {
                    }}
            };
            reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
            reader.setBlockDigestEnabled(true);
            reader.setBlockDigestAlgorithm("sha1");
            reader.setBlockDigestEncoding("base32");
            reader.setPayloadDigestEnabled(true);
            reader.setPayloadDigestAlgorithm("sha1");
            reader.setPayloadDigestEncoding("base32");
            //int recordNumber = 0;
            for (int i=0; i<expectedRecords.length; ++i) {
                record = reader.getNextRecord();
                record.close();
                //++recordNumber;
                Assert.assertTrue(record.isClosed());
                // debug
                //System.out.println(recordNumber);
                String expectedAlgorithm = (String)expectedRecords[i][0];
                String expectedEncoding = (String)expectedRecords[i][1];
                byte[] expectedBlockDigest = (byte[])expectedRecords[i][2];
                byte[] expectedPayloadDigest = (byte[])expectedRecords[i][3];
                Assert.assertNotNull(record.computedBlockDigest);
                Assert.assertNotNull(record.computedPayloadDigest);
                Assert.assertEquals(expectedAlgorithm, record.computedBlockDigest.algorithm);
                Assert.assertEquals(expectedAlgorithm, record.computedPayloadDigest.algorithm);
                Assert.assertEquals(expectedEncoding, record.computedBlockDigest.encoding);
                Assert.assertEquals(expectedEncoding, record.computedPayloadDigest.encoding);
                Assert.assertArrayEquals(expectedBlockDigest, record.computedBlockDigest.digestBytes);
                Assert.assertArrayEquals(expectedPayloadDigest, record.computedPayloadDigest.digestBytes);
                expectedDiagnoses = (Object[][])expectedRecords[i][4];
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
                expectedDiagnoses = (Object[][])expectedRecords[i][5];
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());
                Assert.assertNotNull(record.payload);
                Assert.assertNotNull(record.httpHeader);
                Assert.assertTrue(record.httpHeader.isValid());
                Assert.assertNull(record.header.warcBlockDigest);
                Assert.assertNull(record.header.warcPayloadDigest);
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
            Assert.assertNull(reader.getNextRecord());
            reader.close();
            Assert.assertFalse(reader.isCompliant());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }
    }

}
