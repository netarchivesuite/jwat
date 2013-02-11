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
package org.jwat.warc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.gzip.GzipReader;

/**
 * Factory used for creating <code>WarcReader</code> instances.
 * The general <code>getReader</code> methods will auto-detect Gzip'ed data
 * and return the appropriate <code>WarcReader</code> instances.
 * The other factory methods can be used to return specific
 * <code>WarcReader</code> instances for compressed or uncompressed records.
 * Readers are available for both sequential and random reading of records.
 * Use of buffered methods and/or buffering speeds up the reader considerably.
 *
 * @author nicl
 */
public class WarcReaderFactory {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 32;

    /**
     * Private constructor to enforce factory method.
     */
    protected WarcReaderFactory() {
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
        byte[] streamBytes = new byte[WarcConstants.WARC_MAGIC_HEADER.length()];
        byte[] warcBytes = WarcConstants.WARC_MAGIC_HEADER.getBytes();
        // Look for the leading magic bytes in front of every valid WARC record.
        pbin.peek(streamBytes);
        return (Arrays.equals(warcBytes, streamBytes));
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on
     * GZip auto detection.
     * @param in WARC File represented as <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return appropriate <code>WarcReader</code> based on data read from
     * <code>InputStream</code>
     * @throws IOException if an i/o exception occurs during initialization
     */
    public static WarcReader getReader(InputStream in, int buffer_size)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: " +
                    buffer_size);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(in, buffer_size),
                PUSHBACK_BUFFER_SIZE);
        if (GzipReader.isGzipped(pbin)) {
            return new WarcReaderCompressed(new GzipReader(pbin),
                                            buffer_size);
        }
        return new WarcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on
     * GZip auto detection.
     * @param in WARC File represented as <code>InputStream</code>
     * @return appropriate <code>WarcReader</code> based on data read from
     * <code>InputStream</code>
     * @throws IOException if an i/o exception occurs during initialization
     */
    public static WarcReader getReader(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(in, PUSHBACK_BUFFER_SIZE);
        if (GzipReader.isGzipped(pbin)) {
            return new WarcReaderCompressed(new GzipReader(pbin));
        }
        return new WarcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>WarcReader</code> without any associated
     * <code>InputStream</code> for random access to uncompressed records.
     * @return <code>WarcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     */
    public static WarcReaderUncompressed getReaderUncompressed() {
        return new WarcReaderUncompressed();
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * primarily for random access to uncompressed records.
     * @param in WARC File represented as <code>InputStream</code>
     * @return <code>WarcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     * @throws IOException i/o exception while initializing reader
     */
    public static WarcReaderUncompressed getReaderUncompressed(InputStream in)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(in, PUSHBACK_BUFFER_SIZE);
        return new WarcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code> primarily for random
     * access to uncompressed records.
     * @param in WARC File represented as <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return <code>WarcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     * @throws IOException i/o exception while initializing reader
     */
    public static WarcReaderUncompressed getReaderUncompressed(InputStream in,
                                        int buffer_size) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: " +
                    buffer_size);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(in, buffer_size),
                PUSHBACK_BUFFER_SIZE);
        return new WarcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>WarcReader</code> without any associated
     * <code>InputStream</code> for random access to GZip compressed records.
     * @return <code>WarcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     */
    public static WarcReaderCompressed getReaderCompressed() {
        return new WarcReaderCompressed();
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * primarily for random access to GZip compressed records.
     * @param in WARC File represented as <code>InputStream</code>
     * @return <code>WarcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     * @throws IOException i/o exception while initializing reader
     */
    public static WarcReaderCompressed getReaderCompressed(InputStream in)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        return new WarcReaderCompressed(new GzipReader(in));
    }

    /**
     * Creates a new <code>WarcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code> primarily for random
     * access to GZip compressed records.
     * @param in WARC File represented as <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return <code>WarcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     * @throws IOException i/o exception while initializing reader
     */
    public static WarcReaderCompressed getReaderCompressed(InputStream in,
                                        int buffer_size) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: " +
                    buffer_size);
        }
        return new WarcReaderCompressed(new GzipReader(
                new BufferedInputStream(in, buffer_size)));
    }

}
