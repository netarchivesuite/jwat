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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.RandomAccessFileOutputStream;

@RunWith(JUnit4.class)
public class TestGzipWriter {

    @Test
    public void test_gzipwriter_file_compress() {
        String in_file = "IAH-20080430204825-00000-blackbook.warc";

        RandomAccessFile raf;
        RandomAccessFileOutputStream out;
        InputStream in;

        GzipWriter writer;
        GzipEntry entry = new GzipEntry();
        entry.magic = GzipConstants.GZIP_MAGIC;
        entry.cm = GzipConstants.CM_DEFLATE;
        entry.flg = 0;
        entry.mtime = System.currentTimeMillis() / 1000;
        entry.xfl = 0;
        entry.os = GzipConstants.OS_AMIGA;

        try {
        	/*
        	 * writeFrom().
        	 */
            File out_file1 = File.createTempFile("jwat-testwrite1-", ".gz");
            //out_file1.deleteOnExit();

            raf = new RandomAccessFile(out_file1, "rw");
            raf.seek(0);
            raf.setLength(0);
            out = new RandomAccessFileOutputStream(raf);

            writer = new GzipWriter(out);
            writer.writeEntryHeader(entry);

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            //in = new FileInputStream(in_file);
            entry.writeFrom(in);
            in.close();

            Assert.assertFalse(entry.diagnostics.hasErrors());
            Assert.assertFalse(entry.diagnostics.hasWarnings());

            writer.close();
            writer.close();

            out.flush();
            out.close();
            raf.close();

            //System.out.println(out_file1.length());
            /*
             * GzipEntryOutputStream.
             */
            File out_file2 = File.createTempFile("jwat-testwrite2-", ".gz");
            //out_file2.deleteOnExit();

            raf = new RandomAccessFile(out_file2, "rw");
            raf.seek(0);
            raf.setLength(0);
            out = new RandomAccessFileOutputStream(raf);

            writer = new GzipWriter(out);
            writer.writeEntryHeader(entry);
            OutputStream cout = entry.getOutputStream();

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            byte[] buffer = new byte[16384];
            int read;
            while ((read = in.read(buffer, 0, 16384)) != -1) {
            	cout.write(buffer, 0, read);
            }
            cout.close();
            in.close();

            Assert.assertFalse(entry.diagnostics.hasErrors());
            Assert.assertFalse(entry.diagnostics.hasWarnings());

            writer.close();
            writer.close();

            out.flush();
            out.close();
            raf.close();

            //System.out.println(out_file2.length());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
