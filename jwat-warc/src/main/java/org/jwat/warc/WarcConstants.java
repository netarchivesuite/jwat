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

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing all relevant WARC constants and structures.
 * Including but not limited to field names and mime-types.
 * Also includes non statically initialized structures for validation.
 *
 * @author nicl
 */
public class WarcConstants {

    /**
     * This utility class does not require instantiation.
     */
    protected WarcConstants() {
    }

    /**
     * A WARC header block starts with this string including trailing version
     * information.
     * */
    public static final String WARC_MAGIC_HEADER = "WARC/";

    /** End mark used after each record consisting of two newlines. */
    protected static byte[] endMark = "\r\n\r\n".getBytes();

    /** WARC date format string as specified by the WARC ISO standard. */
    public static final String WARC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /** WARC digest format string as specified by the WARC ISO standard. */
    public static final String WARC_DIGEST_FORMAT = "<digest-algorithm>:<digest-encoded>";

    /** Content-type format string as specified in RFC2616.*/
    public static final String CONTENT_TYPE_FORMAT = "<type>/<sub-type>(; <argument>=<value>)*";

    /*
     * WARC content-types (MIME).
     */

    /** WARC mime type. */
    public static final String WARC_MIME_TYPE = "application/warc";

    /** Suggested content-type/media-type for metadata records and others. */
    public static final String CT_APP_WARC_FIELDS = "application/warc-fields";

    /** Suggested content-type for metadata records and others. */
    public static final String CONTENT_TYPE_METADATA = "application";

    /** Suggested media-type for metadata records and others. */
    public static final String MEDIA_TYPE_METADATA = "warc-fields";

    //"text/dns"
    //"application/http;msgtype=request"
    //"application/http;msgtype=response"

    /*
     * Voodoo magic constants.
     */

    /** Trailing newlines after each record as per the WARC ISO standard. */
    public static final int WARC_RECORD_TRAILING_NEWLINES = 2;

    /** Number of WARC fields (zero-indexed). */
    public static final int FN_MAX_NUMBER = 19+1;

    /** Number of WARC types (zero indexed). */
    public static final int RT_MAX_NUMBER = 8+1;

    /*
     * WARC field names.
     */

    /** Warc-type field name. */
    public static final String FN_WARC_TYPE = "WARC-Type";
    /** Warc-record-id field name. */
    public static final String FN_WARC_RECORD_ID = "WARC-Record-ID";
    /** Warc-date field name. */
    public static final String FN_WARC_DATE = "WARC-Date";
    /** Content-length field name. */
    public static final String FN_CONTENT_LENGTH = "Content-Length";
    /** Content-type field name. */
    public static final String FN_CONTENT_TYPE = "Content-Type";
    /** Warc-concurrent-to field name. */
    public static final String FN_WARC_CONCURRENT_TO = "WARC-Concurrent-To";
    /** Warc-block-digest field name. */
    public static final String FN_WARC_BLOCK_DIGEST = "WARC-Block-Digest";
    /** Warc-payload-digest field name. */
    public static final String FN_WARC_PAYLOAD_DIGEST = "WARC-Payload-Digest";
    /** Warc-ip-address field name. */
    public static final String FN_WARC_IP_ADDRESS = "WARC-IP-Address";
    /** Warc-refers-to field name. */
    public static final String FN_WARC_REFERS_TO = "WARC-Refers-To";
    /** Warc-target-uri field name. */
    public static final String FN_WARC_TARGET_URI = "WARC-Target-URI";
    /** Warc-truncated field name. */
    public static final String FN_WARC_TRUNCATED = "WARC-Truncated";
    /** Warc-warcinfo-id field name. */
    public static final String FN_WARC_WARCINFO_ID = "WARC-Warcinfo-ID";
    /** Warc-filename field name. */
    public static final String FN_WARC_FILENAME = "WARC-Filename";
    /** Warc-profile field name. */
    public static final String FN_WARC_PROFILE = "WARC-Profile";
    /** Warc-identified-payload-type field name. */
    public static final String FN_WARC_IDENTIFIED_PAYLOAD_TYPE = "WARC-Identified-Payload-Type";
    /** Warc-segment-origin-id field name. */
    public static final String FN_WARC_SEGMENT_ORIGIN_ID = "WARC-Segment-Origin-ID";
    /** Warc-segment-number field name. */
    public static final String FN_WARC_SEGMENT_NUMBER = "WARC-Segment-Number";
    /** Warc-segment-totalt-length field name. */
    public static final String FN_WARC_SEGMENT_TOTAL_LENGTH = "WARC-Segment-Total-Length";
    /** WARC-Refers-To-Target-URI field name */
    public static final String FN_WARC_REFERS_TO_TARGET_URI = "WARC-Refers-To-Target-URI";
    /** WARC-Refers-To-Date field name */
    public static final String FN_WARC_REFERS_TO_DATE = "WARC-Refers-To-Date";

