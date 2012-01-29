package org.jwat.common;

import java.io.IOException;
import java.io.StringReader;

/**
 * <code>StringReader</code> that keeps tracks of the number of consumed chars
 * at any given time.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class CharCountingStringReader extends StringReader {

    /** Number of consumed bytes since the beginning of the stream. */
    protected long consumed = 0;

    /** Byte counter which can also be changed. */
    protected long counter = 0;

    /**
     * Creates a new <code>StringReader</code> that keeps track of
     * consumed characters.
     * @param str Arbitrary string.
     */
    public CharCountingStringReader(String str) {
        super(str);
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

    /**
     * Consumed length getter.
     * @return the consumed length
     */
    public long getConsumed() {
        return consumed;
    }

    /**
     * Change the counter value.
     * Useful for reading zero indexed relative data.
     * @param bytes new counter value
     */
    public void setCounter(long bytes) {
        counter = bytes;
    }

    /**
     * Retrieve the current counter value.
     * @return current counter value
     */
    public long getCounter() {
        return counter;
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c != -1) {
            ++consumed;
            ++counter;
        }
        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int n = super.read(cbuf, off, len);
        if (n > 0) {
            consumed += n;
            counter += n;
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        n = super.skip(n);
        consumed += n;
        counter += n;
        return n;
    }

    /**
     * Reads a line defined as characters read until encountering a
     * <code>LF</code> or EOF.
     * @return Line read from buffered <code>StringReader</code>
     * @throws IOException if an io error occurs while reading line
     */
    public String readLine() throws IOException {
        StringBuilder buf = new StringBuilder();
    	int c;
        while (true) {
        	c = read();
        	if (c == -1) {
        		return null;
        	}
        	if (c == '\n') {
        		break;
        	}
        	if (c != '\r') {
                buf.append((char) c);
        	}
        }
        return buf.toString();
    }

}
