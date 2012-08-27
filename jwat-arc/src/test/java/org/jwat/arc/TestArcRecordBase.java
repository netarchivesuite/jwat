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
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ContentType;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestArcRecordBase {

    @Test
    public void test_arcrecordbase() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String tmpStr;
        try {
            out.reset();
            out.write("2 0 InternetArchive".getBytes());
            out.write("\n".getBytes());
            out.write(ArcConstants.VERSION_2_BLOCK_DEF.getBytes());
            out.write("\n".getBytes());
            byte[] versionblock = out.toByteArray();

            out.reset();
            out.write("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz 192.168.1.2 20120712144000 text/htlm 200 checksum location 1234 filename ".getBytes());
            out.write(Integer.toString(versionblock.length).getBytes());
            out.write("\n".getBytes());
            byte[] recordline = out.toByteArray();

            out.reset();
            out.write(recordline);
            out.write(versionblock);
            out.write("\n".getBytes());

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            ArcReader reader = ArcReaderFactory.getReader(in);

            ArcRecordBase record = reader.getNextRecord();
            Assert.assertNotNull(record);
            ArcHeader header = record.header;
            Assert.assertNotNull(header);

            Assert.assertEquals(2, header.recordFieldVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            Assert.assertEquals("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz", header.urlStr);
            Assert.assertEquals("192.168.1.2", header.ipAddressStr);
            Assert.assertEquals("20120712144000", header.archiveDateStr);
            Assert.assertEquals("text/htlm", header.contentTypeStr);
            Assert.assertEquals("200", header.resultCodeStr);
            Assert.assertEquals("checksum", header.checksumStr);
            Assert.assertEquals("location", header.locationStr);
            Assert.assertEquals("1234", header.offsetStr);
            Assert.assertEquals("filename", header.filenameStr);
            Assert.assertEquals(Integer.toString(versionblock.length), header.archiveLengthStr);

            Assert.assertEquals(URI.create("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz"), header.urlUri);
            Assert.assertEquals("filedesc", header.urlScheme);
            Assert.assertEquals(InetAddress.getByName("192.168.1.2"), header.inetAddress);
            Assert.assertEquals(ArcDateParser.getDate("20120712144000"), header.archiveDate);
            Assert.assertEquals(ContentType.parseContentType("text/htlm"), header.contentType);
            Assert.assertEquals(new Integer(200), header.resultCode);
            Assert.assertEquals(new Long(1234), header.offset);
            Assert.assertEquals(new Long(versionblock.length), header.archiveLength);

            Assert.assertEquals(header.urlStr, record.getUrlStr());
            Assert.assertEquals(header.ipAddressStr, record.getIpAddress());
            Assert.assertEquals(header.archiveDateStr, record.getArchiveDateStr());
            Assert.assertEquals(header.contentTypeStr, record.getContentTypeStr());
            Assert.assertEquals(header.resultCodeStr, record.getResultCodeStr());
            Assert.assertEquals(header.checksumStr, record.getChecksum());
            Assert.assertEquals(header.locationStr, record.getLocation());
            Assert.assertEquals(header.offsetStr, record.getOffsetStr());
            Assert.assertEquals(header.filenameStr, record.getFileName());
            Assert.assertEquals(header.archiveLengthStr, record.getArchiveLengthStr());

            Assert.assertEquals(header.urlUri, record.getUrl());
            Assert.assertEquals(header.urlScheme, record.getScheme());
            Assert.assertEquals(header.inetAddress, record.getInetAddress());
            Assert.assertEquals(header.archiveDate, record.getArchiveDate());
            Assert.assertEquals(header.contentType, record.getContentType());
            Assert.assertEquals(header.resultCode, record.getResultCode());
            Assert.assertEquals(header.offset, record.getOffset());
            Assert.assertEquals(header.archiveLength, record.getArchiveLength());

            Assert.assertFalse(header.diagnostics.hasErrors());
            Assert.assertTrue(header.diagnostics.hasWarnings());

            Object[][] expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, ArcConstants.FN_CONTENT_TYPE, 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());

            /*
             * isValidStreamOfCRLF
             */

            ByteArrayInputStream bain;
            try {
                record.isValidStreamOfCRLF(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }

            bain = new ByteArrayInputStream(new byte[0]);
            Assert.assertTrue(record.isValidStreamOfCRLF(bain));

            bain = new ByteArrayInputStream(new byte[] {0x0a});
            Assert.assertTrue(record.isValidStreamOfCRLF(bain));

            bain = new ByteArrayInputStream(new byte[] {0x0d});
            Assert.assertTrue(record.isValidStreamOfCRLF(bain));

            bain = new ByteArrayInputStream(new byte[] {0x0a, 0x0d});
            Assert.assertTrue(record.isValidStreamOfCRLF(bain));

            bain = new ByteArrayInputStream(new byte[] {'a'});
            Assert.assertFalse(record.isValidStreamOfCRLF(bain));

            bain = new ByteArrayInputStream(new byte[] {0x0a, 0x0d, 'a'});
            Assert.assertFalse(record.isValidStreamOfCRLF(bain));

            record.close();
            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_arcrecordbase_payload_truncated() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String tmpStr;
        try {
            out.reset();
            out.write("2 0 InternetArchive".getBytes());
            out.write("\n".getBytes());
            out.write(ArcConstants.VERSION_2_BLOCK_DEF.getBytes());
            out.write("\n".getBytes());
            byte[] versionblock = out.toByteArray();

            out.reset();
            out.write("HTTP/1.0 200 Sending document\r\n".getBytes());
            out.write("MIME-version: 1.0\r\n".getBytes());
            out.write("Server: OSU/1.8\r\n".getBytes());
            out.write("Content-type: text/html\r\n".getBytes());
            out.write("Content-transfer-encoding: 8bit\r\n".getBytes());
            out.write("Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n".getBytes());
            out.write("\r\n".getBytes());
            out.write("Help, I've truncated!".getBytes());
            byte[] truncatedPayload = out.toByteArray();

            out.reset();
            out.write("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz 192.168.1.2 20120712144000 text/htlm 200 checksum location 1234 filename ".getBytes());
            out.write(Integer.toString(versionblock.length).getBytes());
            out.write("\n".getBytes());
            byte[] recordline = out.toByteArray();

            out.reset();
            out.write(recordline);
            out.write(versionblock);
            out.write("\n".getBytes());
            out.write("http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html 102 Checksum Location 2 Filename 4270\n".getBytes());
            out.write(truncatedPayload);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            ArcReader reader = ArcReaderFactory.getReader(in);
            ArcRecordBase record;
            ArcHeader header;
            Object[][] expectedDiagnoses;
            /*
             * Version block.
             */
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            header = record.header;
            Assert.assertNotNull(header);

            Assert.assertEquals(2, header.recordFieldVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            record.close();

            Assert.assertEquals("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz", header.urlStr);
            Assert.assertEquals("192.168.1.2", header.ipAddressStr);
            Assert.assertEquals("20120712144000", header.archiveDateStr);
            Assert.assertEquals("text/htlm", header.contentTypeStr);
            Assert.assertEquals("200", header.resultCodeStr);
            Assert.assertEquals("checksum", header.checksumStr);
            Assert.assertEquals("location", header.locationStr);
            Assert.assertEquals("1234", header.offsetStr);
            Assert.assertEquals("filename", header.filenameStr);
            Assert.assertEquals(Integer.toString(versionblock.length), header.archiveLengthStr);

            Assert.assertEquals(URI.create("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz"), header.urlUri);
            Assert.assertEquals("filedesc", header.urlScheme);
            Assert.assertEquals(InetAddress.getByName("192.168.1.2"), header.inetAddress);
            Assert.assertEquals(ArcDateParser.getDate("20120712144000"), header.archiveDate);
            Assert.assertEquals(ContentType.parseContentType("text/htlm"), header.contentType);
            Assert.assertEquals(new Integer(200), header.resultCode);
            Assert.assertEquals(new Long(1234), header.offset);
            Assert.assertEquals(new Long(versionblock.length), header.archiveLength);

            Assert.assertEquals(header.urlStr, record.getUrlStr());
            Assert.assertEquals(header.ipAddressStr, record.getIpAddress());
            Assert.assertEquals(header.archiveDateStr, record.getArchiveDateStr());
            Assert.assertEquals(header.contentTypeStr, record.getContentTypeStr());
            Assert.assertEquals(header.resultCodeStr, record.getResultCodeStr());
            Assert.assertEquals(header.checksumStr, record.getChecksum());
            Assert.assertEquals(header.locationStr, record.getLocation());
            Assert.assertEquals(header.offsetStr, record.getOffsetStr());
            Assert.assertEquals(header.filenameStr, record.getFileName());
            Assert.assertEquals(header.archiveLengthStr, record.getArchiveLengthStr());

            Assert.assertEquals(header.urlUri, record.getUrl());
            Assert.assertEquals(header.urlScheme, record.getScheme());
            Assert.assertEquals(header.inetAddress, record.getInetAddress());
            Assert.assertEquals(header.archiveDate, record.getArchiveDate());
            Assert.assertEquals(header.contentType, record.getContentType());
            Assert.assertEquals(header.resultCode, record.getResultCode());
            Assert.assertEquals(header.offset, record.getOffset());
            Assert.assertEquals(header.archiveLength, record.getArchiveLength());

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertTrue(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, ArcConstants.FN_CONTENT_TYPE, 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());
            /*
             * Arc record.
             */
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            header = record.header;
            Assert.assertNotNull(header);

            Assert.assertEquals(2, header.recordFieldVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            record.close();

            Assert.assertEquals("http://cctr.umkc.edu:80/user/jbenz/tst.htm", header.urlStr);
            Assert.assertEquals("134.193.4.1", header.ipAddressStr);
            Assert.assertEquals("19970417175710", header.archiveDateStr);
            Assert.assertEquals("text/html", header.contentTypeStr);
            Assert.assertEquals("102", header.resultCodeStr);
            Assert.assertEquals("Checksum", header.checksumStr);
            Assert.assertEquals("Location", header.locationStr);
            Assert.assertEquals("2", header.offsetStr);
            Assert.assertEquals("Filename", header.filenameStr);
            Assert.assertEquals("4270", header.archiveLengthStr);

            Assert.assertEquals(URI.create("http://cctr.umkc.edu:80/user/jbenz/tst.htm"), header.urlUri);
            Assert.assertEquals("http", header.urlScheme);
            Assert.assertEquals(InetAddress.getByName("134.193.4.1"), header.inetAddress);
            Assert.assertEquals(ArcDateParser.getDate("19970417175710"), header.archiveDate);
            Assert.assertEquals(ContentType.parseContentType("text/html"), header.contentType);
            Assert.assertEquals(new Integer(102), header.resultCode);
            Assert.assertEquals(new Long(2), header.offset);
            Assert.assertEquals(new Long(4270), header.archiveLength);

            Assert.assertEquals(header.urlStr, record.getUrlStr());
            Assert.assertEquals(header.ipAddressStr, record.getIpAddress());
            Assert.assertEquals(header.archiveDateStr, record.getArchiveDateStr());
            Assert.assertEquals(header.contentTypeStr, record.getContentTypeStr());
            Assert.assertEquals(header.resultCodeStr, record.getResultCodeStr());
            Assert.assertEquals(header.checksumStr, record.getChecksum());
            Assert.assertEquals(header.locationStr, record.getLocation());
            Assert.assertEquals(header.offsetStr, record.getOffsetStr());
            Assert.assertEquals(header.filenameStr, record.getFileName());
            Assert.assertEquals(header.archiveLengthStr, record.getArchiveLengthStr());

            Assert.assertEquals(header.urlUri, record.getUrl());
            Assert.assertEquals(header.urlScheme, record.getScheme());
            Assert.assertEquals(header.inetAddress, record.getInetAddress());
            Assert.assertEquals(header.archiveDate, record.getArchiveDate());
            Assert.assertEquals(header.contentType, record.getContentType());
            Assert.assertEquals(header.resultCode, record.getResultCode());
            Assert.assertEquals(header.offset, record.getOffset());
            Assert.assertEquals(header.archiveLength, record.getArchiveLength());

            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_DATA, "'" + ArcConstants.FN_OFFSET + "' value", 1},
                    {DiagnosisType.INVALID_DATA, "Payload length mismatch", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_arcrecordbase_typeorder() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in;
        ArcReader reader;
        ArcRecordBase record;
        ArcHeader header;
        Object[][] expectedDiagnoses;
        try {
            out.reset();
            out.write("2 0 InternetArchive".getBytes());
            out.write("\n".getBytes());
            out.write(ArcConstants.VERSION_2_BLOCK_DEF.getBytes());
            out.write("\n".getBytes());
            byte[] versionblock = out.toByteArray();

            out.reset();
            out.write("HTTP/1.0 200 Sending document\r\n".getBytes());
            out.write("MIME-version: 1.0\r\n".getBytes());
            out.write("Server: OSU/1.8\r\n".getBytes());
            out.write("Content-type: text/html\r\n".getBytes());
            out.write("Content-transfer-encoding: 8bit\r\n".getBytes());
            out.write("Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n".getBytes());
            out.write("\r\n".getBytes());
            out.write("Help, I've truncated!".getBytes());
            byte[] payload = out.toByteArray();
            /*
             * Normal
             */
            out.reset();
            out.write(getVersionBlock(out.size(), versionblock));
            out.write(versionblock);
            out.write("\n".getBytes());
            out.write(getArcRecord(out.size(), payload));
            out.write(payload);
            out.write("\n".getBytes());
            in = new ByteArrayInputStream(out.toByteArray());
            reader = ArcReaderFactory.getReader(in);
            // r1
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            // r2
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            // no r3
            record = reader.getNextRecord();
            Assert.assertNull(record);
            reader.close();
            Assert.assertTrue(reader.isCompliant());
            /*
             * Switched
             */
            out.reset();
            out.write(getArcRecord(out.size(), payload));
            out.write(payload);
            out.write("\n".getBytes());
            out.write(getVersionBlock(out.size(), versionblock));
            out.write(versionblock);
            out.write("\n".getBytes());
            in = new ByteArrayInputStream(out.toByteArray());
            reader = ArcReaderFactory.getReader(in);
            // r1
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
            // r2
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
            // no r3
            record = reader.getNextRecord();
            Assert.assertNull(record);
            reader.close();
            Assert.assertFalse(reader.isCompliant());
            /*
             * Version block * 2
             */
            out.reset();
            out.write(getVersionBlock(out.size(), versionblock));
            out.write(versionblock);
            out.write("\n".getBytes());
            out.write(getVersionBlock(out.size(), versionblock));
            out.write(versionblock);
            out.write("\n".getBytes());
            in = new ByteArrayInputStream(out.toByteArray());
            reader = ArcReaderFactory.getReader(in);
            // r1
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            // r2
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
            // no r3
            record = reader.getNextRecord();
            Assert.assertNull(record);
            reader.close();
            Assert.assertFalse(reader.isCompliant());
            /*
             * Arc record * 2
             */
            out.reset();
            out.write(getArcRecord(out.size(), payload));
            out.write(payload);
            out.write("\n".getBytes());
            out.write(getArcRecord(out.size(), payload));
            out.write(payload);
            out.write("\n".getBytes());
            in = new ByteArrayInputStream(out.toByteArray());
            reader = ArcReaderFactory.getReader(in);
            // r1
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
            // r2
            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            // no r3
            record = reader.getNextRecord();
            Assert.assertNull(record);
            reader.close();
            Assert.assertFalse(reader.isCompliant());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public byte[] getVersionBlock(long offset, byte[] versionblock) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz 192.168.1.2 20120712144000 text/plain 200 checksum location ".getBytes());
        out.write(Long.toString(offset).getBytes());
        out.write(" filename ".getBytes());
        out.write(Integer.toString(versionblock.length).getBytes());
        out.write("\n".getBytes());
        return out.toByteArray();
    }

    public byte[] getArcRecord(long offset, byte[] payload) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html 102 Checksum Location ".getBytes());
        out.write(Long.toString(offset).getBytes());
        out.write(" Filename ".getBytes());
        out.write(Integer.toString(payload.length).getBytes());
        out.write("\n".getBytes());
        return out.toByteArray();
    }

    @Test
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

            record = ArcVersionBlock.create(writer);
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
