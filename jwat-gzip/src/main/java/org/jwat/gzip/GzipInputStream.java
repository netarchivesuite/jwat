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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import org.jwat.common.ByteCountingPushBackInputStream;

/**
 * An input stream for reading compressed data in the GZIP file format.
 * <p>
 * This implementation is compliant with
 * <a href="http://www.ietf.org/rfc/rfc1952.txt">RFC 1952</a> (GZIP
 * file format specification version 4.3) and supports multiple member
 * GZIP files.</p>
 * <p>
 * From RFC 1952, section 2.2:</p>
 * <blockquote>
 * A GZip file consists of a series of "members" (compressed data
 * sets). [...] The members simply appear one after another in the file,
 * with no additional information before, between, or after them.
 * <blockquote>
 */
public class GzipInputStream extends InflaterInputStream
{
    /** CRC-32 for uncompressed data. */
    protected CRC32 crc = new CRC32();

    /** Whether the main input stream has been closed. */
    private boolean closed = false;
    /** Whether the end of main input stream has been reached. */
    private boolean eos;
    /** The number of entries found so far, including the current entry. */
    private int entryCount = 0;
    /** The position in the underlying (compressed) input stream. */
    private long pos = 0L;
    /** The current entry. */
    private GzipEntry entry = null;
    /** The input stream to read the current entry data. */
    private final InputStream entryInputStream;
    /** Whether the end of the current entry input stream has been reached. */
    private boolean entryEof = true;                    // No active entry.

