package dk.netarkivet.arc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * Factory used for creating <code>ArcReader</code> instances.
 * The general <code>getReader</code> methods will auto-detect Gzip'ed data
 * and return the appropriate <code>ArcReader</code> instances.
 * The other factory methods can be used to return specific
 * <code>ArcReader</code> instances for compressed or uncompressed records.
 * Readers are available for both sequential and random reading of records.
 * Use of buffered methods and/or buffering speeds up the reader considerably.
 *
 * @author nicl
 */
public class ArcReaderFactory {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /**
     * Private constructor to enforce factory method.
     */
    private ArcReaderFactory() {
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on
     * GZip auto detection.
     * @param in ARC File represented as <code>InputStream</code> 
     * @param buffer_size buffer size to use
     * @return appropriate <code>ArcReader</code> based on data read from
     * <code>InputStream</code>
     * @throws IOException if an exception occurs during initialization
     */
    public static ArcReader getReader(InputStream in, int buffer_size)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(in, buffer_size),
                                                PUSHBACK_BUFFER_SIZE);
        if (GzipInputStream.isGziped(pbin)) {
            return new ArcReaderCompressed(new GzipInputStream(pbin),
                                           buffer_size);
        }
        return new ArcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on
     * GZip auto detection.
     * @param in ARC File represented as <code>InputStream</code> 
     * @return appropriate <code>ArcReader</code> based on data read from
     * <code>InputStream</code>
     * @throws IOException if an exception occurs during initialization
     */
    public static ArcReader getReader(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(in, PUSHBACK_BUFFER_SIZE);
        if (GzipInputStream.isGziped(pbin)) {
            return new ArcReaderCompressed(new GzipInputStream(pbin));
        }
        return new ArcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>ArcReader</code> without any associated
     * <code>InputStream</code> for random access to uncompressed records.
     * @return <code>ArcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     */
    public static ArcReader getReaderUncompressed() {
        return new ArcReaderUncompressed();
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * primarily for random access to uncompressed records.
     * @param in ARC File represented as <code>InputStream</code> 
     * @return <code>ArcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     * @throws IOException io exception while initializing reader
     */
    public static ArcReader getReaderUncompressed(InputStream in)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(in, PUSHBACK_BUFFER_SIZE);
        return new ArcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code> primarily for random
     * access to uncompressed records.
     * @param in ARC File represented as <code>InputStream</code> 
     * @param buffer_size buffer size to use
     * @return <code>ArcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     * @throws IOException io exception while initializing reader
     */
    public static ArcReader getReaderUncompressed(InputStream in,
                                        int buffer_size) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(in, buffer_size),
                        PUSHBACK_BUFFER_SIZE);
        return new ArcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>ArcReader</code> without any associated
     * <code>InputStream</code> for random access to GZip compressed records.
     * @return <code>ArcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     */
    public static ArcReader getReaderCompressed() {
        return new ArcReaderCompressed();
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * primarily for random access to GZip compressed records.
     * @param in ARC File represented as <code>InputStream</code> 
     * @return <code>ArcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     * @throws IOException io exception while initializing reader
     */
    public static ArcReader getReaderCompressed(InputStream in)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        return new ArcReaderCompressed(new GzipInputStream(in));
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code> primarily for random
     * access to GZip compressed records.
     * @param in ARC File represented as <code>InputStream</code> 
     * @param buffer_size buffer size to use
     * @return <code>ArcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     * @throws IOException io exception while initializing reader
     */
    public static ArcReader getReaderCompressed(InputStream in,
                                        int buffer_size) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        return new ArcReaderCompressed(new GzipInputStream(
                                new BufferedInputStream(in, buffer_size)));
    }

}
