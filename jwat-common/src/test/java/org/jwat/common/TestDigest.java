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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

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

    @Test
    public void test_digest_equals_hashcode() {
        Digest d1;
        Digest d2;
        String str = "42";

        /*
         * Nulls.
         */

        d1 = new Digest();
        d1.algorithm = null;
        d1.digestBytes = null;
        d1.digestString = null;
        d1.encoding = null;

        d2 = new Digest();
        d2.algorithm = null;
        d2.digestBytes = null;
        d2.digestString = null;
        d2.encoding = null;

        Assert.assertEquals(d1, d2);
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        Assert.assertFalse(d1.equals(null));
        Assert.assertFalse(d2.equals(null));
        Assert.assertFalse(d1.equals(str));
        Assert.assertFalse(d2.equals(str));

        /*
         * Source null - Obj cycling.
         */

        d2.algorithm = "rsa";
        d2.digestBytes = null;
        d2.digestString = null;
        d2.encoding = null;

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d2.algorithm = null;
        d2.digestBytes = "1234567890".getBytes();
        d2.digestString = null;
        d2.encoding = null;

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d2.algorithm = null;
        d2.digestBytes = null;
        d2.digestString = "1234567890";
        d2.encoding = null;

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d2.algorithm = null;
        d2.digestBytes = null;
        d2.digestString = null;
        d2.encoding = "base32";

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        /*
         * Source obj - Obj cycling
         */

        d1.algorithm = "rsa";
        d1.digestBytes = "1234567890".getBytes();
        d1.digestString = "1234567890";
        d1.encoding = "base32";

        d2.algorithm = "rsa";
        d2.digestBytes = "1234567890".getBytes();
        d2.digestString = "1234567890";
        d2.encoding = "base32";

        Assert.assertEquals(d1, d2);
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d2.algorithm = "dsa";
        d2.digestBytes = "1234567890".getBytes();
        d2.digestString = "1234567890";
        d2.encoding = "base32";

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d2.algorithm = "rsa";
        d2.digestBytes = "0987654321".getBytes();
        d2.digestString = "1234567890";
        d2.encoding = "base32";

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d2.algorithm = "rsa";
        d2.digestBytes = "1234567890".getBytes();
        d2.digestString = "0987654321";
        d2.encoding = "base32";

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d2.algorithm = "rsa";
        d2.digestBytes = "1234567890".getBytes();
        d2.digestString = "1234567890";
        d2.encoding = "base64";

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));
    }

}
