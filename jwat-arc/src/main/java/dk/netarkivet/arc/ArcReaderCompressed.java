package dk.netarkivet.arc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.gzip.GzipEntry;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * ARC Reader used on GZip compressed files.
 *
 * @author nicl
 */
public class ArcReaderCompressed extends ArcReader {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /** ARC file <code>GzipInputStream</code>. */
    protected GzipInputStream in;

    /** Buffer size, if any, to use on GZip entry <code>InputStream</code>. */
    protected int bufferSize;

    /**
     * This constructor is used to get random access to records.
     * The records are then accessed using the getNextRecordFrom methods
     * using a supplied input stream for each record.
     */
    ArcReaderCompressed() {
    }

    /**
     * Construct reader using the supplied input stream.
     * This method is primarily for sequential access to records.
     * @param in ARC file GZip input stream
     */
    ArcReaderCompressed(GzipInputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("The inputstream 'in' is null");
        }
        this.in = in;
    }

    /**
     * Construct object using supplied <code>GzipInputStream</code>.
     * This method is primarily for sequential access to records.
     * @param in GZip input stream
     * @param buffer_size buffer size used on entries
     */
    ArcReaderCompressed(GzipInputStream in, int buffer_size) {
        if (in == null) {
            throw new IllegalArgumentException("The inputstream 'in' is null");
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
        if (arcRecord != null) {
            try {
                arcRecord.close();
            } catch (IOException e) { /* ignore */ }
            arcRecord = null;
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) { /* ignore */ }
            in = null;
        }
    }

    /**
     * Get the current offset in the ARC <code>GzipInputStream</code>.
     * @return offset in ARC <code>InputStream</code>
     */
    @Override
    @Deprecated
    public long getOffset() {
        // FIXME Somehow this is not working properly with the GZip package.
        // Use GzipEntry.getOffset() for record offset.
        return in.getOffset();
    }

    @Override
    public ArcVersionBlock getVersionBlock() throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (in == null) {
            throw new IllegalStateException("The inputstream 'in' is null");
        }
        versionBlock = null;
        GzipEntry entry = in.getNextEntry();
        if (entry != null) {
            if (bufferSize > 0) {
                versionBlock = ArcVersionBlock.parseVersionBlock(
                        new ByteCountingPushBackInputStream(
                                new BufferedInputStream(
                                        in.getEntryInputStream(), bufferSize),
                                PUSHBACK_BUFFER_SIZE));
            } else {
                versionBlock = ArcVersionBlock.parseVersionBlock(
                        new ByteCountingPushBackInputStream(
                                in.getEntryInputStream(),
                                PUSHBACK_BUFFER_SIZE));
            }
        }
        if (versionBlock != null) {
            versionBlock.startOffset = entry.getOffset();
        }
        previousRecord = versionBlock;
        return versionBlock;
    }

    @Override
    public ArcVersionBlock getVersionBlock(InputStream vbin)
            throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (vbin == null) {
            throw new IllegalStateException("The inputstream 'vbin' is null");
        }
        versionBlock = null;
        GzipInputStream gzin = new GzipInputStream(vbin);
        GzipEntry entry = gzin.getNextEntry();
        if (entry != null) {
            if (bufferSize > 0) {
                versionBlock = ArcVersionBlock.parseVersionBlock(
                        new ByteCountingPushBackInputStream(
                                new BufferedInputStream(
                                        gzin.getEntryInputStream(), bufferSize),
                                PUSHBACK_BUFFER_SIZE));
            } else {
                versionBlock = ArcVersionBlock.parseVersionBlock(
                        new ByteCountingPushBackInputStream(
                                gzin.getEntryInputStream(),
                                PUSHBACK_BUFFER_SIZE));
            }
        }
        if (versionBlock != null) {
            versionBlock.startOffset = -1L;
        }
        previousRecord = versionBlock;
        return versionBlock;
    }

    @Override
    public ArcRecord getNextRecord() throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (in == null) {
            throw new IllegalStateException("The inputstream 'in' is null");
        }
        arcRecord = null;
        GzipEntry entry = in.getNextEntry();
        if (entry != null) {
            if (bufferSize > 0) {
                arcRecord = ArcRecord.parseArcRecord(
                        new ByteCountingPushBackInputStream(
                                new BufferedInputStream(
                                        in.getEntryInputStream(),
                                        bufferSize),
                                PUSHBACK_BUFFER_SIZE),
                        versionBlock);
            } else {
                arcRecord = ArcRecord.parseArcRecord(
                        new ByteCountingPushBackInputStream(
                                in.getEntryInputStream(), PUSHBACK_BUFFER_SIZE),
                        versionBlock);
            }
        }
        if (arcRecord != null) {
            arcRecord.startOffset = entry.getOffset();
        }
        previousRecord = arcRecord;
        return arcRecord;
    }

    @Override
    public ArcRecord getNextRecordFrom(InputStream rin, long offset)
            throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (rin == null) {
            throw new InvalidParameterException(
                    "The inputstream 'rin' is null");
        }
        if (offset < 0) {
            throw new InvalidParameterException(
                    "The 'offset' is less than zero: " + offset);
        }
        arcRecord = null;
        GzipInputStream gzin = new GzipInputStream(rin);
        GzipEntry entry = gzin.getNextEntry();
        if (entry != null) {
            ByteCountingPushBackInputStream pbin =
                    new ByteCountingPushBackInputStream(
                            gzin.getEntryInputStream(), PUSHBACK_BUFFER_SIZE);
            arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock);
        }
        if (arcRecord != null) {
            arcRecord.startOffset = offset;
        }
        previousRecord = arcRecord;
        return arcRecord;
    }

    @Override
    public ArcRecord getNextRecordFrom(InputStream rin, int buffer_size,
                                            long offset) throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
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
        if (offset < 0) {
            throw new InvalidParameterException(
                    "The 'offset' is less than zero: " + offset);
        }
        arcRecord = null;
        GzipInputStream gzin = new GzipInputStream(rin);
        GzipEntry entry = gzin.getNextEntry();
        if (entry != null) {
            ByteCountingPushBackInputStream pbin =
                    new ByteCountingPushBackInputStream(
                            new BufferedInputStream(
                                    gzin.getEntryInputStream(),
                                    buffer_size),
                            PUSHBACK_BUFFER_SIZE);
            arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock);
        }
        if (arcRecord != null) {
            arcRecord.startOffset = offset;
        }
        previousRecord = arcRecord;
        return arcRecord;
    }

}
