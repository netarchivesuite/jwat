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
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.Digest;
import org.jwat.gzip.GzipReader;

@RunWith(JUnit4.class)
public class TestParams {

    @Test
    public void test_parameters() throws IOException {
        String str;
        InputStream is;
        ByteArrayOutputStream out;

        WarcConstants constants = new WarcConstants();
        Assert.assertNotNull(constants);

        /*
         * Digest.
         */

        Digest digest;

        digest = WarcDigest.parseWarcDigest(null);
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest("");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest("fail");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest(":");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest("sha1:");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest(":AB2CD3EF4GH5IJ6KL7MN8OPQ");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest("sha1:AB2CD3EF4GH5IJ6KL7MN8OPQ");
        Assert.assertNotNull(digest);

        str = digest.toString();
        Assert.assertNotNull(str);

        /*
         * Date.
         */

        Date warcDate;

        warcDate = WarcDateParser.getDate(null);
        Assert.assertNull(warcDate);

        warcDate = WarcDateParser.getDate("");
        Assert.assertNull(warcDate);

        warcDate = WarcDateParser.getDate("fail");
        Assert.assertNull(warcDate);

        warcDate = WarcDateParser.getDate("YYYY-MM-DDThh:mm:ssZ");
        Assert.assertNull(warcDate);

        warcDate = WarcDateParser.getDate("2011-12-24T19:30:00Z");
        Assert.assertNotNull(warcDate);

        /*
         * WarcReaderUncompressed.
         */

        WarcReaderUncompressed readerUncompressed;

        readerUncompressed = new WarcReaderUncompressed();
        Assert.assertFalse(readerUncompressed.isCompressed());

        Assert.assertFalse(readerUncompressed.getBlockDigestEnabled());
        readerUncompressed.setBlockDigestEnabled(true);
        Assert.assertTrue(readerUncompressed.getBlockDigestEnabled());
        readerUncompressed.setBlockDigestEnabled(false);
        Assert.assertFalse(readerUncompressed.getBlockDigestEnabled());

        Assert.assertFalse(readerUncompressed.getPayloadDigestEnabled());
        readerUncompressed.setPayloadDigestEnabled(true);
        Assert.assertTrue(readerUncompressed.getPayloadDigestEnabled());
        readerUncompressed.setPayloadDigestEnabled(false);
        Assert.assertFalse(readerUncompressed.getPayloadDigestEnabled());

        try {
            Assert.assertNull(readerUncompressed.getBlockDigestAlgorithm());
            readerUncompressed.setBlockDigestAlgorithm("sha1");
            Assert.assertNotNull(readerUncompressed.getBlockDigestAlgorithm());
            readerUncompressed.setBlockDigestAlgorithm(null);
            Assert.assertNull(readerUncompressed.getBlockDigestAlgorithm());

            Assert.assertNull(readerUncompressed.getPayloadDigestAlgorithm());
            readerUncompressed.setPayloadDigestAlgorithm("sha1");
            Assert.assertNotNull(readerUncompressed.getPayloadDigestAlgorithm());
            readerUncompressed.setPayloadDigestAlgorithm(null);
            Assert.assertNull(readerUncompressed.getPayloadDigestAlgorithm());
        } catch (NoSuchAlgorithmException e1) {
            Assert.fail("Unexpected exception!");
        }

        Assert.assertEquals("base32", readerUncompressed.getBlockDigestEncoding());
        readerUncompressed.setBlockDigestEncoding("BASE16");
        Assert.assertEquals("base16", readerUncompressed.getBlockDigestEncoding());
        readerUncompressed.setBlockDigestEncoding("base64");
        Assert.assertEquals("base64", readerUncompressed.getBlockDigestEncoding());
        readerUncompressed.setBlockDigestEncoding(null);
        Assert.assertNull(readerUncompressed.getBlockDigestEncoding());
        readerUncompressed.setBlockDigestEncoding("");
        Assert.assertNull(readerUncompressed.getBlockDigestEncoding());

        Assert.assertEquals("base32", readerUncompressed.getPayloadDigestEncoding());
        readerUncompressed.setPayloadDigestEncoding("BASE16");
        Assert.assertEquals("base16", readerUncompressed.getPayloadDigestEncoding());
        readerUncompressed.setPayloadDigestEncoding("base64");
        Assert.assertEquals("base64", readerUncompressed.getPayloadDigestEncoding());
        readerUncompressed.setPayloadDigestEncoding(null);
        Assert.assertNull(readerUncompressed.getPayloadDigestEncoding());
        readerUncompressed.setPayloadDigestEncoding("");
        Assert.assertNull(readerUncompressed.getPayloadDigestEncoding());

        readerUncompressed = new WarcReaderUncompressed();
        try {
            readerUncompressed = new WarcReaderUncompressed(null);
        } catch (IllegalArgumentException e) {
            readerUncompressed = null;
        }
        Assert.assertNull(readerUncompressed);

        readerUncompressed = new WarcReaderUncompressed();
        try {
            readerUncompressed.getNextRecord();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(null, -2);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(null, 0, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        is = new ByteArrayInputStream(new byte[] {42});

        try {
            readerUncompressed.getNextRecordFrom(is, -2);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(is, -2, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(is, -1, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(is, 0, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        is.close();
        is = null;

        /*
         * WarcReaderCompressed.
         */

        WarcReaderCompressed readerCompressed;

        readerCompressed = new WarcReaderCompressed();
        Assert.assertTrue(readerCompressed.isCompressed());

        Assert.assertFalse(readerCompressed.getBlockDigestEnabled());
        readerCompressed.setBlockDigestEnabled(true);
        Assert.assertTrue(readerCompressed.getBlockDigestEnabled());
        readerCompressed.setBlockDigestEnabled(false);
        Assert.assertFalse(readerCompressed.getBlockDigestEnabled());

        Assert.assertFalse(readerCompressed.getPayloadDigestEnabled());
        readerCompressed.setPayloadDigestEnabled(true);
        Assert.assertTrue(readerCompressed.getPayloadDigestEnabled());
        readerCompressed.setPayloadDigestEnabled(false);
        Assert.assertFalse(readerCompressed.getPayloadDigestEnabled());

        try {
            Assert.assertNull(readerCompressed.getBlockDigestAlgorithm());
            readerCompressed.setBlockDigestAlgorithm("sha1");
            Assert.assertNotNull(readerCompressed.getBlockDigestAlgorithm());
            readerCompressed.setBlockDigestAlgorithm(null);
            Assert.assertNull(readerCompressed.getBlockDigestAlgorithm());

            Assert.assertNull(readerCompressed.getPayloadDigestAlgorithm());
            readerCompressed.setPayloadDigestAlgorithm("sha1");
            Assert.assertNotNull(readerCompressed.getPayloadDigestAlgorithm());
            readerCompressed.setPayloadDigestAlgorithm(null);
            Assert.assertNull(readerCompressed.getPayloadDigestAlgorithm());
        } catch (NoSuchAlgorithmException e1) {
            Assert.fail("Unexpected exception!");
        }

        Assert.assertEquals("base32", readerUncompressed.getBlockDigestEncoding());
        readerUncompressed.setBlockDigestEncoding("BASE16");
        Assert.assertEquals("base16", readerUncompressed.getBlockDigestEncoding());
        readerUncompressed.setBlockDigestEncoding("base64");
        Assert.assertEquals("base64", readerUncompressed.getBlockDigestEncoding());
        readerUncompressed.setBlockDigestEncoding(null);
        Assert.assertNull(readerUncompressed.getBlockDigestEncoding());
        readerUncompressed.setBlockDigestEncoding("");
        Assert.assertNull(readerUncompressed.getBlockDigestEncoding());

        Assert.assertEquals("base32", readerUncompressed.getPayloadDigestEncoding());
        readerUncompressed.setPayloadDigestEncoding("BASE16");
        Assert.assertEquals("base16", readerUncompressed.getPayloadDigestEncoding());
        readerUncompressed.setPayloadDigestEncoding("base64");
        Assert.assertEquals("base64", readerUncompressed.getPayloadDigestEncoding());
        readerUncompressed.setPayloadDigestEncoding(null);
        Assert.assertNull(readerUncompressed.getPayloadDigestEncoding());
        readerUncompressed.setPayloadDigestEncoding("");
        Assert.assertNull(readerUncompressed.getPayloadDigestEncoding());

        readerCompressed = new WarcReaderCompressed();
        try {
            readerCompressed = new WarcReaderCompressed(null);
        } catch (IllegalArgumentException e) {
            readerCompressed = null;
        }
        Assert.assertNull(readerCompressed);

        readerCompressed = new WarcReaderCompressed();
        try {
            readerCompressed = new WarcReaderCompressed(null, 42);
        } catch (IllegalArgumentException e) {
            readerCompressed = null;
        }
        Assert.assertNull(readerCompressed);

        GzipReader gzipReader = new GzipReader(new ByteArrayInputStream(new byte[] {42}));

        readerCompressed = new WarcReaderCompressed();
        try {
            readerCompressed = new WarcReaderCompressed(gzipReader, -1);
        } catch (IllegalArgumentException e) {
            readerCompressed = null;
        }
        Assert.assertNull(readerCompressed);

        readerCompressed = new WarcReaderCompressed();
        try {
            readerCompressed = new WarcReaderCompressed(gzipReader, 0);
        } catch (IllegalArgumentException e) {
            readerCompressed = null;
        }
        Assert.assertNull(readerCompressed);

        gzipReader.close();
        gzipReader = null;

        readerCompressed = new WarcReaderCompressed();
        try {
            readerCompressed.getNextRecord();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(null, -2L);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(null, 0, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        is = new ByteArrayInputStream(new byte[] {42});

        try {
            readerCompressed.getNextRecordFrom(is, -2L);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(is, -2, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(is, -1, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(is, 0, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        is.close();
        is = null;

        /*
         * WarcReaderFactory.
         */

        is = new ByteArrayInputStream(new byte[] {42});

        try {
            WarcReaderFactory.getReader(null, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReader(is, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReader(is, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReader(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReaderUncompressed(null, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReaderUncompressed(is, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReaderUncompressed(is, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReaderUncompressed(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReaderCompressed(null, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReaderCompressed(is, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReaderCompressed(is, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            WarcReaderFactory.getReaderCompressed(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        is.close();
        is = null;

        /*
         * WarcWriterUncompressed.
         */

        WarcWriter writer;
        out = null;
        byte[] headerArr = null;
        WarcRecord record = null;

        try {
            writer = new WarcWriterUncompressed(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = new WarcWriterUncompressed(null, 512);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        out = new ByteArrayOutputStream();
        try {
            writer = new WarcWriterUncompressed(out, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = new WarcWriterUncompressed(out, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        writer = new WarcWriterUncompressed(out);
        Assert.assertNotNull(writer);
        Assert.assertFalse(writer.isCompressed());

        writer = new WarcWriterUncompressed(out, 512);
        Assert.assertNotNull(writer);
        Assert.assertFalse(writer.isCompressed());

        try {
            writer.writeHeader(headerArr);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer.writeHeader(record);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer.streamPayload(null, 1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        writer.close();

        /*
         * WarcWriterCompressed.
         */

        out = null;

        try {
            writer = new WarcWriterCompressed(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = new WarcWriterCompressed(null, 512);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        out = new ByteArrayOutputStream();
        try {
            writer = new WarcWriterCompressed(out, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = new WarcWriterCompressed(out, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        writer = new WarcWriterCompressed(out);
        Assert.assertNotNull(writer);
        Assert.assertTrue(writer.isCompressed());

        writer = new WarcWriterCompressed(out, 512);
        Assert.assertNotNull(writer);
        Assert.assertTrue(writer.isCompressed());

        try {
            writer.writeHeader(headerArr);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer.writeHeader(record);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer.streamPayload(null, 1);
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        writer.close();

        /*
         * WarcWriterFactory.
         */

        out = null;

        try {
            writer = WarcWriterFactory.getWriter(null, false);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriter(null, 512, false);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriterUncompressed(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriterUncompressed(null, 512);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriterCompressed(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriterCompressed(null, 512);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        out = new ByteArrayOutputStream();
        try {
            writer = WarcWriterFactory.getWriter(out, -1, false);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriter(out, 0, false);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriterUncompressed(out, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriterUncompressed(out, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriterCompressed(out, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }
        try {
            writer = WarcWriterFactory.getWriterCompressed(out, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        writer = WarcWriterFactory.getWriter(out, false);
        Assert.assertNotNull(writer);
        Assert.assertFalse(writer.isCompressed());

        writer = WarcWriterFactory.getWriter(out, 512, false);
        Assert.assertNotNull(writer);
        Assert.assertFalse(writer.isCompressed());

        writer = WarcWriterFactory.getWriter(out, true);
        Assert.assertNotNull(writer);
        Assert.assertTrue(writer.isCompressed());

        writer = WarcWriterFactory.getWriter(out, 512, true);
        Assert.assertNotNull(writer);
        Assert.assertTrue(writer.isCompressed());

        writer = WarcWriterFactory.getWriterUncompressed(out);
        Assert.assertNotNull(writer);
        Assert.assertFalse(writer.isCompressed());

        writer = WarcWriterFactory.getWriterUncompressed(out, 512);
        Assert.assertNotNull(writer);
        Assert.assertFalse(writer.isCompressed());

        writer = WarcWriterFactory.getWriterCompressed(out);
        Assert.assertNotNull(writer);
        Assert.assertTrue(writer.isCompressed());

        writer = WarcWriterFactory.getWriterCompressed(out, 512);
        Assert.assertNotNull(writer);
        Assert.assertTrue(writer.isCompressed());

        WarcWriterFactory warcWriterFactory = new WarcWriterFactory();
        Assert.assertNotNull(warcWriterFactory);
    }

}
