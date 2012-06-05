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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.HeaderLine;

@RunWith(Parameterized.class)
public class TestNonWarcHeaders {

    private int expected_records;
    private int expected_errors;
    private int expected_warnings;
    private String warcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 0, 0, "test-non-warc-headers.warc"}
        });
    }

    public TestNonWarcHeaders(int records, int errors, int warnings, String warcFile) {
        this.expected_records = records;
        this.expected_errors = errors;
        this.expected_warnings = warnings;
        this.warcFile = warcFile;
    }

    @Test
    public void test_non_warc_headers() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            WarcReader reader = WarcReaderFactory.getReader(in);
            WarcRecord record;

            while ((record = reader.getNextRecord()) != null) {
                if (bDebugOutput) {
                    RecordDebugBase.printRecord(record);
                    RecordDebugBase.printRecordErrors(record);
                }

                record.close();

                Assert.assertNull(record.getHeader(null));
                Assert.assertNull(record.getHeader(""));

                HeaderLine header1 = record.getHeader("header1");
                HeaderLine header2 = record.getHeader("HEADER2");

                Assert.assertNotNull(header1);
                Assert.assertNotNull(header2);

                Assert.assertEquals("hello", header1.value);
                Assert.assertNotNull(header1.lines);
                Assert.assertEquals("domination", header1.lines.get(0).value);
                Assert.assertEquals("world", header2.value);
                Assert.assertEquals(0, header2.lines.size());

                List<HeaderLine> headers = record.getHeaderList();

                String[][] headerRef = {
                        {"WARC-Type", "warcinfo"},
                        {"Header1", "hello"},
                        {"WARC-Date", "2008-04-30T20:48:25Z"},
                        {"WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz"},
                        {"hEADER2", "world"},
                        {"WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-1>"},
                        {"header1", "domination"},
                        {"Content-Type", "application/warc-fields"},
                        {"Content-Length", "483"}
                };

                Assert.assertEquals(headers.size(), headerRef.length);

                for (int i=0; i<headerRef.length; ++i) {
                    Assert.assertEquals(headerRef[i][0], headers.get(i).name);
                    Assert.assertEquals(headerRef[i][1], headers.get(i).value);
                }

                if (record.diagnostics.hasErrors()) {
                    errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    warnings += record.diagnostics.getWarnings().size();
                }

                ++records;
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors, warnings);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }

        Assert.assertEquals(expected_records, records);
        Assert.assertEquals(expected_errors, errors);
        Assert.assertEquals(expected_warnings, warnings);
    }

}
