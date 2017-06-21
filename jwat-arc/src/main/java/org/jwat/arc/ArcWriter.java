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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;

/**
 * Base class for ARC writer implementations.
 *
 * Note: the content-length is synonymous to the archive-length.
 *
 * @author nicl
 */
public abstract class ArcWriter implements Closeable {

    /** State after writer has been constructed and before records have been written. */
    protected static final int S_INIT = 0;

    /** State after header has been written. */
    protected static final int S_HEADER_WRITTEN = 1;

    /** State after payload has been written. */
    protected static final int S_PAYLOAD_WRITTEN = 2;

    /** State after record has been closed. */
    protected static final int S_RECORD_CLOSED = 3;

    /*
     * Settings.
     */

    /** ARC field parser used. */
    protected ArcFieldParsers fieldParsers;

    /** Buffer used by streamPayload() to copy from one stream to another. */
    protected byte[] stream_copy_buffer;

    /** Configuration for throwing exception on content-length mismatch.
     *  (Default is true) */
    protected boolean bExceptionOnContentLengthMismatch;

    /*
     * State.
     */

    /** Writer level errors and warnings or when writing byte headers. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /** Current state of writer. */
    protected int state = S_INIT;

    /** Outputstream used to write ARC records. */
    protected OutputStream out;

    protected ArcHeader header;

    /** Content-Length from the ARC header. */
    protected Long headerContentLength;

    /** Total bytes written for current record payload. */
    protected long payloadWrittenTotal;

    /**
     * Method used to initialize a readers internal state.
     * Must be called by all constructors.
     */
    protected void init() {
        fieldParsers = new ArcFieldParsers();
        stream_copy_buffer = new byte[8192];
        bExceptionOnContentLengthMismatch = true;
    }

    /**
     * Is this writer producing compressed output.
     * @return boolean indicating whether compressed output is produced
     */
    public abstract boolean isCompressed();

    /**
     * Does this writer throw an exception if the content-length does not match
     * the payload amount written.
     * @return boolean indicating if an exception is thrown or not
     */
    public boolean exceptionOnContentLengthMismatch() {
        return bExceptionOnContentLengthMismatch;
    }

    /**
     * Tell the writer what to do in case of mismatch between content-length
     * and amount payload written.
     * @param enabled boolean indicating exception throwing on/off
     */
    public void setExceptionOnContentLengthMismatch(boolean enabled) {
        bExceptionOnContentLengthMismatch = enabled;
    }

    /**
     * Close ARC writer and free its resources.
     * @throws IOException if an I/O exception occurs while closing the writer
     */
    public abstract void close() throws IOException;

    /**
     * Close the ARC record in an implementation specific way.
     * @throws IOException if an I/O exception occurs while closing the record
     */
    public abstract void closeRecord() throws IOException;

    /**
     * Closes the ARC record by writing one newline and comparing the amount of
     * payload data streamed with the content-length supplied with the header.
     * @throws IOException if an I/O exception occurs while closing the record
     */
    protected void closeRecord_impl() throws IOException {
        Diagnosis diagnosis = null;
        out.write(ArcConstants.endMark);
        out.flush();
        if (headerContentLength == null) {
            diagnosis = new Diagnosis(
                    DiagnosisType.ERROR_EXPECTED,
                    "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' header",
                    "Mandatory!");
        } else {
            if (headerContentLength != payloadWrittenTotal) {
                diagnosis = new Diagnosis(
                        DiagnosisType.INVALID_EXPECTED,
                        "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' header",
                        Long.toString(payloadWrittenTotal),
                        headerContentLength.toString());
            }
        }
        if (diagnosis != null) {
            if (header != null) {
                header.diagnostics.addError(diagnosis);
            } else {
                diagnostics.addError(diagnosis);
            }
            if (bExceptionOnContentLengthMismatch) {
                throw new IllegalStateException("Payload size does not match content-length!");
            }
        }
        header = null;
        headerContentLength = null;
    }

