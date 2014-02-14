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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.Base16;
import org.jwat.common.Base2;
import org.jwat.common.Base32;
import org.jwat.common.Base64;
import org.jwat.common.DiagnosisType;

/**
 * Test WARC digest parsing and detection of encoding.
 * Also tests digest validation.
 */
@RunWith(JUnit4.class)
public class TestWarcRecordDigests extends TestWarcRecordHelper {

    protected Object[][] warcHeaders = null;
    protected byte[] httpHeaderBytes = null;
    protected byte[] payloadBytes = null;

    protected byte[] blockDigestMd5;
    protected byte[] payloadDigestMd5;
    protected byte[] blockDigestSha1;
    protected byte[] payloadDigestSha1;

    protected void init_header1() {
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
            Assert.fail("Unexepected exception!");
        }
        MessageDigest md_sha1 = null;
        try {
            md_sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }

        md_md5.reset();
        md_md5.update(httpHeaderBytes);
        md_md5.update(payloadBytes);
        blockDigestMd5 = md_md5.digest();

        md_md5.reset();
        md_md5.update(payloadBytes);
        payloadDigestMd5 = md_md5.digest();

        md_sha1.reset();
        md_sha1.update(httpHeaderBytes);
        md_sha1.update(payloadBytes);
        blockDigestSha1 = md_sha1.digest();

