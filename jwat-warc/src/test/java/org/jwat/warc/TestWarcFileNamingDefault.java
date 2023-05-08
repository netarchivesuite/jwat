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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWarcFileNamingDefault {

    @Test
    public void test_warcfilenaming_default() {
        WarcFileNamingDefault wafn;

        String hostname = null;
        try {
            // Get current date after this since this could maybe take several seconds in rare cases.
            hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
        }
        catch (UnknownHostException e) {
            Assert.fail("Unexpected exception!");
        }

        Date date = new Date();
        String dateStr;

        wafn = new WarcFileNamingDefault(null, null, null, null);
        Assert.assertEquals("JWAT", wafn.filePrefix);
        Assert.assertEquals(true, (wafn.date.getTime() - date.getTime()) >= 0);
        Assert.assertEquals(true, (wafn.date.getTime() - date.getTime()) < 1000);
        dateStr = wafn.dateFormat.format(wafn.date);
        Assert.assertEquals(dateStr, wafn.dateStr);
        Assert.assertEquals(hostname, wafn.hostname);
        Assert.assertEquals(".warc", wafn.extension);
        Assert.assertEquals(true, wafn.supportMultipleFiles());
        Assert.assertEquals(wafn.filePrefix + "-" + wafn.dateStr + "-" + String.format("%05d", 42) + "-" + wafn.hostname + wafn.extension, wafn.getFilename(42, false));
        Assert.assertEquals(wafn.filePrefix + "-" + wafn.dateStr + "-" + String.format("%05d", 43) + "-" + wafn.hostname + wafn.extension + ".gz", wafn.getFilename(43, true));

        dateStr = wafn.dateFormat.format(date);

        wafn = new WarcFileNamingDefault("Prefix", date, "hostname", ".carw");
        Assert.assertEquals("Prefix", wafn.filePrefix);
        Assert.assertEquals(date, wafn.date);
        Assert.assertEquals(dateStr, wafn.dateStr);
        Assert.assertEquals("hostname", wafn.hostname);
        Assert.assertEquals(".carw", wafn.extension);
        Assert.assertEquals(true, wafn.supportMultipleFiles());
        Assert.assertEquals("Prefix" + "-" + wafn.dateStr + "-" + String.format("%05d", 42) + "-" + "hostname" + ".carw", wafn.getFilename(42, false));
        Assert.assertEquals("Prefix" + "-" + wafn.dateStr + "-" + String.format("%05d", 43) + "-" + "hostname" + ".carw" + ".gz", wafn.getFilename(43, true));
    }

}
