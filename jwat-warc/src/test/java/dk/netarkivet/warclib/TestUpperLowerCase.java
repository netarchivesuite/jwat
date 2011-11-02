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

@RunWith(Parameterized.class)
public class TestUpperLowerCase {

	private int expected_records;
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{5, "test-upper-lower-case.warc"}
		});
	}

	public TestUpperLowerCase(int records, String warcFile) {
		this.expected_records = records;
		this.warcFile = warcFile;
	}

	@Test
	public void test() {
		boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

		InputStream in;

		int records = 0;
		int errors = 0;

		try {
			in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			WarcParser parser = new WarcParser(in);
			WarcRecord record;

			while ((record = parser.nextRecord()) != null) {
				if (bDebugOutput) {
					PrintRecord.printRecord(record);
					PrintRecord.printRecordErrors(record);
				}

				++records;

				if (record.hasErrors()) {
					errors += record.getValidationErrors().size();
				}
			}

			parser.close();
			in.close();

			if (bDebugOutput) {
				PrintRecord.printStatus(records, errors);
			}
		}
		catch (FileNotFoundException e) {
			Assert.fail("Input file missing");
		}
		catch (IOException e) {
			Assert.fail("Unexpected io exception");
		}

		Assert.assertEquals(expected_records, records);
		Assert.assertEquals(0, errors);
	}

}
