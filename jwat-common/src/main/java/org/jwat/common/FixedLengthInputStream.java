package org.jwat.common;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>InputStream</code> with a fixed amount of bytes available to read.
 * When the stream is closed the remaining bytes that have not been read are
 * read or skipped.
 *
 * @author lbihanic, selghissassi, nicl
 */
public final class FixedLengthInputStream extends FilterInputStream {

	/** Remaining bytes available. */
    protected long remaining;

    /**
     * Create a new input stream with a fixed number of bytes available from
     * the underlying stream.
     * @param in the input stream to wrap
     * @param length fixed number of bytes available through this stream
     */
    public FixedLengthInputStream(InputStream in, long length) {
        super(in);
        this.remaining = length;
    }

    /**
     * Closing will only skip to the end of this fixed length input stream and
     * not calling parent close method.
     */
    @Override
    public void close() throws IOException {
        long skippedLast = 0;
        if (remaining > 0) {
            skippedLast = skip(remaining);
            while (remaining > 0 && skippedLast > 0) {
                skippedLast = skip(remaining);
            }
        }
    }

    @Override
    public int available() throws IOException {
        return (remaining > Integer.MAX_VALUE)
                                ? Integer.MAX_VALUE : (int) (remaining);
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
    public int read() throws IOException {
        int b = -1;
        if (remaining > 0) {
            b = in.read();
            if (b != -1) {
                --remaining;
            }
        }
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int l = -1;
        if (remaining > 0) {
            l = in.read(b, off, (int) Math.min(len, remaining));
            if (l > 0){
                remaining -= l;
            }
        }
        return l;
    }

    @Override
    public long skip(long n) throws IOException {
        long l = 0;
        if (remaining > 0) {
            l = in.skip(Math.min(n, remaining));
            remaining -= l;
        }
        return l;
    }

}
