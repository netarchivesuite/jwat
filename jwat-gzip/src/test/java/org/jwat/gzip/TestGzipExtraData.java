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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestGzipExtraData {

    @Test
    public void test_gzip_extradata() {
        SecureRandom random = new SecureRandom();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream dIn;
        ByteArrayInputStream in;
        InputStream dOut;
        int read;
        byte[] tmpBuf = new byte[256];
        long startOffset;

        GzipWriter writer;
        GzipEntry entry;
        GzipReader reader;
        GzipExtraData extraData;

        Object[][] expectedDiagnoses = {
                {DiagnosisType.INVALID_DATA, "FEXTRA", 2}
        };

        int ic = 0;
        long[] consumed = new long[6];

        try {
            List<byte[]> srcDataList = new ArrayList<byte[]>();
            srcDataList.add(new byte[65536]);
            srcDataList.add(new byte[65536]);
            srcDataList.add(new byte[65536]);
            srcDataList.add(new byte[65536]);
            srcDataList.add(new byte[65536]);
            srcDataList.add(new byte[65536]);
            int is = 0;
            random.nextBytes(srcDataList.get(is++));
            random.nextBytes(srcDataList.get(is++));
            random.nextBytes(srcDataList.get(is++));
            random.nextBytes(srcDataList.get(is++));
            random.nextBytes(srcDataList.get(is++));
            random.nextBytes(srcDataList.get(is++));

            //GzipTestHelper.printDiagnoses(entry.diagnostics.getErrors());
            //GzipTestHelper.printDiagnoses(entry.diagnostics.getWarnings());

            /*
             * Write.
             */
            is = 0;
            out.reset();
            writer = new GzipWriter(out, 512);
            /*
             * Write 1
             */
            entry = new GzipEntry();
            Assert.assertEquals(GzipConstants.CM_DEFLATE, entry.cm);
            Assert.assertEquals(GzipConstants.OS_UNKNOWN, entry.os);
            writer.writeEntryHeader(entry);
            dIn = new ByteArrayInputStream(srcDataList.get(is++));
            entry.writeFrom(dIn);
            entry.close();
            Assert.assertTrue(entry.isCompliant());

            consumed[ic++] = out.size();
            /*
             * Write 2.
             */
            entry = new GzipEntry();
            entry.extraBytes = new byte[0];
            entry.extraData.add(new GzipExtraData((byte)'A', (byte)'B', "Hello".getBytes()));
            writer.writeEntryHeader(entry);
            dIn = new ByteArrayInputStream(srcDataList.get(is++));
            entry.writeFrom(dIn);
            entry.close();
            Assert.assertTrue(entry.isCompliant());

            consumed[ic++] = out.size();
            /*
             * Write 3.
             */
            entry = new GzipEntry();
            Assert.assertNull(entry.extraBytes);
            entry.extraData.add(new GzipExtraData((byte)'B', (byte)'C', "World!".getBytes()));
            writer.writeEntryHeader(entry);
            dIn = new ByteArrayInputStream(srcDataList.get(is++));
            entry.writeFrom(dIn);
            entry.close();
            Assert.assertTrue(entry.isCompliant());

            consumed[ic++] = out.size();
            /*
             * Write 4.
             */
            entry = new GzipEntry();
            Assert.assertNull(entry.extraBytes);
            entry.extraData.add(new GzipExtraData((byte)'C', (byte)'D', "Hello".getBytes()));
            entry.extraData.add(new GzipExtraData((byte)'D', (byte)'E', "World!".getBytes()));
            writer.writeEntryHeader(entry);
            dIn = new ByteArrayInputStream(srcDataList.get(is++));
            entry.writeFrom(dIn);
            entry.close();
            Assert.assertTrue(entry.isCompliant());

            consumed[ic++] = out.size();
            Assert.assertTrue(writer.isCompliant());
            /*
             * Write 5.
             */
            entry = new GzipEntry();
            entry.extraBytes = new byte[] {(byte)'E', (byte)'F', 5, 0, 'H', 'e', 'l', 'l', 'o', (byte)'F', (byte)'G'};
            entry.extraData.add(new GzipExtraData((byte)'E', (byte)'F', "Hello".getBytes()));
            entry.extraData.add(new GzipExtraData((byte)'F', (byte)'G', "World!".getBytes()));
            writer.writeEntryHeader(entry);
            dIn = new ByteArrayInputStream(srcDataList.get(is++));
            entry.writeFrom(dIn);
            entry.close();
            Assert.assertFalse(entry.isCompliant());

            consumed[ic++] = out.size();

            GzipTestHelper.compareDiagnoses(expectedDiagnoses, entry.diagnostics.getErrors());
            Assert.assertEquals(0, entry.diagnostics.getWarnings().size());

            Assert.assertFalse(writer.isCompliant());
            /*
             * Write 6.
             */
            entry = new GzipEntry();
            entry.extraBytes = new byte[] {(byte)'G', (byte)'H', 5, 0, 'H', 'e', 'l', 'l', 'o', (byte)'H', (byte)'I', 8, 0, 'W', 'o', 'r', 'l', 'd', '!'};
            entry.extraData.add(new GzipExtraData((byte)'G', (byte)'H', "Hello".getBytes()));
            entry.extraData.add(new GzipExtraData((byte)'H', (byte)'I', "World!".getBytes()));
            writer.writeEntryHeader(entry);
            dIn = new ByteArrayInputStream(srcDataList.get(is++));
            entry.writeFrom(dIn);
            entry.close();
            Assert.assertFalse(entry.isCompliant());

            consumed[ic++] = out.size();

            GzipTestHelper.compareDiagnoses(expectedDiagnoses, entry.diagnostics.getErrors());
            Assert.assertEquals(0, entry.diagnostics.getWarnings().size());

            Assert.assertFalse(writer.isCompliant());
            writer.close();
            Assert.assertFalse(writer.isCompliant());
            /*
             * Read.
             */
            ic = 0;
            is = 0;
            startOffset = 0;
            in = new ByteArrayInputStream(out.toByteArray());
            reader = new GzipReader(in, 256);
            /*
             * Read 1.
             */
            Assert.assertEquals(startOffset, reader.getOffset());

            entry = reader.getNextEntry();
            dOut = entry.getInputStream();
            out.reset();
            while ((read = dOut.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            Assert.assertEquals(0, (entry.flg & GzipConstants.FLG_FEXTRA));
            Assert.assertNull(entry.extraBytes);
            Assert.assertEquals(0, entry.extraData.size());
            Assert.assertArrayEquals(srcDataList.get(is++), out.toByteArray());
            Assert.assertTrue(entry.bIsCompliant);

            Assert.assertEquals(reader.consumed, reader.getConsumed());
            Assert.assertEquals(reader.pbin.getConsumed(), reader.getOffset());
            Assert.assertEquals(reader.getConsumed(), reader.getOffset());
            Assert.assertEquals(consumed[ic++], reader.consumed);

            startOffset = reader.getConsumed();
            /*
             * Read 2.
             */
            Assert.assertEquals(startOffset, reader.getOffset());

            entry = reader.getNextEntry();
            dOut = entry.getInputStream();
            out.reset();
            while ((read = dOut.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            Assert.assertEquals(GzipConstants.FLG_FEXTRA, (entry.flg & GzipConstants.FLG_FEXTRA));
            Assert.assertArrayEquals(new byte[0], entry.extraBytes);
            Assert.assertEquals(0, entry.extraData.size());
            Assert.assertArrayEquals(srcDataList.get(is++), out.toByteArray());
            Assert.assertTrue(entry.bIsCompliant);

            Assert.assertEquals(reader.consumed, reader.getConsumed());
            Assert.assertEquals(reader.pbin.getConsumed(), reader.getOffset());
            Assert.assertEquals(reader.getConsumed(), reader.getOffset());
            Assert.assertEquals(consumed[ic++], reader.consumed);

            startOffset = reader.getConsumed();
            /*
             * Read 3.
             */
            Assert.assertEquals(startOffset, reader.getOffset());

            entry = reader.getNextEntry();
            dOut = entry.getInputStream();
            out.reset();
            while ((read = dOut.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            Assert.assertEquals(GzipConstants.FLG_FEXTRA, (entry.flg & GzipConstants.FLG_FEXTRA));
            Assert.assertArrayEquals(new byte[] {(byte)'B', (byte)'C', 6, 0, 'W', 'o', 'r', 'l', 'd', '!'}, entry.extraBytes);
            Assert.assertEquals(1, entry.extraData.size());
            extraData = entry.extraData.get(0);
            Assert.assertEquals('B', extraData.si1);
            Assert.assertEquals('C', extraData.si2);
            Assert.assertArrayEquals("World!".getBytes(), extraData.data);
            Assert.assertArrayEquals(srcDataList.get(is++), out.toByteArray());
            Assert.assertTrue(entry.bIsCompliant);

            Assert.assertEquals(reader.consumed, reader.getConsumed());
            Assert.assertEquals(reader.pbin.getConsumed(), reader.getOffset());
            Assert.assertEquals(reader.getConsumed(), reader.getOffset());
            Assert.assertEquals(consumed[ic++], reader.consumed);

            startOffset = reader.getConsumed();
            /*
             * Read 4.
             */
            Assert.assertEquals(startOffset, reader.getOffset());

            entry = reader.getNextEntry();
            dOut = entry.getInputStream();
            out.reset();
            while ((read = dOut.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            Assert.assertEquals(GzipConstants.FLG_FEXTRA, (entry.flg & GzipConstants.FLG_FEXTRA));
            Assert.assertArrayEquals(new byte[] {(byte)'C', (byte)'D', 5, 0, 'H', 'e', 'l', 'l', 'o', (byte)'D', (byte)'E', 6, 0, 'W', 'o', 'r', 'l', 'd', '!'}, entry.extraBytes);
            Assert.assertEquals(2, entry.extraData.size());
            extraData = entry.extraData.get(0);
            Assert.assertEquals('C', extraData.si1);
            Assert.assertEquals('D', extraData.si2);
            Assert.assertArrayEquals("Hello".getBytes(), extraData.data);
            extraData = entry.extraData.get(1);
            Assert.assertEquals('D', extraData.si1);
            Assert.assertEquals('E', extraData.si2);
            Assert.assertArrayEquals("World!".getBytes(), extraData.data);
            Assert.assertArrayEquals(srcDataList.get(is++), out.toByteArray());
            Assert.assertTrue(entry.bIsCompliant);

            Assert.assertTrue(reader.bIsCompliant);

            Assert.assertEquals(reader.consumed, reader.getConsumed());
            Assert.assertEquals(reader.pbin.getConsumed(), reader.getOffset());
            Assert.assertEquals(reader.getConsumed(), reader.getOffset());
            Assert.assertEquals(consumed[ic++], reader.consumed);

            startOffset = reader.getConsumed();
            /*
             * Read 5.
             */
            Assert.assertEquals(startOffset, reader.getOffset());

            entry = reader.getNextEntry();
            dOut = entry.getInputStream();
            out.reset();
            while ((read = dOut.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            Assert.assertEquals(GzipConstants.FLG_FEXTRA, (entry.flg & GzipConstants.FLG_FEXTRA));
            Assert.assertArrayEquals(new byte[] {(byte)'E', (byte)'F', 5, 0, 'H', 'e', 'l', 'l', 'o', (byte)'F', (byte)'G'}, entry.extraBytes);
            Assert.assertEquals(1, entry.extraData.size());
            extraData = entry.extraData.get(0);
            Assert.assertEquals('E', extraData.si1);
            Assert.assertEquals('F', extraData.si2);
            Assert.assertArrayEquals("Hello".getBytes(), extraData.data);
            Assert.assertArrayEquals(srcDataList.get(is++), out.toByteArray());
            Assert.assertFalse(entry.bIsCompliant);

            GzipTestHelper.compareDiagnoses(expectedDiagnoses, entry.diagnostics.getErrors());
            Assert.assertEquals(0, entry.diagnostics.getWarnings().size());

            Assert.assertFalse(reader.bIsCompliant);

            Assert.assertEquals(reader.consumed, reader.getConsumed());
            Assert.assertEquals(reader.pbin.getConsumed(), reader.getOffset());
            Assert.assertEquals(reader.getConsumed(), reader.getOffset());
            Assert.assertEquals(consumed[ic++], reader.consumed);

            startOffset = reader.getConsumed();
            /*
             * Read 6.
             */
            Assert.assertEquals(startOffset, reader.getOffset());

            entry = reader.getNextEntry();
            dOut = entry.getInputStream();
            out.reset();
            while ((read = dOut.read(tmpBuf)) != -1) {
                out.write(tmpBuf, 0, read);
            }
            entry.close();
            Assert.assertEquals(GzipConstants.FLG_FEXTRA, (entry.flg & GzipConstants.FLG_FEXTRA));
            Assert.assertArrayEquals(new byte[] {(byte)'G', (byte)'H', 5, 0, 'H', 'e', 'l', 'l', 'o', (byte)'H', (byte)'I', 8, 0, 'W', 'o', 'r', 'l', 'd', '!'}, entry.extraBytes);
            Assert.assertEquals(1, entry.extraData.size());
            extraData = entry.extraData.get(0);
            Assert.assertEquals('G', extraData.si1);
            Assert.assertEquals('H', extraData.si2);
            Assert.assertArrayEquals("Hello".getBytes(), extraData.data);
            Assert.assertArrayEquals(srcDataList.get(is++), out.toByteArray());
            Assert.assertFalse(entry.bIsCompliant);

            GzipTestHelper.compareDiagnoses(expectedDiagnoses, entry.diagnostics.getErrors());
            Assert.assertEquals(0, entry.diagnostics.getWarnings().size());

            Assert.assertFalse(reader.bIsCompliant);

            Assert.assertEquals(reader.consumed, reader.getConsumed());
            Assert.assertEquals(reader.pbin.getConsumed(), reader.getOffset());
            Assert.assertEquals(reader.getConsumed(), reader.getOffset());
            Assert.assertEquals(consumed[ic], reader.consumed);

            reader.close();
            Assert.assertFalse(reader.bIsCompliant);

            Assert.assertEquals(reader.consumed, reader.getConsumed());
            Assert.assertNull(reader.pbin);
            Assert.assertEquals(reader.getConsumed(), reader.getOffset());
            Assert.assertEquals(consumed[ic++], reader.consumed);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

    }

}
