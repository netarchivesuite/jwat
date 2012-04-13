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
import java.net.InetAddress;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.IPAddressParser;

/**
 * Test IPAddressParser class with various legal and illegal parameters.
 *
 * @author nicl
 */
@RunWith(JUnit4.class)
public class TestParams {

    @Test
    public void test_parameters() throws IOException {
        /*
         * IpAddressParser.
         */

        InetAddress ia;

        IPAddressParser iap = new IPAddressParser();
        Assert.assertNotNull(iap);

        ia = IPAddressParser.getAddress(null);
        Assert.assertNull(ia);

        ia = IPAddressParser.getAddress("fail");
        Assert.assertNull(ia);

        ia = IPAddressParser.getAddress("0.0.0");
        Assert.assertNull(ia);

        ia = IPAddressParser.getAddress("0.0.0.0.0");
        Assert.assertNull(ia);

        ia = IPAddressParser.getAddress("a.b.c.d");
        Assert.assertNull(ia);

        ia = IPAddressParser.getAddress("192.168.1.1");
        Assert.assertNotNull(ia);

        ia = IPAddressParser.getAddress("dead::beef:cafe:f800:0000");
        Assert.assertNotNull(ia);
    }

}
