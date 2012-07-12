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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.RandomAccessFileInputStream;

@RunWith(Parameterized.class)
public class TestArcReaderFactoryCompressed {

    private int expected_records;
    private String arcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {300, "IAH-20080430204825-00000-blackbook.arc.gz"}
        });
    }

    public TestArcReaderFactoryCompressed(int records, String arcFile) {
        this.expected_records = records;
        this.arcFile = arcFile;
    }

    @Test
    public void test_arcreaderfactory_compressed() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        URL url;
        String path;
        File file;
        RandomAccessFile ram;
        InputStream in;

        ArcReader reader;
        ArcRecordBase record;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            List<ArcEntry> entries = indexArcFile();
            ArcEntry entry;

            /*
             * getReaderUncompressed() / nextRecordFrom(in).
             */

            records = 0;
            errors = 0;
            warnings = 0;

            url = this.getClass().getClassLoader().getResource(arcFile);
            path = url.getFile();
            path = path.replaceAll("%5b", "[");
            path = path.replaceAll("%5d", "]");
            file = new File(path);
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderCompressed();

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, entry.offset)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    ++records;

                    switch (records) {
                    case 1:
                        Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
                        Assert.assertTrue(record.isCompliant());
                        Assert.assertNotNull(record.versionHeader);
                        Assert.assertNotNull(record.versionHeader.isValid());
                        Assert.assertEquals(ArcVersion.VERSION_1_1, record.versionHeader.version);
                        break;
                    default:
                        Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
                        Assert.assertTrue(record.isCompliant());
                        Assert.assertNull(record.versionHeader);
                        break;
                    }

                    if (record.diagnostics.hasErrors()) {
                        errors += record.diagnostics.getErrors().size();
                    }
                    if (record.diagnostics.hasWarnings()) {
                        warnings += record.diagnostics.getWarnings().size();
                    }

                    if (record.header.urlUri.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                } else {
                    Assert.fail("Location incorrect");
                }
            }

            reader.close();
            in.close();
            ram.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);

            /*
             * getReaderUncompressed(in) / nextRecordFrom(in, buffer_size).
             */

            records = 0;
            errors = 0;
            warnings = 0;

            url = this.getClass().getClassLoader().getResource(arcFile);
            path = url.getFile();
            path = path.replaceAll("%5b", "[");
            path = path.replaceAll("%5d", "]");
            file = new File(path);
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderCompressed(in);

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, entry.offset, 8192)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    ++records;

                    switch (records) {
                    case 1:
                        Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
                        Assert.assertTrue(record.isCompliant());
                        Assert.assertNotNull(record.versionHeader);
                        Assert.assertNotNull(record.versionHeader.isValid());
                        Assert.assertEquals(ArcVersion.VERSION_1_1, record.versionHeader.version);
                        break;
                    default:
                        Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
                        Assert.assertTrue(record.isCompliant());
                        Assert.assertNull(record.versionHeader);
                        break;
                    }

                    if (record.diagnostics.hasErrors()) {
                        errors += record.diagnostics.getErrors().size();
                    }
                    if (record.diagnostics.hasWarnings()) {
                        warnings += record.diagnostics.getWarnings().size();
                    }

                    if (record.header.urlUri.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                } else {
                    Assert.fail("Location incorrect");
                }
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);

            /*
             * getReaderUncompressed(in, buffer_size) / nextRecordFrom(in).
             */

            records = 0;
            errors = 0;
            warnings = 0;

            url = this.getClass().getClassLoader().getResource(arcFile);
            path = url.getFile();
            path = path.replaceAll("%5b", "[");
            path = path.replaceAll("%5d", "]");
            file = new File(path);
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderCompressed(in, 8192);

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, entry.offset)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    ++records;

                    switch (records) {
                    case 1:
                        Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
                        Assert.assertTrue(record.isCompliant());
                        Assert.assertNotNull(record.versionHeader);
                        Assert.assertNotNull(record.versionHeader.isValid());
                        Assert.assertEquals(ArcVersion.VERSION_1_1, record.versionHeader.version);
                        break;
                    default:
                        Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
                        Assert.assertTrue(record.isCompliant());
                        Assert.assertNull(record.versionHeader);
                        break;
                    }

                    if (record.diagnostics.hasErrors()) {
                        errors += record.diagnostics.getErrors().size();
                    }
                    if (record.diagnostics.hasWarnings()) {
                        warnings += record.diagnostics.getWarnings().size();
                    }

                    if (record.header.urlUri.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                } else {
                    Assert.fail("Location incorrect");
                }
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected io exception");
        }
    }

    class ArcEntry {
        URI recordId;
        long offset;
    }

    public List<ArcEntry> indexArcFile() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        List<ArcEntry> arcEntries = new ArrayList<ArcEntry>();
        ArcEntry arcEntry;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(arcFile);

            ArcReader reader = ArcReaderFactory.getReader(in);
            ArcRecordBase record;

            //System.out.println(version.xml);

            Iterator<ArcRecordBase> recordIterator = reader.iterator();

            while (recordIterator.hasNext()) {
                record = recordIterator.next();
                ++records;

                switch (records) {
                case 1:
                    Assert.assertEquals(ArcRecordBase.RT_VERSION_BLOCK, record.recordType);
                    Assert.assertTrue(record.isCompliant());
                    Assert.assertNotNull(record.versionHeader);
                    Assert.assertNotNull(record.versionHeader.isValid());
                    Assert.assertEquals(ArcVersion.VERSION_1_1, record.versionHeader.version);
                    break;
                default:
                    Assert.assertEquals(ArcRecordBase.RT_ARC_RECORD, record.recordType);
                    Assert.assertTrue(record.isCompliant());
                    Assert.assertNull(record.versionHeader);
                    break;
                }

                if (record.header.urlUri == null) {
                    Assert.fail("Invalid arc uri");
                }

                Assert.assertThat(record.getStartOffset(), is(equalTo(reader.getStartOffset())));
                Assert.assertThat(record.getStartOffset(), is(not(equalTo(reader.getOffset()))));

                //System.out.println(record.getStartOffset());
                //System.out.println(reader.getStartOffset());
                //System.out.println(reader.getOffset());

                arcEntry = new ArcEntry();
                arcEntry.recordId = record.header.urlUri;
                arcEntry.offset = record.getStartOffset();
                arcEntries.add(arcEntry);

                if (bDebugOutput) {
                    System.out.println("0x" + Long.toString(arcEntry.offset, 16) + "(" + arcEntry.offset + ") - " + arcEntry.recordId);
                }

                record.close();

                Assert.assertThat(record.getStartOffset(), is(equalTo(reader.getStartOffset())));
                Assert.assertThat(record.getStartOffset(), is(not(equalTo(reader.getOffset()))));

                //System.out.println(record.getStartOffset());
                //System.out.println(reader.getStartOffset());
                //System.out.println(reader.getOffset());

                if (record.diagnostics.hasErrors()) {
                    errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    warnings += record.diagnostics.getWarnings().size();
                }
            }

            if (reader.getIteratorExceptionThrown() != null) {
                reader.getIteratorExceptionThrown().printStackTrace();
                Assert.fail("Unexpected exception!");
            }

            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception");
        }

        Assert.assertEquals(expected_records, records);
        Assert.assertEquals(0, errors);
        Assert.assertEquals(0, warnings);

        return arcEntries;
    }

}
