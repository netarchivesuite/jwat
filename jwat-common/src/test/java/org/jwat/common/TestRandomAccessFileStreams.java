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
package org.jwat.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestRandomAccessFileStreams {

    private SecureRandom random = new SecureRandom();

    private int[] sizes = new int[] {32, 64, 128, 256, 512, 768, 1024, 2048, 4096, 8192, 16384};

    private List<byte[]> buffers = new ArrayList<byte[]>();
    private List<File> files = new ArrayList<File>();
    private List<RandomAccessFile> rams = new ArrayList<RandomAccessFile>();

    private byte[] srcArr;
    private File file;
    private RandomAccessFile ram;

    private RandomAccessFileOutputStream fos;
    private RandomAccessFileInputStream fis;
    private ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
    private byte[] dstArr;

    private int state;
    private int index;
    private int write;
    private byte[] tmpArr;
    private int c;
    private int read;

    @Test
    public void test_randomaccessfile_iostreams() {
        try {
            for (int i=0; i<sizes.length; ++i) {
                srcArr = new byte[sizes[i]];
                random.nextBytes(srcArr);
                file = File.createTempFile("jwat-", ".dat");
                ram = new RandomAccessFile(file, "rw");
                buffers.add(srcArr);
                files.add(file);
                rams.add(ram);

                testSequentialWrites();
                testSequentialReads();
                testSequentialReadsSkips();
                testRandomWrites();
                testSequentialReads();
                testRandomReads();

                RandomAccessFileInputStream fis = new RandomAccessFileInputStream(ram);
                ram.setLength(0);
                fis.mark(16);
                ram.close();
                try {
                    fis.mark(16);
                    Assert.fail("Exception expected!");
                } catch (IllegalStateException e) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    /*
     * Test RAF outputstream with sequential writes.
     */
    private void testSequentialWrites() throws IOException {
        fos = new RandomAccessFileOutputStream(ram);
        ram.seek(0);
        ram.setLength(0);
        state = 0;
        index = 0;
        while (index < srcArr.length) {
            switch (state) {
            case 0:
                fos.write(srcArr[index++]);
                fos.flush();
                break;
            case 1:
                write = random.nextInt(15) + 1;
                if (index + write > srcArr.length) {
                    write = srcArr.length - index;
                }
                fos.write(srcArr, index, write);
                fos.flush();
                index += write;
                break;
            case 2:
                write = random.nextInt(15) + 1;
                if (index + write > srcArr.length) {
                    write = srcArr.length - index;
                }
                tmpArr = new byte[write];
                System.arraycopy(srcArr, index, tmpArr, 0, write);
                fos.write(tmpArr);
                fos.flush();
                index += write;
                break;
            }
            state = (state + 1) % 3;
            Assert.assertEquals(ram.getFilePointer(), index);
        }
        fos.close();
        Assert.assertEquals(srcArr.length, ram.length());
    }

    /**
     * Test RAF inputstream with sequential reads.
     */
    private void testSequentialReads() throws IOException {
        fis = new RandomAccessFileInputStream(ram);
        ram.seek(0);
        dstOut.reset();
        state = 0;
        index = 0;
        Assert.assertTrue( fis.markSupported() );
        try {
            fis.reset();
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
        }
        fis.mark( 1 );
        try {
            fis.reset();
        } catch (IOException e) {
            Assert.fail( "Exception not expected!" );
        }
        while (index < srcArr.length) {
            Assert.assertEquals(srcArr.length - index, fis.available());
            switch (state) {
            case 0:
                c = fis.read();
                if (c == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                dstOut.write(c);
                ++index;
                break;
            case 1:
                read = random.nextInt(15) + 1;
                if (index + read > srcArr.length) {
                    read = srcArr.length - index;
                }
                tmpArr = new byte[read];
                read = fis.read(tmpArr, 0, read);
                if (read == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                dstOut.write(tmpArr, 0, read);
                index += read;
                break;
            case 2:
                read = random.nextInt(15) + 1;
                if (index + read > srcArr.length) {
                    read = srcArr.length - index;
                }
                tmpArr = new byte[read];
                read = fis.read(tmpArr);
                if (read == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                dstOut.write(tmpArr, 0, read);
                index += read;
                break;
            }
            state = (state + 1) % 3;
            Assert.assertEquals(ram.getFilePointer(), index);
        }
        fis.close();
        dstArr = dstOut.toByteArray();
        Assert.assertEquals(ram.length(), dstArr.length);
        Assert.assertArrayEquals(srcArr, dstArr);
    }

    /*
     * Test RAF inputstream with sequential reads and skips.
     */
    private void testSequentialReadsSkips() throws IOException {
        fis = new RandomAccessFileInputStream(ram);
        ram.seek(0);
        state = 0;
        index = 0;
        while (index < srcArr.length) {
            Assert.assertEquals(srcArr.length - index, fis.available());
            switch (state) {
            case 0:
                c = fis.read();
                if (c == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                dstArr[index++] = (byte)c;
                break;
            case 1:
                read = random.nextInt(15) + 1;
                if (index + read > srcArr.length) {
                    read = srcArr.length - index;
                }
                read = fis.read(dstArr, index, read);
                if (read == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                index += read;
                break;
            case 2:
                read = random.nextInt(15) + 1;
                if (index + read > srcArr.length) {
                    read = srcArr.length - index;
                }
                tmpArr = new byte[read];
                read = fis.read(tmpArr);
                if (read == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                System.arraycopy(tmpArr, 0, dstArr, index, read);
                index += read;
                break;
            case 3:
                read = random.nextInt(15) + 1;
                read = (int)fis.skip(read);
                if (read == 0) {
                    Assert.fail("Unexpected EOF!");
                }
                index += read;
                break;
            }
            state = (state + 1) % 4;
            Assert.assertEquals(ram.getFilePointer(), index);
        }
        fis.close();
        Assert.assertArrayEquals(srcArr, dstArr);
    }

    /*
     * Test RAF outputstream with random writes.
     */
    private void testRandomWrites() throws IOException {
        byte[] rndArr = new byte[srcArr.length];
        random.nextBytes(rndArr);
        fos = new RandomAccessFileOutputStream(ram);
        ram.seek(0);
        state = 0;
        index = 0;
        for (int i=0; i<srcArr.length/4; ++i) {
            index = random.nextInt(srcArr.length);
            ram.seek(index);
            switch (state) {
            case 0:
                c = rndArr[index];
                srcArr[index] = (byte)c;
                fos.write(c);
                ++index;
                break;
            case 1:
                write = random.nextInt(15) + 1;
                if (index + write > srcArr.length) {
                    write = srcArr.length - index;
                }
                System.arraycopy(rndArr, index, srcArr, index, write);
                fos.write(srcArr, index, write);
                fos.flush();
                index += write;
                break;
            case 2:
                write = random.nextInt(15) + 1;
                if (index + write > srcArr.length) {
                    write = srcArr.length - index;
                }
                System.arraycopy(rndArr, index, srcArr, index, write);
                tmpArr = new byte[write];
                System.arraycopy(srcArr, index, tmpArr, 0, write);
                fos.write(tmpArr);
                fos.flush();
                index += write;
                break;
            }
            state = (state + 1) % 3;
            Assert.assertEquals(ram.getFilePointer(), index);
        }
        fos.close();
        Assert.assertEquals(srcArr.length, ram.length());
    }

    /*
     * Test RAF inputstream with random reads.
     */
    private void testRandomReads() throws IOException {
        fis = new RandomAccessFileInputStream(ram);
        ram.seek(0);
        state = 0;
        index = 0;
        while (index < srcArr.length) {
            index = random.nextInt(srcArr.length);
            ram.seek(index);
            Assert.assertEquals(srcArr.length - index, fis.available());
            switch (state) {
            case 0:
                c = fis.read();
                if (c == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                dstArr[index++] = (byte)c;
                break;
            case 1:
                read = random.nextInt(15) + 1;
                if (index + read > srcArr.length) {
                    read = srcArr.length - index;
                }
                read = fis.read(dstArr, index, read);
                if (read == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                index += read;
                break;
            case 2:
                read = random.nextInt(15) + 1;
                if (index + read > srcArr.length) {
                    read = srcArr.length - index;
                }
                tmpArr = new byte[read];
                read = fis.read(tmpArr);
                if (read == -1) {
                    Assert.fail("Unexpected EOF!");
                }
                System.arraycopy(tmpArr, 0, dstArr, index, read);
                index += read;
                break;
            }
            state = (state + 1) % 3;
            Assert.assertEquals(ram.getFilePointer(), index);
        }
        fis.close();
        Assert.assertEquals(ram.length(), dstArr.length);
        Assert.assertArrayEquals(srcArr, dstArr);
    }

}
