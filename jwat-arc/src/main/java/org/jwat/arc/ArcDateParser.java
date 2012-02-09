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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * ARC Date parser and format validator ("yyyyMMddHHmmss").
 *
 * @author lbihanic, selghissassi
 */
public final class ArcDateParser {

    /** Allowed format string. */
    private static final String ARC_DATE_FORMAT = "yyyyMMddHHmmss";

    /** Allowed <code>DateFormat</code>. */
    private final DateFormat dateFormat;

    /** Basic <code>DateFormat</code> is not thread safe. */
    private static final ThreadLocal<ArcDateParser> DateParserTL =
        new ThreadLocal<ArcDateParser>() {
        public ArcDateParser initialValue() {
            return new ArcDateParser();
        }
    };

    /**
     * Creates a new <code>DateParser</code>.
     */
    private ArcDateParser() {
        dateFormat = new SimpleDateFormat(ARC_DATE_FORMAT);
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Parses a date.
     * @param dateStr date to parse
     * @return the formatted date
     */
    private Date parseDate(String dateStr) {
        Date date = null;
        try {
            if ((dateStr != null)
                            && dateStr.length() == ARC_DATE_FORMAT.length()) {
                date = dateFormat.parse(dateStr);
            }
        } catch (Exception e) { /* Ignore */ }
        return date;
    }

    /**
     * Parses the date using the format yyyyMMddHHmmss.
     * @param dateStr the date to parse
     * @return the formatted date or <code>null</code> based on whether the date
     * to parse is compliant with the format yyyyMMddHHmmss or not
     */
    public static Date getDate(String dateStr) {
        Date date = DateParserTL.get().parseDate(dateStr);
        boolean isValid = (date == null) ? false
                                         : (date.getTime() > 0);
        return isValid ? date : null;
    }

}
