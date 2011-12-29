package org.jwat.arc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jwat.common.ByteCountingPushBackInputStream;

/**
 * ARC Reader used on uncompressed files.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcReaderUncompressed extends ArcReader {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /** ARC file <code>ByteCountingPushBackInputStream</code>. */
    protected ByteCountingPushBackInputStream in;

    /**
     * This constructor is used to get random access to records.
     * The records are then accessed using the getNextRecordFrom methods
     * using a supplied input stream for each record.
     */
    ArcReaderUncompressed() {
    }

    /**
     * Construct reader using the supplied input stream.
     * This method is primarily for sequential access to records.
     * @param in ARC file input stream
     */
    ArcReaderUncompressed(ByteCountingPushBackInputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("The inputstream 'in' is null");
        }
        this.in = in;
    }

    @Override
    public boolean isCompressed() {
        return false;
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

    @Override
    @Deprecated
    public long getOffset() {
        return in.getConsumed();
    }

    @Override
    public ArcVersionBlock getVersionBlock() throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (in == null) {
            throw new IllegalStateException("The inputstream 'in' is null");
        }
        versionBlock = ArcVersionBlock.parseVersionBlock(in, this);
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
            throw new IllegalArgumentException("The inputstream 'vbin' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(vbin,
                        PUSHBACK_BUFFER_SIZE);
        versionBlock = ArcVersionBlock.parseVersionBlock(pbin, this);
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
        arcRecord = ArcRecord.parseArcRecord(in, versionBlock, this);
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
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException(
                    "The 'offset' is less than zero: " + offset);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(rin,
                        PUSHBACK_BUFFER_SIZE);
        arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock, this);
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
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        if (offset < 0) {
            throw new IllegalArgumentException(
                    "The 'offset' is less than zero: " + offset);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(rin, buffer_size),
                        PUSHBACK_BUFFER_SIZE);
        arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock, this);
        if (arcRecord != null) {
            arcRecord.startOffset = offset;
        }
        previousRecord = arcRecord;
        return arcRecord;
    }

}