    /** WARC field name id to field name mapping table.
     *  Zero indexed array with all indexes used > 1. (Index 0 is unused) */
    public static final String[] FN_IDX_STRINGS = {
        null,
        FN_WARC_TYPE,
        FN_WARC_RECORD_ID,
        FN_WARC_DATE,
        FN_CONTENT_LENGTH,
        FN_CONTENT_TYPE,
        FN_WARC_CONCURRENT_TO,
        FN_WARC_BLOCK_DIGEST,
        FN_WARC_PAYLOAD_DIGEST,
        FN_WARC_IP_ADDRESS,
        FN_WARC_REFERS_TO,
        FN_WARC_TARGET_URI,
        FN_WARC_TRUNCATED,
        FN_WARC_WARCINFO_ID,
        FN_WARC_FILENAME,
        FN_WARC_PROFILE,
        FN_WARC_IDENTIFIED_PAYLOAD_TYPE,
        FN_WARC_SEGMENT_ORIGIN_ID,
        FN_WARC_SEGMENT_NUMBER,
        FN_WARC_SEGMENT_TOTAL_LENGTH
    };

    /** Warc reader warc-type field name id. */
    public static final int FN_IDX_WARC_TYPE = 1;
    /** Warc reader warc-record-id field name id. */
    public static final int FN_IDX_WARC_RECORD_ID = 2;
    /** Warc reader warc-date field name id. */
    public static final int FN_IDX_WARC_DATE = 3;
    /** Warc reader content-length field name id. */
    public static final int FN_IDX_CONTENT_LENGTH = 4;
    /** Warc reader content-type field name id. */
    public static final int FN_IDX_CONTENT_TYPE = 5;
    /** Warc reader warc-concurrent-to field name id. */
    public static final int FN_IDX_WARC_CONCURRENT_TO = 6;
    /** Warc reader warc-block-digest field name id. */
    public static final int FN_IDX_WARC_BLOCK_DIGEST = 7;
    /** Warc reader warc-payload-digest field name id. */
    public static final int FN_IDX_WARC_PAYLOAD_DIGEST = 8;
    /** Warc reader warc-ip-address field name id. */
    public static final int FN_IDX_WARC_IP_ADDRESS = 9;
    /** Warc reader warc-refers-to field name id. */
    public static final int FN_IDX_WARC_REFERS_TO = 10;
    /** Warc reader warc-target-uri field name id. */
    public static final int FN_IDX_WARC_TARGET_URI = 11;
    /** Warc reader warc-truncated field name id. */
    public static final int FN_IDX_WARC_TRUNCATED = 12;
    /** Warc reader warc-warcinfo-id field name id. */
    public static final int FN_IDX_WARC_WARCINFO_ID = 13;
    /** Warc reader warc-filename field name id. */
    public static final int FN_IDX_WARC_FILENAME = 14;                    // warcinfo only
    /** Warc reader warc-profile field name id. */
    public static final int FN_IDX_WARC_PROFILE = 15;                    // revisit only
    /** Warc reader warc-identified-payload-type field name id. */
    public static final int FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE = 16;
    /** Warc reader warc-segment-origin-id field name id. */
    public static final int FN_IDX_WARC_SEGMENT_ORIGIN_ID = 17;            // continuation only
    /** Warc reader warc-segment-number field name id. */
    public static final int FN_IDX_WARC_SEGMENT_NUMBER = 18;
    /** Warc reader warc-segment-totalt-length field name id. */
    public static final int FN_IDX_WARC_SEGMENT_TOTAL_LENGTH = 19;        //continuation only

