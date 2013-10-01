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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;

import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.UriProfile;

/**
 * Base class for WARC writer implementations.
 *
 * @author nicl
 */
public abstract class WarcWriter {

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

    /** WARC-Target-URI profile. */
    protected UriProfile warcTargetUriProfile;

    /** URI profile. */
    protected UriProfile uriProfile;

    /** Block Digesting enabled/disabled. */
    //protected boolean bDigestBlock = false;

    /** WARC <code>DateFormat</code> as specified by the WARC ISO standard. */
    protected DateFormat warcDateFormat;

    /** WARC field parser used. */
    protected WarcFieldParsers fieldParsers;

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

    /** Outputstream used to write WARC records. */
    protected OutputStream out;

    /** Current WARC header written. */
    protected WarcHeader header;

    /** Content-Length from the WARC header. */
    protected Long headerContentLength;

    /** Total bytes written for current record payload. */
    protected long payloadWrittenTotal;

    /**
     * Method used to initialize a readers internal state.
     * Must be called by all constructors.
     */
    protected void init() {
        warcTargetUriProfile = UriProfile.RFC3986;
        uriProfile = UriProfile.RFC3986;
        warcDateFormat = WarcDateParser.getDateFormat();
        fieldParsers = new WarcFieldParsers();
        stream_copy_buffer = new byte[8192];
        bExceptionOnContentLengthMismatch = true;
    }

    /**
     * Is this writer producing compressed output.
     * @return boolean indicating whether compressed output is produced
     */
    public abstract boolean isCompressed();

    /**
     * Set the URI profile used to validate WARC-Target URIs.
     * If null, the uriProfile is set to RCF3986.
     * @param uriProfile URI profile to use
     */
    public void setWarcTargetUriProfile(UriProfile uriProfile) {
        if (uriProfile == null) {
            uriProfile = UriProfile.RFC3986;
        }
        this.warcTargetUriProfile = uriProfile;
    }

    /**
     * Get the URI profile used to validate WARC-Target URIs.
     * @return the URI profile used to validate WARC-Target URIs
     */
    public UriProfile getWarcTargetUriProfile() {
        return warcTargetUriProfile;
    }

    /**
     * Set the URI profile used to validate URIs.
     * If null, the uriProfile is set to RCF3986.
     * @param uriProfile URI profile to use
     */
    public void setUriProfile(UriProfile uriProfile) {
        if (uriProfile == null) {
            uriProfile = UriProfile.RFC3986;
        }
        this.uriProfile = uriProfile;
    }

    /**
     * Get the URI profile used to validate URIs.
     * @return the URI profile used to validate URIs
     */
    public UriProfile getUriProfile() {
        return uriProfile;
    }

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
     * Is this writer set to block digest payload.
     * @return boolean indicating payload block digesting
     */
    /*
    public boolean digestBlock() {
        return bDigestBlock;
    }
    */

    /**
     * Set the writers payload block digest mode
     * @param enabled boolean indicating digest on/off
     */
    /*
    public void setDigestBlock(boolean enabled) {
        bDigestBlock = enabled;
    }
    */

    /**
     * Close WARC writer and free its resources.
     * @throws IOException if an i/o exception occurs while closing the writer
     */
    public abstract void close() throws IOException;

    /**
     * Close the WARC record in an implementation specific way.
     * @throws IOException if an i/o exception occurs while closing the record
     */
    public abstract void closeRecord() throws IOException;

