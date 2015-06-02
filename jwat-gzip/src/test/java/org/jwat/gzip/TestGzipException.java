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
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestGzipException {

    @Test
    public void test_gzipwriter_exceptions() {
        SecureRandom random = new SecureRandom();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream dOut;
        InputStream dIn;

        ByteArrayInputStream in;

        GzipWriter writer = null;
        GzipEntry entry;
        GzipReader reader;
        int read;

        byte[] srcData = new byte[65536];
        random.nextBytes(srcData);
        byte[] tmpBuf = new byte[65536];

        try {
            out.reset();
            writer = new GzipWriter(out, 256);

            entry = new GzipEntry();
            writer.writeEntryHeader(entry);
            dOut = entry.getOutputStream();
            dOut.write(srcData, 0, srcData.length);
            dOut.close();
            writer.close();

            in = new ByteArrayInputStream(out.toByteArray());
            reader = new GzipReader(in, 256);
            entry = reader.getNextEntry();
            dIn = entry.getInputStream();
            out.reset();
            while ((read = dIn.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            reader.close();
            Assert.assertArrayEquals(srcData, out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        try {
            out.reset();
            writer = new GzipWriter(out, 256);

            entry = new GzipEntry();
            writer.writeEntryHeader(entry);
            dIn = new ByteArrayInputStream(srcData);
            entry.writeFrom(dIn);
            writer.close();

            in = new ByteArrayInputStream(out.toByteArray());
            reader = new GzipReader(in, 256);
            entry = reader.getNextEntry();
            dIn = entry.getInputStream();
            out.reset();
            while ((read = dIn.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            reader.close();
            Assert.assertArrayEquals(srcData, out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        try {
            out.reset();
            writer = new GzipWriter(out, 256);

            entry = new GzipEntry();
            writer.writeEntryHeader(entry);
            dOut = entry.getOutputStream();
            writer.def = new CustomDeflater(writer.def);
            dOut.write(srcData, 0, srcData.length);
            Assert.fail("Exception expected!");
        } catch (IOException e) {
            Assert.assertEquals("Deflater malfunction!", e.getMessage());
        }
        if (writer != null) {
            try {
                writer.close();
                Assert.fail("Exception expected!");
            } catch (IOException e) {
                Assert.assertEquals("Deflater malfunction!", e.getMessage());
            }
        }

        try {
            out.reset();
            writer = new GzipWriter(out, 256);

            entry = new GzipEntry();
            writer.writeEntryHeader(entry);
            dOut = entry.getOutputStream();
            dOut.write(srcData, 0, srcData.length);
            writer.def = new CustomDeflater(writer.def);
            dOut.close();
            Assert.fail("Exception expected!");
        } catch (IOException e) {
            Assert.assertEquals("Deflater malfunction!", e.getMessage());
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                Assert.fail("Unexpected exception!");
            }
        }

        try {
            out.reset();
            writer = new GzipWriter(out, 256);

            entry = new GzipEntry();
            writer.writeEntryHeader(entry);
            writer.def = new CustomDeflater(writer.def);
            dIn = new ByteArrayInputStream(srcData);
            entry.writeFrom(dIn);
            writer.close();
            Assert.fail("Exception expected!");
        } catch (IOException e) {
            Assert.assertEquals("java.util.zip.DataFormatException: Deflater malfunction!", e.getMessage());
        }
        if (writer != null) {
            try {
                writer.close();
                Assert.fail("Exception expected!");
            } catch (IOException e) {
                Assert.assertEquals("Deflater malfunction!", e.getMessage());
            }
        }
    }

    public static class CustomDeflater extends Deflater {
        Deflater def;
        public CustomDeflater(Deflater def) {
            this.def = def;
        }
        @Override
        public int deflate(byte[] b, int off, int len) {
            return super.deflate(b, off, len);
        }
        @Override
        public boolean finished() {
            //return super.finished();
            return false;
        }
        @Override
        public boolean needsInput() {
            //return super.needsInput();
            return false;
        }
    }

    @Test
    public void test_gzipreader_exceptions() {
        SecureRandom random = new SecureRandom();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream dOut;
        InputStream dIn;

        ByteArrayInputStream in;

        GzipWriter writer;
        GzipEntry entry;
        GzipReader reader;
        int read;

        byte[] srcData = new byte[65536];
        random.nextBytes(srcData);
        byte[] tmpBuf = new byte[65536];

        try {
            out.reset();
            writer = new GzipWriter(out, 256);

            entry = new GzipEntry();
            writer.writeEntryHeader(entry);
            dOut = entry.getOutputStream();
            dOut.write(srcData, 0, srcData.length);
            dOut.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        byte[] gzipFile = out.toByteArray();

        try {
            in = new ByteArrayInputStream(gzipFile);
            reader = new GzipReader(in, 256);
            entry = reader.getNextEntry();
            dIn = entry.getInputStream();
            reader.inf = new DictionaryInflater(reader.inf);
            out.reset();
            while ((read = dIn.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            reader.close();
            Assert.fail("Exception expected!");
        } catch (IOException e) {
            Assert.assertEquals("java.util.zip.DataFormatException: Dictionary needed!", e.getMessage());
        }

        try {
            in = new ByteArrayInputStream(gzipFile);
            reader = new GzipReader(in, 256);
            entry = reader.getNextEntry();
            dIn = entry.getInputStream();
            reader.inf = new CustomInflater(reader.inf);
            out.reset();
            while ((read = dIn.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            reader.close();
            Assert.fail("Exception expected!");
        } catch (IOException e) {
            Assert.assertEquals("java.util.zip.DataFormatException: Inflater malfunction!", e.getMessage());
        }

        try {
            in = new ByteArrayInputStream(gzipFile);
            reader = new GzipReader(in, 256);
            entry = reader.getNextEntry();
            Assert.assertEquals(reader, entry.reader);
            entry.close();
            Assert.assertNull(entry.reader);
            entry.bEof = false;
            entry.reader = reader;
            Assert.assertEquals(reader, entry.reader);
            entry.close();
            Assert.assertNull(entry.reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        tmpBuf = new byte[256];

        try {
            in = new ByteArrayInputStream(gzipFile);
            reader = new GzipReader(in, 256);
            entry = reader.getNextEntry();
            dIn = entry.getInputStream();
            reader.inf = new RemainingInflater(reader.inf);
            out.reset();
            while ((read = dIn.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            reader.close();
            Assert.fail("Exception expected!");
        } catch (IOException e) {
            Assert.assertEquals("Remaining larger than lastInput!", e.getMessage());
        }
    }

    public static final class DictionaryInflater extends Inflater {
        Inflater inf;
        public DictionaryInflater(Inflater inf) {
            this.inf = inf;
        }
        @Override
        public boolean needsDictionary() {
            //return super.needsDictionary();
            return true;
        }
    }

    public static final class CustomInflater extends Inflater {
        Inflater inf;
        public CustomInflater(Inflater inf) {
            this.inf = inf;
        }
        @Override
        public boolean needsDictionary() {
            //return super.needsDictionary();
            return false;
        }
        @Override
        public boolean needsInput() {
            //return super.needsInput();
            return false;
        }
    }

    public static final class RemainingInflater extends InflaterWrapper {
        public RemainingInflater(Inflater inf) {
            this.inf = inf;
        }
        @Override
        public int getRemaining() {
            //StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
            //System.out.println(ste.getClassName() + "." + ste.getMethodName());
            //return inf.getRemaining();
            return 12345678;
        }
    }

    public static abstract class InflaterWrapper extends Inflater {
        protected Inflater inf;
        @Override
        public void setInput(byte[] b, int off, int len) {
            inf.setInput(b, off, len);
        }
        @Override
        public void setInput(byte[] b) {
            inf.setInput(b);
        }
        @Override
        public void setDictionary(byte[] b, int off, int len) {
            inf.setDictionary(b, off, len);
        }
        @Override
        public void setDictionary(byte[] b) {
            inf.setDictionary(b);
        }
        @Override
        public int getRemaining() {
            return inf.getRemaining();
        }
        @Override
        public boolean needsInput() {
            return inf.needsInput();
        }
        @Override
        public boolean needsDictionary() {
            return inf.needsDictionary();
        }
        @Override
        public boolean finished() {
            return inf.finished();
        }
        @Override
        public int inflate(byte[] b, int off, int len) throws DataFormatException {
            return inf.inflate(b, off, len);
        }
        @Override
        public int inflate(byte[] b) throws DataFormatException {
            return inf.inflate(b);
        }
        @Override
        public int getAdler() {
            return inf.getAdler();
        }
        @Override
        public int getTotalIn() {
            return inf.getTotalIn();
        }
        @Override
        public long getBytesRead() {
            return inf.getBytesRead();
        }
        @Override
        public int getTotalOut() {
            return inf.getTotalOut();
        }
        @Override
        public long getBytesWritten() {
            return inf.getBytesWritten();
        }
        @Override
        public void reset() {
            inf.reset();
        }
        @Override
        public void end() {
            inf.end();
        }
        /*
        @Override
        protected void finalize() {
            inf.finalize();
        }
        */
    }

}