    /** Map used to identify known warc field names. */
    public static final Map<String, Integer> fieldNameIdxMap = new HashMap<String, Integer>();

    /**
     * Populate map of known WARC field names.
     */
    static {
        fieldNameIdxMap.put(FN_WARC_TYPE.toLowerCase(), FN_IDX_WARC_TYPE);
        fieldNameIdxMap.put(FN_WARC_RECORD_ID.toLowerCase(), FN_IDX_WARC_RECORD_ID);
        fieldNameIdxMap.put(FN_WARC_DATE.toLowerCase(), FN_IDX_WARC_DATE);
        fieldNameIdxMap.put(FN_CONTENT_LENGTH.toLowerCase(), FN_IDX_CONTENT_LENGTH);
        fieldNameIdxMap.put(FN_CONTENT_TYPE.toLowerCase(), FN_IDX_CONTENT_TYPE);
        fieldNameIdxMap.put(FN_WARC_CONCURRENT_TO.toLowerCase(), FN_IDX_WARC_CONCURRENT_TO);
        fieldNameIdxMap.put(FN_WARC_BLOCK_DIGEST.toLowerCase(), FN_IDX_WARC_BLOCK_DIGEST);
        fieldNameIdxMap.put(FN_WARC_PAYLOAD_DIGEST.toLowerCase(), FN_IDX_WARC_PAYLOAD_DIGEST);
        fieldNameIdxMap.put(FN_WARC_IP_ADDRESS.toLowerCase(), FN_IDX_WARC_IP_ADDRESS);
        fieldNameIdxMap.put(FN_WARC_REFERS_TO.toLowerCase(), FN_IDX_WARC_REFERS_TO);
        fieldNameIdxMap.put(FN_WARC_TARGET_URI.toLowerCase(), FN_IDX_WARC_TARGET_URI);
        fieldNameIdxMap.put(FN_WARC_TRUNCATED.toLowerCase(), FN_IDX_WARC_TRUNCATED);
        fieldNameIdxMap.put(FN_WARC_WARCINFO_ID.toLowerCase(), FN_IDX_WARC_WARCINFO_ID);
        fieldNameIdxMap.put(FN_WARC_FILENAME.toLowerCase(), FN_IDX_WARC_FILENAME);
        fieldNameIdxMap.put(FN_WARC_PROFILE.toLowerCase(), FN_IDX_WARC_PROFILE);
        fieldNameIdxMap.put(FN_WARC_IDENTIFIED_PAYLOAD_TYPE.toLowerCase(), FN_IDX_WARC_IDENTIFIED_PAYLOAD_TYPE);
        fieldNameIdxMap.put(FN_WARC_SEGMENT_ORIGIN_ID.toLowerCase(), FN_IDX_WARC_SEGMENT_ORIGIN_ID);
        fieldNameIdxMap.put(FN_WARC_SEGMENT_NUMBER.toLowerCase(), FN_IDX_WARC_SEGMENT_NUMBER);
        fieldNameIdxMap.put(FN_WARC_SEGMENT_TOTAL_LENGTH.toLowerCase(), FN_IDX_WARC_SEGMENT_TOTAL_LENGTH);
    }

    /** WARC String field datatype identifier. */
    public static final int FDT_STRING = 0;
    /** WARC Integer field datatype identifier. */
    public static final int FDT_INTEGER = 1;
    /** WARC Long field datatype identifier. */
    public static final int FDT_LONG = 2;
    /** WARC Digest field datatype identifier. */
    public static final int FDT_DIGEST = 3;
    /** WARC ContentType field datatype identifier. */
    public static final int FDT_CONTENTTYPE = 4;
    /** WARC Date field datatype identifier. */
    public static final int FDT_DATE = 5;
    /** WARC InetAddress field datatype identifier. */
    public static final int FDT_INETADDRESS = 6;
    /** WARC URI field datatype identifier. */
    public static final int FDT_URI = 7;

    /** WARC field datatype id to field datatype name mapping table. */
    public static final String[] FDT_IDX_STRINGS = {
        "String",
        "Integer",
        "Long",
        "Digest",
        "ContentType",
        "Date",
        "InetAddress",
        "URI"
    };

