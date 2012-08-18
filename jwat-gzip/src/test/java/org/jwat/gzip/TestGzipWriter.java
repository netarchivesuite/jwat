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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestGzipWriter {

    @Test
    public void test_gzipwriter_file_compress() {
        String in_file = "IAH-20080430204825-00000-blackbook.warc";

        SecureRandom random = new SecureRandom();

        //RandomAccessFile raf;
        //RandomAccessFileOutputStream out;
        InputStream in;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputStream cout;
        byte[] tmpBuf;
        int read;
        int mod;

        GzipWriter writer;
        GzipEntry entry = new GzipEntry();
        entry.magic = GzipConstants.GZIP_MAGIC;
        entry.cm = GzipConstants.CM_DEFLATE;
        entry.flg = 0;
        entry.mtime = System.currentTimeMillis() / 1000;
        entry.xfl = 0;
        entry.os = GzipConstants.OS_AMIGA;

        try {
            entry.getOutputStream();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }
        try {
            entry.getInputStream();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        try {
            /*
             * writeFrom().
             */
            //File out_file1 = File.createTempFile("jwat-testwrite1-", ".gz");
            //out_file1.deleteOnExit();

            /*
            raf = new RandomAccessFile(out_file1, "rw");
            raf.seek(0);
            raf.setLength(0);
            out = new RandomAccessFileOutputStream(raf);
            */
            out.reset();

            writer = new GzipWriter(out);
            writer.writeEntryHeader(entry);

            try {
                entry.getInputStream();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            //in = new FileInputStream(in_file);
            entry.writeFrom(in);
            in.close();

            Assert.assertFalse(entry.diagnostics.hasErrors());
            Assert.assertFalse(entry.diagnostics.hasWarnings());

            Assert.assertTrue(entry.isCompliant());
            Assert.assertEquals(entry.bIsCompliant, entry.isCompliant());
            Assert.assertTrue(writer.isCompliant());
            Assert.assertEquals(writer.bIsCompliant, writer.isCompliant());

            writer.close();
            writer.close();

            Assert.assertTrue(entry.isCompliant());
            Assert.assertEquals(entry.bIsCompliant, entry.isCompliant());
            Assert.assertTrue(writer.isCompliant());
            Assert.assertEquals(writer.bIsCompliant, writer.isCompliant());

            out.flush();
            out.close();
            //raf.close();

            byte[] out1 = out.toByteArray();

            //System.out.println(out_file1.length());
            /*
             * GzipEntryOutputStream. write(b, off, len).
             */
            //File out_file2 = File.createTempFile("jwat-testwrite2-", ".gz");
            //out_file2.deleteOnExit();

            /*
            raf = new RandomAccessFile(out_file2, "rw");
            raf.seek(0);
            raf.setLength(0);
            out = new RandomAccessFileOutputStream(raf);
            */
            out.reset();

            writer = new GzipWriter(out);
            writer.writeEntryHeader(entry);
            cout = entry.getOutputStream();

            try {
                entry.getInputStream();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            tmpBuf = new byte[16384];
            read = 0;
            while ((read = in.read(tmpBuf, 0, 16384)) != -1) {
                cout.write(tmpBuf, 0, read);
            }
            cout.flush();
            cout.close();
            in.close();

            Assert.assertFalse(entry.diagnostics.hasErrors());
            Assert.assertFalse(entry.diagnostics.hasWarnings());

            Assert.assertTrue(entry.isCompliant());
            Assert.assertEquals(entry.bIsCompliant, entry.isCompliant());
            Assert.assertTrue(writer.isCompliant());
            Assert.assertEquals(writer.bIsCompliant, writer.isCompliant());

            writer.close();
            writer.close();

            Assert.assertTrue(entry.isCompliant());
            Assert.assertEquals(entry.bIsCompliant, entry.isCompliant());
            Assert.assertTrue(writer.isCompliant());
            Assert.assertEquals(writer.bIsCompliant, writer.isCompliant());

            out.flush();
            out.close();
            //raf.close();

            byte[] out2 = out.toByteArray();

            //System.out.println(out_file2.length());
            /*
             * GzipEntryOutputStream. all write methods.
             */
            //File out_file2 = File.createTempFile("jwat-testwrite2-", ".gz");
            //out_file2.deleteOnExit();

            /*
            raf = new RandomAccessFile(out_file2, "rw");
            raf.seek(0);
            raf.setLength(0);
            out = new RandomAccessFileOutputStream(raf);
            */
            out.reset();

            writer = new GzipWriter(out);
            writer.writeEntryHeader(entry);
            cout = entry.getOutputStream();

            try {
                entry.getInputStream();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            tmpBuf = new byte[1024];
            read = 0;
            mod = 2;
            while ( read != -1 ) {
                switch ( mod ) {
                case 0:
                    cout.write( read );
                    break;
                case 1:
                case 2:
                    if (read == tmpBuf.length) {
                        cout.write( tmpBuf );
                    } else {
                        cout.write( tmpBuf, 0, read );
                    }
                    break;
                }

                mod = (mod + 1) % 3;

                switch ( mod ) {
                case 0:
                    read = in.read();
                    break;
                case 1:
                    read = in.read( tmpBuf );
                    break;
                case 2:
                    read = random.nextInt( 1023 ) + 1;
                    read = in.read( tmpBuf, 0, read );
                    break;
                }
            }
            cout.flush();
            cout.close();
            in.close();

            Assert.assertFalse(entry.diagnostics.hasErrors());
            Assert.assertFalse(entry.diagnostics.hasWarnings());

            Assert.assertTrue(entry.isCompliant());
            Assert.assertEquals(entry.bIsCompliant, entry.isCompliant());
            Assert.assertTrue(writer.isCompliant());
            Assert.assertEquals(writer.bIsCompliant, writer.isCompliant());

            writer.close();
            writer.close();

            Assert.assertTrue(entry.isCompliant());
            Assert.assertEquals(entry.bIsCompliant, entry.isCompliant());
            Assert.assertTrue(writer.isCompliant());
            Assert.assertEquals(writer.bIsCompliant, writer.isCompliant());

            out.flush();
            out.close();
            //raf.close();

            byte[] out3 = out.toByteArray();

            //System.out.println(out_file2.length());
            /*
             * Compare.
             */
            Assert.assertArrayEquals(out1, out2);
            Assert.assertArrayEquals(out2, out3);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
