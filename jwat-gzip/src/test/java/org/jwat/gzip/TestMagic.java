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
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestMagic {

    @Test
    public void test_magicbytes_old() {
        byte[] bytes;
        ByteCountingPushBackInputStream pbin;
        try {
            bytes = GzipConstants.GZIP_MAGIC_HEADER;
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(GzipInputStream.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x1f, (byte)0x8b, (byte)0x2f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(GzipInputStream.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x8b, (byte)0x1f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipInputStream.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x1f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipInputStream.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x8b};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipInputStream.isGzipped(pbin));
            pbin.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

    @Test
    public void test_magicbytes_new() {
        byte[] bytes;
        ByteCountingPushBackInputStream pbin;
        try {
            bytes = GzipConstants.GZIP_MAGIC_HEADER;
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(GzipReader.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x1f, (byte)0x8b, (byte)0x2f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(GzipReader.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x8b, (byte)0x1f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipReader.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x1f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipReader.isGzipped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x8b};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipReader.isGzipped(pbin));
            pbin.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }


}
