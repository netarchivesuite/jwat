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

/**
 * Base class for WARC writer implementations.
 *
 * @author nicl
 */
public abstract class WarcWriter {

    protected static final int S_INIT = 0;

    protected static final int S_HEADER_WRITTEN = 1;

    protected static final int S_PAYLOAD_WRITTEN = 2;

    protected static final int S_RECORD_CLOSED = 3;

    /** WARC <code>DateFormat</code> as specified by the WARC ISO standard. */
    protected DateFormat warcDateFormat = WarcDateParser.getDateFormat();

    /** Current state of writer. */
    protected int state = S_INIT;

    /** Outputstream used to write WARC records. */
    protected OutputStream out;

    /** Buffer used by streamPayload() to copy from one stream to another. */
    protected byte[] stream_copy_buffer = new byte[8192];

    /** Block Digesting enabled/disabled. */
    protected boolean bDigestBlock = false;

    /** WARC field parser used. */
    protected WarcFieldParsers fieldParser;

    /**
     * Method used to initialize a readers internal state.
     * Must be called by all constructors.
     */
    protected void init() {
        fieldParser = new WarcFieldParsers();
    }

    /**
     * Is this writer producing compressed output.
     * @return boolean indicating whether compressed output is produced
     */
    public abstract boolean isCompressed();

    /**
     * Is this writer set to block digest payload.
     * @return boolean indicating payload block digesting
     */
    public boolean digestBlock() {
        return bDigestBlock;
    }

    /**
     * Set the writers payload block digest mode
     * @param enabled boolean indicating digest on/off
     */
    public void setDigestBlock(boolean enabled) {
        bDigestBlock = enabled;
    }

    /**
     * Close WARC writer and free its resources.
     */
    public abstract void close();

    /**
     * Close the WARC record.
     * @throws IOException if an exception occurs while closing the record
     */
    public abstract void closeRecord() throws IOException;

    /**
     * Close the WARC record by writing two newlines.
     * @throws IOException if an exception occurs while closing the record
     */
    protected void closeRecord_impl() throws IOException {
        out.write(WarcConstants.endMark);
    }

