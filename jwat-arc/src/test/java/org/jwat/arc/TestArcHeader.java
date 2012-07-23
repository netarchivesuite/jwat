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
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;

@RunWith(JUnit4.class)
public class TestArcHeader {

    @Test
    public void test_archeader_fields() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in;
        ByteCountingPushBackInputStream pbin;
        ArcWriter writer;
        ArcReader reader;
        byte[] bytes;
        ArcHeader header;
        boolean success;
        String tmpStr;
        String[] fields;
        Object[][] expectedDiagnoses;
        try {
            /*
             * Writer.
             */
            out.reset();
            Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

            writer = ArcWriterFactory.getWriter(out, false);
            writer.fieldParsers.diagnostics = diagnostics;
            header = ArcHeader.initHeader(writer, diagnostics);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * Valid record V1.
             */
            bytes = "http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html 4270\n".getBytes();

            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in);
            reader.fieldParsers.diagnostics = diagnostics;
            header = ArcHeader.initHeader(reader, 42L, diagnostics);

            pbin = ((ArcReaderUncompressed)reader).in;
            success = header.parseHeader(pbin);

            Assert.assertTrue(success);
            Assert.assertEquals(1, header.recordFieldVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            Assert.assertEquals("http://cctr.umkc.edu:80/user/jbenz/tst.htm", header.urlStr);
            Assert.assertEquals("134.193.4.1", header.ipAddressStr);
            Assert.assertEquals("19970417175710", header.archiveDateStr);
            Assert.assertEquals("text/html", header.contentTypeStr);
            Assert.assertNull(header.resultCodeStr);
            Assert.assertNull(header.checksumStr);
            Assert.assertNull(header.locationStr);
            Assert.assertNull(header.offsetStr);
            Assert.assertNull(header.filenameStr);
            Assert.assertEquals("4270", header.archiveLengthStr);

            Assert.assertEquals(URI.create("http://cctr.umkc.edu:80/user/jbenz/tst.htm"), header.urlUri);
            Assert.assertEquals(InetAddress.getByName("134.193.4.1"), header.inetAddress);
            Assert.assertEquals(ArcDateParser.getDate("19970417175710"), header.archiveDate);
            Assert.assertEquals(ContentType.parseContentType("text/html"), header.contentType);
            Assert.assertNull(header.resultCode);
            Assert.assertNull(header.offset);
            Assert.assertEquals(new Long(4270), header.archiveLength);

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());
            /*
             * Invalid number of fields.
             */
            header = ArcHeader.initHeader(reader, 42L, diagnostics);
            fields = new String[4];
            header.parseHeaders(fields);

