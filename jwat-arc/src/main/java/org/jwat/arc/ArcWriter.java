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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;

/**
 * Base class for ARC writer implementations.
 *
 * @author nicl
 */
public abstract class ArcWriter {

	/** State after writer has been constructed and before records have been written. */
    protected static final int S_INIT = 0;

    /** State after header has been written. */
    protected static final int S_HEADER_WRITTEN = 1;

    /** State after payload has been written. */
    protected static final int S_PAYLOAD_WRITTEN = 2;

    /** State after record has been closed. */
    protected static final int S_RECORD_CLOSED = 3;

    /** ARC <code>DateFormat</code> as described in the IA documentation. */
    protected DateFormat arcDateFormat = ArcDateParser.getDateFormat();

    /** Current state of writer. */
    protected int state = S_INIT;

    /** Outputstream used to write ARC records. */
    protected OutputStream out;

    /** Buffer used by streamPayload() to copy from one stream to another. */
    protected byte[] stream_copy_buffer = new byte[8192];

    /**
     * Method used to initialize a readers internal state.
     * Must be called by all constructors.
     */
    protected void init() {
    }

    /**
     * Is this writer producing compressed output.
     * @return boolean indicating whether compressed output is produced
     */
    public abstract boolean isCompressed();

    /**
     * Close ARC writer and free its resources.
     */
    public abstract void close();

    /**
     * Close the ARC record.
     * @throws IOException if an exception occurs while closing the record
     */
    public abstract void closeRecord() throws IOException;

    /**
     * Close the ARC record, implementation.
     * @throws IOException if an exception occurs while closing the record
     */
    protected void closeRecord_impl() throws IOException {
    	// TODO
    }

    /**
     * Write a raw ARC header to the WARC output stream.
     * @param header_bytes raw ARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    public void writeHeader(byte[] header_bytes) throws IOException {
        if (header_bytes == null) {
            throw new IllegalArgumentException(
                    "The 'header_bytes' parameter is null!");
        }
        if (state == S_HEADER_WRITTEN) {
            throw new IllegalStateException("Headers written back to back!");
        } else if (state == S_PAYLOAD_WRITTEN) {
            closeRecord_impl();
        }
        out.write(header_bytes);
        state = S_HEADER_WRITTEN;
    }

    /**
     * Write a WARC header to the ARC output stream.
     * @param record ARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    public abstract byte[] writeHeader(ArcRecordBase record) throws IOException;

    /**
     * Write an ARC header to the WARC output stream.
     * @param record ARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    protected byte[] writeHeader_impl(ArcRecordBase record) throws IOException {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        /*
         * End Of Header
         */
        outBuf.write("\r\n".getBytes());
        byte[] header = outBuf.toByteArray();
        out.write(header);
        state = S_HEADER_WRITTEN;
        return header;
    }

    /**
    *
    * @param in input stream containing payload data
    * @param length payload length
    * @return written length of payload data
    * @throws IOException if an exception occurs while writing payload data
    */
   public long streamPayload(InputStream in, long length) throws IOException {
       if (in == null) {
           throw new IllegalArgumentException(
                   "The 'in' parameter is null!");
       }
       if (length < 0) {
           throw new IllegalArgumentException(
                   "The 'length' parameter is less than zero!");
       }
       if (state != S_HEADER_WRITTEN && state != S_PAYLOAD_WRITTEN) {
           throw new IllegalStateException("Write a header before writing payload!");
       }
       long written = 0;
       int read = 0;
       while (read != -1) {
           out.write(stream_copy_buffer, 0, read);
           written += read;
           read = in.read(stream_copy_buffer);
       }
       state = S_PAYLOAD_WRITTEN;
       return written;
   }

}
