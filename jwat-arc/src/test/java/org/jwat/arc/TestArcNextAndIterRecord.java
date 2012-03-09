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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecord;
import org.jwat.arc.ArcVersionBlock;

@RunWith(Parameterized.class)
public class TestArcNextAndIterRecord {

    private int expected_records;
    private String arcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {101, "1-1-20110922131213-00000-svc-VirtualBox.arc"},
                {238, "4-3-20111004123336-00000-svc-VirtualBox.arc"},
                {299, "IAH-20080430204825-00000-blackbook.arc"},
                {5, "small_BNF.arc"}
        });
    }

    public TestArcNextAndIterRecord(int records, String arcFile) {
        this.expected_records = records;
        this.arcFile = arcFile;
    }

    @Test
    public void test_arcreader_nextrecord_and_iterator() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        ArcReader reader;
        ArcVersionBlock version;
        Iterator<ArcRecord> recordIterator;
        ArcRecord arcRecord;

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
            version = reader.getVersionBlock();

            if (version != null) {
                if (bDebugOutput) {
                    RecordDebugBase.printVersionBlock(version);
                }

                boolean b = true;
                while ( b ) {
                    arcRecord = reader.getNextRecord();
                    if (arcRecord != null) {
                        if (bDebugOutput) {
                            RecordDebugBase.printRecord(arcRecord);
                        }

                        ++n_records;

                        if (arcRecord.diagnostics.hasErrors()) {
                            n_errors += arcRecord.diagnostics.getErrors().size();
                        }
                        if (arcRecord.diagnostics.hasWarnings()) {
                            n_warnings += arcRecord.diagnostics.getWarnings().size();
                        }
                    }
                    else {
                        b = false;
                    }
                }

                if (bDebugOutput) {
                    RecordDebugBase.printStatus(n_records, n_errors, n_warnings);
                }
            }

            reader.close();
            in.close();

            /*
             * Iterator.
             */

            in = this.getClass().getClassLoader().getResourceAsStream(arcFile);

            reader = ArcReaderFactory.getReader(in);
            version = reader.getVersionBlock();

            if (version != null) {
                if (bDebugOutput) {
                    RecordDebugBase.printVersionBlock(version);
                }

                recordIterator = reader.iterator();

                while (recordIterator.hasNext()) {
                    Assert.assertNull(reader.getIteratorExceptionThrown());
                    arcRecord = recordIterator.next();
                    Assert.assertNull(reader.getIteratorExceptionThrown());
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(arcRecord);
                    }

                    ++i_records;

                    if (arcRecord.diagnostics.hasErrors()) {
                        i_errors += arcRecord.diagnostics.getErrors().size();
                    }
                    if (arcRecord.diagnostics.hasWarnings()) {
                        i_warnings += arcRecord.diagnostics.getWarnings().size();
                    }
                }
                Assert.assertNull(reader.getIteratorExceptionThrown());

                if (bDebugOutput) {
                    RecordDebugBase.printStatus(i_records, i_errors, i_warnings);
                }
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
