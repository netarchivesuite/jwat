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

/**
 * ARC Reader implementation for reading uncompressed files.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcReaderUncompressed extends ArcReader {

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 16;

    /** ARC file <code>ByteCountingPushBackInputStream</code>. */
    protected ByteCountingPushBackInputStream in;

    /** Start offset of current or next valid record. */
    protected long startOffset = 0;

    /**
     * This constructor is used to get random access to records.
     * The records are then accessed using the getNextRecordFrom methods
     * using a supplied input stream for each record.
     */
    ArcReaderUncompressed() {
        init();
    }

    /**
     * Construct reader using the supplied input stream.
     * This method is primarily for sequential access to records.
     * @param in ARC file input stream
     */
    ArcReaderUncompressed(ByteCountingPushBackInputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("The inputstream 'in' is null");
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
        if (currentRecord != null) {
            try {
                currentRecord.close();
            } catch (IOException e) { /* ignore */ }
            currentRecord = null;
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) { /* ignore */ }
            in = null;
        }
    }

    @Override
    protected void recordClosed() {
        consumed += currentRecord.consumed;
    }

    @Override
    public long getStartOffset() {
        return startOffset;
    }

    @Override
    public long getOffset() {
        if (in != null) {
            return in.getConsumed();
        } else {
            return consumed;
        }
    }

    @Override
    public long getConsumed() {
        if (in != null) {
            return in.getConsumed();
        } else {
            return consumed;
        }
    }

    @Override
    public ArcRecordBase getNextRecord() throws IOException {
        if (currentRecord != null) {
            currentRecord.close();
        }
        if (in == null) {
            throw new IllegalStateException("The inputstream 'in' is null");
        }
        currentRecord = ArcRecordBase.parseRecord(in, this);
        if (currentRecord != null) {
            //currentRecord.postProcess();
            startOffset = currentRecord.header.startOffset;
        }
        return currentRecord;
    }

    @Override
    public ArcRecordBase getNextRecordFrom(InputStream rin, long offset)
            throws IOException {
        if (currentRecord != null) {
            currentRecord.close();
        }
        if (in != null) {
            throw new IllegalStateException("The inputstream 'in' is initialized");
        }
        if (rin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'rin' is null");
        }
        if (offset < -1) {
            throw new IllegalArgumentException(
                    "The 'offset' is less than -1: " + offset);
        }
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(rin, PUSHBACK_BUFFER_SIZE);
        currentRecord = ArcRecordBase.parseRecord(pbin, this);
        if (currentRecord != null) {
            //currentRecord.postProcess();
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
        if (in != null) {
            throw new IllegalStateException("The inputstream 'in' is initialized");
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
        ByteCountingPushBackInputStream pbin =
                new ByteCountingPushBackInputStream(
                        new BufferedInputStream(rin, buffer_size),
                        PUSHBACK_BUFFER_SIZE);
        currentRecord = ArcRecordBase.parseRecord(pbin, this);
        if (currentRecord != null) {
            //currentRecord.postProcess();
            startOffset = offset;
            currentRecord.header.startOffset = offset;
        }
        return currentRecord;
    }

}
