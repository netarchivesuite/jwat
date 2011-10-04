package org.jhove2.module.format.arc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TestArc {

	static String arcFile = "/home/test/QUICKSTART/oldjobs/1_1316696892071/arcs/1-1-20110922131213-00000-svc-VirtualBox.arc";
	//static String arcFile = "/home/nicl/BnF/jhove2-bnf/src/test/resources/examples/arc/small_BNF.arc";

	public static void main(String[] args) {
		File file = new File( arcFile );
		try {
			InputStream in = new FileInputStream( file );

			int records = 0;

			ArcParser parser = new ArcParser( in );
			ArcVersionBlock version = parser.getVersionBlock();

			if ( version != null ) {
				System.out.println( "         url: " + version.r_url + " - " + version.url.toString() );
				System.out.println( "              " + version.url.getScheme() );
				System.out.println( "              " + version.url.getSchemeSpecificPart() );
				System.out.println( "      ipaddr: " + version.r_ipAddress + " - " + version.inetAddress.toString() );
				System.out.println( "        date: " + version.r_archiveDate + " - " + version.archiveDate.toString() );
				System.out.println( "content-type: " + version.r_contentType );
				System.out.println( " result-code: " + version.r_resultCode );
				System.out.println( "    checksum: " + version.r_checksum );
				System.out.println( "    location: " + version.r_location );
				System.out.println( "      offset: " + version.r_offset );
				System.out.println( "    filename: " + version.r_filename );
				System.out.println( "      length: " + version.r_length );
				System.out.println( "       major: " + version.versionNumber );
				System.out.println( "       minor: " + version.reserved );
				System.out.println( "      origin: " + version.originCode );
				System.out.println( version.xml );
				System.out.println( "      errors: " + version.hasErrors() );
				System.out.println( "    warnings: " + version.hasWarnings() );

				boolean b = true;
				while ( b ) {
					ArcRecord arcRecord = parser.getNextArcRecord();
					if ( arcRecord != null ) {
						System.out.println( "------------" );
						System.out.println( "         url: " + arcRecord.r_url + " - " + arcRecord.url.toString() );
						System.out.println( "              " + arcRecord.url.getScheme() );
						System.out.println( "              " + arcRecord.url.getSchemeSpecificPart() );
						System.out.println( "      ipaddr: " + arcRecord.r_ipAddress + " - " + arcRecord.inetAddress.toString() );
						System.out.println( "        date: " + arcRecord.r_archiveDate + " - " + arcRecord.archiveDate.toString() );
						System.out.println( "content-type: " + arcRecord.r_contentType );
						System.out.println( " result-code: " + arcRecord.r_resultCode );
						System.out.println( "    checksum: " + arcRecord.r_checksum );
						System.out.println( "    location: " + arcRecord.r_location );
						System.out.println( "      offset: " + arcRecord.r_offset );
						System.out.println( "    filename: " + arcRecord.r_filename );
						System.out.println( "      length: " + arcRecord.r_length );
						System.out.println( "      errors: " + arcRecord.hasErrors() );
						System.out.println( "    warnings: " + arcRecord.hasWarnings() );
						++records;
					}
					else {
						b = false;
					}
				}
				System.out.println( "------------" );
				System.out.println( "     Records: " + records );
			}
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
