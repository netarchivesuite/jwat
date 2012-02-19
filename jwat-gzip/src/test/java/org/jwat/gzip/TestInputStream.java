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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestInputStream {

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

            in = this.getClass().getClassLoader().getResourceAsStream(fname);
            readEntriesOld(in);
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    protected ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected byte[] tmpBuf = new byte[768];
    protected InputStream entryIn;

    protected List<byte[]> dataList = new ArrayList<byte[]>();

    protected void readEntries(GzipReader reader) {
        int entries = 0;
        int read;
        try {
            GzipReaderEntry entry;
            while ((entry = reader.getNextEntry()) != null) {
                out.reset();
                entryIn = entry.getInputStream();
                Assert.assertFalse( entryIn.markSupported() );
                entryIn.mark( 1 );
                try {
                    entryIn.reset();
                    Assert.fail( "Exception expected!" );
                }
                catch (IOException e) {
                    Assert.fail( "Exception expected!" );
                }
                catch (UnsupportedOperationException e) {
                }
                Assert.assertEquals(1, entryIn.available());
                while ((read = entryIn.read(tmpBuf, 0, tmpBuf.length)) != -1) {
                    out.write(tmpBuf, 0, read);
                }
                entryIn.close();
                Assert.assertEquals(0, entryIn.available());
                Assert.assertEquals(-1, entryIn.read());
                Assert.assertEquals(-1, entryIn.read(tmpBuf));
                Assert.assertEquals(-1, entryIn.read(tmpBuf, 0, tmpBuf.length));
                Assert.assertEquals(0, entryIn.skip(1024));
                entryIn.close();
                entry.close();
                Assert.assertEquals(0, entryIn.available());
                Assert.assertEquals(-1, entryIn.read());
                Assert.assertEquals(-1, entryIn.read(tmpBuf));
                Assert.assertEquals(-1, entryIn.read(tmpBuf, 0, tmpBuf.length));
                Assert.assertEquals(0, entryIn.skip(1024));
                entry.close();
                dataList.add(out.toByteArray());
                out.close();
                out.reset();
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

    public void readEntriesOld(InputStream in) {
        GzipInputStream gzin;
        GzipInputStreamEntry entry;
        InputStream gzis;
        int entries = 0;
        int read;
        try {
            gzin = new GzipInputStream(in);
            while ((entry = gzin.getNextEntry()) != null) {
                out.reset();
                gzis = gzin.getEntryInputStream();
                while ((read = gzin.read(tmpBuf)) != -1) {
                    out.write(tmpBuf, 0, read);
                }
                gzis.close();
                gzin.closeEntry();
                out.close();
                Assert.assertArrayEquals(out.toByteArray(), dataList.get(entries));
                out.reset();
                ++entries;
            }
            gzin.close();
            in.close();
        }
        catch (IOException e) {
            Assert.fail("Exception not expected!");
        }
        Assert.assertEquals(822, entries);
    }

}
