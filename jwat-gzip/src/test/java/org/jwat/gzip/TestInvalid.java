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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestInvalid {

    @Test
    public void test() throws IOException {
        InputStream in;
        GzipInputStream gzin;
        GzipEntry entry;

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-compression.gz");
        gzin = new GzipInputStream(in);
        try {
            while ((entry = gzin.getNextEntry()) != null) {
                gzin.closeEntry();
            }
            gzin.close();
            Assert.fail("Exception expected!");
        }
        catch (ZipException e) {
        }
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-entries.gz");
        gzin = new GzipInputStream(in);
        while ((entry = gzin.getNextEntry()) != null) {
            gzin.closeEntry();
        }
        gzin.close();
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-magic.gz");
        gzin = new GzipInputStream(in);
        try {
            while ((entry = gzin.getNextEntry()) != null) {
                gzin.closeEntry();
            }
            gzin.close();
            Assert.fail("Exception expected!");
        }
        catch (ZipException e) {
        }
        in.close();
    }

}
