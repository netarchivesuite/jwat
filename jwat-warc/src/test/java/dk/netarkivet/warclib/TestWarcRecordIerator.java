package dk.netarkivet.warclib;

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
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{822, "IAH-20080430204825-00000-blackbook.warc"},
				{822, "IAH-20080430204825-00000-blackbook.warc.gz"}
		});
	}

	public TestWarcRecordIerator(int records, String warcFile) {
		this.expected_records = records;
		this.warcFile = warcFile;
	}

	@Test
	public void test() {
		boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

		InputStream in;

		WarcReader reader;
		Iterator<WarcRecord> recordIterator;
		WarcRecord record;

		int n_records = 0;
        int n_errors = 0;

        int i_records = 0;
        int i_errors = 0;

		try {
            /*
             * getNextArcRecord.
             */

			in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			reader = WarcReaderFactory.getReader(in, 8192);

			while ((record = reader.nextRecord()) != null) {
				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

				++n_records;

				if (record.hasErrors()) {
					n_errors += record.getValidationErrors().size();
				}
			}

			reader.close();
			in.close();

			if (bDebugOutput) {
				RecordDebugBase.printStatus(n_records, n_errors);
			}

			/*
             * Iterator.
             */

			in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			reader = WarcReaderFactory.getReader(in, 8192);

			recordIterator = reader.iterator();
			while (recordIterator.hasNext()) {
				record = recordIterator.next();

				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

				++i_records;

				if (record.hasErrors()) {
					i_errors += record.getValidationErrors().size();
				}
			}

			reader.close();
			in.close();

			if (bDebugOutput) {
				RecordDebugBase.printStatus(i_records, i_errors);
			}
		}
		catch (FileNotFoundException e) {
			Assert.fail("Input file missing");
		}
		catch (IOException e) {
			Assert.fail("Unexpected io exception");
		}

        Assert.assertEquals(n_records, i_records);
        Assert.assertEquals(n_errors, i_errors);

        Assert.assertEquals(expected_records, n_records);
        Assert.assertEquals(expected_records, i_records);

        Assert.assertEquals(0, n_errors);
        Assert.assertEquals(0, i_errors);
	}

}
