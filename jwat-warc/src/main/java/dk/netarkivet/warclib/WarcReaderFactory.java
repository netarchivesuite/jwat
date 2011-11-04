package dk.netarkivet.warclib;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import dk.netarkivet.gzip.GzipConstants;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * Factory used for creating <code>WarcReader</code> instances based on input type.
 * The head of the <code>InputStream</code> is poked for a possible GZip magic number.
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
			return null;
		}

		WarcInputStream win = new WarcInputStream(in, 16);

		byte[] magicBytes = new byte[2];
		int magicNumber = 0xdeadbeef;
		int read = read(win, magicBytes); 
		if (read == 2) {
			magicNumber = ((magicBytes[1] & 255) << 8) | (magicBytes[0] & 255);
		}
		if (read > 0) {
			win.unread(magicBytes, 0, read);
		}

		if (magicNumber == GzipConstants.GZIP_MAGIC) {
			return new WarcReaderCompressed(new GzipInputStream(win));
		}
		return new WarcReaderUncompressed(win);
	}

	/**
	 * 
	 * @param pbis
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public static int read(PushbackInputStream pbis, byte[] buffer) throws IOException {
		int readOffset = 0;
		int readRemaining = buffer.length;
		int readLast = 0;
		while (readRemaining > 0 && readLast != -1) {
			readRemaining -= readLast;
			readOffset += readLast;
			readLast = pbis.read(buffer, readOffset, readRemaining);
		}
		if (readRemaining > 0) {
			pbis.unread(buffer, 0, readOffset);
			readOffset = 0;
		}
		return readOffset;
	}

}
