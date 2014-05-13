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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.Base16;

@RunWith(JUnit4.class)
public class TestWarc_Params {

    @Test
    public void test_parameters() {

        /*
         * Digest.
         */

        WarcDigest digest;

        digest = WarcDigest.parseWarcDigest(null);
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest("");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest("fail");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest(":");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest("sha1:");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest(":AB2CD3EF4GH5IJ6KL7MN8OPQ");
        Assert.assertNull(digest);

        digest = WarcDigest.parseWarcDigest("SHA1:AB2CD3EF4GH5IJ6KL7MN8OPQ");
        Assert.assertNotNull(digest);
        Assert.assertEquals("sha1", digest.algorithm);
        Assert.assertNull(digest.digestBytes);
        Assert.assertNull(digest.encoding);
        Assert.assertEquals("AB2CD3EF4GH5IJ6KL7MN8OPQ", digest.digestString);
        Assert.assertEquals("sha1:AB2CD3EF4GH5IJ6KL7MN8OPQ", digest.toString());
        Assert.assertEquals("sha1:null:AB2CD3EF4GH5IJ6KL7MN8OPQ", digest.toStringFull());

        byte[] digestBytes = new byte[16];
        for (int i=0; i<digestBytes.length; ++i) {
            digestBytes[i] = (byte)i;
        }
        digest = WarcDigest.createWarcDigest("SHA1", digestBytes, "BASE16", Base16.encodeArray(digestBytes));
        Assert.assertNotNull(digest);
        Assert.assertEquals("sha1", digest.algorithm);
        Assert.assertArrayEquals(digestBytes, digest.digestBytes);
        Assert.assertEquals("base16", digest.encoding);
        Assert.assertEquals(Base16.encodeArray(digestBytes), digest.digestString);
        Assert.assertEquals("sha1:" + Base16.encodeArray(digestBytes), digest.toString());
        Assert.assertEquals("sha1:base16:" + Base16.encodeArray(digestBytes), digest.toStringFull());

        try {
            digest = WarcDigest.createWarcDigest(null, digestBytes, "BASE16", Base16.encodeArray(digestBytes));
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            digest = WarcDigest.createWarcDigest("", digestBytes, "BASE16", Base16.encodeArray(digestBytes));
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            digest = WarcDigest.createWarcDigest("SHA1", null, "BASE16", Base16.encodeArray(digestBytes));
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            digest = WarcDigest.createWarcDigest("SHA1", new byte[0], "BASE16", Base16.encodeArray(digestBytes));
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            digest = WarcDigest.createWarcDigest("SHA1", digestBytes, null, Base16.encodeArray(digestBytes));
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            digest = WarcDigest.createWarcDigest("SHA1", digestBytes, "", Base16.encodeArray(digestBytes));
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            digest = WarcDigest.createWarcDigest("SHA1", digestBytes, "BASE16", null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            digest = WarcDigest.createWarcDigest("SHA1", digestBytes, "BASE16", "");
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}

        /*
         * Date.
         */

        Date warcDate;

        warcDate = WarcDateParser.getDate(null);
        Assert.assertNull(warcDate);

        warcDate = WarcDateParser.getDate("");
        Assert.assertNull(warcDate);

        warcDate = WarcDateParser.getDate("fail");
        Assert.assertNull(warcDate);

        warcDate = WarcDateParser.getDate("YYYY-MM-DDThh:mm:ssZ");
        Assert.assertNull(warcDate);

        warcDate = WarcDateParser.getDate("2011-12-24T19:30:00Z");
        Assert.assertNotNull(warcDate);

        Date date = new Date(0);
        String dateStr = WarcDateParser.getDateFormat().format(date);
        warcDate = WarcDateParser.getDate(dateStr);
        Assert.assertNull(warcDate);
    }

}
