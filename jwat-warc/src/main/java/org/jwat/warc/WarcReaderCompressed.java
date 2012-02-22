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

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.gzip.GzipReader;
import org.jwat.gzip.GzipEntry;

/**
 * WARC Reader used on GZip compressed files.
 *
 * @author nicl
 */
public class WarcReaderCompressed extends WarcReader {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /** WARC file <code>InputStream</code>. */
    protected GzipReader reader;

    /** Buffer size, if any, to use on GZip entry <code>InputStream</code>. */
    protected int bufferSize;

    /**
     * This constructor is used to get random access to records.
     * The records are then accessed using the getNextRecordFrom methods
     * using a supplied input stream for each record.
     */
    WarcReaderCompressed() {
    }

    /**
     * Construct reader using the supplied input stream.
     * This method is primarily for sequential access to records.
     * @param reader GZip reader
     */
    WarcReaderCompressed(GzipReader reader) {
        if (reader == null) {
            throw new IllegalArgumentException(
                    "'reader' is null");
        }
        this.reader = reader;
    }

    /**
     * Construct object using supplied <code>GzipInputStream</code>.
     * This method is primarily for sequential access to records.
     * @param reader GZip reader
     * @param buffer_size buffer size used on entries
     */
    WarcReaderCompressed(GzipReader reader, int buffer_size) {
        if (reader == null) {
            throw new IllegalArgumentException(
                    "'reader' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        this.reader = reader;
        this.bufferSize = buffer_size;
    }

    @Override
    public boolean isCompressed() {
        return true;
    }

    @Override
    public void close() {
        if (warcRecord != null) {
            try {
                warcRecord.close();
            } catch (IOException e) { /* ignore */ }
            warcRecord = null;
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) { /* ignore */ }
            reader = null;
        }
    }

    /**
     * Get the current offset in the WARC <code>GzipReader</code>.
     * @return offset in WARC <code>InputStream</code>
     */
    @Override
    public long getStartOffset() {
        return reader.getStartOffset();
    }

    /**
     * Get the current offset in the WARC <code>GzipReader</code>.
     * @return offset in WARC <code>InputStream</code>
     */
    @Override
    public long getOffset() {
        return reader.getOffset();
    }

    @Override
    public WarcRecord getNextRecord() throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (reader == null) {
            throw new IllegalStateException("The inputstream 'in' is null");
        }
        warcRecord = null;
        GzipEntry entry = reader.getNextEntry();
        if (entry != null) {
            if (bufferSize > 0) {
                warcRecord = WarcRecord.parseRecord(
                        new ByteCountingPushBackInputStream(
                                new BufferedInputStream(entry.getInputStream(),
                                bufferSize),
                                PUSHBACK_BUFFER_SIZE), this);
            }
            else {
                warcRecord = WarcRecord.parseRecord(
                        new ByteCountingPushBackInputStream(
                                entry.getInputStream(),
                                PUSHBACK_BUFFER_SIZE), this);
            }
        }
        if (warcRecord != null) {
            warcRecord.startOffset = entry.getStartOffset();
        }
        return warcRecord;
    }

    @Override
    public WarcRecord getNextRecordFrom(InputStream rin) throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        warcRecord = null;
        GzipReader reader = new GzipReader(rin);
        GzipEntry entry = reader.getNextEntry();
        if (entry != null) {
            warcRecord = WarcRecord.parseRecord(
                    new ByteCountingPushBackInputStream(
                            entry.getInputStream(),
                            PUSHBACK_BUFFER_SIZE), this);
        }
        if (warcRecord != null) {
            warcRecord.startOffset = -1L;
        }
        return warcRecord;
    }

    @Override
    public WarcRecord getNextRecordFrom(InputStream rin, int buffer_size)
                                                        throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' is less than or equal to zero: "
                    + buffer_size);
        }
        warcRecord = null;
        GzipReader reader = new GzipReader(rin);
        GzipEntry entry = reader.getNextEntry();
        if (entry != null) {
            warcRecord = WarcRecord.parseRecord(
                    new ByteCountingPushBackInputStream(
                            new BufferedInputStream(
                                    entry.getInputStream(), buffer_size),
                                    PUSHBACK_BUFFER_SIZE), this);
        }
        if (warcRecord != null) {
            warcRecord.startOffset = -1L;
        }
        return warcRecord;
    }

}
