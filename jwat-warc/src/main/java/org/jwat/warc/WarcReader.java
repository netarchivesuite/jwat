/**
 * Java Web Archive Toolkit - Software to read and validate ARC, WARC
 * and GZip files. (http://jwat.org/)
 * Copyright 2011-2012 Netarkivet.dk (http://netarkivet.dk/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwat.warc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jwat.common.Diagnosis;
import org.jwat.common.Diagnostics;
import org.jwat.common.HeaderLineReader;

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

    /** Aggregate number of warnings encountered while parsing. */
    protected int warnings = 0;

    /** Block Digest enabled/disabled. */
    protected boolean bBlockDigest = false;

    /** Default block digest algorithm to use if none is present in the
     *  record. */
    protected String blockDigestAlgorithm;

    /** Default encoding scheme used to encode block digest into a string,
     *  if none is detected from the record. */
    protected String blockDigestEncoding = "base32";

    /** Payload Digest enabled/disabled. */
    protected boolean bPayloadDigest = false;

    /** Default payload digest algorithm to use if none is present in the
     *  record. */
    protected String payloadDigestAlgorithm;

    /** Default encoding scheme used to encode payload digest into a string,
     *  if none is detected from the record. */
    protected String payloadDigestEncoding = "base32";

    /** Current WARC record object. */
    protected WarcRecord currentRecord;

    /** Exception thrown while using the iterator. */
    protected Exception iteratorExceptionThrown;

    /** Max size allowed for a record header. */
    protected int warcHeaderMaxSize;

    /** Max size allowed for a payload header. */
    protected int payloadHeaderMaxSize;

    /** Line reader used to read version lines. */
    protected HeaderLineReader lineReader;

    /** Header line reader used to read the WARC headers. */
    protected HeaderLineReader headerLineReader;

    /** WARC field parser used. */
    protected WarcFieldParsers fieldParsers;

    /** Reader level errors and warnings or when no record is available. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /**
     * Method used to initialize a readers internal state.
     * Must be called by all constructors.
     */
    protected void init() {
        warcHeaderMaxSize = 8192;
        payloadHeaderMaxSize = 32768;
        lineReader = HeaderLineReader.getReader();
        lineReader.bNameValue = false;
        lineReader.encoding = HeaderLineReader.ENC_US_ASCII;
        headerLineReader = HeaderLineReader.getReader();
        headerLineReader.bNameValue = true;
        headerLineReader.encoding = HeaderLineReader.ENC_UTF8;
        headerLineReader.bLWS = true;
        headerLineReader.bQuotedText = true;
        headerLineReader.bEncodedWords = true;
        fieldParsers = new WarcFieldParsers();
    }

    /**
     * Returns a boolean indicating if all records parsed so far are compliant.
     * @return a boolean indicating if all records parsed so far are compliant
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
     * Get the default block digest algorithm.
     * @return default block digest algorithm
     */
    public String getBlockDigestAlgorithm() {
        return blockDigestAlgorithm;
    }

    /**
     * Tries to set the default block digest algorithm and returns a boolean
     * indicating whether the algorithm was accepted or not. This algorithm is
     * only used in case no WARC payload digest header is present in the record.
     * @param digestAlgorithm block digest algorithm
     * (null means no default block digest algorithm is selected)
     * @return boolean indicating the validity of the algorithm supplied
     */
    public boolean setBlockDigestAlgorithm(String digestAlgorithm) {
        if (digestAlgorithm == null || digestAlgorithm.length() == 0) {
            blockDigestAlgorithm = null;
            return true;
        }
        if (WarcDigest.digestAlgorithmLength(digestAlgorithm) > 0) {
            blockDigestAlgorithm = digestAlgorithm;
            return true;
        }
        return false;
    }

    /**
     * Get the default payload digest algorithm.
     * @return default payload digest algorithm
     */
    public String getPayloadDigestAlgorithm() {
        return payloadDigestAlgorithm;
    }

    /**
     * Tries to set the default payload digest algorithm and returns a boolean
     * indicating whether the algorithm was accepted or not. This algorithm is
     * only used in case no WARC payload digest header is present in the record.
     * @param digestAlgorithm payload digest algorithm
     * (null means no default payload digest algorithm is selected)
     * @return boolean indicating the validity of the algorithm supplied
     */
    public boolean setPayloadDigestAlgorithm(String digestAlgorithm) {
        if (digestAlgorithm == null || digestAlgorithm.length() == 0) {
            payloadDigestAlgorithm = null;
            return true;
        }
        if (WarcDigest.digestAlgorithmLength(digestAlgorithm) > 0) {
            payloadDigestAlgorithm = digestAlgorithm;
            return true;
        }
        return false;
    }

    /**
     * Get the default block digest encoding scheme.
     * @return default block digest encoding scheme
     */
    public String getBlockDigestEncoding() {
        return blockDigestEncoding;
    }

    /**
     * Set the default block digest encoding scheme. This scheme is only
     * used if none can be inferred from an existing block digest header.
     * @param encodingScheme encoding scheme
     * (null means default block digest is not encoded)
     */
    public void setBlockDigestEncoding(String encodingScheme) {
        if (encodingScheme != null && encodingScheme.length() > 0) {
            blockDigestEncoding = encodingScheme.toLowerCase();
        } else {
            blockDigestEncoding = null;
        }
    }

    /**
     * Get the default payload digest encoding scheme.
     * @return default payload digest encoding scheme
     */
    public String getPayloadDigestEncoding() {
        return payloadDigestEncoding;
    }

    /**
     * Set the default payload digest encoding scheme. This scheme is only
     * used if none can be inferred from an existing payload digest header.
     * @param encodingScheme encoding scheme
     * (null means default payload digest is not encoded)
     */
    public void setPayloadDigestEncoding(String encodingScheme) {
        if (encodingScheme != null && encodingScheme.length() > 0) {
            payloadDigestEncoding = encodingScheme.toLowerCase();
        } else {
            payloadDigestEncoding = null;
        }
    }

    /**
     * Get the max size allowed for a record header.
     * @return max size allowed for a record header
     */
    public int getWarcHeaderMaxSize() {
        return warcHeaderMaxSize;
    }

    /**
     * Set the max size allowed for a record header.
     * @param size max size allowed
     */
    public void setWarcHeaderMaxSize(int size) {
        warcHeaderMaxSize = size;
    }

    /**
     * Get the max size allowed for a payload header.
     * @return max size allowed for a payload header
     */
    public int getPayloadHeaderMaxSize() {
        return payloadHeaderMaxSize;
    }

    /**
     * Set the max size allowed for a payload header.
     * @param size max size allowed
     */
    public void setPayloadHeaderMaxSize(int size) {
        payloadHeaderMaxSize = size;
    }

    /**
     * Close current record resource(s) and input stream(s).
     */
    public abstract void close();

    /**
     * Callback method called when the payload has been processed.
     */
    protected abstract void recordClosed();

    /**
     * Get the offset of the current or next WARC record.
     * @return offset of the current of next WARC record
     */
    public abstract long getStartOffset();

    /**
     * Get the current offset in the WARC <code>InputStream</code>.
     * @return offset in WARC <code>InputStream</code>
     * @see WarcRecordBase#getStartOffset()
     */
    public abstract long getOffset();

    /**
     * Get number of bytes consumed by this reader.
     * @return number of bytes consumed by this reader
     */
   public abstract long getConsumed();

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
     * @param offset offset provided by caller
     * @return the next record
     * @throws IOException io exception in parsing process
     */
    public abstract WarcRecord getNextRecordFrom(InputStream in, long offset)
                                                        throws IOException;

    /**
     * Parses and gets the next record from an <code>Inputstream</code> wrapped
     * by a <code>BufferedInputStream</code>.
     * This method is mainly for random access use since there are serious
     * side-effects involved in using multiple <code>PushBackInputStream</code>
     * instances.
     * @param in <code>InputStream</code> used to read next record
     * @param offset offset provided by caller
     * @param buffer_size buffer size to use
     * @return the next record
     * @throws IOException io exception in parsing process
     */
    public abstract WarcRecord getNextRecordFrom(InputStream in, long offset,
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
