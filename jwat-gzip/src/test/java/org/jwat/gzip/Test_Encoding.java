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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;
import org.jwat.common.ISO8859_1;

@RunWith(JUnit4.class)
public class Test_Encoding {

    protected ByteArrayOutputStream out = new ByteArrayOutputStream();

    protected byte[] tmpBuf = new byte[768];

    @Test
    public void test_gzip_writer() {
        StringBuffer sb = new StringBuffer();
        StringBuffer sbFname = new StringBuffer();
        StringBuffer sbFcomment = new StringBuffer();
        for (int i=1; i<256; ++i) {
            sb.append((char)i);
            if (ISO8859_1.validBytes[i] != 0) {
                sbFname.append((char)i);
                sbFcomment.append((char)i);
            } else if (i == 10) {
                sbFcomment.append((char)i);
            }
        }

        GzipEntry wEntry = new GzipEntry();
        wEntry.magic = GzipConstants.GZIP_MAGIC;
        wEntry.cm = GzipConstants.CM_DEFLATE;
        wEntry.flg = 0;
        wEntry.mtime = System.currentTimeMillis() / 1000;
        wEntry.xfl = 0;
        wEntry.os = GzipConstants.OS_AMIGA;
        wEntry.bFText = true;
        wEntry.extraBytes = new byte[] { 'W', 'A', 0, 5, 'D', 'o', 'l', 'l', 'y' };
        wEntry.fname = sb.toString();
        wEntry.fcomment = sb.toString();
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
            Assert.assertTrue(wEntry.diagnostics.hasWarnings());

            Assert.assertEquals(2, wEntry.diagnostics.getWarnings().size());
            Assert.assertTrue(GzipTestHelper.containsWarning(wEntry.diagnostics, DiagnosisType.INVALID_ENCODING, "FName", 2));
            Assert.assertTrue(GzipTestHelper.containsWarning(wEntry.diagnostics, DiagnosisType.INVALID_ENCODING, "FComment", 2));

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
                rEntry.close();
                Assert.assertArrayEquals(data, out.toByteArray());
                out.close();
                out.reset();

                Assert.assertFalse(rEntry.diagnostics.hasErrors());
                Assert.assertFalse(rEntry.diagnostics.hasWarnings());

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
                Assert.assertEquals(wEntry.fname, sbFname.toString());
                Assert.assertEquals(rEntry.fname, sbFname.toString());
                Assert.assertEquals(wEntry.fcomment, sbFcomment.toString());
                Assert.assertEquals(rEntry.fcomment, sbFcomment.toString());
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
            reader.close();

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
    }

}
