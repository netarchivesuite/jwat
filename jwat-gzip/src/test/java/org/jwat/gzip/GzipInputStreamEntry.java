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

import java.util.Date;

/**
 * A GZip file entry.
 */
public class GzipInputStreamEntry
{
    /* Error flags */
    public static final int INVALID_EXTRA_FLAGS = 1;
    public static final int INVALID_OPERATING_SYSTEM = 2;
    public static final int INVALID_RESERVED_FLAGS = 4;
    public static final int INVALID_ISIZE = 8;
    public static final int INVALID_CRC16 = 16;
    public static final int INVALID_CRC32 = 32;

    protected int index;
    protected long offset;

    protected boolean asciiFlag;

    protected int method;
    protected Date date;
    protected int extraFlags;
    protected int os;

    /* Non immutable fields: use defensive copy in getter method. */
    protected byte[] extraFields;

    protected String fileName;

    protected String comment;

    protected long readCrc16;
    protected long computedCrc16;


    protected int errors = 0;
    protected long size  = -1L;
    protected long csize = -1L;
    protected long readISize = -1L;
    protected long computedISize = -1;
    protected long readCrc32 = -1L;
    protected long computedCrc32 = -1L;


    /** Zero argument constructor. */
    public GzipInputStreamEntry()
    {
        super();
    }

    /** Creates a new GzipEntry object. */
    GzipInputStreamEntry(int index, long offset,
                     int method, int extraFlags,
                     Date date, String fileName, int os,
                     String comment, boolean asciiFlag, byte[] extraFields,
                     int reservedFlags, long readCrc16, long computedCrc16) {
        this.index          = index;
        this.offset         = offset;
        this.method         = method;
        this.extraFlags     = extraFlags;
        this.date           = date;
        this.fileName       = fileName;
        this.os             = os;
        this.comment        = comment;
        this.asciiFlag      = asciiFlag;
        this.extraFields    = extraFields;
        this.readCrc16      = readCrc16;
        this.computedCrc16  = computedCrc16;

        if (reservedFlags != 0) {
            this.addErrors(INVALID_RESERVED_FLAGS);
        }
        /*
        if ((os != null) && (! os.isValid())) {
            this.addErrors(INVALID_OPERATING_SYSTEM);
        }
        if ((extraFlags != null) && (! extraFlags.isValid())) {
            this.addErrors(INVALID_EXTRA_FLAGS);
        }
        */
        if ((readCrc16 > 0L) && (readCrc16 != computedCrc16)) {
            this.addErrors(INVALID_CRC16);
        }
    }

    /**
     * Returns the index of this entry in the GZip file.
     * @return the index (0-based) of the entry.
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Returns the offset of the beginning of this entry in the GZip
     * file.
     * @return the offset of this entry, as a number of bytes from the
     *         start of the GZip file.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the compression method used for this entry.
     * @return the compression method.
     */
    public int getCompressionMethod() {
        return method;
    }

    /**
     * Returns the most recent modification time of the original
     * compressed file as a number of milliseconds since 00:00:00 GMT,
     * Jan. 1, 1970.
     * @return last modification time of the compressed file or
     *         <code>-1</code> if none is present in the GZip header.
     */
    public long getTime() {
        return (date != null) ? date.getTime() : -1L;
    }

    /**
     * Returns the most recent modification time of the original
     * compressed file as a {@link Date} object.
     * @return last modification date of the compressed file or
     *         <code>null</code> if none is present in the GZip header.
     */
    public Date getDate() {
        return (date != null) ? new Date(date.getTime()) : null;
    }

    /**
     * Returns the compression type indicated in the extra flags of
     * the member header.
     * @return the compression type or <code>null</code> if absent.
     *
     * @see    #isExtraFlagsValid
     */
    public int getCompressionFlags() {
        return extraFlags;
    }

    /**
     * Returns the operating system on which the GZip member was
     * compressed.
     * @return the operating system.
     *
     * @see    #isOperatingSystemValid
     */
    public int getOperatingSystem() {
        return os;
    }

    /**
     * Returns the extra fields of the GZip member header.
     * @return the extra fields as an array of bytes or
     *         <code>null</code> if none are present.
     */
    public byte[] getExtra() {
        int l = extraFields.length;
        byte[] copy = new byte[l];
        System.arraycopy(extraFields, 0, copy, 0, l);
        return copy;
    }

    /**
     * Returns the name of the compressed file.
     * @return the name of the compressed file or <code>null</code> if
     *         the compressed data did not come from a file.
     */
    public String getName() {
        return fileName;
    }

    /**
     * Returns the GZip member comment.
     * @return the GZip member comment or <code>null</code> if absent.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns whether the GZip member is announced as containing only
     * ASCII text.
     * @return the ASCII text flag from the member header.
     */
    public boolean isAscii() {
        return asciiFlag;
    }

    /**
     * Returns the CRC16 read from the GZip member header.
     * @return the CRC16 of the GZip member header or <code>-1</code>
     *         if absent.
     *
     * @see    #getComputedHeaderCrc
     * @see    #isHeaderCrcValid
     */
    public long getHeaderCrc() {
        return readCrc16;
    }

    /**
     * Returns the CRC16 computed from the GZip member header.
     * @return the computed CRC16.
     *
     * @see    #getHeaderCrc
     * @see    #isHeaderCrcValid
     */
    public long getComputedHeaderCrc() {
        return computedCrc16;
    }

