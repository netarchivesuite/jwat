package dk.netarkivet.warclib;

import java.util.Collection;
import java.util.Iterator;

public class RecordDebugBase {

	private RecordDebugBase() {
	}

	public static void printRecord(WarcRecord record) {
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
		System.out.println("  ConcurrentTo: " + record.warcConcurrentToUriList);
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
	}

	public static void printStatus(int records, int errors) {
		System.out.println("--------------");
		System.out.println("       Records: " + records);
		System.out.println("        Errors: " + errors);
	}

	public static void printRecordErrors(WarcRecord record) {
		if (record.hasErrors()) {
			Collection<WarcValidationError> errorCol = record.getValidationErrors();
			if (errorCol != null && errorCol.size() > 0) {
				Iterator<WarcValidationError> iter = errorCol.iterator();
				while (iter.hasNext()) {
					WarcValidationError error = iter.next();
					System.out.println( error.error );
					System.out.println( error.field );
					System.out.println( error.value );
				}
			}
		}
	}

}