            Assert.assertEquals(0, header.recordFieldVersion);

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());
            /*
             * Null fields v1.
             */
            header = ArcHeader.initHeader(reader, 42L, diagnostics);
            fields = new String[5];
            header.parseHeaders(fields);

            Assert.assertEquals(1, header.recordFieldVersion);

            Assert.assertNull(header.urlStr);
            Assert.assertNull(header.ipAddressStr);
            Assert.assertNull(header.archiveDateStr);
            Assert.assertNull(header.contentTypeStr);
            Assert.assertNull(header.resultCodeStr);
            Assert.assertNull(header.checksumStr);
            Assert.assertNull(header.locationStr);
            Assert.assertNull(header.offsetStr);
            Assert.assertNull(header.filenameStr);
            Assert.assertNull(header.archiveLengthStr);

            Assert.assertNull(header.urlUri);
            Assert.assertNull(header.inetAddress);
            Assert.assertNull(header.archiveDate);
            Assert.assertNull(header.contentType);
            Assert.assertNull(header.resultCode);
            Assert.assertNull(header.offset);
            Assert.assertNull(header.archiveLength);

            Assert.assertTrue(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_URL + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_IP_ADDRESS + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_ARCHIVE_DATE + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_CONTENT_TYPE + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' field", 0}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getWarnings());
            diagnostics.reset();
            /*
             * Valid record v2.
             */
            bytes = "http://www.antiaction.com/ 192.168.1.2 20120712144000 text/htlm 200 checksum location 1234 filename 40\n".getBytes();

            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in);
            reader.fieldParsers.diagnostics = diagnostics;
            header = ArcHeader.initHeader(reader, 42L, diagnostics);

            pbin = ((ArcReaderUncompressed)reader).in;
            success = header.parseHeader(pbin);

            Assert.assertTrue(success);
            Assert.assertEquals(2, header.recordFieldVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            Assert.assertEquals("http://www.antiaction.com/", header.urlStr);
            Assert.assertEquals("192.168.1.2", header.ipAddressStr);
            Assert.assertEquals("20120712144000", header.archiveDateStr);
            Assert.assertEquals("text/htlm", header.contentTypeStr);
            Assert.assertEquals("200", header.resultCodeStr);
            Assert.assertEquals("checksum", header.checksumStr);
            Assert.assertEquals("location", header.locationStr);
            Assert.assertEquals("1234", header.offsetStr);
            Assert.assertEquals("filename", header.filenameStr);
            Assert.assertEquals("40", header.archiveLengthStr);

            Assert.assertEquals(URI.create("http://www.antiaction.com/"), header.urlUri);
            Assert.assertEquals(InetAddress.getByName("192.168.1.2"), header.inetAddress);
            Assert.assertEquals(ArcDateParser.getDate("20120712144000"), header.archiveDate);
            Assert.assertEquals(ContentType.parseContentType("text/htlm"), header.contentType);
            Assert.assertEquals(new Integer(200), header.resultCode);
            Assert.assertEquals(new Long(1234), header.offset);
            Assert.assertEquals(new Long(40), header.archiveLength);

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());
            /*
             * Null fields v2.
             */
            header = ArcHeader.initHeader(reader, 42L, diagnostics);
            fields = new String[10];
            header.parseHeaders(fields);

            Assert.assertEquals(2, header.recordFieldVersion);

            Assert.assertNull(header.urlStr);
            Assert.assertNull(header.ipAddressStr);
            Assert.assertNull(header.archiveDateStr);
            Assert.assertNull(header.contentTypeStr);
            Assert.assertNull(header.resultCodeStr);
            Assert.assertNull(header.checksumStr);
            Assert.assertNull(header.locationStr);
            Assert.assertNull(header.offsetStr);
            Assert.assertNull(header.filenameStr);
            Assert.assertNull(header.archiveLengthStr);

            Assert.assertNull(header.urlUri);
            Assert.assertNull(header.inetAddress);
            Assert.assertNull(header.archiveDate);
            Assert.assertNull(header.contentType);
            Assert.assertNull(header.resultCode);
            Assert.assertNull(header.offset);
            Assert.assertNull(header.archiveLength);

            Assert.assertTrue(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_URL + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_IP_ADDRESS + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_ARCHIVE_DATE + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_CONTENT_TYPE + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_RESULT_CODE + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_CHECKSUM + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_LOCATION + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_OFFSET + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_FILENAME + "' field", 0},
                    {DiagnosisType.EMPTY, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' field", 0}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getWarnings());
            diagnostics.reset();
            /*
             * Invalid record V1.
             */
            bytes = "4270 http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html\n".getBytes();

            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in);
            reader.fieldParsers.diagnostics = diagnostics;
            header = ArcHeader.initHeader(reader, 42L, diagnostics);

            pbin = ((ArcReaderUncompressed)reader).in;
            success = header.parseHeader(pbin);

            Assert.assertTrue(success);
            Assert.assertEquals(1, header.recordFieldVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            Assert.assertEquals("4270", header.urlStr);
            Assert.assertEquals("http://cctr.umkc.edu:80/user/jbenz/tst.htm", header.ipAddressStr);
            Assert.assertEquals("134.193.4.1", header.archiveDateStr);
            Assert.assertEquals("19970417175710", header.contentTypeStr);
            Assert.assertNull(header.resultCodeStr);
            Assert.assertNull(header.checksumStr);
            Assert.assertNull(header.locationStr);
            Assert.assertNull(header.offsetStr);
            Assert.assertNull(header.filenameStr);
            Assert.assertEquals("text/html", header.archiveLengthStr);

            Assert.assertNull(header.urlUri);
            Assert.assertNull(header.inetAddress);
            Assert.assertNull(header.archiveDate);
            Assert.assertNull(header.contentType);
            Assert.assertNull(header.resultCode);
            Assert.assertNull(header.offset);
            Assert.assertNull(header.archiveLength);

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertTrue(header.diagnostics.hasErrors());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();
            /*
             * Invalid record v2.
             */
            bytes = "40 http://www.antiaction.com/ 192.168.1.2 20120712144000 text/htlm 200 checksum location 1234 filename\n".getBytes();

            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in);
            reader.fieldParsers.diagnostics = diagnostics;
            header = ArcHeader.initHeader(reader, 42L, diagnostics);

            pbin = ((ArcReaderUncompressed)reader).in;
            success = header.parseHeader(pbin);

            Assert.assertTrue(success);
            Assert.assertEquals(2, header.recordFieldVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            Assert.assertEquals("40", header.urlStr);
            Assert.assertEquals("http://www.antiaction.com/", header.ipAddressStr);
            Assert.assertEquals("192.168.1.2", header.archiveDateStr);
            Assert.assertEquals("20120712144000", header.contentTypeStr);
            Assert.assertEquals("text/htlm", header.resultCodeStr);
            Assert.assertEquals("200", header.checksumStr);
            Assert.assertEquals("checksum", header.locationStr);
            Assert.assertEquals("location", header.offsetStr);
            Assert.assertEquals("1234", header.filenameStr);
            Assert.assertEquals("filename", header.archiveLengthStr);

            Assert.assertNull(header.urlUri);
            Assert.assertNull(header.inetAddress);
            Assert.assertNull(header.archiveDate);
            Assert.assertNull(header.contentType);
            Assert.assertNull(header.resultCode);
            Assert.assertNull(header.offset);
            Assert.assertNull(header.archiveLength);

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertTrue(header.diagnostics.hasErrors());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_RESULT_CODE + "' value", 2},
                    //{DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CHECKSUM + "' value", 2},
                    //{DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_LOCATION + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_OFFSET + "' value", 2},
                    //{DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_FILENAME + "' value", 2},
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();
            /*
             * Valid record v2 with "-".
             */
            bytes = "http://www.antiaction.com/ 192.168.1.2 20120712144000 text/htlm 200 checksum - 1234 filename 40\n".getBytes();

            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in);
            reader.fieldParsers.diagnostics = diagnostics;
            header = ArcHeader.initHeader(reader, 42L, diagnostics);

            pbin = ((ArcReaderUncompressed)reader).in;
            success = header.parseHeader(pbin);

            Assert.assertTrue(success);
            Assert.assertEquals(2, header.recordFieldVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            Assert.assertEquals("http://www.antiaction.com/", header.urlStr);
            Assert.assertEquals("192.168.1.2", header.ipAddressStr);
            Assert.assertEquals("20120712144000", header.archiveDateStr);
            Assert.assertEquals("text/htlm", header.contentTypeStr);
            Assert.assertEquals("200", header.resultCodeStr);
            Assert.assertEquals("checksum", header.checksumStr);
            Assert.assertEquals("-", header.locationStr);
            Assert.assertEquals("1234", header.offsetStr);
            Assert.assertEquals("filename", header.filenameStr);
            Assert.assertEquals("40", header.archiveLengthStr);

            Assert.assertEquals(URI.create("http://www.antiaction.com/"), header.urlUri);
            Assert.assertEquals(InetAddress.getByName("192.168.1.2"), header.inetAddress);
            Assert.assertEquals(ArcDateParser.getDate("20120712144000"), header.archiveDate);
            Assert.assertEquals(ContentType.parseContentType("text/htlm"), header.contentType);
            Assert.assertEquals(new Integer(200), header.resultCode);
            Assert.assertEquals(new Long(1234), header.offset);
            Assert.assertEquals(new Long(40), header.archiveLength);

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());
            /*
             * Empty file.
             */
            bytes = "".getBytes();

            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in);
            reader.fieldParsers.diagnostics = diagnostics;
            header = ArcHeader.initHeader(reader, 42L, diagnostics);

            pbin = ((ArcReaderUncompressed)reader).in;
            success = header.parseHeader(pbin);

            Assert.assertFalse(success);
            Assert.assertEquals(0, header.recordFieldVersion);

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());
            /*
             * Newline.
             */
            bytes = "\n".getBytes();

            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in);
            reader.fieldParsers.diagnostics = diagnostics;
            header = ArcHeader.initHeader(reader, 42L, diagnostics);

            pbin = ((ArcReaderUncompressed)reader).in;
            success = header.parseHeader(pbin);

            Assert.assertFalse(success);
            Assert.assertEquals(0, header.recordFieldVersion);

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