    /**
     * Write a raw ARC header to the ARC output stream. Closes any previously
     * written record that has not been closed prior to this call.
     * Errors and warnings are reported on the writers diagnostics object.
     * @param header_bytes raw ARC record to output
     * @param contentLength the expected content-length to be written and validated
     * The contentLength is synonymous to the Archive-Length.
     * @throws IOException if an exception occurs while writing header data
     */
    public void writeRawHeader(byte[] header_bytes, Long contentLength) throws IOException {
        if (header_bytes == null) {
            throw new IllegalArgumentException(
                    "The 'header_bytes' parameter is null!");
        }
        if (contentLength != null && contentLength < 0) {
            throw new IllegalArgumentException(
                    "The 'contentLength' parameter is negative!");
        }
        if (state == S_HEADER_WRITTEN) {
            throw new IllegalStateException("Headers written back to back!");
        } else if (state == S_PAYLOAD_WRITTEN) {
            closeRecord_impl();
        }
        out.write(header_bytes);
        state = S_HEADER_WRITTEN;
        header = null;
        headerContentLength = contentLength;
        payloadWrittenTotal = 0;
    }

    /**
     * Write a ARC header to the ARC output stream.
     * Errors and warnings are reported on the records diagnostics object.
     * @param record ARC record to output
     * @return byte array version of header as it was written
     * @throws IOException if an I/O exception occurs while writing header data
     */
    public abstract byte[] writeHeader(ArcRecordBase record) throws IOException;

    /**
     * Write an ARC header to the ARC output stream.
     * The WARC header is not required to be valid.
     * Errors and warnings are reported on the records diagnostics object.
     * @param record ARC record to output
     * @return byte array version of header as it was written
     * @throws IOException if an I/O exception occurs while writing header data
     */
    protected byte[] writeHeader_impl(ArcRecordBase record) throws IOException {
        header = record.header;
        headerContentLength = header.archiveLength;
        if (headerContentLength == null && header.archiveLengthStr != null) {
            headerContentLength = fieldParsers.parseLong(header.archiveLengthStr, ArcConstants.FN_ARCHIVE_LENGTH, false);
            if (headerContentLength != null && headerContentLength < 0) {
                diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' value", header.archiveLengthStr, "A non negative number"));
            }
        }
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        /*
         * URL
         */
        String urlStr;
        if (header.urlUri != null) {
            urlStr = header.urlUri.toString();
        } else if (header.urlStr != null && header.urlStr.length() > 0) {
            urlStr = header.urlStr;
        } else {
            urlStr = "-";
        }
        outBuf.write(urlStr.getBytes());
        /*
         * IP-Address
         */
        String ipAddressStr;
        if (header.inetAddress != null) {
            ipAddressStr = header.inetAddress.getHostAddress();
        } else if (header.ipAddressStr != null && header.ipAddressStr.length() > 0) {
            ipAddressStr = header.ipAddressStr;
        } else {
            ipAddressStr = "-";
        }
        outBuf.write(" ".getBytes());
        outBuf.write(ipAddressStr.getBytes());
        /*
         * Archive-Date
         */
        String archiveDateStr;
        if (header.archiveDate != null) {
            archiveDateStr = ArcDateParser.getDateFormat().format(header.archiveDate);
        } else if (header.archiveDateStr != null && header.archiveDateStr.length() > 0) {
            archiveDateStr = header.archiveDateStr;
        } else {
            archiveDateStr = "-";
        }
        outBuf.write(" ".getBytes());
        outBuf.write(archiveDateStr.getBytes());
        /*
         * Content-Type
         */
        String contentTypeStr;
        if (header.contentType != null) {
            contentTypeStr = header.contentType.toStringShort();
        } else if (header.contentTypeStr != null && header.contentTypeStr.length() > 0) {
            contentTypeStr = header.contentTypeStr;
        } else {
            contentTypeStr = "-";
        }
        outBuf.write(" ".getBytes());
        outBuf.write(contentTypeStr.getBytes());
        /*
         * Version 2 fields.
         */
        if (header.recordFieldVersion == 2) {
            /*
             * Result-Code
             */
            String resultCodeStr;
            if (header.resultCode != null) {
                resultCodeStr = header.resultCode.toString();
            } else if (header.resultCodeStr != null && header.resultCodeStr.length() > 0) {
                resultCodeStr = header.resultCodeStr;
            } else {
                resultCodeStr = "-";
            }
            outBuf.write(" ".getBytes());
            outBuf.write(resultCodeStr.getBytes());
            /*
             * Checksum
             */
            String checksumStr;
            if (header.checksumStr != null && header.checksumStr.length() > 0) {
                checksumStr = header.checksumStr;
            } else {
                checksumStr = "-";
            }
            outBuf.write(" ".getBytes());
            outBuf.write(checksumStr.getBytes());
            /*
             * Location
             */
            String locationStr;
            if (header.locationStr != null && header.locationStr.length() > 0) {
                locationStr = header.locationStr;
            } else {
                locationStr = "-";
            }
            outBuf.write(" ".getBytes());
            outBuf.write(locationStr.getBytes());
            /*
             * Offset
             */
            String offsetStr;
            if (header.offset != null) {
                offsetStr = header.offset.toString();
            } else if (header.offsetStr != null && header.offsetStr.length() > 0) {
                offsetStr = header.offsetStr;
            } else {
                offsetStr = "-";
            }
            outBuf.write(" ".getBytes());
            outBuf.write(offsetStr.getBytes());
            /*
             * Filename
             */
            String filenameStr;
            if (header.filenameStr != null && header.filenameStr.length() > 0) {
                filenameStr = header.filenameStr;
            } else {
                filenameStr = "-";
            }
            outBuf.write(" ".getBytes());
            outBuf.write(filenameStr.getBytes());
        }
        /*
         * Archive-Length
         */
        String archiveLengthStr;
        if (header.archiveLength != null) {
            archiveLengthStr = header.archiveLength.toString();
        } else if (header.archiveLengthStr != null && header.archiveLengthStr.length() > 0) {
            archiveLengthStr = header.archiveLengthStr;
        } else {
            archiveLengthStr = "-";
        }
        outBuf.write(" ".getBytes());
        outBuf.write(archiveLengthStr.getBytes());
        outBuf.write("\n".getBytes());
        /*
         * End Of Header
         */
        byte[] headerBytes = outBuf.toByteArray();
        out.write(headerBytes);
        state = S_HEADER_WRITTEN;
        payloadWrittenTotal = 0;
        return headerBytes;
    }

