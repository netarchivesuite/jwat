package dk.netarkivet.warclib;

import java.io.File;
import java.io.FileInputStream;
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
public class TestContentTypeRecommended {

	private int expected_records;
	private int expected_recommended;
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 1, "src/test/resources/test-contenttype-warcinfo-recommended.warc"},
				{7, 1, "src/test/resources/test-contenttype-recommended.warc"},
				{1, 0, "src/test/resources/test-contenttype-continuation.warc"}
		});
	}

	public TestContentTypeRecommended(int records, int recommended, String warcFile) {
		this.expected_records = records;
		this.expected_recommended = recommended;
		this.warcFile = warcFile;
	}

	@Test
	public void test() {
		File file = new File( warcFile );
		InputStream in;

		int records = 0;
		int errors = 0;

		try {
			in = new FileInputStream( file );

			WarcParser parser = new WarcParser( in );
			WarcRecord record;

			while ( (record = parser.nextRecord()) != null ) {
				TestWarc.printRecord(record);
				TestWarc.printRecordErrors(record);

				++records;

				errors = 0;
				if (record.hasErrors()) {
					errors = record.getValidationErrors().size();
				}

				Assert.assertEquals(expected_recommended, errors);
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
	}

}
