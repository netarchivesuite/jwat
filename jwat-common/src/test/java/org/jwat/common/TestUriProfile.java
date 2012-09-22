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

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestUriProfile {

    @Test
    public void test_uriprofile() {
        UriProfile uriProfile = UriProfile.RFC3986;
        /*
         * indexOf().
         */
        String subDelims  = "!$&'()*+,;=";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<256; ++i) {
            if (subDelims.indexOf(i) == -1) {
                sb.append((char) i);
            }
        }
        String fillStr = sb.toString();
        Assert.assertEquals(256 - subDelims.length(), fillStr.length());
        int pos;
        try {
            pos = uriProfile.indexOf(UriProfile.B_SUB_DELIMS, fillStr, 0);
            Assert.assertEquals(-1, pos);
            pos = uriProfile.indexOf(UriProfile.B_SUB_DELIMS, fillStr + (char)256, 0);
            Assert.fail("Exception expected!");
        } catch (URISyntaxException e) {
        }
        for (int i=0; i<subDelims.length(); ++i) {
            try {
                pos = uriProfile.indexOf(UriProfile.B_SUB_DELIMS, fillStr + subDelims.charAt(i) + (char)256 + fillStr, 0);
                Assert.assertEquals(fillStr.length(), pos);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Assert.fail("Unexpected exception!");
            }
        }
        /*
         * validecode().
         */
    }

}
