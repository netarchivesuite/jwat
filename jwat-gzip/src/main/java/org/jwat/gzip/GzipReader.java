/**
 * Java Web Archive Toolkit - Software to read and validate ARC, WARC
 * and GZip files. (http://jwat.org/)
 * Copyright 2011-2012 Netarkivet.dk (http://netarkivet.dk/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwat.gzip;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.ISO8859_1;

/**
 * A reader for (multi-part) GZip files. Validates header and CRC's.
 * Entries are read sequentially from the input stream. Random access is
 * supported indirectly if the used input stream supports this.
 * Compressed data is available through an uncompressing input stream wrapper.
 *
 * @author nicl
 */
public class GzipReader {

    /** Buffer size to use when read skipping. */
    public static final int SKIP_READ_BUFFER_SIZE = 8192;

    /** Read buffer used by the skip method. */
    protected byte[] skip_read_buffer = new byte[SKIP_READ_BUFFER_SIZE];

    /** Default input buffer size. */
    public static final int DEFAULT_INPUT_BUFFER_SIZE = 8192;

    /** Input stream of GZip (multi-part) file. */
    protected ByteCountingPushBackInputStream pbin;
    /** Inflater used to uncompress GZip entries. */
    protected Inflater inf = new Inflater( true );
    /** Checksum object used to calculate CRC16 and CRC32 values. */
    protected CRC32 crc = new CRC32();
    /** Last number of bytes read into the input buffer. */
    protected int lastInput;
    /** Input buffer used to feed the inflater. */
    protected byte[] inputBytes;

    /** ISO-8859-1 validating de-/encoder. */
    protected final ISO8859_1 iso8859_1 = new ISO8859_1();

    /** Compliance status for records parsed up to now. */
    protected boolean bIsCompliant = true;

    /** Validation errors and warnings. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /** Entries read. */
    protected int entries = 0;

    /** Entry offset, updated each time an entry is closed. */
    protected long startOffset = -1;

    /** Number of bytes consumed by this reader. */
    protected long consumed;

    /** Current GZip entry object. */
    protected GzipEntry gzipEntry;

    /** Partial GZip entry which could not be completely read. */
    public GzipEntry partialEntry;

