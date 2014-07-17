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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class makes the archived payload of an ARC/WARC record accessible
 * through a stream. The stream is fixed length so only the payload is
 * available.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class Payload implements Closeable{

    /** Payload content. */
    private static final int BUFFER_SIZE = 8192;

    /** Has record been closed before. */
    protected boolean bClosed;

    /** Payload length. */
    protected long length;

    /** Base stream used to limit payload access to only the payload and
     * not any record data beyond that. Also detects unexpected EOF. */
    protected FixedLengthInputStream in_fl;

    /** Message digest object. */
    protected MessageDigest md;

    /** Digest bytes. */
    protected byte[] digest;

    /** Automatic digesting of payload input stream. */
    protected DigestInputStream in_digest;

    /** Boolean indicating no such algorithm exception under initialization. */
    protected boolean bNoSuchAlgorithmException;

    /** Payload content. */
    protected BufferedInputStream in_buffered;

    /** Pushback input stream exposed to the outside, usable by payload processors. */
    protected ByteCountingPushBackInputStream in_pb_exposed;

    /** Payload stream. */
    //protected ByteCountingPushBackInputStream in_exposed;

    /** Pushback size. */
    protected int pushback_size;

    /** Header wrapped payload, if present. */
    protected PayloadWithHeaderAbstract payloadHeaderWrapped;

    /** Handler called when this payloads stream has been fully consumed. */
    protected PayloadOnClosedHandler onClosedHandler;

    /**
     * Non public constructor.
     */
    protected Payload() {
    }

    /**
     * Creates new <code>ArcPayload</code> instance.
     * @param in the input stream to parse.
     * @param length payload length.
     * @param pushback_size   pushback size
     * @param digestAlgorithm digest algorithm to use on payload or null
     * @return the payload
     * @throws IOException if an io error occurs while initializing
     */
    public static Payload processPayload(InputStream in, long length,
            int pushback_size, String digestAlgorithm) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "The 'length' is less than zero: " + length);
        }
        if (pushback_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'pushback_size' is less than or equal to zero: " +
                            pushback_size);
        }
        Payload pl = new Payload();
        pl.length = length;
        pl.pushback_size = pushback_size;
        pl.in_fl = new FixedLengthInputStream(in, length);
        /*
         * Block Digest.
         */
        if (digestAlgorithm != null) {
            try {
                pl.md = MessageDigest.getInstance(digestAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                pl.bNoSuchAlgorithmException = true;
            }
        }
        if (pl.md != null) {
            pl.in_digest = new DigestInputStreamNoSkip(pl.in_fl, pl.md);
            pl.in_buffered = new BufferedInputStream(pl.in_digest, BUFFER_SIZE);
        } else {
            pl.in_buffered = new BufferedInputStream(pl.in_fl, BUFFER_SIZE);
        }
        /*
         * Ensure close() is not called on the payload stream!
         */
        pl.in_pb_exposed = new ByteCountingPushBackInputStream(pl.in_buffered, pushback_size) {
            @Override
            public void close() throws IOException {
            }
        };
        return pl;
    }

    /**
     * Set optional handler to be called when payload is closed.
     * This method should not be called by the payload consumer.
     * @param onClosedHandler on closed handler implementation
     */
    public void setOnClosedHandler(PayloadOnClosedHandler onClosedHandler) {
        this.onClosedHandler = onClosedHandler;
    }

    /**
     * Returns the calculated digest.
     * @return the calculated digest
     */
    public byte[] getDigest() {
        if (digest == null && md != null) {
            digest = md.digest();
        }
        return digest;
    }

    /**
     * Get payload total length.
     * @return payload total length
     */
    public long getTotalLength() {
        return length;
    }

    /**
     * Get the number of unavailable bytes missing due to unexpected EOF.
     * This method always returns <code>0</code> as long as the stream is open.
     * @return number of unavailable bytes missing due to unexpected EOF
     * @throws IOException if an io error occurs calling available method on stream
     */
    public long getUnavailable() throws IOException {
        return in_fl.available();
    }

    /**
     * Get pushback buffer size.
     * @return pushback buffer size
     */
    public int getPushbackSize() {
        return pushback_size;
    }

    /**
     * Set <code>PayloadHeaderWrapper</code> object in case of recognized payload content.
     * This method should not be called by the payload consumer.
     * @param payloadHeaderWrapped <code>PayloadHeaderWrapper</code> object
     */
    public void setPayloadHeaderWrapped(PayloadWithHeaderAbstract payloadHeaderWrapped) {
        this.payloadHeaderWrapped = payloadHeaderWrapped;
    }

    /**
     * Get the <code>PayloadHeaderWrapper</code> object associated with this payload.
     * @return <code>PayloadHeaderWrapper</code> object or null
     */
    public PayloadWithHeaderAbstract getPayloadHeaderWrapped() {
        return payloadHeaderWrapped;
    }

    /**
     * Get <code>InputStream</code> to read the complete payload even though
     * a http response header may have been read.
     * @return <code>InputStream</code> to read payload data (in)directly.
     */
    public InputStream getInputStreamComplete() {
        if (payloadHeaderWrapped != null) {
            return payloadHeaderWrapped.getInputStreamComplete();
        } else {
            return in_pb_exposed;
        }
    }

    /**
     * Get <code>InputStream</code> to read the payload directly from the
     * source bypassing any existing <code>HttpResponse</code> object.
     * A parsed http response header will not be accessible through this
     * stream.
     * @return <code>InputStream</code> to read payload data.
     */
    public ByteCountingPushBackInputStream getInputStream() {
        if (payloadHeaderWrapped != null) {
            return payloadHeaderWrapped.getPayloadInputStream();
        } else {
            return in_pb_exposed;
        }
    }

    /**
     * Get payload remaining length.
     * @return payload remaining length
     * @throws IOException if an io error occurs calling available method on stream
     */
    public long getRemaining() throws IOException {
        if (payloadHeaderWrapped != null) {
            return payloadHeaderWrapped.getPayloadInputStream().available();
        } else {
            return in_pb_exposed.available();
        }
    }

    /**
     * Check to see if the payload has been closed.
     * @return boolean indicating whether this payload is closed or not
     */
    public boolean isClosed() {
        return bClosed;
    }

    /**
     * Closes the this payload stream, skipping unread bytes in the process.
     * @throws IOException if an io error occurs in the closing process
     */
    public void close() throws IOException {
        if (!bClosed) {
            if (payloadHeaderWrapped != null) {
                payloadHeaderWrapped.close();
            }
            if (md != null) {
                // Skip remaining unread bytes to ensure payload is completely
                // digested. Skipping because the DigestInputStreamNoSkip
                // has been altered to read when skipping.
                while (in_digest.skip(length) > 0) {
                }
            }
            if (in_buffered != null) {
                in_buffered.close();
                in_buffered = null;
            }
            if (onClosedHandler != null) {
                onClosedHandler.payloadClosed();
                onClosedHandler = null;
            }
            bClosed = true;
        }
    }

}
