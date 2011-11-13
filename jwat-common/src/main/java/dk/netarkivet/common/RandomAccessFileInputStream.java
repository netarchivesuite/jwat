package dk.netarkivet.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 
 * @author nicl
 */
public class RandomAccessFileInputStream extends InputStream {

	/** Encapsulated <code>RandomAccessFile</code> used for stream data. */
	protected RandomAccessFile ram;

	/** Current mark position in file. */
	protected long mark_position = -1;

	/**
	 * Create a new ramdom access <code>InputStream</code> with repositioning
	 * capabilities.
	 * @param ram <code>RandomAccessFile</code> used for stream data
	 */
	public RandomAccessFileInputStream(RandomAccessFile ram) {
		this.ram = ram;
	}

	@Override
	public void close() throws IOException {
		ram.close();
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public synchronized void mark(int readlimit) {
		try {
			mark_position = ram.getFilePointer();
		}
		catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		if (mark_position == -1) {
			throw new IOException("Mark not set or is invalid");
		}
		ram.seek(mark_position);
	}

	@Override
	public int available() throws IOException {
		long avail = ram.length() - ram.getFilePointer();
		return (int)(Math.min(avail, Integer.MAX_VALUE));
	}

	@Override
	public long skip(long n) throws IOException {
		long skip = Math.min(n, ram.length() - ram.getFilePointer());
		ram.seek(ram.getFilePointer() + skip);
		return skip;
	}

	@Override
	public int read() throws IOException {
		return ram.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return ram.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return ram.read(b, off, len);
	}

}
