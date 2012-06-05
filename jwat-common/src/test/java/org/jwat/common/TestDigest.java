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
package org.jwat.common;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestDigest {

    @Test
    public void test_digest_wrapper() throws IOException {
        Assert.assertEquals(16, Digest.digestAlgorithmLength("MD5"));
        Assert.assertEquals(20, Digest.digestAlgorithmLength("SHA1"));
        Assert.assertEquals(-1, Digest.digestAlgorithmLength("SHAFT1"));
        try {
            Digest.digestAlgorithmLength(null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            Digest.digestAlgorithmLength("");
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}

        Digest digest = new Digest();
        Assert.assertNull(digest.algorithm);
        Assert.assertNull(digest.digestBytes);
        Assert.assertNull(digest.digestString);
        Assert.assertNull(digest.encoding);

        digest.algorithm = "sha1";
        digest.digestBytes = new byte[] {1, 2, 3, 4};
        digest.digestString = "test";
        digest.encoding = "base32";

        Assert.assertEquals("sha1", digest.algorithm);
        Assert.assertArrayEquals(new byte[] {1, 2, 3, 4}, digest.digestBytes);
        Assert.assertEquals("test", digest.digestString);
        Assert.assertEquals("base32", digest.encoding);

        digest.algorithm = null;
        digest.digestBytes = null;
        digest.digestString = null;
        digest.encoding = null;

        Assert.assertNull(digest.algorithm);
        Assert.assertNull(digest.digestBytes);
        Assert.assertNull(digest.digestString);
        Assert.assertNull(digest.encoding);
    }

}
