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

/**
 * WARC Reader used on uncompressed files.
 *
 * @author nicl
 */
public class WarcReaderUncompressed extends WarcReader {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /** WARC file <code>InputStream</code>. */
    protected ByteCountingPushBackInputStream in;

    /** Start offset of current or next valid record. */
    protected long startOffset = 0;

    /**
     * This constructor is used to get random access to records.
     * The records are then accessed using the getNextRecordFrom methods
     * using a supplied input stream for each record.
     */
    WarcReaderUncompressed() {
    	init();
    }

    /**
     * Construct reader using the supplied input stream.
     * This method is primarily for sequential access to records.
     * @param in WARC file input stream
     */
    WarcReaderUncompressed(ByteCountingPushBackInputStream in) {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'in' is null");
        }
        this.in = in;
    	init();
    }

    @Override
    public boolean isCompressed() {
        return false;
    }

    @Override
    public void close() {
        if (warcRecord != null) {
            try {
                warcRecord.close();
            } catch (IOException e) { /* ignore */ }
            warcRecord = null;
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) { /* ignore */ }
            in = null;
        }
    }

    @Override
    public long getStartOffset() {
        return startOffset;
    }

    @Override
    public long getOffset() {
        return in.getConsumed();
    }

    @Override
    public WarcRecord getNextRecord() throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (in == null) {
            throw new IllegalStateException(
                    "The inputstream 'in' is null");
        }
        warcRecord = WarcRecord.parseRecord(in, this);
        if (warcRecord != null) {
            startOffset = warcRecord.getStartOffset();
        }
        return warcRecord;
    }

    @Override
    public WarcRecord getNextRecordFrom(InputStream rin, long offset)
                                                        throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        if (offset < -1) {
            throw new IllegalArgumentException(
                    "The 'offset' is less than -1: " + offset);
        }
        warcRecord = WarcRecord.parseRecord(
                new ByteCountingPushBackInputStream(rin, PUSHBACK_BUFFER_SIZE),
                this);
        if (warcRecord != null) {
            warcRecord.startOffset = offset;
            startOffset = offset;
        }
        return warcRecord;
    }

    @Override
    public WarcRecord getNextRecordFrom(InputStream rin, long offset,
                                        int buffer_size) throws IOException {
        if (warcRecord != null) {
            warcRecord.close();
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
        warcRecord = WarcRecord.parseRecord(
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(rin, buffer_size),
                        PUSHBACK_BUFFER_SIZE), this);
        if (warcRecord != null) {
            warcRecord.startOffset = offset;
            startOffset = offset;
        }
        return warcRecord;
    }

}
