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
package org.jwat.arc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ContentType;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Uri;

// TODO "Last-Modified:" is considered a valid URI. Which it may be.
@RunWith(JUnit4.class)
public class TestArcWriter {

    @Test
    public void test_arcwriter_compressed() {
        test_arc_writer_fields(true);
        test_arc_writer_empty_fields_compressed(true);
    }

    @Test
    public void test_arcwriter_uncompressed() {
        test_arc_writer_fields(false);
        test_arc_writer_empty_fields_uncompressed(false);
    }

    public void test_arc_writer_fields(boolean compress) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArcWriter writer;
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        byte[] versionHeaderBytes;
        byte[] httpHeaderBytes;
        byte[] payloadBytes;
        long offset;
        try {
            httpHeaderBytes = ("HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n").getBytes("ISO8859-1");
            payloadBytes = ("Hello\n"
                    + "world!").getBytes("ISO8859-1");
            /*
             * V1.0 string valid.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_1, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = "filedesc://BNF-inktomi_arc39.20011005200622.arc.gz";
            record.header.ipAddressStr = "192.168.1.2";
            record.header.archiveDateStr = "20011005200622";
            record.header.contentTypeStr = "text/plain";
            record.header.archiveLengthStr = Long.toString(versionHeaderBytes.length);
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = "http://cctr.umkc.edu:80/user/jbenz/tst.htm";
            record.header.ipAddressStr = "134.193.4.1";
            record.header.archiveDateStr = "19970417175710";
            record.header.contentTypeStr = "text/html";
            record.header.archiveLengthStr = Long.toString(httpHeaderBytes.length + payloadBytes.length);
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v1_valid_arcfile(out.toByteArray(), versionHeaderBytes, httpHeaderBytes, payloadBytes);
            /*
             * V1.0 object valid.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_1, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlUri = Uri.create("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz");
            record.header.inetAddress= InetAddress.getByName("192.168.1.2");
            record.header.archiveDate = ArcDateParser.getDate("20011005200622");
            record.header.contentType = ContentType.parseContentType("text/plain");
            record.header.archiveLength = new Long(versionHeaderBytes.length);
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlUri = Uri.create("http://cctr.umkc.edu:80/user/jbenz/tst.htm");
            record.header.inetAddress = InetAddress.getByName("134.193.4.1");
            record.header.archiveDate = ArcDateParser.getDate("19970417175710");
            record.header.contentType = ContentType.parseContentType("text/html");
            record.header.archiveLength = new Long(httpHeaderBytes.length + payloadBytes.length);
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v1_valid_arcfile(out.toByteArray(), versionHeaderBytes, httpHeaderBytes, payloadBytes);
            /*
             * V2.0 string valid.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_2, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = "filedesc://BNF-inktomi_arc39.20011005200622.arc.gz";
            record.header.ipAddressStr = "192.168.1.2";
            record.header.archiveDateStr = "20011005200622";
            record.header.contentTypeStr = "text/plain";
            record.header.resultCodeStr = "200";
            record.header.checksumStr = "checksum";
            record.header.locationStr = "location";
            record.header.offsetStr = "0";
            record.header.filenameStr = "filename";
            record.header.archiveLengthStr = Long.toString(versionHeaderBytes.length);
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            offset = out.size();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = "http://cctr.umkc.edu:80/user/jbenz/tst.htm";
            record.header.ipAddressStr = "134.193.4.1";
            record.header.archiveDateStr = "19970417175710";
            record.header.contentTypeStr = "text/html";
            record.header.resultCodeStr = "404";
            record.header.checksumStr = "Checksum";
            record.header.locationStr = "Location";
            record.header.offsetStr = Long.toString(offset);
            record.header.filenameStr = "Filename";
            record.header.archiveLengthStr = Long.toString(httpHeaderBytes.length + payloadBytes.length);
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v2_valid_arcfile(out.toByteArray(), versionHeaderBytes, offset, httpHeaderBytes, payloadBytes);
            /*
             * V2.0 object valid.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_2, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 2;

            record.header.urlUri = Uri.create("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz");
            record.header.inetAddress= InetAddress.getByName("192.168.1.2");
            record.header.archiveDate = ArcDateParser.getDate("20011005200622");
            record.header.contentType = ContentType.parseContentType("text/plain");
            record.header.resultCode = new Integer(200);
            record.header.checksumStr = "checksum";
            record.header.locationStr = "location";
            record.header.offset = new Long(0);
            record.header.filenameStr = "filename";
            record.header.archiveLength = new Long(versionHeaderBytes.length);
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            offset = out.size();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlUri = Uri.create("http://cctr.umkc.edu:80/user/jbenz/tst.htm");
            record.header.inetAddress = InetAddress.getByName("134.193.4.1");
            record.header.archiveDate = ArcDateParser.getDate("19970417175710");
            record.header.contentType = ContentType.parseContentType("text/html");
            record.header.resultCode = new Integer(404);
            record.header.checksumStr = "Checksum";
            record.header.locationStr = "Location";
            record.header.offset = new Long(offset);
            record.header.filenameStr = "Filename";
            record.header.archiveLength = new Long(httpHeaderBytes.length + payloadBytes.length);
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v2_valid_arcfile(out.toByteArray(), versionHeaderBytes, offset, httpHeaderBytes, payloadBytes);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void assert_v1_valid_arcfile(byte[] arcfile,
            byte[] versionHeaderBytes,
            byte[] httpHeaderBytes,
            byte[] payloadBytes) {
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        ArcHeader header;
        ByteArrayInputStream in;
        ArcReader reader;
        String[] expected_fieldStrings;
        Object[] expected_fieldObjects;
        Object[][] expected_errors;
        Object[][] expected_warnings;
        try {
            in = new ByteArrayInputStream(arcfile);
            reader = ArcReaderFactory.getReader(in);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            Assert.assertTrue(versionHeader.isValid());
            Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    "filedesc://BNF-inktomi_arc39.20011005200622.arc.gz",
                    "192.168.1.2",
                    "20011005200622",
                    "text/plain",
                    null,
                    null,
                    null,
                    null,
                    null,
                    Integer.toString(versionHeaderBytes.length)
            };
            expected_fieldObjects = new Object[] {
                    Uri.create("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz"),
                    InetAddress.getByName("192.168.1.2"),
                    ArcDateParser.getDate("20011005200622"),
                    ContentType.parseContentType("text/plain"),
                    null,
                    null,
                    new Long(versionHeaderBytes.length)
            };
            expected_errors = new Object[][] {};
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    "http://cctr.umkc.edu:80/user/jbenz/tst.htm",
                    "134.193.4.1",
                    "19970417175710",
                    "text/html",
                    null,
                    null,
                    null,
                    null,
                    null,
                    Long.toString(httpHeaderBytes.length + payloadBytes.length)
            };
            expected_fieldObjects = new Object[] {
                    Uri.create("http://cctr.umkc.edu:80/user/jbenz/tst.htm"),
                    InetAddress.getByName("134.193.4.1"),
                    ArcDateParser.getDate("19970417175710"),
                    ContentType.parseContentType("text/html"),
                    null,
                    null,
                    new Long(httpHeaderBytes.length + payloadBytes.length)
            };
            expected_errors = new Object[][] {};
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertTrue(reader.isCompliant());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void assert_v2_valid_arcfile(byte[] arcfile,
            byte[] versionHeaderBytes,
            long offset,
            byte[] httpHeaderBytes,
            byte[] payloadBytes) {
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        ArcHeader header;
        ByteArrayInputStream in;
        ArcReader reader;
        String[] expected_fieldStrings;
        Object[] expected_fieldObjects;
        Object[][] expected_errors;
        Object[][] expected_warnings;
        try {
            in = new ByteArrayInputStream(arcfile);
            reader = ArcReaderFactory.getReader(in);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            Assert.assertTrue(versionHeader.isValid());
            Assert.assertEquals(ArcVersion.VERSION_2, versionHeader.version);
            Assert.assertEquals(2, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    "filedesc://BNF-inktomi_arc39.20011005200622.arc.gz",
                    "192.168.1.2",
                    "20011005200622",
                    "text/plain",
                    "200",
                    "checksum",
                    "location",
                    "0",
                    "filename",
                    Integer.toString(versionHeaderBytes.length)
            };
            expected_fieldObjects = new Object[] {
                    Uri.create("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz"),
                    InetAddress.getByName("192.168.1.2"),
                    ArcDateParser.getDate("20011005200622"),
                    ContentType.parseContentType("text/plain"),
                    Integer.parseInt("200"),
                    Long.parseLong("0"),
                    new Long(versionHeaderBytes.length)
            };
            expected_errors = new Object[][] {};
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_2, versionHeader.version);
            //Assert.assertEquals(2, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    "http://cctr.umkc.edu:80/user/jbenz/tst.htm",
                    "134.193.4.1",
                    "19970417175710",
                    "text/html",
                    "404",
                    "Checksum",
                    "Location",
                    Long.toString(offset),
                    "Filename",
                    Long.toString(httpHeaderBytes.length + payloadBytes.length)
            };
            expected_fieldObjects = new Object[] {
                    Uri.create("http://cctr.umkc.edu:80/user/jbenz/tst.htm"),
                    InetAddress.getByName("134.193.4.1"),
                    ArcDateParser.getDate("19970417175710"),
                    ContentType.parseContentType("text/html"),
                    Integer.parseInt("404"),
                    Long.valueOf(offset),
                    new Long(httpHeaderBytes.length + payloadBytes.length)
            };
            expected_errors = new Object[][] {};
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertTrue(reader.isCompliant());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void test_arc_writer_empty_fields_compressed(boolean compress) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArcWriter writer;
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        byte[] versionHeaderBytes;
        byte[] httpHeaderBytes;
        byte[] payloadBytes;
        try {
            httpHeaderBytes = ("HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n").getBytes("ISO8859-1");
            payloadBytes = ("Hello\n"
                    + "world!").getBytes("ISO8859-1");
            /*
             * V1.0 string empty.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_1, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = "";
            record.header.ipAddressStr = "";
            record.header.archiveDateStr = "";
            record.header.contentTypeStr = "";
            record.header.archiveLengthStr = "";
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = "";
            record.header.ipAddressStr = "";
            record.header.archiveDateStr = "";
            record.header.contentTypeStr = "";
            record.header.archiveLengthStr = "";
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v1_empty_arcfile_compressed(out.toByteArray());
            /*
             * V1.0 string null.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_1, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = null;
            record.header.ipAddressStr = null;
            record.header.archiveDateStr = null;
            record.header.contentTypeStr = null;
            record.header.archiveLengthStr = null;
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = null;
            record.header.ipAddressStr = null;
            record.header.archiveDateStr = null;
            record.header.contentTypeStr = null;
            record.header.archiveLengthStr = null;
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v1_empty_arcfile_compressed(out.toByteArray());
            /*
             * V2.0 string empty.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_2, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = "";
            record.header.ipAddressStr = "";
            record.header.archiveDateStr = "";
            record.header.contentTypeStr = "";
            record.header.resultCodeStr = "";
            record.header.checksumStr = "";
            record.header.locationStr = "";
            record.header.offsetStr = "";
            record.header.filenameStr = "";
            record.header.archiveLengthStr = "";
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = "";
            record.header.ipAddressStr = "";
            record.header.archiveDateStr = "";
            record.header.contentTypeStr = "";
            record.header.resultCodeStr = "";
            record.header.checksumStr = "";
            record.header.locationStr = "";
            record.header.offsetStr = "";
            record.header.filenameStr = "";
            record.header.archiveLengthStr = "";
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v2_empty_arcfile_compressed(out.toByteArray());
            /*
             * V2.0 string empty.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_2, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = null;
            record.header.ipAddressStr = null;
            record.header.archiveDateStr = null;
            record.header.contentTypeStr = null;
            record.header.resultCodeStr = null;
            record.header.checksumStr = null;
            record.header.locationStr = null;
            record.header.offsetStr = null;
            record.header.filenameStr = null;
            record.header.archiveLengthStr = null;
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = null;
            record.header.ipAddressStr = null;
            record.header.archiveDateStr = null;
            record.header.contentTypeStr = null;
            record.header.resultCodeStr = null;
            record.header.checksumStr = null;
            record.header.locationStr = null;
            record.header.offsetStr = null;
            record.header.filenameStr = null;
            record.header.archiveLengthStr = null;
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v2_empty_arcfile_compressed(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void assert_v1_empty_arcfile_compressed(byte[] arcfile) {
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        ArcHeader header;
        ByteArrayInputStream in;
        ArcReader reader;
        String[] expected_fieldStrings;
        Object[] expected_fieldObjects;
        Object[][] expected_errors;
        Object[][] expected_warnings;
        try {
            in = new ByteArrayInputStream(arcfile);
            reader = ArcReaderFactory.getReader(in);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0},
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void assert_v2_empty_arcfile_compressed(byte[] arcfile) {
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        ArcHeader header;
        ByteArrayInputStream in;
        ArcReader reader;
        String[] expected_fieldStrings;
        Object[] expected_fieldObjects;
        Object[][] expected_errors;
        Object[][] expected_warnings;
        try {
            in = new ByteArrayInputStream(arcfile);
            reader = ArcReaderFactory.getReader(in);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_RESULT_CODE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_OFFSET + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_FILENAME + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0},
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_RESULT_CODE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_OFFSET + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_FILENAME + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void test_arc_writer_empty_fields_uncompressed(boolean compress) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArcWriter writer;
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        byte[] versionHeaderBytes;
        byte[] httpHeaderBytes;
        byte[] payloadBytes;
        try {
            httpHeaderBytes = ("HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n").getBytes("ISO8859-1");
            payloadBytes = ("Hello\n"
                    + "world!").getBytes("ISO8859-1");
            /*
             * V1.0 string empty.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_1, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = "";
            record.header.ipAddressStr = "";
            record.header.archiveDateStr = "";
            record.header.contentTypeStr = "";
            record.header.archiveLengthStr = "";
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = "";
            record.header.ipAddressStr = "";
            record.header.archiveDateStr = "";
            record.header.contentTypeStr = "";
            record.header.archiveLengthStr = "";
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v1_empty_arcfile_uncompressed(out.toByteArray());
            /*
             * V1.0 string null.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_1, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = null;
            record.header.ipAddressStr = null;
            record.header.archiveDateStr = null;
            record.header.contentTypeStr = null;
            record.header.archiveLengthStr = null;
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 1;
            record.header.urlStr = null;
            record.header.ipAddressStr = null;
            record.header.archiveDateStr = null;
            record.header.contentTypeStr = null;
            record.header.archiveLengthStr = null;
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v1_empty_arcfile_uncompressed(out.toByteArray());
            /*
             * V2.0 string empty.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_2, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = "";
            record.header.ipAddressStr = "";
            record.header.archiveDateStr = "";
            record.header.contentTypeStr = "";
            record.header.resultCodeStr = "";
            record.header.checksumStr = "";
            record.header.locationStr = "";
            record.header.offsetStr = "";
            record.header.filenameStr = "";
            record.header.archiveLengthStr = "";
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = "";
            record.header.ipAddressStr = "";
            record.header.archiveDateStr = "";
            record.header.contentTypeStr = "";
            record.header.resultCodeStr = "";
            record.header.checksumStr = "";
            record.header.locationStr = "";
            record.header.offsetStr = "";
            record.header.filenameStr = "";
            record.header.archiveLengthStr = "";
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v2_empty_arcfile_uncompressed(out.toByteArray());
            /*
             * V2.0 string empty.
             */
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            versionHeader = ArcVersionHeader.create(ArcVersion.VERSION_2, "Netarkivet.dk");
            versionHeader.rebuild();
            versionHeaderBytes = versionHeader.getHeader();

            record = ArcVersionBlock.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = null;
            record.header.ipAddressStr = null;
            record.header.archiveDateStr = null;
            record.header.contentTypeStr = null;
            record.header.resultCodeStr = null;
            record.header.checksumStr = null;
            record.header.locationStr = null;
            record.header.offsetStr = null;
            record.header.filenameStr = null;
            record.header.archiveLengthStr = null;
            writer.writeHeader(record);
            writer.writePayload(versionHeaderBytes);
            writer.closeRecord();

            record = ArcRecord.createRecord(writer);
            record.header.recordFieldVersion = 2;
            record.header.urlStr = null;
            record.header.ipAddressStr = null;
            record.header.archiveDateStr = null;
            record.header.contentTypeStr = null;
            record.header.resultCodeStr = null;
            record.header.checksumStr = null;
            record.header.locationStr = null;
            record.header.offsetStr = null;
            record.header.filenameStr = null;
            record.header.archiveLengthStr = null;
            writer.writeHeader(record);
            writer.writePayload(httpHeaderBytes);
            writer.writePayload(payloadBytes);
            writer.closeRecord();

            System.out.println(new String(out.toByteArray()));

            assert_v2_empty_arcfile_uncompressed(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void assert_v1_empty_arcfile_uncompressed(byte[] arcfile) {
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        ArcHeader header;
        ByteArrayInputStream in;
        ArcReader reader;
        String[] expected_fieldStrings;
        Object[] expected_fieldObjects;
        Object[][] expected_errors;
        Object[][] expected_warnings;
        try {
            in = new ByteArrayInputStream(arcfile);
            reader = ArcReaderFactory.getReader(in);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertFalse(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0},
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    ArcConstants.FN_URL,
                    ArcConstants.FN_IP_ADDRESS,
                    ArcConstants.FN_ARCHIVE_DATE,
                    ArcConstants.FN_CONTENT_TYPE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ArcConstants.FN_ARCHIVE_LENGTH
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2},
                    {DiagnosisType.INVALID, "Data before ARC record", 0}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    "Last-Modified:",
                    "Tuesday,",
                    "21-Aug-96",
                    "05:14:05",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "GMT"
            };
            expected_fieldObjects = new Object[] {
                    Uri.create("Last-Modified:"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    //{DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2},
                    {DiagnosisType.INVALID, "Data before ARC record", 0}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void assert_v2_empty_arcfile_uncompressed(byte[] arcfile) {
        ArcVersionHeader versionHeader;
        ArcRecordBase record;
        ArcHeader header;
        ByteArrayInputStream in;
        ArcReader reader;
        String[] expected_fieldStrings;
        Object[] expected_fieldObjects;
        Object[][] expected_errors;
        Object[][] expected_warnings;
        try {
            in = new ByteArrayInputStream(arcfile);
            reader = ArcReaderFactory.getReader(in);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertFalse(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_RESULT_CODE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_OFFSET + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_FILENAME + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0},
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    ArcConstants.FN_URL,
                    ArcConstants.FN_IP_ADDRESS,
                    ArcConstants.FN_ARCHIVE_DATE,
                    ArcConstants.FN_CONTENT_TYPE,
                    ArcConstants.FN_RESULT_CODE,
                    ArcConstants.FN_CHECKSUM,
                    ArcConstants.FN_LOCATION,
                    ArcConstants.FN_OFFSET,
                    ArcConstants.FN_FILENAME,
                    ArcConstants.FN_ARCHIVE_LENGTH
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_RESULT_CODE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_OFFSET + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2},
                    {DiagnosisType.INVALID, "Data before ARC record", 0}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_fieldObjects = new Object[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_RESULT_CODE + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_OFFSET + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_FILENAME + "' value", 0},
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            versionHeader = record.versionHeader;
            //Assert.assertTrue(versionHeader.isValid());
            //Assert.assertEquals(ArcVersion.VERSION_1, versionHeader.version);
            //Assert.assertEquals(1, versionHeader.blockDescVersion);
            header = record.header;
            expected_fieldStrings = new String[] {
                    "Last-Modified:",
                    "Tuesday,",
                    "21-Aug-96",
                    "05:14:05",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "GMT"
            };
            expected_fieldObjects = new Object[] {
                    Uri.create("Last-Modified:"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
            expected_errors = new Object[][] {
                    //{DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2},
                    {DiagnosisType.INVALID, "Data before ARC record", 0}
            };
            expected_warnings = new Object[][] {};
            TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void test_arcrecord() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArcWriter writer;
        ArcRecordBase record;
        byte[] recordHeader;
        ByteArrayInputStream in;
        byte[] payload;

        String mdData;
        try {
            out.reset();
            writer = ArcWriterFactory.getWriter(out, false);

            writer.setExceptionOnContentLengthMismatch(false);

            mdData = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n";
            mdData += "<arcmetadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:arc=\"http://archive.org/arc/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://archive.org/arc/1.0/\" xsi:schemaLocation=\"http://archive.org/arc/1.0/ http://www.archive.org/arc/1.0/arc.xsd\">\r\n";
            mdData += "<arc:software>Heritrix @VERSION@ http://crawler.archive.org</arc:software>\r\n";
            mdData += "<arc:hostname>blackbook</arc:hostname>\r\n";
            mdData += "<arc:ip>192.168.1.13</arc:ip>\r\n";
            mdData += "<dcterms:isPartOf>archive.org-shallow</dcterms:isPartOf>\r\n";
            mdData += "<dc:description>archive.org shallow</dc:description>\r\n";
            mdData += "<arc:operator>Admin</arc:operator>\r\n";
            mdData += "<ns0:date xmlns:ns0=\"http://purl.org/dc/elements/1.1/\" xsi:type=\"dcterms:W3CDTF\">2008-04-30T20:48:24+00:00</ns0:date>\r\n";
            mdData += "<arc:http-header-user-agent>Mozilla/5.0 (compatible; heritrix/1.14.0 +http://crawler.archive.org)</arc:http-header-user-agent>\r\n";
            mdData += "<arc:http-header-from>archive-crawler-agent@lists.sourceforge.net</arc:http-header-from>\r\n";
            mdData += "<arc:robots>classic</arc:robots>\r\n";
            mdData += "<dc:format>ARC file version 1.1</dc:format>\r\n";
            mdData += "<dcterms:conformsTo xsi:type=\"dcterms:URI\">http://www.archive.org/web/researcher/ArcFileFormat.php</dcterms:conformsTo>\r\n";
            mdData += "</arcmetadata>\r\n";
            payload = mdData.getBytes();

            record = ArcVersionBlock.createRecord(writer);
            record.header.urlStr = "filedesc://BNF-inktomi_arc39.20011005200622.arc.gz";
            record.header.ipAddressStr = "0.0.0.0";
            record.header.archiveDateStr = "20011005200622";
            record.header.contentTypeStr = "text/plain";
            record.header.archiveLengthStr = "76";
            writer.writeHeader(record);

            writer.writePayload(payload);

            writer.closeRecord();

            writer.close();

            System.out.println(out);

            in = new ByteArrayInputStream(out.toByteArray());
            ArcReader reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);

            record = reader.getNextRecord();
            // TODO wrong
            Assert.assertNull(record);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
