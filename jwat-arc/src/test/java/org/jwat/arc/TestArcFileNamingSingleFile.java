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
public class TestArcFileNamingSingleFile {

    @Test
    public void test_arcfilenaming_singlefile() {
        ArcFileNamingSingleFile afn;

        try {
            afn = new ArcFileNamingSingleFile((String)null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            afn = new ArcFileNamingSingleFile((File)null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        afn = new ArcFileNamingSingleFile("filename");
        Assert.assertEquals("filename", afn.getFilename(42, false));
        Assert.assertEquals(false, afn.supportMultipleFiles());

        afn = new ArcFileNamingSingleFile(new File("file"));
        Assert.assertEquals("file", afn.getFilename(42, false));
        Assert.assertEquals(false, afn.supportMultipleFiles());
    }

}