    /** Array to lookup WARC field datatypes. */
    public static final int[] FN_IDX_DT = {
        -1,
        FDT_STRING,
        FDT_URI,
        FDT_DATE,
        FDT_LONG,
        FDT_CONTENTTYPE,
        FDT_URI,
        FDT_DIGEST,
        FDT_DIGEST,
        FDT_INETADDRESS,
        FDT_URI,
        FDT_URI,
        FDT_STRING,
        FDT_URI,
        FDT_STRING,
        FDT_URI,
        FDT_CONTENTTYPE,
        FDT_URI,
        FDT_INTEGER,
        FDT_LONG
    };

    /*
     * WARC fields that can have multiple occurrences in a Warc header.
     */

    /** Lookup table of Warc fields that can have multiple occurrences. */
    public static final boolean[] fieldNamesRepeatableLookup = new boolean[FN_MAX_NUMBER];

    /**
     * Populate multiple occurrences lookup table.
     */
    static {
        fieldNamesRepeatableLookup[FN_IDX_WARC_CONCURRENT_TO] = true;
    }

    /*
     * WARC record types.
     */

    /** WARC-Type warcinfo id. */
    public static final String RT_WARCINFO = "warcinfo";
    /** WARC-Type response id. */
    public static final String RT_RESPONSE = "response";
    /** WARC-Type resource id. */
    public static final String RT_RESOURCE = "resource";
    /** WARC-Type request id. */
    public static final String RT_REQUEST = "request";
    /** WARC-Type metadata id. */
    public static final String RT_METADATA = "metadata";
    /** WARC-Type revisit id. */
    public static final String RT_REVISIT = "revisit";
    /** WARC-Type conversion id. */
    public static final String RT_CONVERSION = "conversion";
    /** WARC-Type continuation id. */
    public static final String RT_CONTINUATION = "continuation";

    /** WARC type id to field name mapping table.
     *  Zero indexed array with all indexes used > 1. (Index 0 is unused) */
    public static final String[] RT_IDX_STRINGS = {
        null,
        RT_WARCINFO,
        RT_RESPONSE,
        RT_RESOURCE,
        RT_REQUEST,
        RT_METADATA,
        RT_REVISIT,
        RT_CONVERSION,
        RT_CONTINUATION
    };

    /** Warc reader unknown warc record type id. */
    public static final int RT_IDX_UNKNOWN = 0;
    /** Warc reader warcinfo warc record type id. */
    public static final int RT_IDX_WARCINFO = 1;
    /** Warc reader response warc record type id. */
    public static final int RT_IDX_RESPONSE = 2;
    /** Warc reader resource warc record type id. */
    public static final int RT_IDX_RESOURCE = 3;
    /** Warc reader request warc record type id. */
    public static final int RT_IDX_REQUEST = 4;
    /** Warc reader metadata warc record type id. */
    public static final int RT_IDX_METADATA = 5;
    /** Warc reader revisit warc record type id. */
    public static final int RT_IDX_REVISIT = 6;
    /** Warc reader conversion warc record type id. */
    public static final int RT_IDX_CONVERSION = 7;
    /** Warc reader continuation warc record type id. */
    public static final int RT_IDX_CONTINUATION = 8;

    /** WARC-Type lookup map. */
    public static final Map<String, Integer> recordTypeIdxMap = new HashMap<String, Integer>();

    /**
     * Populate WARC-Type lookup map.
     */
    static {
        recordTypeIdxMap.put(RT_WARCINFO.toLowerCase(), RT_IDX_WARCINFO);
        recordTypeIdxMap.put(RT_RESPONSE.toLowerCase(), RT_IDX_RESPONSE);
        recordTypeIdxMap.put(RT_RESOURCE.toLowerCase(), RT_IDX_RESOURCE);
        recordTypeIdxMap.put(RT_REQUEST.toLowerCase(), RT_IDX_REQUEST);
        recordTypeIdxMap.put(RT_METADATA.toLowerCase(), RT_IDX_METADATA);
        recordTypeIdxMap.put(RT_REVISIT.toLowerCase(), RT_IDX_REVISIT);
        recordTypeIdxMap.put(RT_CONVERSION.toLowerCase(), RT_IDX_CONVERSION);
        recordTypeIdxMap.put(RT_CONTINUATION.toLowerCase(), RT_IDX_CONTINUATION);
    }

