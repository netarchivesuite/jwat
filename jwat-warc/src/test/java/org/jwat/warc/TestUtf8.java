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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.Payload;

@RunWith(Parameterized.class)
public class TestUtf8 {

    private int expected_records;
    private int expected_errors;
    private int expected_warnings;
    private String warcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 0, 0, "test-utf8.warc"}
        });
    }

    public TestUtf8(int records, int errors, int warnings, String warcFile) {
        this.expected_records = records;
        this.expected_errors = errors;
        this.expected_warnings = warnings;
        this.warcFile = warcFile;
    }

    @Test
    public void test_utf8() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;
        int warnings = 0;

        try {
            if (bDebugOutput) {
                in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

                byte[] bytes = new byte[8192];
                int read;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                in.close();

                bytes = out.toByteArray();
                System.out.println("InputStream length: " + bytes.length);

                RandomAccessFile ram = new RandomAccessFile("r-u-kidding-me.txt", "rw");
                ram.seek(0);
                ram.setLength(0);
                ram.write(bytes);
                ram.close();
            }

            in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            WarcReader reader = WarcReaderFactory.getReader(in);
            WarcRecord record;

            while ((record = reader.getNextRecord()) != null) {
                if (bDebugOutput) {
                    if (record.warcFilename != null) {
                        saveUtf8(record.warcFilename);
                    }

                    Payload payload = record.getPayload();
                    if (payload != null) {
                        InputStream pis = payload.getInputStream();
                        long l = payload.getTotalLength();
                        System.out.println("Payload length: " + l);
                        savePayload(pis);
                    }
                }

                record.close();

                if (bDebugOutput) {
                    RecordDebugBase.printRecord(record);
                    RecordDebugBase.printRecordErrors(record);
                }

                errors = 0;
                warnings = 0;
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

    public static void saveUtf8(String str) {
        RandomAccessFile ram = null;
        try {
            ram = new RandomAccessFile("utf8.txt", "rw");
            ram.write(str.getBytes("UTF-8"));
            ram.close();
            ram = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (ram != null) {
                try {
                    ram.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void savePayload(InputStream is) {
        String filename = "temp-payload.txt";
        try {
            byte[] bytes = new byte[ 8192 ];
            int read;
            File file = new File( filename );
            System.out.println( "            > " + file.getPath() );
            RandomAccessFile ram = new RandomAccessFile( file, "rw" );
            ram.setLength( 0 );
            ram.seek( 0 );
            while ( (read = is.read( bytes )) != -1 ) {
                ram.write( bytes, 0,  read );
            }
            ram.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
