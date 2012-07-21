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
 * ARC Reader implementation for reading GZip compressed files.
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

    /** GZip reader used for the current record, if random access methods used. */
    protected GzipReader currentReader;

    /** GZip entry for the current record, if random access methods used. */
    protected GzipEntry currentEntry;

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
        if (currentRecord != null) {
            try {
                currentRecord.close();
            } catch (IOException e) { /* ignore */ }
            currentRecord = null;
        }
        if (reader != null) {
            // TODO
            //startOffset = reader.getStartOffset();
            consumed = reader.getOffset();
            try {
                reader.close();
            } catch (IOException e) { /* ignore */ }
            reader = null;
        }
    }

    @Override
    protected void recordClosed() {
        if (currentEntry != null) {
            try {
                currentEntry.close();
                consumed += currentEntry.consumed;
            } catch (IOException e) { /* ignore */ }
            currentEntry = null;
        }
    }

    /** Cached start offset used after the reader is closed. */
    protected long startOffset = 0;

    /**
     * Get the current offset in the ARC <code>GzipReader</code>.
     * @return offset in ARC <code>InputStream</code>
     */
    @Override
    public long getStartOffset() {
        if (reader != null) {
            return reader.getStartOffset();
        } else {
            return startOffset;
        }
    }

    /**
     * Get the current offset in the ARC <code>GzipReader</code>.
     * @return offset in ARC <code>InputStream</code>
     */
    @Override
    public long getOffset() {
        if (reader != null) {
            return reader.getOffset();
        } else {
            return consumed;
        }
    }

    /** Get number of bytes consumed by the ARC <code>GzipReader</code>.
     * @return number of bytes consumed by the ARC <code>GzipReader</code>
     */
    @Override
    public long getConsumed() {
        if (reader != null) {
            return reader.getOffset();
        } else {
            return consumed;
        }
    }

    @Override
    public ArcRecordBase getNextRecord() throws IOException {
        if (currentRecord != null) {
            currentRecord.close();
        }
        if (reader == null) {
            throw new IllegalStateException("The GZip reader 'reader' is null");
        }
        currentRecord = null;
        currentReader = reader;
        currentEntry = reader.getNextEntry();
        if (currentEntry != null) {
            ByteCountingPushBackInputStream pbin;
            if (bufferSize > 0) {
                pbin = new ByteCountingPushBackInputStream(
                        new BufferedInputStream(
                                currentEntry.getInputStream(),
                                bufferSize),
                        PUSHBACK_BUFFER_SIZE);
            } else {
                pbin = new ByteCountingPushBackInputStream(
                        currentEntry.getInputStream(), PUSHBACK_BUFFER_SIZE);
            }
            currentRecord = ArcRecordBase.parseRecord(pbin, this);
        }
        if (currentRecord != null) {
            startOffset = currentEntry.getStartOffset();
            currentRecord.header.startOffset = currentEntry.getStartOffset();
        }
        return currentRecord;
    }

    @Override
    public ArcRecordBase getNextRecordFrom(InputStream rin, long offset)
            throws IOException {
        if (currentRecord != null) {
            currentRecord.close();
        }
        if (reader != null) {
            throw new IllegalStateException("The GZip reader 'reader' is initialized");
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        if (offset < -1) {
            throw new IllegalArgumentException(
                    "The 'offset' is less than -1: " + offset);
        }
        currentRecord = null;
        currentReader = new GzipReader(rin);
        currentEntry = currentReader.getNextEntry();
        if (currentEntry != null) {
            ByteCountingPushBackInputStream pbin =
                    new ByteCountingPushBackInputStream(
                            currentEntry.getInputStream(), PUSHBACK_BUFFER_SIZE);
            currentRecord = ArcRecordBase.parseRecord(pbin, this);
        }
        if (currentRecord != null) {
            startOffset = offset;
            currentRecord.header.startOffset = offset;
        }
        return currentRecord;
    }

    @Override
    public ArcRecordBase getNextRecordFrom(InputStream rin, long offset,
                                        int buffer_size) throws IOException {
        if (currentRecord != null) {
            currentRecord.close();
        }
        if (reader != null) {
            throw new IllegalStateException("The GZip reader 'reader' is initialized");
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
        currentRecord = null;
        currentReader = new GzipReader(rin);
        currentEntry = currentReader.getNextEntry();
        if (currentEntry != null) {
            ByteCountingPushBackInputStream pbin =
                    new ByteCountingPushBackInputStream(
                            new BufferedInputStream(
                                    currentEntry.getInputStream(),
                                    buffer_size),
                            PUSHBACK_BUFFER_SIZE);
            currentRecord = ArcRecordBase.parseRecord(pbin, this);
        }
        if (currentRecord != null) {
            startOffset = offset;
            currentRecord.header.startOffset = offset;
        }
        return currentRecord;
    }

}
