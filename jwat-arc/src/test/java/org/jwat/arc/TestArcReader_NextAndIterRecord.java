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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestArcReader_NextAndIterRecord {

    private ArcVersion expected_version;
    private int expected_records;
    private String arcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {ArcVersion.VERSION_1_1, 101, "1-1-20110922131213-00000-svc-VirtualBox.arc"},
                {ArcVersion.VERSION_1_1, 238, "4-3-20111004123336-00000-svc-VirtualBox.arc"},
                {ArcVersion.VERSION_1_1, 299, "IAH-20080430204825-00000-blackbook.arc"},
                {ArcVersion.VERSION_1, 5, "small_BNF.arc"}
        });
    }

    public TestArcReader_NextAndIterRecord(ArcVersion version, int records, String arcFile) {
        this.expected_version = version;
        this.expected_records = records;
        this.arcFile = arcFile;
    }

    @Test
    public void test_arcreader_nextrecord_and_iterator() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        ArcReader reader;
        Iterator<ArcRecordBase> recordIterator;
        ArcRecordBase record;

        int n_records = 0;
        int n_errors = 0;
        int n_warnings = 0;

        int i_records = 0;
        int i_errors = 0;
        int i_warnings = 0;

        try {
            /*
             * getNextArcRecord.
             */

            in = this.getClass().getClassLoader().getResourceAsStream(arcFile);

            reader = ArcReaderFactory.getReader(in);
            record = reader.getNextRecord();

            Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
            Assert.assertTrue(record.isCompliant());
            Assert.assertNotNull(record.versionHeader);
            Assert.assertNotNull(record.versionHeader.isValid());
            Assert.assertEquals(expected_version, record.versionHeader.version);

            if (record != null) {
                if (bDebugOutput) {
                    TestBaseUtils.printRecord(record);
                }

                boolean b = true;
                while ( b ) {
                    record = reader.getNextRecord();
                    if (record != null) {
                        if (bDebugOutput) {
                            TestBaseUtils.printRecord(record);
                        }

                        Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
                        Assert.assertTrue(record.isCompliant());
                        Assert.assertNull(record.versionHeader);

                        ++n_records;

                        if (record.diagnostics.hasErrors()) {
                            n_errors += record.diagnostics.getErrors().size();
                        }
                        if (record.diagnostics.hasWarnings()) {
                            n_warnings += record.diagnostics.getWarnings().size();
                        }
                    }
                    else {
                        b = false;
                    }
                }

                if (bDebugOutput) {
                    TestBaseUtils.printStatus(n_records, n_errors, n_warnings);
                }
            }

            reader.close();
            in.close();

            /*
             * Iterator.
             */

            in = this.getClass().getClassLoader().getResourceAsStream(arcFile);

            reader = ArcReaderFactory.getReader(in);
            recordIterator = reader.iterator();

            if (recordIterator.hasNext()) {
                Assert.assertTrue(recordIterator.hasNext());

                Assert.assertNull(reader.getIteratorExceptionThrown());
                record = recordIterator.next();
                Assert.assertNull(reader.getIteratorExceptionThrown());

                Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
                Assert.assertTrue(record.isCompliant());
                Assert.assertNotNull(record.versionHeader);
                Assert.assertNotNull(record.versionHeader.isValid());
                Assert.assertEquals(expected_version, record.versionHeader.version);

                if (record != null) {
                    if (bDebugOutput) {
                        TestBaseUtils.printRecord(record);
                    }

                    while (recordIterator.hasNext()) {
                        Assert.assertTrue(recordIterator.hasNext());

                        Assert.assertNull(reader.getIteratorExceptionThrown());
                        record = recordIterator.next();
                        Assert.assertNull(reader.getIteratorExceptionThrown());
                        if (bDebugOutput) {
                            TestBaseUtils.printRecord(record);
                        }

                        Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
                        Assert.assertTrue(record.isCompliant());
                        Assert.assertNull(record.versionHeader);

                        ++i_records;

                        if (record.diagnostics.hasErrors()) {
                            i_errors += record.diagnostics.getErrors().size();
                        }
                        if (record.diagnostics.hasWarnings()) {
                            i_warnings += record.diagnostics.getWarnings().size();
                        }
                    }
                    Assert.assertNull(reader.getIteratorExceptionThrown());

                    if (bDebugOutput) {
                        TestBaseUtils.printStatus(i_records, i_errors, i_warnings);
                    }
                }
            }
            Assert.assertNull(reader.getIteratorExceptionThrown());

            Assert.assertFalse(recordIterator.hasNext());

            try {
                recordIterator.next();
                Assert.fail("Exception expected!");
            } catch (NoSuchElementException e) {
            }

            try {
                recordIterator.remove();
                Assert.fail("Exception expected!");
            } catch (UnsupportedOperationException e) {
            }

            reader.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(n_records, i_records);
        Assert.assertEquals(n_errors, i_errors);

        Assert.assertEquals(expected_records, n_records);
        Assert.assertEquals(expected_records, i_records);

        Assert.assertEquals(0, n_errors);
        Assert.assertEquals(0, i_errors);

        Assert.assertEquals(0, n_warnings);
        Assert.assertEquals(0, i_warnings);
    }

}
