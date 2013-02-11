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
package org.jwat.common;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Abstract base class for payload types that include a header. The header
 * is parse and validated. If the header is valid the header is exposed as
 * a byte array including optionally extra data implemented as fields,
 * the rest of the payload is exposed as the complete payload of this object.
 * If on the other hand the header is not valid the input stream is left
 * as is so the complete payload can be read instead.
 * Streams are also available to read the complete payload even though the
 * header has been identified and parsed into optional extra fields.
 *
 * @author nicl
 */
public abstract class PayloadWithHeaderAbstract {

    /** Has record been closed before. */
    protected boolean bClosed;

    /** <code>InputStream</code> to read payload. */
    protected ByteCountingPushBackInputStream in_pb;

    /** Payload length. */
    protected long totalLength;

    protected String digestAlgorithm;

    /** Could the header be validated. */
    protected boolean bIsValid;

    /** Stream used to ensure we don't use more than the pushback buffer on
     *  headers and at the same time store everything read in an array. */
    protected MaxLengthRecordingInputStream in_flr;

    /** The raw header read as bytes. */
    protected byte[] header;

    /** Message digest object. */
    protected MessageDigest md;

    /** Digest bytes. */
    protected byte[] digest;

    /** Automatic digesting of payload input stream. */
    protected DigestInputStream in_digest;

    /** Boolean indicating no such algorithm exception under initialization. */
    protected boolean bNoSuchAlgorithmException;

    /** Payload stream returned to user. */
    protected InputStream in_payload;

    /** Sequence of the header as a stream combined with the payload stream. */
    protected SequenceInputStream in_complete;

    /** Object size, in bytes. */
    public long payloadLength = 0L;

    /** Validation errors and warnings. */
    public Diagnostics<Diagnosis> diagnostics;

    protected void initProcess() throws IOException {
        in_flr = new MaxLengthRecordingInputStream(
                in_pb, in_pb.getPushbackSize());
        bIsValid = readHeader(in_flr, totalLength);
        if (bIsValid) {
            /*
             * Payload Digest.
             */
            if (digestAlgorithm != null) {
                try {
                    md = MessageDigest.getInstance(digestAlgorithm);
                } catch (NoSuchAlgorithmException e) {
                    bNoSuchAlgorithmException = true;
                }
            }
            if (md != null) {
                in_digest = new DigestInputStreamNoSkip(in_pb, md);
                in_payload = in_digest;
            } else {
                in_payload = in_pb;
            }
            /*
             * Ensure close() is not called on the payload stream!
             */
            in_payload = new FilterInputStream(in_payload) {
                @Override
                public void close() throws IOException {
                }
            };
            header = in_flr.getRecording();
            in_complete = new SequenceInputStream(new ByteArrayInputStream(header), in_payload);
            in_flr = null;
        } else {
            // Undo read and leave callers input stream in original state.
            header = in_flr.getRecording();
            in_pb.unread(header);
            in_flr = null;
            bClosed = true;
        }
    }

    /**
     * Reads the header and updates the payloadLength field if the header is valid.
     * @param in the input stream to parse.
     * @param payloadLength the record length.
     * @return boolean indicating whether the header could be read
     * @throws IOException io exception while reading headers
     */
    protected abstract boolean readHeader(MaxLengthRecordingInputStream in,
            long payloadLength) throws IOException;

    /**
     * Get a <code>List</code> of all the headers found during parsing.
     * @return <code>List</code> of <code>HeaderLine</code>
     */
    public List<HeaderLine> getHeaderList() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a header line structure or null, if no header line structure is
     * stored with the given header name.
     * @param field header name
     * @return <code>HeaderLine</code> structure or null
     */
    public HeaderLine getHeader(String field) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the result of the header validation.
     * @return the result of the header validation
     */
    public boolean isValid() {
        return bIsValid;
    }

    /**
     * Get the raw header as bytes.
     * @return raw header as bytes
     */
    public byte[] getHeader() {
        return header;
    }

    /**
     * Returns the <code>MessageDigest</code> used on payload stream.
     * @return <code>MessageDigest</code> used on payload stream
     */
    public byte[] getDigest() {
        if (digest == null && md != null) {
            digest = md.digest();
        }
        return digest;
    }

    /**
     * Get HTTP payload length.
     * @return HTTP payload length
     */
    public long getPayloadLength() {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return payloadLength;
    }

    /**
     * Get payload total length, header and payload.
     * @return payload total length, header and payload
     */
    public long getTotalLength() {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return totalLength;
    }

    /**
     * Get the number of unavailable bytes missing due to unexpected EOF.
     * This method always returns <code>0</code> as long as the stream is open.
     * @return number of unavailable bytes missing due to unexpected EOF
     * @throws IOException if errors occur calling available method on stream
     */
    public long getUnavailable() throws IOException {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return totalLength - in_pb.getConsumed();
    }

    /**
     * Get an <code>InputStream</code> containing both the header and the
     * payload.
     * @return <code>InputStream</code> containing both the header and the
     * payload.
     */
    public InputStream getInputStreamComplete() {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return in_complete;
    }

    /**
     * Get an <code>InputStream</code> containing only the payload.
     * @return <code>InputStream</code> containing only the payload.
     */
    public InputStream getPayloadInputStream() {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return in_payload;
    }

    /**
     * Get payload remaining length.
     * @return payload remaining length
     * @throws IOException if errors occur calling available method on stream
     */
    public long getRemaining() throws IOException {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return totalLength - in_pb.getConsumed();
    }

    /**
     * Check to see if the header has been closed.
     * @return boolean indicating whether this header is closed or not
     */
    public boolean isClosed() {
        return bClosed;
    }

    /**
     * Closes the this payload stream, skipping unread bytes in the process.
     * @throws IOException io exception in closing process
     */
    public void close() throws IOException {
        if (!bClosed) {
            if (md != null) {
                // Skip remaining unread bytes to ensure payload is completely
                // digested. Skipping because the DigestInputStreamNoSkip
                // has been altered to read when skipping.
                while (in_digest.skip(totalLength) > 0) {
                }
            }
            if (in_pb != null) {
                in_pb.close();
                in_pb = null;
            }
            bClosed = true;
        }
    }

}
