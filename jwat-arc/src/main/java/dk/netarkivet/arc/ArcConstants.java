/**
 * JHOVE2 - Next-generation architecture for format-aware characterization
 *
 * Copyright (c) 2009 by The Regents of the University of California,
 * Ithaka Harbors, Inc., and The Board of Trustees of the Leland Stanford
 * Junior University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * o Neither the name of the University of California/California Digital
 *   Library, Ithaka Harbors/Portico, or Stanford University, nor the names of
 *   its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package dk.netarkivet.arc;

/**
 * Class containing all relevant ARC constants and structures.
 * Including but not limited to field names and mime-types.
 *
 * @author lbihanic, selghissassi, nicl
 */
public final class ArcConstants {

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
    public static final int AFIDX_URL = 0;
    /** Ip address record field index. */
    public static final int AFIDX_IPADDRESS = 1;
    /** Archive date record field index. */
    public static final int AFIDX_ARCHIVEDATE = 2;
    /** Content-type record field index. */
    public static final int AFIDX_CONTENETTYPE = 3;
    /** Result code record field index.  */
    public static final int AFIDX_RESULTCODE = 4;
    /** Checksum record field index. */
    public static final int AFIDX_CHECKSUM = 5;
    /** Location record field index. */
    public static final int AFIDX_LOCATION = 6;
    /** Offset record field index. */
    public static final int AFIDX_OFFSET = 7;
    /** Filename record field index. */
    public static final int AFIDX_FILENAME = 8;

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
