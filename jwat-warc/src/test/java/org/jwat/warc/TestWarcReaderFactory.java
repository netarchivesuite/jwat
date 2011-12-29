package org.jwat.warc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

/**
 * Test to check if GZip auto detection is working correctly.
 * Also checks if buffered and unbuffered input streaming work as expected.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestWarcReaderFactory {

	private int expected_records;
	private boolean bDigest;
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{822, false, "IAH-20080430204825-00000-blackbook.warc"},
				{822, true, "IAH-20080430204825-00000-blackbook.warc"},
				{822, false, "IAH-20080430204825-00000-blackbook.warc.gz"},
				{822, true, "IAH-20080430204825-00000-blackbook.warc.gz"}
		});
	}

	public TestWarcReaderFactory(int records, boolean bDigest, String warcFile) {
		this.expected_records = records;
		this.bDigest = bDigest;
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

			if ( bDigest ) {
				reader.setBlockDigestEnabled( true );
				reader.setBlockDigestAlgorithm( "sha1" );
				reader.setPayloadDigestEnabled( true );
				reader.setPayloadDigestAlgorithm( "sha1" );
			}

			while ((record = reader.getNextRecord()) != null) {
				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

				record.close();

				if ( bDigest ) {
					if ( (record.payload != null && record.computedBlockDigest == null)
							|| (record.httpResponse != null && record.computedPayloadDigest == null) ) {
						Assert.fail( "Digest missing!" );
					}
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

	        Assert.assertEquals(expected_records, records);
	        Assert.assertEquals(0, errors);

	        /*
			 * Auto detect buffered.
			 */

	        records = 0;
	        errors = 0;

			in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			reader = WarcReaderFactory.getReader(in, 8192);

			reader.setBlockDigestEnabled( true );
			reader.setBlockDigestAlgorithm( "sha1" );
			reader.setPayloadDigestEnabled( true );
			reader.setPayloadDigestAlgorithm( "sha1" );

			while ((record = reader.getNextRecord()) != null) {
				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

				record.close();

				if ( bDigest ) {
					if ( (record.payload != null && record.computedBlockDigest == null)
							|| (record.httpResponse != null && record.computedPayloadDigest == null) ) {
						Assert.fail( "Digest missing!" );
					}
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

	        Assert.assertEquals(expected_records, records);
	        Assert.assertEquals(0, errors);
		}
		catch (FileNotFoundException e) {
			Assert.fail("Input file missing");
		}
		catch (IOException e) {
			Assert.fail("Unexpected io exception");
		}
		catch (NoSuchAlgorithmException e) {
			Assert.fail("Unexpected algorithm exception");
		}
	}

}
