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
    public void test() {
		boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

		InputStream in;

        ArcReader reader;
        ArcVersionBlock version;
        Iterator<ArcRecord> recordIterator;
        ArcRecord arcRecord;

        int n_records = 0;
        int n_errors = 0;

        int i_records = 0;
        int i_errors = 0;

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

                        if (arcRecord.hasErrors()) {
                            n_errors += arcRecord.getWarnings().size();
                        }
                    }
                    else {
                        b = false;
                    }
                }

            	if (bDebugOutput) {
                    RecordDebugBase.printStatus(n_records, n_errors);
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
                    arcRecord = recordIterator.next();
                	if (bDebugOutput) {
                    	RecordDebugBase.printRecord(arcRecord);
                	}

                    ++i_records;

                    if (arcRecord.hasErrors()) {
                        i_errors += arcRecord.getWarnings().size();
                    }
                }

            	if (bDebugOutput) {
                    RecordDebugBase.printStatus(i_records, i_errors);
            	}
            }

            reader.close();
            in.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(n_records, i_records);
        Assert.assertEquals(n_errors, i_errors);

        Assert.assertEquals(expected_records, n_records);
        Assert.assertEquals(expected_records, i_records);

        Assert.assertEquals(0, n_errors);
        Assert.assertEquals(0, i_errors);
    }

}
