package dk.netarkivet.warclib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.annotation.Inherited;

/**
 * Basic <code>PushBackInputStream</code> that also keeps track of the number
 * of consumed bytes at any given time.
 *
 * @author nicl
 */
public class WarcInputStream extends PushbackInputStream {

    /** Offset relative to beginning of stream. */
    protected long consumed = 0;

    /**
     * Given an <code>InputStream</code> and a push back buffer size returns
     * a wrapped input stream with push back capabilities. 
     * @param in <code>InputStream</code> to wrap
     * @param size push back buffer size
     */
    public WarcInputStream(InputStream in, int size) {
		super(in, size);
	}

    /**
     * Retrieve the number of consumed bytes by this stream.
     * @return current byte offset in this stream
     */
    public long getConsumed() {
        return consumed;
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
        int b = super.read();
        if (b != -1) {
            ++consumed;
        }
        return b;
    }

    /*
	 * The super method did this anyway causing a double amount of
	 * consumed bytes.
     * @see java.io.FilterInputStream#read(byte[])
     */
    @Override
	public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
	}

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            consumed += n;
        }
        return n;
    }

	@Override
    public long skip(long n) throws IOException {
        n = super.skip(n);
        this.consumed += n;
        return n;
    }

	@Override
	public void unread(int b) throws IOException {
		super.unread(b);
		--consumed;
	}

	/*
	 * The super method did this anyway causing a double amount of
	 * un-consumed bytes.
	 * @see java.io.PushbackInputStream#unread(byte[])
	 */
	@Override
	public void unread(byte[] b) throws IOException {
		unread(b, 0, b.length);
	}

	@Override
	public void unread(byte[] b, int off, int len) throws IOException {
		super.unread(b, off, len);
		consumed -= len;
	}

    /**
     * Read a single line into a string.
     * @return single string line
     * @throws IOException io exception while reading line
     */
    public String readLine() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        int b;
        while (true) {
            b = this.read();
            if (b == -1) {
                return null;    //Unexpected EOF
            }
            if (b == '\n'){
                break;
            }
            if (b != '\r') {
                bos.write(b);
            }
        }
        return bos.toString("US-ASCII");
    }

	/**
	 * 
	 * @param pbis
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	protected int readFully(byte[] buffer) throws IOException {
		int readOffset = 0;
		int readRemaining = buffer.length;
		int readLast = 0;
		while (readRemaining > 0 && readLast != -1) {
			readRemaining -= readLast;
			readOffset += readLast;
			readLast = read(buffer, readOffset, readRemaining);
		}
		if (readRemaining > 0) {
			unread(buffer, 0, readOffset);
			readOffset = 0;
		}
		return readOffset;
	}

}
