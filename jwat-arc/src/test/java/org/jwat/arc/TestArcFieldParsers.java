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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.UriProfile;

@RunWith(JUnit4.class)
public class TestArcFieldParsers {

    public static final String emptyFieldsFile = "test-arc-1.0-dashed-fields.arc";

    @Test
    public void test_arcfieldparsers_empty() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(emptyFieldsFile);

            ArcReader reader = ArcReaderFactory.getReader(in);
            ArcRecordBase record;

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
            Assert.fail("Unexpected i/o exception");
        }

        //Assert.assertEquals(expected_records, records);
        //Assert.assertEquals(expected_errors, errors);
        //Assert.assertEquals(expected_warnings, warnings);
    }

    public static final String invalidFormatFieldsFile = "test-arc-1.0-invalid-fields.arc";

    @Test
    public void test_arcfieldparsers_invalid_format() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        Object[][] expectedDiagnoses;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(invalidFormatFieldsFile);

            ArcReader reader = ArcReaderFactory.getReader(in);
            ArcRecordBase record;

            Assert.assertFalse(reader.bStrict);
            Assert.assertFalse(reader.isStrict());
            reader.setStrict(false);
            Assert.assertFalse(reader.bStrict);
            Assert.assertFalse(reader.isStrict());
            reader.setStrict(true);
            Assert.assertTrue(reader.bStrict);
            Assert.assertTrue(reader.isStrict());

            while ((record = reader.getNextRecord()) != null) {
                record.close();

                ++records;

                switch (records) {
                case 1:
                    Assert.assertTrue(record.diagnostics.hasErrors());
                    Assert.assertFalse(record.diagnostics.hasWarnings());
                    expectedDiagnoses = new Object[][] {
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_URL + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_IP_ADDRESS + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_DATE + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 2},
                            {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", 2},
                            {DiagnosisType.ERROR_EXPECTED, "'" + ArcConstants.FN_CONTENT_TYPE + "' value", 1},
                            {DiagnosisType.INVALID, ArcConstants.ARC_FILE, 1},
                            {DiagnosisType.INVALID_EXPECTED, "Trailing newlines", 2}
                    };
                    TestBaseUtils.compareDiagnoses(expectedDiagnoses, record.diagnostics.getErrors());
                    break;
                }

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
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected i/o exception");
        }

        //Assert.assertEquals(expected_records, records);
        //Assert.assertEquals(expected_errors, errors);
        //Assert.assertEquals(expected_warnings, warnings);

    }

    @Test
    public void test_arcfieldparsers() {
        Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();
        ArcFieldParsers fieldParsers = new ArcFieldParsers();
        fieldParsers.diagnostics = diagnostics;

        UriProfile uriProfile = UriProfile.RFC3986;

        Object[][] expectedDiagnoses;
        /*
         * Nullable.
         */
        Assert.assertNull(fieldParsers.parseString(null, null, true));
        Assert.assertNull(fieldParsers.parseInteger(null, null, true));
        Assert.assertNull(fieldParsers.parseLong(null, null, true));
        Assert.assertNull(fieldParsers.parseContentType(null, null, true));
        Assert.assertNull(fieldParsers.parseIpAddress(null, null, true));
        Assert.assertNull(fieldParsers.parseUri(null, uriProfile, null, true));
        Assert.assertNull(fieldParsers.parseDate(null, null, true));

        Assert.assertFalse(fieldParsers.diagnostics.hasErrors());
        Assert.assertFalse(fieldParsers.diagnostics.hasWarnings());

        Assert.assertEquals(fieldParsers.parseString("", null, true), "");
        Assert.assertNull(fieldParsers.parseInteger("", null, true));
        Assert.assertNull(fieldParsers.parseLong("", null, true));
        Assert.assertNull(fieldParsers.parseContentType("", null, true));
        Assert.assertNull(fieldParsers.parseIpAddress("", null, true));
        Assert.assertNull(fieldParsers.parseUri("", uriProfile, null, true));
        Assert.assertNull(fieldParsers.parseDate("", null, true));

        Assert.assertFalse(fieldParsers.diagnostics.hasErrors());
        Assert.assertFalse(fieldParsers.diagnostics.hasWarnings());

        Assert.assertNull(fieldParsers.parseInteger("one", "Integer", true));
        Assert.assertNull(fieldParsers.parseLong("very lengthy", "Long", true));
        Assert.assertNull(fieldParsers.parseContentType("gif\\image", "Content-Type", true));
        Assert.assertNull(fieldParsers.parseIpAddress("a.b.c.d", "IP-Address", true));
        Assert.assertNull(fieldParsers.parseUri("bad_uri", uriProfile, "URI1", true));
        Assert.assertNull(fieldParsers.parseUri("<zaphod>", uriProfile, "URI2", true));
        Assert.assertNull(fieldParsers.parseDate("blue monday", "Date", true));

        Assert.assertTrue(fieldParsers.diagnostics.hasErrors());
        Assert.assertFalse(fieldParsers.diagnostics.hasWarnings());

        expectedDiagnoses = new Object[][] {
                {DiagnosisType.INVALID_EXPECTED, "'Integer' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'Long' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'Content-Type' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'IP-Address' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'URI1' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'URI2' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'Date' value", 2}
        };
        TestBaseUtils.compareDiagnoses(expectedDiagnoses, fieldParsers.diagnostics.getErrors());
        fieldParsers.diagnostics.reset();

        Assert.assertEquals("string", fieldParsers.parseString("string", null, true));
        Assert.assertEquals(new Integer(42), fieldParsers.parseInteger("42", null, true));
        Assert.assertEquals(new Long(421234567890L), fieldParsers.parseLong("421234567890", null, true));
        Assert.assertEquals("text/plain", fieldParsers.parseContentType("text/plain", null, true).toStringShort());
        Assert.assertEquals("4.3.2.1", fieldParsers.parseIpAddress("4.3.2.1", null, true).getHostAddress());
        Assert.assertEquals("http://test/uri", fieldParsers.parseUri("http://test/uri", uriProfile, null, true).toString());
        Assert.assertEquals(1141546971000L, fieldParsers.parseDate("20060305082251", null, true).getTime());

        Assert.assertFalse(fieldParsers.diagnostics.hasErrors());
        Assert.assertFalse(fieldParsers.diagnostics.hasWarnings());
        /*
         * Not nullable.
         */
        Assert.assertNull(fieldParsers.parseString(null, "String", false));
        Assert.assertNull(fieldParsers.parseInteger(null, "Integer", false));
        Assert.assertNull(fieldParsers.parseLong(null, "Long", false));
        Assert.assertNull(fieldParsers.parseContentType(null, "Content-Type", false));
        Assert.assertNull(fieldParsers.parseIpAddress(null, "IP-Address", false));
        Assert.assertNull(fieldParsers.parseUri(null, uriProfile, "URI", false));
        Assert.assertNull(fieldParsers.parseDate(null, "Date", false));

        Assert.assertTrue(fieldParsers.diagnostics.hasErrors());
        Assert.assertFalse(fieldParsers.diagnostics.hasWarnings());

        expectedDiagnoses = new Object[][] {
                {DiagnosisType.REQUIRED_MISSING, "'String' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'Integer' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'Long' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'Content-Type' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'IP-Address' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'URI' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'Date' value", 0}
        };
        TestBaseUtils.compareDiagnoses(expectedDiagnoses, fieldParsers.diagnostics.getErrors());
        fieldParsers.diagnostics.reset();

        Assert.assertEquals(fieldParsers.parseString("", "String", false), "");
        Assert.assertNull(fieldParsers.parseInteger("", "Integer", false));
        Assert.assertNull(fieldParsers.parseLong("", "Long", false));
        Assert.assertNull(fieldParsers.parseContentType("", "Content-Type", false));
        Assert.assertNull(fieldParsers.parseIpAddress("", "IP-Address", false));
        Assert.assertNull(fieldParsers.parseUri("", uriProfile, "URI", false));
        Assert.assertNull(fieldParsers.parseDate("", "Date", false));

        Assert.assertTrue(fieldParsers.diagnostics.hasErrors());
        Assert.assertFalse(fieldParsers.diagnostics.hasWarnings());

        expectedDiagnoses = new Object[][] {
                {DiagnosisType.REQUIRED_MISSING, "'String' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'Integer' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'Long' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'Content-Type' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'IP-Address' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'URI' value", 0},
                {DiagnosisType.REQUIRED_MISSING, "'Date' value", 0}
        };
        TestBaseUtils.compareDiagnoses(expectedDiagnoses, fieldParsers.diagnostics.getErrors());
        fieldParsers.diagnostics.reset();

        Assert.assertNull(fieldParsers.parseInteger("one", "Integer", false));
        Assert.assertNull(fieldParsers.parseLong("very lengthy", "Long", false));
        Assert.assertNull(fieldParsers.parseContentType("gif\\image", "Content-Type", false));
        Assert.assertNull(fieldParsers.parseIpAddress("a.b.c.d", "IP-Address", false));
        Assert.assertNull(fieldParsers.parseUri("bad_uri", uriProfile, "URI1", false));
        Assert.assertNull(fieldParsers.parseUri("<zaphod>", uriProfile, "URI2", false));
        Assert.assertNull(fieldParsers.parseDate("blue monday", "Date", false));

        Assert.assertTrue(fieldParsers.diagnostics.hasErrors());
        Assert.assertFalse(fieldParsers.diagnostics.hasWarnings());

        expectedDiagnoses = new Object[][] {
                {DiagnosisType.INVALID_EXPECTED, "'Integer' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'Long' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'Content-Type' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'IP-Address' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'URI1' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'URI2' value", 2},
                {DiagnosisType.INVALID_EXPECTED, "'Date' value", 2}
        };
        TestBaseUtils.compareDiagnoses(expectedDiagnoses, fieldParsers.diagnostics.getErrors());
        fieldParsers.diagnostics.reset();

        Assert.assertEquals("string", fieldParsers.parseString("string", null, false));
        Assert.assertEquals(new Integer(42), fieldParsers.parseInteger("42", null, false));
        Assert.assertEquals(new Long(421234567890L), fieldParsers.parseLong("421234567890", null, false));
        Assert.assertEquals("text/plain", fieldParsers.parseContentType("text/plain", null, false).toStringShort());
        Assert.assertEquals("4.3.2.1", fieldParsers.parseIpAddress("4.3.2.1", null, false).getHostAddress());
        Assert.assertEquals("http://test/uri", fieldParsers.parseUri("http://test/uri", uriProfile, null, false).toString());
        Assert.assertEquals(1141546971000L, fieldParsers.parseDate("20060305082251", null, false).getTime());

        Assert.assertFalse(fieldParsers.diagnostics.hasErrors());
        Assert.assertFalse(fieldParsers.diagnostics.hasWarnings());
    }

}
