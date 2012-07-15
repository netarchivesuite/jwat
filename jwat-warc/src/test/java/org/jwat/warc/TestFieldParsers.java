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

@RunWith(JUnit4.class)
public class TestFieldParsers {

    public static final String emptyFieldsFile = "test-fields-empty.txt";

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

    public static final String invalidFormatFieldsFile = "test-fields-invalidformat.warc";

    @Test
    public void test_parsers_invalid_format() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(invalidFormatFieldsFile);

            WarcReader reader = WarcReaderFactory.getReader(in);
            WarcRecord record;

            while ((record = reader.getNextRecord()) != null) {
                record.close();

                // Test diagnostics.
                Assert.assertNull(reader.fieldParsers.parseInteger(null, null));
                Assert.assertNull(reader.fieldParsers.parseLong(null, null));
                Assert.assertNull(reader.fieldParsers.parseString(null, null));
                Assert.assertNull(reader.fieldParsers.parseContentType(null, null));
                Assert.assertNull(reader.fieldParsers.parseIpAddress(null, null));
                Assert.assertNull(reader.fieldParsers.parseUri(null, null));
                Assert.assertNull(reader.fieldParsers.parseDate(null, null));
                Assert.assertNull(reader.fieldParsers.parseDigest(null, null));

                Assert.assertNull(reader.fieldParsers.parseInteger("", null));
                Assert.assertNull(reader.fieldParsers.parseLong("", null));
                Assert.assertEquals(reader.fieldParsers.parseString("", null), "");
                Assert.assertNull(reader.fieldParsers.parseContentType("", null));
                Assert.assertNull(reader.fieldParsers.parseIpAddress("", null));
                Assert.assertNull(reader.fieldParsers.parseUri("", null));
                Assert.assertNull(reader.fieldParsers.parseDate("", null));
                Assert.assertNull(reader.fieldParsers.parseDigest("", null));

                Assert.assertNull(reader.fieldParsers.parseInteger("one", null));
                Assert.assertNull(reader.fieldParsers.parseLong("very lengthy", null));
                Assert.assertNull(reader.fieldParsers.parseContentType("gif\\image", null));
                Assert.assertNull(reader.fieldParsers.parseIpAddress("a.b.c.d", null));
                //Assert.assertNull(reader.fieldParsers.parseUri("bad_uri", null));
                //Assert.assertNull(reader.fieldParsers.parseUri("<zaphod>", null));
                Assert.assertNull(reader.fieldParsers.parseUri("http://", null));
                Assert.assertNull(reader.fieldParsers.parseDate("blue monday", null));
                Assert.assertNull(reader.fieldParsers.parseDigest("sharif-1; omar", null));

                Assert.assertNotNull(reader.fieldParsers.parseInteger("42", null));
                Assert.assertNotNull(reader.fieldParsers.parseLong("12345678901234", null));
                Assert.assertNotNull(reader.fieldParsers.parseString("JWAT", null));
                Assert.assertNotNull(reader.fieldParsers.parseContentType("text/plain", null));
                Assert.assertNotNull(reader.fieldParsers.parseIpAddress("127.0.0.1", null));
                Assert.assertNotNull(reader.fieldParsers.parseUri("http://jwat.org", null));
                Assert.assertNotNull(reader.fieldParsers.parseDate("2012-12-24T20:12:34Z", null));
                Assert.assertNotNull(reader.fieldParsers.parseDigest("sha1:1234567890abcdef", null));

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
