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
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestWarcReaderFactory_IsWarc {

    @Test
    public void test_iswarc() {
        byte[] bytes;
        ByteCountingPushBackInputStream pbin;
        try {
            /*
             * isWarcFile.
             */
            bytes = WarcConstants.WARC_MAGIC_HEADER.getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

            bytes = "WARC/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

            bytes = WarcConstants.WARC_MAGIC_HEADER.toLowerCase().getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

            bytes = "warc/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

            bytes = "WARC".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();
            /*
             * isWarcRecord.
             */
            bytes = WarcConstants.WARC_MAGIC_HEADER.getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(WarcReaderFactory.isWarcRecord(pbin));
            pbin.close();

            bytes = "WARC/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertTrue(WarcReaderFactory.isWarcRecord(pbin));
            pbin.close();

            bytes = WarcConstants.WARC_MAGIC_HEADER.toLowerCase().getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(WarcReaderFactory.isWarcRecord(pbin));
            pbin.close();

            bytes = "warc/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(WarcReaderFactory.isWarcRecord(pbin));
            pbin.close();

            bytes = "WARC".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), WarcReaderFactory.PUSHBACK_BUFFER_SIZE);
            Assert.assertFalse(WarcReaderFactory.isWarcRecord(pbin));
            pbin.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

}