    /**
     * Closes the WARC record by writing two newlines and comparing the amount of
     * payload data streamed with the content-length supplied with the header.
     * @throws IOException if an i/o exception occurs while closing the record
     */
    protected void closeRecord_impl() throws IOException {
        Diagnosis diagnosis = null;
        out.write(WarcConstants.endMark);
        out.flush();
        if (headerContentLength == null) {
            diagnosis = new Diagnosis(
                    DiagnosisType.ERROR_EXPECTED,
                    "'" + WarcConstants.FN_CONTENT_LENGTH + "' header",
                    "Mandatory!");
        } else {
            if (headerContentLength != payloadWrittenTotal) {
                diagnosis = new Diagnosis(
                        DiagnosisType.INVALID_EXPECTED,
                        "'" + WarcConstants.FN_CONTENT_LENGTH + "' header",
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
     * Write a raw WARC header to the WARC output stream. Closes any previously
     * written record that has not been closed prior to this call.
     * Errors and warnings are reported on the writers diagnostics object.
     * @param header_bytes raw WARC header to output
     * @param contentLength the expected content-length to be written and validated
     * @throws IOException if an i/o exception occurs while writing header data
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
     * Write a WARC header to the WARC output stream.
     * Errors and warnings are reported on the records diagnostics object.
     * @param record WARC record to output
     * @return byte array version of header as it was written
     * @throws IOException if an i/o exception occurs while writing header data
     */
    public abstract byte[] writeHeader(WarcRecord record) throws IOException;

    /**
     * Write a WARC header to the WARC output stream.
     * The WARC header is not required to be valid.
     * Errors and warnings are reported on the records diagnostics object.
     * @param record WARC record to output
     * @return byte array version of header as it was written
     * @throws IOException if an i/o exception occurs while writing header data
     */
    protected byte[] writeHeader_impl(WarcRecord record) throws IOException {
        header = record.header;
        headerContentLength = header.contentLength;
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        /*
         * Version Line
         */
        byte[] magicVersion = (WarcConstants.WARC_MAGIC_HEADER + header.major + "." + header.minor + "\r\n").getBytes();
        outBuf.write(magicVersion);
        /*
         * Warc-Type
         */
        String warcTypeStr = null;
        if (header.warcTypeIdx != null) {
            if (header.warcTypeIdx > 0
                && header.warcTypeIdx < WarcConstants.RT_IDX_STRINGS.length) {
                warcTypeStr = WarcConstants.RT_IDX_STRINGS[header.warcTypeIdx];
            } else {
                // Warning...
            }
        }
        if (warcTypeStr == null) {
            warcTypeStr = header.warcTypeStr;
        }
        if (warcTypeStr != null) {
            outBuf.write(WarcConstants.FN_WARC_TYPE.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcTypeStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Record-Id
         */
        String warcRecordIdStr = null;
        if (header.warcRecordIdUri != null) {
            warcRecordIdStr = header.warcRecordIdUri.toString();
        } else if (header.warcRecordIdStr != null) {
            warcRecordIdStr = header.warcRecordIdStr;
            // Warning...
        }
        if (warcRecordIdStr != null) {
            outBuf.write(WarcConstants.FN_WARC_RECORD_ID.getBytes());
            outBuf.write(": <".getBytes());
            outBuf.write(warcRecordIdStr.getBytes());
            outBuf.write(">\r\n".getBytes());
        }
        /*
         * Warc-Date
         */
        String warcDateStr = null;
        if (header.warcDate != null) {
            warcDateStr = warcDateFormat.format(header.warcDate);
        } else if (header.warcDateStr != null) {
            warcDateStr = header.warcDateStr;
            // Warning...
        }
        if (warcDateStr != null) {
            outBuf.write(WarcConstants.FN_WARC_DATE.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcDateStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Content-Length
         */
        String contentLengthStr = null;
        if (header.contentLength != null) {
            contentLengthStr = header.contentLength.toString();
        } else if (header.contentLengthStr != null) {
            contentLengthStr = header.contentLengthStr;
            // Warning...
        }
        if (contentLengthStr != null) {
            outBuf.write(WarcConstants.FN_CONTENT_LENGTH.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(contentLengthStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Content-Type
         */
        String contentTypeStr = null;
        if (header.contentType != null) {
            contentTypeStr = header.contentType.toString();
        } else if (header.contentTypeStr != null) {
            contentTypeStr = header.contentTypeStr;
            // Warning...
        }
        if (contentTypeStr != null) {
            outBuf.write(WarcConstants.FN_CONTENT_TYPE.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(contentTypeStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Concurrent-To
         */
        WarcConcurrentTo warcConcurrentTo;
        String warcConcurrentToStr;
        if (header.warcConcurrentToList != null) {
            for (int i=0; i<header.warcConcurrentToList.size(); ++i) {
                warcConcurrentTo = header.warcConcurrentToList.get(i);
                warcConcurrentToStr = null;
                if (warcConcurrentTo.warcConcurrentToUri != null) {
                    warcConcurrentToStr = warcConcurrentTo.warcConcurrentToUri.toString();
                } else if (warcConcurrentTo.warcConcurrentToStr != null) {
                    warcConcurrentToStr = warcConcurrentTo.warcConcurrentToStr;
                    // Warning...
                }
                if (warcConcurrentToStr != null) {
                    outBuf.write(WarcConstants.FN_WARC_CONCURRENT_TO.getBytes());
                    outBuf.write(": <".getBytes());
                    outBuf.write(warcConcurrentToStr.getBytes());
                    outBuf.write(">\r\n".getBytes());
                }
            }
        }
        /*
         * Warc-Block-Digest
         */
        String warcBlockDigestStr = null;
        if (header.warcBlockDigest != null) {
            warcBlockDigestStr = header.warcBlockDigest.toString();
        } else if (header.warcBlockDigestStr != null) {
            warcBlockDigestStr = header.warcBlockDigestStr;
            // Warning...
        }
        if (warcBlockDigestStr != null) {
            outBuf.write(WarcConstants.FN_WARC_BLOCK_DIGEST.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcBlockDigestStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Payload-Digest
         */
        String warcPayloadDigestStr = null;
        if (header.warcPayloadDigest != null) {
            warcPayloadDigestStr = header.warcPayloadDigest.toString();
        } else if (header.warcPayloadDigestStr != null) {
            warcPayloadDigestStr = header.warcPayloadDigestStr;
            // Warning...
        }
        if (warcPayloadDigestStr != null) {
            outBuf.write(WarcConstants.FN_WARC_PAYLOAD_DIGEST.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcPayloadDigestStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Ip-Address
         */
        String warcIpAddress = null;
        if (header.warcInetAddress != null) {
            warcIpAddress = header.warcInetAddress.getHostAddress();
        } else if (header.warcIpAddress != null) {
            warcIpAddress = header.warcIpAddress;
            // Warning...
        }
        if (warcIpAddress != null) {
            outBuf.write(WarcConstants.FN_WARC_IP_ADDRESS.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcIpAddress.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Refers-To
         */
        String warcRefersToUriStr = null;
        if (header.warcRefersToUri != null) {
            warcRefersToUriStr = header.warcRefersToUri.toString();
        } else if (header.warcRefersToStr != null) {
            warcRefersToUriStr = header.warcRefersToStr;
            // Warning...
        }
        if (warcRefersToUriStr != null) {
            outBuf.write(WarcConstants.FN_WARC_REFERS_TO.getBytes());
            outBuf.write(": <".getBytes());
            outBuf.write(warcRefersToUriStr.getBytes());
            outBuf.write(">\r\n".getBytes());
        }
        /*
         * Warc-Target-Uri
         */
        String warcTargetUriStr = null;
        if (header.warcTargetUriUri != null) {
            warcTargetUriStr = header.warcTargetUriUri.toString();
        } else if (header.warcTargetUriStr != null) {
            warcTargetUriStr = header.warcTargetUriStr;
            // Warning...
        }
        if (warcTargetUriStr != null) {
            outBuf.write(WarcConstants.FN_WARC_TARGET_URI.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcTargetUriStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Truncated
         */
        String warcTruncatedStr = null;
        if (header.warcTruncatedIdx != null) {
            if (header.warcTruncatedIdx > 0
                    && header.warcTruncatedIdx < WarcConstants.TT_IDX_STRINGS.length) {
                warcTruncatedStr = WarcConstants.TT_IDX_STRINGS[header.warcTruncatedIdx];
            } else {
                // Warning...
            }
        }
        if (warcTruncatedStr == null) {
            warcTruncatedStr = header.warcTruncatedStr;
        }
        if (warcTruncatedStr != null) {
            outBuf.write(WarcConstants.FN_WARC_TRUNCATED.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcTruncatedStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Warcinfo-Id
         */
        String warcWarcInfoIdStr = null;
        if (header.warcWarcinfoIdUri != null) {
            warcWarcInfoIdStr = header.warcWarcinfoIdUri.toString();
        } else if (header.warcWarcinfoIdStr != null) {
            warcWarcInfoIdStr = header.warcWarcinfoIdStr;
            // Warning...
        }
        if (warcWarcInfoIdStr != null) {
            outBuf.write(WarcConstants.FN_WARC_WARCINFO_ID.getBytes());
            outBuf.write(": <".getBytes());
            outBuf.write(warcWarcInfoIdStr.getBytes());
            outBuf.write(">\r\n".getBytes());
        }
        /*
         * Warc-Filename
         */
        if (header.warcFilename != null) {
            outBuf.write(WarcConstants.FN_WARC_FILENAME.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(header.warcFilename.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Profile
         */
        String warcProfileStr = null;
        if (header.warcProfileUri != null) {
            warcProfileStr = header.warcProfileUri.toString();
        } else if (header.warcProfileIdx != null) {
            if (header.warcProfileIdx > 0
                    && header.warcProfileIdx < WarcConstants.P_IDX_STRINGS.length) {
                warcProfileStr = WarcConstants.P_IDX_STRINGS[header.warcProfileIdx];
            } else {
                // Warning...
            }
        }
        if (warcProfileStr == null) {
            warcProfileStr = header.warcProfileStr;
            // Warning...
        }
        if (warcProfileStr != null) {
            outBuf.write(WarcConstants.FN_WARC_PROFILE.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcProfileStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Identified-Payload-Type
         */
        String warcIdentifiedPayloadTypeStr = null;
        if (header.warcIdentifiedPayloadType != null) {
            warcIdentifiedPayloadTypeStr = header.warcIdentifiedPayloadType.toString();
        } else if (header.warcIdentifiedPayloadTypeStr != null) {
            warcIdentifiedPayloadTypeStr = header.warcIdentifiedPayloadTypeStr;
            // Warning...
        }
        if (warcIdentifiedPayloadTypeStr != null) {
            outBuf.write(WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcIdentifiedPayloadTypeStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Segment-Number
         */
        String warcSegmentNumberStr = null;
        if (header.warcSegmentNumber != null) {
            warcSegmentNumberStr = header.warcSegmentNumber.toString();
        } else if (header.warcSegmentNumberStr != null) {
            warcSegmentNumberStr = header.warcSegmentNumberStr;
            // Warning...
        }
        if (warcSegmentNumberStr != null) {
            outBuf.write(WarcConstants.FN_WARC_SEGMENT_NUMBER.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcSegmentNumberStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Segment-Origin-Id
         */
        String warcSegmentOriginIdStr = null;
        if (header.warcSegmentOriginIdUrl != null) {
            warcSegmentOriginIdStr = header.warcSegmentOriginIdUrl.toString();
        } else if (header.warcSegmentOriginIdStr != null) {
            warcSegmentOriginIdStr = header.warcSegmentOriginIdStr;
            // Warning...
        }
        if (warcSegmentOriginIdStr != null) {
            outBuf.write(WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID.getBytes());
            outBuf.write(": <".getBytes());
            outBuf.write(warcSegmentOriginIdStr.getBytes());
            outBuf.write(">\r\n".getBytes());
        }
        /*
         * Warc-Segment-Total-Length
         */
        String warcSegmentTotalLengthStr = null;
        if (header.warcSegmentTotalLength != null) {
            warcSegmentTotalLengthStr = header.warcSegmentTotalLength.toString();
        } else if (header.warcSegmentTotalLengthStr != null) {
            warcSegmentTotalLengthStr = header.warcSegmentTotalLengthStr;
            // Warning...
        }
        if (warcSegmentTotalLengthStr != null) {
            outBuf.write(WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcSegmentTotalLengthStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        
        /*
         * WARC-Refers-To-Target-URI
         */
        String warcRefersToTargetUriStr = null;
        if (header.warcRefersToTargetUriUri != null) {
            warcRefersToTargetUriStr = header.warcRefersToTargetUriUri.toString();
        } else if (header.warcRefersToTargetUriStr != null) {
            warcRefersToTargetUriStr = header.warcRefersToTargetUriStr;
        }
        if (warcRefersToTargetUriStr != null) {
            outBuf.write(WarcConstants.FN_WARC_REFERS_TO_TARGET_URI.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcTargetUriStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
        
        /*
         * Warc-Refers-To-Date
         */
        String warcRefersToDateStr = null;
        if (header.warcRefersToDate != null) {
            warcRefersToDateStr = warcDateFormat.format(header.warcRefersToDate);
        } else if (header.warcDateStr != null) {
            warcRefersToDateStr = header.warcRefersToDateStr;
            // Warning...
        }
        if (warcRefersToDateStr != null) {
            outBuf.write(WarcConstants.FN_WARC_REFERS_TO_DATE.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcRefersToDateStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }

        
        /*
         * End Of Header
         */
        outBuf.write("\r\n".getBytes());
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
     * @throws IOException if an i/o exception occurs while writing payload data
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
     * @throws IOException if an i/o exception occurs while writing payload data
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
     * @throws IOException if an i/o exception occurs while writing payload data
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
