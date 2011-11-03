package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
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
public class TestWarcNextRecord {

	private int expected_records;
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{822, "/home/nicl/Downloads/IAH-20080430204825-00000-blackbook.warc"},
				{120, "/home/nicl/workspace/netarchivesuite/bin/dk/netarkivet/archive/tools/data/originals/NAS-20100909163324-00000-mette.kb.dk.warc"},
				{120, "/home/nicl/workspace/netarchivesuite/bin/dk/netarkivet/common/distribute/arcrepository/data/originals/NAS-20100909163324-00000-mette.kb.dk.warc"},
				{68, "/home/nicl/workspace/netarchivesuite/bin/dk/netarkivet/common/utils/cdx/data/input/warcs/netarkivet-20081105135926-00000.warc"},
				{63, "/home/nicl/workspace/netarchivesuite/bin/dk/netarkivet/common/utils/cdx/data/input/warcs/netarkivet-20081105135926-00001.warc"},
				{4, "/home/nicl/workspace/netarchivesuite/bin/dk/netarkivet/common/utils/cdx/data/input/warcs/netarkivet-20081105140044-00002.warc"}
		});
	}

	public TestWarcNextRecord(int records, String warcFile) {
		this.expected_records = records;
		this.warcFile = warcFile;
	}

	@Test
	public void test() {
		boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

		File file = new File(warcFile);
		InputStream in;

		int records = 0;
		int errors = 0;

		try {
			in = new FileInputStream(file);
			in = new BufferedInputStream(in, 65536);

			WarcReader reader = WarcReaderFactory.getReader(in);
			WarcRecord record;

			while ((record = reader.nextRecord()) != null) {
				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

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
