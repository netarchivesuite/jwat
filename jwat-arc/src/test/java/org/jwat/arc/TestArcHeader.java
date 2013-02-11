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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.Uri;

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
        Object[][] test_cases;
        String[] fields;
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
             * Test cases, parseHeaders().
             */
            test_cases = new Object[][] {
                    /*
                     * Invalid number of fields.
                     */
                    {0, "".getBytes(), new String[4], new Object[][] {}, new Object[][] {}
                    },
                    /*
                     * Null fields v1.
                     */
                    {1, "".getBytes(), new String[5], new Object[][] {
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0}
                    }, new Object[][] {}},
                    /*
                     * Null fields v2.
                     */
                    {2, "".getBytes(), new String[10], new Object[][] {
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_RESULT_CODE + "' value", 0},
                        //{DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CHECKSUM + "' value", 0},
                        //{DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_LOCATION + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_OFFSET + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_FILENAME + "' value", 0},
                        {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0}
                    }, new Object[][] {}}
            };
            diagnostics.reset();
            for (int i=0; i<test_cases.length; ++i) {
                bytes = (byte[])test_cases[i][1];
                in = new ByteArrayInputStream(bytes);
                reader = ArcReaderFactory.getReader(in);
                reader.fieldParsers.diagnostics = diagnostics;
                header = ArcHeader.initHeader(reader, 42L, diagnostics);
                fields = (String[])test_cases[i][2];
                header.parseHeaders(fields);
                Assert.assertEquals(test_cases[i][0], header.recordFieldVersion);
                String[] expected_fieldStrings = new String[] {null, null, null, null, null, null, null, null, null, null};
                Object[] expected_fieldObjects = new Object[] {null, null, null, null, null, null, null};
                Object[][] expected_errors = (Object[][])test_cases[i][3];
                Object[][] expected_warnings = (Object[][])test_cases[i][4];
                TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);
                diagnostics.reset();

                // Save testfile.
                SaveArcTestFiles.saveTestArcHeader(bytes, expected_errors.length == 0 && expected_warnings.length == 0);
            }
            /*
             * Test cases, parseHeader().
             */
            test_cases = new Object[][] {
                    /*
                     * Empty file.
                     */
                    {false, 0, "".getBytes(), new String[] {
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
                    }, new Object[] {
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * Newline.
                     */
                    {false, 0, "\n".getBytes(), new String[] {
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
                    }, new Object[] {
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    }, new Object[][] {
                            {DiagnosisType.INVALID, "Empty lines before ARC record", 0}
                    }, new Object[][] {}},
                    /*
                     * Garbage.
                     */
                    {false, 0, "garbage\n".getBytes(), new String[] {
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
                    }, new Object[] {
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    }, new Object[][] {
                            {DiagnosisType.INVALID, "Data before ARC record", 0}
                    }, new Object[][] {}},
                    /*
                     * v2 all "-"
                     */
                    {true, 2, "- - - - - - - - - -\n".getBytes(), new String[] {
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
                    }, new Object[] {
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    }, new Object[][] {
                            {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_URL + "' value", 0},
                            {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 0},
                            {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 0},
                            {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 0},
                            {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_RESULT_CODE + "' value", 0},
                            //{DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_CHECKSUM + "' value", 0},
                            //{DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_LOCATION + "' value", 0},
                            {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_OFFSET + "' value", 0},
                            {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_FILENAME + "' value", 0},
                            {DiagnosisType.REQUIRED_MISSING, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 0}
                    }, new Object[][] {}},
                    /*
                     * Valid record V1.
                     */
                    {true, 1, bytes = "http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html 649\n".getBytes(), new String[] {
                        "http://cctr.umkc.edu:80/user/jbenz/tst.htm",
                        "134.193.4.1",
                        "19970417175710",
                        "text/html",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "649"
                    }, new Object[] {
                        Uri.create("http://cctr.umkc.edu:80/user/jbenz/tst.htm"),
                        InetAddress.getByName("134.193.4.1"),
                        ArcDateParser.getDate("19970417175710"),
                        ContentType.parseContentType("text/html"),
                        null,
                        null,
                        new Long(649)
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * Valid record v2.
                     */
                    {true, 2, "http://www.antiaction.com/ 192.168.1.2 20120712144000 text/htlm 200 checksum location 229 filename 649\n".getBytes(), new String[] {
                        "http://www.antiaction.com/",
                        "192.168.1.2",
                        "20120712144000",
                        "text/htlm",
                        "200",
                        "checksum",
                        "location",
                        "229",
                        "filename",
                        "649"
                    }, new Object[] {
                        Uri.create("http://www.antiaction.com/"),
                        InetAddress.getByName("192.168.1.2"),
                        ArcDateParser.getDate("20120712144000"),
                        ContentType.parseContentType("text/htlm"),
                        new Integer(200),
                        new Long(229),
                        new Long(649),
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * Invalid record V1.
                     */
                    {true, 1, "4270 http://cctr.umkc.edu:80/user/jbenz/tst.htm 134.193.4.1 19970417175710 text/html\n".getBytes(), new String[] {
                        "4270",
                        "http://cctr.umkc.edu:80/user/jbenz/tst.htm",
                        "134.193.4.1",
                        "19970417175710",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "text/html"
                    }, new Object[] {
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    }, new Object[][] {
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2}
                    }, new Object[][] {}},
                    /*
                     * Invalid record v2.
                     */
                    {true, 2, "40 http://www.antiaction.com/ 192.168.1.2 20120712144000 text/htlm 200 checksum location 1234 filename\n".getBytes(), new String[] {
                        "40",
                        "http://www.antiaction.com/",
                        "192.168.1.2",
                        "20120712144000",
                        "text/htlm",
                        "200",
                        "checksum",
                        "location",
                        "1234",
                        "filename"
                    }, new Object[] {
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    }, new Object[][] {
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_RESULT_CODE + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_OFFSET + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2}
                    }, new Object[][] {}},
                    /*
                     * Valid record v2 with "-".
                     */
                    {true, 2, "http://www.antiaction.com/ 192.168.1.2 20120712144000 text/htlm 200 checksum - 229 filename 649\n".getBytes(), new String[] {
                        "http://www.antiaction.com/",
                        "192.168.1.2",
                        "20120712144000",
                        "text/htlm",
                        "200",
                        "checksum",
                        null,
                        "229",
                        "filename",
                        "649"
                    }, new Object[] {
                        Uri.create("http://www.antiaction.com/"),
                        InetAddress.getByName("192.168.1.2"),
                        ArcDateParser.getDate("20120712144000"),
                        ContentType.parseContentType("text/htlm"),
                        new Integer(200),
                        new Long(229),
                        new Long(649)
                    }, new Object[][] {}, new Object[][] {}},
                    /*
                     * Semi-valid record v2 content-type=no-type, result-code out of bounds, minus offset, length.
                     */
                    {true, 2, "http://www.antiaction.com/ 192.168.1.2 20120712144000 no-type 99 checksum location -4321 filename -42\n".getBytes(), new String[] {
                        "http://www.antiaction.com/",
                        "192.168.1.2",
                        "20120712144000",
                        "no-type",
                        "99",
                        "checksum",
                        "location",
                        "-4321",
                        "filename",
                        "-42"
                    }, new Object[] {
                        Uri.create("http://www.antiaction.com/"),
                        InetAddress.getByName("192.168.1.2"),
                        ArcDateParser.getDate("20120712144000"),
                        null,
                        new Integer(99),
                        new Long(-4321),
                        new Long(-42)
                    }, new Object[][] {
                        {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_RESULT_CODE + "' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_OFFSET + "' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2}
                    }, new Object[][] {}},
                    /*
                     * Semi-valid record v2 content-type=no-type, result-code out of bounds.
                     */
                    {true, 2, "http://www.antiaction.com/ 192.168.1.2 20120712144000 no-type 1000 checksum location 4321 filename 42\n".getBytes(), new String[] {
                        "http://www.antiaction.com/",
                        "192.168.1.2",
                        "20120712144000",
                        "no-type",
                        "1000",
                        "checksum",
                        "location",
                        "4321",
                        "filename",
                        "42"
                    }, new Object[] {
                        Uri.create("http://www.antiaction.com/"),
                        InetAddress.getByName("192.168.1.2"),
                        ArcDateParser.getDate("20120712144000"),
                        null,
                        new Integer(1000),
                        new Long(4321),
                        new Long(42)
                    }, new Object[][] {
                        {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_RESULT_CODE + "' value", 2}
                    }, new Object[][] {}}
            };
            diagnostics.reset();
            for (int i=0; i<test_cases.length; ++i) {
                bytes = (byte[])test_cases[i][2];
                in = new ByteArrayInputStream(bytes);
                reader = ArcReaderFactory.getReader(in);
                reader.fieldParsers.diagnostics = diagnostics;
                header = ArcHeader.initHeader(reader, 42L, diagnostics);
                pbin = ((ArcReaderUncompressed)reader).in;
                success = header.parseHeader(pbin);
                tmpStr = header.toString();
                Assert.assertNotNull(tmpStr);
                Assert.assertEquals(test_cases[i][0], success);
                Assert.assertEquals(test_cases[i][1], header.recordFieldVersion);
                String[] expected_fieldStrings = (String[])test_cases[i][3];
                Object[] expected_fieldObjects = (Object[])test_cases[i][4];
                Object[][] expected_errors = (Object[][])test_cases[i][5];
                Object[][] expected_warnings = (Object[][])test_cases[i][6];
                TestBaseUtils.assert_header(header, expected_fieldStrings, expected_fieldObjects, expected_errors, expected_warnings);
                diagnostics.reset();

                // Save testfile.
                SaveArcTestFiles.saveTestArcHeader(bytes, expected_errors.length == 0 && expected_warnings.length == 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
