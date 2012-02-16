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
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.RandomAccessFileInputStream;
import org.jwat.common.RandomAccessFileOutputStream;

@RunWith(JUnit4.class)
public class TestGzipWriterCloning {

    @Test
    public void test_gzip_writer_cloning() {
        InputStream in;
        ByteCountingPushBackInputStream pbin;
        GzipReader reader;
        GzipWriter writer = null;

        String in_file = "IAH-20080430204825-00000-blackbook.warc.gz";

        String out_file1 = "testwrite2.gz";
        String out_file2 = "testwrite3.gz";

        RandomAccessFile raf;
        RandomAccessFileOutputStream out;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin);

            raf = new RandomAccessFile(out_file1, "rw");
            raf.seek(0);
            raf.setLength(0);
            out = new RandomAccessFileOutputStream(raf);
            writer = new GzipWriter(out);

            cloneEntries(reader, writer);
            pbin.close();

            out.flush();
            out.close();
            raf.close();

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin, 8192);

            raf = new RandomAccessFile(out_file2, "rw");
            raf.seek(0);
            raf.setLength(0);
            out = new RandomAccessFileOutputStream(raf);
            writer = new GzipWriter(out);

            cloneEntries(reader, writer);
            pbin.close();

            out.flush();
            out.close();
            raf.close();

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            readEntriesOld(in);

            raf = new RandomAccessFile(out_file1, "rw");
            raf.seek(0);
            in = new RandomAccessFileInputStream(raf);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin);
            readEntries(reader);
            pbin.close();

            raf = new RandomAccessFile(out_file2, "rw");
            raf.seek(0);
            in = new RandomAccessFileInputStream(raf);
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

    protected InputStream entryIn;
    protected byte[] tmpBuf = new byte[768];

    protected void cloneEntries(GzipReader reader, GzipWriter writer) {
        int entries = 0;
        InputStream entryIn;
        try {
            GzipReaderEntry entry;
            while ((entry = reader.getNextEntry()) != null) {
                entryIn = entry.getInputStream();
                Assert.assertEquals(1, entryIn.available());
                writer.writeEntryHeader(entry);
                entry.writeFrom(entryIn);
                entryIn.close();
                Assert.assertEquals(0, entryIn.available());
                Assert.assertEquals(-1, entryIn.read());
                Assert.assertEquals(-1, entryIn.read(tmpBuf));
                Assert.assertEquals(-1, entryIn.read(tmpBuf, 0, tmpBuf.length));
                Assert.assertEquals(0, entryIn.skip(1024));
                entry.close();
                ++entries;
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
        Assert.assertEquals(822, entries);
    }

    protected ByteArrayOutputStream out = new ByteArrayOutputStream();
    protected List<byte[]> dataList = new ArrayList<byte[]>();
    protected List<GzipEntry> entryList = new ArrayList<GzipEntry>();

    public void readEntriesOld(InputStream in) {
        GzipInputStream gzin;
        GzipEntry entry;
        InputStream gzis;
        int entries = 0;
        int read;
        try {
            gzin = new GzipInputStream(in);
            while ((entry = gzin.getNextEntry()) != null) {
                entryList.add(entry);
                out.reset();
                gzis = gzin.getEntryInputStream();
                while ((read = gzin.read(tmpBuf)) != -1) {
                    out.write(tmpBuf, 0, read);
                }
                gzis.close();
                gzin.closeEntry();
                out.close();
                dataList.add(out.toByteArray());
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

    protected void readEntries(GzipReader reader) {
        int entries = 0;
        int read;
        GzipEntry refEntry;
        try {
            GzipReaderEntry entry;
            while ((entry = reader.getNextEntry()) != null) {
                refEntry = entryList.get(entries);
                out.reset();
                entryIn = entry.getInputStream();
                Assert.assertEquals(1, entryIn.available());
                while ((read = entryIn.read(tmpBuf, 0, tmpBuf.length)) != -1) {
                    out.write(tmpBuf, 0, read);
                }
                entryIn.close();
                Assert.assertEquals(entry.cm, refEntry.method);
                Assert.assertEquals((entry.mtime == 0) ? -1 : entry.mtime, refEntry.getTime());
                Assert.assertEquals(entry.os, refEntry.os);
                Assert.assertEquals(entry.xfl, refEntry.extraFlags);
                Assert.assertEquals(entry.fname, refEntry.fileName);
                Assert.assertEquals(entry.fcomment, refEntry.getComment());
                Assert.assertEquals(entry.crc32, entry.comp_crc32);
                Assert.assertEquals(entry.crc32 & 0xffffffffL, refEntry.readCrc32 & 0xffffffffL);
                Assert.assertEquals(entry.isize, refEntry.readISize);
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
                Assert.assertArrayEquals(out.toByteArray(), dataList.get(entries));
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

}
