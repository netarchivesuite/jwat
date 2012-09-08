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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
import org.jwat.common.ByteCountingInputStream;
import org.jwat.common.HttpHeader;
import org.jwat.common.RandomAccessFileInputStream;
import org.jwat.common.Uri;
import org.jwat.gzip.GzipEntry;
import org.jwat.gzip.GzipReader;

@RunWith(Parameterized.class)
public class TestWarcReaderCompressed {

    private int expected_records;
    private boolean bDigest;
    private String warcFile;
    private String warcFile2;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {822, false, "IAH-20080430204825-00000-blackbook.warc.gz", "IAH-20080430204825-00000-blackbook.warc"},
                {822, true, "IAH-20080430204825-00000-blackbook.warc.gz", "IAH-20080430204825-00000-blackbook.warc"}
        });
    }

    public TestWarcReaderCompressed(int records, boolean bDigest, String warcFile, String warcFile2) {
        this.expected_records = records;
        this.bDigest = bDigest;
        this.warcFile = warcFile;
        this.warcFile2 = warcFile2;
    }

    public String getUrlPath(URL url) {
        String path = url.getFile();
        path = path.replaceAll("%5b", "[");
        path = path.replaceAll("%5d", "]");
        return path;
    }

    @Test
    public void test_warcreaderfactory_compressed_sequential() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        URL url;
        File file;
        RandomAccessFile ram;
        InputStream in;

        WarcReader reader;
        WarcRecord record;

        int records = 0;
        long consumed = 0;
        int errors = 0;
        int warnings = 0;

        try {
            List<WarcEntry> entries = indexWarcFile();
            WarcEntry entry;

            /*
             * getReaderUncompressed(in) / getNextRecord().
             */

            records = 0;
            consumed = 0;
            errors = 0;
            warnings = 0;

            url = this.getClass().getClassLoader().getResource(warcFile);
            file = new File(getUrlPath(url));
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = WarcReaderFactory.getReaderCompressed(in);

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
                        TestBaseUtils.printRecordErrors(record);
                    }

                    record.close();

                    consumed += record.getConsumed();
                    Assert.assertEquals(record.consumed, record.getConsumed());

                    // Test content-type and http response/request
                    if (record.header.contentType != null) {
                        if ("application".equals(record.header.contentType.contentType)
                                && "http".equals(record.header.contentType.mediaType)) {
                            if ("response".equals(record.header.contentType.getParameter("msgtype"))) {
                                Assert.assertNotNull(record.payload);
                                Assert.assertNotNull(record.httpHeader);
                                Assert.assertEquals(HttpHeader.HT_RESPONSE, record.httpHeader.headerType);
                            } else if ("request".equals(record.header.contentType.getParameter("msgtype"))) {
                                Assert.assertNotNull(record.payload);
                                Assert.assertNotNull(record.httpHeader);
                                Assert.assertEquals(HttpHeader.HT_REQUEST, record.httpHeader.headerType);
                            }
                        }
                    }

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                            Assert.fail( "Digest missing!" );
                        }
                    }

                    ++records;

                    if (record.diagnostics.hasErrors()) {
                        errors += record.diagnostics.getErrors().size();
                    }
                    if (record.diagnostics.hasWarnings()) {
                        warnings += record.diagnostics.getWarnings().size();
                    }

                    if (record.header.warcRecordIdUri.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                } else {
                    Assert.fail("Location incorrect");
                }
            }

            record = reader.getNextRecord();
            Assert.assertNull(record);

            url = this.getClass().getClassLoader().getResource(warcFile2);
            file = new File(getUrlPath(url));

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

            reader.close();

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

            in.close();
            ram.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);

            /*
             * getReaderUncompressed(in, buffer_size) / getNextRecord().
             */

            records = 0;
            consumed = 0;
            errors = 0;
            warnings = 0;

            url = this.getClass().getClassLoader().getResource(warcFile);
            file = new File(getUrlPath(url));
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = WarcReaderFactory.getReaderCompressed(in, 8192);

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
                        TestBaseUtils.printRecordErrors(record);
                    }

                    record.close();

                    consumed += record.getConsumed();
                    Assert.assertEquals(record.consumed, record.getConsumed());

                    // Test content-type and http response/request
                    if (record.header.contentType != null) {
                        if ("application".equals(record.header.contentType.contentType)
                                && "http".equals(record.header.contentType.mediaType)) {
                            if ("response".equals(record.header.contentType.getParameter("msgtype"))) {
                                Assert.assertNotNull(record.payload);
                                Assert.assertNotNull(record.httpHeader);
                                Assert.assertEquals(HttpHeader.HT_RESPONSE, record.httpHeader.headerType);
                            } else if ("request".equals(record.header.contentType.getParameter("msgtype"))) {
                                Assert.assertNotNull(record.payload);
                                Assert.assertNotNull(record.httpHeader);
                                Assert.assertEquals(HttpHeader.HT_REQUEST, record.httpHeader.headerType);
                            }
                        }
                    }

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                            Assert.fail( "Digest missing!" );
                        }
                    }

                    ++records;

                    if (record.diagnostics.hasErrors()) {
                        errors += record.diagnostics.getErrors().size();
                    }
                    if (record.diagnostics.hasWarnings()) {
                        warnings += record.diagnostics.getWarnings().size();
                    }

                    if (record.header.warcRecordIdUri.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                } else {
                    Assert.fail("Location incorrect");
                }
            }

            record = reader.getNextRecord();
            Assert.assertNull(record);

            url = this.getClass().getClassLoader().getResource(warcFile2);
            file = new File(getUrlPath(url));

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

            reader.close();

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

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
            Assert.fail("Unexpected io exception");
        }
    }

    @Test
    public void test_warcreaderfactory_compressed_random() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        URL url;
        File file;
        RandomAccessFile ram;
        InputStream in;

        WarcReader reader;
        WarcRecord record;

        int records = 0;
        long consumed = 0;
        int errors = 0;
        int warnings = 0;

        try {
            List<WarcEntry> entries = indexWarcFile();
            WarcEntry entry;

            /*
             * getReaderUncompressed() / getNextRecordFrom(in).
             */

            records = 0;
            consumed = 0;
            errors = 0;
            warnings = 0;

            url = this.getClass().getClassLoader().getResource(warcFile);
            file = new File(getUrlPath(url));
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = WarcReaderFactory.getReaderCompressed();

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
                        TestBaseUtils.printRecordErrors(record);
                    }

                    record.close();

                    consumed += record.getConsumed();
                    Assert.assertEquals(record.consumed, record.getConsumed());

                    // Test content-type and http response/request
                    if (record.header.contentType != null) {
                        if ("application".equals(record.header.contentType.contentType)
                                && "http".equals(record.header.contentType.mediaType)) {
                            if ("response".equals(record.header.contentType.getParameter("msgtype"))) {
                                Assert.assertNotNull(record.payload);
                                Assert.assertNotNull(record.httpHeader);
                                Assert.assertEquals(HttpHeader.HT_RESPONSE, record.httpHeader.headerType);
                            } else if ("request".equals(record.header.contentType.getParameter("msgtype"))) {
                                Assert.assertNotNull(record.payload);
                                Assert.assertNotNull(record.httpHeader);
                                Assert.assertEquals(HttpHeader.HT_REQUEST, record.httpHeader.headerType);
                            }
                        }
                    }

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                            Assert.fail( "Digest missing!" );
                        }
                    }

                    ++records;

                    if (record.diagnostics.hasErrors()) {
                        errors += record.diagnostics.getErrors().size();
                    }
                    if (record.diagnostics.hasWarnings()) {
                        warnings += record.diagnostics.getWarnings().size();
                    }

                    if (record.header.warcRecordIdUri.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                } else {
                    Assert.fail("Location incorrect");
                }
            }

            record = reader.getNextRecordFrom(in, reader.getConsumed());
            Assert.assertNull(record);

            url = this.getClass().getClassLoader().getResource(warcFile2);
            file = new File(getUrlPath(url));

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

            reader.close();

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

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

            url = this.getClass().getClassLoader().getResource(warcFile);
            file = new File(getUrlPath(url));
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = WarcReaderFactory.getReaderCompressed();

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
                        TestBaseUtils.printRecordErrors(record);
                    }

                    record.close();

                    consumed += record.getConsumed();
                    Assert.assertEquals(record.consumed, record.getConsumed());

                    // Test content-type and http response/request
                    if (record.header.contentType != null) {
                        if ("application".equals(record.header.contentType.contentType)
                                && "http".equals(record.header.contentType.mediaType)) {
                            if ("response".equals(record.header.contentType.getParameter("msgtype"))) {
                                Assert.assertNotNull(record.payload);
                                Assert.assertNotNull(record.httpHeader);
                                Assert.assertEquals(HttpHeader.HT_RESPONSE, record.httpHeader.headerType);
                            } else if ("request".equals(record.header.contentType.getParameter("msgtype"))) {
                                Assert.assertNotNull(record.payload);
                                Assert.assertNotNull(record.httpHeader);
                                Assert.assertEquals(HttpHeader.HT_REQUEST, record.httpHeader.headerType);
                            }
                        }
                    }

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                            Assert.fail( "Digest missing!" );
                        }
                    }

                    ++records;

                    if (record.diagnostics.hasErrors()) {
                        errors += record.diagnostics.getErrors().size();
                    }
                    if (record.diagnostics.hasWarnings()) {
                        warnings += record.diagnostics.getWarnings().size();
                    }

                    if (record.header.warcRecordIdUri.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                } else {
                    Assert.fail("Location incorrect");
                }
            }

            record = reader.getNextRecordFrom(in, reader.getConsumed(), 8192);
            Assert.assertNull(record);

            url = this.getClass().getClassLoader().getResource(warcFile2);
            file = new File(getUrlPath(url));

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

            reader.close();

            Assert.assertEquals(ram.length(), reader.getConsumed());
            Assert.assertEquals(ram.length(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

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
            Assert.fail("Unexpected io exception");
        }
    }

    class WarcEntry {
        Uri recordId;
        long offset;
    }

    public List<WarcEntry> indexWarcFile() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        List<WarcEntry> warcEntries = new ArrayList<WarcEntry>();
        WarcEntry warcEntry;

        int records = 0;
        long consumed = 0;
        int errors = 0;
        int warnings = 0;

        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(warcFile);
            ByteCountingInputStream bcin = new ByteCountingInputStream(in);
            WarcReader reader = WarcReaderFactory.getReader(bcin);

            reader.setBlockDigestEnabled( bDigest );
            Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
            reader.setPayloadDigestEnabled( bDigest );
            Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));

            Iterator<WarcRecord> recordIterator = reader.iterator();
            WarcRecord record;

            while (recordIterator.hasNext()) {
                record = recordIterator.next();
                ++records;

                if (record.header.warcRecordIdUri == null) {
                    Assert.fail("Invalid warc-record-id");
                }

                Assert.assertThat(record.getStartOffset(), is(equalTo(reader.getStartOffset())));
                Assert.assertThat(record.getStartOffset(), is(not(equalTo(reader.getOffset()))));

                warcEntry = new WarcEntry();
                warcEntry.recordId = record.header.warcRecordIdUri;
                warcEntry.offset = record.getStartOffset();
                warcEntries.add(warcEntry);

                record.close();

                consumed += record.getConsumed();
                Assert.assertEquals(record.consumed, record.getConsumed());

                // Test content-type and http response/request
                if (record.header.contentType != null) {
                    if ("application".equals(record.header.contentType.contentType)
                            && "http".equals(record.header.contentType.mediaType)) {
                        if ("response".equals(record.header.contentType.getParameter("msgtype"))) {
                            Assert.assertNotNull(record.payload);
                            Assert.assertNotNull(record.httpHeader);
                            Assert.assertEquals(HttpHeader.HT_RESPONSE, record.httpHeader.headerType);
                        } else if ("request".equals(record.header.contentType.getParameter("msgtype"))) {
                            Assert.assertNotNull(record.payload);
                            Assert.assertNotNull(record.httpHeader);
                            Assert.assertEquals(HttpHeader.HT_REQUEST, record.httpHeader.headerType);
                        }
                    }
                }

                Assert.assertThat(record.getStartOffset(), is(equalTo(reader.getStartOffset())));
                Assert.assertThat(record.getStartOffset(), is(not(equalTo(reader.getOffset()))));

                if ( bDigest ) {
                    if ( (record.payload != null && record.computedBlockDigest == null)
                            || (record.httpHeader != null && record.computedPayloadDigest == null) ) {
                        Assert.fail( "Digest missing!" );
                    }
                }

                if (bDebugOutput) {
                    System.out.println("0x" + Long.toString(warcEntry.offset, 16) + "(" + warcEntry.offset + ") - " + warcEntry.recordId);
                }

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

            URL url = this.getClass().getClassLoader().getResource(warcFile2);
            File file = new File(getUrlPath(url));

            Assert.assertEquals(bcin.getConsumed(), reader.getConsumed());
            Assert.assertEquals(bcin.getConsumed(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);

            reader.close();
            bcin.close();

            Assert.assertEquals(bcin.getConsumed(), reader.getConsumed());
            Assert.assertEquals(bcin.getConsumed(), reader.getOffset());
            Assert.assertEquals(file.length(), consumed);
        } catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }

        Assert.assertEquals(expected_records, records);
        Assert.assertEquals(0, errors);
        Assert.assertEquals(0, warnings);

        return warcEntries;
    }

    @Test
    public void test_arcreadercompressed_exceptions() {
        WarcReaderCompressed reader = WarcReaderFactory.getReaderCompressed();

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
        WarcRecord record = new WarcRecord() {
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
