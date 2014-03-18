package org.jwat.archive.common;

import java.io.IOException;
import java.util.Arrays;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Scheme;

public class ReaderFactoryAbstract {

    /** GZip header magic number. */
    protected static final int GZIP_MAGIC = 0x8b1f;

    /** An ARC version block starts with this string. */
    protected static final String ARC_MAGIC_HEADER = "filedesc:";

    /**
     * A WARC header block starts with this string including trailing version
     * information.
     * */
    protected static final String WARC_MAGIC_HEADER = "WARC/";

    /**
     * Private constructor to enforce factory methods.
     */
    protected ReaderFactoryAbstract() {
    }

    /**
     * Check head of <code>PushBackInputStream</code> for a GZip magic number.
     * The state of the <code>PushBackInputStream</code> is the same after the
     * call as before the call.
     * @param pbin <code>PushBackInputStream</code> with GZip entries
     * @return boolean indicating presence of a GZip magic number
     * @throws IOException if an io error occurs while examining head of stream
     */
    public static boolean isGzipped(ByteCountingPushBackInputStream pbin) throws IOException {
        if (pbin == null) {
            throw new IllegalArgumentException("'pbin'is null!");
        }
        byte[] magicBytes = new byte[2];
        int magicNumber = 0xdeadbeef;
        // Look for the leading 2 magic bytes in front of every valid GZip entry.
        int read = pbin.readFully(magicBytes);
        if (read == 2) {
            magicNumber = ((magicBytes[1] & 255) << 8) | (magicBytes[0] & 255);
        }
        if (read > 0) {
            pbin.unread(magicBytes, 0, read);
        }
        return (magicNumber == GZIP_MAGIC);
    }

    /**
     * Check head of <code>PushBackInputStream</code> for an ARC file identifier.
     * The identifier for ARC files is "filedesc:" in the beginning.
     * @param pbin <code>PushBackInputStream</code> with an ARC version block
     * @return boolean indicating presence of an ARC file identifier
     * @throws IOException if an i/o error occurs while examining head of stream
     */
    public static boolean isArcFile(ByteCountingPushBackInputStream pbin) throws IOException {
        byte[] streamBytes = new byte[ARC_MAGIC_HEADER.length()];
        byte[] arcBytes = ARC_MAGIC_HEADER.getBytes();
        // Look for an ARC file identifier in the beginning of the stream.
        pbin.peek(streamBytes);
        return (Arrays.equals(arcBytes, streamBytes));
    }

    /**
     * Check head of <code>PushBackInputStream</code> for an ARC record identifier.
     * The identifier for ARC files is "filedesc:" in the beginning.
     * @param pbin <code>PushBackInputStream</code> with an ARC version block
     * @return boolean indicating presence of an ARC file identifier
     * @throws IOException if an i/o error occurs while examining head of stream
     */
    public static boolean isArcRecord(ByteCountingPushBackInputStream pbin) throws IOException {
        byte[] streamBytes = new byte[32];
        // Look for a valid scheme in the beginning of the stream.
        pbin.peek(streamBytes);
        return Scheme.startsWithScheme(streamBytes);
    }

    /**
     * Check head of <code>PushBackInputStream</code> for a WARC file identifier.
     * The identifier for WARC files is "WARC/" in the beginning.
     * @param pbin <code>PushBackInputStream</code> with WARC records
     * @return boolean indicating presence of a WARC file identifier
     * @throws IOException if an i/o error occurs while examining head of stream
     */
    public static boolean isWarcFile(ByteCountingPushBackInputStream pbin) throws IOException {
        return isWarcRecord(pbin);
    }

    /**
     * Check head of <code>PushBackInputStream</code> for a WARC record identifier.
     * The identifier for WARC records is "WARC/" in the beginning.
     * @param pbin <code>PushBackInputStream</code> with WARC records
     * @return boolean indicating presence of a WARC magic number
     * @throws IOException if an i/o error occurs while examining head of stream
     */
    public static boolean isWarcRecord(ByteCountingPushBackInputStream pbin) throws IOException {
        byte[] streamBytes = new byte[WARC_MAGIC_HEADER.length()];
        byte[] warcBytes = WARC_MAGIC_HEADER.getBytes();
        // Look for the leading magic bytes in front of every valid WARC record.
        pbin.peek(streamBytes);
        return (Arrays.equals(warcBytes, streamBytes));
    }

}
