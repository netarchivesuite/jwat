/*
 * Created on 04/11/2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Iterator;

import dk.netarkivet.gzip.GzipEntry;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * 
 * @author nicl
 */
public class WarcReaderCompressed extends WarcReader {

    /** WARC file <code>InputStream</code>. */
	protected GzipInputStream in;

    /** Current WARC record object. */
	protected WarcRecord warcRecord;

	/** Buffer size, if any, to use on GZip entry <code>InputStream</code>. */
	protected int bufferSize;

	/**
	 * Construct object using supplied <code>GzipInputStream</code>.
	 * @param in <code>GzipInputStream</code>
	 */
	WarcReaderCompressed(GzipInputStream in) {
		this.in = in;
	}

	/**
	 * Construct object using supplied <code>GzipInputStream</code>.
	 */
	WarcReaderCompressed(GzipInputStream in, int buffer_size) {
		this.in = in;
		this.bufferSize = buffer_size;
	}

	@Override
	public void close() {
        if (warcRecord != null) {
            try {
                warcRecord.close();
            }
            catch (IOException e) { /* ignore */ }
            warcRecord = null;
        }
		if (in != null) {
			try {
				in.close();
			}
			catch (IOException e) { /* ignore */ }
			in = null;
		}
	}

	@Override
	public WarcRecord nextRecord() throws IOException {
		if (in == null) {
			throw new IllegalStateException();
		}
		WarcRecord record = null;
		GzipEntry entry = in.getNextEntry();
		if (entry != null) {
			if (bufferSize > 0) {
				record = WarcRecord.parseRecord(new WarcInputStream(new BufferedInputStream(in.getEntryInputStream(), bufferSize), 16));
			}
			else {
				record = WarcRecord.parseRecord(new WarcInputStream(in.getEntryInputStream(), 16));
			}
		}
		return record;
	}

	@Override
	public WarcRecord nextRecordFrom(InputStream in) throws IOException {
		if (in == null) {
			throw new InvalidParameterException();
		}
		WarcRecord record = null;
		GzipInputStream gzin = new GzipInputStream(in);
		GzipEntry entry = gzin.getNextEntry();
		if (entry != null) {
			record = WarcRecord.parseRecord(new WarcInputStream(gzin.getEntryInputStream(), 16));
		}
		return record;
	}

	@Override
	public WarcRecord nextRecordFrom(InputStream in, int buffer_size) throws IOException {
		if (in == null || buffer_size <= 0) {
			throw new InvalidParameterException();
		}
		WarcRecord record = null;
		GzipInputStream gzin = new GzipInputStream(in);
		GzipEntry entry = gzin.getNextEntry();
		if (entry != null) {
			record = WarcRecord.parseRecord(new WarcInputStream(new BufferedInputStream(gzin.getEntryInputStream(), buffer_size), 16));
		}
		return record;
	}

	@Override
	public Iterator<WarcRecord> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

}
