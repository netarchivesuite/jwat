package dk.netarkivet.warclib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Base class for WARC reader implementations.
 * 
 * @author nicl
 */
public abstract class WarcReader {

	/** Digesting enabled/disabled. */
	protected boolean bDigest = true;

    /** Current WARC record object. */
	protected WarcRecord warcRecord;

	/** Exception thrown while using the iterator. */
	public Exception exceptionThrown;

	/**
	 * Is this reader assuming compressed input.
	 * @return boolean indicating the assumption of compressed input
	 */
	public abstract boolean isCompressed();

	/**
	 * Is this reader set to digest payload.
	 * @return boolean indicating the assumption of payload digesting
	 */
	public boolean digest() {
		return bDigest;
	}

	/**
	 * Set the readers payload digest mode
	 * @param enabled boolean indicating digest on/off
	 */
	public void setDigest(boolean enabled) {
		bDigest = enabled;
	}

	/**
     * Close current record resource(s) and input stream(s). 
     */
	public abstract void close();

    /**
     * Parses and gets the next record.
	 * This method is for linear access to records.
     * @return the next record
     * @throws IOException io exception in parsing process
     */
	public abstract WarcRecord getNextRecord() throws IOException;

    /**
     * Parses and gets the next record from an <code>Inputstream</code>.
     * This method is mainly for random access use since there are serious
     * side-effects involved in using multiple <code>PushBackInputStream</code>
     * instances.
	 * @param in <code>InputStream</code> used to read next record
     * @return the next record
     * @throws IOException io exception in parsing process
     */
	public abstract WarcRecord getNextRecordFrom(InputStream in) throws IOException;

	/**
     * Parses and gets the next record from an <code>Inputstream</code> wrapped
     * by a <code>BufferedInputStream</code>.
     * This method is mainly for random access use since there are serious
     * side-effects involved in using multiple <code>PushBackInputStream</code>
     * instances.
	 * @param in <code>InputStream</code> used to read next record
	 * @param buffer_size buffer size to use
     * @return the next record
     * @throws IOException io exception in parsing process
	 */
	public abstract WarcRecord getNextRecordFrom(InputStream in, int buffer_size) throws IOException;

	/**
	 * Returns an <code>Iterator</code> over the records as they are being
	 * parsed. Any exception thrown during parsing is accessible in the
	 * <code>exceptionThrown</code> attribute.
	 * @return <code>Iterator</code> over the records
	 */
	public Iterator<WarcRecord> iterator() {
		return new Iterator<WarcRecord>() {

			/** Internal next record updated by either hasNext() or next(). */
			private WarcRecord next;

			/** Entry returned by next(). */
			private WarcRecord current;

			@Override
			public boolean hasNext() {
				if (next == null) {
					try {
						next = getNextRecord();
					} catch (IOException e) {
						exceptionThrown = e;
					}
				}
				return (next != null);
			}

			@Override
			public WarcRecord next() {
				if (next == null) {
					try {
						next = getNextRecord();
					} catch (IOException e) {
						exceptionThrown = e;
					}
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
