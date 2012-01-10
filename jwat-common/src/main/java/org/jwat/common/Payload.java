package org.jwat.common;

import java.io.BufferedInputStream;
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
public class Payload {

    /** Payload content. */
    private static final int BUFFER_SIZE = 4096;

    /** Has record been closed before. */
    protected boolean bClosed;

    /** Payload length. */
    protected long length;

    /** Base stream used to limit payload access to only the payload and
     * not any record data beyond that. Also detects unexpected EOF. */
    protected FixedLengthInputStream in_fl;

    /** Actual message digest algorithm used. */
    protected MessageDigest md;

    /** Automatic digesting of payload input stream. */
    protected DigestInputStream in_digest;

    /** Boolean indicating no such algorithm exception under initialization. */
    protected boolean bNoSuchAlgorithmException;

    /** Payload content. */
    protected BufferedInputStream in_buffered;

    /** Pushback input stream usable by payload processors. */
    protected ByteCountingPushBackInputStream in_pb_exposed;

    /** Payload stream. */
    protected ByteCountingPushBackInputStream in_exposed;

    /** Pushback size. */
    protected int pushback_size;

    /** HttpResponse payload, if present. */
    protected HttpResponse httpResponse;

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
     * @param digestAlgorithm digest algorithm to use on payload or null
     * @throws IOException if an error occurs while initializing
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
     * @param onClosedHandler on closed handler implementation
     */
    public void setOnClosedHandler(PayloadOnClosedHandler onClosedHandler) {
        this.onClosedHandler = onClosedHandler;
    }

    /**
     * Returns the <code>MessageDigest</code> used on payload stream.
     * @return <code>MessageDigest</code> used on payload stream
     */
    public MessageDigest getMessageDigest() {
        return md;
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
     * @throws IOException if errors occur calling available method on stream
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
     * Set http response object in case of recognized payload content.
     * @param httpResponse http response payload object
     */
    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    /**
     * Get the <code>HttpResponse</code> object associated with this payload.
     * @return <code>HttpResponse</code> object or null
     */
    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    /**
     * Get <code>InputStream</code> to read payload data.
     * @return <code>InputStream</code> to read payload data.
     */
    public InputStream getInputStreamComplete() {
        if (httpResponse != null) {
            return httpResponse.getInputStreamComplete();
        } else {
            return in_pb_exposed;
        }
    }

    /**
     * Get <code>InputStream</code> to read payload data.
     * @return <code>InputStream</code> to read payload data.
     */
    public ByteCountingPushBackInputStream getInputStream() {
        return in_pb_exposed;
    }

    /**
     * Get payload remaining length.
     * @return payload remaining length
     * @throws IOException if errors occur calling available method on stream
     */
    public long getRemaining() throws IOException {
        if (httpResponse != null) {
            return httpResponse.getPayloadInputStream().available();
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
     * @throws IOException io exception in closing process
     */
    public void close() throws IOException {
        if (!bClosed) {
            if (httpResponse != null) {
                httpResponse.close();
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
