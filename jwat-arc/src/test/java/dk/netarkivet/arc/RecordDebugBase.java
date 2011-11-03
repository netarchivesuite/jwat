package dk.netarkivet.arc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import dk.netarkivet.common.HttpResponse;

public class RecordDebugBase {

	private RecordDebugBase() {
	}

	public static void printVersionBlock(ArcVersionBlock version) {
        System.out.println( "         url: " + version.recUrl + " - " + version.url );
        if ( version.url != null ) {
            System.out.println( "              " + version.url.getScheme() );
            System.out.println( "              " + version.url.getSchemeSpecificPart() );
        }
        System.out.println( "      ipaddr: " + version.recIpAddress + " - " + version.inetAddress );
        System.out.println( "        date: " + version.recArchiveDate + " - " + version.archiveDate );
        System.out.println( "content-type: " + version.recContentType );
        System.out.println( " result-code: " + version.recResultCode );
        System.out.println( "    checksum: " + version.recChecksum );
        System.out.println( "    location: " + version.recLocation );
        System.out.println( "      offset: " + version.recOffset );
        System.out.println( "    filename: " + version.recFilename );
        System.out.println( "      length: " + version.recLength );
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
	}

	public static void printRecord(ArcRecord arcRecord) {
        System.out.println( "------------" );
        System.out.println( "         url: " + arcRecord.recUrl + " - " + arcRecord.url );
        if ( arcRecord.url != null ) {
            System.out.println( "              " + arcRecord.url.getScheme() );
            System.out.println( "              " + arcRecord.url.getSchemeSpecificPart() );
        }
        System.out.println( "      ipaddr: " + arcRecord.recIpAddress + " - " + arcRecord.inetAddress );
        System.out.println( "        date: " + arcRecord.recArchiveDate + " - " + arcRecord.archiveDate );
        System.out.println( "content-type: " + arcRecord.recContentType );
        System.out.println( " result-code: " + arcRecord.recResultCode );
        System.out.println( "    checksum: " + arcRecord.recChecksum );
        System.out.println( "    location: " + arcRecord.recLocation );
        System.out.println( "      offset: " + arcRecord.recOffset );
        System.out.println( "    filename: " + arcRecord.recFilename );
        System.out.println( "      length: " + arcRecord.recLength );
        if (arcRecord.httpResponse != null ) {
            System.out.println( " result-code: " + arcRecord.httpResponse.resultCode );
            System.out.println( "protocol-ver: " + arcRecord.httpResponse.protocolVersion );
            System.out.println( "content-type: " + arcRecord.httpResponse.contentType );
            System.out.println( " object-size: " + arcRecord.httpResponse.objectSize );
            // TODO move save away from package and refine payload processing
            //save( arcRecord.recUrl, arcRecord.httpResponse );
        }
        System.out.println( "      errors: " + arcRecord.hasErrors() );
        System.out.println( "    warnings: " + arcRecord.hasWarnings() );
	}

	public static void printStatus(int records, int errors) {
        System.out.println("------------");
        System.out.println("     Records: " + records);
		System.out.println("      Errors: " + errors);
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
					while ( (read = httpResponse.getPayloadInputStream().read( bytes )) != -1 ) {
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
