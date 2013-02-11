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
    }

}
