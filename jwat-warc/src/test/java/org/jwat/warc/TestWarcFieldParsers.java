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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;
import org.jwat.common.UriProfile;

@RunWith(JUnit4.class)
public class TestWarcFieldParsers {

    //invalid-warcfile-fields-missing.warc
    public static final String emptyFieldsFile = "invalid-warcfile-fields-empty.warc";

    @Test
    public void test_parsers_empty() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(emptyFieldsFile);

            WarcReader reader = WarcReaderFactory.getReader(in);
            WarcRecord record;

            while ((record = reader.getNextRecord()) != null) {
                record.close();

                if (bDebugOutput) {
                    TestBaseUtils.printRecord(record);
                    TestBaseUtils.printRecordErrors(record);
                }

                errors = 0;
                warnings = 0;
                if (record.diagnostics.hasErrors()) {
                    errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    warnings += record.diagnostics.getWarnings().size();
                }

                ++records;
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }

        //Assert.assertEquals(expected_records, records);
        //Assert.assertEquals(expected_errors, errors);
        //Assert.assertEquals(expected_warnings, warnings);
    }

    public static final String missingFieldsFile = "invalid-warcfile-fields-missing.warc";

    @Test
    public void test_parsers_missing() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(missingFieldsFile);

            WarcReader reader = WarcReaderFactory.getReader(in);
            WarcRecord record;

            while ((record = reader.getNextRecord()) != null) {
                record.close();

                if (bDebugOutput) {
                    TestBaseUtils.printRecord(record);
                    TestBaseUtils.printRecordErrors(record);
                }

                errors = 0;
                warnings = 0;
                if (record.diagnostics.hasErrors()) {
                    errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    warnings += record.diagnostics.getWarnings().size();
                }

                ++records;
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }
    }

    public static final String invalidFormatFieldsFile = "invalid-warcfile-fields-invalidformat.warc";

    @Test
    public void test_parsers_invalid_format() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        UriProfile uriProfile = UriProfile.RFC3986;

        Object[][] expectedDiagnoses;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(invalidFormatFieldsFile);

            WarcReader reader = WarcReaderFactory.getReader(in);
            WarcRecord record;

            while ((record = reader.getNextRecord()) != null) {
                record.close();

                // Test diagnostics.
                reader.fieldParsers.diagnostics.reset();
                Assert.assertNull(reader.fieldParsers.parseInteger(null, "1"));
                Assert.assertNull(reader.fieldParsers.parseLong(null, "2"));
                Assert.assertNull(reader.fieldParsers.parseString(null, "3"));
                Assert.assertNull(reader.fieldParsers.parseContentType(null, "4"));
                Assert.assertNull(reader.fieldParsers.parseIpAddress(null, "5"));
                Assert.assertNull(reader.fieldParsers.parseUri(null, false, uriProfile, "6"));
                Assert.assertNull(reader.fieldParsers.parseUri(null, true, uriProfile, "7"));
                Assert.assertNull(reader.fieldParsers.parseDate(null, "8"));
                Assert.assertNull(reader.fieldParsers.parseDigest(null, "9"));
                Assert.assertFalse(reader.fieldParsers.diagnostics.hasErrors());
                Assert.assertTrue(reader.fieldParsers.diagnostics.hasWarnings());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getErrors());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getWarnings());
                expectedDiagnoses = new Object[][] {
                        {DiagnosisType.EMPTY, "'1' field", 0},
                        {DiagnosisType.EMPTY, "'2' field", 0},
                        {DiagnosisType.EMPTY, "'3' field", 0},
                        {DiagnosisType.EMPTY, "'4' field", 0},
                        {DiagnosisType.EMPTY, "'5' field", 0},
                        {DiagnosisType.EMPTY, "'6' field", 0},
                        {DiagnosisType.EMPTY, "'7' field", 0},
                        {DiagnosisType.EMPTY, "'8' field", 0},
                        {DiagnosisType.EMPTY, "'9' field", 0}
                };
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, reader.fieldParsers.diagnostics.getWarnings());

                reader.fieldParsers.diagnostics.reset();
                Assert.assertNull(reader.fieldParsers.parseInteger("", "1"));
                Assert.assertNull(reader.fieldParsers.parseLong("", "2"));
                Assert.assertEquals(reader.fieldParsers.parseString("", "3"), "");
                Assert.assertNull(reader.fieldParsers.parseContentType("", "4"));
                Assert.assertNull(reader.fieldParsers.parseIpAddress("", "5"));
                Assert.assertNull(reader.fieldParsers.parseUri("", false, uriProfile, "6"));
                Assert.assertNull(reader.fieldParsers.parseUri("", true, uriProfile, "7"));
                Assert.assertNull(reader.fieldParsers.parseDate("", "8"));
                Assert.assertNull(reader.fieldParsers.parseDigest("", "9"));
                Assert.assertFalse(reader.fieldParsers.diagnostics.hasErrors());
                Assert.assertTrue(reader.fieldParsers.diagnostics.hasWarnings());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getErrors());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getWarnings());
                expectedDiagnoses = new Object[][] {
                        {DiagnosisType.EMPTY, "'1' field", 0},
                        {DiagnosisType.EMPTY, "'2' field", 0},
                        {DiagnosisType.EMPTY, "'3' field", 0},
                        {DiagnosisType.EMPTY, "'4' field", 0},
                        {DiagnosisType.EMPTY, "'5' field", 0},
                        {DiagnosisType.EMPTY, "'6' field", 0},
                        {DiagnosisType.EMPTY, "'7' field", 0},
                        {DiagnosisType.EMPTY, "'8' field", 0},
                        {DiagnosisType.EMPTY, "'9' field", 0}
                };
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, reader.fieldParsers.diagnostics.getWarnings());

                reader.fieldParsers.diagnostics.reset();
                Assert.assertNull(reader.fieldParsers.parseInteger("one", "1"));
                Assert.assertNull(reader.fieldParsers.parseLong("very lengthy", "2"));
                Assert.assertNull(reader.fieldParsers.parseContentType("gif\\image", "3"));
                Assert.assertNull(reader.fieldParsers.parseIpAddress("a.b.c.d", "4"));
                Assert.assertNull(reader.fieldParsers.parseDate("blue monday", "5"));
                Assert.assertNull(reader.fieldParsers.parseDigest("sharif-1; omar", "6"));
                Assert.assertTrue(reader.fieldParsers.diagnostics.hasErrors());
                Assert.assertFalse(reader.fieldParsers.diagnostics.hasWarnings());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getErrors());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getWarnings());
                expectedDiagnoses = new Object[][] {
                        {DiagnosisType.INVALID_EXPECTED, "'1' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'2' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'3' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'4' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'5' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'6' value", 2}
                };
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, reader.fieldParsers.diagnostics.getErrors());

                reader.fieldParsers.diagnostics.reset();
                Assert.assertNull(reader.fieldParsers.parseUri("bad_uri", false, uriProfile, "1"));
                Assert.assertNull(reader.fieldParsers.parseUri("bad_uri", true, uriProfile, "2"));
                Assert.assertNull(reader.fieldParsers.parseUri("<zaphod>", false, uriProfile, "3"));
                Assert.assertNull(reader.fieldParsers.parseUri("<zaphod>", true, uriProfile, "4"));
                Assert.assertNull(reader.fieldParsers.parseUri("1331:muhid:42", false, uriProfile, "5"));
                Assert.assertNull(reader.fieldParsers.parseUri("1331:muhid:42", true, uriProfile, "6"));
                Assert.assertNull(reader.fieldParsers.parseUri("bad_uri", false, UriProfile.RFC3986_ABS_16BIT_LAX, "7"));
                Assert.assertNull(reader.fieldParsers.parseUri("bad_uri", true, UriProfile.RFC3986_ABS_16BIT_LAX, "8"));
                Assert.assertNull(reader.fieldParsers.parseUri("<zaphod>", false, UriProfile.RFC3986_ABS_16BIT_LAX, "9"));
                Assert.assertNull(reader.fieldParsers.parseUri("<zaphod>", true, UriProfile.RFC3986_ABS_16BIT_LAX, "10"));
                Assert.assertNull(reader.fieldParsers.parseUri("1331:muhid:42", false, UriProfile.RFC3986_ABS_16BIT_LAX, "11"));
                Assert.assertNull(reader.fieldParsers.parseUri("1331:muhid:42", true, UriProfile.RFC3986_ABS_16BIT_LAX, "12"));
                Assert.assertTrue(reader.fieldParsers.diagnostics.hasErrors());
                Assert.assertFalse(reader.fieldParsers.diagnostics.hasWarnings());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getErrors());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getWarnings());
                expectedDiagnoses = new Object[][] {
                        {DiagnosisType.INVALID_EXPECTED, "'1' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'2' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'2' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'3' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'3' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'4' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'5' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'6' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'6' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'7' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'8' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'8' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'9' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'9' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'10' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'11' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'12' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'12' value", 2}
                };
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, reader.fieldParsers.diagnostics.getErrors());

                reader.fieldParsers.diagnostics.reset();
                Assert.assertNotNull(reader.fieldParsers.parseInteger("42", null));
                Assert.assertNotNull(reader.fieldParsers.parseLong("12345678901234", null));
                Assert.assertNotNull(reader.fieldParsers.parseString("JWAT", null));
                Assert.assertNotNull(reader.fieldParsers.parseContentType("text/plain", null));
                Assert.assertNotNull(reader.fieldParsers.parseIpAddress("127.0.0.1", null));
                Assert.assertNotNull(reader.fieldParsers.parseDate("2012-12-24T20:12:34Z", null));
                Assert.assertNotNull(reader.fieldParsers.parseDigest("sha1:1234567890abcdef", null));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org", false, uriProfile, null));
                Assert.assertNotNull(reader.fieldParsers.parseUri("<http://jwat.org>", true, uriProfile, null));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org", false, UriProfile.RFC3986_ABS_16BIT_LAX, null));
                Assert.assertNotNull(reader.fieldParsers.parseUri("<http://jwat.org>", true, UriProfile.RFC3986_ABS_16BIT_LAX, null));
                Assert.assertFalse(reader.fieldParsers.diagnostics.hasErrors());
                Assert.assertFalse(reader.fieldParsers.diagnostics.hasWarnings());

                reader.fieldParsers.diagnostics.reset();
                Assert.assertNotNull(reader.fieldParsers.parseUri("<http://jwat.org", true, uriProfile, "1"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org>", true, uriProfile, "2"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org", true, uriProfile, "3"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("<http://jwat.org", false, uriProfile, "4"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org>", false, uriProfile, "5"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("<http://jwat.org>", false, uriProfile, "6"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("<http://jwat.org", true, UriProfile.RFC3986_ABS_16BIT_LAX, "7"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org>", true, UriProfile.RFC3986_ABS_16BIT_LAX, "8"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org", true, UriProfile.RFC3986_ABS_16BIT_LAX, "9"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("<http://jwat.org", false, UriProfile.RFC3986_ABS_16BIT_LAX, "10"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org>", false, UriProfile.RFC3986_ABS_16BIT_LAX, "11"));
                Assert.assertNotNull(reader.fieldParsers.parseUri("<http://jwat.org>", false, UriProfile.RFC3986_ABS_16BIT_LAX, "12"));
                Assert.assertTrue(reader.fieldParsers.diagnostics.hasErrors());
                Assert.assertFalse(reader.fieldParsers.diagnostics.hasWarnings());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getErrors());
                //TestBaseUtils.printDiagnoses(reader.fieldParsers.diagnostics.getWarnings());
                expectedDiagnoses = new Object[][] {
                        {DiagnosisType.INVALID_EXPECTED, "'1' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'2' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'3' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'4' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'5' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'6' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'7' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'8' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'9' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'10' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'11' value", 2},
                        {DiagnosisType.INVALID_EXPECTED, "'12' value", 2}
                };
                TestBaseUtils.compareDiagnoses(expectedDiagnoses, reader.fieldParsers.diagnostics.getErrors());

                if (bDebugOutput) {
                    TestBaseUtils.printRecord(record);
                    TestBaseUtils.printRecordErrors(record);
                }

                errors = 0;
                warnings = 0;
                if (record.diagnostics.hasErrors()) {
                    errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    warnings += record.diagnostics.getWarnings().size();
                }

                ++records;
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }

        //Assert.assertEquals(expected_records, records);
        //Assert.assertEquals(expected_errors, errors);
        //Assert.assertEquals(expected_warnings, warnings);
    }

}