    /*
     * Truncation reason types.
     */

    /** WARC-Truncated length id. */
    public static final String TT_LENGTH = "length";
    /** WARC-Truncated time id*/
    public static final String TT_TIME = "time";
    /** WARC-Truncated disconnect id. */
    public static final String TT_DISCONNECT = "disconnect";
    /** WARC-Truncated unspecified id. */
    public static final String TT_UNSPECIFIED = "unspecified";

    /** WARC truncation reason id to field name mapping table.
     *  Zero indexed array with all indexes used > 1. (Index 0 is unused) */
    public static final String[] TT_IDX_STRINGS = {
        null,
        TT_LENGTH,
        TT_TIME,
        TT_DISCONNECT,
        TT_UNSPECIFIED
    };

    /** Warc reader future reason id. */
    public static final int TT_IDX_FUTURE_REASON = 0;
    /** Warc reader length reason id. */
    public static final int TT_IDX_LENGTH = 1;
    /** Warc reader time reason id. */
    public static final int TT_IDX_TIME = 2;
    /** Warc reader disconnect reason id. */
    public static final int TT_IDX_DISCONNECT = 3;
    /** Warc reader unspecified reason id. */
    public static final int TT_IDX_UNSPECIFIED = 4;

    /** Lookup map for known truncation reason id's. */
    public static final Map<String, Integer> truncatedTypeIdxMap = new HashMap<String, Integer>();

    /**
     * Populate truncation reason id lookup map.
     */
    static {
        truncatedTypeIdxMap.put(TT_LENGTH.toLowerCase(), TT_IDX_LENGTH);
        truncatedTypeIdxMap.put(TT_TIME.toLowerCase(), TT_IDX_TIME);
        truncatedTypeIdxMap.put(TT_DISCONNECT.toLowerCase(), TT_IDX_DISCONNECT);
        truncatedTypeIdxMap.put(TT_UNSPECIFIED.toLowerCase(), TT_IDX_UNSPECIFIED);
    }

    /*
     * Warc revisit profile ids used in the WARC-Profile header (See ISO).
     */

    /** Revisit WARC-Profile id for identical payload digest. */
    public static final String PROFILE_IDENTICAL_PAYLOAD_DIGEST =
            "http://netpreserve.org/warc/1.0/revisit/identical-payload-digest";

    /** Revisit WARC-Profile id for server not modified. */
    public static final String PROFILE_SERVER_NOT_MODIFIED =
            "http://netpreserve.org/warc/1.0/revisit/server-not-modified";

    /** WARC profile id to field name mapping table.
     *  Zero indexed array with all indexes used > 1. (Index 0 is unused) */
    public static final String[] P_IDX_STRINGS = {
        null,
        PROFILE_IDENTICAL_PAYLOAD_DIGEST,
        PROFILE_SERVER_NOT_MODIFIED
    };

    /*
     * Warc revisit profile ids returned by the warc reader.
     * The raw value is also available in case of unknown profiles.
     */

    /** Warc reader id for unknown profile. */
    public static final int PROFILE_IDX_UNKNOWN = 0;
    /** Warc reader id for identical payload digest profile. */
    public static final int PROFILE_IDX_IDENTICAL_PAYLOAD_DIGEST = 1;
    /** Warc reader id for server not modified profile. */
    public static final int PROFILE_IDX_SERVER_NOT_MODIFIED = 2;

    /** Profile lookup map used to identify WARC-Profile values. */
    public static final Map<String, Integer> profileIdxMap = new HashMap<String, Integer>();

    /**
     * Populate the lookup map with known WARC-Profile ids.
     */
    static {
        profileIdxMap.put(PROFILE_IDENTICAL_PAYLOAD_DIGEST.toLowerCase(),
                PROFILE_IDX_IDENTICAL_PAYLOAD_DIGEST);
        profileIdxMap.put(PROFILE_SERVER_NOT_MODIFIED.toLowerCase(),
                PROFILE_IDX_SERVER_NOT_MODIFIED);
    }

