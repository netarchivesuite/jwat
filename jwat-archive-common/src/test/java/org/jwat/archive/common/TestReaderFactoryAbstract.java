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
package org.jwat.archive.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestReaderFactoryAbstract {

    /** GZip header magic number. */
    public static final byte[] GZIP_MAGIC_HEADER = new byte[] {(byte)0x1f, (byte)0x8b};

    @Test
    public void test_readerfactoryabstract_isgzip() {
        Assert.assertNotNull(new ReaderFactoryAbstract());
        byte[] bytes;
        ByteCountingPushBackInputStream pbin;
        try {
            ReaderFactoryAbstract.isGzipped(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
        try {
            bytes = GZIP_MAGIC_HEADER;
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(ReaderFactoryAbstract.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x1f, (byte)0x8b, (byte)0x2f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(ReaderFactoryAbstract.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x8b, (byte)0x1f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(ReaderFactoryAbstract.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x1f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(ReaderFactoryAbstract.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x8b};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(ReaderFactoryAbstract.isGzipped(pbin));
            pbin.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    /** Buffer size used by <code>PushbackInputStream</code>. */
    public static final int PUSHBACK_BUFFER_SIZE = 32;

    @Test
    public void test_readerfactoryabstract_isarc() {
        Assert.assertNotNull(new ReaderFactoryAbstract());
        byte[] bytes;
        ByteCountingPushBackInputStream pbin;
        try {
            /*
             * isArcFile().
             */
            bytes = ReaderFactoryAbstract.ARC_MAGIC_HEADER.getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isArcFile(pbin));
            pbin.close();

            bytes = "filedesc://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isArcFile(pbin));
            pbin.close();

            bytes = ReaderFactoryAbstract.ARC_MAGIC_HEADER.toUpperCase().getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isArcFile(pbin));
            pbin.close();

            bytes = "FILEDESC://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isArcFile(pbin));
            pbin.close();

            bytes = "filedesc".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isArcFile(pbin));
            pbin.close();
            /*
             * isArcRecord().
             */
            bytes = ReaderFactoryAbstract.ARC_MAGIC_HEADER.getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isArcRecord(pbin));
            pbin.close();

            bytes = "filedesc://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isArcRecord(pbin));
            pbin.close();

            bytes = ReaderFactoryAbstract.ARC_MAGIC_HEADER.toUpperCase().getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isArcRecord(pbin));
            pbin.close();

            bytes = "FILEDESC://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isArcRecord(pbin));
            pbin.close();

            bytes = "filedesc".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isArcRecord(pbin));
            pbin.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
        try {
            /*
             * isArcFile().
             */
            bytes = "http://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isArcFile(pbin));
            pbin.close();

            bytes = "HTTPS://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isArcFile(pbin));
            pbin.close();

            bytes = "http".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isArcFile(pbin));
            pbin.close();
            /*
             * isArcRecord().
             */
            bytes = "http://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isArcRecord(pbin));
            pbin.close();

            bytes = "HTTPS://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isArcRecord(pbin));
            pbin.close();

            bytes = "http".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isArcRecord(pbin));
            pbin.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    @Test
    public void test_readerfactoryabstract_iswarc() {
        Assert.assertNotNull(new ReaderFactoryAbstract());
        byte[] bytes;
        ByteCountingPushBackInputStream pbin;
        try {
            /*
             * isWarcFile.
             */
            bytes = ReaderFactoryAbstract.WARC_MAGIC_HEADER.getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isWarcFile(pbin));
            pbin.close();

            bytes = "WARC/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isWarcFile(pbin));
            pbin.close();

            bytes = ReaderFactoryAbstract.WARC_MAGIC_HEADER.toLowerCase().getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isWarcFile(pbin));
            pbin.close();

            bytes = "warc/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isWarcFile(pbin));
            pbin.close();

            bytes = "WARC".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isWarcFile(pbin));
            pbin.close();
            /*
             * isWarcRecord.
             */
            bytes = ReaderFactoryAbstract.WARC_MAGIC_HEADER.getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isWarcRecord(pbin));
            pbin.close();

            bytes = "WARC/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(ReaderFactoryAbstract.isWarcRecord(pbin));
            pbin.close();

            bytes = ReaderFactoryAbstract.WARC_MAGIC_HEADER.toLowerCase().getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isWarcRecord(pbin));
            pbin.close();

            bytes = "warc/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isWarcRecord(pbin));
            pbin.close();

            bytes = "WARC".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(ReaderFactoryAbstract.isWarcRecord(pbin));
            pbin.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

}
