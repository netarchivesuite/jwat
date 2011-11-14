package dk.netarkivet.warclib;

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
			in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			WarcReader reader = WarcReaderFactory.getReader(in);
			WarcRecord record;

			while ((record = reader.getNextRecord()) != null) {
				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

				record.close();

				errors = 0;
				if (record.hasErrors()) {
					errors = record.getValidationErrors().size();
				}

				if (record.warcFilename != null) {
					saveUtf8(record.warcFilename);
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

}
