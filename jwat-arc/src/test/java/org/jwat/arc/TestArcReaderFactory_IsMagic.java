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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestArcReaderFactory_IsMagic {

    @Test
    public void test_magicbytes() {
        byte[] bytes;
        ByteCountingPushBackInputStream pbin;
        try {
            bytes = ArcConstants.ARC_MAGIC_HEADER.getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(ArcReaderFactory.isArcFile(pbin));
            pbin.close();

            bytes = "filedesc://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(ArcReaderFactory.isArcFile(pbin));
            pbin.close();

            bytes = ArcConstants.ARC_MAGIC_HEADER.toUpperCase().getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(ArcReaderFactory.isArcFile(pbin));
            pbin.close();

            bytes = "FILEDESC://url".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(ArcReaderFactory.isArcFile(pbin));
            pbin.close();

            bytes = "filedesc".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(ArcReaderFactory.isArcFile(pbin));
            pbin.close();

        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

}
