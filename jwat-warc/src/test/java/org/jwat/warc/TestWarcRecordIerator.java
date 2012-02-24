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
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test to check that the nextRecord() and iterator() approach to reading all the
 * records in a file have the same number of records and no errors.
 * Also checks that GZip support is working correctly.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestWarcRecordIerator {

    private int expected_records;
    private boolean bDigest;
    private String warcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {822, false, "IAH-20080430204825-00000-blackbook.warc"},
                {822, true, "IAH-20080430204825-00000-blackbook.warc"},
                {822, false, "IAH-20080430204825-00000-blackbook.warc.gz"},
                {822, true, "IAH-20080430204825-00000-blackbook.warc.gz"}
        });
    }

    public TestWarcRecordIerator(int records, boolean bDigest, String warcFile) {
        this.expected_records = records;
        this.bDigest = bDigest;
        this.warcFile = warcFile;
    }

    @Test
    public void test_warcreader_nextrecord_and_iterator() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        WarcReader reader;
        Iterator<WarcRecord> recordIterator;
        WarcRecord record;

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

            in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            reader = WarcReaderFactory.getReader(in, 8192);

            reader.setBlockDigestEnabled( true );
            reader.setBlockDigestAlgorithm( "sha1" );
            reader.setPayloadDigestEnabled( true );
            reader.setPayloadDigestAlgorithm( "sha1" );

            while ((record = reader.getNextRecord()) != null) {
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

                ++n_records;

                if (record.diagnostics.hasErrors()) {
                    n_errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    n_warnings += record.diagnostics.getWarnings().size();
                }
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(n_records, n_errors, n_warnings);
            }

            /*
             * Iterator.
             */

            in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            reader = WarcReaderFactory.getReader(in, 8192);

            reader.setBlockDigestEnabled( true );
            reader.setBlockDigestAlgorithm( "sha1" );
            reader.setPayloadDigestEnabled( true );
            reader.setPayloadDigestAlgorithm( "sha1" );

            recordIterator = reader.iterator();
            while (recordIterator.hasNext()) {
                Assert.assertNull(reader.getIteratorExceptionThrown());
                record = recordIterator.next();
                Assert.assertNull(reader.getIteratorExceptionThrown());

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

                ++i_records;

                if (record.diagnostics.hasErrors()) {
                    i_errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    i_warnings += record.diagnostics.getWarnings().size();
                }
            }
            Assert.assertNull(reader.getIteratorExceptionThrown());

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(i_records, i_errors, i_warnings);
            }
        }
        catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        }
        catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }
        catch (NoSuchAlgorithmException e) {
            Assert.fail("Unexpected algorithm exception");
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
