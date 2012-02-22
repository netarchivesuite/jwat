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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestInputStreamReadSkip {

    @Test
    public void test_gzipreader() {
        InputStream in;
        ByteCountingPushBackInputStream pbin;
        GzipReader reader;

        out = new ByteArrayOutputStream();

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
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin);
            readEntryTotalTripleTrouble(reader);
            pbin.close();

            in = this.getClass().getClassLoader().getResourceAsStream(fname);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin, 8192);
            readEntryTotalTripleTrouble(reader);
            pbin.close();

            in = this.getClass().getClassLoader().getResourceAsStream(fname);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin);
            readEntryTotalTripleTroubleSkip(reader);
            pbin.close();

            in = this.getClass().getClassLoader().getResourceAsStream(fname);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin, 8192);
            readEntryTotalTripleTroubleSkip(reader);
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

    protected List<byte[]> dataList = new ArrayList<byte[]>();

    protected SecureRandom random = new SecureRandom();

    protected void readEntries(GzipReader reader) {
        int entries = 0;
        int read;
        try {
            GzipEntry entry;
            while ((entry = reader.getNextEntry()) != null) {
                out.reset();
                entryIn = entry.getInputStream();
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
                Assert.assertFalse(entry.diagnostics.hasErrors());
                Assert.assertFalse(entry.diagnostics.hasWarnings());
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

    protected void readEntryTotalTripleTrouble(GzipReader reader) {
        int entries = 0;
        byte[] tmpReadBuf = new byte[16];
        int read;
        int mod;
        try {
            GzipEntry entry;
            while ((entry = reader.getNextEntry()) != null) {
                out.reset();
                entryIn = entry.getInputStream();
                Assert.assertEquals(1, entryIn.available());
                read = 0;
                mod = 2;
                while ( read != -1 ) {
                    switch ( mod ) {
                    case 0:
                        out.write( read );
                        break;
                    case 1:
                        out.write( tmpReadBuf, 0, read );
                        break;
                    case 2:
                        out.write( tmpReadBuf, 0, read );
                        break;
                    }
                    mod = (mod + 1) % 3;
                    switch ( mod ) {
                    case 0:
                        read = entryIn.read();
                        break;
                    case 1:
                        read = entryIn.read( tmpReadBuf );
                        break;
                    case 2:
                        read = random.nextInt( 15 ) + 1;
                        read = entryIn.read( tmpReadBuf, 0, read );
                        break;
                    }
                }
                entryIn.close();
                Assert.assertEquals(0, entryIn.available());
                Assert.assertEquals(-1, entryIn.read());
                Assert.assertEquals(-1, entryIn.read(tmpBuf));
                Assert.assertEquals(-1, entryIn.read(tmpBuf, 0, tmpBuf.length));
                Assert.assertEquals(0, entryIn.skip(1024));
                entryIn.close();
                entry.close();
                Assert.assertFalse(entry.diagnostics.hasErrors());
                Assert.assertFalse(entry.diagnostics.hasWarnings());
                Assert.assertEquals(0, entryIn.available());
                Assert.assertEquals(-1, entryIn.read());
                Assert.assertEquals(-1, entryIn.read(tmpBuf));
                Assert.assertEquals(-1, entryIn.read(tmpBuf, 0, tmpBuf.length));
                Assert.assertEquals(0, entryIn.skip(1024));
                entry.close();
                Assert.assertArrayEquals(dataList.get(entries), out.toByteArray());
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

    protected void readEntryTotalTripleTroubleSkip(GzipReader reader) {
        int entries = 0;
        byte[] tmpReadBuf = new byte[16];
        byte[] out;
        int index;
        int read;
        int mod;
        try {
            GzipEntry entry;
            while ((entry = reader.getNextEntry()) != null) {
                entryIn = entry.getInputStream();
                Assert.assertEquals(1, entryIn.available());
                out = new byte[dataList.get(entries).length];
                System.arraycopy(dataList.get(entries), 0, out, 0, dataList.get(entries).length);
                index = 0;
                read = 0;
                mod = 3;
                while ( read != -1 ) {
                    switch ( mod ) {
                    case 0:
                        out[ index ] = (byte)read;
                        index += 1;
                        break;
                    case 1:
                        System.arraycopy(tmpReadBuf, 0, out, index, read);
                        index += read;
                        break;
                    case 2:
                        System.arraycopy(tmpReadBuf, 0, out, index, read);
                        index += read;
                        break;
                    case 3:
                        index += read;
                        break;
                    }
                    mod = (mod + 1) % 4;
                    switch ( mod ) {
                    case 0:
                        read = entryIn.read();
                        break;
                    case 1:
                        read = entryIn.read( tmpReadBuf );
                        break;
                    case 2:
                        read = random.nextInt( 15 ) + 1;
                        read = entryIn.read( tmpReadBuf, 0, read );
                        break;
                    case 3:
                        read = random.nextInt( 15 ) + 1;
                        read = (int)entryIn.skip( read );
                        break;
                    }
                }
                entryIn.close();
                Assert.assertEquals(0, entryIn.available());
                Assert.assertEquals(-1, entryIn.read());
                Assert.assertEquals(-1, entryIn.read(tmpBuf));
                Assert.assertEquals(-1, entryIn.read(tmpBuf, 0, tmpBuf.length));
                Assert.assertEquals(0, entryIn.skip(1024));
                entryIn.close();
                entry.close();
                Assert.assertFalse(entry.diagnostics.hasErrors());
                Assert.assertFalse(entry.diagnostics.hasWarnings());
                Assert.assertEquals(0, entryIn.available());
                Assert.assertEquals(-1, entryIn.read());
                Assert.assertEquals(-1, entryIn.read(tmpBuf));
                Assert.assertEquals(-1, entryIn.read(tmpBuf, 0, tmpBuf.length));
                Assert.assertEquals(0, entryIn.skip(1024));
                entry.close();
                Assert.assertArrayEquals(dataList.get(entries), out);
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
