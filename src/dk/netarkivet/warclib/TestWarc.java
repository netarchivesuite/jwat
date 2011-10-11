package dk.netarkivet.warclib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.Date;

public class TestWarc {

	static String warcFile = "/home/nicl/Downloads/IAH-20080430204825-00000-blackbook.warc";
	
	public static void main(String[] args) {
		File file = new File( warcFile );
		try {
			InputStream in = new FileInputStream( file );

			int records = 0;

			WarcParser parser = new WarcParser( in );
			WarcRecord record;

			while ( (record = parser.nextRecord()) != null ) {
				System.out.println("          Type: " + record.warcType);
				System.out.println("      Filename: " + record.warcFilename);
				System.out.println("     Record-ID: " + record.warcRecordIdUri);
				System.out.println("          Date: " + record.warcDateDate);
				System.out.println("Content-Length: " + record.contentLength);
				System.out.println("  Content-Type: " + record.contentType);
				System.out.println("     Truncated: " + record.warcTruncated);
				System.out.println("   InetAddress: " + record.warcInetAddress);
				System.out.println("  ConcurrentTo: " + record.warcConcurrentToUri);
				System.out.println("      RefersTo: " + record.warcRefersToUri);
				System.out.println("     TargetUri: " + record.warcTargetUriUri);
				System.out.println("   WarcInfo-Id: " + record.warcWarcInfoIdUri);
				System.out.println("   BlockDigest: " + record.warcBlockDigest);
				System.out.println(" PayloadDigest: " + record.warcPayloadDigest);
				System.out.println("IdentPloadType: " + record.warcIdentifiedPayloadType);
				System.out.println("       Profile: " + record.warcProfile);
				System.out.println("      Segment#: " + record.warcSegmentNumber);
				System.out.println(" SegmentOrg-Id: " + record.warcSegmentOriginIdUrl);
				System.out.println("SegmentTLength: " + record.warcSegmentTotalLength);
				++records;
			}

			System.out.println( "Records: " + records );

			parser.close();
			in.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
