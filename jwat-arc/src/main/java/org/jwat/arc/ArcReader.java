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
package org.jwat.arc;

import org.jwat.common.Diagnosis;
import org.jwat.common.Diagnostics;
import org.jwat.common.Digest;
import org.jwat.common.HeaderLineReader;
import org.jwat.common.UriProfile;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * ARC Reader base class.
 *
 * @author nicl
 */
public abstract class ArcReader implements Closeable{

    /*
     * Settings.
     */

    /** Strict mode enabled or disabled.
     *  Non strict mode allows newlines when there should be no xml metadata.
     *  Non strict mode allows a varied number of newlines between records. */
    protected boolean bStrict = false;

    /** URL URI profile. */
    protected UriProfile uriProfile;

    /** Block Digest enabled/disabled. */
    protected boolean bBlockDigest = false;

    /** Optional block digest algorithm to use. */
    protected String blockDigestAlgorithm;

    /** Encoding scheme used to encode block digest into a string. */
    protected String blockDigestEncoding = "base32";

    /** Payload Digest enabled/disabled. */
    protected boolean bPayloadDigest = false;

    /** Optional payload digest algorithm to use. */
    protected String payloadDigestAlgorithm;

    /** Encoding scheme used to encode payload digest into a string. */
    protected String payloadDigestEncoding = "base32";

    /** Max size allowed for a record header. */
    protected int recordHeaderMaxSize;

    /** Max size allowed for a payload header. */
    protected int payloadHeaderMaxSize;

    /** ARC field parser used. */
    protected ArcFieldParsers fieldParsers;

    /** Line reader used to read header lines. */
    protected HeaderLineReader lineReader;

    /*
     * State.
     */

    /** Reader level errors and warnings or when no record is available. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /** Compliance status for records parsed up to now. */
    protected boolean bIsCompliant = true;

    /** Number of bytes consumed by this reader. */
    protected long consumed = 0;

    /** Records parsed. */
    protected int records = 0;

    /** Aggregated number of errors encountered while parsing. */
    protected int errors = 0;

    /** Aggregate number of warnings encountered while parsing. */
    protected int warnings = 0;

    /** Version header from version block. */
    public ArcVersionHeader versionHeader;

    /** Current record of either kind. */
    protected ArcRecordBase currentRecord = null;

    /** Exception thrown while using the iterator. */
    protected Exception iteratorExceptionThrown;

    /**
     * Method used to initialize a readers internal state.
     */
    protected void init() {
        uriProfile = UriProfile.RFC3986;
        recordHeaderMaxSize = 8192;
        payloadHeaderMaxSize = 32768;
        lineReader = HeaderLineReader.getReader();
        lineReader.bNameValue = false;
        lineReader.encoding = HeaderLineReader.ENC_US_ASCII;
        fieldParsers = new ArcFieldParsers();
    }

    /**
     * Reset reader for reuse.
     */
    public void reset() {
        diagnostics.reset();
        bIsCompliant = true;
        consumed = 0;
        records = 0;
        errors = 0;
        warnings = 0;
        versionHeader = null;
        currentRecord = null;
    }

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
     * Set the readers strict mode on/off.
     * @param bStrict strict mode on/off
     */
    public void setStrict(boolean bStrict) {
        this.bStrict = bStrict;
    }

    /**
     * Get the readers strict mode setting.
     * @return readers strict mode setting
     */
    public boolean isStrict() {
        return bStrict;
    }

    /**
     * Set the URI profile used to validate URL URIs.
     * If null, the uriProfile is set to RCF3986.
     * @param uriProfile URI profile to use
     */
    public void setUriProfile(UriProfile uriProfile) {
        if (uriProfile == null) {
            uriProfile = UriProfile.RFC3986;
        }
        this.uriProfile = uriProfile;
    }

    /**
     * Get the URI profile used to validate URL URIs.
     * @return the URI profile used to validate URL URIs
     */
    public UriProfile getUriProfile() {
        return uriProfile;
    }

    /**
     * Get the readers block digest on/off status.
     * @return boolean indicating block digest on/off
     */
    public boolean getBlockDigestEnabled() {
        return bBlockDigest;
    }

