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

	/** WARC magic header byte array. */
    protected byte[] magicVersion = (WarcConstants.WARC_MAGIC_HEADER + "1.0\r\n").getBytes();

	/** WARC <code>DateFormat</code> as specified by the WARC ISO standard. */
    protected DateFormat warcDateFormat = WarcDateParser.getWarcDateFormat();

    protected byte[] endMark = "\r\n\r\n".getBytes();

    /** Outputstream used to write WARC records. */
    protected OutputStream out;

    /** Block Digesting enabled/disabled. */
    protected boolean bDigestBlock = false;

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
     * Write a raw WARC header to the WARC output stream.
     * @param header_bytes raw WARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    public void writeHeader(byte[] header_bytes) throws IOException {
        if (header_bytes == null) {
            throw new IllegalArgumentException(
                    "The 'header_bytes' parameter is null!");
        }
    	out.write(header_bytes);
    }

    /**
     * Write a WARC header to the WARC output stream.
     * @param record WARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    public abstract void writeHeader(WarcRecord record) throws IOException;

   	/**
     * Write a WARC header to the WARC output stream.
     * @param record WARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    protected void writeHeader_impl(WarcRecord record) throws IOException {
        /*
         * Version Line
         */
        out.write(magicVersion);
        /*
         * Warc-Type
         */
        String warcTypeStr = null;
        if (record.warcTypeIdx != null) {
            if (record.warcTypeIdx > 0
                && record.warcTypeIdx < WarcConstants.RT_IDX_STRINGS.length) {
                warcTypeStr = WarcConstants.RT_IDX_STRINGS[record.warcTypeIdx];
            } else if (record.warcTypeStr != null) {
                warcTypeStr = record.warcTypeStr;
                // Warning...
            }
        }
        if (warcTypeStr != null) {
            out.write(WarcConstants.FN_WARC_TYPE.getBytes());
            out.write(": ".getBytes());
            out.write(warcTypeStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Record-Id
         */
        String warcRecordIdStr = null;
        if (record.warcRecordIdUri != null) {
            warcRecordIdStr = record.warcRecordIdUri.toString();
        } else if (record.warcRecordIdStr != null) {
            warcRecordIdStr = record.warcRecordIdStr;
            // Warning...
        }
        if (warcRecordIdStr != null) {
            out.write(WarcConstants.FN_WARC_RECORD_ID.getBytes());
            out.write(": <".getBytes());
            out.write(warcRecordIdStr.getBytes());
            out.write(">\r\n".getBytes());
        }
        /*
         * Warc-Date
         */
        String warcDateStr = null;
        if (record.warcDate != null) {
            warcDateStr = warcDateFormat.format(record.warcDate);
        } else if (record.warcDateStr != null) {
            warcDateStr = record.warcDateStr;
            // Warning...
        }
        if (warcDateStr != null) {
            out.write(WarcConstants.FN_WARC_DATE.getBytes());
            out.write(": ".getBytes());
            out.write(warcDateStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Content-Length
         */
        String contentLengthStr = null;
        if (record.contentLength != null) {
            contentLengthStr = record.contentLength.toString();
        } else if (record.contentLengthStr != null) {
            contentLengthStr = record.contentLengthStr;
            // Warning...
        }
        if (contentLengthStr != null) {
            out.write(WarcConstants.FN_CONTENT_LENGTH.getBytes());
            out.write(": ".getBytes());
            out.write(contentLengthStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Content-Type
         */
        String contentTypeStr = null;
        if (record.contentType != null) {
            contentTypeStr = record.contentType.toString();
        } else if (record.contentTypeStr != null) {
            contentTypeStr = record.contentTypeStr;
            // Warning...
        }
        if (contentTypeStr != null) {
            out.write(WarcConstants.FN_CONTENT_TYPE.getBytes());
            out.write(": ".getBytes());
            out.write(contentTypeStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Concurrent-To-Uri
         */
        if (record.warcConcurrentToUriList != null) {
            // TODO
            for (int i=0; i<record.warcConcurrentToUriList.size(); ++i) {
                out.write(WarcConstants.FN_WARC_CONCURRENT_TO.getBytes());
                out.write(": <".getBytes());
                out.write(record.warcConcurrentToUriList.get(i).toString().getBytes());
                out.write(">\r\n".getBytes());
            }
        }
        /*
         * Warc-Block-Digest
         */
        String warcBlockDigestStr = null;
        if (record.warcBlockDigest != null) {
            warcBlockDigestStr = record.warcBlockDigest.toString();
        } else if (record.warcBlockDigestStr != null) {
            warcBlockDigestStr = record.warcBlockDigestStr;
            // Warning...
        }
        if (warcBlockDigestStr != null) {
            out.write(WarcConstants.FN_WARC_BLOCK_DIGEST.getBytes());
            out.write(": ".getBytes());
            out.write(warcBlockDigestStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Payload-Digest
         */
        String warcPayloadDigestStr = null;
        if (record.warcPayloadDigest != null) {
            warcPayloadDigestStr = record.warcPayloadDigest.toString();
        } else if (record.warcPayloadDigestStr != null) {
            warcPayloadDigestStr = record.warcPayloadDigestStr;
            // Warning...
        }
        if (warcPayloadDigestStr != null) {
            out.write(WarcConstants.FN_WARC_PAYLOAD_DIGEST.getBytes());
            out.write(": ".getBytes());
            out.write(warcPayloadDigestStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Ip-Address
         */
        String warcIpAddress = null;
        if (record.warcInetAddress != null) {
            warcIpAddress = record.warcInetAddress.getHostAddress();
        } else if (record.warcIpAddress != null) {
            warcIpAddress = record.warcIpAddress;
            // Warning...
        }
        if (warcIpAddress != null) {
            out.write(WarcConstants.FN_WARC_IP_ADDRESS.getBytes());
            out.write(": ".getBytes());
            out.write(warcIpAddress.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Refers-To
         */
        String warcRefersToUriStr = null;
        if (record.warcRefersToUri != null) {
            warcRefersToUriStr = record.warcRefersToUri.toString();
        } else if (record.warcRefersToStr != null) {
            warcRefersToUriStr = record.warcRefersToStr;
            // Warning...
        }
        if (warcRefersToUriStr != null) {
            out.write(WarcConstants.FN_WARC_REFERS_TO.getBytes());
            out.write(": <".getBytes());
            out.write(warcRefersToUriStr.getBytes());
            out.write(">\r\n".getBytes());
        }
        /*
         * Warc-Target-Uri
         */
        String warcTargetUriStr = null;
        if (record.warcTargetUriUri != null) {
            warcTargetUriStr = record.warcTargetUriUri.toString();
        } else if (record.warcTargetUriStr != null) {
            warcTargetUriStr = record.warcTargetUriStr;
            // Warning...
        }
        if (warcTargetUriStr != null) {
            out.write(WarcConstants.FN_WARC_TARGET_URI.getBytes());
            out.write(": ".getBytes());
            out.write(warcTargetUriStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Truncated
         */
        String warcTruncatedStr = null;
        if (record.warcTruncatedIdx != null) {
            if (record.warcTruncatedIdx > 0
                    && record.warcTruncatedIdx < WarcConstants.TT_IDX_STRINGS.length) {
                warcTruncatedStr = WarcConstants.TT_IDX_STRINGS[record.warcTruncatedIdx];
            } else if (record.warcTruncatedStr != null) {
                warcTruncatedStr = record.warcTruncatedStr;
                // Warning...
            }
        }
        if (warcTruncatedStr != null) {
            out.write(WarcConstants.FN_WARC_TRUNCATED.getBytes());
            out.write(": ".getBytes());
            out.write(warcTruncatedStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Warcinfo-Id
         */
        String warcWarcInfoIdStr = null;
        if (record.warcWarcInfoIdUri != null) {
            warcWarcInfoIdStr = record.warcWarcInfoIdUri.toString();
        } else if (record.warcWarcinfoIdStr != null) {
            warcWarcInfoIdStr = record.warcWarcinfoIdStr;
            // Warning...
        }
        if (warcWarcInfoIdStr != null) {
            out.write(WarcConstants.FN_WARC_WARCINFO_ID.getBytes());
            out.write(": <".getBytes());
            out.write(warcWarcInfoIdStr.getBytes());
            out.write(">\r\n".getBytes());
        }
        /*
         * Warc-Filename
         */
        if (record.warcFilename != null) {
            out.write(WarcConstants.FN_WARC_FILENAME.getBytes());
            out.write(": ".getBytes());
            out.write(record.warcFilename.toString().getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Profile
         */
        String warcProfileStr = null;
        if (record.warcProfileIdx != null) {
            if (record.warcProfileIdx > 0
                    && record.warcProfileIdx < WarcConstants.P_IDX_STRINGS.length) {
                warcProfileStr = WarcConstants.P_IDX_STRINGS[record.warcProfileIdx];
            } else if (record.warcProfileStr != null) {
                warcProfileStr = record.warcProfileStr;
                // Warning...
            }
        }
        if (warcProfileStr != null) {
            out.write(WarcConstants.FN_WARC_PROFILE.getBytes());
            out.write(": ".getBytes());
            out.write(warcProfileStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Identified-Payload-Type
         */
        String warcIdentifiedPayloadTypeStr = null;
        if (record.warcIdentifiedPayloadType != null) {
            warcIdentifiedPayloadTypeStr = record.warcIdentifiedPayloadType.toString();
        } else if (record.warcIdentifiedPayloadTypeStr != null) {
            warcIdentifiedPayloadTypeStr = record.warcIdentifiedPayloadTypeStr;
            // Warning...
        }
        if (warcIdentifiedPayloadTypeStr != null) {
            out.write(WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE.getBytes());
            out.write(": ".getBytes());
            out.write(warcIdentifiedPayloadTypeStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Segment-Number
         */
        String warcSegmentNumberStr = null;
        if (record.warcSegmentNumber != null) {
            warcSegmentNumberStr = record.warcSegmentNumber.toString();
        } else if (record.warcSegmentNumberStr != null) {
            warcSegmentNumberStr = record.warcSegmentNumberStr;
            // Warning...
        }
        if (warcSegmentNumberStr != null) {
            out.write(WarcConstants.FN_WARC_SEGMENT_NUMBER.getBytes());
            out.write(": ".getBytes());
            out.write(warcSegmentNumberStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * Warc-Segment-Origin-Id
         */
        String warcSegmentOriginIdStr = null;
        if (record.warcSegmentOriginIdUrl != null) {
            warcSegmentOriginIdStr = record.warcSegmentOriginIdUrl.toString();
        } else if (record.warcSegmentOriginIdStr != null) {
            warcSegmentOriginIdStr = record.warcSegmentOriginIdStr;
            // Warning...
        }
        if (warcSegmentOriginIdStr != null) {
            out.write(WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID.getBytes());
            out.write(": <".getBytes());
            out.write(warcSegmentOriginIdStr.getBytes());
            out.write(">\r\n".getBytes());
        }
        /*
         * Warc-Segment-Total-Length
         */
        String warcSegmentTotalLengthStr = null;
        if (record.warcSegmentTotalLength != null) {
            warcSegmentTotalLengthStr = record.warcSegmentTotalLength.toString();
        } else if (record.warcSegmentTotalLengthStr != null) {
            warcSegmentTotalLengthStr = record.warcSegmentTotalLengthStr;
            // Warning...
        }
        if (warcSegmentTotalLengthStr != null) {
            out.write(WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH.getBytes());
            out.write(": ".getBytes());
            out.write(warcSegmentTotalLengthStr.getBytes());
            out.write("\r\n".getBytes());
        }
        /*
         * End Of Header
         */
        out.write("\r\n".getBytes());
    }

    /**
     *
     * @param in input stream containing payload data
     * @param length payload length
     * @return written length of payload data
     * @throws IOException if an exception occurs while writing payload data
     */
    public long transferPayload(InputStream in, long length) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException(
                    "The 'in' parameter is null!");
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "The 'length' parameter is less than zero!");
        }
        long written = 0;
        byte[] buffer = new byte[1024];
        int read = 0;
        while (read != -1) {
            out.write(buffer, 0, read);
            written += read;
            read = in.read(buffer);
        }
        return written;
    }

    /**
     * Close the WARC record.
     * @throws IOException if an exception occurs while closing the record
     */
    public void closeRecord() throws IOException {
        out.write(endMark);
    }

}
