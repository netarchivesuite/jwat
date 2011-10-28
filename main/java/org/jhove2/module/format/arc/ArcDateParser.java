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
package org.jhove2.module.format.arc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date parser.
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
