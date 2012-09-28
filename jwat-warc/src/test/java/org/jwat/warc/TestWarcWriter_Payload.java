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
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestWarcWriter_Payload {

    private int min;
    private int max;
    private int runs;
    private boolean bCompress;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 8192, 1, false},
                {1, 8192, 1, true}
        });
    }

    public TestWarcWriter_Payload(int min, int max, int runs, boolean bCompress) {
        this.min = min;
        this.max = max;
        this.runs = runs;
        this.bCompress = bCompress;
    }

    @Test
    public void test_warcwriter_payload() {
        SecureRandom random = new SecureRandom();
        byte[] srcArr;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] tmpArr = new byte[16];

        WarcWriter writer;
        WarcRecord record;
        ByteArrayInputStream inBytes;
        WarcReader reader;
        InputStream in;

        byte[] warcFileBytes1;
        byte[] warcFileBytes2;
        byte[] warcFileBytes3;

        byte[] dstArr1;
        byte[] dstArr2;
        byte[] dstArr3;

        try {
            for ( int r=0; r<runs; ++r) {
                for ( int n=min; n<max; n += 16 ) {
                    srcArr = new byte[ n ];
                    random.nextBytes( srcArr );

                    /*
                     * Write.
                     */

                    out.reset();
                    writer = WarcWriterFactory.getWriter(out, bCompress);
                    if (bCompress) {
                        Assert.assertTrue(writer instanceof WarcWriterCompressed);
                    } else {
                        Assert.assertTrue(writer instanceof WarcWriterUncompressed);
                    }
                    record = WarcRecord.createRecord(writer);
                    record.header.addHeader("WARC-Type", "warcinfo");
                    record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
                    record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
                    record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
                    record.header.addHeader("Content-Length", Integer.toString(srcArr.length));
                    record.header.addHeader("Content-Type", "application/warc-fields");
                    writer.writeHeader(record);
                    inBytes = new ByteArrayInputStream(srcArr);
                    writer.streamPayload(inBytes);
                    writer.closeRecord();
                    writer.close();

                    warcFileBytes1 = out.toByteArray();

                    out.reset();
                    writer = WarcWriterFactory.getWriter(out, bCompress);
                    if (bCompress) {
                        Assert.assertTrue(writer instanceof WarcWriterCompressed);
                    } else {
                        Assert.assertTrue(writer instanceof WarcWriterUncompressed);
                    }
                    record = WarcRecord.createRecord(writer);
                    record.header.addHeader("WARC-Type", "warcinfo");
                    record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
                    record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
                    record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
                    record.header.addHeader("Content-Length", Integer.toString(srcArr.length));
                    record.header.addHeader("Content-Type", "application/warc-fields");
                    writer.writeHeader(record);
                    writer.writePayload(srcArr);
                    writer.closeRecord();
                    writer.close();

                    warcFileBytes2 = out.toByteArray();

                    out.reset();
                    writer = WarcWriterFactory.getWriter(out, bCompress);
                    if (bCompress) {
                        Assert.assertTrue(writer instanceof WarcWriterCompressed);
                    } else {
                        Assert.assertTrue(writer instanceof WarcWriterUncompressed);
                    }
                    record = WarcRecord.createRecord(writer);
                    record.header.addHeader("WARC-Type", "warcinfo");
                    record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
                    record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
                    record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
                    record.header.addHeader("Content-Length", Integer.toString(srcArr.length));
                    record.header.addHeader("Content-Type", "application/warc-fields");
                    writer.writeHeader(record);
                    int remaining = srcArr.length;
                    int idx = 0;
                    int read = 0;
                    int mod = 0;
                    while ( remaining > 0 && read != -1 ) {
                        switch ( mod ) {
                        case 0:
                            //read = in.read();
                            //--remaining;
                            break;
                        case 1:
                            if (remaining >= tmpArr.length) {
                                System.arraycopy(srcArr, idx, tmpArr, 0, tmpArr.length);
                                writer.writePayload(tmpArr);
                                remaining -= tmpArr.length;
                                idx += tmpArr.length;
                            }
                            break;
                        case 2:
                            read = random.nextInt( 15 ) + 1;
                            if (read > remaining) {
                                read = remaining;
                            }
                            writer.writePayload(srcArr, idx, read);
                            remaining -= read;
                            idx += read;
                            break;
                        }
                        mod = (mod + 1) % 3;
                    }
                    writer.closeRecord();
                    writer.close();

                    warcFileBytes3 = out.toByteArray();

                    /*
                     * Read.
                     */

                    out.reset();
                    reader = WarcReaderFactory.getReader(new ByteArrayInputStream(warcFileBytes1));
                    if (bCompress) {
                        Assert.assertTrue(reader instanceof WarcReaderCompressed);
                    } else {
                        Assert.assertTrue(reader instanceof WarcReaderUncompressed);
                    }
                    record = reader.getNextRecord();
                    in = record.getPayload().getInputStream();
                    while ((read = in.read(tmpArr)) != -1) {
                        out.write(tmpArr, 0, read);
                    }
                    reader.close();

                    dstArr1 = out.toByteArray();

                    out.reset();
                    reader = WarcReaderFactory.getReader(new ByteArrayInputStream(warcFileBytes2));
                    if (bCompress) {
                        Assert.assertTrue(reader instanceof WarcReaderCompressed);
                    } else {
                        Assert.assertTrue(reader instanceof WarcReaderUncompressed);
                    }
                    record = reader.getNextRecord();
                    in = record.getPayload().getInputStream();
                    while ((read = in.read(tmpArr)) != -1) {
                        out.write(tmpArr, 0, read);
                    }
                    reader.close();

                    dstArr2 = out.toByteArray();

                    out.reset();
                    reader = WarcReaderFactory.getReader(new ByteArrayInputStream(warcFileBytes3));
                    if (bCompress) {
                        Assert.assertTrue(reader instanceof WarcReaderCompressed);
                    } else {
                        Assert.assertTrue(reader instanceof WarcReaderUncompressed);
                    }
                    record = reader.getNextRecord();
                    in = record.getPayload().getInputStream();
                    while ((read = in.read(tmpArr)) != -1) {
                        out.write(tmpArr, 0, read);
                    }
                    reader.close();

                    dstArr3 = out.toByteArray();

                    // debug
                    //System.out.println(new String(warcFileBytes1));
                    //System.out.println(new String(warcFileBytes2));
                    //System.out.println(new String(warcFileBytes3));

                    // debug
                    //System.out.println(n);

                    Assert.assertArrayEquals(srcArr, dstArr1);
                    Assert.assertArrayEquals(srcArr, dstArr2);
                    Assert.assertArrayEquals(srcArr, dstArr3);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
