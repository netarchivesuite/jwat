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

import dk.netarkivet.common.ByteCountingPushBackInputStream;

/**
 * WARC Reader used on uncompressed files.
 *
 * @author nicl
 */
public class WarcReaderUncompressed extends WarcReader {

    /** WARC file <code>InputStream</code>. */
	protected ByteCountingPushBackInputStream in;

	/**
	 * Construct object not associated with any input stream.
	 * The reader must be supplied an input stream for each record read.
	 * This method is for use with random access to records.
	 * @param in <code>WarcInputStream</code>
	 */
	WarcReaderUncompressed() {
	}

	/**
	 * Construct object using supplied <code>WarcInputStream</code>.
	 * This method is primarily for linear access to records.
	 * @param in <code>WarcInputStream</code>
	 */
	WarcReaderUncompressed(ByteCountingPushBackInputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("in");
        }
		this.in = in;
	}

	@Override
	public boolean isCompressed() {
		return false;
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
	public WarcRecord getNextRecord() throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
		if (in == null) {
			throw new IllegalStateException();
		}
		warcRecord = WarcRecord.parseRecord(in);
		return warcRecord;
	}

	@Override
	public WarcRecord getNextRecordFrom(InputStream in) throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
		if (in == null) {
			throw new InvalidParameterException();
		}
		warcRecord = WarcRecord.parseRecord(new ByteCountingPushBackInputStream(in, 16));
		return warcRecord;
	}

	@Override
	public WarcRecord getNextRecordFrom(InputStream in, int buffer_size) throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
		if (in == null || buffer_size <= 0) {
			throw new InvalidParameterException();
		}
		warcRecord = WarcRecord.parseRecord(new ByteCountingPushBackInputStream(new BufferedInputStream(in, buffer_size), 16));
		return warcRecord;
	}

}
