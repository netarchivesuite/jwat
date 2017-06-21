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
package org.jwat.arc;

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
public class TestArcFileWriter {

    private byte[] tmpBuf = new byte[8192];

    @Test
    public void test_arcfilewriter() {
        ArcFileNaming arcFileNaming;
        ArcFileWriterConfig arcFileWriterConfig;
        ArcFileWriter arcFileWriter;
        int sequenceNr;
        boolean bNewWriter;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        File resourcesFile = TestHelpers.getTestResourceFile("");

        File targetDir = resourcesFile;
        arcFileNaming = new ArcFileNamingSingleFile("single-file.arc");

        File arcFile = new File(resourcesFile, "4-3-20111004123336-00000-svc-VirtualBox.arc");
        List<Record> srcRecords;
        Record record;
        InputStream in;
        int read;

        RandomAccessFile raf;

        try {
            Assert.assertEquals(false, fileEquals(arcFile, new File(targetDir, "1-1-20110922131213-00000-svc-VirtualBox.arc")));
            /*
             * Check overwrite.
             */
            File openFile = new File(targetDir, "single-file.arc.open");
            File closedFile = new File(targetDir, "single-file.arc");
            File otherFile = new File(targetDir, "other-file.arc");

            deleteSingleFileArc(targetDir);

            raf = new RandomAccessFile(new File(targetDir, "single-file.arc.open"), "rw");
            raf.close();
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());

            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, false);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            try {
                arcFileWriter.open();
                Assert.fail("Exception expected!");
            } catch (IOException e) {
                Assert.assertEquals(true, e.getMessage().endsWith("single-file.arc.open' already exists, will not overwrite"));
            }
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());

            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, true);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            arcFileWriter.open();
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());

            deleteSingleFileArc(targetDir);

            raf = new RandomAccessFile(new File(targetDir, "single-file.arc"), "rw");
            raf.close();
            Assert.assertEquals(false, openFile.exists());
            Assert.assertEquals(true, closedFile.exists());

            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, false);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            try {
                arcFileWriter.open();
                Assert.fail("Exception expected!");
            } catch (IOException e) {
                Assert.assertEquals(true, e.getMessage().endsWith("single-file.arc' already exists, will not overwrite"));
            }
            Assert.assertEquals(false, openFile.exists());
            Assert.assertEquals(true, closedFile.exists());

            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, true);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            arcFileWriter.open();
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());

            deleteSingleFileArc(targetDir);
            /*
             * nextWriter() for each record.
             */
            raf = new RandomAccessFile(new File(targetDir, "single-file.arc.open"), "rw");
            raf.close();
            raf = new RandomAccessFile(new File(targetDir, "single-file.arc"), "rw");
            raf.close();
            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, true);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            srcRecords = indexArcFile(arcFile);
            Assert.assertEquals(-1, arcFileWriter.sequenceNr);
            Assert.assertEquals(arcFileWriter.sequenceNr, arcFileWriter.getSequenceNr());
            for (int i=0; i<srcRecords.size(); ++i) {
                record = srcRecords.get(i);
                if (i == 0) {
                    Assert.assertEquals(-1, arcFileWriter.sequenceNr);
                    Assert.assertNull(null, arcFileWriter.writerFile);
                    Assert.assertNull(arcFileWriter.writer_rafout);
                    Assert.assertNull(arcFileWriter.writer_raf);
                    Assert.assertNull(null, arcFileWriter.writer);
                }
                bNewWriter = arcFileWriter.nextWriter();
                if (i == 0) {
                    Assert.assertEquals(true, bNewWriter);
                } else {
                    Assert.assertEquals(false, bNewWriter);
                }
                Assert.assertEquals(0, arcFileWriter.sequenceNr);
                Assert.assertNotNull(arcFileWriter.writerFile);
                Assert.assertNotNull(arcFileWriter.writer_rafout);
                Assert.assertNotNull(arcFileWriter.writer_raf);
                Assert.assertNotNull(arcFileWriter.writer);
                arcFileWriter.writer.writeRawHeader(record.header_bytes, (long)record.payload.length);
                in = new ByteArrayInputStream(record.payload);
                arcFileWriter.writer.streamPayload(in);
            }
            assertCloseSingleFile(arcFileWriter, targetDir);
            Assert.assertEquals(true, fileEquals(arcFile, closedFile));
            deleteSingleFileArc(targetDir);
            /*
             * open() for each record.
             */
            raf = new RandomAccessFile(new File(targetDir, "single-file.arc.open"), "rw");
            raf.close();
            raf = new RandomAccessFile(new File(targetDir, "single-file.arc"), "rw");
            raf.close();
            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, true);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            srcRecords = indexArcFile(arcFile);
            Assert.assertEquals(-1, arcFileWriter.sequenceNr);
            Assert.assertEquals(arcFileWriter.sequenceNr, arcFileWriter.getSequenceNr());
            arcFileWriter.open();
            ArcWriter writer = arcFileWriter.writer;
            for (int i=0; i<srcRecords.size(); ++i) {
                record = srcRecords.get(i);
                Assert.assertEquals(writer, arcFileWriter.writer);
                Assert.assertEquals(0, arcFileWriter.sequenceNr);
                Assert.assertNotNull(arcFileWriter.writerFile);
                Assert.assertNotNull(arcFileWriter.writer_rafout);
                Assert.assertNotNull(arcFileWriter.writer_raf);
                Assert.assertNotNull(arcFileWriter.writer);
                arcFileWriter.open();
                Assert.assertEquals(writer, arcFileWriter.writer);
                Assert.assertEquals(0, arcFileWriter.sequenceNr);
                Assert.assertNotNull(arcFileWriter.writerFile);
                Assert.assertNotNull(arcFileWriter.writer_rafout);
                Assert.assertNotNull(arcFileWriter.writer_raf);
                Assert.assertNotNull(arcFileWriter.writer);
                arcFileWriter.writer.writeRawHeader(record.header_bytes, (long)record.payload.length);
                in = new ByteArrayInputStream(record.payload);
                arcFileWriter.writer.streamPayload(in);
            }
            assertCloseSingleFile(arcFileWriter, targetDir);
            Assert.assertEquals(true, fileEquals(arcFile, closedFile));
            deleteSingleFileArc(targetDir);
            /*
             * close().
             */
            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, false);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            arcFileWriter.open();
            try {
                raf = new RandomAccessFile(new File(targetDir, "single-file.arc"), "rw");
                raf.close();
                arcFileWriter.close();
                Assert.fail("Unexpected exception!");
            } catch (IOException e) {
                Assert.assertEquals(true, e.getMessage().startsWith("Unable to rename "));
                Assert.assertEquals(true, e.getMessage().endsWith("single-file.arc' - destination file already exists"));
            }
            deleteSingleFileArc(targetDir);
            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, false);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            arcFileWriter.open();
            try {
                openFile.delete();
                arcFileWriter.close();
                Assert.fail("Unexpected exception!");
            } catch (IOException e) {
                Assert.assertEquals(true, e.getMessage().startsWith("Unable to rename "));
                Assert.assertEquals(true, e.getMessage().endsWith("single-file.arc' - unknown problem"));
            }
            deleteSingleFileArc(targetDir);
            Assert.assertEquals(false, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());
            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 100000, false);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            arcFileWriter.open();
            Assert.assertEquals(true, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());
            arcFileWriter.writerFile.renameTo(otherFile);
            arcFileWriter.writerFile = otherFile;
            Assert.assertEquals(false, openFile.exists());
            Assert.assertEquals(false, closedFile.exists());
            arcFileWriter.close();
            /*
             * Multifile.
             */
            String tmpStr;
            File tmpFile;
            RandomAccessFile tmpRaf;
            byte[] tmpBuf = new byte[8192];
            long length;
            byte[] arcFileBytes = new byte[(int)arcFile.length()];
            tmpRaf = new RandomAccessFile(arcFile, "r");
            tmpRaf.readFully(arcFileBytes);
            tmpRaf.close();
            /*
             * nextWriter() for each record.
             */
            GregorianCalendar cal = new GregorianCalendar(2015, 7, 12, 14, 44, 42);
            arcFileNaming = new ArcFileNamingDefault("JWHAT", cal.getTime(), "hostname", ".arghc");
            arcFileWriterConfig = new ArcFileWriterConfig(targetDir, false, 1000000, true);
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            Assert.assertEquals(-1, arcFileWriter.sequenceNr);
            Assert.assertEquals(arcFileWriter.sequenceNr, arcFileWriter.getSequenceNr());
            sequenceNr = -1;
            for (int i=0; i<srcRecords.size(); ++i) {
                record = srcRecords.get(i);
                if (i == 0) {
                    Assert.assertEquals(-1, arcFileWriter.sequenceNr);
                    Assert.assertEquals(-1, arcFileWriter.getSequenceNr());
                    Assert.assertNull(null, arcFileWriter.writerFile);
                    Assert.assertNull(arcFileWriter.writer_rafout);
                    Assert.assertNull(arcFileWriter.writer_raf);
                    Assert.assertNull(null, arcFileWriter.writer);
                }
                bNewWriter = arcFileWriter.nextWriter();
                if (bNewWriter) {
                    ++sequenceNr;
                }
                Assert.assertEquals(sequenceNr, arcFileWriter.sequenceNr);
                Assert.assertEquals(sequenceNr, arcFileWriter.getSequenceNr());
                Assert.assertNotNull(arcFileWriter.writerFile);
                Assert.assertNotNull(arcFileWriter.writer_rafout);
                Assert.assertNotNull(arcFileWriter.writer_raf);
                Assert.assertNotNull(arcFileWriter.writer);
                arcFileWriter.writer.writeRawHeader(record.header_bytes, (long)record.payload.length);
                in = new ByteArrayInputStream(record.payload);
                arcFileWriter.writer.streamPayload(in);
            }
            arcFileWriter.close();
            Assert.assertEquals(sequenceNr, arcFileWriter.sequenceNr);
            Assert.assertEquals(sequenceNr, arcFileWriter.getSequenceNr());
            Assert.assertEquals(8, sequenceNr);
            length = 0;
            bOut.reset();
            for (int i=0; i<=8; i++) {
                tmpStr = arcFileNaming.getFilename(i, false);
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
            Assert.assertEquals(arcFile.length(), length);
            Assert.assertArrayEquals(arcFileBytes, bOut.toByteArray());
            /*
             * open(), nextWriter() for each record.
             */
            arcFileWriter = ArcFileWriter.getArcWriterInstance(arcFileNaming, arcFileWriterConfig);
            Assert.assertEquals(-1, arcFileWriter.sequenceNr);
            Assert.assertEquals(arcFileWriter.sequenceNr, arcFileWriter.getSequenceNr());
            sequenceNr = -1;
            for (int i=0; i<srcRecords.size(); ++i) {
                record = srcRecords.get(i);
                if (i == 0) {
                    Assert.assertEquals(-1, arcFileWriter.sequenceNr);
                    Assert.assertEquals(-1, arcFileWriter.getSequenceNr());
                    Assert.assertNull(null, arcFileWriter.writerFile);
                    Assert.assertNull(arcFileWriter.writer_rafout);
                    Assert.assertNull(arcFileWriter.writer_raf);
                    Assert.assertNull(null, arcFileWriter.writer);
                }
                arcFileWriter.open();
                bNewWriter = arcFileWriter.nextWriter();
                if (bNewWriter) {
                    ++sequenceNr;
                }
                Assert.assertNotEquals(-1, arcFileWriter.sequenceNr);
                Assert.assertNotEquals(-1, arcFileWriter.getSequenceNr());
                Assert.assertNotNull(arcFileWriter.writerFile);
                Assert.assertNotNull(arcFileWriter.writer_rafout);
                Assert.assertNotNull(arcFileWriter.writer_raf);
                Assert.assertNotNull(arcFileWriter.writer);
                arcFileWriter.writer.writeRawHeader(record.header_bytes, (long)record.payload.length);
                in = new ByteArrayInputStream(record.payload);
                arcFileWriter.writer.streamPayload(in);
            }
            arcFileWriter.close();
            Assert.assertEquals(8, arcFileWriter.sequenceNr);
            Assert.assertEquals(8, arcFileWriter.getSequenceNr());
            Assert.assertEquals(7, sequenceNr);
            length = 0;
            bOut.reset();
            for (int i=0; i<=8; i++) {
                tmpStr = arcFileNaming.getFilename(i, false);
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
            Assert.assertEquals(arcFile.length(), length);
            Assert.assertArrayEquals(arcFileBytes, bOut.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public void deleteSingleFileArc(File targetDir)throws IOException {
        File openFile = new File(targetDir, "single-file.arc.open");
        if (openFile.exists()) {
            if (!openFile.delete()) {
                Assert.fail("Unable to delete file 'single-file.arc.open'!");
            }
        }
        File closedFile = new File(targetDir, "single-file.arc");
        if (closedFile.exists()) {
            if (!closedFile.delete()) {
                Assert.fail("Unable to delete file 'single-file.arc'!");
            }
        }
        File otherFile = new File(targetDir, "other-file.arc");
        if (otherFile.exists()) {
            if (!otherFile.delete()) {
                Assert.fail("Unable to delete file 'other-file.arc'!");
            }
        }
    }

    public void assertCloseSingleFile(ArcFileWriter arcFileWriter, File targetDir) throws IOException {
        File openFile = new File(targetDir, "single-file.arc.open");
        File closedFile = new File(targetDir, "single-file.arc");
        Assert.assertEquals(true, openFile.exists());
        Assert.assertEquals(false, closedFile.exists());
        Assert.assertEquals(0, arcFileWriter.sequenceNr);
        Assert.assertEquals(arcFileWriter.sequenceNr, arcFileWriter.getSequenceNr());
        Assert.assertEquals("single-file.arc.open", arcFileWriter.writerFile.getName());
        Assert.assertEquals(arcFileWriter.writerFile, arcFileWriter.getFile());
        Assert.assertNotNull(arcFileWriter.writer_rafout);
        Assert.assertNotNull(arcFileWriter.writer_raf);
        Assert.assertNotNull(arcFileWriter.writer);
        Assert.assertEquals(arcFileWriter.writer, arcFileWriter.getWriter());
        arcFileWriter.close();
        Assert.assertEquals(false, openFile.exists());
        Assert.assertEquals(true, closedFile.exists());
        Assert.assertEquals(0, arcFileWriter.sequenceNr);
        Assert.assertEquals(arcFileWriter.sequenceNr, arcFileWriter.getSequenceNr());
        Assert.assertNull(arcFileWriter.writerFile);
        Assert.assertEquals(arcFileWriter.writerFile, arcFileWriter.getFile());
        Assert.assertNull(arcFileWriter.writer_rafout);
        Assert.assertNull(arcFileWriter.writer_raf);
        Assert.assertNull(arcFileWriter.writer);
        Assert.assertEquals(arcFileWriter.writer, arcFileWriter.getWriter());
        arcFileWriter.close();
        Assert.assertEquals(false, openFile.exists());
        Assert.assertEquals(true, closedFile.exists());
        Assert.assertEquals(0, arcFileWriter.sequenceNr);
        Assert.assertEquals(arcFileWriter.sequenceNr, arcFileWriter.getSequenceNr());
        Assert.assertNull(arcFileWriter.writerFile);
        Assert.assertEquals(arcFileWriter.writerFile, arcFileWriter.getFile());
        Assert.assertNull(arcFileWriter.writer_rafout);
        Assert.assertNull(arcFileWriter.writer_raf);
        Assert.assertNull(arcFileWriter.writer);
        Assert.assertEquals(arcFileWriter.writer, arcFileWriter.getWriter());
    }

    public List<Record> indexArcFile(File arcFile) throws IOException {
        List<Record> index = new ArrayList<Record>();
        byte[] header_bytes;
        byte[] payload;
        RandomAccessFile raf = new RandomAccessFile(arcFile, "r");
        RandomAccessFileInputStream rafis = new RandomAccessFileInputStream(raf);
        ArcReader arcReader = ArcReaderFactory.getReader(rafis, 16384);
        ArcRecordBase arcRecord;
        InputStream in;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((arcRecord = arcReader.getNextRecord()) != null) {
            out.reset();
            header_bytes = arcRecord.header.headerBytes;
            in = arcRecord.getPayload().getInputStreamComplete();
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
