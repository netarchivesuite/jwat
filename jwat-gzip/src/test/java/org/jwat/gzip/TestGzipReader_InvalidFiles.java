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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.ZipException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestGzipReader_InvalidFiles {

    @Test
    public void test_invalid_gzip_inputstream() throws IOException {
        InputStream in;
        GzipInputStream gzin;
        GzipInputStreamEntry entry;

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-compression.gz");
        gzin = new GzipInputStream(in);
        try {
            while ((entry = gzin.getNextEntry()) != null) {
                Assert.assertNotNull(entry);
                gzin.closeEntry();
            }
            Assert.assertNull(entry);
            gzin.close();
            Assert.fail("Exception expected!");
        } catch (ZipException e) {
        }
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-entries.gz");
        gzin = new GzipInputStream(in);
        while ((entry = gzin.getNextEntry()) != null) {
            Assert.assertNotNull(entry);
            gzin.closeEntry();
        }
        Assert.assertNull(entry);
        gzin.close();
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-magic.gz");
        gzin = new GzipInputStream(in);
        try {
            while ((entry = gzin.getNextEntry()) != null) {
                Assert.assertNotNull(entry);
                gzin.closeEntry();
            }
            Assert.assertNull(entry);
            gzin.close();
            Assert.fail("Exception expected!");
        } catch (ZipException e) {
        }
        in.close();
    }

    @Test
    public void test_invalid_gzip_reader() throws IOException {
        InputStream in;
        GzipReader reader;
        GzipEntry entry;
        int entries;

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-compression.gz");
        reader = new GzipReader(in);
        try {
            entries = 0;
            while ((entry = reader.getNextEntry()) != null) {
                entry.close();
                Assert.assertTrue(entry.diagnostics.hasErrors());
                Assert.assertFalse(entry.diagnostics.hasWarnings());
                Assert.assertEquals(1, entry.diagnostics.getErrors().size());
                Assert.assertTrue(GzipTestHelper.containsError(entry.diagnostics, DiagnosisType.INVALID_EXPECTED, "Compression Method", 2));
                ++entries;
            }
            reader.close();
            Assert.assertEquals(1, entries);
        } catch (IOException e) {
            Assert.fail("Unexpected exception!");
        }
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-entries.gz");
        reader = new GzipReader(in);
        try {
            entries = 0;
            while ((entry = reader.getNextEntry()) != null) {
                entry.close();
                ++entries;
                if (entries == 1) {
                    Assert.assertFalse(entry.diagnostics.hasErrors());
                    Assert.assertTrue(entry.diagnostics.hasWarnings());
                    Assert.assertEquals(1, entry.diagnostics.getWarnings().size());
                    Assert.assertTrue(GzipTestHelper.containsWarning(entry.diagnostics, DiagnosisType.RESERVED, "eXtra FLags", 1));
                } else if (entries == 2) {
                    Assert.assertFalse(entry.diagnostics.hasErrors());
                    Assert.assertTrue(entry.diagnostics.hasWarnings());
                    Assert.assertEquals(2, entry.diagnostics.getWarnings().size());
                    Assert.assertTrue(GzipTestHelper.containsWarning(entry.diagnostics, DiagnosisType.RESERVED, "FLaGs", 1));
                    Assert.assertTrue(GzipTestHelper.containsWarning(entry.diagnostics, DiagnosisType.UNKNOWN, "Operating System", 1));
                } else if (entries == 3) {
                    Assert.assertTrue(entry.diagnostics.hasErrors());
                    Assert.assertFalse(entry.diagnostics.hasWarnings());
                    Assert.assertEquals(2, entry.diagnostics.getErrors().size());
                    Assert.assertTrue(GzipTestHelper.containsError(entry.diagnostics, DiagnosisType.INVALID_EXPECTED, "CRC32", 2));
                    Assert.assertTrue(GzipTestHelper.containsError(entry.diagnostics, DiagnosisType.INVALID_EXPECTED, "ISize", 2));
                }
            }
            reader.close();
            Assert.assertEquals(3, entries);
        } catch (IOException e) {
            Assert.fail("Unexpected exception!");
        }
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-magic.gz");
        reader = new GzipReader(in);
        try {
            entries = 0;
            while ((entry = reader.getNextEntry()) != null) {
                entry.close();
                Assert.assertTrue(entry.diagnostics.hasErrors());
                Assert.assertFalse(entry.diagnostics.hasWarnings());
                Assert.assertEquals(1, entry.diagnostics.getErrors().size());
                Assert.assertTrue(GzipTestHelper.containsError(entry.diagnostics, DiagnosisType.INVALID_EXPECTED, "Magic Value", 2));
                ++entries;
            }
            reader.close();
            Assert.assertEquals(1, entries);
        } catch (IOException e) {
            Assert.fail("Unexpected exception!");
        }
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-truncated.gz");
        reader = new GzipReader(in);
        try {
            entries = 0;
            while ((entry = reader.getNextEntry()) != null) {
                entry.close();
                ++entries;
                Assert.fail("Exception expected!");
            }
            reader.close();
            Assert.assertEquals(0, entries);
        } catch (IOException e) {
            Assert.assertTrue(e.getCause() instanceof DataFormatException);
        }
        in.close();
    }

    protected ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void test_invalid_header_trailer() {
        GzipEntry wEntry = new GzipEntry();
        wEntry.magic = GzipConstants.GZIP_MAGIC;
        wEntry.cm = GzipConstants.CM_DEFLATE;
        wEntry.flg = 0;
        wEntry.mtime = System.currentTimeMillis() / 1000;
        wEntry.xfl = 0;
        wEntry.os = 142;
        wEntry.bFText = true;
        wEntry.extraBytes = new byte[] { 'W', 'A', 0, 5, 'D', 'o', 'l', 'l', 'y' };
        wEntry.fname = "hello dolly\t\n";
        wEntry.fcomment = "This is my gzip'ed sheep\nDolly!\t\n";
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
            gzipFile = out.toByteArray();

            Assert.assertFalse(wEntry.diagnostics.hasErrors());
            Assert.assertTrue(wEntry.diagnostics.hasWarnings());
            Assert.assertEquals(3, wEntry.diagnostics.getWarnings().size());
            Assert.assertTrue(GzipTestHelper.containsWarning(wEntry.diagnostics, DiagnosisType.UNKNOWN, "Operating System", 1));
            Assert.assertTrue(GzipTestHelper.containsWarning(wEntry.diagnostics, DiagnosisType.INVALID_ENCODING, "FName", 2));
            Assert.assertTrue(GzipTestHelper.containsWarning(wEntry.diagnostics, DiagnosisType.INVALID_ENCODING, "FComment", 2));

            Assert.assertEquals("hello dolly", wEntry.fname);
            Assert.assertEquals("This is my gzip'ed sheep\nDolly!\n", wEntry.fcomment);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        GzipEntry rEntry = null;

        rEntry = readentry(wEntry, data, gzipFile);
        Assert.assertNotNull(rEntry);
        Assert.assertEquals(142, rEntry.os);

        Assert.assertFalse(rEntry.diagnostics.hasErrors());
        Assert.assertTrue(rEntry.diagnostics.hasWarnings());
        Assert.assertEquals(1, rEntry.diagnostics.getWarnings().size());
        Assert.assertTrue(GzipTestHelper.containsWarning(rEntry.diagnostics, DiagnosisType.UNKNOWN, "Operating System", 1));

        gzipFile[0] = GzipConstants.GZIP_MAGIC_HEADER[1];
        gzipFile[1] = GzipConstants.GZIP_MAGIC_HEADER[0];
        gzipFile[2] = 22;
        gzipFile[3] |= GzipConstants.FLG_FRESERVED;
        gzipFile[8] = (byte)0xff;
        gzipFile[9] = 43;
        gzipFile[gzipFile.length-8] = (byte)0xDE;
        gzipFile[gzipFile.length-7] = (byte)0xAD;
        gzipFile[gzipFile.length-6] = (byte)0xBE;
        gzipFile[gzipFile.length-5] = (byte)0xEF;
        gzipFile[gzipFile.length-4] = (byte)0xBE;
        gzipFile[gzipFile.length-3] = (byte)0xEF;
        gzipFile[gzipFile.length-2] = (byte)0xDE;
        gzipFile[gzipFile.length-1] = (byte)0xAD;

        rEntry = readentry(wEntry, data, gzipFile);
        Assert.assertNotNull(rEntry);
        Assert.assertEquals(43, rEntry.os);

        Assert.assertTrue(rEntry.diagnostics.hasErrors());
        Assert.assertTrue(rEntry.diagnostics.hasWarnings());
        Assert.assertEquals(5, rEntry.diagnostics.getErrors().size());
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_EXPECTED, "Magic Value", 2));
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_EXPECTED, "Compression Method", 2));
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_EXPECTED, "CRC16", 2));
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_EXPECTED, "CRC32", 2));
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_EXPECTED, "ISize", 2));
        Assert.assertEquals(2, rEntry.diagnostics.getWarnings().size());
        Assert.assertTrue(GzipTestHelper.containsWarning(rEntry.diagnostics, DiagnosisType.RESERVED, "FLaGs", 1));
        Assert.assertTrue(GzipTestHelper.containsWarning(rEntry.diagnostics, DiagnosisType.UNKNOWN, "Operating System", 1));

        gzipFile[0] = GzipConstants.GZIP_MAGIC_HEADER[0];
        gzipFile[1] = GzipConstants.GZIP_MAGIC_HEADER[1];
        gzipFile[2] = 8;
        gzipFile[3] |= GzipConstants.FLG_FRESERVED;
        gzipFile[8] = (byte)0xff;
        gzipFile[9] = 4;
        gzipFile[30] = 8;
        gzipFile[60] = 8;

        rEntry = readentry(wEntry, data, gzipFile);
        Assert.assertNotNull(rEntry);
        Assert.assertEquals(4, rEntry.os);

        Assert.assertTrue(rEntry.diagnostics.hasErrors());
        Assert.assertTrue(rEntry.diagnostics.hasWarnings());
        Assert.assertEquals(4, rEntry.diagnostics.getErrors().size());
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_DATA, "eXtra FLags", 1));
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_EXPECTED, "CRC16", 2));
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_EXPECTED, "CRC32", 2));
        Assert.assertTrue(GzipTestHelper.containsError(rEntry.diagnostics, DiagnosisType.INVALID_EXPECTED, "ISize", 2));
        Assert.assertEquals(4, rEntry.diagnostics.getWarnings().size());
        Assert.assertTrue(GzipTestHelper.containsWarning(rEntry.diagnostics, DiagnosisType.RESERVED, "eXtra FLags", 1));
        Assert.assertTrue(GzipTestHelper.containsWarning(rEntry.diagnostics, DiagnosisType.RESERVED, "FLaGs", 1));
        Assert.assertTrue(GzipTestHelper.containsWarning(rEntry.diagnostics, DiagnosisType.INVALID_ENCODING, "FName", 2));
        Assert.assertTrue(GzipTestHelper.containsWarning(rEntry.diagnostics, DiagnosisType.INVALID_ENCODING, "FComment", 2));
    }

    protected byte[] tmpBuf = new byte[768];

    public GzipEntry readentry(GzipEntry wEntry, byte[] data, byte[] gzipFile) {
        GzipReader reader;
        reader = new GzipReader(new ByteArrayInputStream(gzipFile));

        GzipEntry rEntry = null;
        InputStream entryIn;
        try {
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
                out.close();
                Assert.assertArrayEquals(data, out.toByteArray());
                out.reset();

                /*
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
                */
            } else {
                Assert.fail("Expected an entry!");
            }
            if (reader.getNextEntry() != null) {
                Assert.fail("Did not expect more entries!");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        return rEntry;
    }

}
