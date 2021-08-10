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

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(JUnit4.class)
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ArcFileNamingDefault.class, InetAddress.class})
public class TestArcFileNamingDefault {

    @Test
    public void test_arcfilenaming_default() {
        ArcFileNamingDefault afn;

        Date date = new Date();
        String dateStr;
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
        }
        catch (UnknownHostException e) {
            Assert.fail("Unexpected exception!");
        }

        afn = new ArcFileNamingDefault(null, null, null, null);
        Assert.assertEquals("JWAT", afn.filePrefix);
        Assert.assertEquals(true, (afn.date.getTime() - date.getTime()) >= 0);
        Assert.assertEquals(true, (afn.date.getTime() - date.getTime()) < 1000);
        dateStr = afn.dateFormat.format(afn.date);
        Assert.assertEquals(dateStr, afn.dateStr);
        Assert.assertEquals(hostname, afn.hostname);
        Assert.assertEquals(".arc", afn.extension);
        Assert.assertEquals(true, afn.supportMultipleFiles());
        Assert.assertEquals(afn.filePrefix + "-" + afn.dateStr + "-" + String.format("%05d", 42) + "-" + afn.hostname + afn.extension, afn.getFilename(42, false));
        Assert.assertEquals(afn.filePrefix + "-" + afn.dateStr + "-" + String.format("%05d", 43) + "-" + afn.hostname + afn.extension + ".gz", afn.getFilename(43, true));

        dateStr = afn.dateFormat.format(date);

        afn = new ArcFileNamingDefault("Prefix", date, "hostname", ".car");
        Assert.assertEquals("Prefix", afn.filePrefix);
        Assert.assertEquals(date, afn.date);
        Assert.assertEquals(dateStr, afn.dateStr);
        Assert.assertEquals("hostname", afn.hostname);
        Assert.assertEquals(".car", afn.extension);
        Assert.assertEquals(true, afn.supportMultipleFiles());
        Assert.assertEquals("Prefix" + "-" + afn.dateStr + "-" + String.format("%05d", 42) + "-" + "hostname" + ".car", afn.getFilename(42, false));
        Assert.assertEquals("Prefix" + "-" + afn.dateStr + "-" + String.format("%05d", 43) + "-" + "hostname" + ".car" + ".gz", afn.getFilename(43, true));
    }
// TODO cleanup or use
/*
    @Test
    public void test_arcfilenaming_default_unknownhostexception() {
        mockStatic(InetAddress.class);
        //InetAddress inetAddress = mock(InetAddress.class);
        try {
            //when(InetAddress.getLocalHost()).thenReturn(inetAddress);
            when(InetAddress.getLocalHost()).thenThrow(new UnknownHostException());
        }
        catch (UnknownHostException e) {
            Assert.fail("Mocking failure!");
        }
        //when(inetAddress.getHostAddress()).thenThrow(new UnknownHostException());
        //when(inetAddress.getHostAddress()).thenReturn("1.1.1.1");

        ArcFileNamingDefault afn;
        Date date = new Date();
        String dateStr;

        afn = new ArcFileNamingDefault(null, null, null, null);
        Assert.assertEquals("JWAT", afn.filePrefix);
        Assert.assertEquals(true, (afn.date.getTime() - date.getTime()) >= 0);
        Assert.assertEquals(true, (afn.date.getTime() - date.getTime()) < 1000);
        dateStr = afn.dateFormat.format(afn.date);
        Assert.assertEquals(dateStr, afn.dateStr);
        Assert.assertEquals("unknown", afn.hostname);
        Assert.assertEquals(".arc", afn.extension);
        Assert.assertEquals(true, afn.supportMultipleFiles());
        Assert.assertEquals(afn.filePrefix + "-" + afn.dateStr + "-" + String.format("%05d", 42) + "-" + afn.hostname + afn.extension, afn.getFilename(42, false));
        Assert.assertEquals(afn.filePrefix + "-" + afn.dateStr + "-" + String.format("%05d", 43) + "-" + afn.hostname + afn.extension + ".gz", afn.getFilename(43, true));
    }
*/
}
