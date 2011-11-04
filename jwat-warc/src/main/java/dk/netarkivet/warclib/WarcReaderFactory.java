package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.gzip.GzipConstants;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * Factory used for creating <code>WarcReader</code> instances based on input 
 * data. The head of the <code>InputStream</code> is poked for a possible GZip
 * magic number.
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
	 * Creates a new WARC parser from an <code>InputStream</code> wrapped by 
	 * a <code>BufferedInputStream</code>.
     * @param in WARC file <code>InputStream</code>
	 * @param buffer_size
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
     * Creates a new WARC parser from an <code>InputStream</code>.
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

	public static WarcReader getReaderUncompressed() {
		return new WarcReaderUncompressed();
	} 

	public static WarcReader getReaderUncompressed(InputStream in) {
		if (in == null) {
			throw new InvalidParameterException();
		}
		WarcInputStream win = new WarcInputStream(in, 16);
		return new WarcReaderUncompressed(win);
	} 

	public static WarcReader getReaderUncompressed(InputStream in, int buffer_size) {
		if (in == null || buffer_size <= 0) {
			throw new InvalidParameterException();
		}
		WarcInputStream win = new WarcInputStream(new BufferedInputStream(in, buffer_size), 16);
		return new WarcReaderUncompressed(win);
	} 

}
