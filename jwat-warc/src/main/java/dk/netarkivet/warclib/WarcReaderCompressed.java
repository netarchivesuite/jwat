package dk.netarkivet.warclib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.gzip.GzipEntry;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * WARC Reader used on GZip compressed files.
 *
 * @author nicl
 */
public class WarcReaderCompressed extends WarcReader {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /** WARC file <code>InputStream</code>. */
    protected GzipInputStream in;

    /** Buffer size, if any, to use on GZip entry <code>InputStream</code>. */
    protected int bufferSize;

    /**
     * This constructor is used to get random access to records.
     * The records are then accessed using the getNextRecordFrom methods
     * using a supplied input stream for each record.
     */
    WarcReaderCompressed() {
    }

    /**
     * Construct reader using the supplied input stream.
     * This method is primarily for sequential access to records.
     * @param in WARC file GZip input stream
     */
    WarcReaderCompressed(GzipInputStream in) {
        if (in == null) {
            throw new InvalidParameterException(
                    "The inputstream 'in' is null");
        }
        this.in = in;
    }

    /**
     * Construct object using supplied <code>GzipInputStream</code>.
     * This method is primarily for sequential access to records.
     * @param in GZip input stream
     * @param buffer_size buffer size used on entries
     */
    WarcReaderCompressed(GzipInputStream in, int buffer_size) {
        if (in == null) {
            throw new InvalidParameterException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new InvalidParameterException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        this.in = in;
        this.bufferSize = buffer_size;
    }

    @Override
    public boolean isCompressed() {
        return true;
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
            throw new IllegalStateException("The inputstream 'in' is null");
        }
        warcRecord = null;
        long offset = in.getOffset();
        GzipEntry entry = in.getNextEntry();
        if (entry != null) {
            if (bufferSize > 0) {
                warcRecord = WarcRecord.parseRecord(
                        new ByteCountingPushBackInputStream(
                                new BufferedInputStream(in.getEntryInputStream(),
                                bufferSize),
                        PUSHBACK_BUFFER_SIZE));
            }
            else {
                warcRecord = WarcRecord.parseRecord(
                        new ByteCountingPushBackInputStream(
                                in.getEntryInputStream(), PUSHBACK_BUFFER_SIZE));
            }
        }
        if (warcRecord != null) {
            warcRecord.offset = offset;
        }
        return warcRecord;
    }

    @Override
    public WarcRecord getNextRecordFrom(InputStream rin) throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (rin == null) {
            throw new InvalidParameterException(
                    "The inputstream 'rin' is null");
        }
        warcRecord = null;
        GzipInputStream gzin = new GzipInputStream(rin);
        GzipEntry entry = gzin.getNextEntry();
        if (entry != null) {
            warcRecord = WarcRecord.parseRecord(
                    new ByteCountingPushBackInputStream(
                            gzin.getEntryInputStream(), PUSHBACK_BUFFER_SIZE));
        }
        if (warcRecord != null) {
            warcRecord.offset = -1L;
        }
        return warcRecord;
    }

    @Override
    public WarcRecord getNextRecordFrom(InputStream rin, int buffer_size)
                                                        throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (rin == null) {
            throw new InvalidParameterException(
                    "The inputstream 'rin' is null");
        }
        if (buffer_size <= 0) {
            throw new InvalidParameterException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        warcRecord = null;
        GzipInputStream gzin = new GzipInputStream(rin);
        GzipEntry entry = gzin.getNextEntry();
        if (entry != null) {
            warcRecord = WarcRecord.parseRecord(
                    new ByteCountingPushBackInputStream(
                            new BufferedInputStream(
                                    gzin.getEntryInputStream(), buffer_size),
                    PUSHBACK_BUFFER_SIZE));
        }
        if (warcRecord != null) {
            warcRecord.offset = -1L;
        }
        return warcRecord;
    }

}
