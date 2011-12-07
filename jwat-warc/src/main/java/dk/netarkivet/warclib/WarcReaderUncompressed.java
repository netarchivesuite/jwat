package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import dk.netarkivet.common.ByteCountingPushBackInputStream;

/**
 * WARC Reader used on uncompressed files.
 *
 * @author nicl
 */
public class WarcReaderUncompressed extends WarcReader {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /** WARC file <code>InputStream</code>. */
    protected ByteCountingPushBackInputStream in;

    /**
     * This constructor is used to get random access to records.
     * The records are then accessed using the getNextRecordFrom methods
     * using a supplied input stream for each record.
     */
    WarcReaderUncompressed() {
    }

    /**
     * Construct reader using the supplied input stream.
     * This method is primarily for sequential access to records.
     * @param in WARC file input stream
     */
    WarcReaderUncompressed(ByteCountingPushBackInputStream in) {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        this.in = in;
    }

    @Override
    public boolean isCompressed() {
        return false;
    }

    @Override
    public void close() {
        if (warcRecord != null) {
            try {
                warcRecord.close();
            } catch (IOException e) { /* ignore */ }
            warcRecord = null;
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) { /* ignore */ }
            in = null;
        }
    }

    @Override
    public WarcRecord getNextRecord() throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (in == null) {
            throw new IllegalStateException(
                    "The inputstream 'in' is null");
        }
        warcRecord = WarcRecord.parseRecord(in);
        return warcRecord;
    }

    @Override
    public WarcRecord getNextRecordFrom(InputStream rin) throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        warcRecord = WarcRecord.parseRecord(
                new ByteCountingPushBackInputStream(rin, PUSHBACK_BUFFER_SIZE));
        return warcRecord;
    }

    @Override
    public WarcRecord getNextRecordFrom(InputStream rin, int buffer_size)
                                                        throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        warcRecord = WarcRecord.parseRecord(
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(rin, buffer_size),
                PUSHBACK_BUFFER_SIZE));
        return warcRecord;
    }

}
