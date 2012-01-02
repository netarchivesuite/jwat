package org.jwat.arc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
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
public class TestArcReaderFactoryUncompressed {

    private int expected_records;
    private String arcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {299, "IAH-20080430204825-00000-blackbook.arc"}
        });
    }

    public TestArcReaderFactoryUncompressed(int records, String arcFile) {
        this.expected_records = records;
        this.arcFile = arcFile;
    }

    @Test
    public void test() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        URL url;
        File file;
        RandomAccessFile ram;
        InputStream in;

        ArcReader reader;
        ArcVersionBlock version;
        ArcRecord record;

        int records = 0;
        int errors = 0;

        try {
            List<ArcEntry> entries = indexArcFile();
            ArcEntry entry;

            /*
             * getReaderUncompressed() / nextRecordFrom(in).
             */

            records = 0;
            errors = 0;

            url = this.getClass().getClassLoader().getResource(arcFile);
            file = new File(url.getFile());
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderUncompressed();
            version = reader.getVersionBlock(in);

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, entry.offset)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    ++records;

                    if (record.hasErrors()) {
                        errors += record.getValidationErrors().size();
                    }

                    if (record.url.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                }
                else {
                    Assert.fail("Location incorrect");
                }
            }

            reader.close();
            in.close();
            ram.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);

            /*
             * getReaderUncompressed(in) / nextRecordFrom(in, buffer_size).
             */

            records = 0;
            errors = 0;

            url = this.getClass().getClassLoader().getResource(arcFile);
            file = new File(url.getFile());
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderUncompressed(in);
            version = reader.getVersionBlock();

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, 8192, entry.offset)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    ++records;

                    if (record.hasErrors()) {
                        errors += record.getValidationErrors().size();
                    }

                    if (record.url.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                }
                else {
                    Assert.fail("Location incorrect");
                }
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);

            /*
             * getReaderUncompressed(in, buffer_size) / nextRecordFrom(in).
             */

            records = 0;
            errors = 0;

            url = this.getClass().getClassLoader().getResource(arcFile);
            file = new File(url.getFile());
            ram = new RandomAccessFile(file, "r");
            in = new RandomAccessFileInputStream(ram);

            reader = ArcReaderFactory.getReaderUncompressed(in, 8192);
            version = reader.getVersionBlock();

            for (int i=0; i<entries.size(); ++i) {
                entry = entries.get(i);

                ram.seek(entry.offset);

                if ((record = reader.getNextRecordFrom(in, entry.offset)) != null) {
                    if (bDebugOutput) {
                        RecordDebugBase.printRecord(record);
                        //RecordDebugBase.printRecordErrors(record);
                    }

                    record.close();

                    ++records;

                    if (record.hasErrors()) {
                        errors += record.getValidationErrors().size();
                    }

                    if (record.url.compareTo(entry.recordId) != 0) {
                        Assert.fail("Wrong record");
                    }
                }
                else {
                    Assert.fail("Location incorrect");
                }
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors);
            }

            Assert.assertEquals(expected_records, records);
            Assert.assertEquals(0, errors);
        }
        catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }
    }

    class ArcEntry {
        URI recordId;
        long offset;
    }

    public List<ArcEntry> indexArcFile() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        List<ArcEntry> arcEntries = new ArrayList<ArcEntry>();
        ArcEntry arcEntry;

        int records = 0;
        int errors = 0;

        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(arcFile);

            ArcReader reader = ArcReaderFactory.getReader(in);
            ArcVersionBlock version = reader.getVersionBlock();
            ArcRecord record;

            Iterator<ArcRecord> recordIterator = reader.iterator();

            while (recordIterator.hasNext()) {
                record = recordIterator.next();
                ++records;

                if (bDebugOutput) {
                    System.out.println("0x" + Long.toString(record.getStartOffset(), 16) + "(" + record.getStartOffset() + ")");
                    System.out.println( record.recUrl );
                    System.out.println( record.recIpAddress );
                    System.out.println( record.recArchiveDate );
                    System.out.println( record.recContentType );
                    System.out.println( record.recResultCode );
                    System.out.println( record.recChecksum );
                    System.out.println( record.recLocation );
                    System.out.println( record.recOffset );
                    System.out.println( record.recFilename );
                    System.out.println( record.recLength );
                }

                if (record.url == null) {
                    Assert.fail("Invalid arc uri");
                }

                arcEntry = new ArcEntry();
                arcEntry.recordId = record.url;
                arcEntry.offset = record.getStartOffset();
                arcEntries.add(arcEntry);

                if (bDebugOutput) {
                    System.out.println("0x" + Long.toString(arcEntry.offset, 16) + "(" + arcEntry.offset + ") - " + arcEntry.recordId);
                }

                record.close();

                if (record.hasErrors()) {
                    errors += record.getValidationErrors().size();
                }
            }

            reader.close();
            in.close();
        }
        catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }

        Assert.assertEquals(expected_records, records);
        Assert.assertEquals(0, errors);

        return arcEntries;
    }

}
