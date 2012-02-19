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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.RandomAccessFileOutputStream;

@RunWith(JUnit4.class)
public class TestGzipWriter {

    @Test
    public void test_gzip_writer() {
        String in_file = "C:\\Java\\workspace\\jwat\\jwat-warc\\src\\test\\resources\\IAH-20080430204825-00000-blackbook.warc";

        String out_file = "testwrite.gz";

        RandomAccessFile raf;
        RandomAccessFileOutputStream out;
        InputStream in;

        GzipReaderEntry entry = new GzipReaderEntry();
        entry.magic = GzipConstants.GZIP_MAGIC;
        entry.cm = GzipConstants.CM_DEFLATE;
        entry.flg = 0;
        entry.mtime = System.currentTimeMillis() / 1000;
        entry.xfl = 0;
        entry.os = GzipConstants.OS_AMIGA;

        try {
            raf = new RandomAccessFile(out_file, "rw");
            raf.seek(0);
            raf.setLength(0);
            out = new RandomAccessFileOutputStream(raf);

            GzipWriter writer = new GzipWriter(out);
            writer.writeEntryHeader(entry);

            in = new FileInputStream(in_file);
            entry.writeFrom(in);
            in.close();

            writer.close();
            writer.close();

            out.flush();
            out.close();
            raf.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
