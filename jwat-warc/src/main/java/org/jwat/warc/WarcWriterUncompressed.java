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
import java.io.OutputStream;

/**
 * Join the Darkside! Work in progress.
 *
 * @author nicl
 */
public class WarcWriterUncompressed extends WarcWriter {

    /**
     * Construct an unbuffered WARC writer used to write uncompressed records.
     * @param out outputstream to write to
     */
    WarcWriterUncompressed(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        this.out = out;
    }

    /**
     * Construct a buffered WARC writer used to write uncompressed records.
     * @param out outputstream to stream to
     * @param buffer_size outputstream buffer size
     * @throws IllegalArgumentException if out is null.
     * @throws IllegalArgumentException if buffer_size <= 0.
     */
    WarcWriterUncompressed(OutputStream out, int buffer_size) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' parameter is less than or equal to zero!");
        }
        this.out = new BufferedOutputStream(out, buffer_size);
    }

    @Override
    public boolean isCompressed() {
        return false;
    }

    @Override
    public void writeHeader(WarcRecord record) throws IOException {
        if (record == null) {
            throw new IllegalArgumentException(
                    "The 'record' parameter is null!");
        }
        writeHeader_impl(record);
    }

    @Override
    public void close() {
        try {
        	if (out != null) {
                out.flush();
                out.close();
                out = null;
        	}
        }
        catch (IOException e) {
        }
    }

}
