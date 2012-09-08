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
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestArcVersionBlock {

    @Test
    public void test_arcversionblock() {
        String vbData;
        String mdData;
        String arcData;
        byte[] bytes;
        ByteArrayInputStream in;
        ArcReader reader;
        ArcRecordBase record;
        String tmpStr;
        Object[][] expectedDiagnoses;

        try {
            /*
             * 1.0 no metadata.
             */
            vbData = "1 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";

            Assert.assertEquals(76, vbData.length());

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertTrue(record.isCompliant());
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertTrue(record.hasPayload());
            Assert.assertTrue(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertTrue(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 2.0 no metadata.
             */
            vbData = "2 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Result-code Checksum Location Offset Filename Archive-length\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain 200 checksum location 1234 filename " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(2, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_2, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertTrue(record.isCompliant());
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertTrue(record.hasPayload());
            Assert.assertTrue(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertTrue(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 1.1 metadata.
             */
            vbData = "1 1 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
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
            arcData = "filedesc://IAH-20080430204825-00000-blackbook.arc 0.0.0.0 20080430204825 text/plain " + (vbData.length() + mdData.length()) +  "\n";
            arcData += vbData;
            arcData += mdData;
            arcData += "\n";

            Assert.assertEquals(1300, vbData.length() + mdData.length());

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_1_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertTrue(record.isCompliant());
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertTrue(record.hasPayload());
            Assert.assertFalse(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertEquals(record.getPayloadContent(), record.payload.getInputStream());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertTrue(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 1.0 archive-length = 0
             */
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain 0\n";
            arcData += "\n";

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertNull(record.version);
            Assert.assertFalse(record.isCompliant());
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID, ArcConstants.ARC_FILE, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

            Assert.assertFalse(record.hasPayload());
            Assert.assertNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 2.0 archive-length null
             */
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain 200 checksum location 1234 filename -\n";
            arcData += "\n";

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(2, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertNull(record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());

            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0},
                    {DiagnosisType.INVALID, ArcConstants.ARC_FILE, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

            Assert.assertFalse(record.hasPayload());
            Assert.assertNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 1.0 v2 desc block
             */
            vbData = "1 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Result-code Checksum Location Offset Filename Archive-length\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.INVALID_EXPECTED, "ARC record does not match the version block definition", 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

            Assert.assertTrue(record.hasPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertEquals(record.getPayloadContent(), record.payload.getInputStream());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 1.1 no metadata.
             */
            vbData = "1 1 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";

            Assert.assertEquals(76, vbData.length());

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_1_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

            Assert.assertTrue(record.hasPayload());
            Assert.assertTrue(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 1.0 metadata
             */
            vbData = "1 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
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
            arcData = "filedesc://IAH-20080430204825-00000-blackbook.arc 0.0.0.0 20080430204825 text/plain " + (vbData.length() + mdData.length()) +  "\n";
            arcData += vbData;
            arcData += mdData;
            arcData += "\n";

            Assert.assertEquals(1300, vbData.length() + mdData.length());

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.UNDESIRED_DATA, "version block metadata payload", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

            Assert.assertTrue(record.hasPayload());
            Assert.assertFalse(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertEquals(record.getPayloadContent(), record.payload.getInputStream());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 2.0 metadata
             */
            vbData = "2 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Result-code Checksum Location Offset Filename Archive-length\n";
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
            arcData = "filedesc://IAH-20080430204825-00000-blackbook.arc 0.0.0.0 20080430204825 text/plain 200 checksum location 1234 filename " + (vbData.length() + mdData.length()) +  "\n";
            arcData += vbData;
            arcData += mdData;
            arcData += "\n";

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(2, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_2, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.UNDESIRED_DATA, "version block metadata payload", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

            Assert.assertTrue(record.hasPayload());
            Assert.assertFalse(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertEquals(record.getPayloadContent(), record.payload.getInputStream());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 1.0 no metadata no content-type.
             */
            vbData = "1 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 - " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";

            Assert.assertEquals(76, vbData.length());

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                    {DiagnosisType.ERROR_EXPECTED, ArcConstants.FN_CONTENT_TYPE, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());

            Assert.assertTrue(record.hasPayload());
            Assert.assertTrue(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 1.0 no metadata no content-type=application/plain.
             */
            vbData = "1 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 application/plain " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";

            Assert.assertEquals(76, vbData.length());

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertTrue(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, ArcConstants.FN_CONTENT_TYPE, 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());

            Assert.assertTrue(record.hasPayload());
            Assert.assertTrue(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * 1.0 no metadata no content-type=text/binary.
             */
            vbData = "1 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/binary " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";

            Assert.assertEquals(76, vbData.length());

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(ArcVersion.VERSION_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertTrue(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, ArcConstants.FN_CONTENT_TYPE, 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());

            Assert.assertTrue(record.hasPayload());
            Assert.assertTrue(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_arcversionblock_strict() {
        String vbData;
        //String mdData;
        String arcData;
        byte[] bytes;
        ByteArrayInputStream in;
        ArcReader reader;
        ArcRecordBase record;
        String tmpStr;
        Object[][] expectedDiagnoses;

        try {
            /*
             * Not strict.
             */
            vbData = "1 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
            vbData += "\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/binary " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";
            arcData += "\n";

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);
            reader.setStrict(false);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(2, record.trailingNewLines);
            Assert.assertEquals(ArcVersion.VERSION_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertTrue(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, ArcConstants.FN_CONTENT_TYPE, 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());

            Assert.assertTrue(record.hasPayload());
            Assert.assertTrue(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            /*
             * Strict.
             */
            vbData = "1 0 InternetArchive\n";
            vbData += "URL IP-address Archive-date Content-type Archive-length\n";
            vbData += "\n";
            arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/binary " + vbData.length() + "\n";
            arcData += vbData;
            arcData += "\n";
            arcData += "\n";

            bytes = arcData.getBytes();
            in = new ByteArrayInputStream(bytes);

            reader = ArcReaderFactory.getReader(in, 1024);
            reader.setStrict(true);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);
            tmpStr = record.toString();
            Assert.assertNotNull(tmpStr);
            record.close();
            Assert.assertEquals(1, record.header.recordFieldVersion);
            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertEquals(2, record.trailingNewLines);
            Assert.assertEquals(ArcVersion.VERSION_1, record.version);
            Assert.assertEquals(record.version, record.getVersion());
            Assert.assertFalse(record.isCompliant());
            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertTrue(record.diagnostics.hasWarnings());

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.UNDESIRED_DATA, "version block metadata payload", 1},
                    {DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, ArcConstants.FN_CONTENT_TYPE, 2}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getWarnings());

            Assert.assertTrue(record.hasPayload());
            Assert.assertFalse(record.hasPseudoEmptyPayload());
            Assert.assertNotNull(record.payload);
            Assert.assertEquals(record.payload, record.getPayload());
            Assert.assertNotNull(record.getPayloadContent());
            Assert.assertNull(record.httpHeader);
            Assert.assertEquals(record.httpHeader, record.getHttpHeader());

            record = reader.getNextRecord();
            Assert.assertNull(record);

            reader.close();
            Assert.assertFalse(reader.isCompliant());
            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
