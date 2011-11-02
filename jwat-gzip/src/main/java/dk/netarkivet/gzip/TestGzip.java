package dk.netarkivet.gzip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.warc.WARCReaderFactory;

import dk.netarkivet.warclib.WarcParser;
import dk.netarkivet.warclib.WarcRecord;

public class TestGzip {

	public static void main(String[] args) throws IOException
	{
        long consumed = 0L;

		long millis;

        //String warcFile = "/home/nicl/Desktop/Java/ia-warcs/WIDE-20110225183219005-04371-13730~crawl301.us.archive.org~9443.warc.gz";
        String warcFile = "C:\\Java\\ia-warcs\\WIDE-20110225183219005-04371-13730~crawl301.us.archive.org~9443.warc.gz";

		File file = new File( warcFile );
		InputStream in = new FileInputStream( file );

		GzipInputStream gz = new GzipInputStream(new BufferedInputStream(in, 8192));

		WarcParser parser = new WarcParser();
		WarcRecord record;

		int records = 0;
		int errors = 0;

		millis = System.currentTimeMillis();

		try {
            GzipEntry e = null;
            int memberCount = 0;
            while ((e = gz.getNextEntry()) != null) {
            	InputStream stream = gz.getEntryInputStream();

            	/*
            	System.out.println(e.getOffset());
            	System.out.println(e.getCompressionMethod());
            	System.out.println(e.getCompressionFlags());
            	System.out.println(e.getDate());
            	System.out.println(e.getName());
            	System.out.println(e.getComment());
            	*/

            	++memberCount;

            	if ( (record = parser.nextRecord(new BufferedInputStream(stream, 8192))) != null ) {
    				++records;
    				if (record.hasErrors()) {
    					errors += record.getValidationErrors().size();
    				}
    			}

            	/*
    			System.out.println("--------------");
    			System.out.println("       Records: " + records);
    			System.out.println("        Errors: " + errors);
    			*/

    			// Check member compression method (always deflate).
            	/*
            	if (e.getCompressionMethod().getValue() ==
            			GzipInputStream.DEFLATE) {
                        //	this.deflateMemberCount.incrementAndGet();
            		++this.deflateMemberCount;
            	}
            	*/
            	// Check member validity.
            	/*
            	if (! e.isValid()) {
                }
                */
            }

    		System.out.println(memberCount);
        }
        catch (IOException e) {
        }
        finally {
            // Close GZip input stream.
            try {
                gz.close();
            }
            catch (Exception e) { /* Ignore... */ }

        }

		parser.close();
		in.close();

		millis = System.currentTimeMillis() - millis;
		System.out.println(millis);

		System.out.println("--------------");
		System.out.println("       Records: " + records);
		System.out.println("        Errors: " + errors);

		millis = System.currentTimeMillis();
		heritrixIterate(file, false);
		millis = System.currentTimeMillis() - millis;
		System.out.println(millis);

		//return consumed;
	}

    public static void heritrixIterate(File warcFile, boolean buffered) {
        ArchiveReader archiveReader;
        int records = 0;
        try {
			//archiveReader = ARCReaderFactory.get(warcFile, 0);
	        archiveReader = WARCReaderFactory.get(warcFile, 0);
	        Iterator<ArchiveRecord> iterator = archiveReader.iterator();
	        ArchiveRecord record;
	        while (iterator.hasNext()) {
	            record = iterator.next();
	            ArchiveRecordHeader header = record.getHeader();
	            ++records;
	        }
			System.out.println("--------------");
			System.out.println("       Records: " + records);
			//System.out.println("        Errors: " + errors);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}
