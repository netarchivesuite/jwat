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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWarcConcurrentTo {

    @Test
    public void test_warc_concurrentto_equals_hashcode() {
        WarcConcurrentTo ct1;
        WarcConcurrentTo ct2;
        String str = "42";

        /*
         * Nulls.
         */

        ct1 = new WarcConcurrentTo();
        Assert.assertNull(ct1.warcConcurrentToStr);
        Assert.assertNull(ct1.warcConcurrentToUri);
        Assert.assertEquals(0, ct1.hashCode());

        ct2 = new WarcConcurrentTo();
        Assert.assertNull(ct2.warcConcurrentToStr);
        Assert.assertNull(ct2.warcConcurrentToUri);
        Assert.assertEquals(0, ct2.hashCode());

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        Assert.assertFalse(ct1.equals(null));
        Assert.assertFalse(ct2.equals(null));
        Assert.assertFalse(ct1.equals(str));
        Assert.assertFalse(ct2.equals(str));

        /*
         * Reference String.
         */

        ct1.warcConcurrentToStr = "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88";
        ct1.warcConcurrentToUri = null;

        ct2.warcConcurrentToStr = null;
        ct2.warcConcurrentToUri = URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.warcConcurrentToStr = "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88";
        ct2.warcConcurrentToUri = URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.warcConcurrentToStr = "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88";
        ct2.warcConcurrentToUri = null;

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        ct2.warcConcurrentToStr = "urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1";
        ct2.warcConcurrentToUri = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        /*
         * Reference Object.
         */

        ct1.warcConcurrentToStr = null;
        ct1.warcConcurrentToUri = URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88");

        ct2.warcConcurrentToStr = "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88";
        ct2.warcConcurrentToUri = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.warcConcurrentToStr = "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88";
        ct2.warcConcurrentToUri = URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.warcConcurrentToStr = null;
        ct2.warcConcurrentToUri = URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88");

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        ct2.warcConcurrentToStr = null;
        ct2.warcConcurrentToUri = URI.create("urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        /*
         * Reference String and Object.
         */

        ct1.warcConcurrentToStr = "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88";
        ct1.warcConcurrentToUri = URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88");

        ct2.warcConcurrentToStr = "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88";
        ct2.warcConcurrentToUri = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.warcConcurrentToStr = null;
        ct2.warcConcurrentToUri = URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.warcConcurrentToStr = "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88";
        ct2.warcConcurrentToUri = URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88");

        Assert.assertEquals(ct1, ct2);
        Assert.assertEquals(ct1.hashCode(), ct2.hashCode());

        ct2.warcConcurrentToStr = null;
        ct2.warcConcurrentToUri = URI.create("urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.warcConcurrentToStr = "urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1";
        ct2.warcConcurrentToUri = null;

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));

        ct2.warcConcurrentToStr = "urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1";
        ct2.warcConcurrentToUri = URI.create("urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1");

        Assert.assertFalse(ct1.equals(ct2));
        Assert.assertThat(ct1.hashCode(), is(not(equalTo(ct2.hashCode()))));
    }

}
