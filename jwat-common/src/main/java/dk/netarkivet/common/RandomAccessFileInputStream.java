package dk.netarkivet.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * This class wraps a <code>RancomAccessFile</code> into a usable
 * <code>InputStream</code> which supports random re-position.
 * Re-positioning is done by using seek() on the <code>RancomAccessFile</code>
 * object. (@see RandomAccessFile#seek())
 *
 * @author nicl
 */
public class RandomAccessFileInputStream extends InputStream {

    /** Encapsulated <code>RandomAccessFile</code> used for stream data. */
    protected RandomAccessFile raf;

    /** Current mark position in file. */
    protected long mark_position = -1;

    /**
     * Create a new random access <code>InputStream</code> with repositioning
     * capabilities.
     * @param raf <code>RandomAccessFile</code> used for stream data
     */
    public RandomAccessFileInputStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            mark_position = raf.getFilePointer();
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if (mark_position == -1) {
            throw new IOException("Mark not set or is invalid");
        }
        raf.seek(mark_position);
    }

    @Override
    public int available() throws IOException {
        long avail = raf.length() - raf.getFilePointer();
        return (int) (Math.min(avail, Integer.MAX_VALUE));
    }

    @Override
    public long skip(long n) throws IOException {
        long skip = Math.min(n, raf.length() - raf.getFilePointer());
        raf.seek(raf.getFilePointer() + skip);
        return skip;
    }

    @Override
    public int read() throws IOException {
        return raf.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return raf.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

}