    /** Buffer used to read header.  */
    protected byte[] headerBytes = new byte[10];
    /** Buffer used to read the XLEN value. */
    protected byte[] xlenBytes = new byte[2];
    /** Buffer used to read the FNAME data. */
    protected byte[] fnameBytes;
    /** Buffer used to read the FCOMMENT data. */
    protected byte[] fcommentBytes;
    /** Buffer used to read the CRC16 value. */
    protected byte[] crc16Bytes = new byte[2];
    /** Buffer used to read trailer. */
    protected byte[] trailerBytes = new byte[8];

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
        return (magicNumber == GzipConstants.GZIP_MAGIC);
    }

    /**
     * Construct a GZip reader with a default input buffer size of
     * DEFAULT_INPUT_BUFFER_SIZE.
     * @param in input stream of GZip file
     */
    public GzipReader(InputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("in is null!");
        }
        pbin = new ByteCountingPushBackInputStream(in, DEFAULT_INPUT_BUFFER_SIZE);
        inputBytes = new byte[DEFAULT_INPUT_BUFFER_SIZE];
    }

    /**
     * Construct a GZip reader with the specified input buffer size.
     * @param in input stream of GZip file
     * @param buffer_size input buffer size to use
     */
    public GzipReader(InputStream in, int buffer_size) {
        if (in == null) {
            throw new IllegalArgumentException("in is null!");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "buffer_size is less or equals to zero: " + buffer_size);
        }
        in = new BufferedInputStream(in, buffer_size);
        pbin = new ByteCountingPushBackInputStream(in, DEFAULT_INPUT_BUFFER_SIZE);
        inputBytes = new byte[DEFAULT_INPUT_BUFFER_SIZE];
    }

    /**
     * Release resources associated with this reader.
     * @throws IOException if an io error occurs while closing reader
     */
    public void close() throws IOException {
        if (gzipEntry != null) {
            gzipEntry.close();
            startOffset = pbin.getConsumed();
            gzipEntry = null;
        }
        if (inf != null) {
            inf.end();
            inf = null;
        }
        pbin = null;
    }

    /**
     * Returns a boolean indicating whether all entries parsed so far are compliant.
     * @return a boolean indicating whether all entries parsed so far are compliant
     */
    public boolean isCompliant() {
        return bIsCompliant;
    }

    /**
     * Returns the offset of the current entry or -1 if none have been read.
     * @return the offset of the current entry or -1
     */
    public long getStartOffset() {
        return startOffset;
    }

    /**
     * Returns the current offset in the input stream. Which could be anywhere
     * in the (multi-part) GZip file.
     * @return current offset in the input stream
     */
    public long getOffset() {
        if (pbin != null) {
            return pbin.getConsumed();
        } else {
            return consumed;
        }
    }

    /**
     * Get number of bytes consumed by this reader.
     * @return number of bytes consumed by this reader
     */
    public long getConsumed() {
        return consumed;
    }

    /**
     * Get the next GZip entry header and prepare the compressed data for
     * input stream retrieval.
     * @return GZip entry or null
     * @throws IOException if an io error occurs while reading entry
     */
    public GzipEntry getNextEntry() throws IOException {
        if (gzipEntry != null) {
            gzipEntry.close();
            gzipEntry = null;
        }
        int read = pbin.readFully(headerBytes);
        if (read == 10) {
            try {
                crc.reset();
                inf.reset();
                startOffset = pbin.getConsumed() - 10;
                gzipEntry = new GzipEntry();
                gzipEntry.reader = this;
                gzipEntry.startOffset = startOffset;
                /*
                 * Header.
                 */
                gzipEntry.magic = ((headerBytes[1] & 255) << 8) | (headerBytes[0] & 255);
                gzipEntry.cm = (short)(headerBytes[2] & 255);
                gzipEntry.flg = (short)(headerBytes[3] & 255);
                gzipEntry.mtime = ((headerBytes[7] & 255) << 24) | ((headerBytes[6] & 255) << 16) | ((headerBytes[5] & 255) << 8) | (headerBytes[4] & 255);
                gzipEntry.date = (gzipEntry.mtime != 0) ? new Date(gzipEntry.mtime * 1000) : null;
                gzipEntry.xfl = (short)(headerBytes[8] & 255);
                gzipEntry.os = (short)(headerBytes[9] & 255);
                crc.update(headerBytes);
                if (gzipEntry.magic != GzipConstants.GZIP_MAGIC) {
                    gzipEntry.diagnostics.addError(
                            new Diagnosis(
                                    DiagnosisType.INVALID_EXPECTED,
                                    "Magic Value",
                                    Integer.toHexString(gzipEntry.magic),
                                    Integer.toHexString(GzipConstants.GZIP_MAGIC)
                                )
                            );
                }
                if (gzipEntry.cm != GzipConstants.CM_DEFLATE) {
                    // Currently only the deflate compression method is supported in GZip.
                    gzipEntry.diagnostics.addError(
                            new Diagnosis(
                                    DiagnosisType.INVALID_EXPECTED,
                                    "Compression Method",
                                    Integer.toHexString(gzipEntry.cm),
                                    Integer.toHexString(GzipConstants.CM_DEFLATE)
                                )
                            );
                } else {
                    // Currently only the deflate compression method is supported in GZip.
                    // Check to see whether some xfl reserved bits have been used.
                    if ((gzipEntry.xfl & GzipConstants.DEFLATE_XLF_RESERVED) != 0) {
                        gzipEntry.diagnostics.addWarning(
                                new Diagnosis(
                                        DiagnosisType.RESERVED,
                                        "eXtra FLags",
                                        Integer.toHexString(gzipEntry.xfl & GzipConstants.DEFLATE_XLF_RESERVED)
                                    )
                                );
                    }
                    if ((gzipEntry.xfl & GzipConstants.DEFLATE_XFL_COMPRESSION_MASK) == GzipConstants.DEFLATE_XFL_COMPRESSION_MASK) {
                        gzipEntry.diagnostics.addError(
                                new Diagnosis(
                                        DiagnosisType.INVALID_DATA,
                                        "eXtra FLags",
                                        Integer.toHexString(gzipEntry.xfl & GzipConstants.DEFLATE_XFL_COMPRESSION_MASK)
                                    )
                                );
                    }
                }
                if ((gzipEntry.flg & GzipConstants.FLG_FRESERVED) != 0) {
                    gzipEntry.diagnostics.addWarning(
                            new Diagnosis(
                                    DiagnosisType.RESERVED,
                                    "FLaGs",
                                    Integer.toHexString(gzipEntry.flg & GzipConstants.FLG_FRESERVED)
                                )
                            );
                }
                if (!GzipConstants.osIdxStr.containsKey((int)gzipEntry.os)) {
                    gzipEntry.diagnostics.addWarning(
                            new Diagnosis(
                                    DiagnosisType.UNKNOWN,
                                    "Operating System",
                                    Integer.toString(gzipEntry.os)
                        )
                    );
                }
                /*
                 * FTEXT.
                 */
                if ((gzipEntry.flg & GzipConstants.FLG_FTEXT) == GzipConstants.FLG_FTEXT) {
                    gzipEntry.bFText = true;
                }
                /*
                 * FEXTRA.
                 */
                if ((gzipEntry.flg & GzipConstants.FLG_FEXTRA) == GzipConstants.FLG_FEXTRA) {
                    read = pbin.read(xlenBytes);
                    if (read == 2) {
                        gzipEntry.xlen = ((xlenBytes[1] & 255) << 8) | (xlenBytes[0] & 255);
                        if (gzipEntry.xlen > 0) {
                            gzipEntry.extraBytes = new byte[gzipEntry.xlen];
                            read = pbin.readFully(gzipEntry.extraBytes);
                            if (read != gzipEntry.xlen) {
                                throw new EOFException("Unexpected EOF!");
                            }
                        } else {
                            gzipEntry.extraBytes = new byte[0];
                        }
                    } else {
                        throw new EOFException("Unexpected EOF!");
                    }
                    crc.update(xlenBytes);
                    crc.update(gzipEntry.extraBytes);
                }
                /*
                 * FNAME.
                 */
                if ((gzipEntry.flg & GzipConstants.FLG_FNAME) == GzipConstants.FLG_FNAME) {
                    fnameBytes = readZeroTerminated();
                    if (fnameBytes == null) {
                        throw new EOFException("Unexpected EOF!");
                    }
                    if (!iso8859_1.decode(fnameBytes, "")) {
                        gzipEntry.diagnostics.addWarning(
                                new Diagnosis(
                                        DiagnosisType.INVALID_ENCODING,
                                        "FName",
                                        iso8859_1.decoded,
                                        "ISO-8859-1"
                                    )
                                );
                    }
                    gzipEntry.fname = iso8859_1.decoded;
                    crc.update(fnameBytes);
                    crc.update(0);
                }
                /*
                 * FCOMMENT.
                 */
                if ((gzipEntry.flg & GzipConstants.FLG_FCOMMENT) == GzipConstants.FLG_FCOMMENT) {
                    fcommentBytes = readZeroTerminated();
                    if (fcommentBytes == null) {
                        throw new EOFException("Unexpected EOF!");
                    }
                    if (!iso8859_1.decode(fcommentBytes, "\n")) {
                        gzipEntry.diagnostics.addWarning(
                                new Diagnosis(
                                        DiagnosisType.INVALID_ENCODING,
                                        "FComment",
                                        iso8859_1.decoded,
                                        "ISO-8859-1"
                                    )
                                );
                    }
                    gzipEntry.fcomment = iso8859_1.decoded;
                    crc.update(fcommentBytes);
                    crc.update(0);
                }
                /*
                 * FHCRC.
                 */
                if ((gzipEntry.flg & GzipConstants.FLG_FHCRC) == GzipConstants.FLG_FHCRC) {
                    read = pbin.read(crc16Bytes);
                    if (read == 2) {
                        gzipEntry.bFhCrc = true;
                        gzipEntry.crc16 = ((crc16Bytes[1] & 255) << 8) | (crc16Bytes[0] & 255);
                    } else {
                        throw new EOFException("Unexpected EOF!");
                    }
                }
                /*
                 * Computed crc16.
                 */
                gzipEntry.comp_crc16 = ((int)crc.getValue()) & 0x0000ffff;
                crc.reset();
                if (gzipEntry.crc16 != null && gzipEntry.crc16 != gzipEntry.comp_crc16) {
                    gzipEntry.diagnostics.addError(
                            new Diagnosis(
                                    DiagnosisType.INVALID_EXPECTED,
                                    "CRC16",
                                    Integer.toHexString(gzipEntry.crc16),
                                    Integer.toHexString(gzipEntry.comp_crc16)
                                )
                            );
                }
                /*
                 * Prepare Entry InputStream.
                 */
                lastInput = 0;
                gzipEntry.in = new GzipEntryInputStream(this, gzipEntry);
                // Compliance
                if (gzipEntry.diagnostics.hasErrors() || gzipEntry.diagnostics.hasWarnings()) {
                    gzipEntry.bIsCompliant = false;
                } else {
                    gzipEntry.bIsCompliant = true;
                }
                bIsCompliant &= gzipEntry.bIsCompliant;
                ++entries;
            } catch (EOFException e) {
                partialEntry = gzipEntry;
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "GZip file", "Unexpected EOF!"));
                bIsCompliant = false;
                gzipEntry = null;
            }
        } else {
            // Require one or more entries to be present.
            if (entries == 0) {
                diagnostics.addError(new Diagnosis(DiagnosisType.ERROR_EXPECTED, "GZip file", "One or more records"));
                bIsCompliant = false;
            }
            if (pbin.read() != -1) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "GZip file", "Unexpected trailling data!"));
                bIsCompliant = false;
            }
        }
        return gzipEntry;
    }

    /**
     * Read non-compressed zero terminated data.
     * @return zero terminated data
     * @throws IOException if an io error occurs while reading data
     */
    protected byte[] readZeroTerminated() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(32);
        int b;
        while ((b = pbin.read()) > 0) {
            out.write(b);
        }
        return (b != -1) ? out.toByteArray() : null;
    }

    /**
     * Read the non-compressed trailing 8 bytes with CRC32 and ISize values.
     * @param entry Gzip entry
     * @throws IOException if an io error occurs while reading data
     */
    protected void readTrailer(GzipEntry entry) throws IOException {
        int read = pbin.readFully(trailerBytes);
        entry.consumed = pbin.getConsumed() - entry.startOffset;
        entry.compressed_size = inf.getBytesRead();
        entry.uncompressed_size = inf.getBytesWritten();
        consumed += entry.consumed;
        entry.reader = null;
        if (read == 8) {
            entry.crc32 = ((trailerBytes[3] & 255) << 24) | ((trailerBytes[2] & 255) << 16) | ((trailerBytes[1] & 255) << 8) | (trailerBytes[0] & 255);
            entry.isize = ((trailerBytes[7] & 255) << 24) | ((trailerBytes[6] & 255) << 16) | ((trailerBytes[5] & 255) << 8) | (trailerBytes[4] & 255);
            entry.comp_crc32 = (int)(crc.getValue() & 0xffffffff);
            entry.comp_isize = (int)(inf.getBytesWritten() & 0xffffffff);
            if (entry.comp_crc32 != entry.crc32) {
                entry.diagnostics.addError(
                        new Diagnosis(
                                DiagnosisType.INVALID_EXPECTED,
                                "CRC32",
                                Integer.toHexString(entry.crc32),
                                Integer.toHexString(entry.comp_crc32)
                            )
                        );
            }
            if (entry.comp_isize != entry.isize) {
                entry.diagnostics.addError(
                        new Diagnosis(
                                DiagnosisType.INVALID_EXPECTED,
                                "ISize",
                                Long.toString(entry.isize),
                                Long.toString(entry.comp_isize)
                            )
                        );
            }
        } else {
            gzipEntry.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "GZip file", "Unexpected EOF!"));
            bIsCompliant = false;
            //throw new EOFException("Unexpected EOF!");
        }
        // Compliance
        if (gzipEntry.diagnostics.hasErrors() || gzipEntry.diagnostics.hasWarnings()) {
            gzipEntry.bIsCompliant = false;
        } else {
            gzipEntry.bIsCompliant = true;
        }
        bIsCompliant &= gzipEntry.bIsCompliant;
    }

    /**
     * Read and uncompress data into a buffer.
     * @param b destination buffer for uncompressed data
     * @param off offset in buffer
     * @param len length of uncompressed data to read
     * @return number of bytes uncompressed or -1
     * @throws DataFormatException if the compressed data is in an unknown format
     * @throws IOException if an io error occurs while reading data
     */
    protected int readInflated(byte[] b, int off, int len)
                                    throws DataFormatException, IOException {
        int inflated = 0;
        while((inflated = inf.inflate(b, off, len)) == 0) {
            if (inf.finished()) {
                return -1;
            } else if (inf.needsDictionary()) {
                gzipEntry.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "GZip file", "Unexpected EOF!"));
                bIsCompliant = false;
                throw new DataFormatException("Dictionary needed!");
            } else if (inf.needsInput()) {
                lastInput = pbin.read(inputBytes, 0, inputBytes.length);
                if (lastInput == -1) {
                    gzipEntry.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "GZip file", "Unexpected EOF!"));
                    bIsCompliant = false;
                    throw new DataFormatException("Data missing!");
                }
                inf.setInput(inputBytes, 0, lastInput);
            } else {
                gzipEntry.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "GZip file", "Unexpected EOF!"));
                bIsCompliant = false;
                throw new DataFormatException("Inflater malfunction!");
            }
        }
        return inflated;
    }

    /**
     * <code>InputStream</code> to expose GZip'ed data in a controlled fashion.
     *
     * @author nicl
     */
    protected static class GzipEntryInputStream extends InputStream {

        /** GZip reader used to inflate. */
        GzipReader reader;

        /** Associated GZip entry. */
        GzipEntry gzipEntry;

        /** End of uncompressed file status. */
        boolean bEof = false;

        /** Small buffer used by the read() method. */
        byte[] singleByteArray = new byte[1];

        /**
         * Construct input stream bound to a specific reader and entry.
         * @param reader GZip reader
         * @param gzipEntry GZip entry
         */
        public GzipEntryInputStream(GzipReader reader,
                                    GzipEntry gzipEntry) {
            this.reader = reader;
            this.gzipEntry = gzipEntry;
        }

        @Override
        public void close() throws IOException {
            while (!bEof) {
                skip(reader.skip_read_buffer.length);
            }
            reader = null;
            gzipEntry = null;
            singleByteArray = null;
            bEof = true;
        }

        @Override
        public int available() throws IOException {
            return !bEof ? 1 : 0;
        }

        @Override
        public int read() throws IOException {
            return read(singleByteArray, 0, 1) != -1 ? (singleByteArray[0] & 255) : -1;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (bEof) {
                return -1;
            }
            int read;
            try {
                read = reader.readInflated(b, off, len);
            } catch (DataFormatException e) {
                gzipEntry.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "GZip file", "Unexpected EOF!"));
                reader.bIsCompliant = false;
                throw new IOException(e);
            }
            if (read != -1) {
                reader.crc.update(b, off, read);
            }
            else {
                int remaining = reader.inf.getRemaining();
                if (remaining > reader.lastInput) {
                    throw new IOException("Remaining larger than lastInput!");
                }
                reader.pbin.unread(reader.inputBytes, reader.lastInput - remaining,
                                   remaining);
                bEof = true;
                reader.readTrailer(gzipEntry);
            }
            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            if (bEof) {
                return 0;
            }
            long remaining = n;
            long skipped = 0;
            long readLast = 0;
            while (remaining > 0 && readLast != -1) {
                remaining -= readLast;
                skipped += readLast;
                if (remaining > 0) {
                    readLast = read(reader.skip_read_buffer, 0,
                            (int) Math.min(remaining, SKIP_READ_BUFFER_SIZE));
                }
            }
            return skipped;
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

    }

}
