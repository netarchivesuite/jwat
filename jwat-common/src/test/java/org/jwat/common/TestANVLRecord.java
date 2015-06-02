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

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestANVLRecord {

    @Test
    public void test_anvlrecord() {
        Object[][] cases = null;
        try {
            cases = new Object[][] {
                    {
                        new String[][] {
                        },
                        "\r\n".getBytes("UTF-8")
                    },
                    {
                        new String[][] {
                                {"entry", ""},
                                {null, "# first draft"},
                                {"who", "   Gilbert, W.S. | Sullivan, Arthur"},
                                {"what", "  The Yeomen of\r\n       the Guard"},
                                {"when/created", "  1888"}
                        },
                        ("entry:\r\n"
                        + "# first draft\r\n"
                        + "who:   Gilbert, W.S. | Sullivan, Arthur\r\n"
                        + "what:  The Yeomen of\r\n"
                        + "       the Guard\r\n"
                        + "when/created:  1888\r\n"
                        + "\r\n").getBytes("UTF-8")
                    },
                    {
                        new String[][] {
                                {"software", "NetarchiveSuite/Version: 5.0.MILESTONE1 status UNSTABLE/https://sbforge.org/display/NAS"},
                                {"ip", "127.0.1.1"},
                                {"hostname", "ubuntu8a"},
                                {"conformsTo", "http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf"},
                                {"isPartOf", "3"}
                        },
                        ("software: NetarchiveSuite/Version: 5.0.MILESTONE1 status UNSTABLE/https://sbforge.org/display/NAS\r\n"
                        + "ip: 127.0.1.1\r\n"
                        + "hostname: ubuntu8a\r\n"
                        + "conformsTo: http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf\r\n"
                        + "isPartOf: 3\r\n"
                        + "\r\n").getBytes("UTF-8")
                    }
            };
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        try {
            String name;
            String value;
            for (int i=0; i<cases.length; ++i) {
                String[][] nvs = (String[][])cases[i][0];
                byte[] expected = (byte[])cases[i][1];
                ANVLRecord anvlRecord = new ANVLRecord();
                for (int j=0; j<nvs.length; ++j) {
                    name = nvs[j][0];
                    value = nvs[j][1];
                    if (name != null) {
                        anvlRecord.addLabelValue(name, value);
                    } else {
                        anvlRecord.addValue(value);
                    }
                }
                byte[] bytes = anvlRecord.getBytes("UTF-8");
                // debug
                //System.out.println(i);
                //System.out.println(new String(expected));
                //System.out.println(new String(bytes));
                Assert.assertArrayEquals(expected, bytes);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
