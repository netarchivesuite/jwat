package org.jwat.common;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * The standard <code>DigestInputStream</code> does not update the digest
 * when performing a skip operation. Since this is completely useless in
 * most situations, the following implementation remedies this. 
 *
 * @author nicl
 */
public class DigestInputStreamNoSkip extends DigestInputStream {

	/** Buffer size to use when read skipping. */
	public static final int SKIP_READ_BUFFER_SIZE = 1024;

	protected byte[] skip_read_buffer = new byte[SKIP_READ_BUFFER_SIZE];

	/**
	 * Construct a <code>DigestInputStream</code> with the skip method
	 * overridden.
	 * @param stream input stream to digest
	 * @param digest digest implementation to use
	 */
	public DigestInputStreamNoSkip(InputStream stream, MessageDigest digest) {
		super(stream, digest);
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void mark(int readlimit) {
	}

	@Override
	public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
	}

	@Override
	public long skip(long n) throws IOException {
		long remaining = n;
		long skipped = 0;
		long readLast = 0;
        while (remaining > 0 && readLast != -1) {
            remaining -= readLast;
            skipped += readLast;
            readLast = read(skip_read_buffer, 0, (int) Math.min(remaining,
            										  SKIP_READ_BUFFER_SIZE));
        }
   		return skipped;
	}

}