    /**
     * Set the readers block digest on/off status. Digest, however,
     * will only be computed if an algorithm has also been chosen.
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
     * will only be computed if an algorithm has also been chosen.
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
     * Tries to set the optional block digest algorithm and returns a boolean
     * indicating whether the algorithm was accepted or not.
     * @param digestAlgorithm block digest algorithm
     * (null means no optional block digest algorithm is selected)
     * @return boolean indicating the validity of the algorithm supplied
     */
    public boolean setBlockDigestAlgorithm(String digestAlgorithm) {
        if (digestAlgorithm == null || digestAlgorithm.length() == 0) {
            blockDigestAlgorithm = null;
            return true;
        }
        if (Digest.digestAlgorithmLength(digestAlgorithm) > 0) {
            blockDigestAlgorithm = digestAlgorithm;
            return true;
        }
        return false;
    }

    /**
     * Get the optional payload digest algorithm.
     * @return optional payload digest algorithm
     */
    public String getPayloadDigestAlgorithm() {
        return payloadDigestAlgorithm;
    }

    /**
     * Tries to set the optional payload digest algorithm and returns a boolean
     * indicating whether the algorithm was accepted or not.
     * @param digestAlgorithm payload digest algorithm
     * (null means no optional payload digest algorithm is selected)
     * @return boolean indicating the validity of the algorithm supplied
     */
    public boolean setPayloadDigestAlgorithm(String digestAlgorithm) {
        if (digestAlgorithm == null || digestAlgorithm.length() == 0) {
            payloadDigestAlgorithm = null;
            return true;
        }
        if (Digest.digestAlgorithmLength(digestAlgorithm) > 0) {
            payloadDigestAlgorithm = digestAlgorithm;
            return true;
        }
        return false;
    }

    /**
     * Get the optional block digest encoding scheme.
     * @return optional block digest encoding scheme
     */
    public String getBlockDigestEncoding() {
        return blockDigestEncoding;
    }

    /**
     * Set the optional block digest encoding scheme.
     * @param encodingScheme encoding scheme
     * (null means optional block digest is not encoded)
     */
    public void setBlockDigestEncoding(String encodingScheme) {
        if (encodingScheme != null && encodingScheme.length() > 0) {
            blockDigestEncoding = encodingScheme.toLowerCase();
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
     * Set the optional payload digest encoding scheme.
     * @param encodingScheme encoding scheme
     * (null means optional payload digest is not encoded)
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
    public int getRecordHeaderMaxSize() {
        return recordHeaderMaxSize;
    }

    /**
     * Set the max size allowed for a record header.
     * @param size max size allowed
     */
    public void setRecordHeaderMaxSize(int size) {
        recordHeaderMaxSize = size;
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
     * Get the offset of the current ARC record or -1 if none have been read.
     * @return offset of the current ARC record or -1
     */
    public abstract long getStartOffset();

    /**
     * Get the current offset in the ARC <code>InputStream</code>.
     * @return offset in ARC <code>InputStream</code>
     * @see ArcRecordBase#getOffset()
     */
    public abstract long getOffset();

    /**
     * Get number of bytes consumed by this reader.
     * @return number of bytes consumed by this reader
     */
    public abstract long getConsumed();

    /**
     * Parses and gets the next ARC record.
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    public abstract ArcRecordBase getNextRecord() throws IOException;

    /**
     * Parses and gets the next ARC record.
     * @param in ARC record <code>InputStream</code>
     * @param offset offset provided by caller
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    public abstract ArcRecordBase getNextRecordFrom(InputStream in, long offset)
            throws IOException;

    /**
     * Parses and gets the next ARC record.
     * @param in ARC record <code>InputStream</code>
     * @param buffer_size size of buffer used to wrap <code>InputStream</code>
     * @param offset offset provided by caller
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    public abstract ArcRecordBase getNextRecordFrom(InputStream in,
            long offset, int buffer_size) throws IOException;

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
     * @return <code>Iterator</code> over the <code>WARC</code> records
     */
    public Iterator<ArcRecordBase> iterator() {
        return new Iterator<ArcRecordBase>() {

            private ArcRecordBase next;

            private ArcRecordBase current;

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
            public ArcRecordBase next() {
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
