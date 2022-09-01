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

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestUriProfile {

    @Test
    public void test_uriprofile() {
        UriProfile uriProfile;
        UriProfile uriProfile2;
        /*
         * new UriProfile(uriProfile)
         */
        uriProfile = UriProfile.RFC3986;
        uriProfile2 = new UriProfile(uriProfile);
        Assert.assertNotSame(uriProfile, uriProfile2);
        Assert.assertEquals(uriProfile.bAllowRelativeUris, uriProfile2.bAllowRelativeUris);
        Assert.assertEquals(uriProfile.bAllowInvalidPercentEncoding, uriProfile2.bAllowInvalidPercentEncoding);
        Assert.assertEquals(uriProfile.bAllow16bitPercentEncoding, uriProfile2.bAllow16bitPercentEncoding);
        Assert.assertArrayEquals(uriProfile.charTypeMap, uriProfile2.charTypeMap);

        uriProfile = UriProfile.RFC3986_ABS_16BIT_LAX;
        uriProfile2 = new UriProfile(uriProfile);
        Assert.assertNotSame(uriProfile, uriProfile2);
        Assert.assertEquals(uriProfile.bAllowRelativeUris, uriProfile2.bAllowRelativeUris);
        Assert.assertEquals(uriProfile.bAllowInvalidPercentEncoding, uriProfile2.bAllowInvalidPercentEncoding);
        Assert.assertEquals(uriProfile.bAllow16bitPercentEncoding, uriProfile2.bAllow16bitPercentEncoding);
        Assert.assertArrayEquals(uriProfile.charTypeMap, uriProfile2.charTypeMap);

        uriProfile = UriProfile.RFC3986;
        uriProfile2 = new UriProfile(UriProfile.RFC3986_ABS_16BIT_LAX);
        Assert.assertNotSame(uriProfile, uriProfile2);
        Assert.assertNotSame(uriProfile.bAllowRelativeUris, uriProfile2.bAllowRelativeUris);
        Assert.assertNotSame(uriProfile.bAllowInvalidPercentEncoding, uriProfile2.bAllowInvalidPercentEncoding);
        Assert.assertNotSame(uriProfile.bAllow16bitPercentEncoding, uriProfile2.bAllow16bitPercentEncoding);
        Assert.assertThat(uriProfile, is(not(equalTo(uriProfile2))));
        /*
         * indexOf().
         */
        uriProfile = UriProfile.RFC3986;
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
         * charTypeAddAndOr().
         */
        sb = new StringBuilder();
        for (int i=0; i<256; ++i) {
            sb.append((char) i);
        }
        String chars = sb.toString();
        uriProfile = new UriProfile();
        uriProfile.charTypeAddAndOr(chars, 0, 1 << 31);
        for (int i=0; i<256; ++i) {
            Assert.assertEquals(1 << 31, uriProfile.charTypeMap[i] ^ UriProfile.RFC3986.charTypeMap[i]);
        }
        /*
         * validate_first_follow
         */
        String[] valid_ff = {
                "",
                "aa",
                "a1"
        };
        uriProfile = UriProfile.RFC3986;
        for (int i=0; i<valid_ff.length; ++i) {
            try {
                uriProfile.validate_first_follow(valid_ff[i], UriProfile.B_ALPHAS, UriProfile.B_DIGITS);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Assert.fail("Unexpected exception!");
            }
        }
        String[] invalid_ff = {
                "1a",
                "11",
                "aé"
        };
        for (int i=0; i<invalid_ff.length; ++i) {
            try {
                uriProfile.validate_first_follow(invalid_ff[i], UriProfile.B_ALPHAS, UriProfile.B_DIGITS);
                Assert.fail("Exception expected!");
            } catch (URISyntaxException e) {
            }
        }
        /*
         * validate_decode().
         */
        String[][] valid_cases = {
                {"", ""},
                {"test", "test"},
                {"%0a%2e", "\n."},
                {"%0A%2E", "\n."}
        };
        uriProfile = UriProfile.RFC3986;
        Assert.assertFalse(uriProfile.bAllow16bitPercentEncoding);
        Assert.assertTrue(uriProfile.bAllowRelativeUris);
        String str;
        String expectedDecodedStr;
        String decodedStr;
        for (int i=0; i<valid_cases.length; ++i) {
            str = valid_cases[i][0];
            expectedDecodedStr = valid_cases[i][1];
            // debug
            //System.out.println("+" + str);
            try {
                decodedStr = uriProfile.validate_decode(UriProfile.B_ALPHAS, "unit-test", str);
                Assert.assertEquals(expectedDecodedStr, decodedStr);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Assert.fail("Unexpected exception!");
            }
        }
        String[] invalid_cases = {
                "\u1234",
                "42",
                "%",
                "%a",
                "%h",
                "%hh",
                "%1\u1234",
                "%u",
                "%ua",
                "%uaa",
                "%uaaa",
                "%u1234",
                "%U",
                "%Ua",
                "%Uaa",
                "%Uaaa",
                "%U1234"
        };
        for (int i=0; i<invalid_cases.length; ++i) {
            str = invalid_cases[i];
            // debug
            //System.out.println("-" + str);
            try {
                uriProfile.validate_decode(UriProfile.B_ALPHAS, "unit-test", str);
                Assert.fail("Exception expected!");
            } catch (URISyntaxException e) {
            }
        }

        try {
            uriProfile.validate_decode(UriProfile.B_ALPHAS, "unit-test", "\n");
            Assert.fail("Exception expected!");
        } catch (URISyntaxException e) {
            Assert.assertEquals(e.getMessage(), "Invalid URI unit-test component - invalid character '0x0a': \n");
        }
        try {
            uriProfile.validate_decode(UriProfile.B_ALPHAS, "unit-test", " ");
            Assert.fail("Exception expected!");
        } catch (URISyntaxException e) {
            Assert.assertEquals(e.getMessage(), "Invalid URI unit-test component - invalid character ' ':  ");
        }
        try {
            uriProfile.validate_decode(UriProfile.B_ALPHAS, "unit-test", "\u0019");
            Assert.fail("Exception expected!");
        } catch (URISyntaxException e) {
            Assert.assertEquals(e.getMessage(), "Invalid URI unit-test component - invalid character '0x19': \u0019");
        }

        valid_cases = new String[][] {
                {"", ""},
                {"test", "test"},
                {"%0a%2e", "\n."},
                {"%0A%2E", "\n."},
                {"%u1234", "\u1234"},
                {"%uabcd", "\uabcd"},
                {"%uABCD", "\uABCD"},
                {"%U1234", "\u1234"},
                {"%Uabcd", "\uabcd"},
                {"%UABCD", "\uABCD"}
        };
        uriProfile = new UriProfile();
        uriProfile.bAllow16bitPercentEncoding = true;
        for (int i=0; i<valid_cases.length; ++i) {
            str = valid_cases[i][0];
            expectedDecodedStr = valid_cases[i][1];
            // debug
            //System.out.println("+" + str);
            try {
                decodedStr = uriProfile.validate_decode(UriProfile.B_ALPHAS, "unit-test", str);
                Assert.assertEquals(expectedDecodedStr, decodedStr);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Assert.fail("Unexpected exception!");
            }
        }
    }

}
