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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.Payload;
import org.jwat.common.RandomAccessFileInputStream;
import org.jwat.common.RandomAccessFileOutputStream;

@RunWith(Parameterized.class)
public class TestWarcWriterCopy {

    private int expected_records;
    private String warcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {822, "IAH-20080430204825-00000-blackbook.warc"}
        });
    }

    public TestWarcWriterCopy(int records, String warcFile) {
        this.expected_records = records;
        this.warcFile = warcFile;
    }

    @Test
    public void test_warcwriter_cloning() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;
        RandomAccessFile raf = null;

        WarcReader reader;
        WarcRecord record;

        int records;
        int errors;
        int warnings;

        try {
            /*
             * Clone warc.
             */

            in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            File out_file = File.createTempFile("jwat-warcwritetest-", ".warc");

            raf = new RandomAccessFile(out_file, "rw");
            raf.seek(0);
            raf.setLength(0);
            //OutputStream out = new FileOutputStream( "WarcWriteTest.warc" );
            OutputStream out = new RandomAccessFileOutputStream( raf );

            reader = WarcReaderFactory.getReader( in );

            WarcWriter writer = new WarcWriterUncompressed( out );

            records = 0;
            errors = 0;
            warnings = 0;

            while ( (record = reader.getNextRecord()) != null ) {
                if (bDebugOutput) {
                    RecordDebugBase.printRecord(record);
                    RecordDebugBase.printRecordErrors(record);
                }

                ++records;

                if (record.diagnostics.hasErrors()) {
                    errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    warnings += record.diagnostics.getWarnings().size();
                }

                writer.write(record);

                if ( record.hasPayload() ) {
                    Payload payload = record.getPayload();
                    //writer.transfer( payload.getInputStream(), payload.getTotalLength() );
                    writer.transfer( payload.getInputStreamComplete(), payload.getTotalLength() );
                }

                writer.closeRecord();
            }

            if (bDebugOutput) {
                System.out.println("--------------");
                System.out.println("       Records: " + records);
                System.out.println("        Errors: " + errors);
            }

            reader.close();
            in.close();
            writer.close();
            out.close();

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);

            /*
             * Validate written warc.
             */

            raf.seek(0);
            //in = new FileInputStream("WarcWriteTest.warc");
            in = new RandomAccessFileInputStream( raf );

            reader = WarcReaderFactory.getReader( in );

            records = 0;
            errors = 0;
            warnings = 0;

            while ( (record = reader.getNextRecord()) != null ) {
                if (bDebugOutput) {
                    RecordDebugBase.printRecord(record);
                    RecordDebugBase.printRecordErrors(record);
                }

                ++records;

                if (record.diagnostics.hasErrors()) {
                    errors += record.diagnostics.getErrors().size();
                }
                if (record.diagnostics.hasWarnings()) {
                    warnings += record.diagnostics.getWarnings().size();
                }
            }

            if (bDebugOutput) {
                System.out.println("--------------");
                System.out.println("       Records: " + records);
                System.out.println("        Errors: " + errors);
            }

            reader.close();
            in.close();
            writer.close();
            out.close();

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
            Assert.assertEquals(0, warnings);
        } catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        } catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }
        finally {
            if ( raf != null ) {
                try {
                    raf.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Assert.fail( "Exception not expected!" );
                }
            }
        }

    }

}
