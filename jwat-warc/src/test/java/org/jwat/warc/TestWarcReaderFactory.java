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
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.HttpHeader;

/**
 * Test to check if GZip auto detection is working correctly.
 * Also checks if buffered and unbuffered input streaming work as expected.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestWarcReaderFactory {

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

    public TestWarcReaderFactory(int records, boolean bDigest, String warcFile) {
        this.expected_records = records;
        this.bDigest = bDigest;
        this.warcFile = warcFile;
    }

    @Test
    public void test_warcreaderfactory_autodetect() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        WarcReader reader;
        WarcRecord record;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        WarcReaderFactory factory = new WarcReaderFactory();
        Assert.assertNotNull(factory);

        try {
            /*
             * Auto detect unbuffered.
             */

            records = 0;
            errors = 0;
            warnings = 0;

            in = TestHelpers.getTestResourceAsStream(warcFile);

            reader = WarcReaderFactory.getReader(in);

            if ( bDigest ) {
                reader.setBlockDigestEnabled( true );
                Assert.assertTrue(reader.setBlockDigestAlgorithm( "sha1" ));
                reader.setPayloadDigestEnabled( true );
                Assert.assertTrue(reader.setPayloadDigestAlgorithm( "sha1" ));
            }

            while ((record = reader.getNextRecord()) != null) {
                if (bDebugOutput) {
                    TestBaseUtils.printRecord(record);
                    TestBaseUtils.printRecordErrors(record);
                }

                record.close();

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
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);

            /*
             * Auto detect buffered.
             */

            records = 0;
            errors = 0;
            warnings = 0;

            in = TestHelpers.getTestResourceAsStream(warcFile);

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

                record.close();

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
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected I/O exception");
        }
    }

}
