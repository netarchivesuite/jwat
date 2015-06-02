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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWarcFileWriterConfig {

    @Test
    public void test_arcfilewriterconfig() {
        WarcFileWriterConfig wfwc;

        wfwc = new WarcFileWriterConfig();
        Assert.assertNotNull(wfwc);
        Assert.assertEquals(null, wfwc.targetDir);
        Assert.assertEquals(false, wfwc.bCompression);
        Assert.assertEquals(new Long(WarcFileWriterConfig.DEFAULT_MAX_FILE_SIZE), new Long(wfwc.maxFileSize));
        Assert.assertEquals(false, wfwc.bOverwrite);
        Assert.assertNotNull(wfwc.metadata);
        Assert.assertEquals(0, wfwc.metadata.size());

        wfwc = new WarcFileWriterConfig(null, false, WarcFileWriterConfig.DEFAULT_MAX_FILE_SIZE, false);
        Assert.assertNotNull(wfwc);
        Assert.assertEquals(null, wfwc.targetDir);
        Assert.assertEquals(false, wfwc.bCompression);
        Assert.assertEquals(new Long(WarcFileWriterConfig.DEFAULT_MAX_FILE_SIZE), new Long(wfwc.maxFileSize));
        Assert.assertEquals(false, wfwc.bOverwrite);
        Assert.assertNotNull(wfwc.metadata);
        Assert.assertEquals(0, wfwc.metadata.size());

        File targetDir = new File("targetDir");

        wfwc = new WarcFileWriterConfig(targetDir, true, 42L, true);
        Assert.assertNotNull(wfwc);
        Assert.assertEquals(targetDir, wfwc.targetDir);
        Assert.assertEquals(true, wfwc.bCompression);
        Assert.assertEquals(new Long(42), new Long(wfwc.maxFileSize));
        Assert.assertEquals(true, wfwc.bOverwrite);
        Assert.assertNotNull(wfwc.metadata);
        Assert.assertEquals(0, wfwc.metadata.size());
    }

}
