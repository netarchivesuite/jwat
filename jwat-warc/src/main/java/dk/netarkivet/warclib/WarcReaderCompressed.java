package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.gzip.GzipEntry;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * WARC Reader used on GZip compressed files.
 *
 * @author nicl
 */
public class WarcReaderCompressed extends WarcReader {

    /** WARC file <code>InputStream</code>. */
	protected GzipInputStream in;

	/** Buffer size, if any, to use on GZip entry <code>InputStream</code>. */
	protected int bufferSize;

	WarcReaderCompressed() {
	}

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
	public boolean isCompressed() {
		return true;
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
			throw new IllegalStateException("in");
		}
		warcRecord = null;
		long offset = in.getOffset();
		GzipEntry entry = in.getNextEntry();
		if (entry != null) {
			if (bufferSize > 0) {
				warcRecord = WarcRecord.parseRecord(new ByteCountingPushBackInputStream(new BufferedInputStream(in.getEntryInputStream(), bufferSize), 16));
			}
			else {
				warcRecord = WarcRecord.parseRecord(new ByteCountingPushBackInputStream(in.getEntryInputStream(), 16));
			}
		}
		if (warcRecord != null) {
			warcRecord.offset = offset;
		}
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
		warcRecord = null;
		GzipInputStream gzin = new GzipInputStream(in);
		GzipEntry entry = gzin.getNextEntry();
		if (entry != null) {
			warcRecord = WarcRecord.parseRecord(new ByteCountingPushBackInputStream(gzin.getEntryInputStream(), 16));
		}
		if (warcRecord != null) {
			warcRecord.offset = -1L;
		}
		return warcRecord;
	}

	@Override
	public WarcRecord getNextRecordFrom(InputStream in, int buffer_size) throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
		if (in == null) {
			throw new InvalidParameterException("in");
		}
		if (buffer_size <= 0) {
			throw new InvalidParameterException("buffer_size");
		}
		warcRecord = null;
		GzipInputStream gzin = new GzipInputStream(in);
		GzipEntry entry = gzin.getNextEntry();
		if (entry != null) {
			warcRecord = WarcRecord.parseRecord(new ByteCountingPushBackInputStream(new BufferedInputStream(gzin.getEntryInputStream(), buffer_size), 16));
		}
		if (warcRecord != null) {
			warcRecord.offset = -1L;
		}
		return warcRecord;
	}

}
