package dk.netarkivet.warclib;

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

/**
 * Test to check if GZip auto detection is working correctly.
 * Also checks if buffered and unbuffered input streaming work as expected.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestWarcReaderFactory {

	private int expected_records;
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{822, "IAH-20080430204825-00000-blackbook.warc"},
				{822, "IAH-20080430204825-00000-blackbook.warc.gz"}
		});
	}

	public TestWarcReaderFactory(int records, String warcFile) {
		this.expected_records = records;
		this.warcFile = warcFile;
	}

	@Test
	public void test() {
		boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

		InputStream in;

		WarcReader reader;
		WarcRecord record;

        int records = 0;
        int errors = 0;

		try {
			/*
			 * Auto detect unbuffered.
			 */

	        records = 0;
	        errors = 0;

	        in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			reader = WarcReaderFactory.getReader(in);

			while ((record = reader.nextRecord()) != null) {
				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

				record.close();

				++records;

				if (record.hasErrors()) {
					errors += record.getValidationErrors().size();
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
			 * Auto detect buffered.
			 */

	        records = 0;
	        errors = 0;

			in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			reader = WarcReaderFactory.getReader(in, 8192);

			while ((record = reader.nextRecord()) != null) {
				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

				record.close();

				++records;

				if (record.hasErrors()) {
					errors += record.getValidationErrors().size();
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
		catch (FileNotFoundException e) {
			Assert.fail("Input file missing");
		}
		catch (IOException e) {
			Assert.fail("Unexpected io exception");
		}
	}

}
