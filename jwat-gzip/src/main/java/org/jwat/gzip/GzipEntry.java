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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.DataFormatException;

import org.jwat.common.Diagnosis;
import org.jwat.common.Diagnostics;

/**
 * GZip entry container.
 *
 * @author nicl
 */
public class GzipEntry {

	/** Starting offset of this entry in the input stream from whence it came.
	 */
	public long startOffset = -1L;

	/** Leading magic. */
    public int magic;
    /** Compression mode. */
    public short cm;
    /** Flags. */
    public short flg;
    /** Date in seconds. */
    public long mtime;
    /** Date converted. */
    public Date date;
    /** Compression flags. */
    public short xfl;
    /** Operating system. */
    public short os;

    /** Is compressed data ASCII. */
    public boolean bFText;

    /** Optional FEXTRA XLEN values. */
    public Integer xlen;
    /** Optional FEXTRA data. */
    public byte[] extraBytes;
    /** Optional FNAME in iso-8859-1 format. */
    public String fname;
    /** Optional FCOMMENT in iso-8859-1 format (new lines should be LF only).*/
    public String fcomment;
    /** Optional CRC16 if present. */
    public Integer crc16;

    /** Trailing CRC32. */
    public int crc32;
    /** Trailing uncompressed size modulo 2^32. */
    public long isize;

    /** CRC16 Calculate(d). */
    public boolean bFhCrc;

    /** Computed CRC16. */
    public int comp_crc16;
    /** Computed CRC32. */
    public int comp_crc32;
    /** Computed CRC32. */
    public long comp_isize;

    /** Input stream to read uncompressed data. */
    protected InputStream in;

    /** GZip writer responsible for writing this entry. */
    protected GzipWriter writer;

    /** End of uncompressed file status. */
    protected boolean bEof = false;

    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /**
     * Release resources associated with this record.
     * @throws IOException
     */
    public void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
        }
        if (writer != null) {
            writer = null;
        }
    }

    public long getStartOffset() {
    	return startOffset;
    }

    /**
     * Returns an input stream which must be used to read the compressed data
     * after it has been uncompressed.
     * @return input stream to read uncompressed data
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * Read from input stream and write compressed data on output stream.
     * @param in input stream with uncompressed data
     * @throws IOException if an io error occurs while transferring
     */
    public void writeFrom(InputStream in) throws IOException {
        if (writer == null) {
            throw new IllegalArgumentException("Not in writing state!");
        }
        if (in == null) {
            throw new IllegalArgumentException("in is null!");
        }
        byte[] tmpBuf = new byte[512];
        int read;
        try {
            while ((read = writer.readCompressed(in, tmpBuf, 0, tmpBuf.length)) != -1) {
                writer.out.write(tmpBuf, 0, read);
            }
        }
        catch (DataFormatException e) {
            throw new IOException(e);
        }
        comp_crc32 = ((int)writer.crc.getValue()) & 0xffffffff;
        crc32 = comp_crc32;
        comp_isize = writer.def.getBytesRead();
        isize = comp_isize;
        writer.writeTrailer(this);
        bEof = true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(128);
        sb.append("magic: " + magic);
        sb.append("gzipmagic: " + GzipConstants.GZIP_MAGIC);
        sb.append("cm: " + cm);
        sb.append("flg: " + flg);
        sb.append("mtime: " + mtime);
        sb.append("mtime(date): " + date);
        sb.append("xlf: " + xfl);
        sb.append("os: " + os);
        if (xlen != null) {
            sb.append("xlen: " + xlen);
        }
        if (fname != null) {
            sb.append("fname: " + fname);
        }
        if (fcomment != null) {
            sb.append("fcomment: " + fcomment);
        }
        if (crc16 != null) {
            sb.append("crc16: " + crc16);
        }
        sb.append("comp_crc16: " + comp_crc16);
        sb.append("crc32: " + crc32);
        sb.append("isize: " + isize);
        sb.append("comp_crc32: " + comp_crc32);
        return sb.toString();
    }

}
