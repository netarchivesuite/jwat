package dk.netarkivet.arc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.gzip.GzipInputStream;

public class ArcReaderFactory {

	/**
	 * Private constructor to enforce factory method.
	 */
	private ArcReaderFactory() {
	}

	/**
	 * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
	 * wrapped by a <code>BufferedInputStream</code>.
     * @param in ARC file <code>InputStream</code>
	 * @param buffer_size buffer size to use
	 * @return appropriate <code>ArcReader</code> chosen from 
	 * <code>InputStream</code>
	 * @throws IOException if an exception occurs during initialization
	 */
	public static ArcReader getReader(InputStream in, int buffer_size) throws IOException {
		if (in == null || buffer_size <= 0) {
			throw new InvalidParameterException();
		}
		ByteCountingPushBackInputStream bpin = new ByteCountingPushBackInputStream(new BufferedInputStream(in, buffer_size), 16);
		if (GzipInputStream.isGziped(bpin)) {
			return new ArcReaderCompressed(new GzipInputStream(bpin), buffer_size);
		}
		return new ArcReaderUncompressed(bpin);
	}

	/**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on 
     * GZip auto detection. 
     * @param in ARC file <code>InputStream</code>
	 * @return appropriate <code>ArcReader</code> chosen from 
	 * <code>InputStream</code>
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

}
