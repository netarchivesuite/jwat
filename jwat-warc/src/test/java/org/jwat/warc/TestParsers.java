package org.jwat.warc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestParsers {

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
                    RecordDebugBase.printRecord(record);
                    RecordDebugBase.printRecordErrors(record);
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
                RecordDebugBase.printStatus(records, errors, warnings);
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

                Assert.assertNull(record.parseInteger(null, null));
                Assert.assertNull(record.parseLong(null, null));
                Assert.assertNull(record.parseString(null, null));
                Assert.assertNull(record.parseDate(null, null));
                Assert.assertNull(record.parseIpAddress(null, null));
                Assert.assertNull(record.parseUri(null, null));
                Assert.assertNull(record.parseContentType(null, null));
                Assert.assertNull(record.parseDigest(null, null));

                Assert.assertNull(record.parseInteger("", null));
                Assert.assertNull(record.parseLong("", null));
                Assert.assertEquals(record.parseString("", null), "");
                Assert.assertNull(record.parseDate("", null));
                Assert.assertNull(record.parseIpAddress("", null));
                Assert.assertNull(record.parseUri("", null));
                Assert.assertNull(record.parseContentType("", null));
                Assert.assertNull(record.parseDigest("", null));

                Assert.assertNull(record.parseInteger("one", null));
                Assert.assertNull(record.parseLong("very lengthy", null));
                Assert.assertNull(record.parseDate("blue monday", null));
                Assert.assertNull(record.parseIpAddress("a.b.c.d", null));
                //Assert.assertNull(record.parseUri("bad_uri", null));
                //Assert.assertNull(record.parseUri("<zaphod>", null));
                Assert.assertNull(record.parseContentType("gif\\image", null));
                Assert.assertNull(record.parseDigest("sharif-1; omar", null));

                if (bDebugOutput) {
                    RecordDebugBase.printRecord(record);
                    RecordDebugBase.printRecordErrors(record);
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
                RecordDebugBase.printStatus(records, errors, warnings);
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
