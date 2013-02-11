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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestArcRecord {

    @Test
    public void test_arcrecord() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in;
        ArcReader reader;
        ArcRecordBase record;

        String arcData;
        String payloadData;
        try {
            Object[][] test_cases = new Object[][] {
                    /*
                     * ArchiveLength=0, HTTP payload.
                     */
                    {"http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html ",
                    "HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n", new TestHeaderCallback() {
                        public String getArchiveLength(Object[] test_case) {
                            return "0";
                        }
                    }, new Object[][] {
                            {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
                    }, new Object[][] {}},
                    /*
                     * ArchiveLength=0, HTTP payload, no-type.
                     */
                    {"http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 no-type ",
                        "", new TestHeaderCallback() {
                            public String getArchiveLength(Object[] test_case) {
                                return "0";
                            }
                    }, new Object[][] {
                            {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
                    }, new Object[][] {}},
                    /*
                     * ArchiveLength=0, TNT payload.
                     */
                    {"tnt://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/explosive ",
                        "", new TestHeaderCallback() {
                            public String getArchiveLength(Object[] test_case) {
                                return "0";
                                }
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * ArchiveLength=0, TNT payload, no-type.
                     */
                    {"tnt://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 no-type ",
                        "", new TestHeaderCallback() {
                            public String getArchiveLength(Object[] test_case) {
                                return "0";
                            }
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * ArchiveLength=invalid, HTTP payload.
                     */
                    {"http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html ",
                    "HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n", new TestHeaderCallback() {
                        public String getArchiveLength(Object[] test_case) {
                            return "invalid";
                        }
                    }, new Object[][] {
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2},
                            {DiagnosisType.ERROR_EXPECTED, ArcConstants.ARC_FILE, 1}
                    }, new Object[][] {}},
                    /*
                     * ArchiveLength=payload length, HTTP response.
                     */
                    {"http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html ",
                    "GET / HTTP/1.1\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n", new TestHeaderCallback() {
                        public String getArchiveLength(Object[] test_case) {
                            int archiveLength = ((String)(test_case[1])).length();
                            return Integer.toString(archiveLength);
                        }
                    }, new Object[][] {
                            {DiagnosisType.ERROR, "http header", 1}
                    }, new Object[][] {}},
                    /*
                     * ArchiveLength=payload length.
                     */
                    {"http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html ",
                    "HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n", new TestHeaderCallback() {
                        public String getArchiveLength(Object[] test_case) {
                            int archiveLength = ((String)(test_case[1])).length();
                            return Integer.toString(archiveLength);
                        }
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * ArchiveLength=payload length, no-type.
                     */
                    {"http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 no-type ",
                    "HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n", new TestHeaderCallback() {
                        public String getArchiveLength(Object[] test_case) {
                            int archiveLength = ((String)(test_case[1])).length();
                            return Integer.toString(archiveLength);
                        }
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * ArchiveLength=payload length.
                     */
                    {"gopher://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html ",
                    "HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n", new TestHeaderCallback() {
                        public String getArchiveLength(Object[] test_case) {
                            int archiveLength = ((String)(test_case[1])).length();
                            return Integer.toString(archiveLength);
                        }
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * ArchiveLength=payload length.
                     */
                    {"gopher://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 no-type ",
                    "HTTP/1.0 200 Sending document\r\n"
                    + "MIME-version: 1.0\r\n"
                    + "Server: OSU/1.8\r\n"
                    + "Content-type: text/html\r\n"
                    + "Content-transfer-encoding: 8bit\r\n"
                    + "Last-Modified: Tuesday, 21-Aug-96 05:14:05 GMT\r\n"
                    + "\r\n", new TestHeaderCallback() {
                        public String getArchiveLength(Object[] test_case) {
                            int archiveLength = ((String)(test_case[1])).length();
                            return Integer.toString(archiveLength);
                        }
                    }, new Object[][] {}, new Object[][] {}},
            };
            for (int i=0; i<test_cases.length; ++i) {
                out.reset();
                out.write(filedesc());
                TestHeaderCallback archiveLengthCB = (TestHeaderCallback)test_cases[i][2];
                Assert.assertNotNull(archiveLengthCB);
                payloadData = (String)test_cases[i][1];
                arcData = (String)test_cases[i][0];
                arcData += archiveLengthCB.getArchiveLength(test_cases[i]);
                arcData += "\n";
                arcData += payloadData;
                arcData += "\n";
                out.write(arcData.getBytes());
                out.close();

                // debug
                //System.out.println(out);

                // Save testfile.
                SaveArcTestFiles.saveTestArcRecord(out.toByteArray(),
                        ((Object[][])test_cases[i][3]).length == 0 && ((Object[][])test_cases[i][4]).length == 0);

                in = new ByteArrayInputStream(out.toByteArray());
                reader = ArcReaderFactory.getReader(in, 1024);
                record = reader.getNextRecord();
                Assert.assertNotNull(record);
                Assert.assertFalse(record.diagnostics.hasErrors());
                Assert.assertFalse(record.diagnostics.hasWarnings());
                record = reader.getNextRecord();
                Assert.assertNotNull(record);
                TestBaseUtils.compareDiagnoses((Object[][])test_cases[i][3], record.diagnostics.getErrors());
                TestBaseUtils.compareDiagnoses((Object[][])test_cases[i][4], record.diagnostics.getWarnings());
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public abstract class TestHeaderCallback {
        public abstract String getArchiveLength(Object[] test_case);
    }

    public byte[] filedesc() {
        String arcData;
        String vbData;
        String mdData;
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
        vbData = "1 1 InternetArchive\n";
        vbData += "URL IP-address Archive-date Content-type Archive-length\n";
        arcData = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain " + (vbData.length() + mdData.length()) + "\n";
        arcData += vbData;
        arcData += mdData;
        arcData += "\n";
        return arcData.getBytes();
    }


}
