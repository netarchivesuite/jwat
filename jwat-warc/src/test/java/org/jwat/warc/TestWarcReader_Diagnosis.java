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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;
import org.jwat.common.RandomAccessFileInputStream;

@RunWith(JUnit4.class)
public class TestWarcReader_Diagnosis {

    @Test
    public void test_warcreader_emptyfile() {
        try {
            byte[] bytes = new byte[0];
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);

            // Save testfile.
            SaveWarcTestFiles.saveTestWarcReader_Diagnosis(bytes);

            WarcReader reader = WarcReaderFactory.getReaderUncompressed();
            WarcRecord record = reader.getNextRecordFrom(in, 0);

            Assert.assertNull(record);

            Assert.assertTrue(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(1, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            Object[][] expectedErrors = new Object[][] {
                {DiagnosisType.ERROR_EXPECTED, "WARC file", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedErrors, reader.diagnostics.getErrors());

            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_warcreader_diagnosis_garbage() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("\r\n".getBytes());
            out.write("The monkeys are a coming!\r\n".getBytes());
            out.write("\r\n".getBytes());
            out.write("Donkey Kong country...\r\n".getBytes());
            out.write("\r\n".getBytes());

            // Save testfile.
            SaveWarcTestFiles.saveTestWarcReader_Diagnosis(out.toByteArray());

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            WarcReader reader = WarcReaderFactory.getReaderUncompressed();

            WarcRecord record = reader.getNextRecordFrom(in, 0);

            Assert.assertNull(record);

            Assert.assertTrue(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(4, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            Object[][] expectedErrors = new Object[][] {
                {DiagnosisType.INVALID, "Data before WARC version", 0},
                {DiagnosisType.INVALID, "Empty lines before WARC version", 0},
                {DiagnosisType.ERROR_EXPECTED, "WARC file", 1},
                {DiagnosisType.UNDESIRED_DATA, "Trailing data", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedErrors, reader.diagnostics.getErrors());

            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_warcreader_diagnosis_record_garbage() {
        try {
            File testfile = TestHelpers.getTestResourceFile("invalid-warcfile-record-then-garbage.warc");
            RandomAccessFile raf = new RandomAccessFile(testfile, "r");
            RandomAccessFileInputStream rafIn = new RandomAccessFileInputStream(raf);

            WarcReader reader = WarcReaderFactory.getReaderUncompressed(rafIn);
            WarcRecord record = reader.getNextRecord();

            Assert.assertNotNull(record);

            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(0, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            record = reader.getNextRecord();

            Assert.assertNull(record);

            Assert.assertTrue(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(3, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            Object[][] expectedErrors = new Object[][] {
                {DiagnosisType.INVALID, "Data before WARC version", 0},
                {DiagnosisType.INVALID, "Empty lines before WARC version", 0},
                {DiagnosisType.UNDESIRED_DATA, "Trailing data", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedErrors, reader.diagnostics.getErrors());

            reader.close();
            rafIn.close();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
