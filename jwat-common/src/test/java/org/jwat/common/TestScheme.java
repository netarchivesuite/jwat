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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestScheme {

    @Test
    public void test_scheme() {
        Scheme scheme = new Scheme();
        Assert.assertNotNull(scheme);

        /*
         * startsWithScheme().
         */

        Assert.assertFalse(Scheme.startsWithScheme(null));

        Assert.assertFalse(Scheme.startsWithScheme("".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme(":".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("#:".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("filedesc".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("http".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("git+".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("git-".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("git.".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("git+ssh".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("git-ssh".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("git.ssh".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("+ssh".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("-ssh".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme(".ssh".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("112:".getBytes()));
        Assert.assertFalse(Scheme.startsWithScheme("filedesc#:".getBytes()));

        Assert.assertTrue(Scheme.startsWithScheme("filedesc:".getBytes()));
        Assert.assertTrue(Scheme.startsWithScheme("http:".getBytes()));
        Assert.assertTrue(Scheme.startsWithScheme("git+:".getBytes()));
        Assert.assertTrue(Scheme.startsWithScheme("git-:".getBytes()));
        Assert.assertTrue(Scheme.startsWithScheme("git.:".getBytes()));
        Assert.assertTrue(Scheme.startsWithScheme("git+ssh:".getBytes()));
        Assert.assertTrue(Scheme.startsWithScheme("git-ssh:".getBytes()));
        Assert.assertTrue(Scheme.startsWithScheme("git.ssh:".getBytes()));

        /*
         * getScheme().
         */

        Assert.assertNull(Scheme.getScheme(null));

        Assert.assertNull(Scheme.getScheme(""));
        Assert.assertNull(Scheme.getScheme(":"));
        Assert.assertNull(Scheme.getScheme("#:"));
        Assert.assertNull(Scheme.getScheme("filedesc"));
        Assert.assertNull(Scheme.getScheme("http"));
        Assert.assertNull(Scheme.getScheme("git+"));
        Assert.assertNull(Scheme.getScheme("git-"));
        Assert.assertNull(Scheme.getScheme("git."));
        Assert.assertNull(Scheme.getScheme("git+ssh"));
        Assert.assertNull(Scheme.getScheme("git-ssh"));
        Assert.assertNull(Scheme.getScheme("git.ssh"));
        Assert.assertNull(Scheme.getScheme("+ssh"));
        Assert.assertNull(Scheme.getScheme("-ssh"));
        Assert.assertNull(Scheme.getScheme(".ssh"));
        Assert.assertNull(Scheme.getScheme("112:"));
        Assert.assertNull(Scheme.getScheme("filedesc#:"));

        Assert.assertEquals("filedesc", Scheme.getScheme("filedesc:"));
        Assert.assertEquals("http", Scheme.getScheme("http:"));
        Assert.assertEquals("git+", Scheme.getScheme("git+:"));
        Assert.assertEquals("git-", Scheme.getScheme("git-:"));
        Assert.assertEquals("git.", Scheme.getScheme("git.:"));
        Assert.assertEquals("git+ssh", Scheme.getScheme("git+ssh:"));
        Assert.assertEquals("git-ssh", Scheme.getScheme("git-ssh:"));
        Assert.assertEquals("git.ssh", Scheme.getScheme("git.ssh:"));

        Assert.assertEquals("filedesc", Scheme.getScheme("filedesc:\u1234"));
        Assert.assertEquals("http", Scheme.getScheme("http:\u1234"));
        Assert.assertEquals("git+", Scheme.getScheme("git+:\u1234"));
        Assert.assertEquals("git-", Scheme.getScheme("git-:\u1234"));
        Assert.assertEquals("git.", Scheme.getScheme("git.:\u1234"));
        Assert.assertEquals("git+ssh", Scheme.getScheme("git+ssh:\u1234"));
        Assert.assertEquals("git-ssh", Scheme.getScheme("git-ssh:\u1234"));
        Assert.assertEquals("git.ssh", Scheme.getScheme("git.ssh:\u1234"));

        Assert.assertNull(Scheme.getScheme("\u1234filedesc:"));
        Assert.assertNull(Scheme.getScheme("http\u1234:"));
        Assert.assertNull(Scheme.getScheme("git\u1234+:"));
        Assert.assertNull(Scheme.getScheme("git\u1234-:"));
        Assert.assertNull(Scheme.getScheme("git\u1234.:"));
        Assert.assertNull(Scheme.getScheme("git+\u1234ssh:"));
        Assert.assertNull(Scheme.getScheme("git-\u1234ssh:"));
        Assert.assertNull(Scheme.getScheme("git.\u1234ssh:"));
    }

}
