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
package org.jwat.arc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.jwat.archive.common.ReaderFactoryAbstract;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Scheme;
import org.jwat.gzip.GzipReader;

/**
 * Factory used for creating <code>ArcReader</code> instances.
 * The general <code>getReader</code> methods will auto-detect Gzip'ed data
 * and return the appropriate <code>ArcReader</code> instances.
 * The other factory methods can be used to return specific
 * <code>ArcReader</code> instances for compressed or uncompressed records.
 * Readers are available for both sequential and random reading of records.
 * Use of buffered methods and/or buffering speeds up the reader considerably.
 *
 * @author nicl
 */
public class ArcReaderFactory extends ReaderFactoryAbstract {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 32;

    /**
     * Private constructor to enforce factory methods.
     */
    protected ArcReaderFactory() {
    }

    /**
     * Check head of <code>PushBackInputStream</code> for an ARC file identifier.
     * The identifier for ARC files is "filedesc:" in the beginning.
     * @param pbin <code>PushBackInputStream</code> with an ARC version block
     * @return boolean indicating presence of an ARC file identifier
     * @throws IOException if an I/O error occurs while examining head of stream
     */
    public static boolean isArcFile(ByteCountingPushBackInputStream pbin) throws IOException {
        byte[] streamBytes = new byte[ArcConstants.ARC_MAGIC_HEADER.length()];
        byte[] arcBytes = ArcConstants.ARC_MAGIC_HEADER.getBytes();
        // Look for an ARC file identifier in the beginning of the stream.
        pbin.peek(streamBytes);
        return (Arrays.equals(arcBytes, streamBytes));
    }

    /**
     * Check head of <code>PushBackInputStream</code> for an ARC record identifier.
     * The identifier for ARC files is "filedesc:" in the beginning.
     * @param pbin <code>PushBackInputStream</code> with an ARC version block
     * @return boolean indicating presence of an ARC file identifier
     * @throws IOException if an I/O error occurs while examining head of stream
     */
    public static boolean isArcRecord(ByteCountingPushBackInputStream pbin) throws IOException {
        byte[] streamBytes = new byte[32];
        // Look for a valid scheme in the beginning of the stream.
        pbin.peek(streamBytes);
        return Scheme.startsWithScheme(streamBytes);
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on
     * GZip auto detection.
     * @param in ARC File represented as <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return appropriate <code>ArcReader</code> based on data read from
     * <code>InputStream</code>
     * @throws IOException if an I/O exception occurs during initialization
     */
    public static ArcReader getReader(InputStream in, int buffer_size)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(in, buffer_size),
                                                PUSHBACK_BUFFER_SIZE);
        if (GzipReader.isGzipped(pbin)) {
            return new ArcReaderCompressed(new GzipReader(pbin),
                                           buffer_size);
        }
        return new ArcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>.
     * The <code>WarcReader</code> implementation returned is chosen based on
     * GZip auto detection.
     * @param in ARC File represented as <code>InputStream</code>
     * @return appropriate <code>ArcReader</code> based on data read from
     * <code>InputStream</code>
     * @throws IOException if an I/O exception occurs during initialization
     */
    public static ArcReader getReader(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(in, PUSHBACK_BUFFER_SIZE);
        if (GzipReader.isGzipped(pbin)) {
            return new ArcReaderCompressed(new GzipReader(pbin));
        }
        return new ArcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>ArcReader</code> without any associated
     * <code>InputStream</code> for random access to uncompressed records.
     * @return <code>ArcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     */
    public static ArcReaderUncompressed getReaderUncompressed() {
        return new ArcReaderUncompressed();
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * primarily for random access to uncompressed records.
     * @param in ARC File represented as <code>InputStream</code>
     * @return <code>ArcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     * @throws IOException I/O exception while initializing reader
     */
    public static ArcReaderUncompressed getReaderUncompressed(InputStream in)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(in, PUSHBACK_BUFFER_SIZE);
        return new ArcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code> primarily for random
     * access to uncompressed records.
     * @param in ARC File represented as <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return <code>ArcReader</code> for uncompressed records read from
     * <code>InputStream</code>
     * @throws IOException I/O exception while initializing reader
     */
    public static ArcReaderUncompressed getReaderUncompressed(InputStream in,
                                        int buffer_size) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(in, buffer_size),
                        PUSHBACK_BUFFER_SIZE);
        return new ArcReaderUncompressed(pbin);
    }

    /**
     * Creates a new <code>ArcReader</code> without any associated
     * <code>InputStream</code> for random access to GZip compressed records.
     * @return <code>ArcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     */
    public static ArcReaderCompressed getReaderCompressed() {
        return new ArcReaderCompressed();
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * primarily for random access to GZip compressed records.
     * @param in ARC File represented as <code>InputStream</code>
     * @return <code>ArcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     * @throws IOException I/O exception while initializing reader
     */
    public static ArcReaderCompressed getReaderCompressed(InputStream in)
                                                        throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        return new ArcReaderCompressed(new GzipReader(in));
    }

    /**
     * Creates a new <code>ArcReader</code> from an <code>InputStream</code>
     * wrapped by a <code>BufferedInputStream</code> primarily for random
     * access to GZip compressed records.
     * @param in ARC File represented as <code>InputStream</code>
     * @param buffer_size buffer size to use
     * @return <code>ArcReader</code> for GZip compressed records read from
     * <code>InputStream</code>
     * @throws IOException I/O exception while initializing reader
     */
    public static ArcReaderCompressed getReaderCompressed(InputStream in,
                                        int buffer_size) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        return new ArcReaderCompressed(new GzipReader(
                                new BufferedInputStream(in, buffer_size)));
    }

}
