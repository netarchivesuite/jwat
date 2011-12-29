package org.jwat.warc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
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

	public TestWarcRecordIerator(int records, boolean bDigest, String warcFile) {
		this.expected_records = records;
		this.bDigest = bDigest;
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

			reader.setBlockDigestEnabled( true );
			reader.setBlockDigestAlgorithm( "sha1" );
			reader.setPayloadDigestEnabled( true );
			reader.setPayloadDigestAlgorithm( "sha1" );

			recordIterator = reader.iterator();
			while (recordIterator.hasNext()) {
            	Assert.assertNull(reader.getIteratorExceptionThrown());
				record = recordIterator.next();
            	Assert.assertNull(reader.getIteratorExceptionThrown());

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

				++i_records;

				if (record.hasErrors()) {
					i_errors += record.getValidationErrors().size();
				}
			}
        	Assert.assertNull(reader.getIteratorExceptionThrown());

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
		catch (NoSuchAlgorithmException e) {
			Assert.fail("Unexpected algorithm exception");
		}

        Assert.assertEquals(n_records, i_records);
        Assert.assertEquals(n_errors, i_errors);

        Assert.assertEquals(expected_records, n_records);
        Assert.assertEquals(expected_records, i_records);

        Assert.assertEquals(0, n_errors);
        Assert.assertEquals(0, i_errors);
	}

}
