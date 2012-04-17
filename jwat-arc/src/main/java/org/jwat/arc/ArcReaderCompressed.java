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

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.gzip.GzipReader;
import org.jwat.gzip.GzipEntry;

/**
 * ARC Reader used on GZip compressed files.
 *
 * @author nicl
 */
public class ArcReaderCompressed extends ArcReader {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /** ARC file <code>GzipInputStream</code>. */
    protected GzipReader reader;

    /** Buffer size, if any, to use on GZip entry <code>InputStream</code>. */
    protected int bufferSize;

    /**
     * This constructor is used to get random access to records.
     * The records are then accessed using the getNextRecordFrom methods
     * using a supplied input stream for each record.
     */
    ArcReaderCompressed() {
        init();
    }

    /**
     * Construct reader using the supplied input stream.
     * This method is primarily for sequential access to records.
     * @param reader GZip reader
     */
    ArcReaderCompressed(GzipReader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("'reader' is null");
        }
        this.reader = reader;
        init();
    }

    /**
     * Construct object using supplied <code>GzipInputStream</code>.
     * This method is primarily for sequential access to records.
     * @param reader GZip reader
     * @param buffer_size buffer size used on entries
     */
    ArcReaderCompressed(GzipReader reader, int buffer_size) {
        if (reader == null) {
            throw new IllegalArgumentException("'reader' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        this.reader = reader;
        this.bufferSize = buffer_size;
        init();
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
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) { /* ignore */ }
            reader = null;
        }
    }

    /**
     * Get the current offset in the ARC <code>GzipReader</code>.
     * @return offset in ARC <code>InputStream</code>
     */
    @Override
    public long getStartOffset() {
        return reader.getStartOffset();
    }

    /**
     * Get the current offset in the ARC <code>GzipReader</code>.
     * @return offset in ARC <code>InputStream</code>
     */
    @Override
    public long getOffset() {
        return reader.getOffset();
    }

    @Override
    public ArcVersionBlock getVersionBlock() throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (reader == null) {
            throw new IllegalStateException("The inputstream 'in' is null");
        }
        versionBlock = null;
        GzipEntry entry = reader.getNextEntry();
        if (entry != null) {
            if (bufferSize > 0) {
                versionBlock = ArcVersionBlock.parseVersionBlock(
                        new ByteCountingPushBackInputStream(
                                new BufferedInputStream(
                                        entry.getInputStream(), bufferSize),
                                PUSHBACK_BUFFER_SIZE), this);
            } else {
                versionBlock = ArcVersionBlock.parseVersionBlock(
                        new ByteCountingPushBackInputStream(
                                entry.getInputStream(),
                                PUSHBACK_BUFFER_SIZE), this);
            }
        }
        if (versionBlock != null) {
            versionBlock.startOffset = entry.getStartOffset();
        }
        previousRecord = versionBlock;
        return versionBlock;
    }

    @Override
    public ArcVersionBlock getVersionBlockFrom(InputStream vbin, long offset)
            throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (vbin == null) {
            throw new IllegalArgumentException("The inputstream 'vbin' is null");
        }
        if (offset < -1) {
            throw new IllegalArgumentException(
                    "The 'offset' is less than -1: " + offset);
        }
        versionBlock = null;
        GzipReader reader = new GzipReader(vbin);
        GzipEntry entry = reader.getNextEntry();
        if (entry != null) {
            if (bufferSize > 0) {
                versionBlock = ArcVersionBlock.parseVersionBlock(
                        new ByteCountingPushBackInputStream(
                                new BufferedInputStream(
                                        entry.getInputStream(), bufferSize),
                                PUSHBACK_BUFFER_SIZE), this);
            } else {
                versionBlock = ArcVersionBlock.parseVersionBlock(
                        new ByteCountingPushBackInputStream(
                                entry.getInputStream(),
                                PUSHBACK_BUFFER_SIZE), this);
            }
        }
        if (versionBlock != null) {
            versionBlock.startOffset = offset;
        }
        previousRecord = versionBlock;
        return versionBlock;
    }

    @Override
    public ArcRecord getNextRecord() throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (reader == null) {
            throw new IllegalStateException("The inputstream 'in' is null");
        }
        arcRecord = null;
        GzipEntry entry = reader.getNextEntry();
        if (entry != null) {
            if (bufferSize > 0) {
                arcRecord = ArcRecord.parseArcRecord(
                        new ByteCountingPushBackInputStream(
                                new BufferedInputStream(
                                        entry.getInputStream(),
                                        bufferSize),
                                PUSHBACK_BUFFER_SIZE),
                        versionBlock, this);
            } else {
                arcRecord = ArcRecord.parseArcRecord(
                        new ByteCountingPushBackInputStream(
                                entry.getInputStream(), PUSHBACK_BUFFER_SIZE),
                        versionBlock, this);
            }
        }
        if (arcRecord != null) {
            arcRecord.startOffset = entry.getStartOffset();
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
        if (offset < -1) {
            throw new IllegalArgumentException(
                    "The 'offset' is less than -1: " + offset);
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        arcRecord = null;
        GzipReader reader = new GzipReader(rin);
        GzipEntry entry = reader.getNextEntry();
        if (entry != null) {
            ByteCountingPushBackInputStream pbin =
                    new ByteCountingPushBackInputStream(
                            entry.getInputStream(), PUSHBACK_BUFFER_SIZE);
            arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock, this);
        }
        if (arcRecord != null) {
            arcRecord.startOffset = offset;
        }
        previousRecord = arcRecord;
        return arcRecord;
    }

    @Override
    public ArcRecord getNextRecordFrom(InputStream rin, long offset,
                                        int buffer_size) throws IOException {
        if (previousRecord != null) {
            previousRecord.close();
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        if (offset < -1) {
            throw new IllegalArgumentException(
                    "The 'offset' is less than -1: " + offset);
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        arcRecord = null;
        GzipReader reader = new GzipReader(rin);
        GzipEntry entry = reader.getNextEntry();
        if (entry != null) {
            ByteCountingPushBackInputStream pbin =
                    new ByteCountingPushBackInputStream(
                            new BufferedInputStream(
                                    entry.getInputStream(),
                                    buffer_size),
                            PUSHBACK_BUFFER_SIZE);
            arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock, this);
        }
        if (arcRecord != null) {
            arcRecord.startOffset = offset;
        }
        previousRecord = arcRecord;
        return arcRecord;
    }

}
