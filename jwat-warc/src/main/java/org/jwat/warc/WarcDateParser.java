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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * WARC-Date parser and format validator. The format "yyyy-MM-dd'T'HH:mm:ss'Z'"
 * is specified in the WARC ISO standard.
 *
 * @author lbihanic, selghissassi, nicl
 */
public final class WarcDateParser {

    /** WARC <code>DateFormat</code> as specified in the WARC ISO standard. */
    private final DateFormat dateFormat;

    /** Basic <code>DateFormat</code> is not thread safe. */
    private static final ThreadLocal<WarcDateParser> DateParserTL =
        new ThreadLocal<WarcDateParser>() {
        @Override
        public WarcDateParser initialValue() {
            return new WarcDateParser();
        }
    };

    /**
     * Creates a new <code>DateParser</code>.
     */
    private WarcDateParser() {
        dateFormat = new SimpleDateFormat(WarcConstants.WARC_DATE_FORMAT);
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Parses a date.
     * @param dateStr date to parse
     * @return the formatted date or null if unable to parse date
     */
    private Date parseDate(String dateStr) {
        Date date = null;
        try {
            // We subtract 4 from the format because of the ' characters.
            // These characters are to specify constants in the format string.
            if ((dateStr != null) && dateStr.length()
                            == WarcConstants.WARC_DATE_FORMAT.length() - 4) {
                // Support upper/lower-case.
                date = dateFormat.parse(dateStr.toUpperCase());
            }
        } catch (Exception e) { /* Ignore */ }
        return date;
    }

    /**
     * Parses the date using the format "yyyy-MM-ddTHH:mm:ssZ".
     * @param dateStr the date to parse
     * @return the formatted date or <code>null</code> based on whether the date
     * to parse is compliant with the format "yyyy-MM-ddTHH:mm:ssZ" or not
     */
    public static Date getDate(String dateStr) {
        Date date = DateParserTL.get().parseDate(dateStr);
        boolean isValid = (date == null) ? false
                                         : (date.getTime() > 0);
        return isValid ? date : null;
    }

    /**
     * Return a <code>DateFormat</code> object which can be used to string
     * format WARC dates.
     * @return <code>DateFormat</code> object which can be used to string
     * format WARC dates.
     */
    public static DateFormat getDateFormat() {
        return DateParserTL.get().dateFormat;
    }

}

