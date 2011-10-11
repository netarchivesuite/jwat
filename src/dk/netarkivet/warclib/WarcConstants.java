package dk.netarkivet.warclib;

import java.util.HashMap;
import java.util.Map;

public class WarcConstants {

	public static final String WARC_MAGIC_HEADER = "WARC/";

	public static final String FN_WARC_TYPE = "WARC-Type";
	public static final String FN_WARC_RECORD_ID = "WARC-Record-ID";
	public static final String FN_WARC_DATE = "WARC-Date";
	public static final String FN_CONTENT_LENGTH = "Content-Length";
	public static final String FN_CONTENT_TYPE = "Content-Type";
	public static final String FN_WARC_CONCURRENT_TO = "WARC-Concurrent-To";
	public static final String FN_WARC_BLOCK_DIGEST = "WARC-Block-Digest";
	public static final String FN_WARC_PAYLOAD_DIGEST = "WARC-Payload-Digest";
	public static final String FN_WARC_IP_ADDRESS = "WARC-IP-Address";
	public static final String FN_WARC_REFERS_TO = "WARC-Refers-To";
	public static final String FN_WARC_TARGET_URI = "WARC-Target-URI";
	public static final String FN_WARC_TRUNCATED = "WARC-Truncated";
	public static final String FN_WARC_WARCINFO_ID = "WARC-Warcinfo-ID";
	public static final String FN_WARC_FILENAME = "WARC-Filename";
	public static final String FN_WARC_PROFILE = "WARC-Profile";
	public static final String FN_WARC_IDENTIFIED_PAYLOAD_TYPE = "WARC-Identified-Payload-Type";
	public static final String FN_WARC_SEGMENT_ORIGIN_ID = "WARC-Segment-Origin-ID";
	public static final String FN_WARC_SEGMENT_NUMBER = "WARC-Segment-Number";
	public static final String FN_WARC_SEGMENT_TOTAL_LENGTH = "WARC-Segment-Total-Length";

	public static final int FN_IDX_WARC_TYPE = 1;
	public static final int FN_IDX_WARC_RECORD_ID = 2;
	public static final int FN_IDX_WARC_DATE = 3;
	public static final int FN_IDX_CONTENT_LENGTH = 4;
	public static final int FN_IDX_CONTENT_TYPE = 5;
	public static final int FN_IDX_WARC_CONCURRENT_TO = 6;
	public static final int FN_IDX_WARC_BLOCK_DIGEST = 7;
	public static final int FN_IDX_WARC_PAYLOAD_DIGEST = 8;
	public static final int FN_IDX_WARC_IP_ADDRESS = 9;
	public static final int FN_IDX_WARC_REFERS_TO = 10;
	public static final int FN_IDX_WARC_TARGET_URI = 11;
	public static final int FN_IDX_WARC_TRUNCATED = 12;
	public static final int FN_IDX_WARC_WARCINFO_ID = 13;
	public static final int FN_IDX_WARC_FILENAME = 14;					// warcinfo only
	public static final int FN_IDX_WARC_PROFILE = 15;					// revisit only
	public static final int FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE = 16;
	public static final int FN_IDX_WARC_SEGMENT_ORIGIN_ID = 17;			// continuation only
	public static final int FN_IDX_WARC_SEGMENT_NUMBER = 18;
	public static final int FN_IDX_WARC_SEGMENT_TOTAL_LENGTH = 19;		//continuation only

	public static final Map<String, Integer> fieldNameIdxMap = new HashMap<String, Integer>();

