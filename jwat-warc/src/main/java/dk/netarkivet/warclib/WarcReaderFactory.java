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
 * 
 * It is discouraged to used a non buffered stream as it slows down the reader
 * considerably.
 *
 * @author nicl
 */
public class WarcReaderFactory {

	/**
	 * Private constructor to enforce factory method.
	 */
	private WarcReaderFactory() {
	}

	/**
	 * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
	 * wrapped by a <code>BufferedInputStream</code>.
     * @param in WARC file <code>InputStream</code>
	 * @param buffer_size buffer size to use
	 * @return appropriate <code>WarcReader</code> chosen from 
	 * <code>InputStream</code>
	 * @throws IOException if an exception occurs during initialization
	 */
	public static WarcReader getReader(InputStream in, int buffer_size) throws IOException {
		if (in == null || buffer_size <= 0) {
			throw new InvalidParameterException();
		}
		ByteCountingPushBackInputStream bpin = new ByteCountingPushBackInputStream(new BufferedInputStream(in, buffer_size), 16);
		if (GzipInputStream.isGziped(bpin)) {
			return new WarcReaderCompressed(new GzipInputStream(bpin), buffer_size);
		}
		return new WarcReaderUncompressed(bpin);
	}

	/**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on 
     * GZip auto detection. 
     * @param in WARC file <code>InputStream</code>
	 * @return appropriate <code>WarcReader</code> chosen from 
	 * <code>InputStream</code>
	 * @throws IOException if an exception occurs during initialization
	 */
	public static WarcReader getReader(InputStream in) throws IOException {
		if (in == null) {
			throw new InvalidParameterException();
		}
		ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(in, 16);
		if (GzipInputStream.isGziped(pbin)) {
			return new WarcReaderCompressed(new GzipInputStream(pbin));
		}
		return new WarcReaderUncompressed(pbin);
	}

	/**
	 * Creates a new <code>WarcReader</code> without any associated
	 * <code>InputStream</code> for random access to uncompressed records.
	 * a <code>BufferedInputStream</code>.
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
	 */
	public static WarcReader getReaderUncompressed(InputStream in) {
		if (in == null) {
			throw new InvalidParameterException();
		}
		ByteCountingPushBackInputStream bpin = new ByteCountingPushBackInputStream(in, 16);
		return new WarcReaderUncompressed(bpin);
	} 

	/**
	 * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
	 * wrapped by a <code>BufferedInputStream</code> primarily for random
	 * access to uncompressed records.
     * @param in WARC file <code>InputStream</code>
	 * @param buffer_size buffer size to use
	 * @return <code>WarcReader</code> for uncompressed records
	 * <code>InputStream</code>
	 */
	public static WarcReader getReaderUncompressed(InputStream in, int buffer_size) {
		if (in == null || buffer_size <= 0) {
			throw new InvalidParameterException();
		}
		ByteCountingPushBackInputStream bpin = new ByteCountingPushBackInputStream(new BufferedInputStream(in, buffer_size), 16);
		return new WarcReaderUncompressed(bpin);
	} 

}
