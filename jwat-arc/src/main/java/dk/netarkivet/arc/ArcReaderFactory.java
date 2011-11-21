package dk.netarkivet.arc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

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
     * @param in ARC file <code>InputStream</code>
	 * @param buffer_size buffer size to use
	 * @return appropriate <code>ArcReader</code> based on 
	 * <code>InputStream</code> data
	 * @throws IOException if an exception occurs during initialization
	 */
	public static ArcReader getReader(InputStream in, int buffer_size) throws IOException {
		if (in == null || buffer_size <= 0) {
			throw new InvalidParameterException();
		}
		ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(new BufferedInputStream(in, buffer_size), 16);
		if (GzipInputStream.isGziped(pbin)) {
			return new ArcReaderCompressed(new GzipInputStream(pbin), buffer_size);
		}
		return new ArcReaderUncompressed(pbin);
	}

	/**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on 
     * GZip auto detection. 
     * @param in ARC file <code>InputStream</code>
	 * @return appropriate <code>ArcReader</code> based on 
	 * <code>InputStream</code> data
	 * @throws IOException if an exception occurs during initialization
	 */
	public static ArcReader getReader(InputStream in) throws IOException {
		if (in == null) {
			throw new InvalidParameterException();
		}
		ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(in, 16);
		if (GzipInputStream.isGziped(pbin)) {
			return new ArcReaderCompressed(new GzipInputStream(pbin));
		}
		return new ArcReaderUncompressed(pbin);
	}

	/**
	 * Creates a new <code>ArcReader</code> without any associated
	 * <code>InputStream</code> for random access to uncompressed records.
	 * @return <code>ArcReader</code> for uncompressed records
	 * <code>InputStream</code>
	 */
	public static ArcReader getReaderUncompressed() {
		return new ArcReaderUncompressed();
	} 

	/**
	 * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
	 * primarily for random access to uncompressed records.
     * @param in ARC file <code>InputStream</code>
	 * @return <code>ArcReader</code> for uncompressed records
	 * <code>InputStream</code>
	 */
	public static ArcReader getReaderUncompressed(InputStream in) throws IOException {
		if (in == null) {
			throw new InvalidParameterException("in");
		}
		ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(in, 16);
		return new ArcReaderUncompressed(pbin);
	} 

	/**
	 * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
	 * wrapped by a <code>BufferedInputStream</code> primarily for random
	 * access to uncompressed records.
     * @param in ARC file <code>InputStream</code>
	 * @param buffer_size buffer size to use
	 * @return <code>ArcReader</code> for uncompressed records
	 * <code>InputStream</code>
	 */
	public static ArcReader getReaderUncompressed(InputStream in, int buffer_size) throws IOException {
		if (in == null) {
			throw new InvalidParameterException("in");
		}
		if (buffer_size <= 0) {
			throw new InvalidParameterException("buffer_size");
		}
		ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(new BufferedInputStream(in, buffer_size), 16);
		return new ArcReaderUncompressed(pbin);
	} 

	/**
	 * Creates a new <code>ArcReader</code> without any associated
	 * <code>InputStream</code> for random access to GZip compressed records.
	 * @return <code>ArcReader</code> for GZip compressed records
	 * <code>InputStream</code>
	 */
	public static ArcReader getReaderCompressed() {
		return new ArcReaderCompressed();
	} 

	/**
	 * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
	 * primarily for random access to GZip compressed records.
     * @param in ARC file <code>InputStream</code>
	 * @return <code>ArcReader</code> for GZip compressed records
	 * <code>InputStream</code>
	 */
	public static ArcReader getReaderCompressed(InputStream in) throws IOException {
		if (in == null) {
			throw new InvalidParameterException("in");
		}
		return new ArcReaderCompressed(new GzipInputStream(in));
	} 

	/**
	 * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
	 * wrapped by a <code>BufferedInputStream</code> primarily for random
	 * access to GZip compressed records.
     * @param in ARC file <code>InputStream</code>
	 * @param buffer_size buffer size to use
	 * @return <code>ArcReader</code> for GZip compressed records
	 * <code>InputStream</code>
	 */
	public static ArcReader getReaderCompressed(InputStream in, int buffer_size) throws IOException {
		if (in == null) {
			throw new InvalidParameterException("in");
		}
		if (buffer_size <= 0) {
			throw new InvalidParameterException("buffer_size");
		}
		return new ArcReaderCompressed(new GzipInputStream(new BufferedInputStream(in, buffer_size)));
	} 

}
