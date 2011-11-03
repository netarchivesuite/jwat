package dk.netarkivet.warclib;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import dk.netarkivet.gzip.GzipConstants;

/**
 * WARC file parser and validator.
 *
 * @author nicl
 */
public class WarcReaderFactory {

	private WarcReaderFactory() {
	}

	/**
     * Creates a new WARC parser from an <code>InputStream</code>.
     * @param in WARC file <code>InputStream</code>
     */
	public static WarcReader getReader(InputStream in) {
		WarcInputStream win = new WarcInputStream(in, 16);

		byte[] magicBytes = new byte[2];
		int magicNumber = 0xdeadbeef;
		int read = read(win, magicBytes); 
		if (read == 2) {
			magicNumber = ((magicBytes[1] & 255) << 8) | (magicBytes[0] & 255);
		}
		if (read > 0) {
			try {
				win.unread(magicBytes, 0, read);
			} catch (IOException e) {
				return null;
			}
		}

		if (magicNumber == GzipConstants.GZIP_MAGIC) {
			// TODO refactor for gzip support tomorrow.
		}
		return new WarcReaderUncompressed(win);
	}

	public static int read(PushbackInputStream pbis, byte[] buffer) {
		int readOffset = 0;
		int readRemaining = buffer.length;
		int readLast = 0;
		try {
			while (readRemaining > 0 && readLast != -1) {
				readRemaining -= readLast;
				readOffset += readLast;
				readLast = pbis.read(buffer, readOffset, readRemaining);
			}
		} catch (IOException e) { /* ignore */ }
		if (readRemaining > 0) {
			try {
				pbis.unread(buffer, 0, readOffset);
				readOffset = 0;
			} catch (IOException e) { /* ignore */ }
		}
		return readOffset;
	}

}
