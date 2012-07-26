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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.ISO8859_1;

/**
 * A writer for (multi-part) GZip files.
 * As per the GZIP file format specification version 4.3.
 * The class writes a GZip header. The compressed data and trailer is also
 * written by this class but is instigated by calling the write method on the
 * GZip entry itself.
 *
 * @author nicl
 */
public class GzipWriter {

    /** Default input buffer size. */
    public static final int DEFAULT_INPUT_BUFFER_SIZE = 8192;

    /** Output stream for GZip (multi-part) file. */
    protected OutputStream out;
    /** Deflater used to compress GZip entries. */
    protected Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
    /** Checksum object used to calculate CRC16 and CRC32 values. */
    protected CRC32 crc = new CRC32();
    /** Input buffer used to feed the deflater. */
    protected byte[] inputBytes;

    /** Compression level to be used by deflater. */
    protected int compressionLevel = Deflater.DEFAULT_COMPRESSION;

    /** ISO-8859-1 validating de-/encoder. */
    protected final ISO8859_1 iso8859_1 = new ISO8859_1();

    /** Current GZip entry object. */
    protected GzipEntry gzipEntry;

    /** Buffer used to read header.  */
    byte[] headerBytes = new byte[10];
    /** Buffer used to read the XLEN value. */
    byte[] xlenBytes = new byte[2];
    /** Buffer used to read the FNAME data. */
    byte[] fnameBytes = null;
    /** Buffer used to read the FCOMMENT data. */
    byte[] fcommentBytes = null;
    /** Buffer used to read the CRC16 value. */
    byte[] crc16Bytes = new byte[2];
    /** Buffer used to read trailer. */
    byte[] trailerBytes = new byte[8];

