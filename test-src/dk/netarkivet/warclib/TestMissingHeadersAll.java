package dk.netarkivet.warclib;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.jhove2.module.format.arc.ArcValidationError;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestMissingHeadersAll {

	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{"test-missing-all-except-warctype.warc"},
				{"test-missing-fields.warc"}
		});
	}

	public TestMissingHeadersAll(String warcFile) {
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
				System.out.println("--------------");
				System.out.println("       Version: " + record.bMagicIdentified + " " + record.bVersionParsed + " " + record.major + "." + record.minor);
				System.out.println("       TypeIdx: " + record.warcTypeIdx);
				System.out.println("          Type: " + record.warcTypeStr);
				System.out.println("      Filename: " + record.warcFilename);
				System.out.println("     Record-ID: " + record.warcRecordIdUri);
				System.out.println("          Date: " + record.warcDate);
				System.out.println("Content-Length: " + record.contentLength);
				System.out.println("  Content-Type: " + record.contentType);
				System.out.println("     Truncated: " + record.warcTruncatedStr);
				System.out.println("   InetAddress: " + record.warcInetAddress);
				System.out.println("  ConcurrentTo: " + record.warcConcurrentToUri);
				System.out.println("      RefersTo: " + record.warcRefersToUri);
				System.out.println("     TargetUri: " + record.warcTargetUriUri);
				System.out.println("   WarcInfo-Id: " + record.warcWarcInfoIdUri);
				System.out.println("   BlockDigest: " + record.warcBlockDigest);
				System.out.println(" PayloadDigest: " + record.warcPayloadDigest);
				System.out.println("IdentPloadType: " + record.warcIdentifiedPayloadType);
				System.out.println("       Profile: " + record.warcProfileStr);
				System.out.println("      Segment#: " + record.warcSegmentNumber);
				System.out.println(" SegmentOrg-Id: " + record.warcSegmentOriginIdUrl);
				System.out.println("SegmentTLength: " + record.warcSegmentTotalLength);
				++records;

				if (record.hasErrors()) {
					Collection<ArcValidationError> errorCol = record.getValidationErrors();
					errors += errorCol.size();

					if (errorCol != null && errorCol.size() > 0) {
						Iterator<ArcValidationError> iter = errorCol.iterator();
						while (iter.hasNext()) {
							ArcValidationError error = iter.next();
							System.out.println( error.error );
							System.out.println( error.field );
							System.out.println( error.value );
						}
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

		//Assert.assertEquals(expected, records);
		//Assert.assertEquals(0, errors);
	}

}
