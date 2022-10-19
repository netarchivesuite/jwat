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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import org.jwat.common.Numbers;

/**
 * Separate class to handle WARC dates now that they can have varying precision levels.
 * Also useful now that the <code>Date</code> class has been deprecated.
 * This class maintains the UTC date time internally along with other fields.
 * The old implementation just used the <code>Date</code> object directly leaving the timezone mess to the user.
 *
 * Exampples:
 *
 * WARC-Date: 2016-01-11T23:24:25.412030Z
 * WARC-Date: 2016-01-11T23:24:25Z
 * WARC-Date: 2016-01
 * Year:
 * YYYY (eg 1997)
 * Year and month:
 * YYYY-MM (eg 1997-07)
 * Complete date:
 * YYYY-MM-DD (eg 1997-07-16)
 * Complete date plus hours and minutes:
 * YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
 * Complete date plus hours, minutes and seconds:
 * YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
 * Complete date plus hours, minutes, seconds and a decimal fraction of a second
 * YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
 *
 * @author nicl
 */
public class WarcDate {

    /** UTC <code>TimeZone</code> object for reuse. */
    protected static TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    /** internal array used to convert digit to char. */
    protected static int[] asciiInt;

    /*
     * Initialize the digit to char array.
     */
    static {
        asciiInt = new int[256];
        for (int i=0; i<256; ++i) {
            asciiInt[i] = -1;
        }
        asciiInt['0'] = 0;
        asciiInt['1'] = 1;
        asciiInt['2'] = 2;
        asciiInt['3'] = 3;
        asciiInt['4'] = 4;
        asciiInt['5'] = 5;
        asciiInt['6'] = 6;
        asciiInt['7'] = 7;
        asciiInt['8'] = 8;
        asciiInt['9'] = 9;
    }

    /** Year level precision. */
    public static final int P_YEAR = 0;
    /** Month level precision. */
    public static final int P_MONTH = 1;
    /** Day of month level precision. */
    public static final int P_DAYOFMONTH = 2;
    /** Minute level precision. */
    public static final int P_MINUTE = 3;
    /** Second level precision. */
    public static final int P_SECOND = 4;
    /** Nano/fraction level precision. */
    public static final int P_FRACTION = 5;

    /** Object used to convert dates, handle timezones and retrieve fields. */
    public LocalDateTime ldt;

    /** Precision level of the warc date (year to fraction). */
    public int precision;

    /** Year. */
    public int year;
    /** Month (1-12). */
    public int month;
    /** Day of month (1-31). */
    public int dayOfMonth;
    /** Hour (0-23). */
    public int hour;
    /** Minute (0-59). */
    public int minute;
    /** Second (0-59). */
    public int second;
    /** Nano of second (0-999.999.999).*/
    public int nanoOfSecond;
    /** Bigger fraction in case someone needs more than nano precision. */
    public long fraction;
    /** Length of the fraction string representation. */
    public int fractionLen;

    /**
     * Construct <code>WarcDate</code> with year level precision.
     * @param year year
     */
    public WarcDate(int year) {
        precision = P_YEAR;
        this.ldt = LocalDateTime.of(year, 1, 1, 0, 0);
        this.year = year;
    }

    /**
     * Construct <code>WarcDate</code> with month level precision.
     * @param year year
     * @param month integer between 1 and 12
     */
    public WarcDate(int year, int month) {
        precision = P_MONTH;
        this.ldt = LocalDateTime.of(year, month, 1, 0, 0);
        this.year = year;
        this.month = month;
    }

