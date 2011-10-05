package org.jhove2.module.format.arc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class TestArc {

	//static String arcFile = "/home/test/QUICKSTART/oldjobs/1_1316696892071/arcs/1-1-20110922131213-00000-svc-VirtualBox.arc";
	//static String arcFile = "/home/nicl/BnF/jhove2-bnf/src/test/resources/examples/arc/small_BNF.arc";
	//static String arcFile = "/home/test/QUICKSTART/oldjobs/4_1317731601951/arcs/4-3-20111004123336-00000-svc-VirtualBox.arc";
	static String arcFile = "/home/nicl/Downloads/IAH-20080430204825-00000-blackbook.arc";
	
	public static void main(String[] args) {
		File file = new File( arcFile );
		try {
			InputStream in = new FileInputStream( file );

			int records = 0;

			ArcParser parser = new ArcParser( in );
			ArcVersionBlock version = parser.getVersionBlock();

			if ( version != null ) {
				System.out.println( "         url: " + version.r_url + " - " + version.url );
				if ( version.url != null ) {
					System.out.println( "              " + version.url.getScheme() );
					System.out.println( "              " + version.url.getSchemeSpecificPart() );
				}
				System.out.println( "      ipaddr: " + version.r_ipAddress + " - " + version.inetAddress );
				System.out.println( "        date: " + version.r_archiveDate + " - " + version.archiveDate );
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
				System.out.println( "compl.fields: " + version.hasCompliantFields );
				System.out.println( "       magic: " + version.isMagicArcFile );
				System.out.println( "validVersion: " + version.isVersionValid );
				System.out.println( "ValidFldDesc: " + version.isValidFieldDesc );
				System.out.println( "      errors: " + version.hasErrors() );
				System.out.println( "    warnings: " + version.hasWarnings() );

				boolean b = true;
				while ( b ) {
					ArcRecord arcRecord = parser.getNextArcRecord();
					if ( arcRecord != null ) {
						System.out.println( "------------" );
						System.out.println( "         url: " + arcRecord.r_url + " - " + arcRecord.url );
						if ( arcRecord.url != null ) {
							System.out.println( "              " + arcRecord.url.getScheme() );
							System.out.println( "              " + arcRecord.url.getSchemeSpecificPart() );
						}
						System.out.println( "      ipaddr: " + arcRecord.r_ipAddress + " - " + arcRecord.inetAddress );
						System.out.println( "        date: " + arcRecord.r_archiveDate + " - " + arcRecord.archiveDate );
						System.out.println( "content-type: " + arcRecord.r_contentType );
						System.out.println( " result-code: " + arcRecord.r_resultCode );
						System.out.println( "    checksum: " + arcRecord.r_checksum );
						System.out.println( "    location: " + arcRecord.r_location );
						System.out.println( "      offset: " + arcRecord.r_offset );
						System.out.println( "    filename: " + arcRecord.r_filename );
						System.out.println( "      length: " + arcRecord.r_length );
						if (arcRecord.httpResponse != null ) {
							System.out.println( " result-code: " + arcRecord.httpResponse.resultCode );
							System.out.println( "protocol-ver: " + arcRecord.httpResponse.protocolVersion );
							System.out.println( "content-type: " + arcRecord.httpResponse.contentType );
							System.out.println( " object-size: " + arcRecord.httpResponse.objectSize );
							save( arcRecord.r_url, arcRecord.httpResponse );
						}
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

	public static void save(String url, HttpResponse httpResponse) {
		if ( "200".equals(httpResponse.resultCode) && url != null && url.length() > 0 && httpResponse.objectSize > 0L ) {
			if ( url.startsWith("http://") ) {
				int fidx = "http://".length();
				fidx = url.indexOf( '/', fidx );
				if ( fidx == -1 ) {
					fidx = url.length();
				}
				if ( fidx < url.length() && url.charAt( fidx ) == '/' ) {
					++fidx;
				}
				int lidx = url.indexOf( '?', fidx );
				if ( lidx == -1 ) {
					 lidx = url.length();
				}
				if ( lidx > 0 && lidx > fidx && url.charAt( lidx - 1) == '/' ) {
					--lidx;
				}
				String filename;
				if ( lidx == fidx ) {
					filename = "_index.html";
				}
				else {
					filename = url.substring( fidx, lidx ).replace( '/', '_' );
				}
				try {
					byte[] bytes = new byte[ 1024 ];
					int read;
					File file = new File( "tmp/", filename );
					System.out.println( "            > " + file.getPath() );
					RandomAccessFile ram = new RandomAccessFile( file, "rw" );
					ram.setLength( 0 );
					ram.seek( 0 );
					while ( (read = httpResponse.in.read( bytes )) != -1 ) {
						ram.write( bytes, 0,  read );
					}
					ram.close();
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
	}

}
