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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestGzipReader {

    @Test
    public void test_gzipreader_nextentry() {
        InputStream in;
        ByteCountingPushBackInputStream pbin;
        GzipReader reader;

        out = new ByteArrayOutputStream();

        String fname = "IAH-20080430204825-00000-blackbook.warc.gz";

        try {
            in = TestHelpers.getTestResourceAsStream(fname);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin);
            readEntries(reader, Integer.MAX_VALUE);
            reader.close();
            pbin.close();

            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());

            in = TestHelpers.getTestResourceAsStream(fname);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin, 8192);
            readEntries(reader, Integer.MAX_VALUE);
            reader.close();
            pbin.close();

            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());

            in = TestHelpers.getTestResourceAsStream(fname);
            pbin = new ByteCountingPushBackInputStream(in, 16);
            reader = new GzipReader(pbin);
            readEntries(reader, 1);
            reader.close();
            pbin.close();

            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());

        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    protected ByteArrayOutputStream out;
    protected byte[] tmpBuf = new byte[768];
    protected InputStream entryIn;

    protected void readEntries(GzipReader reader, int max_entries) {
        int entries = 0;
        int read;
        try {
            GzipEntry entry;
            Assert.assertEquals(-1, reader.getStartOffset());
            Assert.assertEquals(0, reader.getOffset());
            while ((entry = reader.getNextEntry()) != null && (entries < max_entries)) {
                out.reset();
                try {
                    entry.getOutputStream();
                    Assert.fail("Exception expected!");
                } catch (IllegalStateException e) {
                }
                entryIn = entry.getInputStream();
                while ((read = entryIn.read(tmpBuf, 0, tmpBuf.length)) != -1) {
                    out.write(tmpBuf, 0, read);
                }
                entryIn.close();
                Assert.assertEquals(entry.cm, GzipConstants.CM_DEFLATE);
                Assert.assertEquals(entry.crc32, entry.comp_crc32);
                entryIn.close();
                Assert.assertFalse(entry.diagnostics.hasErrors());
                Assert.assertFalse(entry.diagnostics.hasWarnings());
                Assert.assertTrue(entry.isCompliant());
                Assert.assertEquals(entry.bIsCompliant, entry.isCompliant());
                entry.close();
                Assert.assertFalse(entry.diagnostics.hasErrors());
                Assert.assertFalse(entry.diagnostics.hasWarnings());
                Assert.assertTrue(entry.isCompliant());
                Assert.assertEquals(entry.bIsCompliant, entry.isCompliant());
                entry.close();
                out.close();
                out.reset();
                ++entries;
                Assert.assertEquals(entry.getStartOffset(), reader.getStartOffset());
                Assert.assertThat(reader.getStartOffset(), is(not(equalTo(reader.getOffset()))));
            }
            Assert.assertTrue(reader.isCompliant());
            Assert.assertEquals(reader.bIsCompliant, reader.isCompliant());
            reader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
        Assert.assertEquals(Math.min(822, max_entries), entries);
    }

    @Test
    public void test_empty_gzipfile() {
        ByteArrayInputStream in;
        GzipReader reader = null;
        GzipEntry record;
        Diagnosis d;
        try {
            in = new ByteArrayInputStream(new byte[0]);
            reader = new GzipReader(in);
            record = reader.getNextEntry();
            Assert.assertNull(record);
            Assert.assertFalse(reader.isCompliant());
            Assert.assertEquals(1, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            d = reader.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.ERROR_EXPECTED, d.type);
            Assert.assertEquals("GZip file", d.entity);
            Assert.assertEquals(1, d.information.length);
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                Assert.fail("Unexpected exception!");
            }
        }
        try {
            in = new ByteArrayInputStream("GZip".getBytes());
            reader = new GzipReader(in);
            record = reader.getNextEntry();
            Assert.assertNull(record);
            Assert.assertFalse(reader.isCompliant());
            Assert.assertEquals(2, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            d = reader.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.ERROR_EXPECTED, d.type);
            Assert.assertEquals("GZip file", d.entity);
            Assert.assertEquals(1, d.information.length);

            d = reader.diagnostics.getErrors().get(1);
            Assert.assertEquals(DiagnosisType.INVALID_DATA, d.type);
            Assert.assertEquals("GZip file", d.entity);
            Assert.assertEquals(1, d.information.length);
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                Assert.fail("Unexpected exception!");
            }
        }
    }

}
