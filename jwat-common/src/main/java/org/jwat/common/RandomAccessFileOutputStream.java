package org.jwat.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * This class wraps a <code>RandomAccessFile</code> into a usable
 * <code>OutputStream</code> which supports random re-position.
 * Re-positioning is done by using seek() on the <code>RandomAccessFile</code>
 * object. (@see RandomAccessFile#seek())
 *
 * @author nicl
 */
public class RandomAccessFileOutputStream extends OutputStream {

    /** Encapsulated <code>RandomAccessFile</code> used for stream data. */
    protected RandomAccessFile raf;

    /**
     * Create a new random access <code>OutputStream</code> with repositioning
     * capabilities.
     * @param raf <code>RandomAccessFile</code> used for stream data
     */
    public RandomAccessFileOutputStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    /**
     * Closing this stream has no effect.
     */
    @Override
	public void close() throws IOException {
	}

    /**
     * Flushing this stream has no effect.
     */
	@Override
	public void flush() throws IOException {
	}

	@Override
	public void write(int b) throws IOException {
		raf.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		raf.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		raf.write(b, off, len);
	}

}