    /**
     * Stream the content of an input stream to the payload content.
     * @param in input stream containing payload data
     * @return number of bytes written during method invocation
     * @throws IOException if an I/O exception occurs while writing payload data
     */
   public long streamPayload(InputStream in) throws IOException {
       if (in == null) {
           throw new IllegalArgumentException(
                   "The 'in' parameter is null!");
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
       payloadWrittenTotal += written;
       return written;
   }

   /**
    * Append the content of a byte array to the payload content.
    * @param b byte array with data to be written
    * @return number of bytes written during method invocation
    * @throws IOException if an I/O exception occurs while writing payload data
    */
   public long writePayload(byte[] b) throws IOException {
       if (state != S_HEADER_WRITTEN && state != S_PAYLOAD_WRITTEN) {
           throw new IllegalStateException("Write a header before writing payload!");
       }
       out.write(b);
       state = S_PAYLOAD_WRITTEN;
       payloadWrittenTotal += b.length;
       return b.length;
   }

   /**
    * Append the partial content of a byte array to the payload content.
    * @param b byte array with partial data to be written
    * @param offset offset to data to be written
    * @param len length of data to be written
    * @return number of bytes written during method invocation
    * @throws IOException if an I/O exception occurs while writing payload data
    */
   public long writePayload(byte[] b, int offset, int len) throws IOException {
       if (state != S_HEADER_WRITTEN && state != S_PAYLOAD_WRITTEN) {
           throw new IllegalStateException("Write a header before writing payload!");
       }
       out.write(b, offset, len);
       state = S_PAYLOAD_WRITTEN;
       payloadWrittenTotal += len;
       return len;
   }

}
