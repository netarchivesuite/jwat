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
    private String warcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 0, "test-utf8.warc"}
        });
    }

    public TestUtf8(int records, int errors, String warcFile) {
        this.expected_records = records;
        this.expected_errors = errors;
        this.warcFile = warcFile;
    }

    @Test
    public void test() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;

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
                if (record.hasErrors()) {
                    errors = record.getValidationErrors().size();
                }

                ++records;
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors);
            }
        }
        catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        }
        catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }

        Assert.assertEquals(expected_records, records);
        Assert.assertEquals(expected_errors, errors);
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
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
