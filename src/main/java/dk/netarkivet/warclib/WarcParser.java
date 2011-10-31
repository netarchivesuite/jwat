package dk.netarkivet.warclib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

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

	public WarcParser() {
	}

	/**
     * Creates a new WARC parser from an <code>InputStream</code>.
     * @param in WARC file <code>InputStream</code>
     */
	public WarcParser(InputStream in) {
		this.in = new WarcInputStream(in, 16);
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