    /**
     * Write a raw WARC header to the WARC output stream.
     * @param header_bytes raw WARC record to output
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
     * Write a WARC header to the WARC output stream.
     * @param record WARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    public abstract byte[] writeHeader(WarcRecord record) throws IOException;

    /**
     * Write a WARC header to the WARC output stream.
     * @param record WARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    protected byte[] writeHeader_impl(WarcRecord record) throws IOException {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        /*
         * Version Line
         */
        byte[] magicVersion = (WarcConstants.WARC_MAGIC_HEADER + record.header.major + "." + record.header.minor + "\r\n").getBytes();
        outBuf.write(magicVersion);
        /*
         * Warc-Type
         */
        String warcTypeStr = null;
        if (record.header.warcTypeIdx != null) {
            if (record.header.warcTypeIdx > 0
                && record.header.warcTypeIdx < WarcConstants.RT_IDX_STRINGS.length) {
                warcTypeStr = WarcConstants.RT_IDX_STRINGS[record.header.warcTypeIdx];
            } else if (record.header.warcTypeStr != null) {
                warcTypeStr = record.header.warcTypeStr;
                // Warning...
            }
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
        if (record.header.warcRecordIdUri != null) {
            warcRecordIdStr = record.header.warcRecordIdUri.toString();
        } else if (record.header.warcRecordIdStr != null) {
            warcRecordIdStr = record.header.warcRecordIdStr;
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
        if (record.header.warcDate != null) {
            warcDateStr = warcDateFormat.format(record.header.warcDate);
        } else if (record.header.warcDateStr != null) {
            warcDateStr = record.header.warcDateStr;
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
        if (record.header.contentLength != null) {
            contentLengthStr = record.header.contentLength.toString();
        } else if (record.header.contentLengthStr != null) {
            contentLengthStr = record.header.contentLengthStr;
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
        if (record.header.contentType != null) {
            contentTypeStr = record.header.contentType.toString();
        } else if (record.header.contentTypeStr != null) {
            contentTypeStr = record.header.contentTypeStr;
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
        if (record.header.warcConcurrentToList != null) {
            for (int i=0; i<record.header.warcConcurrentToList.size(); ++i) {
                warcConcurrentTo = record.header.warcConcurrentToList.get(i);
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
        if (record.header.warcBlockDigest != null) {
            warcBlockDigestStr = record.header.warcBlockDigest.toString();
        } else if (record.header.warcBlockDigestStr != null) {
            warcBlockDigestStr = record.header.warcBlockDigestStr;
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
        if (record.header.warcPayloadDigest != null) {
            warcPayloadDigestStr = record.header.warcPayloadDigest.toString();
        } else if (record.header.warcPayloadDigestStr != null) {
            warcPayloadDigestStr = record.header.warcPayloadDigestStr;
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
        if (record.header.warcInetAddress != null) {
            warcIpAddress = record.header.warcInetAddress.getHostAddress();
        } else if (record.header.warcIpAddress != null) {
            warcIpAddress = record.header.warcIpAddress;
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
        if (record.header.warcRefersToUri != null) {
            warcRefersToUriStr = record.header.warcRefersToUri.toString();
        } else if (record.header.warcRefersToStr != null) {
            warcRefersToUriStr = record.header.warcRefersToStr;
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
        if (record.header.warcTargetUriUri != null) {
            warcTargetUriStr = record.header.warcTargetUriUri.toString();
        } else if (record.header.warcTargetUriStr != null) {
            warcTargetUriStr = record.header.warcTargetUriStr;
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
        if (record.header.warcTruncatedIdx != null) {
            if (record.header.warcTruncatedIdx > 0
                    && record.header.warcTruncatedIdx < WarcConstants.TT_IDX_STRINGS.length) {
                warcTruncatedStr = WarcConstants.TT_IDX_STRINGS[record.header.warcTruncatedIdx];
            } else if (record.header.warcTruncatedStr != null) {
                warcTruncatedStr = record.header.warcTruncatedStr;
                // Warning...
            }
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
        if (record.header.warcWarcInfoIdUri != null) {
            warcWarcInfoIdStr = record.header.warcWarcInfoIdUri.toString();
        } else if (record.header.warcWarcinfoIdStr != null) {
            warcWarcInfoIdStr = record.header.warcWarcinfoIdStr;
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
        if (record.header.warcFilename != null) {
            outBuf.write(WarcConstants.FN_WARC_FILENAME.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(record.header.warcFilename.toString().getBytes());
            outBuf.write("\r\n".getBytes());
        }
        /*
         * Warc-Profile
         */
        String warcProfileStr = null;
        if (record.header.warcProfileIdx != null) {
            if (record.header.warcProfileIdx > 0
                    && record.header.warcProfileIdx < WarcConstants.P_IDX_STRINGS.length) {
                warcProfileStr = WarcConstants.P_IDX_STRINGS[record.header.warcProfileIdx];
            } else if (record.header.warcProfileStr != null) {
                warcProfileStr = record.header.warcProfileStr;
                // Warning...
            }
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
        if (record.header.warcIdentifiedPayloadType != null) {
            warcIdentifiedPayloadTypeStr = record.header.warcIdentifiedPayloadType.toString();
        } else if (record.header.warcIdentifiedPayloadTypeStr != null) {
            warcIdentifiedPayloadTypeStr = record.header.warcIdentifiedPayloadTypeStr;
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
        if (record.header.warcSegmentNumber != null) {
            warcSegmentNumberStr = record.header.warcSegmentNumber.toString();
        } else if (record.header.warcSegmentNumberStr != null) {
            warcSegmentNumberStr = record.header.warcSegmentNumberStr;
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
        if (record.header.warcSegmentOriginIdUrl != null) {
            warcSegmentOriginIdStr = record.header.warcSegmentOriginIdUrl.toString();
        } else if (record.header.warcSegmentOriginIdStr != null) {
            warcSegmentOriginIdStr = record.header.warcSegmentOriginIdStr;
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
        if (record.header.warcSegmentTotalLength != null) {
            warcSegmentTotalLengthStr = record.header.warcSegmentTotalLength.toString();
        } else if (record.header.warcSegmentTotalLengthStr != null) {
            warcSegmentTotalLengthStr = record.header.warcSegmentTotalLengthStr;
            // Warning...
        }
        if (warcSegmentTotalLengthStr != null) {
            outBuf.write(WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH.getBytes());
            outBuf.write(": ".getBytes());
            outBuf.write(warcSegmentTotalLengthStr.getBytes());
            outBuf.write("\r\n".getBytes());
        }
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
