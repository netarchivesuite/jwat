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
import org.jwat.common.ByteCountingInputStream;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.RandomAccessFileInputStream;
import org.jwat.common.Uri;
import org.jwat.gzip.GzipEntry;
import org.jwat.gzip.GzipReader;

@RunWith(Parameterized.class)
public class TestArcReaderCompressed {

    private int expected_records;
    private boolean bDigest;
    private String arcFile;
    private String arcFile2;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {300, false, "IAH-20080430204825-00000-blackbook.arc.gz", "IAH-20080430204825-00000-blackbook.arc"},
                {300, true, "IAH-20080430204825-00000-blackbook.arc.gz", "IAH-20080430204825-00000-blackbook.arc"}
        });
    }

    public TestArcReaderCompressed(int records, boolean bDigest, String arcFile, String arcFile2) {
        this.expected_records = records;
        this.bDigest = bDigest;
        this.arcFile = arcFile;
        this.arcFile2 = arcFile2;
    }

    @Test
    public void test_arcreaderfactory_compressed_sequential() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        File resourceFile;
        RandomAccessFile ram;
        InputStream in;

        ArcReader reader;
        ArcRecordBase record;

        int records = 0;
        long consumed = 0;
        int errors = 0;
        int warnings = 0;

        try {
            List<ArcEntry> entries = indexArcFile();
            ArcEntry entry;

            /*
             * getReaderCompressed(in) / getNextRecord().
             */

            records = 0;
            consumed = 0;
            errors = 0;
            warnings = 0;

            resourceFile = TestHelpers.getTestResourceFile(arcFile);
            ram = new RandomAccessFile(resourceFile, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderCompressed(in);

            reader.setBlockDigestEnabled( bDigest );
            Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
            reader.setPayloadDigestEnabled( bDigest );
            Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                try {
                    reader.getNextRecordFrom(in, entry.offset);
                    Assert.fail("Exception expected!");
                } catch (IllegalStateException e) {
                }

                try {
                    reader.getNextRecordFrom(in, entry.offset, 8192);
                    Assert.fail("Exception expected!");
                } catch (IllegalStateException e) {
                }

                if ((record = reader.getNextRecord()) != null) {
                    if (bDebugOutput) {
                        TestBaseUtils.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    consumed += record.getConsumed();
                    Assert.assertEquals(record.consumed, record.getConsumed());

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                            Assert.fail( "Digest missing!" );
                        }
                    }

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

            record = reader.getNextRecord();
            Assert.assertNull(record);

            resourceFile = TestHelpers.getTestResourceFile(arcFile2);

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            reader.close();

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            in.close();
            ram.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);

            /*
             * getReaderCompressed(in, buffer_size) / getNextRecord().
             */

            records = 0;
            consumed = 0;
            errors = 0;
            warnings = 0;

            resourceFile = TestHelpers.getTestResourceFile(arcFile);
            ram = new RandomAccessFile(resourceFile, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderCompressed(in, 8192);

            reader.setBlockDigestEnabled( bDigest );
            Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
            reader.setPayloadDigestEnabled( bDigest );
            Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                try {
                    reader.getNextRecordFrom(in, entry.offset);
                    Assert.fail("Exception expected!");
                } catch (IllegalStateException e) {
                }

                try {
                    reader.getNextRecordFrom(in, entry.offset, 8192);
                    Assert.fail("Exception expected!");
                } catch (IllegalStateException e) {
                }

                if ((record = reader.getNextRecord()) != null) {
                    if (bDebugOutput) {
                        TestBaseUtils.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    consumed += record.getConsumed();
                    Assert.assertEquals(record.consumed, record.getConsumed());

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                            Assert.fail( "Digest missing!" );
                        }
                    }

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

            record = reader.getNextRecord();
            Assert.assertNull(record);

            resourceFile = TestHelpers.getTestResourceFile(arcFile2);

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            reader.close();

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            in.close();
            ram.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected I/O exception");
        }
    }

    @Test
    public void test_arcreaderfactory_compressed_random() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        File resourceFile;
        RandomAccessFile ram;
        InputStream in;

        ArcReader reader;
        ArcRecordBase record;

        int records = 0;
        long consumed = 0;
        int errors = 0;
        int warnings = 0;

        try {
            List<ArcEntry> entries = indexArcFile();
            ArcEntry entry;

            /*
             * getReaderUncompressed() / getNextRecordFrom(in).
             */

            records = 0;
            consumed = 0;
            errors = 0;
            warnings = 0;

            resourceFile = TestHelpers.getTestResourceFile(arcFile);
            ram = new RandomAccessFile(resourceFile, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderCompressed();

            reader.setBlockDigestEnabled( bDigest );
            Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
            reader.setPayloadDigestEnabled( bDigest );
            Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                try {
                    reader.getNextRecord();
                    Assert.fail("Exception expected!");
                } catch (IllegalStateException e) {
                }

                if ((record = reader.getNextRecordFrom(in, entry.offset)) != null) {
                    if (bDebugOutput) {
                        TestBaseUtils.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    consumed += record.getConsumed();
                    Assert.assertEquals(record.consumed, record.getConsumed());

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                            Assert.fail( "Digest missing!" );
                        }
                    }

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

            record = reader.getNextRecordFrom(in, reader.getConsumed());
            Assert.assertNull(record);

            resourceFile = TestHelpers.getTestResourceFile(arcFile2);

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            reader.close();

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            in.close();
            ram.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);

            /*
             * getReaderUncompressed() / getNextRecordFrom(in, buffer_size).
             */

            records = 0;
            consumed = 0;
            errors = 0;
            warnings = 0;

            resourceFile = TestHelpers.getTestResourceFile(arcFile);
            ram = new RandomAccessFile(resourceFile, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderCompressed();

            reader.setBlockDigestEnabled( bDigest );
            Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
            reader.setPayloadDigestEnabled( bDigest );
            Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                try {
                    reader.getNextRecord();
                    Assert.fail("Exception expected!");
                } catch (IllegalStateException e) {
                }

                if ((record = reader.getNextRecordFrom(in, entry.offset, 8192)) != null) {
                    if (bDebugOutput) {
                        TestBaseUtils.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    consumed += record.getConsumed();
                    Assert.assertEquals(record.consumed, record.getConsumed());

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                            Assert.fail( "Digest missing!" );
                        }
                    }

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

            record = reader.getNextRecordFrom(in, reader.getConsumed(), 8192);
            Assert.assertNull(record);

            resourceFile = TestHelpers.getTestResourceFile(arcFile2);

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            reader.close();

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            in.close();
            ram.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected I/O exception");
        }
    }

    class ArcEntry {
        Uri recordId;
        long offset;
    }

    public List<ArcEntry> indexArcFile() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        List<ArcEntry> arcEntries = new ArrayList<ArcEntry>();
        ArcEntry arcEntry;

        int records = 0;
        long consumed = 0;
        int errors = 0;
        int warnings = 0;

        try {
            InputStream in = TestHelpers.getTestResourceAsStream(arcFile);
            ByteCountingInputStream bcin = new ByteCountingInputStream(in);
            ArcReader reader = ArcReaderFactory.getReader(bcin);
            ArcRecordBase record;

            reader.setBlockDigestEnabled( bDigest );
            Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
            reader.setPayloadDigestEnabled( bDigest );
            Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));

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

                consumed += record.getConsumed();
                Assert.assertEquals(record.consumed, record.getConsumed());

                Assert.assertThat(record.getStartOffset(), is(equalTo(reader.getStartOffset())));
                Assert.assertThat(record.getStartOffset(), is(not(equalTo(reader.getOffset()))));

                if ( bDigest ) {
                    if ( (record.payload != null && record.computedBlockDigest == null)
                            || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                        Assert.fail( "Digest missing!" );
                    }
                }

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

            File resourceFile = TestHelpers.getTestResourceFile(arcFile2);

            Assert.assertEquals(bcin.getConsumed(), reader.getConsumed());
            Assert.assertEquals(bcin.getConsumed(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);

            reader.close();
            bcin.close();

            Assert.assertEquals(bcin.getConsumed(), reader.getConsumed());
            Assert.assertEquals(bcin.getConsumed(), reader.getOffset());
            Assert.assertEquals(resourceFile.length(), consumed);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception");
        }

        Assert.assertEquals(expected_records, records);
        Assert.assertEquals(0, errors);
        Assert.assertEquals(0, warnings);

        return arcEntries;
    }

    @Test
    public void test_arcreadercompressed_exceptions() {
        ArcReaderCompressed reader = ArcReaderFactory.getReaderCompressed();

        InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };
        GzipReader gzipReader = new GzipReader(in) {
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };
        ArcRecordBase record = new ArcRecordBase() {
            @Override
            protected void processPayload(ByteCountingPushBackInputStream in,
                    ArcReader reader) throws IOException {
            }
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };
        GzipEntry gzipEntry = new GzipEntry() {
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };

        Assert.assertNull(reader.reader);
        Assert.assertNull(reader.currentRecord);

        reader.reader = gzipReader;
        reader.close();
        Assert.assertNull(reader.reader);
        Assert.assertNull(reader.currentRecord);

        reader.currentRecord = record;
        reader.close();
        Assert.assertNull(reader.reader);
        Assert.assertNull(reader.currentRecord);

        try {
            reader.recordClosed();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        Assert.assertNull(reader.currentEntry);
        reader.currentEntry = gzipEntry;
        reader.recordClosed();
        Assert.assertNull(reader.currentEntry);
    }

}
