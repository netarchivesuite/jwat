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

    /** An ARC version block starts with this string. */
    public static final String ARC_MAGIC_HEADER = "filedesc:";

    /** Arc file magic number. */
    public static final String ARC_SCHEME = "filedesc://";

    /** Text plain content type. */
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    /** Special content-type for none. */
    public static final String CONTENT_TYPE_NO_TYPE = "no-type";

    /** Version block preferred content-type. */
    public static final String VERSION_BLOCK_CONTENT_TYPE = "text";

    /** Version block preferred media-type. */
    public static final String VERSION_BLOCK_MEDIA_TYPE = "plain";

    /** Allowed format string. */
    public static final String ARC_DATE_FORMAT = "yyyyMMddHHmmss";

    /** Content-type format string as specified in RFC2616.*/
    public static final String CONTENT_TYPE_FORMAT = "<type>/<sub-type>(; <argument>=<value>)*";

    /**
     * Record block fields.
     */

    /** Url fieldname. */
    public static final String URL_FIELD                = "URL";
    /** Ip-Address fieldname. */
    public static final String IP_ADDRESS_FIELD         = "IP-address";
    /** Date fieldname. */
    public static final String DATE_FIELD               = "Archive-date";
    /** Content-Type fieldname. */
    public static final String CONTENT_TYPE_FIELD       = "Content-type";
    /** Result-Code fieldname. */
    public static final String RESULT_CODE_FIELD        = "Result-code";
    /** Checksum fieldname. */
    public static final String CHECKSUM_FIELD           = "Checksum";
    /** Location fieldname. */
    public static final String LOCATION_FIELD           = "Location";
    /** Offset fieldname. */
    public static final String OFFSET_FIELD             = "Offset";
    /** Filename fieldname. */
    public static final String FILENAME_FIELD           = "Filename";
    /** Length fieldname. */
    public static final String LENGTH_FIELD             = "Archive-length";

    /** URL record field index. */
    public static final int AF_IDX_URL = 0;
    /** Ip address record field index. */
    public static final int AF_IDX_IPADDRESS = 1;
    /** Archive date record field index. */
    public static final int AF_IDX_ARCHIVEDATE = 2;
    /** Content-type record field index. */
    public static final int AF_IDX_CONTENTTYPE = 3;
    /** Result code record field index.  */
    public static final int AF_IDX_RESULTCODE = 4;
    /** Checksum record field index. */
    public static final int AF_IDX_CHECKSUM = 5;
    /** Location record field index. */
    public static final int AF_IDX_LOCATION = 6;
    /** Offset record field index. */
    public static final int AF_IDX_OFFSET = 7;
    /** Filename record field index. */
    public static final int AF_IDX_FILENAME = 8;

    /**
     * Version block fields.
     */

    /** Version fieldname. */
    public static final String VERSION_FIELD            = "Version-number";
    /** Reserved fieldname. */
    public static final String RESERVED_FIELD           = "Reserved";
    /** Origin fieldname. */
    public static final String ORIGIN_FIELD             = "Origin-code";

    /** Version-1-block fields. */
    public static final String[] VERSION_1_BLOCK_FIELDS = {
            URL_FIELD, IP_ADDRESS_FIELD, DATE_FIELD, CONTENT_TYPE_FIELD,
            LENGTH_FIELD};
    /** Version-2-block fields. */
    public static final String[] VERSION_2_BLOCK_FIELDS = {
            URL_FIELD, IP_ADDRESS_FIELD, DATE_FIELD, CONTENT_TYPE_FIELD,
            RESULT_CODE_FIELD, CHECKSUM_FIELD, LOCATION_FIELD,
            OFFSET_FIELD, FILENAME_FIELD,
            LENGTH_FIELD };

    /** Version description fields. */
    public static final String[] VERSION_DESC_FIELDS = {
        VERSION_FIELD, RESERVED_FIELD, ORIGIN_FIELD };

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
    private static String join(char sep, String... elts) {
        StringBuilder buf = new StringBuilder();
        for (String s : elts) {
            buf.append(s).append(sep);
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }

    /**
     * No constructor for this utility class, static access only.
     */
    private ArcConstants() {
    }

}