    /*
     * The different requirement levels as per RFC 2119.
     * (See http://www.ietf.org/rfc/rfc2119.txt)
     */

    /** Warc header can be ignored. */
    public static final int POLICY_IGNORE = 0;
    /** Warc header is mandatory (equal to shall). */
    public static final int POLICY_MANDATORY = 1;
    /** Warc header must be present. */
    public static final int POLICY_SHALL = 2;
    /** Warc header must not be present. */
    public static final int POLICY_SHALL_NOT = 3;
    /** Warc header can be present. */
    public static final int POLICY_MAY = 4;
    /** Warc header should not be present. */
    public static final int POLICY_MAY_NOT = 5;

    /** A (Warc-Types x Warc-Header-Fields) matrix used for policy validation.
     *  (See below) */
    public static final int[][] field_policy;

    /**
     * The following section initializes the policy matrix used to check the
     * usage of each known warc header line against each known warc record
     * type.
     * The ISO standard was used to build the data in the matrix.
     */
    static {
        field_policy = new int[RT_MAX_NUMBER][FN_MAX_NUMBER];

        // Warc-Record-id
        // Warc-Type
        // Warc-Date
        // Content-Length
        // Also required for unknown warc-types.
        for (int i=0; i<=8; ++i) {
            field_policy[i][FN_IDX_WARC_RECORD_ID] = POLICY_MANDATORY;
            field_policy[i][FN_IDX_WARC_TYPE] = POLICY_MANDATORY;
            field_policy[i][FN_IDX_WARC_DATE] = POLICY_MANDATORY;
            field_policy[i][FN_IDX_CONTENT_LENGTH] = POLICY_MANDATORY;
        }

        // Content-Type
        field_policy[RT_IDX_CONTINUATION][FN_IDX_CONTENT_TYPE] = POLICY_SHALL_NOT;

        // Warc-Ip-Address
        field_policy[RT_IDX_REQUEST][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_RESPONSE][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_RESOURCE][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_METADATA][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_IP_ADDRESS] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONVERSION][FN_IDX_WARC_IP_ADDRESS] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_IP_ADDRESS] = POLICY_SHALL_NOT;

        // Warc-Concurrent-To
        field_policy[RT_IDX_REQUEST][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_RESPONSE][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_RESOURCE][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_METADATA][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_CONCURRENT_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONVERSION][FN_IDX_WARC_CONCURRENT_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_CONCURRENT_TO] = POLICY_SHALL_NOT;

        // Warc-Refers-To
        field_policy[RT_IDX_METADATA][FN_IDX_WARC_REFERS_TO] = POLICY_MAY;
        field_policy[RT_IDX_CONVERSION][FN_IDX_WARC_REFERS_TO] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_REFERS_TO] = POLICY_MAY;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_REQUEST][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_RESPONSE][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_RESOURCE][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;

        // Warc-Target-Uri
        field_policy[RT_IDX_REQUEST][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_RESPONSE][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_RESOURCE][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_CONVERSION][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_METADATA][FN_IDX_WARC_TARGET_URI] = POLICY_MAY;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL_NOT;

        // Warc-Warcinfo-Id
        // Warc-Filename
        // Warc-Profile
        // Warc-Segment-Origin-Id
        // Warc-Segment-Total-Length
        for (int i=1; i<=8; ++i) {
            field_policy[i][FN_IDX_WARC_WARCINFO_ID] = POLICY_MAY;
            field_policy[i][FN_IDX_WARC_FILENAME] = POLICY_SHALL_NOT;
            field_policy[i][FN_IDX_WARC_PROFILE] = POLICY_IGNORE;
            field_policy[i][FN_IDX_WARC_SEGMENT_ORIGIN_ID] = POLICY_SHALL_NOT;
            field_policy[i][FN_IDX_WARC_SEGMENT_ORIGIN_ID] = POLICY_SHALL_NOT;
        }
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_WARCINFO_ID] = POLICY_MAY_NOT;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_FILENAME] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_PROFILE] = POLICY_MANDATORY;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_SEGMENT_ORIGIN_ID] = POLICY_MANDATORY;

        // Warc-Segment-Number
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_SEGMENT_NUMBER] = POLICY_MANDATORY;
    }

}
