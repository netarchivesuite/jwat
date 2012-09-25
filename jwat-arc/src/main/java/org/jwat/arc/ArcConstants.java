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

/**
 * Class containing all relevant ARC constants and structures.
 * Including but not limited to field names and mime-types.
 *
 * @author lbihanic, selghissassi, nicl
 */
public final class ArcConstants {

    /**
     * No constructor for this utility class, static access only.
     */
    protected ArcConstants() {
    }

    /** Invalid ARC file property. */
    protected static final String ARC_FILE = "ARC file";

    /** Invalid ARC record property. */
    protected static final String ARC_RECORD = "ARC record";

    /** Invalid ARC version block property. */
    protected static final String ARC_VERSION_BLOCK = "ARC version block";

    /** Maximum payload size in bytes of unwanted trailing new lines in
     * V1.0/V2.0 version block. */
    public static final int ARC_VB_MAX_TRAILING_NEWLINES = 1024*1024;

    /** Trailing newlines after each record as per the ARC documentation. */
    public static final int ARC_RECORD_TRAILING_NEWLINES = 1;

    /** An ARC version block starts with this string. */
    public static final String ARC_MAGIC_HEADER = "filedesc:";

    /** Arc file URL URI scheme. */
    public static final String ARC_SCHEME = "filedesc";

    /** End mark used after each record consisting of one line feed. */
    protected static byte[] endMark = "\n".getBytes();

    /** Text plain content type. */
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    /** Special content-type for none. */
    public static final String CONTENT_TYPE_NO_TYPE = "no-type";

    /** Version block preferred content-type. */
    public static final String VERSION_BLOCK_CONTENT_TYPE = "text";

    /** Version block preferred media-type. */
    public static final String VERSION_BLOCK_MEDIA_TYPE = "plain";

    /** ARC date format string as specified in the ARC documentation. */
    public static final String ARC_DATE_FORMAT = "yyyyMMddHHmmss";

    /** Content-type format string as specified in RFC2616.*/
    public static final String CONTENT_TYPE_FORMAT = "<type>/<sub-type>(; <argument>=<value>)*";

    /**
     * Record block fields.
     */

    /** Url field name. */
    public static final String FN_URL = "URL";
    /** Ip-Address field name. */
    public static final String FN_IP_ADDRESS = "IP-address";
    /** Date field name. */
    public static final String FN_ARCHIVE_DATE = "Archive-date";
    /** Content-Type field name. */
    public static final String FN_CONTENT_TYPE = "Content-type";
    /** Result-Code field name. */
    public static final String FN_RESULT_CODE = "Result-code";
    /** Checksum field name. */
    public static final String FN_CHECKSUM = "Checksum";
    /** Location field name. */
    public static final String FN_LOCATION = "Location";
    /** Offset field name. */
    public static final String FN_OFFSET = "Offset";
    /** Filename field name. */
    public static final String FN_FILENAME = "Filename";
    /** Length field name. */
    public static final String FN_ARCHIVE_LENGTH = "Archive-length";

    /** URL record field index. */
    public static final int FN_IDX_URL = 0;
    /** Ip address record field index. */
    public static final int FN_IDX_IP_ADDRESS = 1;
    /** Archive date record field index. */
    public static final int FN_IDX_ARCHIVE_DATE = 2;
    /** Content-type record field index. */
    public static final int FN_IDX_CONTENT_TYPE = 3;
    /** Result code record field index.  */
    public static final int FN_IDX_RESULT_CODE = 4;
    /** Checksum record field index. */
    public static final int FN_IDX_CHECKSUM = 5;
    /** Location record field index. */
    public static final int FN_IDX_LOCATION = 6;
    /** Offset record field index. */
    public static final int FN_IDX_OFFSET = 7;
    /** Filename record field index. */
    public static final int FN_IDX_FILENAME = 8;

    /**
     * Version block fields.
     */

    /** Version field name. */
    public static final String FN_VERSION_NUMBER = "Version-number";
    /** Reserved field name. */
    public static final String FN_RESERVED = "Reserved";
    /** Origin field name. */
    public static final String FN_ORIGIN_CODE = "Origin-code";

    /** Version number version field index. */
    public static final int FN_IDX_VERSION_NUMBER = 0;
    /** Reserved version field index. */
    public static final int FN_IDX_RESERVED = 1;
    /** Origin code version field index. */
    public static final int FN_IDX_ORIGIN_CODE = 2;

    /** Version-1-block fields. */
    public static final String[] VERSION_1_BLOCK_FIELDS = {
            FN_URL, FN_IP_ADDRESS, FN_ARCHIVE_DATE, FN_CONTENT_TYPE,
            FN_ARCHIVE_LENGTH};

    /** Version-2-block fields. */
    public static final String[] VERSION_2_BLOCK_FIELDS = {
            FN_URL, FN_IP_ADDRESS, FN_ARCHIVE_DATE, FN_CONTENT_TYPE,
            FN_RESULT_CODE, FN_CHECKSUM, FN_LOCATION,
            FN_OFFSET, FN_FILENAME,
            FN_ARCHIVE_LENGTH };

    /** Number of fields in a version 1 block. */
    public static final int VERSION_1_BLOCK_NUMBER_FIELDS = VERSION_1_BLOCK_FIELDS.length;

    /** Number of fields in a version 2 block. */
    public static final int VERSION_2_BLOCK_NUMBER_FIELDS = VERSION_2_BLOCK_FIELDS.length;

    /** Version description fields. */
    public static final String[] VERSION_DESC_FIELDS = {
        FN_VERSION_NUMBER, FN_RESERVED, FN_ORIGIN_CODE };

    /** Version 1 description string. */
    public static final String VERSION_1_BLOCK_DEF =
                    join(' ', VERSION_1_BLOCK_FIELDS);

    /** Version 2 description string. */
    public static final String VERSION_2_BLOCK_DEF =
                    join(' ', VERSION_2_BLOCK_FIELDS);

    /**
     * Helper method for converting an array in a string using a specified
     * separator.
     * @param sep desired separator
     * @param elts array of string to join
     * @return an array joined into a string
     */
    public static String join(char sep, String... elts) {
        StringBuilder buf = new StringBuilder();
        for (String s : elts) {
            buf.append(s).append(sep);
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }

}