    /**
     * Check head of <code>PushBackInputStream</code> for a GZip magic number.
     * @param pbin <code>PushBackInputStream</code> with GZip entries
     * @return boolean indicating presence of a GZip magic number
     * @throws IOException if an io error occurs while examining head of stream
     */
    public static boolean isGziped(ByteCountingPushBackInputStream pbin) throws IOException {
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
     * Creates a new input stream with a default buffer size.
     * (default buffersize is 512 bytes)
     * @param  in   the input stream.
     * @throws ZipException if a GZip format error has occurred or the
     *                      compression method used is unsupported.
     * @throws IOException  if an I/O error has occurred.
     */
    public GzipInputStream(InputStream in) throws IOException {
        this(in, 512);
    }

    /**
     * Creates a new input stream with the specified buffer size.
     * @param  in     the input stream.
     * @param  size   the input buffer size.
     *
     * @throws ZipException if a GZip format error has occurred or the
     *                      compression method used is unsupported.
     * @throws IOException  if an I/O error has occurred.
     * @throws IllegalArgumentException if size is &lt;= 0.
     */
    public GzipInputStream(InputStream in, int size) throws IOException {
        super(new PushbackInputStream(in, size), new Inflater(true), size);

        this.entryInputStream = new FilterInputStream(this) {
            public void close() throws IOException {
                closeEntry();
            }
        };
    }

    /**
     * Reads the next GZip file entry and positions the stream at the
     * beginning of the entry data.
     * @return the next GZip file entry, or <code>null</code> if there
     * are no more entries.
     *
     * @throws ZipException if a GZip format error has occurred.
     * @throws IOException  if an I/O error has occurred.
     */
    public GzipEntry getNextEntry() throws IOException {
        closeEntry();
        entry = readHeader();
        entryEof = (entry == null);
        return entry;
    }

    /**
     * Closes the current GZip entry and positions the stream for
     * reading the next entry.
     *
     * @throws ZipException if a GZip format error has occurred.
     * @throws IOException  if an I/O error has occurred.
     */
    public void closeEntry() throws IOException {
        ensureOpen();
        if (! entryEof) {
            // Skip remaining entry data.
            byte[] tmpbuf = new byte[256];
        while (read(tmpbuf) != -1) { /* Keep on reading... */ }
        }
        entryEof = true;
    }

    /**
     * Returns an input stream on the current GZip entry data stream.
     * Once the entry data have been read, is it safe to call
     * {@link InputStream#close} on the returned stream.
     *
     * @return an input stream on the entry data.
     */
    public InputStream getEntryInputStream() {
        return entryInputStream;
    }

    /**
     * Returns 0 after EOF has reached for the current entry data,
     * otherwise always return 1.
     * <p>
     * Programs should not count on this method to return the actual
     * number of bytes that could be read without blocking.</p>
     * @return 1 before EOF and 0 after EOF has reached for current
     *         entry.
     *
     * @throws IOException  if an I/O error has occurred.
     */
    public int available() throws IOException {
        this.ensureOpen();
        return (entryEof) ? 0: 1;
    }

    /**
     * Reads uncompressed data into an array of bytes.  If
     * <code>len</code> is not zero, the method will block until some
     * input can be uncompressed; otherwise no bytes are read and
     * <code>0</code> is returned.
     * @param  buf   the buffer into which the data is read.
     * @param  off   the start offset in the destination buffer.
     * @param  len   the maximum number of bytes read.
     * @return the actual number of bytes read, or -1 if the end of the
     *         compressed input stream is reached.
     *
     * @throws ZipException if the compressed input data is corrupt.
     * @throws IOException  if an I/O error has occurred.
     * @throws NullPointerException if <code>buf</code> is
     *                              <code>null</code>.
     * @throws IndexOutOfBoundsException if <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>buf.length - off</code>.
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        this.ensureOpen();
        if ((eos) || (entryEof)) {
            return -1;
        }
        int n = super.read(buf, off, len);
        if (n == -1) {
            this.entryEof = true;
            if (this.readTrailer()) {
                this.eos = true;
            }
        }
        else {
            // this.pos += n;
            crc.update(buf, off, n);
        }
        return n;
    }

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream.
     *
     * @throws IOException if an I/O error has occurred.
     */
    public void close() throws IOException {
        if (!closed) {
            super.close();
            // Close the default inflater as we have no way to let the
            // superclass know that the default inflater impl. is being used.
            this.inf.end();
            closed = true;
        }
    }

    /**
     * Returns the current position in the input stream.
     *
     * @return the current position as an number of bytes.
     */
    public long getOffset() {
        return pos;
    }

    /**
     * Check to make sure that this stream has not been closed.
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Reads the next GZip member header and returns a GZip entry object
     * describing the member.
     *
     * @throws ZipException if the compressed input data is corrupt.
     * @throws IOException  if an I/O error has occurred.
     */
    private GzipEntry readHeader() throws IOException {
        if (eos) {
            return null;        // end of stream reached.
        }
        CheckedInputStream cis = new CheckedInputStream(in, crc);
        // Reset inflater for reading next GZip member.
        inf.reset();
        // Reset CRC to compute header CRC (CRC16).
        crc.reset();

        // Entry index and start offset
        entryCount++;
        long startOffset = getOffset();

        // Check magic number
        int header = readUShort(cis);
        if (header != GzipConstants.GZIP_MAGIC) {
            throw new ZipException("Not in GZIP format: invalid magic number");
        }

        // Read compression method
        int cm = readUByte(cis);
        if (cm != GzipConstants.CM_DEFLATE) {
            throw new ZipException("Invalid compression method: " + cm);
        }

        // Read flags
        int flg = readUByte(cis);

        // Read MTIME field
        long time = this.readUInt(cis);
        Date date = (time != 0L)? new Date(time * 1000L): null;

        // Read XFL field
        int xfl = readUByte(cis);
        if (xfl != 0) {
        }

        // Read OS field
        int os = readUByte(cis);

        // Check ASCII text flag
        boolean asciiFlag = ((flg & GzipConstants.FTEXT) == GzipConstants.FTEXT);

        // Read optional extra fields
        byte[] extraFields = null;
        if ((flg & GzipConstants.FEXTRA) == GzipConstants.FEXTRA) {
            int xlen = readUShort(cis);
            extraFields = readBytes(cis, xlen);
        }

        // Read optional file name
        String fileName = null;
        if ((flg & GzipConstants.FNAME) == GzipConstants.FNAME) {
            fileName = readString(cis);
        }

        // Read optional file comment
        String comment = null;
        if ((flg & GzipConstants.FCOMMENT) == GzipConstants.FCOMMENT) {
            comment = readString(cis);
        }

        // Check that no reserved flags is set
        int reservedFlags = (flg & GzipConstants.FRESERVED);

        // Check optional header CRC
        int readCrc16 = -1;
        int computedCrc16 = ((int)(this.crc.getValue())) & 0x0000ffff;
        if ((flg & GzipConstants.FHCRC) == GzipConstants.FHCRC) {
            readCrc16 = readUShort(cis);
        }

        // Create GZip entry object with header fields
        GzipEntry e = new GzipEntry(this.entryCount, startOffset, cm,
                                    xfl, date, fileName, os, comment,
                                    asciiFlag, extraFields, reservedFlags,
                                    readCrc16, computedCrc16);

        // Reset CRC to compute data CRC (CRC32).
        crc.reset();
        return e;
    }

    /**
     * Reads GZip member trailer and returns true if the end of stream
     * has been reached, false if there are more members (concatenated
     * GZip data set).
     * @return <code>true</code> if the end of stream has been reached;
     *         <code>false</code> if there are more members.
     *
     * @throws ZipException if the compressed input data is corrupt.
     * @throws IOException  if an I/O error has occurred.
     */
    private boolean readTrailer() throws IOException {
        int n = this.inf.getRemaining();
        if (n > 0) {
            ((PushbackInputStream)this.in).unread(this.buf, this.len - n, n);
        }
        long csize = this.inf.getBytesRead();
        long size  = this.inf.getBytesWritten();
        pos += csize;

        // Check member data CRC
        long readCrc32 = readUInt(this.in);
        long computedCrc32 = crc.getValue();
        // Check expanded size
        long readISize = readUInt(this.in);
        // rfc1952; ISIZE is the input size modulo 2^32.
        long computedISize = this.inf.getBytesWritten() & 0xffffffffL;
        if (entry != null) {
            entry.setSizes(csize, size);
            entry.setISize(readISize, computedISize);
            entry.setDataCrc(readCrc32, computedCrc32);
        }
        return (this.in.available() == 0);
    }

    /*
     * Reads unsigned integer in Intel byte order.
     */
    private long readUInt(InputStream in) throws IOException {
        long s = readUShort(in);
        return ((long)readUShort(in) << 16) | s;
    }

    /*
     * Reads unsigned short in Intel byte order.
     */
    private int readUShort(InputStream in) throws IOException {
        int b = readUByte(in);
        return (readUByte(in) << 8) | b;
    }

    /**
     * Read a null-terminated ISO 8859-1 encoded string from the input
     * stream.
     * @return the read string minus the ending null.
     *
     * @throws IOException  if an I/O error has occurred or the stream
     *         ended before the terminating null was reached.
     */
    private String readString(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
        int b;
        while ((b = readUByte(in)) != 0x00) {
            bos.write(b);
        }
        return new String(bos.toByteArray(), "ISO-8859-1");
    }

    /*
     * Reads unsigned byte.
     */
    private int readUByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        if (b < -1 || b > 255) {
            // Report on this.in, not argument in; see read{Header, Trailer}.
            throw new IOException(this.in.getClass().getName()
                        + ".read() returned value out of range -1..255: " + b);
        }
        pos++;
        return b;
    }

    /**
     * Reads the specified number of bytes from the input stream.
     * @param  n   the number of bytes to read.
     * @return an byte array filled with the read data.
     *
     * @throws IOException  if an I/O error has occurred or the stream
     *         ended before the <code>n</code> bytes could be read.
     */
    private byte[] readBytes(InputStream in, int n) throws IOException {
        byte[] tmpbuf = new byte[n];
        int l = in.read(tmpbuf, 0, n);
        if (l != n) {
            throw new EOFException();
        }
        pos += n;
        return tmpbuf;
    }

}
