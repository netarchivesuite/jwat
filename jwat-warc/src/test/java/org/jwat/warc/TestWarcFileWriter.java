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
package org.jwat.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.RandomAccessFileInputStream;

@RunWith(JUnit4.class)
public class TestWarcFileWriter {

    private byte[] tmpBuf = new byte[8192];

    @Test
    public void test_warcfilewriter() {
        WarcFileNaming warcFileNaming;
        WarcFileWriterConfig warcFileWriterConfig;
        WarcFileWriter warcFileWriter;
        int sequenceNr;
        boolean bNewWriter;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        File resourcesFile = TestHelpers.getTestResourceFile("");

        File targetDir = resourcesFile;
        warcFileNaming = new WarcFileNamingSingleFile("single-file.warc");

        File warcFile = new File(resourcesFile, "IAH-20080430204825-00000-blackbook.warc");
        List<Record> srcRecords;
        Record record;
        InputStream in;
        int read;

        RandomAccessFile raf;

        try {
            Assert.assertEquals(false, fileEquals(warcFile, new File(targetDir, "IAH-20080430204825-00000-blackbook.warc.gz")));
            /*
             * Check overwrite.
             */
            File openFile = new File(targetDir, "single-file.warc.open");
            File closedFile = new File(targetDir, "single-file.warc");
            File otherFile = new File(targetDir, "other-file.arc");

            deleteSingleFileWarc(targetDir);

            raf = new RandomAccessFile(new File(targetDir, "single-file.warc.open"), "rw");
            raf.close();
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());

            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, false);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            try {
                warcFileWriter.open();
                Assert.fail("Exception expected!");
            } catch (IOException e) {
                Assert.assertEquals(true, e.getMessage().endsWith("single-file.warc.open' already exists, will not overwrite"));
            }
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());

            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, true);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            warcFileWriter.open();
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());

            deleteSingleFileWarc(targetDir);

            raf = new RandomAccessFile(new File(targetDir, "single-file.warc"), "rw");
            raf.close();
            Assert.assertEquals(false, openFile.exists());
            Assert.assertEquals(true, closedFile.exists());

            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, false);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            try {
                warcFileWriter.open();
                Assert.fail("Exception expected!");
            } catch (IOException e) {
                Assert.assertEquals(true, e.getMessage().endsWith("single-file.warc' already exists, will not overwrite"));
            }
            Assert.assertEquals(false, openFile.exists());
            Assert.assertEquals(true, closedFile.exists());

            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, true);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            warcFileWriter.open();
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());

            deleteSingleFileWarc(targetDir);
            /*
             * nextWriter() for each record.
             */
            raf = new RandomAccessFile(new File(targetDir, "single-file.warc.open"), "rw");
            raf.close();
            raf = new RandomAccessFile(new File(targetDir, "single-file.warc"), "rw");
            raf.close();
            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, true);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            srcRecords = indexWarcFile(warcFile);
            Assert.assertEquals(-1, warcFileWriter.sequenceNr);
            Assert.assertEquals(warcFileWriter.sequenceNr, warcFileWriter.getSequenceNr());
            for (int i=0; i<srcRecords.size(); ++i) {
                record = srcRecords.get(i);
                if (i == 0) {
                    Assert.assertEquals(-1, warcFileWriter.sequenceNr);
                    Assert.assertNull(null, warcFileWriter.writerFile);
                    Assert.assertNull(warcFileWriter.writer_rafout);
                    Assert.assertNull(warcFileWriter.writer_raf);
                    Assert.assertNull(null, warcFileWriter.writer);
                }
                bNewWriter = warcFileWriter.nextWriter();
                if (i == 0) {
                    Assert.assertEquals(true, bNewWriter);
                } else {
                    Assert.assertEquals(false, bNewWriter);
                }
                Assert.assertEquals(0, warcFileWriter.sequenceNr);
                Assert.assertNotNull(warcFileWriter.writerFile);
                Assert.assertNotNull(warcFileWriter.writer_rafout);
                Assert.assertNotNull(warcFileWriter.writer_raf);
                Assert.assertNotNull(warcFileWriter.writer);
                warcFileWriter.writer.writeRawHeader(record.header_bytes, (long)record.payload.length);
                in = new ByteArrayInputStream(record.payload);
                warcFileWriter.writer.streamPayload(in);
            }
            AssertCloseSingleFile(warcFileWriter, targetDir);
            Assert.assertEquals(true, fileEquals(warcFile, closedFile));
            deleteSingleFileWarc(targetDir);
            /*
             * open() for each record.
             */
            raf = new RandomAccessFile(new File(targetDir, "single-file.warc.open"), "rw");
            raf.close();
            raf = new RandomAccessFile(new File(targetDir, "single-file.warc"), "rw");
            raf.close();
            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, true);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            srcRecords = indexWarcFile(warcFile);
            Assert.assertEquals(-1, warcFileWriter.sequenceNr);
            Assert.assertEquals(warcFileWriter.sequenceNr, warcFileWriter.getSequenceNr());
            warcFileWriter.open();
            WarcWriter writer = warcFileWriter.writer;
            for (int i=0; i<srcRecords.size(); ++i) {
                record = srcRecords.get(i);
                Assert.assertEquals(writer, warcFileWriter.writer);
                Assert.assertEquals(0, warcFileWriter.sequenceNr);
                Assert.assertNotNull(warcFileWriter.writerFile);
                Assert.assertNotNull(warcFileWriter.writer_rafout);
                Assert.assertNotNull(warcFileWriter.writer_raf);
                Assert.assertNotNull(warcFileWriter.writer);
                warcFileWriter.open();
                Assert.assertEquals(writer, warcFileWriter.writer);
                Assert.assertEquals(0, warcFileWriter.sequenceNr);
                Assert.assertNotNull(warcFileWriter.writerFile);
                Assert.assertNotNull(warcFileWriter.writer_rafout);
                Assert.assertNotNull(warcFileWriter.writer_raf);
                Assert.assertNotNull(warcFileWriter.writer);
                warcFileWriter.writer.writeRawHeader(record.header_bytes, (long)record.payload.length);
                in = new ByteArrayInputStream(record.payload);
                warcFileWriter.writer.streamPayload(in);
            }
            AssertCloseSingleFile(warcFileWriter, targetDir);
            Assert.assertEquals(true, fileEquals(warcFile, closedFile));
            deleteSingleFileWarc(targetDir);
            /*
             * close().
             */
            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, false);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            warcFileWriter.open();
            try {
                raf = new RandomAccessFile(new File(targetDir, "single-file.warc"), "rw");
                raf.close();
                warcFileWriter.close();
                Assert.fail("Unexpected exception!");
            } catch (IOException e) {
                Assert.assertEquals(true, e.getMessage().startsWith("unable to rename "));
                Assert.assertEquals(true, e.getMessage().endsWith("single-file.warc' - destination file already exists"));
            }
            deleteSingleFileWarc(targetDir);
            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, false);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            warcFileWriter.open();
            try {
                openFile.delete();
                warcFileWriter.close();
                Assert.fail("Unexpected exception!");
            } catch (IOException e) {
                Assert.assertEquals(true, e.getMessage().startsWith("unable to rename "));
                Assert.assertEquals(true, e.getMessage().endsWith("single-file.warc' - unknown problem"));
            }
            deleteSingleFileWarc(targetDir);
            Assert.assertEquals(false, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());
            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 100000, false);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            warcFileWriter.open();
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());
            warcFileWriter.writerFile.renameTo(otherFile);
            warcFileWriter.writerFile = otherFile;
            Assert.assertEquals(false, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());
            warcFileWriter.close();
            /*
             * Multifile.
             */
            String tmpStr;
            File tmpFile;
            RandomAccessFile tmpRaf;
            byte[] tmpBuf = new byte[8192];
            long length;
            byte[] warcFileBytes = new byte[(int)warcFile.length()];
            tmpRaf = new RandomAccessFile(warcFile, "r");
            tmpRaf.readFully(warcFileBytes);
            tmpRaf.close();
            /*
             * nextWriter() for each record.
             */
            GregorianCalendar cal = new GregorianCalendar(2015, 7, 12, 14, 44, 42);
            warcFileNaming = new WarcFileNamingDefault("JWHAT", cal.getTime(), "hostname", ".warghc");
            warcFileWriterConfig = new WarcFileWriterConfig(targetDir, false, 1000000, true);
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            Assert.assertEquals(-1, warcFileWriter.sequenceNr);
            Assert.assertEquals(warcFileWriter.sequenceNr, warcFileWriter.getSequenceNr());
            sequenceNr = -1;
            for (int i=0; i<srcRecords.size(); ++i) {
                record = srcRecords.get(i);
                if (i == 0) {
                    Assert.assertEquals(-1, warcFileWriter.sequenceNr);
                    Assert.assertEquals(-1, warcFileWriter.getSequenceNr());
                    Assert.assertNull(null, warcFileWriter.writerFile);
                    Assert.assertNull(warcFileWriter.writer_rafout);
                    Assert.assertNull(warcFileWriter.writer_raf);
                    Assert.assertNull(null, warcFileWriter.writer);
                }
                bNewWriter = warcFileWriter.nextWriter();
                if (bNewWriter) {
                    ++sequenceNr;
                }
                Assert.assertEquals(sequenceNr, warcFileWriter.sequenceNr);
                Assert.assertEquals(sequenceNr, warcFileWriter.getSequenceNr());
                Assert.assertNotNull(warcFileWriter.writerFile);
                Assert.assertNotNull(warcFileWriter.writer_rafout);
                Assert.assertNotNull(warcFileWriter.writer_raf);
                Assert.assertNotNull(warcFileWriter.writer);
                warcFileWriter.writer.writeRawHeader(record.header_bytes, (long)record.payload.length);
                in = new ByteArrayInputStream(record.payload);
                warcFileWriter.writer.streamPayload(in);
            }
            warcFileWriter.close();
            Assert.assertEquals(sequenceNr, warcFileWriter.sequenceNr);
            Assert.assertEquals(sequenceNr, warcFileWriter.getSequenceNr());
            Assert.assertEquals(9, sequenceNr);
            length = 0;
            bOut.reset();
            for (int i=0; i<=9; i++) {
                tmpStr = warcFileNaming.getFilename(i, false);
                tmpFile = new File(targetDir, tmpStr);
                Assert.assertEquals(true, tmpFile.exists());
                length += tmpFile.length();
                tmpRaf = new RandomAccessFile(tmpFile, "r");
                while ((read = tmpRaf.read(tmpBuf)) != -1) {
                    bOut.write(tmpBuf, 0, read);
                }
                tmpRaf.close();
            }
            bOut.close();
            Assert.assertEquals(warcFile.length(), length);
            Assert.assertArrayEquals(warcFileBytes, bOut.toByteArray());
            /*
             * open(), nextWriter() for each record.
             */
            warcFileWriter = WarcFileWriter.getWarcWriterInstance(warcFileNaming, warcFileWriterConfig);
            Assert.assertEquals(-1, warcFileWriter.sequenceNr);
            Assert.assertEquals(warcFileWriter.sequenceNr, warcFileWriter.getSequenceNr());
            sequenceNr = -1;
            for (int i=0; i<srcRecords.size(); ++i) {
                record = srcRecords.get(i);
                if (i == 0) {
                    Assert.assertEquals(-1, warcFileWriter.sequenceNr);
                    Assert.assertEquals(-1, warcFileWriter.getSequenceNr());
                    Assert.assertNull(null, warcFileWriter.writerFile);
                    Assert.assertNull(warcFileWriter.writer_rafout);
                    Assert.assertNull(warcFileWriter.writer_raf);
                    Assert.assertNull(null, warcFileWriter.writer);
                }
                warcFileWriter.open();
                bNewWriter = warcFileWriter.nextWriter();
                if (bNewWriter) {
                    ++sequenceNr;
                }
                Assert.assertNotEquals(-1, warcFileWriter.sequenceNr);
                Assert.assertNotEquals(-1, warcFileWriter.getSequenceNr());
                Assert.assertNotNull(warcFileWriter.writerFile);
                Assert.assertNotNull(warcFileWriter.writer_rafout);
                Assert.assertNotNull(warcFileWriter.writer_raf);
                Assert.assertNotNull(warcFileWriter.writer);
                warcFileWriter.writer.writeRawHeader(record.header_bytes, (long)record.payload.length);
                in = new ByteArrayInputStream(record.payload);
                warcFileWriter.writer.streamPayload(in);
            }
            warcFileWriter.close();
            Assert.assertEquals(9, warcFileWriter.sequenceNr);
            Assert.assertEquals(9, warcFileWriter.getSequenceNr());
            Assert.assertEquals(8, sequenceNr);
            length = 0;
            bOut.reset();
            for (int i=0; i<=9; i++) {
                tmpStr = warcFileNaming.getFilename(i, false);
                tmpFile = new File(targetDir, tmpStr);
                Assert.assertEquals(true, tmpFile.exists());
                length += tmpFile.length();
                tmpRaf = new RandomAccessFile(tmpFile, "r");
                while ((read = tmpRaf.read(tmpBuf)) != -1) {
                    bOut.write(tmpBuf, 0, read);
                }
                tmpRaf.close();
            }
            bOut.close();
            Assert.assertEquals(warcFile.length(), length);
            Assert.assertArrayEquals(warcFileBytes, bOut.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void deleteSingleFileWarc(File targetDir)throws IOException {
        File openFile = new File(targetDir, "single-file.warc.open");
        if (openFile.exists()) {
            if (!openFile.delete()) {
                Assert.fail("Unable to delete file 'single-file.warc.open'!");
            }
        }
        File closedFile = new File(targetDir, "single-file.warc");
        if (closedFile.exists()) {
            if (!closedFile.delete()) {
                Assert.fail("Unable to delete file 'single-file.warc'!");
            }
        }
        File otherFile = new File(targetDir, "other-file.arc");
        if (otherFile.exists()) {
            if (!otherFile.delete()) {
                Assert.fail("Unable to delete file 'other-file.arc'!");
            }
        }
    }

    public void AssertCloseSingleFile(WarcFileWriter warcFileWriter, File targetDir) throws IOException {
        File openFile = new File(targetDir, "single-file.warc.open");
        File closedFile = new File(targetDir, "single-file.warc");
        Assert.assertEquals(true, openFile.exists());
        Assert.assertEquals(false, closedFile.exists());
        Assert.assertEquals(0, warcFileWriter.sequenceNr);
        Assert.assertEquals(warcFileWriter.sequenceNr, warcFileWriter.getSequenceNr());
        Assert.assertEquals("single-file.warc.open", warcFileWriter.writerFile.getName());
        Assert.assertEquals(warcFileWriter.writerFile, warcFileWriter.getFile());
        Assert.assertNotNull(warcFileWriter.writer_rafout);
        Assert.assertNotNull(warcFileWriter.writer_raf);
        Assert.assertNotNull(warcFileWriter.writer);
        Assert.assertEquals(warcFileWriter.writer, warcFileWriter.getWriter());
        warcFileWriter.close();
        Assert.assertEquals(false, openFile.exists());
        Assert.assertEquals(true, closedFile.exists());
        Assert.assertEquals(0, warcFileWriter.sequenceNr);
        Assert.assertEquals(warcFileWriter.sequenceNr, warcFileWriter.getSequenceNr());
        Assert.assertNull(warcFileWriter.writerFile);
        Assert.assertEquals(warcFileWriter.writerFile, warcFileWriter.getFile());
        Assert.assertNull(warcFileWriter.writer_rafout);
        Assert.assertNull(warcFileWriter.writer_raf);
        Assert.assertNull(warcFileWriter.writer);
        Assert.assertEquals(warcFileWriter.writer, warcFileWriter.getWriter());
        warcFileWriter.close();
        Assert.assertEquals(false, openFile.exists());
        Assert.assertEquals(true, closedFile.exists());
        Assert.assertEquals(0, warcFileWriter.sequenceNr);
        Assert.assertEquals(warcFileWriter.sequenceNr, warcFileWriter.getSequenceNr());
        Assert.assertNull(warcFileWriter.writerFile);
        Assert.assertEquals(warcFileWriter.writerFile, warcFileWriter.getFile());
        Assert.assertNull(warcFileWriter.writer_rafout);
        Assert.assertNull(warcFileWriter.writer_raf);
        Assert.assertNull(warcFileWriter.writer);
        Assert.assertEquals(warcFileWriter.writer, warcFileWriter.getWriter());
    }

    public List<Record> indexWarcFile(File warcFile) throws IOException {
        List<Record> index = new ArrayList<Record>();
        byte[] header_bytes;
        byte[] payload;
        RandomAccessFile raf = new RandomAccessFile(warcFile, "r");
        RandomAccessFileInputStream rafis = new RandomAccessFileInputStream(raf);
        WarcReader warcReader = WarcReaderFactory.getReader(rafis, 16384);
        WarcRecord warcRecord;
        InputStream in;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((warcRecord = warcReader.getNextRecord()) != null) {
            out.reset();
            header_bytes = warcRecord.header.headerBytes;
            in = warcRecord.getPayload().getInputStreamComplete();
            while ((read = in.read(tmpBuf, 0, tmpBuf.length)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            out.close();
            payload = out.toByteArray();
            index.add(new Record(header_bytes, payload));
        }
        return index;
    }

    private class Record {
        public byte[] header_bytes;
        public byte[] payload;
        public Record(byte[] header_bytes, byte[] payload) {
            this.header_bytes = header_bytes;
            this.payload = payload;
        }
    }

    public boolean fileEquals(File expectedFile, File compareFile) throws IOException {
        Assert.assertEquals(true, expectedFile.exists());
        Assert.assertEquals(true, compareFile.exists());
        boolean bEquals = expectedFile.length() == compareFile.length();
        RandomAccessFile expectedRaf = new RandomAccessFile(expectedFile, "r");
        RandomAccessFile compareRaf = new RandomAccessFile(compareFile, "r");
        FileChannel expectedChannel = expectedRaf.getChannel();
        FileChannel compareChannel = compareRaf.getChannel();
        ByteBuffer expectedByteBuffer = ByteBuffer.allocate(8192);
        ByteBuffer compareByteBuffer = ByteBuffer.allocate(8192);
        byte[] expectedBuffer = expectedByteBuffer.array();
        byte[] compareBuffer = compareByteBuffer.array();
        int expectedRead = 0;
        int compareRead = 0;
        int compared = 0;
        int pos;
        int minLimit;
        boolean bLoop = true;
        while (bEquals && bLoop) {
            while ((expectedRead = expectedChannel.read(expectedByteBuffer)) > 0) {
            }
            while ((compareRead = compareChannel.read(compareByteBuffer)) > 0) {
            }
            expectedByteBuffer.flip();
            compareByteBuffer.flip();
            minLimit = Math.min(expectedByteBuffer.limit(), compareByteBuffer.limit());
            pos = 0;
            while (bEquals && pos < minLimit) {
                bEquals = expectedBuffer[pos] == compareBuffer[pos];
                ++pos;
            }
            compared += pos;
            if (bEquals) {
                expectedByteBuffer.position(pos);
                compareByteBuffer.position(pos);
                if (expectedRead == -1 && !expectedByteBuffer.hasRemaining()) {
                    bEquals = (compareRead == -1 && !compareByteBuffer.hasRemaining() && compared == compareFile.length());
                    bLoop = false;
                } else if (compareRead == -1 && !compareByteBuffer.hasRemaining()) {
                    bEquals = (expectedRead == -1 && !expectedByteBuffer.hasRemaining() && compared == expectedFile.length());
                    bLoop = false;
                }
                expectedByteBuffer.compact();
                compareByteBuffer.compact();
            }
        }
        compareRaf.close();
        expectedRaf.close();
        return bEquals;
    }

}
