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

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ContentType;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestWarcHeaderAddDatatyped extends TestWarcHeaderHelper {

    @Test
    public void test_warcheader_adddatatyped() {
        String integerStr = "42";
        Integer integerObj = Integer.parseInt(integerStr);

        String longStr = "123456789012345678";
        Long longObj = Long.parseLong(longStr);

        String digestStr = "sha1:Y4N5SWNQBIBIGQ66IFXDMLGJW6FZFV6U";
        WarcDigest digestObj = WarcDigest.parseWarcDigest(digestStr);

        String contentTypeStr = "application/http; msgtype=response";
        ContentType contentTypeObj = ContentType.parseContentType(contentTypeStr);

        String dateStr = "2012-05-17T00:14:47Z";
        Date dateObj = WarcDateParser.getDate(dateStr);

        String inetAddressStr = "172.20.10.12";
        InetAddress inetAddressObj = null;
        try {
            inetAddressObj = InetAddress.getByName(inetAddressStr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        String uriStr = "urn:uuid:a1bc4f20-d057-44bd-a299-bdf31f4dfa53";
        URI uriObj = URI.create(uriStr);

        /*
         * Non warc headers.
         */

        header = getTestHeader();
        headerLine = header.addHeader("X-Header1", dateObj, dateStr);
        Assert.assertNotNull(headerLine);
        Assert.assertEquals("X-Header1", headerLine.name);
        Assert.assertEquals(dateStr, headerLine.value);
        headerLine = header.addHeader("X-Header2", dateObj, dateStr);
        Assert.assertNotNull(headerLine);
        Assert.assertEquals("X-Header2", headerLine.name);
        Assert.assertEquals(dateStr, headerLine.value);

        /*
         * Duplicate date headers.
         */

        header = getTestHeader();
        headerLine = header.addHeader("WARC-Date", dateObj, dateStr);
        Assert.assertNotNull(headerLine);
        Assert.assertEquals("WARC-Date", headerLine.name);
        Assert.assertEquals(dateStr, headerLine.value);
        errors = header.diagnostics.getErrors();
        warnings = header.diagnostics.getWarnings();
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(0, warnings.size());
        headerLine = header.addHeader("WARC-Date", dateObj, dateStr);
        Assert.assertNotNull(headerLine);
        Assert.assertEquals("WARC-Date", headerLine.name);
        Assert.assertEquals(dateStr, headerLine.value);
        errors = header.diagnostics.getErrors();
        warnings = header.diagnostics.getWarnings();
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(0, warnings.size());
        diagnosis = errors.get(0);
        Assert.assertEquals(DiagnosisType.DUPLICATE, diagnosis.type);
        Assert.assertEquals("'WARC-Date' header", diagnosis.entity);
        Assert.assertEquals(1, diagnosis.information.length);

        /*
         * Integer add header.
         */

        cases = generate_invalid_datatype_cases(integerObj, integerStr, WarcConstants.FN_WARC_SEGMENT_NUMBER, WarcConstants.FN_WARC_RECORD_ID);
        test_headeradd_object_cases(cases, WarcConstants.FDT_INTEGER);

        /*
         * Long add header.
         */

        cases = generate_invalid_datatype_cases(longObj, longStr, WarcConstants.FN_CONTENT_LENGTH, WarcConstants.FN_CONTENT_TYPE);
        test_headeradd_object_cases(cases, WarcConstants.FDT_LONG);

        /*
         * Digest add header.
         */

        cases = generate_invalid_datatype_cases(digestObj, digestStr, WarcConstants.FN_WARC_BLOCK_DIGEST, WarcConstants.FN_WARC_SEGMENT_NUMBER);
        test_headeradd_object_cases(cases, WarcConstants.FDT_DIGEST);

        /*
         * ContentType add header.
         */

        cases = generate_invalid_datatype_cases(contentTypeObj, contentTypeStr, WarcConstants.FN_CONTENT_TYPE, WarcConstants.FN_WARC_BLOCK_DIGEST);
        test_headeradd_object_cases(cases, WarcConstants.FDT_CONTENTTYPE);

        /*
         * Date add header.
         */

        cases = generate_invalid_datatype_cases(dateObj, dateStr, WarcConstants.FN_WARC_DATE, WarcConstants.FN_CONTENT_TYPE);
        test_headeradd_object_cases(cases, WarcConstants.FDT_DATE);

        /*
         * InetAddress add header.
         */

        cases = generate_invalid_datatype_cases(inetAddressObj, inetAddressStr, WarcConstants.FN_WARC_IP_ADDRESS, WarcConstants.FN_WARC_DATE);
        test_headeradd_object_cases(cases, WarcConstants.FDT_INETADDRESS);

        /*
         * URI add header.
         */

        cases = generate_invalid_datatype_cases(uriObj, uriStr, WarcConstants.FN_WARC_RECORD_ID, WarcConstants.FN_WARC_IP_ADDRESS);
        test_headeradd_object_cases(cases, WarcConstants.FDT_URI);

        /*
         * Test datatype for headers.
         */

        /*
        WarcConstants.FN_WARC_TYPE
        WarcConstants.FN_WARC_TRUNCATED
        WarcConstants.FN_WARC_FILENAME
        WarcConstants.FN_WARC_PROFILE
        */

        /*
         * Integer.
         */

        String segmentNrStr = "42";
        Integer segmentNrObj = Integer.parseInt(segmentNrStr);
        cases = generate_header_datatype_cases(segmentNrObj, segmentNrStr, WarcConstants.FN_WARC_SEGMENT_NUMBER, "warcSegmentNumberStr", "warcSegmentNumber");
        test_headeradd_object_cases(cases, WarcConstants.FDT_INTEGER);

        /*
         * Long.
         */

        String contentLengthStr = "1234567890123456";
        Long contentLengthObj = Long.parseLong(contentLengthStr);
        cases = generate_header_datatype_cases(contentLengthObj, contentLengthStr, WarcConstants.FN_CONTENT_LENGTH, "contentLengthStr", "contentLength");
        test_headeradd_object_cases(cases, WarcConstants.FDT_LONG);

        String segmentTotalLengthStr = "9876543210987654";
        Long segmentTotalLengthObj = Long.parseLong(segmentTotalLengthStr);
        cases = generate_header_datatype_cases(segmentTotalLengthObj, segmentTotalLengthStr, WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH, "warcSegmentTotalLengthStr", "warcSegmentTotalLength");
        test_headeradd_object_cases(cases, WarcConstants.FDT_LONG);

        /*
         * Digest.
         */

        String blockDigestStr = "sha1:Y4N5SWNQBIBIGQ66IFXDMLGJW6FZFV6U";
        WarcDigest blockDigestObj = WarcDigest.parseWarcDigest(blockDigestStr);
        cases = generate_header_datatype_cases(blockDigestObj, blockDigestStr, WarcConstants.FN_WARC_BLOCK_DIGEST, "warcBlockDigestStr", "warcBlockDigest");
        test_headeradd_object_cases(cases, WarcConstants.FDT_DIGEST);

        String payloadDigestStr = "sha1:BCCYP7NW6QIIOSM523Y5XHQKE5KWLMBD";
        WarcDigest payloadDigestObj = WarcDigest.parseWarcDigest(payloadDigestStr);
        cases = generate_header_datatype_cases(payloadDigestObj, payloadDigestStr, WarcConstants.FN_WARC_PAYLOAD_DIGEST, "warcPayloadDigestStr", "warcPayloadDigest");
        test_headeradd_object_cases(cases, WarcConstants.FDT_DIGEST);

        /*
         * ContentType.
         */

        contentTypeStr = "application/http; msgtype=request";
        contentTypeObj = ContentType.parseContentType(contentTypeStr);
        cases = generate_header_datatype_cases(contentTypeObj, contentTypeStr, WarcConstants.FN_CONTENT_TYPE, "contentTypeStr", "contentType");
        test_headeradd_object_cases(cases, WarcConstants.FDT_CONTENTTYPE);

        String identifiedPayloadTypeStr = "application/http; msgtype=response";
        ContentType identifiedPayloadTypeObj = ContentType.parseContentType(identifiedPayloadTypeStr);
        cases = generate_header_datatype_cases(identifiedPayloadTypeObj, identifiedPayloadTypeStr, WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE, "warcIdentifiedPayloadTypeStr", "warcIdentifiedPayloadType");
        test_headeradd_object_cases(cases, WarcConstants.FDT_CONTENTTYPE);

        /*
         * Date.
         */

        dateStr = "2010-06-23T13:33:21Z";
        dateObj = WarcDateParser.getDate(dateStr);
        cases = generate_header_datatype_cases(dateObj, dateStr, WarcConstants.FN_WARC_DATE, "warcDateStr", "warcDate");
        test_headeradd_object_cases(cases, WarcConstants.FDT_DATE);

        /*
         * InetAddress.
         */

        inetAddressStr = "174.36.20.141";
        inetAddressObj = null;
        try {
            inetAddressObj = InetAddress.getByName(inetAddressStr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        cases = generate_header_datatype_cases(inetAddressObj, inetAddressStr, WarcConstants.FN_WARC_IP_ADDRESS, "warcIpAddress", "warcInetAddress");
        test_headeradd_object_cases(cases, WarcConstants.FDT_INETADDRESS);

        /*
         * URI.
         */

        String recordIdStr = "urn:uuid:12eab1ec-8615-4f09-b6d2-976d96552073";
        URI recordIdObj = URI.create(recordIdStr);
        cases = generate_header_datatype_cases(recordIdObj, recordIdStr, WarcConstants.FN_WARC_RECORD_ID, "warcRecordIdStr", "warcRecordIdUri");
        test_headeradd_object_cases(cases, WarcConstants.FDT_URI);

        String refersToStr = "urn:uuid:bfa9d26b-ff19-402a-8508-e7ff852d4ded";
        URI refersToObj = URI.create(refersToStr);
        cases = generate_header_datatype_cases(refersToObj, refersToStr, WarcConstants.FN_WARC_REFERS_TO, "warcRefersToStr", "warcRefersToUri");
        test_headeradd_object_cases(cases, WarcConstants.FDT_URI);

        String targetUriStr = "urn:uuid:de715d47-9f36-4cdf-84db-eb3b47dcd0e3";
        URI targetUriObj = URI.create(targetUriStr);
        cases = generate_header_datatype_cases(targetUriObj, targetUriStr, WarcConstants.FN_WARC_TARGET_URI, "warcTargetUriStr", "warcTargetUriUri");
        test_headeradd_object_cases(cases, WarcConstants.FDT_URI);

        String warcinfoIdStr = "urn:uuid:1cb0e3be-d9e2-4058-bf00-9775c75a71a6";
        URI warcinfoIdObj = URI.create(warcinfoIdStr);
        cases = generate_header_datatype_cases(warcinfoIdObj, warcinfoIdStr, WarcConstants.FN_WARC_WARCINFO_ID, "warcWarcinfoIdStr", "warcWarcinfoIdUri");
        test_headeradd_object_cases(cases, WarcConstants.FDT_URI);

        String segmentOriginIdStr = "urn:uuid:c4fc410a-4c7b-4bcc-a251-382b1d669f9a";
        URI segmentOriginIdObj = URI.create(segmentOriginIdStr);
        cases = generate_header_datatype_cases(segmentOriginIdObj, segmentOriginIdStr, WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID, "warcSegmentOriginIdStr", "warcSegmentOriginIdUrl");
        test_headeradd_object_cases(cases, WarcConstants.FDT_URI);

        Object[][] concurrentHeaders = new Object[][] {
                {URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88"), "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88"},
                {URI.create("urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1"), "urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1"},
                {URI.create("urn:uuid:e6c0d888-b384-4ef4-a698-6166bc0875b8"), "urn:uuid:e6c0d888-b384-4ef4-a698-6166bc0875b8"}
        };
        cases = generate_multivalue_cases(concurrentHeaders, WarcConstants.FN_WARC_CONCURRENT_TO, "warcConcurrentToList");
        test_headeradd_multivalue_object_cases(cases, WarcConstants.FDT_URI);

        /*
        public List<WarcConcurrentTo> warcConcurrentToList = new LinkedList<WarcConcurrentTo>();
                */
    }

    public Object[][] generate_invalid_datatype_cases(Object valObj, String valStr, String fnGood, String fnBad) {
        Object[][] typeError = new Object[][] {
                {DiagnosisType.INVALID_EXPECTED, "Invalid datatype for '" + fnBad + "' header", 2}
        };
        Object [][] dupError = new Object[][] {
                {DiagnosisType.DUPLICATE, "'" + fnGood + "' header", 1}
        };
        Object [][] type2Error = new Object[][] {
                {DiagnosisType.INVALID_EXPECTED, "Invalid datatype for '" + fnBad + "' header", 2},
                {DiagnosisType.INVALID_EXPECTED, "Invalid datatype for '" + fnBad + "' header", 2}
        };
        cases = new Object[][] {
                /* single */
                {fnGood, new Object[][] {
                        {null, null}
                }, null, null, null, null},
                {fnGood, new Object[][] {
                        {valObj, null}
                }, valStr, null, null, null},
                {fnGood, new Object[][] {
                        {null, valStr}
                }, valStr, null, null, null},
                {fnGood, new Object[][] {
                        {valObj, valStr}
                }, valStr, null, null, null},
                {fnBad, new Object[][] {
                        {null, null}
                }, null, typeError, null, null},
                {fnBad, new Object[][] {
                        {valObj, null}
                }, valStr, typeError, null, null},
                {fnBad, new Object[][] {
                        {null, valStr}
                }, valStr, typeError, null, null},
                {fnBad, new Object[][] {
                        {valObj, valStr}
                }, valStr, typeError, null, null},
                /* double */
                {fnGood, new Object[][] {
                        {null, null},
                        {null, null}
                }, null, dupError, null, null},
                {fnGood, new Object[][] {
                        {valObj, null},
                        {valObj, null}
                }, valStr, dupError, null, null},
                {fnGood, new Object[][] {
                        {null, valStr},
                        {null, valStr}
                }, valStr, dupError, null, null},
                {fnGood, new Object[][] {
                        {valObj, valStr},
                        {valObj, valStr}
                }, valStr, dupError, null, null},
                {fnBad, new Object[][] {
                        {null, null},
                        {null, null}
                }, null, type2Error, null, null},
                {fnBad, new Object[][] {
                        {valObj, null},
                        {valObj, null}
                }, valStr, type2Error, null, null},
                {fnBad, new Object[][] {
                        {null, valStr},
                        {null, valStr}
                }, valStr, type2Error, null, null},
                {fnBad, new Object[][] {
                        {valObj, valStr},
                        {valObj, valStr}
                }, valStr, type2Error, null, null}
        };
        return cases;
    }

    public Object[][] generate_header_datatype_cases(final Object valObj, final String valStr, final String fn, final String headerStrFieldName, final String headerObjFieldName) {
        Object [][] dupError = new Object[][] {
                {DiagnosisType.DUPLICATE, "'" + fn + "' header", 1}
        };
        TestHeaderCallback cbNull = new TestHeaderCallback() {
            public void callback(WarcHeader header) {
                Field f;
                try {
                    f = header.getClass().getField(headerStrFieldName);
                    Assert.assertNull(f.get(header));
                    f = header.getClass().getField(headerObjFieldName);
                    Assert.assertNull(f.get(header));
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                }
            }
        };
        TestHeaderCallback cbValue = new TestHeaderCallback() {
            public void callback(WarcHeader header) {
                Field f;
                try {
                    f = header.getClass().getField(headerStrFieldName);
                    Assert.assertEquals(valStr, f.get(header));
                    f = header.getClass().getField(headerObjFieldName);
                    Assert.assertEquals(valObj, f.get(header));
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                }
            }
        };
        Object[][] cases = {
                /* single */
                {fn, new Object[][] {
                        {null, null}
                },  null, null, null, cbNull},
                {fn, new Object[][] {
                        {valObj, null}
                }, valStr, null, null, cbValue},
                {fn, new Object[][] {
                        {null, valStr}
                }, valStr, null, null, cbValue},
                {fn, new Object[][] {
                        {valObj, valStr}
                }, valStr, null, null, cbValue},
                /* double */
                {fn, new Object[][] {
                        {null, null},
                        {null, null}
                },  null, dupError, null, cbNull},
                {fn, new Object[][] {
                        {valObj, null},
                        {valObj, null}
                }, valStr, dupError, null, cbValue},
                {fn, new Object[][] {
                        {null, valStr},
                        {null, valStr}
                }, valStr, dupError, null, cbValue},
                {fn, new Object[][] {
                        {valObj, valStr},
                        {valObj, valStr}
                }, valStr, dupError, null, cbValue}
        };
        return cases;
    }

    public Object[][] generate_multivalue_cases(final Object[][] values, final String fn, final String headerListFieldName) {
        Object[][] header1 = new Object[values.length][];
        Object[][] header2 = new Object[values.length][];
        Object[][] header3 = new Object[values.length][];
        Object[][] header4 = new Object[values.length][];
        for (int i=0; i<values.length; ++i) {
            header1[i] = new Object[2];
            header1[i][0] = null;
            header1[i][1] = null;
            header2[i] = new Object[2];
            header2[i][0] = values[i][0];
            header2[i][1] = null;
            header3[i] = new Object[2];
            header3[i][0] = null;
            header3[i][1] = values[i][1];
            header4[i] = new Object[2];
            header4[i][0] = values[i][0];
            header4[i][1] = values[i][1];
        }
        final String[] expectedFieldValueStrArr = new String[values.length];
        for (int i=0; i<values.length; ++i) {
            expectedFieldValueStrArr[i] = (String)values[i][1];
        }
        TestHeaderCallback cbNull = new TestHeaderCallback() {
            public void callback(WarcHeader header) {
                Field f;
                try {
                    f = header.getClass().getField(headerListFieldName);
                    Assert.assertNotNull(f.get(header));
                    List<?> list = (List<?>)f.get(header);
                    Assert.assertEquals(0, list.size());
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                }
            }
        };
        TestHeaderCallback cbValue = new TestHeaderCallback() {
            public void callback(WarcHeader header) {
                Field f;
                try {
                    f = header.getClass().getField(headerListFieldName);
                    Assert.assertNotNull(f.get(header));
                    List<?> list = (List<?>)f.get(header);
                    Assert.assertEquals(expectedFieldValueStrArr.length, list.size());
                    for (int i=0; i<expectedFieldValueStrArr.length; ++i) {
                        WarcConcurrentTo concurrentTo = (WarcConcurrentTo)list.get(i);
                        Assert.assertEquals(values[i][0], concurrentTo.warcConcurrentToUri);
                        Assert.assertEquals(values[i][1], concurrentTo.warcConcurrentToStr);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Assert.fail("Unexpected exception!");
                }
            }
        };
        Object[][] cases = {
                /* single */
                {fn, header1, null, null, null, cbNull},
                {fn, header2, expectedFieldValueStrArr, null, null, cbValue},
                {fn, header3, expectedFieldValueStrArr, null, null, cbValue},
                {fn, header4, expectedFieldValueStrArr, null, null, cbValue},
        };
        return cases;
    }

    public void test_headeradd_object_cases(Object[][] cases, int dt) {
        for (int i=0; i<cases.length; ++i) {
            // debug
            //System.out.println(i);
            String fieldName = (String)cases[i][0];
            Object[][] values = (Object[][])cases[i][1];
            String expectedFieldValueStr = (String)cases[i][2];
            Object[][] expectedErrors = (Object[][])cases[i][3];
            Object[][] expectedWarnings = (Object[][])cases[i][4];
            TestHeaderCallback callback = (TestHeaderCallback)cases[i][5];
            header = getTestHeader();
            for (int j=0; j<values.length; ++j) {
                Object fieldValue = values[j][0];
                String fieldValueStr = (String)values[j][1];
                switch (dt) {
                case WarcConstants.FDT_INTEGER:
                    headerLine = header.addHeader(fieldName, (Integer)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_LONG:
                    headerLine = header.addHeader(fieldName, (Long)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_DIGEST:
                    headerLine = header.addHeader(fieldName, (WarcDigest)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_CONTENTTYPE:
                    headerLine = header.addHeader(fieldName, (ContentType)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_DATE:
                    headerLine = header.addHeader(fieldName, (Date)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_INETADDRESS:
                    headerLine = header.addHeader(fieldName, (InetAddress)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_URI:
                    headerLine = header.addHeader(fieldName, (URI)fieldValue, fieldValueStr);
                    break;
                }
                Assert.assertNotNull(headerLine);
                Assert.assertEquals(fieldName, headerLine.name);
                if (fieldValue != null || fieldValueStr != null) {
                    Assert.assertEquals(expectedFieldValueStr, headerLine.value);
                } else {
                    Assert.assertNull(headerLine.value);
                }
            }
            test_result(expectedErrors, expectedWarnings, callback);
        }
    }

    public void test_headeradd_multivalue_object_cases(Object[][] cases, int dt) {
        for (int i=0; i<cases.length; ++i) {
            // debug
            //System.out.println(i);
            String fieldName = (String)cases[i][0];
            Object[][] values = (Object[][])cases[i][1];
            String[] expectedFieldValueStrList = (String[])cases[i][2];
            Object[][] expectedErrors = (Object[][])cases[i][3];
            Object[][] expectedWarnings = (Object[][])cases[i][4];
            TestHeaderCallback callback = (TestHeaderCallback)cases[i][5];
            header = getTestHeader();
            for (int j=0; j<values.length; ++j) {
                Object fieldValue = values[j][0];
                String fieldValueStr = (String)values[j][1];
                switch (dt) {
                case WarcConstants.FDT_INTEGER:
                    headerLine = header.addHeader(fieldName, (Integer)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_LONG:
                    headerLine = header.addHeader(fieldName, (Long)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_DIGEST:
                    headerLine = header.addHeader(fieldName, (WarcDigest)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_CONTENTTYPE:
                    headerLine = header.addHeader(fieldName, (ContentType)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_DATE:
                    headerLine = header.addHeader(fieldName, (Date)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_INETADDRESS:
                    headerLine = header.addHeader(fieldName, (InetAddress)fieldValue, fieldValueStr);
                    break;
                case WarcConstants.FDT_URI:
                    headerLine = header.addHeader(fieldName, (URI)fieldValue, fieldValueStr);
                    break;
                }
                Assert.assertNotNull(headerLine);
                Assert.assertEquals(fieldName, headerLine.name);
                if (fieldValue != null || fieldValueStr != null) {
                    Assert.assertEquals(expectedFieldValueStrList[j], headerLine.value);
                } else {
                    Assert.assertNull(headerLine.value);
                }
            }
            test_result(expectedErrors, expectedWarnings, callback);
        }
    }

}
