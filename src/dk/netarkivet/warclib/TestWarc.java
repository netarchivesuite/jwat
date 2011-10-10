package dk.netarkivet.warclib;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


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
