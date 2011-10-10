package dk.netarkivet.warclib;
import java.io.IOException;
import java.io.InputStream;

import org.jhove2.module.format.arc.ByteCountingInputStream;


public class WarcParser {

	protected ByteCountingInputStream in;

	public WarcParser(InputStream in) {
		this.in = new ByteCountingInputStream( in );
	}

	public void close() {
		if (in != null) {
			try {
				in.close();
			}
			catch (IOException e) { /* ignore */ }
			in = null;
		}
	}

	public WarcRecord nextRecord() {
		return WarcRecord.parseRecord( in );
	}

	public WarcRecord nextRecord(InputStream in) {
		return WarcRecord.parseRecord( new ByteCountingInputStream( in ) );
	}

}
