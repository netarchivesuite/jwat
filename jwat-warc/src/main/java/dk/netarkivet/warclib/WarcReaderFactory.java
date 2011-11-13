package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.common.WarcInputStream;
import dk.netarkivet.gzip.GzipConstants;
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
		WarcInputStream win = new WarcInputStream(new BufferedInputStream(in, buffer_size), 16);
		if (isGziped(win)) {
			return new WarcReaderCompressed(new GzipInputStream(win), buffer_size);
		}
		return new WarcReaderUncompressed(win);
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
		WarcInputStream win = new WarcInputStream(in, 16);
		if (isGziped(win)) {
			return new WarcReaderCompressed(new GzipInputStream(win));
		}
		return new WarcReaderUncompressed(win);
	}

	/**
	 * Check head of <code>PushBackInputStream</code> for a GZip magic number.
	 * @param pbin <code>PushBackInputStream</code> with records
	 * @return boolean indicating presence of GZip magic number
	 * @throws IOException io exception while examing head of stream
	 */
	public static boolean isGziped(WarcInputStream pbin) throws IOException {
		byte[] magicBytes = new byte[2];
		int magicNumber = 0xdeadbeef;
		int read = pbin.readFully(magicBytes); 
		if (read == 2) {
			magicNumber = ((magicBytes[1] & 255) << 8) | (magicBytes[0] & 255);
		}
		if (read > 0) {
			pbin.unread(magicBytes, 0, read);
		}
		return (magicNumber == GzipConstants.GZIP_MAGIC);
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
		WarcInputStream win = new WarcInputStream(in, 16);
		return new WarcReaderUncompressed(win);
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
		WarcInputStream win = new WarcInputStream(new BufferedInputStream(in, buffer_size), 16);
		return new WarcReaderUncompressed(win);
	} 

}