    /**
     * Returns the data CRC (a.k.a. CRC32) read from the GZip member
     * trailer.
     * @return the CRC32 of the GZip member trailer or <code>-1</code>
     *         if the member trailer has not yet been read.
     *
     * @see    #getComputedDataCrc
     * @see    #isDataCrcValid
     */
    public long getDataCrc() {
        return readCrc32;
    }

    /**
     * Returns the data CRC (a.k.a. CRC32) computed from the read
     * member data.
     * @return the computed CRC32 or <code>-1</code> if the member
     *         trailer has not yet been read.
     *
     * @see    #getDataCrc
     * @see    #isDataCrcValid
     */
    public long getComputedDataCrc() {
        return computedCrc32;
    }

    /**
     * Returns the (computed) uncompressed size of the member data.
     * @return the uncompressed size of the member data or
     *         <code>-1</code> if the member trailer has not yet
     *         been read.
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the (computed) compressed size of the member data.
     * @return the compressed size of the member data or
     *         <code>-1</code> if the member trailer has not yet
     *         been read.
     */
    public long getCompressedSize() {
        return csize;
    }

    /**
     * Returns the ISIZE of the GZip member trailer, i.e. the announced
     * compressed size of the member data modulo 32.
     * @return the ISIZE value of the member trailer or
     *         <code>-1</code> if the member trailer has not yet
     *         been read.
     *
     * @see    #isISizeValid
     */
    public long getISize() {
        return readISize;
    }

    /**
     * Returns the (computed) ISIZE of the GZip member trailer, i.e.
     * the compressed size of the member data modulo 32.
     * @return the computed ISIZE value of the member data or
     *         <code>-1</code> if the member trailer has not yet
     *         been read.
     *
     * @see    #isISizeValid
     */
    public long getComputedISize() {
        return computedISize;
    }

    /**
     * Returns whether this entry is compliant with the rules listed
     * in section 2.3.1.2 of RFC 1952.
     * <blockquote>
     * Compliant decompressors shall only check ID1 + ID2 (magic
     * number), CM (compression method) and unset reserved flags. They
     * may ignore FTEXT and OS header fields.
     * <blockquote>
     * <p>
     * As no GzipEntry instance can be created with invalid magic number
     * or unsupported compression method (deflate), this method only
     * checks that no reserved flag is set.</p>
     *
     * @return <code>true</code> if the entry is compliant with RFC 1952
     *         rules; <code>false</code> otherwise.
     */
    public boolean isCompliant() {
        return isReservedFlagsValid();
    }

    /**
     * Returns whether this entry is valid, i.e. is compliant and no
     * error (invalid CRC or ISize) was found.
     *
     * @return <code>true</code> if the entry is valid;
     *         <code>false</code> otherwise.
     */
    public boolean isValid() {
        return (errors == 0);
    }

    /**
     * Returns whether the header extra flags are valid, i.e. only the
     * the compression type flags are set at most.
     * @return <code>true</code> if the header extra flags are valid;
     *         <code>false</code> otherwise.
     *
     * @see    #getCompressionFlags
     */
    public boolean isExtraFlagsValid() {
        return (! isErrorSet(INVALID_EXTRA_FLAGS));
    }

    /**
     * Returns whether the operating system value is valid.
     * @return <code>true</code> if the operating system value is valid;
     *         <code>false</code> otherwise.
     *
     * @see    #getOperatingSystem
     */
    public boolean isOperatingSystemValid() {
        return (! isErrorSet(INVALID_OPERATING_SYSTEM));
    }

    /**
     * Returns whether the header reserved flags are valid
     * (i.e. not set).
     * @return <code>true</code> if the header reserved flags are valid;
     *         <code>false</code> otherwise.
     */
    public boolean isReservedFlagsValid() {
        return (! isErrorSet(INVALID_RESERVED_FLAGS));
    }

    /**
     * Returns whether the read data ISIZE and the computed one
     * are equals.
     * @return <code>true</code> if the read ISIZE and the computed one
     *         are equals; <code>false</code> otherwise.
     *
     * @see    #getISize
     */
    public boolean isISizeValid() {
        return (! isErrorSet(INVALID_ISIZE));
    }

    /**
     * Returns whether the read header CRC (a.k.a. CRC16) and the
     * computed one are equals.
     * @return <code>true</code> if the read CRC16) and the computed one
     *         are equals; <code>false</code> otherwise.
     *
     * @see    #getHeaderCrc
     * @see    #getComputedHeaderCrc
     */
    public boolean isHeaderCrcValid() {
        return (! isErrorSet(INVALID_CRC16));
    }

    /**
     * Returns whether the read data CRC (a.k.a. CRC32) and the computed
     * one are equals.
     * @return <code>true</code> if the read CRC32) and the computed one
     *         are equals; <code>false</code> otherwise.
     *
     * @see    #getDataCrc
     * @see    #getComputedDataCrc
     */
    public boolean isDataCrcValid() {
        return (! isErrorSet(INVALID_CRC32));
    }

    /* package */ void setSizes(long csize, long size) {
        this.csize = csize;
        this.size  = size;
    }

    /* package */ void setISize(long readISize, long computedISize) {
        this.readISize = readISize;
        this.computedISize = computedISize;
        if (readISize != computedISize) {
            this.addErrors(INVALID_ISIZE);
        }
    }

    /* package */ void setDataCrc(long readCrc32, long computedCrc32) {
        this.readCrc32 = readCrc32;
        this.computedCrc32 = computedCrc32;
        if (readCrc32 != computedCrc32) {
            this.addErrors(INVALID_CRC32);
        }
    }

    private void addErrors(int errors) {
        errors |= errors;
    }

    private boolean isErrorSet(int errorMask) {
        return ((errors & errorMask) != 0);
    }

}