    /**
     * Create <code>WarcDate</code> with day of month level precision.
     * @param year year
     * @param month integer between 1 and 12
     * @param dayOfMonth integer between 1 and 31
     */
    public WarcDate(int year, int month, int dayOfMonth) {
        precision = P_DAYOFMONTH;
        this.ldt = LocalDateTime.of(year, month, dayOfMonth, 0, 0);
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    /**
     * Create <code>WarcDate</code> with minute level precision.
     * @param year year
     * @param month integer between 1 and 12
     * @param dayOfMonth integer between 1 and 31
     * @param hour integer between 0 and 59
     * @param minute integer between 0 and 59
     */
    public WarcDate(int year, int month, int dayOfMonth, int hour, int minute) {
        precision = P_MINUTE;
        this.ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.hour = hour;
        this.minute = minute;
    }

    /**
     * Create <code>WarcDate</code> with second level precision.
     * @param year year
     * @param month integer between 1 and 12
     * @param dayOfMonth integer between 1 and 31
     * @param hour integer between 0 and 59
     * @param minute integer between 0 and 59
     * @param second integer between 0 and 59
     */
    public WarcDate(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        precision = P_SECOND;
        this.ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * Create <code>WarcDate</code> with nano if second level precision.
     * Nano of second is used with the internal <code>LocalDateTime</code> object.
     * @param year year
     * @param month integer between 1 and 12
     * @param dayOfMonth integer between 1 and 31
     * @param hour integer between 0 and 59
     * @param minute integer between 0 and 59
     * @param second integer between 0 and 59
     * @param nanoOfSecond integer between 0 and 999.999.999.
     */
    public WarcDate(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        precision = P_FRACTION;
        this.ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nanoOfSecond = nanoOfSecond;
        this.fraction = nanoOfSecond;
        if (nanoOfSecond == 0) {
            this.fractionLen = 1;
        }
        else {
            this.fractionLen = 9;
            int trailingZeros = Numbers.intTrailingZeros(nanoOfSecond);
            while (trailingZeros > 0) {
                fraction = fraction / 10;
                --fractionLen;
                --trailingZeros;
            }
        }
    }

    /**
     * Create <code>WarcDate</code> with nano if second level precision.
     * Nano of second is used with the internal <code>LocalDateTime</code> object.
     * Even though nano of second is currently the limit in Java this implementation can
     * parse, store and ouput the fraction part as an long value.
     * @param year year
     * @param month integer between 1 and 12
     * @param dayOfMonth integer between 1 and 31
     * @param hour integer between 0 and 59
     * @param minute integer between 0 and 59
     * @param second integer between 0 and 59
     * @param nanoOfSecond integer between 0 and 999999999.
     * @param fraction long fraction between 0 and 9223372036854775807.
     * @param fractionLen franction length to output when converting to a string representation
     */
    public WarcDate(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond, long fraction, int fractionLen) {
        precision = P_FRACTION;
        this.ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nanoOfSecond = nanoOfSecond;
        this.fraction = fraction;
        this.fractionLen = fractionLen;
    }

    /**
     * Create <code>WarcDate</code> with nano if second level precision.
     * Nano of second is used with the internal <code>LocalDateTime</code> object.
     * Do not reuse the <code>LocalDateTime</code> instance!
     * Nano of second is used with the internal <code>LocalDateTime</code> object.
     * @param ldt keep this object internally and use it values
     */
    public WarcDate(LocalDateTime ldt) {
        precision = P_FRACTION;
        this.ldt = ldt;
        this.year = ldt.getYear();
        this.month = ldt.getMonthValue();
        this.dayOfMonth = ldt.getDayOfMonth();
        this.hour = ldt.getHour();
        this.minute = ldt.getMinute();
        this.second = ldt.getSecond();
        this.nanoOfSecond = ldt.getNano();
        this.fraction = nanoOfSecond;
        this.fractionLen = 9;
    }

    /**
     * Returns a <code>WarcDate</code> representing the current UTC date time.
     * @return a <code>WarcDate</code> representing the current UTC date time
     */
    public static WarcDate now() {
        return new WarcDate(LocalDateTime.now(UTC_TIMEZONE.toZoneId()));
    }

    /**
     * Returns a <code>WarcDate</code> object from on a <code>Date</code> in the default system timezone.
     * @param date date in the system default timezone (technically deprecated)
     * @return <code>WarcDate</code> object
     */
    public static WarcDate fromLocalDate(Date date) {
        // Date adjusted from system default timezone to UTC timezone.
        return new WarcDate(LocalDateTime.ofInstant(date.toInstant(), UTC_TIMEZONE.toZoneId()));
    }

    /**
     * Returns a <code>Date</code> representing the internal date time adjusted to the system default timezone.
     * @return a <code>Date</code> representing the internal date time adjusted to the system default timezone
     */
    public Date getDateLocal() {
        // Date adjusted from UTZ timezone to system default timezone.
        // Meaning it is the local date time.
        return Date.from((ldt.atZone(UTC_TIMEZONE.toZoneId()).toInstant()));
    }

    /**
     * Returns a <code>WarcDate</code> object from on a <code>Date</code> in the default system timezone.
     * @param date date in the system default timezone (technically deprecated)
     * @return <code>WarcDate</code> object
     */
    public static WarcDate fromUTCDate(Date date) {
        // Date adjusted from system default timezone to system default timezone.
        // Meaning it is left as is, UTC timezone date expected.
        return new WarcDate(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * Returns a <code>Date</code> representing the internal date time (UTC timezone).
     * @return a <code>Date</code> representing the internal date time (UTC timezone)
     */
    public Date getDateUTC() {
        // Date adjusted from system default timezone to system default timezone.
        // Meaning it is left as is, which should be UTC.
        return Date.from((ldt.atZone(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * Attempt to parse a WARC date string in a subset of the W3CDTF format.
     * @param datestring WARC date string
     * @return <code>WarcDate</code> object
     */
    public static WarcDate getWarcDate(String datestring) {
        byte[] bytes = datestring.getBytes();
        return getWarcDate(bytes, 0, bytes.length);
    }

    /**
     * Attempt to parse a WARC date string from a byte array in a subset of the W3CDTF format.
     * @param bytes WARC date string as a byte array
     * @param pos start position in the byte array
     * @param limit limit in the byte array
     * @return <code>WarcDate</code> object
     */
    public static WarcDate getWarcDate(byte[] bytes, int pos, int limit) {
        //TimeZone timeZone = TimeZone.getTimeZone("UTC");
        //Calendar calendar = Calendar.getInstance(timeZone);
        int idx = pos;
        int len = limit - pos;
        int c;
        int chr;
        int chr2;
        int year;
        int month;
        int dayOfMonth;
        int hour;
        int minute;
        int second;
        int ie;
        int ie2;
        int nanoOfSecond;
        int nanoStrLen;
        long fraction;
        int fractionStrLen;
        long le;
        if (len >= 4) {
            // Year
            year = asciiInt[bytes[idx++] & 255];
            ie = year;
            c = asciiInt[bytes[idx++] & 255];
            ie |= c;
            year = (year << 3) + (year << 1) + c;
            c = asciiInt[bytes[idx++] & 255];
            ie |= c;
            year = (year << 3) + (year << 1) + c;
            c = asciiInt[bytes[idx++] & 255];
            ie |= c;
            year = (year << 3) + (year << 1) + c;
            if (ie < 0) {
                return null;
            }
            if (len == 4) {
                return new WarcDate(year);
            }
            if (len >= 7) {
                // Month
                chr = bytes[idx++] & 255;
                month = asciiInt[bytes[idx++] & 255];
                ie = month;
                c = asciiInt[bytes[idx++] & 255];
                ie |= c;
                month = (month << 3) + (month << 1) + c;
                if (ie < 0 || '-' != chr) {
                    return null;
                }
                if (len == 7) {
                    return new WarcDate(year, month);
                }
                if (len >= 10) {
                    // Date
                    chr = bytes[idx++] & 255;
                    dayOfMonth = asciiInt[bytes[idx++] & 255];
                    ie = dayOfMonth;
                    c = asciiInt[bytes[idx++] & 255];
                    ie |= c;
                    dayOfMonth = (dayOfMonth << 3) + (dayOfMonth << 1) + c;
                    if (ie < 0 || '-' != chr) {
                        return null;
                    }
                    if (len == 10) {
                        return new WarcDate(year, month, dayOfMonth);
                    }
                    if (len >= 16) {
                        // 'T' hour and minute.
                        chr = bytes[idx++] & 255;
                        hour = asciiInt[bytes[idx++] & 255];
                        ie = hour;
                        c = asciiInt[bytes[idx++] & 255];
                        ie |= c;
                        hour = (hour << 3) + (hour << 1) + c;
                        chr2 = bytes[idx++] & 255;
                        minute = asciiInt[bytes[idx++] & 255];
                        ie2 = minute;
                        c = asciiInt[bytes[idx++] & 255];
                        ie2 |= c;
                        minute = (minute << 3) + (minute << 1) + c;
                        if (ie < 0 || ie2 < 0 || ('T' != chr && 't' != chr) || ':' != chr2) {
                            return null;
                        }
                        if (len == 17) {
                            chr = (bytes[idx++] & 255);
                            if ('Z' != chr && 'z' != chr) {
                                return null;
                            }
                            else {
                                return new WarcDate(year, month, dayOfMonth, hour, minute);
                            }
                        }
                        if (len >= 19) {
                            // Second.
                            chr = bytes[idx++] & 255;
                            second = asciiInt[bytes[idx++] & 255];
                            ie = second;
                            c = asciiInt[bytes[idx++] & 255];
                            ie |= c;
                            second = (second << 3) + (second << 1) + c;
                            if (ie < 0 || ':' != chr) {
                                return null;
                            }
                            if (len == 20) {
                                chr = (bytes[idx++] & 255);
                                if ('Z' != chr && 'z' != chr) {
                                    return null;
                                }
                                else {
                                    return new WarcDate(year, month, dayOfMonth, hour, minute, second);
                                }
                            }
                            if (len >= 22) {
                                // '.' Fraction.
                                chr = bytes[idx++] & 255;
                                fraction = 0;
                                fractionStrLen = 0;
                                le = 0;
                                while (idx < len - 1 && fractionStrLen < 9) {
                                    c = asciiInt[bytes[idx++] & 255];
                                    le |= c;
                                    fraction = (fraction << 3) + (fraction << 1) + c;
                                    ++fractionStrLen;
                                }
                                nanoOfSecond = (int)fraction;
                                nanoStrLen = fractionStrLen;
                                while (idx < len - 1) {
                                    c = asciiInt[bytes[idx++] & 255];
                                    le |= c;
                                    fraction = (fraction << 3) + (fraction << 1) + c;
                                    ++fractionStrLen;
                                }
                                while (nanoStrLen < 9) {
                                    nanoOfSecond = (nanoOfSecond << 3) + (nanoOfSecond << 1);
                                    ++nanoStrLen;
                                }
                                chr = (bytes[idx++] & 255);
                                if (le < 0 || ('Z' != chr && 'z' != chr)) {
                                    return null;
                                }
                                else {
                                    return new WarcDate(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, fraction, fractionStrLen);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Adjust the precision and initialize the appropriate fields.
     * @param newPrecision the new precision
     */
    public void adjustPrecisionTo(int newPrecision) {
        if (newPrecision > precision) {
            switch (precision) {
            case P_FRACTION:
            case P_SECOND:
            case P_MINUTE:
            case P_DAYOFMONTH:
                if (dayOfMonth == 0) {
                    dayOfMonth = 1;
                }
            case P_MONTH:
                if (month == 0) {
                    month = 1;
                }
            case P_YEAR:
            default:
                break;
            }
        }
        precision = newPrecision;
    }

    @Override
    public String toString() {
        char[] tmpStr = null;
        int fractionIdx;
        int tmpFractionLen;
        long tmpFraction;
        switch (precision) {
        case P_YEAR:
            tmpStr = new char[4];
            break;
        case P_MONTH:
            tmpStr = new char[7];
            break;
        case P_DAYOFMONTH:
            tmpStr = new char[10];
            break;
        case P_MINUTE:
            tmpStr = new char[17];
            break;
        case P_SECOND:
            tmpStr = new char[20];
            break;
        case P_FRACTION:
            tmpStr = new char[21 + fractionLen];
            break;
        default:
            throw new IllegalStateException("Invalid precision level.");
        }
        int idx = 0;
        if (precision >= P_YEAR) {
            tmpStr[idx++] = (char)('0' + year / 1000 % 10);
            tmpStr[idx++] = (char)('0' + year / 100 % 10);
            tmpStr[idx++] = (char)('0' + year / 10 % 10);
            tmpStr[idx++] = (char)('0' + year % 10);
            if (precision >= P_MONTH) {
                tmpStr[idx++] = '-';
                tmpStr[idx++] = (char)('0' + month / 10 % 10);
                tmpStr[idx++] = (char)('0' + month % 10);
                if (precision >= P_DAYOFMONTH) {
                    tmpStr[idx++] = '-';
                    tmpStr[idx++] = (char)('0' + dayOfMonth / 10 % 10);
                    tmpStr[idx++] = (char)('0' + dayOfMonth % 10);
                    if (precision >= P_MINUTE) {
                        tmpStr[idx++] = 'T';
                        tmpStr[idx++] = (char)('0' + hour / 10 % 10);
                        tmpStr[idx++] = (char)('0' + hour % 10);
                        tmpStr[idx++] = ':';
                        tmpStr[idx++] = (char)('0' + minute / 10 % 10);
                        tmpStr[idx++] = (char)('0' + minute % 10);
                        if (precision >= P_SECOND) {
                            tmpStr[idx++] = ':';
                            tmpStr[idx++] = (char)('0' + second / 10 % 10);
                            tmpStr[idx++] = (char)('0' + second % 10);
                            if (precision >= P_FRACTION) {
                                tmpStr[idx++] = '.';
                                tmpFraction = fraction;
                                tmpFractionLen = fractionLen;
                                idx += fractionLen;
                                fractionIdx = idx;
                                tmpStr[--fractionIdx] = (char)('0' + tmpFraction % 10);
                                --tmpFractionLen;
                                while (tmpFractionLen > 0) {
                                    tmpFraction = tmpFraction / 10;
                                    tmpStr[--fractionIdx] = (char)('0' + tmpFraction % 10);
                                    --tmpFractionLen;
                                }
                            }
                        }
                        tmpStr[idx++] = 'Z';
                    }
                }
            }
        }
        return new String(tmpStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfMonth, fraction, fractionLen, hour, ldt, minute, month, nanoOfSecond, precision, second, year);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WarcDate other = (WarcDate) obj;
        return dayOfMonth == other.dayOfMonth && fraction == other.fraction && fractionLen == other.fractionLen
                && hour == other.hour && Objects.equals(ldt, other.ldt) && minute == other.minute
                && month == other.month && nanoOfSecond == other.nanoOfSecond && precision == other.precision
                && second == other.second && year == other.year;
    }

}
