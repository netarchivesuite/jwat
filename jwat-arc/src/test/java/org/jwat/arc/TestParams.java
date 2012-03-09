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
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.gzip.GzipReader;

@RunWith(JUnit4.class)
public class TestParams {

    @Test
    public void test_parameters() throws IOException {
        String str;
        InputStream is;

        /*
         * Date.
         */

        Date warcDate;

        warcDate = ArcDateParser.getDate(null);
        Assert.assertNull(warcDate);

        warcDate = ArcDateParser.getDate("");
        Assert.assertNull(warcDate);

        warcDate = ArcDateParser.getDate("fail");
        Assert.assertNull(warcDate);

        warcDate = ArcDateParser.getDate("yyyyMMddHHmmss");
        Assert.assertNull(warcDate);

        warcDate = ArcDateParser.getDate("20111224193000");
        Assert.assertNotNull(warcDate);

        /*
         * ArcVersion.
         */

        ArcVersion av;

        av = ArcVersion.VERSION_1;
        Assert.assertNotNull(av);
        str = av.toString();
        Assert.assertNotNull(str);

        av = ArcVersion.VERSION_1_1;
        Assert.assertNotNull(av);
        str = av.toString();
        Assert.assertNotNull(str);

        av = ArcVersion.VERSION_2;
        Assert.assertNotNull(av);
        str = av.toString();
        Assert.assertNotNull(str);

        /*
         * ArcReaderUncompressed.
         */

        ArcReaderUncompressed readerUncompressed;

        readerUncompressed = new ArcReaderUncompressed();
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

        readerUncompressed = new ArcReaderUncompressed();
        try {
            readerUncompressed.getVersionBlockFrom(null, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        readerUncompressed = new ArcReaderUncompressed();
        try {
            readerUncompressed.getVersionBlock();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        readerUncompressed = new ArcReaderUncompressed();
        try {
            readerUncompressed = new ArcReaderUncompressed(null);
        } catch (IllegalArgumentException e) {
            readerUncompressed = null;
        }
        Assert.assertNull(readerUncompressed);

        readerUncompressed = new ArcReaderUncompressed();
        try {
            readerUncompressed.getNextRecord();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        is = new ByteArrayInputStream(new byte[] {42});

        try {
            readerUncompressed.getNextRecordFrom(null, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(is, -2);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(is, 0L, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(is, -2L, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(null, -1L, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerUncompressed.getNextRecordFrom(null, 0L, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        is.close();
        is = null;

        /*
         * ArcReaderCompressed.
         */

        ArcReaderCompressed readerCompressed;

        readerCompressed = new ArcReaderCompressed();
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

        readerCompressed = new ArcReaderCompressed();
        try {
            readerCompressed.getVersionBlockFrom(null, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        readerCompressed = new ArcReaderCompressed();
        try {
            readerCompressed.getVersionBlock();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        readerCompressed = new ArcReaderCompressed();
        try {
            readerCompressed = new ArcReaderCompressed(null);
        } catch (IllegalArgumentException e) {
            readerCompressed = null;
        }
        Assert.assertNull(readerCompressed);

        readerCompressed = new ArcReaderCompressed();
        try {
            readerCompressed = new ArcReaderCompressed(null, 42);
        } catch (IllegalArgumentException e) {
            readerCompressed = null;
        }
        Assert.assertNull(readerCompressed);

        GzipReader gzipReader = new GzipReader(new ByteArrayInputStream(new byte[] {42}));

        readerCompressed = new ArcReaderCompressed();
        try {
            readerCompressed = new ArcReaderCompressed(gzipReader, -1);
        } catch (IllegalArgumentException e) {
            readerCompressed = null;
        }
        Assert.assertNull(readerCompressed);

        readerCompressed = new ArcReaderCompressed();
        try {
            readerCompressed = new ArcReaderCompressed(gzipReader, 0);
        } catch (IllegalArgumentException e) {
            readerCompressed = null;
        }
        Assert.assertNull(readerCompressed);

        gzipReader.close();
        gzipReader = null;

        readerCompressed = new ArcReaderCompressed();
        try {
            readerCompressed.getNextRecord();
            Assert.fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        is = new ByteArrayInputStream(new byte[] {42});

        try {
            readerCompressed.getNextRecordFrom(null, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(is, -2);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(is, 0L, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(is, -2L, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(null, -1L, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            readerCompressed.getNextRecordFrom(is, 0L, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        is.close();
        is = null;

        /*
         * ArcReaderFactory.
         */

        is = new ByteArrayInputStream(new byte[] {42});

        try {
            ArcReaderFactory.getReader(null, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReader(is, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReader(is, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReader(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReaderUncompressed(null, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReaderUncompressed(is, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReaderUncompressed(is, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReaderUncompressed(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReaderCompressed(null, 42);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReaderCompressed(is, -1);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReaderCompressed(is, 0);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            ArcReaderFactory.getReaderCompressed(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        is.close();
        is = null;
    }

}
