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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

//@RunWith(JUnit4.class)
public class TestWarcDate {

    //@Test
    public void test_warcdate_w3cdtf() {
        Object[][] cases;
        WarcDate wd;

        cases = new Object[][] {
            {"4"},
            {"a"},
            {"43"},
            {"a3"},
            {"ab"},
            {"4b"},
            {"432"},
            {"a32"},
            {"4b2"},
            {"43c"},
            {"abc"},
            {"a321"},
            {"4b21"},
            {"43c1"},
            {"432d"},
            {"a33d"},
            {"4bc1"},
            {"abcd"},
            {"1972-1"},
            {"1972-a2"},
            {"1972-1b"},
            {"1972:12"},
            {"1984-34-3"},
            {"1984-34-a4"},
            {"1984-34-3b"},
            {"1984-34:34"},
            {"1992-56-78T"},
            {"1992-56-78T1"},
            {"1992-56-78T12"},
            {"1992-56-78T12:"},
            {"1992-56-78T12:3"},
            {"1992-56-78T12:34"},
            {"1992-56-78T12:34S"},
            {"1992-56-78S12:34Z"},
            {"1992-56-78Ta2:34Z"},
            {"1992-56-78T1b:34Z"},
            {"1992-56-78T12:c4Z"},
            {"1992-56-78T12:3dZ"},
            {"1992-56-78Ta2:3dZ"},
            {"1992-56-78T1b:c4Z"},
            {"1992-56-78Tab:cdZ"},
            {"1992-56-78T12-34Z"},
            {"1992-56-78T12:34:"},
            {"1992-56-78T12:34:5"},
            {"1992-56-78T12:34:56"},
            {"1992-56-78T12:34:5b"},
            {"1992-56-78T12:34:a6"},
            {"1992-56-78T12:34:ab"},
            {"1992-56-78T12:34:5bZ"},
            {"1992-56-78T12:34:a6Z"},
            {"1992-56-78T12:34:abZ"},
            {"1992-56-78T12:34-56Z"},
            {"1992-56-78T12:34:56S"},
            {"1992-56-78T12:34:56.Z"},
            {"1991-07-12T14:41:00.aZ"},
            {"1991-07-12T14:41:00.1bZ"},
            {"1991-07-12T14:41:00.12cZ"},
            {"1991-07-12T14:41:00.123dZ"},
            {"1991-07-12T14:41:00.12cdZ"},
            {"1991-07-12T14:41:00.1bcdZ"},
            {"1991-07-12T14:41:00.abcdZ"},
            {"1991-07-12T14:41:00.abc4Z"},
            {"1991-07-12T14:41:00.abc4Z"},
            {"1991-07-12T14:41:00.a234Z"},
            {"1991-07-12T14:41:00.1234567890S"},
        };
        for (int i=0; i<cases.length; ++i) {
            String dstr = (String)cases[i][0];
            wd = WarcDate.getWarcDate(dstr);
            Assert.assertEquals(null, wd);
            //System.out.println(wd);
        }

        cases = new Object[][] {
            {
                "2016-01-11T23:24:25.412030Z", 8, WarcDate.P_FRACTION,
                2016, 1, 11, 23, 24, 25, 412030000, 412030L, 6
            },
            {
                "2016-01-11T23:24:25Z", 6, WarcDate.P_SECOND,
                2016, 1, 11, 23, 24, 25, 0, 0L, 0
               },
            {
                "2016-01", 2, WarcDate.P_MONTH,
                2016, 1, 0, 0, 0, 0, 0, 0L, 0
            },
            {
                "1997", 1, WarcDate.P_YEAR,
                1997, 0, 0, 0, 0, 0, 0, 0L, 0
            },
            {
                "1997-07", 2, WarcDate.P_MONTH,
                1997, 7, 0, 0, 0, 0, 0, 0L, 0
            },
            {
                "1997-07-16", 3, WarcDate.P_DAYOFMONTH,
                1997, 7, 16, 0, 0, 0, 0, 0L, 0
            },
            {
                "1997-07-16T19:20Z", 5, WarcDate.P_MINUTE,
                1997, 7, 16, 19, 20, 0, 0, 0L, 0
            },
            {
                "1997-07-16t19:20z", 5, WarcDate.P_MINUTE,
                1997, 7, 16, 19, 20, 0, 0, 0L, 0
            },
            {
                "1997-07-16T19:20:30Z", 6, WarcDate.P_SECOND,
                1997, 7, 16, 19, 20, 30, 0, 0L, 0
            },
            {
                "1997-07-16t19:20:30z", 6, WarcDate.P_SECOND,
                1997, 7, 16, 19, 20, 30, 0, 0L, 0
            },
            {
                "1997-07-16T19:20:30.45Z", 7, WarcDate.P_FRACTION,
                1997, 7, 16, 19, 20, 30, 450000000, 45L, 2
            },
            {
                "1997-07-16t19:20:30.45z", 7, WarcDate.P_FRACTION,
                1997, 7, 16, 19, 20, 30, 450000000, 45L, 2
            },
            {
                "2010-01-30T14:40:42.0Z", 7, WarcDate.P_FRACTION,
                2010, 1, 30, 14, 40, 42, 0, 0L, 1
            },
            {
                "2022-08-07T00:57:35.2147483647Z", 8, WarcDate.P_FRACTION,
                2022, 8, 7, 0, 57, 35, 214748364, 2147483647L, 10
            },
            {
                "1999-12-01T23:59:00.9223372036854775807Z", 8, WarcDate.P_FRACTION,
                1999, 12, 1, 23, 59, 00, 922337203, 9223372036854775807L, 19
            },
            {
                "1592-07-12T14:40:00.999999999Z", 7, WarcDate.P_FRACTION,
                1592, 7, 12, 14, 40, 0, 999999999, 999999999L, 9
            },
            {
                "1592-07-12T14:40:00.000000009Z", 7, WarcDate.P_FRACTION,
                1592, 7, 12, 14, 40, 0, 9, 9L, 9
            },
            {
                "1592-07-12T14:40:00.900000000Z", 8, WarcDate.P_FRACTION,
                1592, 7, 12, 14, 40, 0, 900000000, 900000000L, 9
            }
        };

        String dateStr;
        WarcDate wd2;
        String dateStr2;
        for (int i=0; i<cases.length; ++i) {
            String dstr = (String)cases[i][0];
            dstr = dstr.toUpperCase();
            // debug
            //System.out.println(dstr);
            wd = WarcDate.getWarcDate(dstr);
            assertWarcDate(cases[i], wd);
            dateStr = wd.toString();
            // Debug
            //System.out.println(dateStr);
            wd2 = WarcDate.getWarcDate(dateStr);
            assertWarcDate(cases[i], wd2);
            dateStr2 = wd2.toString();
            // Debug
            //System.out.println(dateStr2);
            Assert.assertEquals(dstr, dateStr);
            Assert.assertEquals(dstr, dateStr2);
            Assert.assertEquals(wd, wd2);
        }

        Date d1;
        Date d2;
        WarcDate wd1;

        wd = WarcDate.now();
        //System.out.println(wd);
        //System.out.println("-");
        d1 = wd.getDateLocal();
        wd1 = WarcDate.fromLocalDate(d1);
        //System.out.println(d1);
        //System.out.println(wd1);
        //System.out.println("-");
        d2 = wd.getDateUTC();
        wd2 = WarcDate.fromUTCDate(d2);
        //System.out.println(d2);
        //System.out.println(wd2);
        Assert.assertEquals(wd, wd1);
        Assert.assertEquals(wd, wd2);
    }

