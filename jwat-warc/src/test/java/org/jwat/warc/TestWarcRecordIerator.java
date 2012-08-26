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

//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.Matchers.lessThanOrEqualTo;
//import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.core.Is.is;

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
import org.jwat.common.HttpHeader;

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

        int recordNumber = 0;

        long[] offsets = new long[expected_records + 1];
        long offset1;
        long offset2;

        try {
            /*
             * getNextArcRecord.
             */

            in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            reader = WarcReaderFactory.getReader(in, 8192);

            reader.setBlockDigestEnabled( true );
            Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
            reader.setPayloadDigestEnabled( true );
            Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));

            while ((record = reader.getNextRecord()) != null) {
                if (bDebugOutput) {
                    TestBaseUtils.printRecord(record);
                    TestBaseUtils.printRecordErrors(record);
                }

                ++recordNumber;
                Assert.assertEquals(offsets[recordNumber - 1], record.getStartOffset());
                Assert.assertEquals(offsets[recordNumber - 1], record.header.getStartOffset());
                Assert.assertEquals(offsets[recordNumber - 1], reader.getStartOffset());
                offset1 = reader.getOffset();

                record.close();

                Assert.assertEquals(offsets[recordNumber - 1], record.getStartOffset());
                Assert.assertEquals(offsets[recordNumber - 1], reader.getStartOffset());
                Assert.assertEquals(offsets[recordNumber - 1], record.header.getStartOffset());
                offset2 = reader.getOffset();

                offsets[recordNumber] = offset2;

                Assert.assertThat(offset1, is(greaterThan(offsets[recordNumber - 1])));
                Assert.assertThat(offset2, is(lessThanOrEqualTo(offsets[recordNumber])));

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

            Assert.assertEquals(offsets[recordNumber - 1], reader.getStartOffset());
            Assert.assertEquals(offsets[recordNumber], reader.getOffset());
            Assert.assertEquals(offsets[recordNumber], reader.getConsumed());

            if (bDebugOutput) {
                TestBaseUtils.printStatus(n_records, n_errors, n_warnings);
            }

            /*
             * Iterator.
             */

            in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            reader = WarcReaderFactory.getReader(in, 8192);

            reader.setBlockDigestEnabled( true );
            Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
            reader.setPayloadDigestEnabled( true );
            Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));

            recordNumber = 0;

            recordIterator = reader.iterator();
            while (recordIterator.hasNext()) {
                Assert.assertTrue(recordIterator.hasNext());

                Assert.assertNull(reader.getIteratorExceptionThrown());
                record = recordIterator.next();
                Assert.assertNull(reader.getIteratorExceptionThrown());

                if (bDebugOutput) {
                    TestBaseUtils.printRecord(record);
                    TestBaseUtils.printRecordErrors(record);
                }

                ++recordNumber;
                Assert.assertEquals(offsets[recordNumber - 1], record.getStartOffset());
                Assert.assertEquals(offsets[recordNumber - 1], record.header.getStartOffset());
                Assert.assertEquals(offsets[recordNumber - 1], reader.getStartOffset());
                offset1 = reader.getOffset();

                record.close();

                Assert.assertEquals(offsets[recordNumber - 1], record.getStartOffset());
                Assert.assertEquals(offsets[recordNumber - 1], reader.getStartOffset());
                Assert.assertEquals(offsets[recordNumber - 1], record.header.getStartOffset());
                offset2 = reader.getOffset();

                Assert.assertThat(offset1, is(greaterThan(offsets[recordNumber - 1])));
                Assert.assertThat(offset2, is(lessThanOrEqualTo(offsets[recordNumber])));

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

                ++i_records;

                if (record.diagnostics.hasErrors()) {
                    i_errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    i_warnings += record.diagnostics.getWarnings().size();
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

            Assert.assertEquals(offsets[recordNumber - 1], reader.getStartOffset());
            Assert.assertEquals(offsets[recordNumber], reader.getOffset());
            Assert.assertEquals(offsets[recordNumber], reader.getConsumed());

            if (bDebugOutput) {
                TestBaseUtils.printStatus(i_records, i_errors, i_warnings);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected io exception");
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
