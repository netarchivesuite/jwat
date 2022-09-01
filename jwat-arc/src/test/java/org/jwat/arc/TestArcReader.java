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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;
import org.jwat.common.RandomAccessFileInputStream;

@RunWith(JUnit4.class)
public class TestArcReader {

    @Test
    public void test_arcreader_iterator_exceptions() {
        ArcReader reader = new ArcReader() {
            @Override
            public boolean isCompressed() {
                return false;
            }
            @Override
            public void close() {
            }
            @Override
            protected void recordClosed() {
            }
            @Override
            public long getStartOffset() {
                return 0;
            }
            @Override
            public long getOffset() {
                return 0;
            }
            @Override
            public long getConsumed() {
                return 0;
            }
            @Override
            public ArcRecordBase getNextRecord() throws IOException {
                throw new IOException();
            }
            @Override
            public ArcRecordBase getNextRecordFrom(InputStream in,
                    long offset) throws IOException {
                return null;
            }
            @Override
            public ArcRecordBase getNextRecordFrom(InputStream in,
                    long offset, int buffer_size) throws IOException {
                return null;
            }
        };
        Iterator<ArcRecordBase> iter = reader.iterator();

        Assert.assertNull(reader.iteratorExceptionThrown);
        iter.hasNext();
        Assert.assertNotNull(reader.iteratorExceptionThrown);
        try {
            iter.next();
            Assert.fail("Exception expected!");
        } catch (NoSuchElementException e) {
        }
        Assert.assertNotNull(reader.iteratorExceptionThrown);

        Assert.assertTrue(reader.bIsCompliant);
        Assert.assertEquals(0, reader.consumed);
        Assert.assertEquals(0, reader.records);
        Assert.assertEquals(0, reader.errors);
        Assert.assertEquals(0, reader.warnings);

        reader.bIsCompliant = false;
        reader.consumed = 1;
        reader.records = 2;
        reader.errors = 3;
        reader.warnings = 4;

        Assert.assertFalse(reader.bIsCompliant);
        Assert.assertEquals(1, reader.consumed);
        Assert.assertEquals(2, reader.records);
        Assert.assertEquals(3, reader.errors);
        Assert.assertEquals(4, reader.warnings);

        reader.reset();

        Assert.assertTrue(reader.bIsCompliant);
        Assert.assertEquals(0, reader.consumed);
        Assert.assertEquals(0, reader.records);
        Assert.assertEquals(0, reader.errors);
        Assert.assertEquals(0, reader.warnings);

        reader.close();
    }

    @Test
    public void test_empty_arcfile() {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            ArcReader reader = ArcReaderFactory.getReaderUncompressed(in);
            ArcRecordBase record = reader.getNextRecord();
            Assert.assertNull(record);

            Assert.assertFalse(reader.isCompliant());

            Assert.assertTrue(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(1, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            Object[][] expectedErrors = new Object[][] {
                {DiagnosisType.ERROR_EXPECTED, "ARC file", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedErrors, reader.diagnostics.getErrors());

            reader.close();
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_arcfile_garbage() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("http://www.bt.dk/node/100".getBytes());

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            ArcReader reader = ArcReaderFactory.getReaderUncompressed(in);

            ArcRecordBase record = reader.getNextRecord();
            Assert.assertNull(record);

            Assert.assertFalse(reader.isCompliant());

            Assert.assertTrue(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(2, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            Object[][] expectedErrors = new Object[][] {
                {DiagnosisType.ERROR_EXPECTED, "ARC file", 1},
                {DiagnosisType.UNDESIRED_DATA, "Trailing data", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedErrors, reader.diagnostics.getErrors());

            reader.close();
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    @Test
    public void test_arcfile_record_then_garbage() {
        try {
            File testfile = TestHelpers.getTestResourceFile("invalid-arcfile-record-then-garbage.arc");
            RandomAccessFile raf = new RandomAccessFile(testfile, "r");
            RandomAccessFileInputStream rafIn = new RandomAccessFileInputStream(raf);

            ArcReader reader = ArcReaderFactory.getReaderUncompressed(rafIn);
            ArcRecordBase record = reader.getNextRecord();

            Assert.assertNotNull(record);
            Assert.assertFalse(reader.isCompliant());

            Assert.assertFalse(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(0, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            record = reader.getNextRecord();

            Assert.assertNull(record);
            Assert.assertFalse(reader.isCompliant());

            Assert.assertTrue(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(1, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            Object[][] expectedErrors = new Object[][] {
                {DiagnosisType.UNDESIRED_DATA, "Trailing data", 1}
            };
            TestBaseUtils.compareDiagnoses(expectedErrors, reader.diagnostics.getErrors());

            reader.close();
            rafIn.close();
            raf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
