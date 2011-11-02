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
public class TestDuplicateFields {

	private int expected_records;
	private int expected_duplicates;
	private int expected_concurrenttos;
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 6, 0, "test-duplicate-fields.warc"},
				{1, 0, 3, "test-duplicate-concurrentto.warc"}
		});
	}

	public TestDuplicateFields(int records, int duplicates, int concurrenttos, String warcFile) {
		this.expected_records = records;
		this.expected_duplicates = duplicates;
		this.expected_concurrenttos = concurrenttos;
		this.warcFile = warcFile;
	}

	@Test
	public void test() {
		InputStream in;

		int records = 0;
		int errors = 0;
		int duplicates = 0;

		try {
			in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			WarcParser parser = new WarcParser(in);
			WarcRecord record;

			while ((record = parser.nextRecord()) != null) {
				TestWarc.printRecord(record);
				TestWarc.printRecordErrors(record);

				++records;

				if (record.hasErrors()) {
					errors += record.getValidationErrors().size();
				}

				// Check number of concurrentto fields.
				if (expected_concurrenttos == 0) {
					if (record.warcConcurrentToUriList != null) {
						Assert.fail("Not expecting any concurrent-to fields");
					}
				}
				else {
					if (record.warcConcurrentToUriList == null) {
						Assert.fail("Expecting concurrent-to fields");
					}
					else {
						Assert.assertEquals(record.warcConcurrentToUriList.size(), 3);
					}
				}
			}

			System.out.println("--------------");
			System.out.println("       Records: " + records);
			System.out.println("        Errors: " + errors);
			parser.close();
			in.close();
		}
		catch (FileNotFoundException e) {
			Assert.fail("Input file missing");
		}
		catch (IOException e) {
			Assert.fail("Unexpected io exception");
		}

		Assert.assertEquals(expected_records, records);
		Assert.assertEquals(expected_duplicates, errors);
	}

}