    public void assertWarcDate(Object[] expected, WarcDate wd) {
        String dstr = (String)expected[0];
        dstr = dstr.toUpperCase();
        int constPrecision = (int)expected[1];
        int idx = 2;
        int precision = (int)expected[idx++];
        int year = (int)expected[idx++];
        int month = (int)expected[idx++];
        int dayOfMonth = (int)expected[idx++];
        int hour = (int)expected[idx++];
        int minute = (int)expected[idx++];
        int second = (int)expected[idx++];
        int nano = (int)expected[idx++];
        long fraction = (long)expected[idx++];
        int fractionLen = (int)expected[idx++];
        Assert.assertEquals(precision, wd.precision);
        Assert.assertEquals(year, wd.year);
        Assert.assertEquals(month, wd.month);
        Assert.assertEquals(dayOfMonth, wd.dayOfMonth);
        Assert.assertEquals(hour, wd.hour);
        Assert.assertEquals(minute, wd.minute);
        Assert.assertEquals(second, wd.second);
        Assert.assertEquals(nano, wd.nanoOfSecond);
        Assert.assertEquals(fraction, wd.fraction);
        Assert.assertEquals(fractionLen, wd.fractionLen);
        WarcDate wd2 = null;
        switch (constPrecision) {
        case 1:
            wd2 = new WarcDate(year);
            break;
        case 2:
            wd2 = new WarcDate(year, month);
            break;
        case 3:
            wd2 = new WarcDate(year, month, dayOfMonth);
            break;
        case 5:
            wd2 = new WarcDate(year, month, dayOfMonth, hour, minute);
            break;
        case 6:
            wd2 = new WarcDate(year, month, dayOfMonth, hour, minute, second);
            break;
        case 7:
            wd2 = new WarcDate(year, month, dayOfMonth, hour, minute, second, nano);
            break;
        case 8:
            wd2 = new WarcDate(year, month, dayOfMonth, hour, minute, second, nano, fraction, fractionLen);
            break;
        }
        // Debug
        //System.out.println(wd2.toString());
        Assert.assertEquals(precision, wd2.precision);
        Assert.assertEquals(year, wd2.year);
        Assert.assertEquals(month, wd2.month);
        Assert.assertEquals(dayOfMonth, wd2.dayOfMonth);
        Assert.assertEquals(hour, wd2.hour);
        Assert.assertEquals(minute, wd2.minute);
        Assert.assertEquals(second, wd2.second);
        Assert.assertEquals(nano, wd2.nanoOfSecond);
        Assert.assertEquals(fraction, wd2.fraction);
        Assert.assertEquals(fractionLen, wd2.fractionLen);
        Assert.assertEquals(dstr, wd2.toString());
        Assert.assertEquals(wd, wd2);
    }

}
