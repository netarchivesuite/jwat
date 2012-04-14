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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jwat.common.ByteCountingInputStream;
import org.jwat.gzip.GzipConstants;
import org.jwat.gzip.GzipEntry;
import org.jwat.gzip.GzipWriter;

public class WarcWriterCompressed extends WarcWriter {

    protected GzipWriter writer;

    protected GzipEntry entry;

    /**
     * Construct an unbuffered WARC writer used to write compressed records.
     * @param out outputstream to write to
     */
    public WarcWriterCompressed(OutputStream out) {
        writer = new GzipWriter(out);
    }

    /**
     * Construct a buffered WARC writer used to write compressed records.
     * @param out outputstream to stream to
     * @param buffer_size outputstream buffer size
     * @throws IllegalArgumentException if out is null.
     * @throws IllegalArgumentException if buffer_size <= 0.
     */
    public WarcWriterCompressed(OutputStream out, int buffer_size) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' parameter is less than or equal to zero!");
        }
        writer = new GzipWriter(new BufferedOutputStream(out, buffer_size));

    }

    @Override
    public boolean isCompressed() {
        return true;
    }

    @Override
    public void close() {
        try {
            out.flush();
            out.close();
        }
        catch (IOException e) {
        }
    }

    @Override
    public void writeHeader(byte[] header_bytes) throws IOException {
        if (entry != null) {
            closeRecord();
        }
        if (header_bytes == null) {
            throw new IllegalArgumentException(
                    "The 'header_bytes' parameter is null!");
        }
        entry = new GzipEntry();
        entry.magic = GzipConstants.GZIP_MAGIC;
        entry.cm = GzipConstants.CM_DEFLATE;
        entry.flg = 0;
        entry.mtime = System.currentTimeMillis() / 1000;
        entry.xfl = 0;
        entry.os = GzipConstants.OS_UNKNOWN;
        writer.writeEntryHeader(entry);
        out = entry.getOutputStream();
        out.write(header_bytes);
    }

    @Override
    public void writeHeader(WarcRecord record) throws IOException {
        if (entry != null) {
            closeRecord();
        }
        if (record == null) {
            throw new IllegalArgumentException(
                    "The 'record' parameter is null!");
        }
        entry = new GzipEntry();
        entry.magic = GzipConstants.GZIP_MAGIC;
        entry.cm = GzipConstants.CM_DEFLATE;
        entry.flg = 0;
        entry.mtime = System.currentTimeMillis() / 1000;
        entry.xfl = 0;
        entry.os = GzipConstants.OS_UNKNOWN;
        writer.writeEntryHeader(entry);
        out = entry.getOutputStream();
        writeHeader_impl(record);
    }

    @Override
    public long transferPayload(InputStream in, long length) throws IOException {
        if (entry == null) {
            throw new IllegalStateException();
        }
        ByteCountingInputStream bcin = new ByteCountingInputStream(in);
        entry.writeFrom(bcin);
        //bcin.close();
        return bcin.getConsumed();
    }

    @Override
    public void closeRecord() throws IOException {
        if (entry != null) {
            out.write(endMark);
            entry.close();
            entry = null;
        }
    }

}