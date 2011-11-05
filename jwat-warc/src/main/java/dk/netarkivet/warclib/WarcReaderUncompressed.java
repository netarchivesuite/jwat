/*
 * Created on 03/11/2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

/**
 * WARC file parser and validator.
 *
 * @author nicl
 */
public class WarcReaderUncompressed extends WarcReader {

    /** WARC file <code>InputStream</code>. */
	protected WarcInputStream in;

    /** Current WARC record object. */
	protected WarcRecord warcRecord;

	/**
	 * Construct object not associated with any input stream.
	 * The reader must be supplied an input stream for each record read.
	 * @param in <code>WarcInputStream</code>
	 */
	WarcReaderUncompressed() {
	}

	/**
	 * Construct object using supplied <code>WarcInputStream</code>.
	 * @param in <code>WarcInputStream</code>
	 */
	WarcReaderUncompressed(WarcInputStream in) {
		this.in = in;
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
		return WarcRecord.parseRecord(in);
	}

	@Override
	public WarcRecord nextRecordFrom(InputStream in) throws IOException {
		if (in == null) {
			throw new InvalidParameterException();
		}
		return WarcRecord.parseRecord(new WarcInputStream(in, 16));
	}

	@Override
	public WarcRecord nextRecordFrom(InputStream in, int buffer_size) throws IOException {
		if (in == null || buffer_size <= 0) {
			throw new InvalidParameterException();
		}
		return WarcRecord.parseRecord(new WarcInputStream(new BufferedInputStream(in, buffer_size), 16));
	}

}
