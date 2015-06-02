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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestArcFileWriterConfig {

    @Test
    public void test_arcfilewriterconfig() {
        ArcFileWriterConfig afwc;

        afwc = new ArcFileWriterConfig();
        Assert.assertNotNull(afwc);
        Assert.assertEquals(null, afwc.targetDir);
        Assert.assertEquals(false, afwc.bCompression);
        Assert.assertEquals(new Long(ArcFileWriterConfig.DEFAULT_MAX_FILE_SIZE), new Long(afwc.maxFileSize));
        Assert.assertEquals(false, afwc.bOverwrite);
        Assert.assertNotNull(afwc.metadata);
        Assert.assertEquals(0, afwc.metadata.size());

        afwc = new ArcFileWriterConfig(null, false, ArcFileWriterConfig.DEFAULT_MAX_FILE_SIZE, false);
        Assert.assertNotNull(afwc);
        Assert.assertEquals(null, afwc.targetDir);
        Assert.assertEquals(false, afwc.bCompression);
        Assert.assertEquals(new Long(ArcFileWriterConfig.DEFAULT_MAX_FILE_SIZE), new Long(afwc.maxFileSize));
        Assert.assertEquals(false, afwc.bOverwrite);
        Assert.assertNotNull(afwc.metadata);
        Assert.assertEquals(0, afwc.metadata.size());

        File targetDir = new File("targetDir");

        afwc = new ArcFileWriterConfig(targetDir, true, 42L, true);
        Assert.assertNotNull(afwc);
        Assert.assertEquals(targetDir, afwc.targetDir);
        Assert.assertEquals(true, afwc.bCompression);
        Assert.assertEquals(new Long(42), new Long(afwc.maxFileSize));
        Assert.assertEquals(true, afwc.bOverwrite);
        Assert.assertNotNull(afwc.metadata);
        Assert.assertEquals(0, afwc.metadata.size());
    }

}
