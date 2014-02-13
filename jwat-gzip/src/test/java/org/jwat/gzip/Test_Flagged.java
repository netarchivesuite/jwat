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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.zip.DataFormatException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class Test_Flagged {

    protected ByteArrayOutputStream out = new ByteArrayOutputStream();

    protected byte[] tmpBuf = new byte[768];

    @Test
    public void test_gzip_writer() {
        GzipEntry wEntry = new GzipEntry();
        wEntry.magic = GzipConstants.GZIP_MAGIC;
        wEntry.cm = GzipConstants.CM_DEFLATE;
        wEntry.flg = 0;
        wEntry.mtime = System.currentTimeMillis() / 1000;
        wEntry.xfl = 0;
        wEntry.os = GzipConstants.OS_AMIGA;
        wEntry.bFText = true;
        wEntry.extraBytes = new byte[] { 'W', 'A', 5, 0, 'D', 'o', 'l', 'l', 'y' };
        wEntry.fname = "hello dolly";
        wEntry.fcomment = "This is my gzip'ed sheep\nDolly!";
        wEntry.bFhCrc = true;

        byte[] data = null;
        try {
            data = "No without my sheep - DOLLY. (æøå)".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        byte[] gzipFile = null;

        try {
            GzipWriter writer = new GzipWriter(out);
            writer.writeEntryHeader(wEntry);
            wEntry.writeFrom(in);
            in.close();
            wEntry.close();
            writer.close();
            out.flush();
            out.close();

            Assert.assertFalse(wEntry.diagnostics.hasErrors());
            Assert.assertFalse(wEntry.diagnostics.hasWarnings());
            Assert.assertTrue(wEntry.isCompliant());
            Assert.assertEquals(wEntry.bIsCompliant, wEntry.isCompliant());

            gzipFile = out.toByteArray();

            GzipReader reader;
            reader = new GzipReader(new ByteArrayInputStream(gzipFile));

            GzipEntry rEntry;
            InputStream entryIn;
            int read;
            if ((rEntry = reader.getNextEntry()) != null) {
                out.reset();
                entryIn = rEntry.getInputStream();
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

                Assert.assertFalse(rEntry.diagnostics.hasErrors());
                Assert.assertFalse(rEntry.diagnostics.hasWarnings());
                Assert.assertTrue(rEntry.isCompliant());
                Assert.assertEquals(rEntry.bIsCompliant, rEntry.isCompliant());

                rEntry.close();
                Assert.assertArrayEquals(data, out.toByteArray());
                out.close();
                out.reset();

                Assert.assertFalse(rEntry.diagnostics.hasErrors());
                Assert.assertFalse(rEntry.diagnostics.hasWarnings());
                Assert.assertTrue(rEntry.isCompliant());
                Assert.assertEquals(rEntry.bIsCompliant, rEntry.isCompliant());

                Assert.assertEquals(wEntry.cm, rEntry.cm);
                Assert.assertEquals(wEntry.flg, rEntry.flg);
                Assert.assertEquals(wEntry.mtime, rEntry.mtime);
                Assert.assertEquals(wEntry.date, rEntry.date);
                Assert.assertEquals(wEntry.xfl, rEntry.xfl);
                Assert.assertEquals(wEntry.os, rEntry.os);
                Assert.assertEquals(wEntry.bFText, rEntry.bFText);
                Assert.assertEquals(wEntry.xlen, rEntry.xlen);
                Assert.assertArrayEquals(wEntry.extraBytes, rEntry.extraBytes);
                Assert.assertEquals(wEntry.fname, rEntry.fname);
                Assert.assertEquals(wEntry.fcomment, rEntry.fcomment);
                Assert.assertEquals(wEntry.bFhCrc, rEntry.bFhCrc);
                Assert.assertEquals(wEntry.crc16, rEntry.crc16);
                Assert.assertEquals(wEntry.comp_crc16, rEntry.comp_crc16);
                Assert.assertEquals(wEntry.crc32, rEntry.crc32);
                Assert.assertEquals(wEntry.comp_crc32, rEntry.comp_crc32);
                Assert.assertEquals(wEntry.isize, rEntry.isize);
                Assert.assertEquals(rEntry.crc16.intValue(), rEntry.comp_crc16);
                Assert.assertEquals(rEntry.crc32, rEntry.comp_crc32);
                Assert.assertEquals(wEntry.toString(), rEntry.toString());
            } else {
                Assert.fail("Expected an entry!");
            }
            if (reader.getNextEntry() != null) {
                Assert.fail("Did not expect more entries!");
            }
            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());
            reader.close();
            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());

            GzipInputStream gzin;
            GzipInputStreamEntry entry;
            InputStream gzis;
            gzin = new GzipInputStream(new ByteArrayInputStream(gzipFile));
            if ((entry = gzin.getNextEntry()) != null) {
                out.reset();
                gzis = gzin.getEntryInputStream();
                Assert.assertEquals(1, gzin.available());
                while ((read = gzin.read(tmpBuf)) != -1) {
                    out.write(tmpBuf, 0, read);
                }
                Assert.assertEquals(0, gzin.available());
                gzis.close();
                Assert.assertEquals(0, gzin.available());
                gzin.closeEntry();
                Assert.assertEquals(0, gzin.available());
                out.close();
                Assert.assertArrayEquals(data, out.toByteArray());
                out.reset();

                Assert.assertEquals(rEntry.cm, entry.method);
                Assert.assertEquals((rEntry.mtime == 0) ? -1 : rEntry.mtime * 1000, entry.getTime());
                Assert.assertEquals(rEntry.os, entry.os);
                Assert.assertEquals(rEntry.xfl, entry.extraFlags);
                Assert.assertEquals(rEntry.fname, entry.fileName);
                Assert.assertEquals(rEntry.fcomment, entry.getComment());
                Assert.assertEquals(rEntry.crc32, rEntry.comp_crc32);
                Assert.assertEquals(rEntry.crc32 & 0xffffffffL, entry.readCrc32 & 0xffffffffL);
                Assert.assertEquals(rEntry.isize, entry.readISize);
            } else {
                Assert.fail("Expected an entry!");
            }
            if (gzin.getNextEntry() != null) {
                Assert.fail("Did not expect more entries!");
            }
            gzin.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        try {
            byte[] partialFile;
            for (int i=0; i<gzipFile.length; ++i) {
                partialFile = new byte[i];
                System.arraycopy(gzipFile, 0, partialFile, 0, i);

                // debug
                //System.out.println(gzipFile.length + " " + i);

                tryread(partialFile, gzipFile.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void tryread(byte[] gzipFile, long length) throws IOException {
        GzipReader reader = new GzipReader(new ByteArrayInputStream(gzipFile));
        GzipEntry entry;
        InputStream entryIn;
        int read;
        if ((entry = reader.getNextEntry()) != null) {
            try {
                out.reset();
                entryIn = entry.getInputStream();
                while ((read = entryIn.read(tmpBuf, 0, tmpBuf.length)) != -1) {
                    out.write(tmpBuf, 0, read);
                }
                entryIn.close();
                entry.close();
                out.close();
                out.reset();
            } catch (IOException e) {
                Assert.assertTrue(e.getCause() instanceof DataFormatException);
            }
            Assert.assertFalse(entry.isCompliant());
        }
        Assert.assertFalse(reader.isCompliant());
        Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());
        try {
            if (reader.getNextEntry() != null) {
                Assert.fail("Did not expect more entries!");
            }
        } catch (IOException e) {
            Assert.assertTrue(e.getCause() instanceof DataFormatException);
        }
        Assert.assertFalse(reader.isCompliant());
        Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());
        reader.close();
        Assert.assertFalse(reader.isCompliant());
        Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());

        // TODO Improvable...
        //System.out.println(reader.diagnostics.getErrors().size());
    }

    @Test
    public void test_gzip_dates() {
        GzipEntry wEntry = new GzipEntry();
        wEntry.magic = GzipConstants.GZIP_MAGIC;
        wEntry.cm = GzipConstants.CM_DEFLATE;
        wEntry.flg = 0;
        wEntry.date = new Date(System.currentTimeMillis());
        wEntry.xfl = 0;
        wEntry.os = GzipConstants.OS_AMIGA;
        wEntry.bFText = true;
        wEntry.extraBytes = new byte[0];
        wEntry.fname = "hello dolly";
        wEntry.fcomment = "This is my gzip'ed sheep\nDolly!";
        wEntry.bFhCrc = true;

        byte[] data = null;
        try {
            data = "No without my sheep - DOLLY. (æøå)".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        byte[] gzipFile = null;

        try {
            GzipWriter writer = new GzipWriter(out);
            writer.writeEntryHeader(wEntry);
            wEntry.writeFrom(in);
            in.close();
            wEntry.close();
            writer.close();
            out.flush();
            out.close();

            Assert.assertFalse(wEntry.diagnostics.hasErrors());
            Assert.assertFalse(wEntry.diagnostics.hasWarnings());
            Assert.assertTrue(wEntry.isCompliant());
            Assert.assertEquals(wEntry.bIsCompliant, wEntry.isCompliant());

            gzipFile = out.toByteArray();

            GzipReader reader;
            reader = new GzipReader(new ByteArrayInputStream(gzipFile));

            GzipEntry rEntry;
            InputStream entryIn;
            int read;
            if ((rEntry = reader.getNextEntry()) != null) {
                out.reset();
                entryIn = rEntry.getInputStream();
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

                Assert.assertFalse(rEntry.diagnostics.hasErrors());
                Assert.assertFalse(rEntry.diagnostics.hasWarnings());
                Assert.assertTrue(rEntry.isCompliant());
                Assert.assertEquals(rEntry.bIsCompliant, rEntry.isCompliant());

                rEntry.close();
                Assert.assertArrayEquals(data, out.toByteArray());
                out.close();
                out.reset();

                Assert.assertFalse(rEntry.diagnostics.hasErrors());
                Assert.assertFalse(rEntry.diagnostics.hasWarnings());
                Assert.assertTrue(rEntry.isCompliant());
                Assert.assertEquals(rEntry.bIsCompliant, rEntry.isCompliant());

                Assert.assertEquals(wEntry.cm, rEntry.cm);
                Assert.assertEquals(wEntry.flg, rEntry.flg);
                Assert.assertEquals(wEntry.mtime, rEntry.mtime);
                Assert.assertEquals((wEntry.date.getTime() / 1000) * 1000, rEntry.date.getTime());
                Assert.assertEquals(wEntry.xfl, rEntry.xfl);
                Assert.assertEquals(wEntry.os, rEntry.os);
                Assert.assertEquals(wEntry.bFText, rEntry.bFText);
                Assert.assertEquals(wEntry.xlen, rEntry.xlen);
                Assert.assertArrayEquals(wEntry.extraBytes, rEntry.extraBytes);
                Assert.assertEquals(wEntry.fname, rEntry.fname);
                Assert.assertEquals(wEntry.fcomment, rEntry.fcomment);
                Assert.assertEquals(wEntry.bFhCrc, rEntry.bFhCrc);
                Assert.assertEquals(wEntry.crc16, rEntry.crc16);
                Assert.assertEquals(wEntry.comp_crc16, rEntry.comp_crc16);
                Assert.assertEquals(wEntry.crc32, rEntry.crc32);
                Assert.assertEquals(wEntry.comp_crc32, rEntry.comp_crc32);
                Assert.assertEquals(wEntry.isize, rEntry.isize);
                Assert.assertEquals(rEntry.crc16.intValue(), rEntry.comp_crc16);
                Assert.assertEquals(rEntry.crc32, rEntry.comp_crc32);
            } else {
                Assert.fail("Expected an entry!");
            }
            if (reader.getNextEntry() != null) {
                Assert.fail("Did not expect more entries!");
            }
            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());
            reader.close();
            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