	static {
		fieldNameIdxMap.put(FN_WARC_TYPE.toUpperCase(), FN_IDX_WARC_TYPE);
		fieldNameIdxMap.put(FN_WARC_RECORD_ID.toUpperCase(), FN_IDX_WARC_RECORD_ID);
		fieldNameIdxMap.put(FN_WARC_DATE.toUpperCase(), FN_IDX_WARC_DATE);
		fieldNameIdxMap.put(FN_CONTENT_LENGTH.toUpperCase(), FN_IDX_CONTENT_LENGTH);
		fieldNameIdxMap.put(FN_CONTENT_TYPE.toUpperCase(), FN_IDX_CONTENT_TYPE);
		fieldNameIdxMap.put(FN_WARC_CONCURRENT_TO.toUpperCase(), FN_IDX_WARC_CONCURRENT_TO);
		fieldNameIdxMap.put(FN_WARC_BLOCK_DIGEST.toUpperCase(), FN_IDX_WARC_BLOCK_DIGEST);
		fieldNameIdxMap.put(FN_WARC_PAYLOAD_DIGEST.toUpperCase(), FN_IDX_WARC_PAYLOAD_DIGEST);
		fieldNameIdxMap.put(FN_WARC_IP_ADDRESS.toUpperCase(), FN_IDX_WARC_IP_ADDRESS);
		fieldNameIdxMap.put(FN_WARC_REFERS_TO.toUpperCase(), FN_IDX_WARC_REFERS_TO);
		fieldNameIdxMap.put(FN_WARC_TARGET_URI.toUpperCase(), FN_IDX_WARC_TARGET_URI);
		fieldNameIdxMap.put(FN_WARC_TRUNCATED.toUpperCase(), FN_IDX_WARC_TRUNCATED);
		fieldNameIdxMap.put(FN_WARC_WARCINFO_ID.toUpperCase(), FN_IDX_WARC_WARCINFO_ID);
		fieldNameIdxMap.put(FN_WARC_FILENAME.toUpperCase(), FN_IDX_WARC_FILENAME);
		fieldNameIdxMap.put(FN_WARC_PROFILE.toUpperCase(), FN_IDX_WARC_PROFILE);
		fieldNameIdxMap.put(FN_WARC_IDENTIFIED_PAYLOAD_TYPE.toUpperCase(), FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE);
		fieldNameIdxMap.put(FN_WARC_SEGMENT_ORIGIN_ID.toUpperCase(), FN_IDX_WARC_SEGMENT_ORIGIN_ID);
		fieldNameIdxMap.put(FN_WARC_SEGMENT_NUMBER.toUpperCase(), FN_IDX_WARC_SEGMENT_NUMBER);
		fieldNameIdxMap.put(FN_WARC_SEGMENT_TOTAL_LENGTH.toUpperCase(), FN_IDX_WARC_SEGMENT_TOTAL_LENGTH);
	}

	public static final String RT_WARCINFO = "warcinfo";
	public static final String RT_RESPONSE = "response";
	public static final String RT_RESOURCE = "resource";
	public static final String RT_REQUEST = "request";
	public static final String RT_METADATA = "metadata";
	public static final String RT_REVISIT = "revisit";
	public static final String RT_CONVERSION = "conversion";
	public static final String RT_CONTINUATION = "continuation";

	public static final int RT_IDX_WARCINFO = 1;
	public static final int RT_IDX_RESPONSE = 2;
	public static final int RT_IDX_RESOURCE = 3;
	public static final int RT_IDX_REQUEST = 4;
	public static final int RT_IDX_METADATA = 5;
	public static final int RT_IDX_REVISIT = 6;
	public static final int RT_IDX_CONVERSION = 7;
	public static final int RT_IDX_CONTINUATION = 8;

	public static final Map<String, Integer> recordTypeIdxMap = new HashMap<String, Integer>();

	static {
		recordTypeIdxMap.put( RT_WARCINFO, RT_IDX_WARCINFO );
		recordTypeIdxMap.put( RT_RESPONSE, RT_IDX_RESPONSE );
		recordTypeIdxMap.put( RT_RESOURCE, RT_IDX_RESOURCE );
		recordTypeIdxMap.put( RT_REQUEST, RT_IDX_REQUEST );
		recordTypeIdxMap.put( RT_METADATA, RT_IDX_METADATA );
		recordTypeIdxMap.put( RT_REVISIT, RT_IDX_REVISIT );
		recordTypeIdxMap.put( RT_CONVERSION, RT_IDX_CONVERSION );
		recordTypeIdxMap.put( RT_CONTINUATION, RT_IDX_CONTINUATION );
	}

	public static final String PROFILE_IdENTICAL_PAYLOAD_DIGEST =
			"http://netpreserve.org/warc/1.0/revisit/identical-payload-digest";

	public static final String PROFILE_SERVER_NOT_MODIFIED =
			"http://netpreserve.org/warc/1.0/revisit/server-not-modified";

	//"application/warc"
	//"application/warc-fields"

	//"text/dns"
	//"application/http;msgtype=request"
	//"application/http;msgtype=response"

	/*
	WARC-Type
	WARC-Record-ID
	WARC-Date
	Content-Length
	Content-Type
	WARC-Concurrent-To
	WARC-Block-Digest
	WARC-Payload-Digest
	WARC-IP-Address
	WARC-Refers-To
	WARC-Target-URI
	WARC-Truncated
	WARC-Warcinfo-ID
	WARC-Filename
	WARC-Profile
	WARC-Identified-Payload-Type
	WARC-Segment-Origin-ID
	WARC-Segment-Number
	WARC-Segment-Total-Length
	*/

	//'warcinfo', 'response', 'resource', 'request', 'metadata', 'revisit', 'conversion', 'continuation'

	private WarcConstants() {
	}

}
