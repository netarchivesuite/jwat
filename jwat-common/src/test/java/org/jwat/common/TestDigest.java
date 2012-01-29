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
