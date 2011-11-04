/*
 * Created on 03/11/2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

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
public class WarcReaderUncompressed extends WarcReader {

    /** WARC file <code>InputStream</code>. */
	protected WarcInputStream in;

    /** Current WARC record object. */
	protected WarcRecord warcRecord;

	WarcReaderUncompressed(WarcInputStream in) {
		this.in = in;
	}

	/**
     * Close current record resources and parser inputstream. 
     */
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

    /**
     * Parses and gets the next ARC record.
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
	@Override
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
	@Override
	public WarcRecord nextRecord(InputStream in) {
		return WarcRecord.parseRecord(new WarcInputStream(in, 16));
	}

	@Override
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
