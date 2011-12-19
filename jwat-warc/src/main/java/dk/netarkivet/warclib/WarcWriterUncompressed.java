package dk.netarkivet.warclib;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Join the Darkside! Work in progress.
 *
 * @author nicl
 */
public class WarcWriterUncompressed extends WarcWriter {

	/** Outputstream used to write WARC records. */
	public OutputStream out;

	/**
	 * Construct an unbuffered WARC writer used to write uncompressed records.
	 * @param out outputstream to write to
	 */
	public WarcWriterUncompressed(OutputStream out) {
		this.out = out;
	}

	/**
	 * Construct a buffered WARC writer used to write uncompressed records.
	 * @param out outputstream to stream to
	 * @param buffer_size outputstream buffer size
	 */
	public WarcWriterUncompressed(OutputStream out, int buffer_size) {
		this.out = new BufferedOutputStream(out, buffer_size);
	}

	@Override
	public boolean isCompressed() {
		return false;
	}

	@Override
	public void close() {
		try {
			out.close();
		}
		catch (IOException e) {
			// TODO
		}
	}

	byte[] magicVersion = (WarcConstants.WARC_MAGIC_HEADER + "1.0\r\n").getBytes();

	@Override
	public void write(WarcRecord record) throws IOException {
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

	public long transfer(InputStream in, long length) throws IOException {
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

	public void closeRecord() throws IOException {
		out.write("\r\n".getBytes());
		out.write("\r\n".getBytes());
	}

}
