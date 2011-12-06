package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * Factory used for creating <code>WarcReader</code> instances.
 * The general <code>getReader</code> methods will auto-detect Gzip'ed data
 * and return the appropriate <code>WarcReader</code> instances.
 * The other factory methods can be used to return specific
 * <code>WarcReader</code> instances for compressed or uncompressed records.
 * Readers are available for both sequential and random reading of records.
 * Use of buffered methods and/or buffering speeds up the reader considerably.
 *
 * @author nicl
 */
public class WarcReaderFactory {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /**
     * Private constructor to enforce factory method.
     */
    private WarcReaderFactory() {
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on
     * GZip auto detection.
     * @param in WARC file <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return appropriate <code>WarcReader</code> based on
     * <code>InputStream</code> data
     * @throws IOException if an exception occurs during initialization
     */
    public static WarcReader getReader(InputStream in, int buffer_size)
                                                        throws IOException {
        if (in == null) {
            throw new InvalidParameterException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new InvalidParameterException(
                    "The 'buffer_size' is less than or equal to zero: " +
                    buffer_size);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(in, buffer_size),
                PUSHBACK_BUFFER_SIZE);
        if (GzipInputStream.isGziped(pbin)) {
            return new WarcReaderCompressed(new GzipInputStream(pbin),
                                            buffer_size);
        }
        return new WarcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on
     * GZip auto detection.
     * @param in WARC file <code>InputStream</code>
     * @return appropriate <code>WarcReader</code> based on
     * <code>InputStream</code> data
     * @throws IOException if an exception occurs during initialization
     */
    public static WarcReader getReader(InputStream in) throws IOException {
        if (in == null) {
            throw new InvalidParameterException(
                    "The inputstream 'in' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(in, PUSHBACK_BUFFER_SIZE);
        if (GzipInputStream.isGziped(pbin)) {
            return new WarcReaderCompressed(new GzipInputStream(pbin));
        }
        return new WarcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>WarcReader</code> without any associated
     * <code>InputStream</code> for random access to uncompressed records.
     * @return <code>WarcReader</code> for uncompressed records
     * <code>InputStream</code>
     */
    public static WarcReader getReaderUncompressed() {
        return new WarcReaderUncompressed();
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * primarily for random access to uncompressed records.
     * @param in WARC file <code>InputStream</code>
     * @return <code>WarcReader</code> for uncompressed records
     * <code>InputStream</code>
     * @throws IOException io exception while initializing reader
     */
    public static WarcReader getReaderUncompressed(InputStream in)
                                                        throws IOException {
        if (in == null) {
            throw new InvalidParameterException(
                    "The inputstream 'in' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(in, PUSHBACK_BUFFER_SIZE);
        return new WarcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code> primarily for random
     * access to uncompressed records.
     * @param in WARC file <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return <code>WarcReader</code> for uncompressed records
     * <code>InputStream</code>
     * @throws IOException io exception while initializing reader
     */
    public static WarcReader getReaderUncompressed(InputStream in,
                                        int buffer_size) throws IOException {
        if (in == null) {
            throw new InvalidParameterException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new InvalidParameterException(
                    "The 'buffer_size' is less than or equal to zero: " +
                    buffer_size);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(in, buffer_size),
                PUSHBACK_BUFFER_SIZE);
        return new WarcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>WarcReader</code> without any associated
     * <code>InputStream</code> for random access to GZip compressed records.
     * @return <code>WarcReader</code> for GZip compressed records
     * <code>InputStream</code>
     */
    public static WarcReader getReaderCompressed() {
        return new WarcReaderCompressed();
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * primarily for random access to GZip compressed records.
     * @param in WARC file <code>InputStream</code>
     * @return <code>WarcReader</code> for GZip compressed records
     * <code>InputStream</code>
     * @throws IOException io exception while initializing reader
     */
    public static WarcReader getReaderCompressed(InputStream in)
                                                        throws IOException {
        if (in == null) {
            throw new InvalidParameterException(
                    "The inputstream 'in' is null");
        }
        return new WarcReaderCompressed(new GzipInputStream(in));
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code> primarily for random
     * access to GZip compressed records.
     * @param in WARC file <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return <code>WarcReader</code> for GZip compressed records
     * <code>InputStream</code>
     * @throws IOException io exception while initializing reader
     */
    public static WarcReader getReaderCompressed(InputStream in,
                                        int buffer_size) throws IOException {
        if (in == null) {
            throw new InvalidParameterException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new InvalidParameterException(
                    "The 'buffer_size' is less than or equal to zero: " +
                    buffer_size);
        }
        return new WarcReaderCompressed(new GzipInputStream(
                new BufferedInputStream(in, buffer_size)));
    }

}
