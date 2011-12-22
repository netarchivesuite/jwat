package org.jwat.common;

import java.io.ByteArrayOutputStream;
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
public class FixedLengthRecordingInputStream extends FilterInputStream {

	/** Buffer size to use when read skipping. */
	public static final int SKIP_READ_BUFFER_SIZE = 1024;

	protected byte[] skip_read_buffer = new byte[SKIP_READ_BUFFER_SIZE];

	/** Output stream used to keep a record of data read. */
	protected ByteArrayOutputStream record;

	/** Remaining bytes available. */
    protected long remaining;

    /**
     * Create a new input stream with a fixed number of bytes available from
     * the underlying stream.
     * @param in the input stream to wrap
     * @param length fixed number of bytes available through this stream
     */
    public FixedLengthRecordingInputStream(InputStream in, long length) {
        super(in);
        this.record = new ByteArrayOutputStream();
        this.remaining = length;
    }

    /**
     * Return the bytes recorded by the stream.
     * @return recorded data as a byte array
     */
    public byte[] getRecording() {
    	return record.toByteArray();
    }

    /**
     * Closing will only skip to the end of this fixed length input stream and
     * not calling parent close method.
     */
    @Override
    public void close() throws IOException {
        long skippedLast = 0;
        while (remaining > 0 && skippedLast != -1) {
            remaining -= skippedLast;
            skippedLast = skip(remaining);
        }
        record.close();
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
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int l = -1;
        if (remaining > 0L) {
            l = super.read(b, off, (int) Math.min(len, remaining));
            if (l > 0){
                remaining -= l;
                record.write(b, off, l);
            }
        }
        return l;
    }

    @Override
    public int read() throws IOException {
        int b = -1;
        if (remaining > 0L) {
            b = super.read();
            if (b > 0) {
                --remaining;
                record.write(b);
            }
        }
        return b;
    }

    @Override
    public long skip(long n) throws IOException {
        long l = -1;
        if (remaining > 0L){
            l = super.skip(Math.min(n, remaining));
            l = read(skip_read_buffer, 0, (int) Math.min(
            				Math.min(n, remaining), SKIP_READ_BUFFER_SIZE));
            if (l > 0L) {
                remaining -= l;
                record.write(skip_read_buffer, 0, (int) l);
            }
        }
        return l;
    }

}
