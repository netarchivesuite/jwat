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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestWarcHeaderFieldPolicy extends TestWarcHeaderHelper {

    @Test
    public void test() {
        /*
         * Check fields.
         */

        cases = new Object[][] {
            {new Object[][] {
                    {"WARC-Type", "hello_kitty"}
                    }, new Object[][] {
                        {DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_RECORD_ID + "' header", 1},
                        {DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_DATE + "' header", 1},
                        {DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_CONTENT_LENGTH + "' header", 1}
                    }, new Object[][] {
                        {DiagnosisType.UNKNOWN, "'" + WarcConstants.FN_WARC_TYPE + "' value", 1}
                    }, null
            },
            {new Object[][] {
                    {"WARC-Type", "warcinfo"},
                    {"WARC-Record-ID", "<urn:uuid:0d5d5e9f-2222-4780-b5a4-bbcb3f28431f>"},
                    {"WARC-Date", "2012-05-17T00:14:47Z"},
                    {"Content-Length", "0"},
                    {"Content-Type", "application/warc-fields"},
                    {"WARC-Filename", "converted-IAH-20080430204825-00000-blackbook.warc"}
                    }, null, null, null
            },
            {new Object[][] {
                    {"WARC-Type", "revisit"},
                    {"WARC-Target-URI", "http://state.tn.us/sos/rules/0240/0240-04/0240-04.htm"},
                    {"WARC-Date", "2010-07-07T13:29:44Z"},
                    {"WARC-Payload-Digest", "sha1:TTINQ47F6426Y5KPEZOUYEKE76BBVT56"},
                    {"WARC-IP-Address", "170.143.36.24"},
                    {"WARC-Profile", "http://netpreserve.org/warc/1.0/revisit/identical-payload-digest"},
                    {"WARC-Truncated", "length"},
                    {"WARC-Record-ID", "<urn:uuid:4b09f3c5-6aaa-460c-ba2c-b3c449394a1c>"},
                    {"Content-Type", "application/http; msgtype=response"},
                    {"Content-Length", "206"},
                    }, null, null, null
            },
            {new Object[][] {
                    {"WARC-Type", "revisit"},
                    {"WARC-Target-URI", "http://state.tn.us/sos/rules/0240/0240-04/0240-04.htm"},
                    {"WARC-Date", "2010-07-07T13:29:44Z"},
                    {"WARC-Payload-Digest", "sha1:TTINQ47F6426Y5KPEZOUYEKE76BBVT56"},
                    {"WARC-IP-Address", "170.143.36.24"},
                    {"WARC-Profile", "hello kitty"},
                    {"WARC-Truncated", "length"},
                    {"WARC-Record-ID", "<urn:uuid:4b09f3c5-6aaa-460c-ba2c-b3c449394a1c>"},
                    {"Content-Type", "application/http; msgtype=response"},
                    {"Content-Length", "206"},
                    }, null, new Object[][] {
                        {DiagnosisType.UNKNOWN, "'" + WarcConstants.FN_WARC_PROFILE + "' value", 1}
                    }, null
            },
            {new Object[][] {
                    {"WARC-Type", "warcinfo"},
                    {"WARC-Date", "2008-04-30T20:48:25Z"},
                    {"WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz"},
                    {"WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>"},
                    //{"Content-Type", "application/warc-fields"},
                    {"Content-Length", "483"},
                    }, null, new Object[][] {
                        {DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' header", 0}
                    }, null
            },
            {new Object[][] {
                    {"WARC-Type", "continuation"},
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"WARC-Segment-Number", "42"},
                    {"WARC-Segment-Total-Length", "1234"},
                    {"WARC-Segment-Origin-ID", "<urn:uuid:ff728363-2d5f-4f5f-b832-9552de1a6037>"},
                    {"Content-Length", "693"},
                    {"WARC-Truncated", "length"}
                    }, null, null, null
            },
            {new Object[][] {
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"Content-Length", "693"}
                    }, new Object[][] {
                        {DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_TYPE + "' header", 1}
                    }, new Object[][] {
                        {DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' header", 0}
                    }, null
            },
            {new Object[][] {
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"Content-Length", "693"},
                    {"Content-Type", ""},
                    }, new Object[][] {
                        {DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_TYPE + "' header", 1}
                    }, new Object[][] {
                        {DiagnosisType.EMPTY, "'" + WarcConstants.FN_CONTENT_TYPE + "' field", 0},
                        {DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' header", 0}
                    }, null
            },
            {new Object[][] {
                    {"WARC-Type", "warcinfo"},
                    {"WARC-Record-ID", "<urn:uuid:0d5d5e9f-2222-4780-b5a4-bbcb3f28431f>"},
                    {"WARC-Date", "2012-05-17T00:14:47Z"},
                    {"Content-Length", "0"},
                    {"Content-Type", "hello/warc-fields"},
                    {"WARC-Filename", "converted-IAH-20080430204825-00000-blackbook.warc"}
                    }, null, new Object[][] {
                        {DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' value", 2}
                    }, null
            },
            {new Object[][] {
                    {"WARC-Type", "warcinfo"},
                    {"WARC-Record-ID", "<urn:uuid:0d5d5e9f-2222-4780-b5a4-bbcb3f28431f>"},
                    {"WARC-Date", "2012-05-17T00:14:47Z"},
                    {"Content-Length", "0"},
                    {"Content-Type", "application/kitty"},
                    {"WARC-Filename", "converted-IAH-20080430204825-00000-blackbook.warc"}
                    }, null, new Object[][] {
                        {DiagnosisType.RECOMMENDED, "'" + WarcConstants.FN_CONTENT_TYPE + "' value", 2}
                    }, null
            },
            {new Object[][] {
                    {"WARC-Type", "response"},
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
                    {"WARC-IP-Address", "207.241.229.39"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"Content-Type", "application/http; msgtype=response"},
                    {"Content-Length", "693"}
                    }, null, null, null
            },
            {new Object[][] {
                    {"WARC-Type", "response"},
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
                    {"WARC-IP-Address", "207.241.229.39"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"WARC-Segment-Number", "1"},
                    {"Content-Type", "application/http; msgtype=response"},
                    {"Content-Length", "693"}
                    }, null, null, null
            },
            {new Object[][] {
                    {"WARC-Type", "response"},
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
                    {"WARC-IP-Address", "207.241.229.39"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"WARC-Segment-Number", "2"},
                    {"Content-Type", "application/http; msgtype=response"},
                    {"Content-Length", "693"}
                    }, new Object[][] {
                        {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER + "' value", 2}
                    }, null, null
            },
            {new Object[][] {
                    {"WARC-Type", "continuation"},
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"WARC-Segment-Origin-ID", "<urn:uuid:ff728363-2d5f-4f5f-b832-9552de1a6037>"},
                    {"Content-Length", "693"}
                    }, new Object[][] {
                        {DiagnosisType.REQUIRED_INVALID, "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER + "' value", 1}
                    }, null, null
            },
            {new Object[][] {
                    {"WARC-Type", "continuation"},
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"WARC-Segment-Number", "1"},
                    {"WARC-Segment-Origin-ID", "<urn:uuid:ff728363-2d5f-4f5f-b832-9552de1a6037>"},
                    {"Content-Length", "693"}
                    }, new Object[][] {
                        {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER + "' value", 2}
                    }, null, null
            },
            {new Object[][] {
                    {"WARC-Type", "continuation"},
                    {"WARC-Target-URI", "http://www.archive.org/"},
                    {"WARC-Date", "2008-04-30T20:48:26Z"},
                    {"WARC-Payload-Digest", "sha1:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV"},
                    {"WARC-Record-ID", "<urn:uuid:4042c21b-d898-43f0-9c95-b50da2d1aa42>"},
                    {"WARC-Segment-Number", "2"},
                    {"WARC-Segment-Origin-ID", "<urn:uuid:ff728363-2d5f-4f5f-b832-9552de1a6037>"},
                    {"Content-Length", "693"}
                    }, null, null, null
            },
            {new Object[][] {
                    {"WARC-Type", "metadata"},
                    {"WARC-Date", "2008-04-30T20:48:25Z"},
                    {"WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-1>"},
                    {"Content-Length", "483"},
                    {"Content-Type", "application/warc-fields"},
                    {"WARC-Concurrent-To", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-2>"},
                    {"WARC-Concurrent-To", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-3>"},
                    {"WARC-Concurrent-To", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-4>"}
                    }, null, null, new TestHeaderCallback() {
                        public void callback(WarcHeader header) {
                            Assert.assertEquals(3, header.warcConcurrentToList.size());
                            WarcConcurrentTo concurrentTo;
                            concurrentTo = header.warcConcurrentToList.get(0);
                            Assert.assertEquals("<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-2>", concurrentTo.warcConcurrentToStr);
                            concurrentTo = header.warcConcurrentToList.get(1);
                            Assert.assertEquals("<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-3>", concurrentTo.warcConcurrentToStr);
                            concurrentTo = header.warcConcurrentToList.get(2);
                            Assert.assertEquals("<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c-4>", concurrentTo.warcConcurrentToStr);
                        }
                    }
            }
        };
        test_checkfields_cases(cases);

        /*
         * Check fields policy.
         */

        for (int rtype=1; rtype<WarcConstants.field_policy.length; ++rtype) {
            for (int ftype=1; ftype<WarcConstants.field_policy[rtype].length; ++ftype) {
                header = getTestHeader();
                header.checkFieldPolicy(rtype, ftype, null, null);
                errors = header.diagnostics.getErrors();
                warnings = header.diagnostics.getWarnings();
                switch (WarcConstants.field_policy[rtype][ftype]) {
                case WarcConstants.POLICY_MANDATORY:
                    Assert.assertEquals(1, errors.size());
                    Assert.assertEquals(0, warnings.size());
                    diagnosis = errors.get(0);
                    Assert.assertEquals(DiagnosisType.REQUIRED_INVALID, diagnosis.type);
                    Assert.assertEquals("'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value", diagnosis.entity);
                    Assert.assertEquals(1, diagnosis.information.length);
                    break;
                case WarcConstants.POLICY_SHALL:
                    Assert.assertEquals(1, errors.size());
                    diagnosis = errors.get(0);
                    Assert.assertEquals(DiagnosisType.REQUIRED_INVALID, diagnosis.type);
                    Assert.assertEquals("'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value", diagnosis.entity);
                    Assert.assertEquals(1, diagnosis.information.length);
                    Assert.assertEquals(0, warnings.size());
                    break;
                case WarcConstants.POLICY_SHALL_NOT:
                case WarcConstants.POLICY_MAY_NOT:
                case WarcConstants.POLICY_MAY:
                case WarcConstants.POLICY_IGNORE:
                    Assert.assertEquals(0, errors.size());
                    Assert.assertEquals(0, warnings.size());
                    break;
                }
                header = getTestHeader();
                header.checkFieldPolicy(rtype, ftype, new Object(), null);
                errors = header.diagnostics.getErrors();
                warnings = header.diagnostics.getWarnings();
                switch (WarcConstants.field_policy[rtype][ftype]) {
                case WarcConstants.POLICY_SHALL_NOT:
                    Assert.assertEquals(1, errors.size());
                    Assert.assertEquals(0, warnings.size());
                    diagnosis = errors.get(0);
                    Assert.assertEquals(DiagnosisType.UNDESIRED_DATA, diagnosis.type);
                    Assert.assertEquals("'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value", diagnosis.entity);
                    Assert.assertEquals(1, diagnosis.information.length);
                    break;
                case WarcConstants.POLICY_MAY_NOT:
                    Assert.assertEquals(0, errors.size());
                    Assert.assertEquals(1, warnings.size());
                    diagnosis = warnings.get(0);
                    Assert.assertEquals(DiagnosisType.UNDESIRED_DATA, diagnosis.type);
                    Assert.assertEquals("'" + WarcConstants.FN_IDX_STRINGS[ftype] + "' value", diagnosis.entity);
                    Assert.assertEquals(1, diagnosis.information.length);
                    break;
                case WarcConstants.POLICY_MANDATORY:
                case WarcConstants.POLICY_SHALL:
                case WarcConstants.POLICY_MAY:
                case WarcConstants.POLICY_IGNORE:
                    Assert.assertEquals(0, errors.size());
                    Assert.assertEquals(0, warnings.size());
                    break;
                }
            }
        }
    }

    public void test_checkfields_cases(Object[][] cases) {
        for (int i=0; i<cases.length; ++i) {
            Object[][] headers = (Object[][])cases[i][0];
            Object[][] expectedErrors = (Object[][])cases[i][1];
            Object[][] expectedWarnings = (Object[][])cases[i][2];
            TestHeaderCallback callback = (TestHeaderCallback)cases[i][3];
            header = getTestHeader();
            for (int j=0; j<headers.length; ++j) {
                String fieldName = (String)headers[j][0];
                String fieldValue = (String)headers[j][1];
                headerLine = header.addHeader(fieldName, fieldValue);
                Assert.assertNotNull(headerLine);
                Assert.assertEquals(fieldName, headerLine.name);
                Assert.assertEquals(fieldValue, headerLine.value);
            }
            header.checkFields();
            test_result(expectedErrors, expectedWarnings, callback);
        }
    }

}
