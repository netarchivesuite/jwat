package org.jwat.arc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * ARC Reader base class.
 *
 * @author nicl
 */
public abstract class ArcReader {

    /** Current ARC version block object. */
    protected ArcVersionBlock versionBlock = null;

    /** Current ARC record object. */
    protected ArcRecord arcRecord = null;

    /** Previous record of either kind. */
    protected ArcRecordBase previousRecord = null;

    /** Exception thrown while using the iterator. */
    public Exception exceptionThrown;

    /**
     * Is this reader assuming compressed input.
     * @return boolean indicating the assumption of compressed input
     */
    public abstract boolean isCompressed();

    /**
     * Close current record resource(s) and input stream(s).
     */
    public abstract void close();

    /**
     * Get the currect offset in the ARC <code>InputStream</code>.
     * @return offset in ARC <code>InputStream</code>
     * @see ArcRecordBase#getOffset()
     */
    @Deprecated
    public abstract long getOffset();

    /**
     * Parses and gets the version block of the ARC file.
     * @return the version block of the ARC file
     * @throws IOException io exception in reading process
     */
    public abstract ArcVersionBlock getVersionBlock() throws IOException;

    /**
     * Parses and gets the version block of an ARC file from the supplied
     * <code>InputStream</code>.
     * @param in input stream from which to read version block
     * @return the version block of the ARC file
     * @throws IOException io exception in reading process
     */
    public abstract ArcVersionBlock getVersionBlock(InputStream in)
            throws IOException;

    /**
     * Parses and gets the next ARC record.
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    public abstract ArcRecord getNextRecord() throws IOException;

    /**
     * Parses and gets the next ARC record.
     * @param in ARC record <code>InputStream</code>
     * @param offset offset provided by caller
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    public abstract ArcRecord getNextRecordFrom(InputStream in, long offset)
            throws IOException;

    /**
     * Parses and gets the next ARC record.
     * @param in ARC record <code>InputStream</code>
     * @param buffer_size size of buffer used to wrap <code>InputStream</code>
     * @param offset offset provided by caller
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    public abstract ArcRecord getNextRecordFrom(InputStream in,
            int buffer_size, long offset) throws IOException;

    /**
     * <code>Iterator</code> over the <code>ARC</code> records.
     * @return <code>Iterator</code> over the <code>ARC</code> records
     */
    public Iterator<ArcRecord> iterator() {
        return new Iterator<ArcRecord>() {

            private ArcRecord next;

            private ArcRecord current;

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
            public ArcRecord next() {
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
