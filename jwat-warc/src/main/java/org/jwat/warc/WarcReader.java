package org.jwat.warc;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Base class for WARC reader implementations.
 *
 * @author nicl
 */
public abstract class WarcReader {

    /** Compliance status for records parsed up to now. */
    protected boolean bIsCompliant = true;

    /** Number of bytes consumed by this reader. */
    protected long consumed = 0;

    /** Aggregated number of errors encountered while parsing. */
    protected int errors = 0;

    /** Block Digest enabled/disabled. */
    protected boolean bBlockDigest = false;

    /** Optional block digest algorithm to use if none is present in the
     *  record. */
    protected String blockDigestAlgorithm;

    /** Optional encoding scheme used to encode block digest into a string,
     *  if none is detected from the record. */
    protected String blockDigestEncoding = "base32";

    /** Payload Digest enabled/disabled. */
    protected boolean bPayloadDigest = false;

    /** Optional payload digest algorithm to use if none is present in the
     *  record. */
    protected String payloadDigestAlgorithm;

    /** Optional encoding scheme used to encode payload digest into a string,
     *  if none is detected from the record. */
    protected String payloadDigestEncoding = "base32";

    /** Current WARC record object. */
    protected WarcRecord warcRecord;

    /** Exception thrown while using the iterator. */
    protected Exception iteratorExceptionThrown;

    /**
     * Returns a boolean indicating whether the reader has only parsed
     * compliant records up to now.
     * @return a boolean indicating all compliant records parsed to far
     */
    public boolean isCompliant() {
        return bIsCompliant;
    }

    /**
     * Is this reader assuming GZip compressed input.
     * @return boolean indicating the assumption of GZip compressed input
     */
    public abstract boolean isCompressed();

    /**
     * Get the readers block digest on/off status.
     * @return boolean indicating block digest on/off
     */
    public boolean getBlockDigestEnabled() {
        return bBlockDigest;
    }

    /**
     * Set the readers block digest on/off status. Digest, however,
     * will only be computed if either a Warc-Block-Digest header is
     * present or an optional algorithm has been chosen.
     * The Warc-Block-Digest always takes precedence.
     * @param enabled boolean indicating block digest on/off
     */
    public void setBlockDigestEnabled(boolean enabled) {
        bBlockDigest = enabled;
    }

    /**
     * Get the readers payload digest on/off status.
     * @return boolean indicating payload digest on/off
     */
    public boolean getPayloadDigestEnabled() {
        return bPayloadDigest;
    }

    /**
     * Set the readers payload digest on/off status. Digest, however,
     * will only be computed if either a Warc-Payload-Digest header is
     * present or an optional algorithm has been chosen.
     * The Warc-Payload-Digest always takes precedence.
     * @param enabled boolean indicating payload digest on/off
     */
    public void setPayloadDigestEnabled(boolean enabled) {
        bPayloadDigest = enabled;
    }

    /**
     * Get the optional block digest algorithm.
     * @return optional block digest algorithm
     */
    public String getBlockDigestAlgorithm() {
        return blockDigestAlgorithm;
    }

    /**
     * Set the optional block digest algorithm. This algorithm is only used
     * in case no WARC payload digest header is present in the record.
     * @param digestAlgorithm block digest algorithm
     * (null means optional block digest is disabled)
     * @throws NoSuchAlgorithmException occurs in case the algorithm can not
     * be identified
     */
    public void setBlockDigestAlgorithm(String digestAlgorithm)
                                            throws NoSuchAlgorithmException {
        if (digestAlgorithm != null) {
            MessageDigest.getInstance(digestAlgorithm);
        }
        blockDigestAlgorithm = digestAlgorithm;
    }

    /**
     * Get the optional payload digest algorithm.
     * @return optional payload digest algorithm
     */
    public String getPayloadDigestAlgorithm() {
        return payloadDigestAlgorithm;
    }

    /**
     * Set the optional payload digest algorithm. This algorithm is only used
     * in case no WARC payload digest header is present in the record.
     * @param digestAlgorithm payload digest algorithm
     * (null means optional payload digest is disabled)
     * @throws NoSuchAlgorithmException occurs in case the algorithm can not
     * be identified
     */
    public void setPayloadDigestAlgorithm(String digestAlgorithm)
            throws NoSuchAlgorithmException {
        if (digestAlgorithm != null) {
            MessageDigest.getInstance(digestAlgorithm);
        }
        payloadDigestAlgorithm = digestAlgorithm;
    }

    /**
     * Get the optional block digest encoding scheme.
     * @return optional block digest encoding scheme
     */
    public String getBlockDigestEncoding() {
        return blockDigestEncoding;
    }

    /**
     * Set the optional block digest encoding scheme. This scheme is only
     * used if none can be inferred from an existing block digest header.
     * @param encoding encoding scheme
     * (null means optional block digest is not encoded)
     */
    public void setBlockDigestEncoding(String encoding) {
        if (encoding != null && encoding.length() > 0) {
            blockDigestEncoding = encoding.toLowerCase();
        } else {
            blockDigestEncoding = null;
        }
    }

    /**
     * Get the optional payload digest encoding scheme.
     * @return optional payload digest encoding scheme
     */
    public String getPayloadDigestEncoding() {
        return payloadDigestEncoding;
    }

    /**
     * Set the optional payload digest encoding scheme. This scheme is only
     * used if none can be inferred from an existing payload digest header.
     * @param encoding encoding scheme
     * (null means optional payload digest is not encoded)
     */
    public void setPayloadDigestEncoding(String encoding) {
        if (encoding != null && encoding.length() > 0) {
            payloadDigestEncoding = encoding.toLowerCase();
        } else {
            payloadDigestEncoding = null;
        }
    }

    /**
     * Close current record resource(s) and input stream(s).
     */
    public abstract void close();

    /** Get number of bytes consumed by this reader. */
   public long getConsumed() {
        return consumed;
    }

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
    public abstract WarcRecord getNextRecordFrom(InputStream in)
                                                        throws IOException;

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
    public abstract WarcRecord getNextRecordFrom(InputStream in,
                                        int buffer_size) throws IOException;

    /**
     * Gets an exception thrown in the iterator if any or null.
     * @return exception thrown in the iterator if any or null
     */
    public Exception getIteratorExceptionThrown() {
        return iteratorExceptionThrown;
    }

    /**
     * Returns an <code>Iterator</code> over the records as they are being
     * parsed. Any exception thrown during parsing is accessible through the
     * <code>getIteratorExceptionThrown</code> method.
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
                    iteratorExceptionThrown = null;
                    try {
                        next = getNextRecord();
                    } catch (IOException e) {
                        iteratorExceptionThrown = e;
                    }
                }
                return (next != null);
            }

            @Override
            public WarcRecord next() {
                if (next == null) {
                    iteratorExceptionThrown = null;
                    try {
                        next = getNextRecord();
                    } catch (IOException e) {
                        iteratorExceptionThrown = e;
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
