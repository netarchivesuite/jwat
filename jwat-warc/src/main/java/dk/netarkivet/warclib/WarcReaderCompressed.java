/*
 * Created on 04/11/2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.netarkivet.warclib;

import java.io.InputStream;
import java.util.Iterator;

import dk.netarkivet.gzip.GzipInputStream;

public class WarcReaderCompressed extends WarcReader {

    /** WARC file <code>InputStream</code>. */
	protected GzipInputStream in;

    /** Current WARC record object. */
	protected WarcRecord warcRecord;

	WarcReaderCompressed(GzipInputStream in) {
		this.in = in;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WarcRecord nextRecord() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WarcRecord nextRecord(InputStream in) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<WarcRecord> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

}
