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
public class TestWarcHeader extends TestWarcHeaderHelper {

    @Test
    public void test_warcheader_addheader() {
        headers = new String[] {
                WarcConstants.FN_WARC_TYPE,
                WarcConstants.FN_WARC_RECORD_ID,
                WarcConstants.FN_WARC_DATE,
                WarcConstants.FN_CONTENT_LENGTH,
                WarcConstants.FN_CONTENT_TYPE,
                WarcConstants.FN_WARC_CONCURRENT_TO,
                WarcConstants.FN_WARC_BLOCK_DIGEST,
                WarcConstants.FN_WARC_PAYLOAD_DIGEST,
                WarcConstants.FN_WARC_IP_ADDRESS,
                WarcConstants.FN_WARC_REFERS_TO,
                WarcConstants.FN_WARC_TARGET_URI,
                WarcConstants.FN_WARC_TRUNCATED,
                WarcConstants.FN_WARC_WARCINFO_ID,
                WarcConstants.FN_WARC_FILENAME,
                WarcConstants.FN_WARC_PROFILE,
                WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE,
                WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID,
                WarcConstants.FN_WARC_SEGMENT_NUMBER,
                WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH
        };
        testEmptyHeader(headers, null);
        testEmptyHeader(headers, "");

        String fieldName;
        String fieldValue;

        fieldName = "WARC-Record-ID";
        fieldValue = "<http://hello_kitty$>";
        testHeader(fieldName, fieldValue, 0, 0);
        /*
        "WARC-Concurrent-To",
        "WARC-Refers-To",
        "WARC-Target-URI",
        "WARC-Warcinfo-ID",
        "WARC-Segment-Origin-ID",
         */

        /*
         * Add header.
         */

        cases = new Object[][] {
                /*
                 * String
                 */
                {WarcConstants.FN_WARC_TYPE, new Object[][] {
                        {"hello_kitty", null, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(new Integer(WarcConstants.RT_IDX_UNKNOWN), header.warcTypeIdx);
                            }
                        }}
                }},
                {WarcConstants.FN_WARC_TRUNCATED, new Object[][] {
                        {"hello_kitty", null, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(new Integer(WarcConstants.TT_IDX_FUTURE_REASON), header.warcTruncatedIdx);
                            }
                        }}
                }},
                {WarcConstants.FN_WARC_FILENAME, new Object[][] {
                        {"hello_kitty", null, null, null}
                }},
                /*
                 * Integer.
                 */
                {WarcConstants.FN_WARC_SEGMENT_NUMBER, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_SEGMENT_NUMBER +"' value", 2}
                        }, null, null}
                }},
                /*
                 * Longs.
                 */
                {WarcConstants.FN_CONTENT_LENGTH, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_CONTENT_LENGTH +"' value", 2}
                        }, null, null}
                }},
                {WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH + "' value", 2}
                        }, null, null}
                }},
                /*
                 * Content-Type.
                 */
                {WarcConstants.FN_CONTENT_TYPE, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_CONTENT_TYPE + "' value", 2}

                        }, null, null}
                }},
                {WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE + "' value", 2}
                        }, null, null}
                }},
                /*
                 * IP-Address.
                 */
                {WarcConstants.FN_WARC_IP_ADDRESS, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_IP_ADDRESS + "' value", 2}
                        }, null, null}
                }},
                /*
                 * Date.
                 */
                {WarcConstants.FN_WARC_DATE, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_DATE + "' value", 2}
                        }, null, null}
                }},
                /*
                 * Digest.
                 */
                {WarcConstants.FN_WARC_BLOCK_DIGEST, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_BLOCK_DIGEST + "' value", 2}
                        }, null, null}
                }},
                {WarcConstants.FN_WARC_PAYLOAD_DIGEST, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_PAYLOAD_DIGEST + "' value", 2}
                        }, null, null}
                }},
                /*
                 * Uri.
                 */
                {WarcConstants.FN_WARC_RECORD_ID, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_RECORD_ID + "' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_RECORD_ID + "' value", 2}
                        }, null, null}
                }},
                {WarcConstants.FN_WARC_CONCURRENT_TO, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_CONCURRENT_TO + "' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_CONCURRENT_TO + "' value", 2}
                        }, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(1, header.warcConcurrentToList.size());
                            }
                        }},
                        {"hello_kitty2", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_CONCURRENT_TO + "' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_CONCURRENT_TO + "' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_CONCURRENT_TO + "' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_CONCURRENT_TO + "' value", 2}
                        }, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(2, header.warcConcurrentToList.size());
                            }
                        }}
                }},
                {WarcConstants.FN_WARC_REFERS_TO, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_REFERS_TO + "' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_REFERS_TO + "' value", 2}
                        }, null, null}
                }},
                {WarcConstants.FN_WARC_TARGET_URI, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_TARGET_URI + "' value", 2}
                        }, null, null}
                }},
                {WarcConstants.FN_WARC_WARCINFO_ID, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_WARCINFO_ID + "' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_WARCINFO_ID + "' value", 2}
                        }, null, null}
                }},

                {WarcConstants.FN_WARC_PROFILE, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_PROFILE + "' value", 2}
                        }, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(new Integer(WarcConstants.PROFILE_IDX_UNKNOWN), header.warcProfileIdx);
                            }
                        }}
                }},
                {WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID, new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID + "' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'" + WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID + "' value", 2}
                        }, null, null}
                }},
        };
        test_headeradd_cases(cases);

        /*
         * Duplicate headers.
         */

        header = getTestHeader();
        headerLine = header.addHeader("WARC-Type", "warcinfo");
        Assert.assertNotNull(headerLine);
        Assert.assertEquals("WARC-Type", headerLine.name);
        Assert.assertEquals("warcinfo", headerLine.value);
        errors = header.diagnostics.getErrors();
        warnings = header.diagnostics.getWarnings();
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(0, warnings.size());
        headerLine = header.addHeader("WARC-Type", "warcinfo");
        Assert.assertNotNull(headerLine);
        Assert.assertEquals("WARC-Type", headerLine.name);
        Assert.assertEquals("warcinfo", headerLine.value);
        errors = header.diagnostics.getErrors();
        warnings = header.diagnostics.getWarnings();
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(0, warnings.size());
        diagnosis = errors.get(0);
        Assert.assertEquals(DiagnosisType.DUPLICATE, diagnosis.type);
        Assert.assertEquals("'WARC-Type' header", diagnosis.entity);
        Assert.assertEquals(1, diagnosis.information.length);

        /*
         * Non warc headers.
         */

        header = getTestHeader();
        headerLine = header.addHeader("X-Header1", "Hello");
        Assert.assertNotNull(headerLine);
        Assert.assertEquals("X-Header1", headerLine.name);
        Assert.assertEquals("Hello", headerLine.value);
        headerLine = header.addHeader("X-Header2", "Kitty");
        Assert.assertNotNull(headerLine);
        Assert.assertEquals("X-Header2", headerLine.name);
        Assert.assertEquals("Kitty", headerLine.value);
    }

    public void test_headeradd_cases(Object[][] cases) {
        for (int i=0; i<cases.length; ++i) {
            String fieldName = (String)cases[i][0];
            Object[][] values = (Object[][])cases[i][1];
            header = getTestHeader();
            for (int j=0; j<values.length; ++j) {
                String fieldValue = (String)values[j][0];
                Object[][] expectedErrors = (Object[][])values[j][1];
                Object[][] expectedWarnings = (Object[][])values[j][2];
                TestHeaderCallback callback = (TestHeaderCallback)values[j][3];
                headerLine = header.addHeader(fieldName, fieldValue);
                // debug
                //System.out.println(fieldName + ": " + fieldValue);
                Assert.assertNotNull(headerLine);
                Assert.assertEquals(fieldName, headerLine.name);
                Assert.assertEquals(fieldValue, headerLine.value);
                test_result(expectedErrors, expectedWarnings, callback);
            }
        }
    }

    public WarcHeader testHeader(String fieldName, String fieldValue, int expectedErrors, int expectedWarnings) {
        header = getTestHeader();
        headerLine = header.addHeader(fieldName, fieldValue);
        Assert.assertNotNull(headerLine);
        Assert.assertEquals(fieldName, headerLine.name);
        Assert.assertEquals(fieldValue, headerLine.value);
        errors = header.diagnostics.getErrors();
        warnings = header.diagnostics.getWarnings();
        Assert.assertEquals(expectedErrors, errors.size());
        Assert.assertEquals(expectedWarnings, warnings.size());
        return header;
    }

    public void testEmptyHeader(String[] headers, String fieldValue) {
        for (int i=0; i<headers.length; ++i) {
            // debug
            //System.out.println(headers[i]);
            header = getTestHeader();
            headerLine = header.addHeader(headers[i], fieldValue);
            Assert.assertNotNull(headerLine);
            Assert.assertEquals(headers[i], headerLine.name);
            Assert.assertEquals(fieldValue, headerLine.value);
            errors = header.diagnostics.getErrors();
            warnings = header.diagnostics.getWarnings();
            Assert.assertEquals(0, errors.size());
            Assert.assertEquals(1, warnings.size());
            diagnosis = warnings.get(0);
            Assert.assertEquals(DiagnosisType.EMPTY, diagnosis.type);
            Assert.assertEquals("'" + headers[i] + "' field", diagnosis.entity);
            Assert.assertEquals(0, diagnosis.information.length);
        }
    }

}