        md_sha1.reset();
        md_sha1.update(payloadBytes);
        payloadDigestSha1 = md_sha1.digest();
    }

    @Test
    public void test_warcrecord_digests_valid() {
        Object[][] writedata;
        Object[][] expectedDigests;

        ByteArrayOutputStream out;
        WarcWriter writer;
        WarcReader reader;
        WarcRecord record;

        init_header1();

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

            // Save testfile.
            SaveWarcTestFiles.saveTestWarcRecordDigests(out.toByteArray(), true);

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
                if (record.diagnostics.getErrors().size() == 0 && record.diagnostics.getWarnings().size() == 0) {
                    Assert.assertTrue(record.isCompliant());
                } else {
                    Assert.assertFalse(record.isCompliant());
                }
            }
            Assert.assertNull(reader.getNextRecord());
            reader.close();
            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(0, reader.errors);
            Assert.assertEquals(0, reader.warnings);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }
    }

    @Test
    public void test_warcrecord_digests_invalid() {
        Object[][] writedata;
        Object[][] expectedDigests;
        Object[][] expectedDiagnoses;

        ByteArrayOutputStream out;
        WarcWriter writer;
        WarcReader reader;
        WarcRecord record;

        init_header1();

        try {
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

            // Save testfile.
            SaveWarcTestFiles.saveTestWarcRecordDigests(out.toByteArray(), false);

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
                            {DiagnosisType.UNKNOWN, "Record block digest encoding scheme", 1},
                            {DiagnosisType.UNKNOWN, "Record payload digest encoding scheme", 1}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
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
                if (record.diagnostics.getErrors().size() == 0 && record.diagnostics.getWarnings().size() == 0) {
                    Assert.assertTrue(record.isCompliant());
                } else {
                    Assert.assertFalse(record.isCompliant());
                }
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
                            {DiagnosisType.UNKNOWN, "Record block digest encoding scheme", 1},
                            {DiagnosisType.UNKNOWN, "Record payload digest encoding scheme", 1}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
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
                if (record.diagnostics.getErrors().size() == 0 && record.diagnostics.getWarnings().size() == 0) {
                    Assert.assertTrue(record.isCompliant());
                } else {
                    Assert.assertFalse(record.isCompliant());
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

            // Save testfile.
            SaveWarcTestFiles.saveTestWarcRecordDigests(out.toByteArray(), false);

            // debug
            //System.out.println(new String(out.toByteArray()));

            /*
             * Enable digest validation.
             */
            expectedDigests = new Object[][] {
                    {"md5", null, blockDigestMd5, payloadDigestMd5},
                    {"sha1", null, blockDigestSha1, payloadDigestSha1}
            };
            reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
            reader.setBlockDigestEnabled(true);
            reader.setPayloadDigestEnabled(true);
            reader.setBlockDigestEncoding("base2");
            reader.setPayloadDigestEncoding("base2");
            for (int i=0; i<expectedDigests.length; ++i) {
                record = reader.getNextRecord();
                record.close();
                Assert.assertTrue(record.isClosed());
                String expectedAlgo = (String)expectedDigests[i][0];
                String expectedEnc = (String)expectedDigests[i][1];
                byte[] expectedBlockDigest = (byte[])expectedDigests[i][2];
                byte[] expectedPayloadDigest = (byte[])expectedDigests[i][3];
                Assert.assertEquals(4, record.diagnostics.getErrors().size());
                Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                expectedDiagnoses = new Object[][] {
                        {DiagnosisType.UNKNOWN, "Record block digest encoding scheme", 1},
                        {DiagnosisType.UNKNOWN, "Default block digest encoding scheme", 1},
                        {DiagnosisType.UNKNOWN, "Record payload digest encoding scheme", 1},
                        {DiagnosisType.UNKNOWN, "Default payload digest encoding scheme", 1}
                };
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

                Assert.assertEquals(expectedAlgo, record.header.warcBlockDigest.algorithm);
                Assert.assertEquals(expectedAlgo, record.header.warcPayloadDigest.algorithm);
                Assert.assertEquals(expectedAlgo, record.computedBlockDigest.algorithm);
                Assert.assertEquals(expectedAlgo, record.computedPayloadDigest.algorithm);
                Assert.assertEquals(expectedEnc, record.header.warcBlockDigest.encoding);
                Assert.assertEquals(expectedEnc, record.header.warcPayloadDigest.encoding);
                Assert.assertEquals(expectedEnc, record.computedBlockDigest.encoding);
                Assert.assertEquals(expectedEnc, record.computedPayloadDigest.encoding);
                Assert.assertNull(record.header.warcBlockDigest.digestBytes);
                Assert.assertNull(record.header.warcPayloadDigest.digestBytes);
                Assert.assertArrayEquals(expectedBlockDigest, record.computedBlockDigest.digestBytes);
                Assert.assertArrayEquals(expectedPayloadDigest, record.computedPayloadDigest.digestBytes);
                Assert.assertFalse(record.isValidBlockDigest);
                Assert.assertFalse(record.isValidPayloadDigest);
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
            Assert.assertEquals(8, reader.errors);
            Assert.assertEquals(0, reader.warnings);
            /*
             * Switch block and payload digests..
             */
            writedata = new Object[][] {
                    {httpHeaderBytes, payloadBytes,
                        new Object[] {"MD5", payloadDigestMd5, "base32", Base32.encodeArray(payloadDigestMd5)},
                        new Object[] {"MD5", blockDigestMd5, "base32", Base32.encodeArray(blockDigestMd5)}
                    },
                    {httpHeaderBytes, payloadBytes,
                        new Object[] {"SHA1", payloadDigestSha1, "base32", Base32.encodeArray(payloadDigestSha1)},
                        new Object[] {"SHA1", blockDigestSha1, "base32", Base32.encodeArray(blockDigestSha1)}
                    }
            };
            out = new ByteArrayOutputStream();
            writer = WarcWriterFactory.getWriter(out, false);
            writeRecords(writer, warcHeaders, writedata);
            writer.close();

            // Save testfile.
            SaveWarcTestFiles.saveTestWarcRecordDigests(out.toByteArray(), false);

            // debug
            //System.out.println(new String(out.toByteArray()));

            /*
             * Enable digest validation.
             */
            expectedDigests = new Object[][] {
                    {"md5", "base32", blockDigestMd5, payloadDigestMd5},
                    {"sha1", "base32", blockDigestSha1, payloadDigestSha1}
            };
            reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
            reader.setBlockDigestEnabled(true);
            reader.setPayloadDigestEnabled(true);
            reader.setBlockDigestEncoding("base2");
            reader.setPayloadDigestEncoding("base2");
            for (int i=0; i<expectedDigests.length; ++i) {
                record = reader.getNextRecord();
                record.close();
                Assert.assertTrue(record.isClosed());
                String expectedAlgo = (String)expectedDigests[i][0];
                String expectedEnc = (String)expectedDigests[i][1];
                byte[] expectedBlockDigest = (byte[])expectedDigests[i][2];
                byte[] expectedPayloadDigest = (byte[])expectedDigests[i][3];
                Assert.assertEquals(2, record.diagnostics.getErrors().size());
                Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                expectedDiagnoses = new Object[][] {
                        {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2},
                        {DiagnosisType.INVALID_EXPECTED, "Incorrect payload digest", 2},
                };
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
                Assert.assertEquals(expectedAlgo, record.header.warcBlockDigest.algorithm);
                Assert.assertEquals(expectedAlgo, record.header.warcPayloadDigest.algorithm);
                Assert.assertEquals(expectedAlgo, record.computedBlockDigest.algorithm);
                Assert.assertEquals(expectedAlgo, record.computedPayloadDigest.algorithm);
                Assert.assertEquals(expectedEnc, record.header.warcBlockDigest.encoding);
                Assert.assertEquals(expectedEnc, record.header.warcPayloadDigest.encoding);
                Assert.assertEquals(expectedEnc, record.computedBlockDigest.encoding);
                Assert.assertEquals(expectedEnc, record.computedPayloadDigest.encoding);
                Assert.assertArrayEquals(expectedPayloadDigest, record.header.warcBlockDigest.digestBytes);
                Assert.assertArrayEquals(expectedBlockDigest, record.header.warcPayloadDigest.digestBytes);
                Assert.assertArrayEquals(expectedBlockDigest, record.computedBlockDigest.digestBytes);
                Assert.assertArrayEquals(expectedPayloadDigest, record.computedPayloadDigest.digestBytes);
                Assert.assertFalse(record.isValidBlockDigest);
                Assert.assertFalse(record.isValidPayloadDigest);
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
            Assert.assertEquals(4, reader.errors);
            Assert.assertEquals(0, reader.warnings);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }
    }

    @Test
    public void test_warcheader_digest_defaults() {
        Object[][] writedata;
        Object[][] expectedDigests;

        ByteArrayOutputStream out;
        WarcWriter writer;
        WarcReader reader;
        WarcRecord record;

        init_header1();

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

            // Save testfile.
            SaveWarcTestFiles.saveTestWarcRecordDigests(out.toByteArray(), true);

            // debug
            //System.out.println(new String(out.toByteArray()));

            Object[][] default_cases = {
                    {"md5", "base16", blockDigestMd5, payloadDigestMd5},
                    {"md5", "base32", blockDigestMd5, payloadDigestMd5},
                    {"md5", "base64", blockDigestMd5, payloadDigestMd5},
                    {"sha1", "base16", blockDigestSha1, payloadDigestSha1},
                    {"sha1", "base32", blockDigestSha1, payloadDigestSha1},
                    {"sha1", "base64", blockDigestSha1, payloadDigestSha1},
            };
            expectedDigests = new Object[][] {
                    {null, null, null, null},
                    {"md5", "base16", blockDigestMd5, payloadDigestMd5},
                    {"md5", "base32", blockDigestMd5, payloadDigestMd5},
                    {"md5", "base64", blockDigestMd5, payloadDigestMd5},
                    {"sha1", "base16", blockDigestSha1, payloadDigestSha1},
                    {"sha1", "base32", blockDigestSha1, payloadDigestSha1},
                    {"sha1", "base64", blockDigestSha1, payloadDigestSha1}
            };
            for (int i=0; i<default_cases.length; ++i) {
                String defaultAlgorithm = (String)default_cases[i][0];
                String defaultEncoding = (String)default_cases[i][1];
                byte[] defaultBlockDigest = (byte[])default_cases[i][2];
                byte[] defaultPayloadDigest = (byte[])default_cases[i][3];
                reader = WarcReaderFactory.getReader(new ByteArrayInputStream(out.toByteArray()));
                reader.setBlockDigestEnabled(true);
                reader.setPayloadDigestEnabled(true);
                Assert.assertTrue(reader.setBlockDigestAlgorithm(defaultAlgorithm));
                Assert.assertTrue(reader.setPayloadDigestAlgorithm(defaultAlgorithm));
                reader.setBlockDigestEncoding(defaultEncoding);
                reader.setPayloadDigestEncoding(defaultEncoding);
                for (int j=0; j<expectedDigests.length; ++j) {
                    record = reader.getNextRecord();
                    record.close();
                    Assert.assertTrue(record.isClosed());
                    String expectedAlgo = (String)expectedDigests[j][0];
                    String expectedEnc = (String)expectedDigests[j][1];
                    byte[] expectedBlockDigest = (byte[])expectedDigests[j][2];
                    byte[] expectedPayloadDigest = (byte[])expectedDigests[j][3];
                    Assert.assertEquals(0, record.diagnostics.getErrors().size());
                    Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                    if (expectedAlgo == null && expectedEnc == null) {
                        Assert.assertNull(record.header.warcBlockDigest);
                        Assert.assertNull(record.header.warcPayloadDigest);
                        Assert.assertEquals(defaultAlgorithm, record.computedBlockDigest.algorithm);
                        Assert.assertEquals(defaultAlgorithm, record.computedPayloadDigest.algorithm);
                        Assert.assertEquals(defaultEncoding, record.computedBlockDigest.encoding);
                        Assert.assertEquals(defaultEncoding, record.computedPayloadDigest.encoding);
                        Assert.assertArrayEquals(defaultBlockDigest, record.computedBlockDigest.digestBytes);
                        Assert.assertArrayEquals(defaultPayloadDigest, record.computedPayloadDigest.digestBytes);
                        Assert.assertNull(record.isValidBlockDigest);
                        Assert.assertNull(record.isValidPayloadDigest);
                    } else {
                        Assert.assertEquals(expectedAlgo, record.header.warcBlockDigest.algorithm);
                        Assert.assertEquals(expectedAlgo, record.header.warcPayloadDigest.algorithm);
                        Assert.assertEquals(expectedEnc, record.header.warcBlockDigest.encoding);
                        Assert.assertEquals(expectedEnc, record.header.warcPayloadDigest.encoding);
                        Assert.assertArrayEquals(expectedBlockDigest, record.header.warcBlockDigest.digestBytes);
                        Assert.assertArrayEquals(expectedPayloadDigest, record.header.warcPayloadDigest.digestBytes);
                        Assert.assertEquals(expectedAlgo, record.computedBlockDigest.algorithm);
                        Assert.assertEquals(expectedAlgo, record.computedPayloadDigest.algorithm);
                        Assert.assertEquals(expectedEnc, record.computedBlockDigest.encoding);
                        Assert.assertEquals(expectedEnc, record.computedPayloadDigest.encoding);
                        Assert.assertArrayEquals(expectedBlockDigest, record.computedBlockDigest.digestBytes);
                        Assert.assertArrayEquals(expectedPayloadDigest, record.computedPayloadDigest.digestBytes);
                        Assert.assertTrue(record.isValidBlockDigest);
                        Assert.assertTrue(record.isValidPayloadDigest);
                    }
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
                Assert.assertTrue(reader.isCompliant());
                Assert.assertEquals(0, reader.errors);
                Assert.assertEquals(0, reader.warnings);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }
    }

    @Test
    public void test_warcrecord_digestalgorithms() {
        Security.addProvider(new BouncyCastleProvider());
        SecureRandom random = new SecureRandom();
        MessageDigest md = null;
        byte[] blockDigest;
        byte[] payloadDigest;

        String[] algorithms = {"sha1", "sha-256", "sha-512", "tiger", "RipeMD128", "RipeMD160", "RipeMD256", "RipeMD320"};

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in;
        WarcWriter writer;
        WarcReader reader;
        WarcRecord record;
        WarcHeader header;
        int records = 0;

        init_header1();

        byte[] srcArr = new byte[ 8192 ];
        random.nextBytes( srcArr );

        Object[][] writedata = new Object[algorithms.length][];

        try {
            for (int i=0; i<algorithms.length; ++i) {
                writedata[i] = new Object[4];
                writedata[i][0] = httpHeaderBytes;
                writedata[i][1] = payloadBytes;

                md = MessageDigest.getInstance(algorithms[i]);
                md.reset();
                md.update(httpHeaderBytes);
                md.update(payloadBytes);
                blockDigest = md.digest();

                md.reset();
                md.update(payloadBytes);
                payloadDigest = md.digest();

                writedata[i][2] = new Object[] {algorithms[i], blockDigest, "base32", Base32.encodeArray(blockDigest)};
                writedata[i][3] = new Object[] {algorithms[i], payloadDigest, "base32", Base32.encodeArray(payloadDigest)};
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }

        try {
            out.reset();
            writer = WarcWriterFactory.getWriter(out, false);
            writeRecords(writer, warcHeaders, writedata);
            writer.close();

            // debug
            //System.out.println(new String(out.toByteArray()));

            in = new ByteArrayInputStream(out.toByteArray());
            reader = WarcReaderFactory.getReader(in);
            reader.setBlockDigestEnabled(true);
            reader.setPayloadDigestEnabled(true);
            while ((record = reader.getNextRecord()) != null) {
                header = record.header;
                record.close();

                Assert.assertTrue(record.isCompliant());
                Assert.assertEquals(0, record.diagnostics.getErrors().size());
                Assert.assertEquals(0, record.diagnostics.getWarnings().size());
                Assert.assertTrue(record.isValidBlockDigest);
                Assert.assertTrue(record.isValidPayloadDigest);

                Object[] digestParams = (Object[])writedata[records][2];
                Assert.assertEquals(digestParams[0].toString().toLowerCase(), header.warcBlockDigest.algorithm);
                Assert.assertArrayEquals((byte[])digestParams[1], header.warcBlockDigest.digestBytes);
                Assert.assertEquals(digestParams[2], header.warcBlockDigest.encoding);
                Assert.assertEquals(digestParams[3], header.warcBlockDigest.digestString);

                ++records;
            }
            Assert.assertNull(reader.getNextRecord());
            reader.close();
            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(0, reader.errors);
            Assert.assertEquals(0, reader.warnings);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexepected exception!");
        }
    }

}
