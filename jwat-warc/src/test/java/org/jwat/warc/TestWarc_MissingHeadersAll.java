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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.Diagnosis;

@RunWith(Parameterized.class)
public class TestWarc_MissingHeadersAll {

    private int expected_records;
    private String warcFile;
    private Set<String> errorsFieldNamesSet;
    private Set<String> warningsFieldNamesSet;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {2, "invalid-warcfile-lonely-warcinfo-metadata.warc", "WARC-Record-ID,WARC-Date,Content-Length", ""},
                {4, "invalid-warcfile-lonely-request-response-resource-conversion.warc", "WARC-Record-ID,WARC-Date,Content-Length,WARC-Target-URI", ""},
                {1, "invalid-warcfile-lonely-continuation.warc", "WARC-Record-ID,WARC-Date,Content-Length,WARC-Target-URI,WARC-Segment-Number,WARC-Segment-Origin-ID", ""},
                {1, "invalid-warcfile-lonely-revisit.warc", "WARC-Record-ID,WARC-Date,Content-Length,WARC-Target-URI,WARC-Profile", ""},
                {1, "invalid-warcfile-lonely-monkeys.warc", "WARC-Type,WARC-Record-ID,WARC-Date,Content-Length", "WARC-Type"}
        });
    }

    public TestWarc_MissingHeadersAll(int records, String warcFile, String errorFieldNames, String warningFieldNames) {
        this.expected_records = records;
        this.warcFile = warcFile;
        this.errorsFieldNamesSet = new HashSet<String>(Arrays.asList((errorFieldNames.split(",", -1))));
        this.warningsFieldNamesSet = new HashSet<String>(Arrays.asList((warningFieldNames.split(",", -1))));
    }

    @Test
    public void test_missing_headers() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            in = TestHelpers.getTestResourceAsStream(warcFile);

            WarcReader reader = WarcReaderFactory.getReader(in);
            WarcRecord record;

            while ((record = reader.getNextRecord()) != null) {
                if (bDebugOutput) {
                    TestBaseUtils.printRecord(record);
                    TestBaseUtils.printRecordErrors(record);
                }

                record.close();

                ++records;

                if (record.diagnostics.hasErrors()) {
                    errors += record.diagnostics.getErrors().size();

                    Assert.assertTrue(errorsFieldNamesSet.containsAll(
                            filter(record.diagnostics.getErrors())
                            ));
                }
                else {
                    Assert.fail("There must be errors.");
                }
                if (record.diagnostics.hasWarnings()) {
                    warnings += record.diagnostics.getWarnings().size();

                    Assert.assertTrue(warningsFieldNamesSet.containsAll(
                            filter(record.diagnostics.getWarnings())
                            ));
                }
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                TestBaseUtils.printStatus(records, errors, warnings);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected I/O exception");
        }

        Assert.assertEquals(expected_records, records);
        //Assert.assertEquals(0, errors);
        //Assert.assertEquals(0, warnings);
    }

    public List<String> filter(List<Diagnosis> errors) {
        List<String> fields = new ArrayList<String>();
        for (Diagnosis error : errors) {
            int idx = error.entity.indexOf('\'');
            if (idx != 0) {
                idx = error.entity.indexOf('\'', 1);
                if (idx != -1) {
                    fields.add(error.entity.substring(1, idx));
                }
            }
        }
        return fields;
    }

}
