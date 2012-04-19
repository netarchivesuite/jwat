package org.jwat.arc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestFieldParsers {

    public static final String emptyFieldsFile = "test-arc-1.0-dashed-fields.arc";

    @Test
    public void test_parsers_empty() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(emptyFieldsFile);

            ArcReader reader = ArcReaderFactory.getReader(in);
            ArcRecord record;

            ArcVersionBlock version = reader.getVersionBlock();

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

    public static final String invalidFormatFieldsFile = "test-arc-1.0-invalid-fields.arc";

    @Test
    public void test_parsers_invalid_format() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(invalidFormatFieldsFile);

            ArcReader reader = ArcReaderFactory.getReader(in);
            ArcRecord record;

            ArcVersionBlock version = reader.getVersionBlock();

            // Test diagnostics.
            Assert.assertNull(reader.fieldParser.parseString(null, null));
            Assert.assertNull(reader.fieldParser.parseInteger(null, null));
            Assert.assertNull(reader.fieldParser.parseLong(null, null));
            Assert.assertNull(reader.fieldParser.parseContentType(null, null));
            Assert.assertNull(reader.fieldParser.parseIpAddress(null, null));
            Assert.assertNull(reader.fieldParser.parseUri(null, null));
            Assert.assertNull(reader.fieldParser.parseDate(null, null));

            Assert.assertEquals(reader.fieldParser.parseString("", null), "");
            Assert.assertNull(reader.fieldParser.parseInteger("", null));
            Assert.assertNull(reader.fieldParser.parseLong("", null));
            Assert.assertNull(reader.fieldParser.parseContentType("", null));
            Assert.assertNull(reader.fieldParser.parseIpAddress("", null));
            Assert.assertNull(reader.fieldParser.parseUri("", null));
            Assert.assertNull(reader.fieldParser.parseDate("", null));

            Assert.assertNull(reader.fieldParser.parseInteger("one", null));
            Assert.assertNull(reader.fieldParser.parseLong("very lengthy", null));
            Assert.assertNull(reader.fieldParser.parseContentType("gif\\image", null));
            Assert.assertNull(reader.fieldParser.parseIpAddress("a.b.c.d", null));
            //Assert.assertNull(reader.fieldParser.parseUri("bad_uri", null));
            //Assert.assertNull(reader.fieldParser.parseUri("<zaphod>", null));
            Assert.assertNull(reader.fieldParser.parseDate("blue monday", null));

            Assert.assertEquals("string", reader.fieldParser.parseString("string", null));
            Assert.assertEquals(new Integer(42), reader.fieldParser.parseInteger("42", null));
            Assert.assertEquals(new Long(421234567890L), reader.fieldParser.parseLong("421234567890", null));
            Assert.assertEquals("text/plain", reader.fieldParser.parseContentType("text/plain", null).toStringShort());
            Assert.assertEquals("4.3.2.1", reader.fieldParser.parseIpAddress("4.3.2.1", null).getHostAddress());
            Assert.assertEquals("http://test/uri", reader.fieldParser.parseUri("http://test/uri", null).toString());
            Assert.assertEquals(1141546971000L, reader.fieldParser.parseDate("20060305082251", null).getTime());

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

}
