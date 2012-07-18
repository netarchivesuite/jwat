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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.Base16;
import org.jwat.common.Payload;
import org.jwat.common.PayloadWithHeaderAbstract;

@RunWith(Parameterized.class)
public class TestArcReader {

    private String blockAlgo;
    private String blockBase;
    private String payloadAlgo;
    private String payloadBase;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {null, null, null, null},
                {"md5", "base16", "sha1", "base64"},
                {"sha1", "base32", "md5", "base32"},
                {"sha1", "base64", "sha1", "base16"}
        });
    }

    public TestArcReader(String blockAlgo, String blockBase, String payloadAlgo, String payloadBase) {
        this.blockAlgo = blockAlgo;
        this.blockBase = blockBase;
        this.payloadAlgo = payloadAlgo;
        this.payloadBase = payloadBase;
    }

    @Test
    public void test_arcreader() {
        String in_file;
        InputStream in;
        ArcReader reader;
        ArcRecordBase record;
        Payload payload;
        PayloadWithHeaderAbstract payloadWithHeader;
        InputStream in_payload;
        byte[] tmpBuf = new byte[8192];
        int read;
        int records;

        MessageDigest md_block = null;
        if (blockAlgo != null) {
            try {
                md_block = MessageDigest.getInstance(blockAlgo);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        MessageDigest md_payload = null;
        if (payloadAlgo != null) {
            try {
                md_payload = MessageDigest.getInstance(payloadAlgo);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        byte[] block_digest;
        byte[] payload_digest;

        try {
            in_file = "IAH-20080430204825-00000-blackbook.arc";

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            reader = ArcReaderFactory.getReader(in, 8192);
            reader.setBlockDigestAlgorithm(blockAlgo);
            reader.setBlockDigestEncoding(blockBase);
            reader.setBlockDigestEnabled(true);
            reader.setPayloadDigestAlgorithm(payloadAlgo);
            reader.setPayloadDigestEncoding(payloadBase);
            reader.setPayloadDigestEnabled(true);

            records = 0;
            while ((record = reader.getNextRecord()) != null) {
                payload = record.getPayload();
                Assert.assertEquals(record.payload, payload);
                payloadWithHeader = null;
                if (payload != null) {
                    Assert.assertTrue(record.hasPayload());
                    payloadWithHeader = payload.getPayloadHeaderWrapped();
                } else {
                    Assert.assertFalse(record.hasPayload());
                }

                if (md_payload != null) {
                    md_payload.reset();
                }
                if (md_block != null && payload != null) {
                    md_block.reset();
                }

                block_digest = null;
                payload_digest = null;

                if (md_payload != null && payloadWithHeader != null) {
                    md_block.update(payloadWithHeader.getHeader());

                    //System.out.println("--");
                    //System.out.println(new String(payloadWithHeader.getHeader()));
                    //System.out.println("--");

                    in_payload = payloadWithHeader.getPayloadInputStream();
                    while ((read = in_payload.read(tmpBuf)) != -1) {
                        md_block.update(tmpBuf, 0, read);
                        md_payload.update(tmpBuf, 0, read);
                    }
                } else if (md_block != null && payload != null) {
                    in_payload = payload.getInputStream();
                    while ((read = in_payload.read(tmpBuf)) != -1) {
                        md_block.update(tmpBuf, 0, read);
                    }
                }

                Assert.assertFalse(record.isClosed());
                record.close();
                Assert.assertTrue(record.isClosed());
                ++records;

                if (md_block != null && payload != null) {
                    block_digest = md_block.digest();
                    //System.out.println("b1: " + Base16.encodeArray(block_digest));
                    //System.out.println("b2: " + Base16.encodeArray(payload.getDigest()));
                    Assert.assertArrayEquals(block_digest, payload.getDigest());
                }
                if (md_payload != null && payloadWithHeader != null) {
                    payload_digest = md_payload.digest();
                    //System.out.println("p1: " + Base16.encodeArray(payload_digest));
                    //System.out.println("p2: " + Base16.encodeArray(payloadWithHeader.getDigest()));
                    Assert.assertArrayEquals(payload_digest, payloadWithHeader.getDigest());
                }
            }

            Assert.assertEquals(300, records);

            in_file = "IAH-20080430204825-00000-blackbook.arc.gz";

            in = this.getClass().getClassLoader().getResourceAsStream(in_file);
            reader = ArcReaderFactory.getReader(in, 8192);
            reader.setBlockDigestAlgorithm("SHA1");
            reader.setBlockDigestEnabled(true);
            reader.setPayloadDigestAlgorithm("MD5");
            reader.setPayloadDigestEnabled(true);

            records = 0;
            while ((record = reader.getNextRecord()) != null) {
                payload = record.getPayload();
                Assert.assertEquals(record.payload, payload);
                payloadWithHeader = null;
                if (payload != null) {
                    Assert.assertTrue(record.hasPayload());
                    payloadWithHeader = payload.getPayloadHeaderWrapped();
                } else {
                    Assert.assertFalse(record.hasPayload());
                }

                if (payloadWithHeader != null) {
                } else if (payload != null) {
                }

                Assert.assertFalse(record.isClosed());
                record.close();
                Assert.assertTrue(record.isClosed());
                ++records;
            }

            Assert.assertEquals(300, records);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        /*
        md_block.reset();
        md_block.update(httpHeaderBytes);
        md_block.update(payloadBytes);
        blockDigestMd5 = md_block.digest();
        */
    }

}
