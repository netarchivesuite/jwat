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
                "WARC-Type",
                "WARC-Record-ID",
                "WARC-Date",
                "Content-Length",
                "Content-Type",
                "WARC-Concurrent-To",
                "WARC-Block-Digest",
                "WARC-Payload-Digest",
                "WARC-IP-Address",
                "WARC-Refers-To",
                "WARC-Target-URI",
                "WARC-Truncated",
                "WARC-Warcinfo-ID",
                "WARC-Filename",
                "WARC-Profile",
                "WARC-Identified-Payload-Type",
                "WARC-Segment-Origin-ID",
                "WARC-Segment-Number",
                "WARC-Segment-Total-Length"
        };
        testEmptyHeader(headers, null);
        testEmptyHeader(headers, "");

        String fieldName;
        String fieldValue;

        fieldName = "WARC-Record-ID";
        fieldValue = "http://hello_kitty$";
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
                {"WARC-Type", new Object[][] {
                        {"hello_kitty", null, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(new Integer(WarcConstants.RT_IDX_UNKNOWN), header.warcTypeIdx);
                            }
                        }}
                }},
                {"WARC-Concurrent-To", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Concurrent-To' value", 2}
                        }, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(1, header.warcConcurrentToList.size());
                            }
                        }},
                        {"hello_kitty2", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Concurrent-To' value", 2},
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Concurrent-To' value", 2}
                        }, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(2, header.warcConcurrentToList.size());
                            }
                        }}
                }},
                {"WARC-Date", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Date' value", 2}
                        }, null, null}
                }},
                {"Content-Length", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'Content-Length' value", 2}
                        }, null, null}
                }},
                {"Content-Type", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'Content-Type' value", 2}

                        }, null, null}
                }},
                {"WARC-Block-Digest", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Block-Digest' value", 2}
                        }, null, null}
                }},
                {"WARC-Payload-Digest", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Payload-Digest' value", 2}
                        }, null, null}
                }},
                {"WARC-IP-Address", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-IP-Address' value", 2}
                        }, null, null}
                }},
                {"WARC-Segment-Number", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Segment-Number' value", 2}
                        }, null, null}
                }},
                {"WARC-Segment-Total-Length", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Segment-Total-Length' value", 2}
                        }, null, null}
                }},
                {"WARC-Truncated", new Object[][] {
                        {"hello_kitty", null, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(new Integer(WarcConstants.TT_IDX_FUTURE_REASON), header.warcTruncatedIdx);
                            }
                        }}
                }},
                {"WARC-Filename", new Object[][] {
                        {"hello_kitty", null, null, null}
                }},
                {"WARC-Profile", new Object[][] {
                        {"hello_kitty", null, null, new TestHeaderCallback() {
                            public void callback(WarcHeader header) {
                                Assert.assertEquals(new Integer(WarcConstants.PROFILE_IDX_UNKNOWN), header.warcProfileIdx);
                            }
                        }}
                }},
                {"WARC-Identified-Payload-Type", new Object[][] {
                        {"hello_kitty", new Object[][] {
                                {DiagnosisType.INVALID_EXPECTED, "'WARC-Identified-Payload-Type' value", 2}
                        }, null, null}
                }}
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