    /**
     * Construct a GZip writer with a default input buffer size of 1024.
     * @param out output stream of GZip file
     * @param in input stream of GZip file
     */
    public GzipWriter(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException("out is null!");
        }
        this.out = new BufferedOutputStream(out, DEFAULT_INPUT_BUFFER_SIZE);
        inputBytes = new byte[DEFAULT_INPUT_BUFFER_SIZE];
    }

    /**
     * Construct a GZip writer with the specified input buffer size.
     * @param out output stream of GZip file
     * @param buffer_size input buffer size to use
     */
    public GzipWriter(OutputStream out, int buffer_size) {
        if (out == null) {
            throw new IllegalArgumentException("out is null!");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException("buffer_size is less or equals to zero!");
        }
        this.out = new BufferedOutputStream(out, buffer_size);
        inputBytes = new byte[buffer_size];
    }

    /**
     * Release resources associated with this writer.
     * @throws IOException if an io error occurs while closing writer
     */
    public void close() throws IOException {
        if (gzipEntry != null) {
            gzipEntry.close();
            gzipEntry = null;
        }
        if (out != null) {
            out.flush();
            out = null;
        }
        if (def != null) {
            def.end();
            def = null;
        }
    }

    /**
     * Set compression level used by deflater. Only changed in deflater prior
     * to writing an entry header.
     * @param compressionLevel compression level
     * @throws IllegalArgumentException If CompressionLevel is invalid
     */
    public void setCompressionLevel(int compressionLevel) {
        if (compressionLevel == Deflater.DEFAULT_COMPRESSION
                || compressionLevel == Deflater.NO_COMPRESSION
                || (compressionLevel >= 1 && compressionLevel <= 9)) {
            this.compressionLevel = compressionLevel;
        }
        else {
            throw new IllegalArgumentException("Invalid compression level: " + compressionLevel);
        }
    }

    /**
     * Returns current compression level used by deflater.
     * @return current compression level
     */
    public int getCompressionLevel() {
        return compressionLevel;
    }

    /**
     * Write a GZip entry header and prepare for compressing input data.
     * @param entry GZip entry object
     * @throws IOException if an io error occurs while writing header
     */
    public void writeEntryHeader(GzipEntry entry) throws IOException {
        if (gzipEntry != null) {
            gzipEntry.close();
            gzipEntry = null;
        }
        if (entry == null) {
            throw new IllegalArgumentException("entry is null!");
        }
        crc.reset();
        def.reset();
        def.setLevel(compressionLevel);
        gzipEntry = entry;
        /*
         * Header.
         */
        entry.magic = GzipConstants.GZIP_MAGIC;
        if (entry.date != null) {
            entry.mtime = entry.date.getTime() / 1000;
        } else if (entry.mtime != 0) {
            entry.date = new Date(entry.mtime * 1000);
        }
        entry.xfl = 0;
        if (compressionLevel == 1) {
            entry.xfl |= GzipConstants.DEFLATE_XFL_FASTEST_COMPRESSION;
        } else if (compressionLevel == 9) {
            entry.xfl |= GzipConstants.DEFLATE_XFL_MAXIMUM_COMPRESSION;
        }
        entry.flg = 0;
        if (!GzipConstants.osIdxStr.containsKey((int)entry.os)) {
            entry.diagnostics.addWarning(
                    new Diagnosis(
                            DiagnosisType.UNKNOWN,
                            "Operating System",
                            Integer.toString(entry.os)
                )
            );
        }
        /*
         * FTEXT.
         */
        if (entry.bFText) {
            entry.flg |= GzipConstants.FLG_FTEXT;
        }
        /*
         * FEXTRA.
         */
        if (entry.extraBytes != null) {
            entry.flg |= GzipConstants.FLG_FEXTRA;
            entry.xlen = entry.extraBytes.length;
            xlenBytes[0] = (byte)(entry.xlen & 255);
            xlenBytes[1] = (byte)((entry.xlen >> 8) & 255);
        }
        /*
         * FNAME.
         */
        if (entry.fname != null) {
            entry.flg |= GzipConstants.FLG_FNAME;
            if (!iso8859_1.encode(entry.fname, "")) {
                entry.diagnostics.addWarning(
                        new Diagnosis(
                                DiagnosisType.INVALID_ENCODING,
                                "FName",
                                entry.fname,
                                "ISO-8859-1"
                            )
                        );
            }
            entry.fname = iso8859_1.decoded;
            fnameBytes = iso8859_1.encoded;
        }
        /*
         * FCOMMENT.
         */
        if (entry.fcomment != null) {
            entry.flg |= GzipConstants.FLG_FCOMMENT;
            if (!iso8859_1.encode(entry.fcomment, "\n")) {
                entry.diagnostics.addWarning(
                        new Diagnosis(
                                DiagnosisType.INVALID_ENCODING,
                                "FComment",
                                entry.fcomment,
                                "ISO-8859-1"
                            )
                        );
            }
            entry.fcomment = iso8859_1.decoded;
            fcommentBytes = iso8859_1.encoded;
        }
        /*
         * FHCRC.
         */
        if (entry.bFhCrc) {
            entry.flg |= GzipConstants.FLG_FHCRC;
        }
        /*
         * Header.
         */
        headerBytes[0] = (byte)(entry.magic & 255);
        headerBytes[1] = (byte)((entry.magic >> 8) & 255);
        headerBytes[2] = (byte)entry.cm;
        headerBytes[3] = (byte)entry.flg;
        headerBytes[4] = (byte)(entry.mtime & 255);
        headerBytes[5] = (byte)((entry.mtime >> 8) & 255);
        headerBytes[6] = (byte)((entry.mtime >> 16) & 255);
        headerBytes[7] = (byte)((entry.mtime >> 24) & 255);
        headerBytes[8] = (byte)entry.xfl;
        headerBytes[9] = (byte)entry.os;
        out.write(headerBytes);
        crc.update(headerBytes);
        if ((entry.flg & GzipConstants.FLG_FEXTRA) == GzipConstants.FLG_FEXTRA) {
            out.write(xlenBytes);
            out.write(entry.extraBytes);
            crc.update(xlenBytes);
            crc.update(entry.extraBytes);
        }
        if ((entry.flg & GzipConstants.FLG_FNAME) == GzipConstants.FLG_FNAME) {
            out.write(fnameBytes);
            out.write(0);
            crc.update(fnameBytes);
            crc.update(0);
        }
        if ((entry.flg & GzipConstants.FLG_FCOMMENT) == GzipConstants.FLG_FCOMMENT) {
            out.write(fcommentBytes);
            out.write(0);
            crc.update(fcommentBytes);
            crc.update(0);
        }
        if ((entry.flg & GzipConstants.FLG_FHCRC) == GzipConstants.FLG_FHCRC) {
            entry.comp_crc16 = ((int)crc.getValue()) & 0x0000ffff;
            entry.crc16 = entry.comp_crc16;
            crc16Bytes[0] = (byte)(entry.crc16 & 255);
            crc16Bytes[1] = (byte)((entry.crc16 >> 8) & 255);
            out.write(crc16Bytes);
        }
        /*
         * Prepare Entry InputStream.
         */
        crc.reset();
        entry.isize = 0;
        entry.writer = this;
        entry.bEof = false;
        entry.out = new GzipEntryOutputStream(this, gzipEntry);
    }

    /**
     * Computes the CRC32 and iSize values and Writes the GZip entry trailer.
     * @param entry GZip entry object
     * @throws IOException if an io error occurs while writing trailer
     */
    protected void writeTrailer(GzipEntry entry) throws IOException {
        entry.uncompressed_size = def.getBytesRead();
        entry.compressed_size = def.getBytesWritten();
        entry.comp_crc32 = (int)(crc.getValue() & 0xffffffff);
        entry.crc32 = entry.comp_crc32;
        entry.comp_isize = (int)(def.getBytesRead() & 0xffffffff);
        entry.isize = entry.comp_isize;
        trailerBytes[0] = (byte)(entry.crc32 & 255);
        trailerBytes[1] = (byte)((entry.crc32 >> 8) & 255);
        trailerBytes[2] = (byte)((entry.crc32 >> 16) & 255);
        trailerBytes[3] = (byte)((entry.crc32 >> 24) & 255);
        trailerBytes[4] = (byte)(entry.isize & 255);
        trailerBytes[5] = (byte)((entry.isize >> 8) & 255);
        trailerBytes[6] = (byte)((entry.isize >> 16) & 255);
        trailerBytes[7] = (byte)((entry.isize >> 24) & 255);
        out.write(trailerBytes);
        out.flush();
    }

    /**
     * Read data from input stream and fill buffer with compressed data.
     * @param in input stream with uncompressed data
     * @param b compressed data buffer
     * @param off offset in compressed data buffer
     * @param len length of compressed data to read
     * @return number of compressed bytes read
     * @throws DataFormatException if an error occurs in deflater
     * @throws IOException if an error occurs while compressing
     */
    protected int readCompressed(InputStream in, byte[] b, int off, int len) throws DataFormatException, IOException {
        int deflated = 0;
        while ((deflated = def.deflate(b, off, len)) == 0) {
            if (def.finished()) {
                return -1;
            } else if (def.needsInput()) {
                int read = in.read(inputBytes, 0, inputBytes.length);
                if (read != -1) {
                    def.setInput(inputBytes, 0, read);
                    crc.update(inputBytes, 0, read);
                } else {
                    def.finish();
                }
            } else {
                throw new DataFormatException("Deflater malfunction!");
            }
        }
        return deflated;
    }

    /**
     * Read data from <code>ByteBuffer</code> and fill buffer with compressed
     * data.
     * @param bb <code>ByteBuffer</code> with uncompressed data
     * @param b compressed data buffer
     * @param off offset in compressed data buffer
     * @param len length of compressed data to read
     * @param bFinish boolean indicating that there is no more data to compress
     * @return number of compressed bytes read
     * @throws DataFormatException if an error occurs in deflater
     * @throws IOException if an error occurs while compressing
     */
    protected int readCompressed(ByteBuffer bb, byte[] b, int off, int len, boolean bFinish) throws DataFormatException, IOException {
        int deflated = 0;
        while ((deflated = def.deflate(b, off, len)) == 0) {
            if (def.finished()) {
                return -1;
            } else if (def.needsInput()) {
                int lastInput = bb.remaining();
                if (lastInput > 0) {
                    bb.get(inputBytes, 0, lastInput);
                    def.setInput(inputBytes, 0, lastInput);
                    crc.update(inputBytes, 0, lastInput);
                } else {
                    if (bFinish) {
                        def.finish();
                    } else {
                        // Deflater needs more input.
                        return 0;
                    }
                }
            } else {
                throw new DataFormatException("Deflater malfunction!");
            }
        }
        return deflated;
    }

    /**
     * <code>OutputStream</code> to GZip compress data in a controlled fashion.
     *
     * @author nicl
     */
    protected static class GzipEntryOutputStream extends OutputStream {

        /** GZip reader used to inflate. */
        GzipWriter writer;

        /** Associated GZip entry. */
        GzipEntry gzipEntry;

        /** End of compressed file status. */
        boolean bEof = false;

        /** Small buffer used by the read() method. */
        byte[] singleByteArray = new byte[1];

        /** Buffer for feeding the deflater. */
        ByteBuffer bb = ByteBuffer.allocate(DEFAULT_INPUT_BUFFER_SIZE);

        /** Buffer used for compressed data. */
        byte[] compressedBytes = new byte[DEFAULT_INPUT_BUFFER_SIZE];

        /**
         * Construct ouput stream bound to a specific reader and entry.
         * @param reader GZip reader
         * @param gzipEntry GZip entry
         */
        public GzipEntryOutputStream(GzipWriter writer,
                                    GzipEntry gzipEntry) {
            this.writer = writer;
            this.gzipEntry = gzipEntry;
        }

        @Override
        public void close() throws IOException {
            if (!bEof) {
                bEof = true;
                try {
                    int deflated = 0;
                    while (deflated != -1) {
                        bb.flip();
                        deflated = writer.readCompressed(bb, compressedBytes, 0, compressedBytes.length, true);
                        bb.compact();
                        if (deflated > 0) {
                            writer.out.write(compressedBytes, 0, deflated);
                        }
                    }
                    writer.writeTrailer(gzipEntry);
                } catch (DataFormatException e) {
                    throw new IOException("Deflater malfunction!", e);
                } finally {
                    writer = null;
                    gzipEntry = null;
                    singleByteArray = null;
                }
            }
        }

        @Override
        public void flush() throws IOException {
            // Flush is performed in the close method.
        }

        @Override
        public void write(int b) throws IOException {
            singleByteArray[0] = (byte)b;
            write(singleByteArray, 0, 1);
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            int pLen;
            try {
                while (len > 0) {
                    if (bb.remaining() > 0) {
                        pLen = Math.min(bb.remaining(), len);
                        bb.put(b, off, pLen);
                        off += pLen;
                        len -= pLen;
                    } else {
                        bb.flip();
                        int compressed = writer.readCompressed(bb, compressedBytes, 0, compressedBytes.length, false);
                        bb.compact();
                        if (compressed > 0) {
                            writer.out.write(compressedBytes, 0, compressed);
                        }
                    }
                }
            } catch (DataFormatException e) {
                throw new IOException("Deflater malfunction!", e);
            }
        }

    }

}
