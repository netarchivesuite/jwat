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
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
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
public class TestWarcReaderFactoryCompressed {

    private int expected_records;
    private boolean bDigest;
    private String warcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {822, false, "IAH-20080430204825-00000-blackbook.warc.gz"},
                {822, true, "IAH-20080430204825-00000-blackbook.warc.gz"}
        });
    }

    public TestWarcReaderFactoryCompressed(int records, boolean bDigest, String warcFile) {
        this.expected_records = records;
        this.bDigest = bDigest;
        this.warcFile = warcFile;
    }

    @Test
    public void test_warcreaderfactory_compressed() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        URL url;
        File file;
        RandomAccessFile ram;
        InputStream in;

        WarcReader reader;
        WarcRecord record;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            List<WarcEntry> entries = indexWarcFile();
            WarcEntry entry;

            /*
             * getReaderUncompressed() / nextRecordFrom(in).
             */

            records = 0;
            errors = 0;
            warnings = 0;

            url = this.getClass().getClassLoader().getResource(warcFile);
            file = new File(url.getFile());
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = WarcReaderFactory.getReaderCompressed();

            reader.setBlockDigestEnabled( true );
            reader.setBlockDigestAlgorithm( "sha1" );
            reader.setPayloadDigestEnabled( true );
            reader.setPayloadDigestAlgorithm( "sha1" );

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, entry.offset)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpResponse != null && record.computedPayloadDigest == null) ) {
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

                    if (record.warcRecordIdUri.compareTo(entry.recordId) != 0) {
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

            url = this.getClass().getClassLoader().getResource(warcFile);
            file = new File(url.getFile());
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = WarcReaderFactory.getReaderCompressed(in);

            reader.setBlockDigestEnabled( true );
            reader.setBlockDigestAlgorithm( "sha1" );
            reader.setPayloadDigestEnabled( true );
            reader.setPayloadDigestAlgorithm( "sha1" );

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, entry.offset, 8192)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpResponse != null && record.computedPayloadDigest == null) ) {
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

                    if (record.warcRecordIdUri.compareTo(entry.recordId) != 0) {
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

            url = this.getClass().getClassLoader().getResource(warcFile);
            file = new File(url.getFile());
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = WarcReaderFactory.getReaderCompressed(in, 8192);

            reader.setBlockDigestEnabled( true );
            reader.setBlockDigestAlgorithm( "sha1" );
            reader.setPayloadDigestEnabled( true );
            reader.setPayloadDigestAlgorithm( "sha1" );

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, entry.offset)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    if ( bDigest ) {
                        if ( (record.payload != null && record.computedBlockDigest == null)
                                || (record.httpResponse != null && record.computedPayloadDigest == null) ) {
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

                    if (record.warcRecordIdUri.compareTo(entry.recordId) != 0) {
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
            Assert.fail("Unexpected io exception");
        } catch (NoSuchAlgorithmException e) {
            Assert.fail("Unexpected algorithm exception");
        }
    }

    class WarcEntry {
        URI recordId;
        long offset;
    }

    public List<WarcEntry> indexWarcFile() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        List<WarcEntry> warcEntries = new ArrayList<WarcEntry>();
        WarcEntry warcEntry;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            WarcReader reader = WarcReaderFactory.getReader(in);

            reader.setBlockDigestEnabled( true );
            reader.setBlockDigestAlgorithm( "sha1" );
            reader.setPayloadDigestEnabled( true );
            reader.setPayloadDigestAlgorithm( "sha1" );

            Iterator<WarcRecord> recordIterator = reader.iterator();
            WarcRecord record;

            while (recordIterator.hasNext()) {
                record = recordIterator.next();
                ++records;

                if (record.warcRecordIdUri == null) {
                    Assert.fail("Invalid warc-record-id");
                }

                Assert.assertThat(record.getStartOffset(), is(equalTo(reader.getStartOffset())));
                Assert.assertThat(record.getStartOffset(), is(not(equalTo(reader.getOffset()))));

                warcEntry = new WarcEntry();
                warcEntry.recordId = record.warcRecordIdUri;
                warcEntry.offset = record.startOffset;
                warcEntries.add(warcEntry);

                record.close();

                Assert.assertThat(record.getStartOffset(), is(equalTo(reader.getStartOffset())));
                Assert.assertThat(record.getStartOffset(), is(not(equalTo(reader.getOffset()))));

                if ( bDigest ) {
                    if ( (record.payload != null && record.computedBlockDigest == null)
                            || (record.httpResponse != null && record.computedPayloadDigest == null) ) {
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

            reader.close();
            in.close();
        } catch (IOException e) {
            Assert.fail("Unexpected io exception");
        } catch (NoSuchAlgorithmException e) {
            Assert.fail("Unexpected algorithm exception");
        }

        Assert.assertEquals(expected_records, records);
        Assert.assertEquals(0, errors);
        Assert.assertEquals(0, warnings);

        return warcEntries;
    }

}
