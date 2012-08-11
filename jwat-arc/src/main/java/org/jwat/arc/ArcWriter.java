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

import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;

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
    protected DateFormat arcDateFormat;

    /** WARC field parser used. */
    protected ArcFieldParsers fieldParsers;

    /** Buffer used by streamPayload() to copy from one stream to another. */
    protected byte[] stream_copy_buffer;

    /** Configuration for throwing exception on content-length mismatch.
     *  (Default is true) */
    protected boolean bExceptionOnContentLengthMismatch;

    /** Writer level errors and warnings or when writing byte headers. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /** Current state of writer. */
    protected int state = S_INIT;

    /** Outputstream used to write ARC records. */
    protected OutputStream out;

    protected ArcHeader header;

    /** Content-Length from the WARC header. */
    protected Long headerContentLength;

    /** Total bytes written for current record payload. */
    protected long payloadWrittenTotal;

    /**
     * Method used to initialize a readers internal state.
     * Must be called by all constructors.
     */
    protected void init() {
        arcDateFormat = ArcDateParser.getDateFormat();
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
     */
    public abstract void close() throws IOException;

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
        Diagnostics<Diagnosis> diagnosticsUsed;
        out.write(ArcConstants.endMark);
        if (headerContentLength == null) {
            if (header != null) {
                diagnosticsUsed = header.diagnostics;
            } else {
                diagnosticsUsed = diagnostics;
            }
            diagnosticsUsed.addError(new Diagnosis(
                    DiagnosisType.ERROR_EXPECTED,
                    "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' header",
                    "Mandatory!"));
            if (bExceptionOnContentLengthMismatch) {
                throw new IllegalStateException("Payload size does not match content-length!");
            }
        } else {
            if (headerContentLength != payloadWrittenTotal) {
                if (header != null) {
                    diagnosticsUsed = header.diagnostics;
                } else {
                    diagnosticsUsed = diagnostics;
                }
                diagnosticsUsed.addError(new Diagnosis(
                        DiagnosisType.INVALID_EXPECTED,
                        "'" + ArcConstants.FN_ARCHIVE_LENGTH + "' header",
                        Long.toString(payloadWrittenTotal),
                        headerContentLength.toString()));
                if (bExceptionOnContentLengthMismatch) {
                    throw new IllegalStateException("Payload size does not match content-length!");
                }
            }
        }
        header = null;
        headerContentLength = null;
    }

    /**
     * Write a raw ARC header to the ARC output stream.
     * @param header_bytes raw ARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    public void writeHeader(byte[] header_bytes, Long contentLength) throws IOException {
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
     * @param record ARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    public abstract byte[] writeHeader(ArcRecordBase record) throws IOException;

    /**
     * Write an ARC header to the ARC output stream.
     * @param record ARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    protected byte[] writeHeader_impl(ArcRecordBase record) throws IOException {
        /*
         * Version block.
         */
        byte[] versionBytes = null;
        if (record.recordType == ArcRecordBase.RT_VERSION_BLOCK) {
            ByteArrayOutputStream versionBuf = new ByteArrayOutputStream();
            versionBuf.write(Integer.toString(record.versionHeader.versionNumber).getBytes());
            versionBuf.write(" ".getBytes());
            versionBuf.write(Integer.toString(record.versionHeader.reserved).getBytes());
            versionBuf.write(" ".getBytes());
            versionBuf.write(record.versionHeader.originCode.getBytes());
            versionBuf.write("\n".getBytes());
            switch (record.versionHeader.blockDescVersion) {
            case 1:
                versionBuf.write(ArcConstants.VERSION_1_BLOCK_DEF.getBytes());
                versionBuf.write("\n".getBytes());
                break;
            case 2:
                versionBuf.write(ArcConstants.VERSION_2_BLOCK_DEF.getBytes());
                versionBuf.write("\n".getBytes());
                break;
            default:
                throw new IllegalStateException("Invalid block description version!");
            }
            versionBytes = versionBuf.toByteArray();
        }
        /*
         * Record line.
         */
        header = record.header;
        headerContentLength = header.archiveLength;
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
            archiveDateStr = arcDateFormat.format(header.archiveDateStr);
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

        if (versionBytes != null) {
            writePayload(versionBytes);
        }

        return headerBytes;
    }

    /**
    * TODO javadocs.
    * @param in input stream containing payload data
    * @return written length of payload data
    * @throws IOException if an exception occurs while writing payload data
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

   public long writePayload(byte[] b) throws IOException {
       if (state != S_HEADER_WRITTEN && state != S_PAYLOAD_WRITTEN) {
           throw new IllegalStateException("Write a header before writing payload!");
       }
       out.write(b);
       state = S_PAYLOAD_WRITTEN;
       payloadWrittenTotal += b.length;
       return b.length;
   }

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
