package org.jwat.warc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.RandomAccessFileInputStream;

/**
 * Test to check wether the getReaderUncompressed() and nextRecordFrom(in)
 * combination of methods work for random access to WARC records.
 * The WARC test file is first indexed and then all records are pseudo randomly
 * checked in sequential order and the record-id is compared to check if the
 * location points to a record and if it's the correct one according to the
 * index.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestWarcReaderFactoryUncompressed {

	private int expected_records;
	private boolean bDigest;
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{822, false, "IAH-20080430204825-00000-blackbook.warc"},
				{822, true, "IAH-20080430204825-00000-blackbook.warc"}
		});
	}

	public TestWarcReaderFactoryUncompressed(int records, boolean bDigest, String warcFile) {
		this.expected_records = records;
		this.bDigest = bDigest;
		this.warcFile = warcFile;
	}

	@Test
	public void test() {
		boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        URL url;
        File file;
        RandomAccessFile ram;
		InputStream in;

		WarcReader reader;
		WarcRecord record;

        int records = 0;
        int errors = 0;

		try {
			List<WarcEntry> entries = indexWarcFile();
			WarcEntry entry;

			/*
			 * getReaderUncompressed() / nextRecordFrom(in).
			 */

	        records = 0;
	        errors = 0;

	        url = this.getClass().getClassLoader().getResource(warcFile);
	        file = new File(url.getFile());
	        ram = new RandomAccessFile(file, "r");
	        in = new RandomAccessFileInputStream(ram);

			reader = WarcReaderFactory.getReaderUncompressed();

			reader.setBlockDigestEnabled( true );
			reader.setBlockDigestAlgorithm( "sha1" );
			reader.setPayloadDigestEnabled( true );
			reader.setPayloadDigestAlgorithm( "sha1" );

			for (int i=0; i<entries.size(); ++i) {
				entry = entries.get(i);

				ram.seek(entry.offset);

				if ((record = reader.getNextRecordFrom(in)) != null) {
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

					if (record.warcRecordIdUri.compareTo(entry.recordId) != 0) {
						Assert.fail("Wrong record");
					}
				}
				else {
					Assert.fail("Location incorrect");
				}
			}

			reader.close();
			in.close();
			ram.close();

			if (bDebugOutput) {
				RecordDebugBase.printStatus(records, errors);
			}

	        Assert.assertEquals(expected_records, records);
	        Assert.assertEquals(0, errors);

			/*
			 * getReaderUncompressed(in) / nextRecordFrom(in, buffer_size).
			 */

	        records = 0;
	        errors = 0;

	        url = this.getClass().getClassLoader().getResource(warcFile);
	        file = new File(url.getFile());
	        ram = new RandomAccessFile(file, "r");
	        in = new RandomAccessFileInputStream(ram);

			reader = WarcReaderFactory.getReaderUncompressed(in);

			reader.setBlockDigestEnabled( true );
			reader.setBlockDigestAlgorithm( "sha1" );
			reader.setPayloadDigestEnabled( true );
			reader.setPayloadDigestAlgorithm( "sha1" );

			for (int i=0; i<entries.size(); ++i) {
				entry = entries.get(i);

				ram.seek(entry.offset);

				if ((record = reader.getNextRecordFrom(in, 8192)) != null) {
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

					if (record.warcRecordIdUri.compareTo(entry.recordId) != 0) {
						Assert.fail("Wrong record");
					}
				}
				else {
					Assert.fail("Location incorrect");
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
			 * getReaderUncompressed(in, buffer_size) / nextRecordFrom(in).
			 */

	        records = 0;
	        errors = 0;

	        url = this.getClass().getClassLoader().getResource(warcFile);
	        file = new File(url.getFile());
	        ram = new RandomAccessFile(file, "r");
	        in = new RandomAccessFileInputStream(ram);

			reader = WarcReaderFactory.getReaderUncompressed(in, 8192);

			reader.setBlockDigestEnabled( true );
			reader.setBlockDigestAlgorithm( "sha1" );
			reader.setPayloadDigestEnabled( true );
			reader.setPayloadDigestAlgorithm( "sha1" );

			for (int i=0; i<entries.size(); ++i) {
				entry = entries.get(i);

				ram.seek(entry.offset);

				if ((record = reader.getNextRecordFrom(in)) != null) {
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

					if (record.warcRecordIdUri.compareTo(entry.recordId) != 0) {
						Assert.fail("Wrong record");
					}
				}
				else {
					Assert.fail("Location incorrect");
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
		catch (IOException e) {
			Assert.fail("Unexpected io exception");
		}
		catch (NoSuchAlgorithmException e) {
			Assert.fail("Unexpected algorithm exception");
		}
	}

	class WarcEntry {
		URI recordId;
		long offset;
	}

	public List<WarcEntry> indexWarcFile() {
		boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

		List<WarcEntry> warcEntries = new ArrayList<WarcEntry>();
		WarcEntry warcEntry;

		int records = 0;
        int errors = 0;

        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

    		WarcReader reader = WarcReaderFactory.getReader(in);

			reader.setBlockDigestEnabled( true );
			reader.setBlockDigestAlgorithm( "sha1" );
			reader.setPayloadDigestEnabled( true );
			reader.setPayloadDigestAlgorithm( "sha1" );

			Iterator<WarcRecord> recordIterator = reader.iterator();
    		WarcRecord record;

    		while (recordIterator.hasNext()) {
    			record = recordIterator.next();
    			++records;

				if (record.warcRecordIdUri == null) {
    				Assert.fail("Invalid warc-record-id");
    			}

    			warcEntry = new WarcEntry();
    			warcEntry.recordId = record.warcRecordIdUri;
    			warcEntry.offset = record.offset;
    			warcEntries.add(warcEntry);

				record.close();

				if ( bDigest ) {
					if ( (record.payload != null && record.computedBlockDigest == null)
							|| (record.httpResponse != null && record.computedPayloadDigest == null) ) {
						Assert.fail( "Digest missing!" );
					}
				}

				if (bDebugOutput) {
	    			System.out.println("0x" + Long.toString(warcEntry.offset, 16) + "(" + warcEntry.offset + ") - " + warcEntry.recordId);
				}

				if (record.hasErrors()) {
    				errors += record.getValidationErrors().size();
    			}
    		}

    		reader.close();
    		in.close();
        }
        catch (IOException e) {
			Assert.fail("Unexpected io exception");
        } 
        catch (NoSuchAlgorithmException e) {
			Assert.fail("Unexpected algorithm exception");
		}

        Assert.assertEquals(expected_records, records);
        Assert.assertEquals(0, errors);

        return warcEntries;
	}

}