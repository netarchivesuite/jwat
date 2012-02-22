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
package org.jwat.gzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestSkippingClose {

    @Test
    public void test_gzipreader() {
        InputStream in;
        ByteCountingPushBackInputStream pbin;
        GzipReader reader;

        String fname = "IAH-20080430204825-00000-blackbook.warc.gz";

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(fname);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin);
            readEntries(reader);
            pbin.close();

            in = this.getClass().getClassLoader().getResourceAsStream(fname);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin, 8192);
            readEntries(reader);
            pbin.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    protected ByteArrayOutputStream out;
    protected byte[] tmpBuf = new byte[768];
    protected InputStream entryIn;

    protected void readEntries(GzipReader reader) {
        int entries = 0;
        String tmpStr;
        try {
            GzipEntry entry;
            while ((entry = reader.getNextEntry()) != null) {
                entryIn = entry.getInputStream();
                tmpStr = entry.toString();
                Assert.assertEquals(1, entryIn.available());
                entry.close();
                Assert.assertFalse(entry.diagnostics.hasErrors());
                Assert.assertFalse(entry.diagnostics.hasWarnings());
                Assert.assertEquals(0, entryIn.available());
                Assert.assertEquals(-1, entryIn.read());
                Assert.assertEquals(-1, entryIn.read(tmpBuf));
                Assert.assertEquals(-1, entryIn.read(tmpBuf, 0, tmpBuf.length));
                Assert.assertEquals(0, entryIn.skip(1024));
                entry.close();
                ++entries;
            }
            reader.close();
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
        Assert.assertEquals(822, entries);
    }

}
