package dk.netarkivet.warclib;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import dk.netarkivet.gzip.GzipConstants;

/**
 * WARC file parser and validator.
 *
 * @author nicl
 */
public class WarcParser {

    /** WARC file <code>InputStream</code>. */
	protected WarcInputStream in;

    /** Current WARC record object. */
	protected WarcRecord warcRecord;

	/**
	 * Creates a new WARC parser without an associated <code>InputStream</code>
	 * for
	 */
	public WarcParser() {
	}

	/**
     * Creates a new WARC parser from an <code>InputStream</code>.
     * @param in WARC file <code>InputStream</code>
     */
	public WarcParser(InputStream in) {
		this.in = new WarcInputStream(in, 16);

		byte[] magic = new byte[2];
		if (read(this.in, magic) == 2) {
			if ((((magic[1] & 255) << 8) | (magic[0] & 255)) == GzipConstants.GZIP_MAGIC) {
				// TODO refactor for gzip support tomorrow.
			}
		}
	}

	public int read(PushbackInputStream pbis, byte[] buffer) {
		int readOffset = 0;
		int readRemaining = buffer.length;
		int readLast = 0;
		try {
			while (readRemaining > 0 && readLast != -1) {
				readRemaining -= readLast;
				readOffset += readLast;
				readLast = pbis.read(buffer, readOffset, readRemaining);
			}
		} catch (IOException e) { /* ignore */ }
		if (readRemaining > 0) {
			try {
				pbis.unread(buffer, 0, readOffset);
				readOffset = 0;
			} catch (IOException e) { /* ignore */ }
		}
		return readOffset;
	}

	/**
     * Close current record resources and parser inputstream. 
     */
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

    /**
     * Parses and gets the next ARC record.
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
	public WarcRecord nextRecord() {
		if (in == null) {
			throw new IllegalStateException();
		}
		return WarcRecord.parseRecord(in);
	}

    /**
     * Parses and gets the next ARC record.
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
	public WarcRecord nextRecord(InputStream in) {
		return WarcRecord.parseRecord(new WarcInputStream(in, 16));
	}

	public Iterator<WarcRecord> iterator() {
		return new Iterator<WarcRecord>() {

			private WarcRecord next;

			private WarcRecord current;

			@Override
			public boolean hasNext() {
				if (next == null) {
					next = nextRecord();
				}
				return (next != null);
			}

			@Override
			public WarcRecord next() {
				if (next == null) {
					next = nextRecord();
				}
				if (next == null) {
					throw new NoSuchElementException();
				}
				current = next;
				next = null;
				return current;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
