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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.Base32;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Uri;

@RunWith(JUnit4.class)
public class TestWarc_RecordTypeDigestCheck {

    @Test
    public void test_warcrecordtype_digestcheck() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in;
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA1");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        byte[] payload1 = null;
        byte[] response2 = null;
        byte[] content2 = null;
        byte[] payload2 = null;
        byte[] payload3 = null;
        byte[] payload4 = null;
        byte[] payload5 = null;
        byte[] payload6 = null;
        byte[] payload7 = null;
        byte[] payload8 = null;
        String emptyDigest = null;
        String blockDigest1 = null;
        String blockDigest2 = null;
        String contentDigest2 = null;
        String blockDigest3 = null;
        String blockDigest4 = null;
        String blockDigest5 = null;
        String blockDigest6 = null;
        String blockDigest7 = null;
        String blockDigest8 = null;

        try {
            payload1 = "software: jwhat?\r\n".getBytes("UTF-8");
            content2 = "<html>Hello</html>".getBytes("UTF-8");
            response2 = ("HTTP/1.1 200 OK\r\n"
                    + "Date: Fri, 25 Feb 2011 18:32:18 GMT\r\n"
                    + "Server: Apache/2.2.9 (Debian) PHP/5.2.6-1+lenny9 with Suhosin-Patch mod_ssl/2.2.9 OpenSSL/0.9.8g\r\n"
                    + "Last-Modified: Wed, 12 May 2010 17:10:21 GMT\r\n"
                    + "ETag: \"56eb28-b94-48668b78c7940\"\r\n"
                    + "Accept-Ranges: bytes\r\n"
                    + "Content-Length: " + content2.length + "\r\n"
                    + "Connection: close\r\n"
                    + "Content-Type: image/jpeg\r\n"
                    + "\r\n").getBytes("UTF-8");
            payload2 = new byte[response2.length + content2.length];
            System.arraycopy(response2, 0, payload2, 0, response2.length);
            System.arraycopy(content2, 0, payload2, response2.length, content2.length);

            payload3 = "<html>jwassup</html>\r\n".getBytes("UTF-8");

            payload4 = ("GET /yp/CompanyDetail.aspx?ID=9898 HTTP/1.0\r\n"
                    + "User-Agent: Mozilla/5.0 (compatible; archive.org_bot +http://www.archive.org/details/archive.org_bot)\r\n"
                    + "Connection: close\r\n"
                    + "Referer: http://e2822.3dtz.cn/yp/typesearchlist.aspx?TypeID=107001&PageNum=6\r\n"
                    + "Host: e2822.3dtz.cn\r\n"
                    + "\r\n").getBytes("UTF-8");

            payload5 = ("via: http://jwat.org/coolio\r\n"
                    + "hopsFromSeed: LOL\r\n"
                    + "fetchTimeMs: 42\r\n"
                    + "outlink: http://www.antiaction.com L a/@href\r\n").getBytes("UTF-8");

            payload6 = ("HTTP/1.1 200 OK\r\n"
                    + "Content-Type: image/jpeg\r\n"
                    + "cache-control: public, max-age=2592000\r\n"
                    + "Via: vvarnish\r\n"
                    + "X-Backend-Server: varnish\r\n"
                    + "Via: 1.1 varnish\r\n"
                    + "Fastly-Debug-Digest: 986a634ffed56a664a46fb63eced87706c6f80f9e912b82a1b6009a582787b71\r\n"
                    + "Access-Control-Allow-Origin: *\r\n"
                    + "Content-Length: 1940\r\n"
                    + "Accept-Ranges: bytes\r\n"
                    + "Date: Fri, 03 Mar 2017 04:29:25 GMT\r\n"
                    + "Via: 1.1 varnish\r\n"
                    + "Age: 812496\r\n"
                    + "Connection: close\r\n"
                    + "X-Served-By: cache-dfw1822-DFW, cache-sjc3621-SJC\r\n"
                    + "X-Cache: miss, HIT, HIT\r\n"
                    + "X-Cache-Hits: 2, 1\r\n"
                    + "X-Timer: S1488515365.783689,VS0,VE0\r\n"
                    + "\r\n").getBytes("UTF-8");

            payload7 = "converted".getBytes("UTF-8");

            payload8 = "continuation".getBytes("UTF-8");

            md.reset();
            emptyDigest = Base32.encodeArray(md.digest(new byte[0]));
            md.reset();
            blockDigest1 = Base32.encodeArray(md.digest(payload1));
            md.reset();
            blockDigest2 = Base32.encodeArray(md.digest(payload2));
            md.reset();
            contentDigest2 = Base32.encodeArray(md.digest(content2));
            md.reset();
            blockDigest3 = Base32.encodeArray(md.digest(payload3));
            md.reset();
            blockDigest4 = Base32.encodeArray(md.digest(payload4));
            md.reset();
            blockDigest5 = Base32.encodeArray(md.digest(payload5));
            md.reset();
            blockDigest6 = Base32.encodeArray(md.digest(payload6));
            md.reset();
            blockDigest7 = Base32.encodeArray(md.digest(payload7));
            md.reset();
            blockDigest8 = Base32.encodeArray(md.digest(payload8));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        Object[][] records = new Object[][] {
            {
                WarcConstants.RT_IDX_WARCINFO, new String[][] {
                    {WarcConstants.FN_CONTENT_TYPE, WarcConstants.CT_APP_WARC_FIELDS},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest1}
                }, payload1, new Object[][] {
                }
            },
            {
                WarcConstants.RT_IDX_RESPONSE, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_IP_ADDRESS, "87.119.197.90"},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest2},
                    {WarcConstants.FN_WARC_PAYLOAD_DIGEST, "sha1:" + contentDigest2},
                    {WarcConstants.FN_CONTENT_TYPE, "application/http; msgtype=response"}
                }, payload2, new Object[][] {
                }
            },
            {
                WarcConstants.RT_IDX_RESOURCE, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest3},
                    {WarcConstants.FN_CONTENT_TYPE, "text/html;charset=UTF-8"},
                }, payload3, new Object[][] {
                }
            },
            {
                WarcConstants.RT_IDX_REQUEST, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest4},
                    {WarcConstants.FN_WARC_PAYLOAD_DIGEST, "sha1:" + emptyDigest},
                    {WarcConstants.FN_CONTENT_TYPE, "application/http; msgtype=request"}
                }, payload4, new Object[][] {
                }
            },
            {
                WarcConstants.RT_IDX_METADATA, new String[][] {
                    {WarcConstants.FN_CONTENT_TYPE, WarcConstants.CT_APP_WARC_FIELDS},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest5}
                }, payload5, new Object[][] {
                }
            },
            {
                WarcConstants.RT_IDX_REVISIT, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_TRUNCATED, "length"},
                    {WarcConstants.FN_WARC_REFERS_TO_TARGET_URI, "https://i.vimeocdn.com/portrait/8927943_64x64.jpg"},
                    {WarcConstants.FN_WARC_REFERS_TO_DATE, "2017-03-03T03:53:36Z"},
                    {WarcConstants.FN_WARC_REFERS_TO, "<urn:uuid:33ab10b0-6a22-4b6b-976c-36746569ce2f>"},
                    {WarcConstants.FN_WARC_PROFILE, WarcConstants.WARC10_PROFILE_IDENTICAL_PAYLOAD_DIGEST},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest6},
                    {WarcConstants.FN_WARC_PAYLOAD_DIGEST, "sha1:" + contentDigest2},
                    {WarcConstants.FN_CONTENT_TYPE, "application/http; msgtype=response"}
                }, payload6, new Object[][] {
                }
            },
            {
                WarcConstants.RT_IDX_CONVERSION, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/transformers-go.gif"},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest7},
                    {WarcConstants.FN_CONTENT_TYPE, "image/gif"}
                }, payload7, new Object[][] {
                }
            },
            {
                WarcConstants.RT_IDX_CONTINUATION, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_SEGMENT_NUMBER, "42"},
                    {WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID, "urn:uuid:" + UUID.randomUUID().toString()},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest8},
                    {WarcConstants.FN_WARC_PAYLOAD_DIGEST, "sha1:" + blockDigest3}
                }, payload8, new Object[][] {
                }
            }
        };

        try {
            out.reset();
            write_warcfile(records, out);
            out.close();

            // debug
            //System.out.println(new String(out.toByteArray()));

            in = new ByteArrayInputStream(out.toByteArray());
            test_warcfile(records, in);
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

        records = new Object[][] {
            {
                WarcConstants.RT_IDX_WARCINFO, new String[][] {
                    {WarcConstants.FN_CONTENT_TYPE, WarcConstants.CT_APP_WARC_FIELDS},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest2}
                }, payload1, new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2}
                }
            },
            {
                WarcConstants.RT_IDX_RESPONSE, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_IP_ADDRESS, "87.119.197.90"},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest3},
                    {WarcConstants.FN_WARC_PAYLOAD_DIGEST, "sha1:" + blockDigest3},
                    {WarcConstants.FN_CONTENT_TYPE, "application/http; msgtype=response"}
                }, payload2, new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2},
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect payload digest", 2}
                }
            },
            {
                WarcConstants.RT_IDX_RESOURCE, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest4},
                    {WarcConstants.FN_CONTENT_TYPE, "text/html;charset=UTF-8"},
                }, payload3, new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2}
                }
            },
            {
                WarcConstants.RT_IDX_REQUEST, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest5},
                    {WarcConstants.FN_WARC_PAYLOAD_DIGEST, "sha1:" + emptyDigest},
                    {WarcConstants.FN_CONTENT_TYPE, "application/http; msgtype=request"}
                }, payload4, new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2}
                }
            },
            {
                WarcConstants.RT_IDX_METADATA, new String[][] {
                    {WarcConstants.FN_CONTENT_TYPE, WarcConstants.CT_APP_WARC_FIELDS},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest6}
                }, payload5, new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2}
                }
            },
            {
                WarcConstants.RT_IDX_REVISIT, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_TRUNCATED, "length"},
                    {WarcConstants.FN_WARC_REFERS_TO_TARGET_URI, "https://i.vimeocdn.com/portrait/8927943_64x64.jpg"},
                    {WarcConstants.FN_WARC_REFERS_TO_DATE, "2017-03-03T03:53:36Z"},
                    {WarcConstants.FN_WARC_REFERS_TO, "<urn:uuid:33ab10b0-6a22-4b6b-976c-36746569ce2f>"},
                    {WarcConstants.FN_WARC_PROFILE, WarcConstants.WARC10_PROFILE_IDENTICAL_PAYLOAD_DIGEST},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest7},
                    {WarcConstants.FN_WARC_PAYLOAD_DIGEST, "sha1:" + blockDigest1},
                    {WarcConstants.FN_CONTENT_TYPE, "application/http; msgtype=response"}
                }, payload6, new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2}
                }
            },
            {
                WarcConstants.RT_IDX_CONVERSION, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/transformers-go.gif"},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest8},
                    {WarcConstants.FN_CONTENT_TYPE, "image/gif"}
                }, payload7, new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2}
                }
            },
            {
                WarcConstants.RT_IDX_CONTINUATION, new String[][] {
                    {WarcConstants.FN_WARC_TARGET_URI, "https://jwat.org/"},
                    {WarcConstants.FN_WARC_SEGMENT_NUMBER, "42"},
                    {WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID, "urn:uuid:" + UUID.randomUUID().toString()},
                    {WarcConstants.FN_WARC_BLOCK_DIGEST, "sha1:" + blockDigest1},
                    {WarcConstants.FN_WARC_PAYLOAD_DIGEST, "sha1:" + blockDigest1}
                }, payload8, new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "Incorrect block digest", 2}
                }
            }
        };

        try {
            out.reset();
            write_warcfile(records, out);
            out.close();

            // debug
            //System.out.println(new String(out.toByteArray()));

            in = new ByteArrayInputStream(out.toByteArray());
            test_warcfile(records, in);
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

    public static void write_warcfile(Object[][] records, OutputStream out) throws IOException {
        WarcWriter writer = WarcWriterFactory.getWriter(out, false);
        WarcRecord record;
        WarcHeader header;
        String[][] headers;
        InputStream in;
        for (int i=0; i<records.length; ++i) {
            record = WarcRecord.createRecord(writer);
            header = record.header;
            header.addHeader(WarcConstants.FN_WARC_TYPE, WarcConstants.RT_IDX_STRINGS[(Integer)records[i][0]]);
            header.addHeader(WarcConstants.FN_WARC_RECORD_ID, Uri.create("urn:uuid:" + UUID.randomUUID().toString()), null);
            header.addHeader(WarcConstants.FN_WARC_DATE, WarcDate.now(), null);
            headers = (String[][])records[i][1];
            for (int j=0; j<headers.length; ++j) {
                header.addHeader(headers[j][0], headers[j][1]);
            }
            byte[] payload = (byte[])records[i][2];
            header.addHeader(WarcConstants.FN_CONTENT_LENGTH, payload.length, null);
            writer.writeHeader(record);
            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            in.close();
            writer.closeRecord();
        }
        writer.close();
    }

    public static Object[][] expectedWarnings = new Object[][] {};

    public static void test_warcfile(Object[][] records, InputStream in) throws IOException {
        WarcReader reader = WarcReaderFactory.getReader(in);
        WarcRecord record = null;
        reader.setBlockDigestEnabled(true);
        reader.setPayloadDigestEnabled(true);
        Object[][] expectedErrors;
        for (int i=0; i<records.length; ++i) {
            Assert.assertNotNull(records);
            record = reader.getNextRecord();
            record.close();
            //debug
            /*
            System.out.println(record.header.warcTypeStr);
            if (record.header.warcBlockDigest != null) {
                System.out.println("expected block digest: " + record.header.warcBlockDigest.digestString);
            }
            if (record.computedBlockDigest != null) {
                System.out.println("computed block digest: " + record.computedBlockDigest.digestString);
            }
            if (record.header.warcPayloadDigest != null) {
                System.out.println("expected payload digest: " + record.header.warcPayloadDigest.digestString);
            }
            if (record.computedPayloadDigest != null) {
                System.out.println("computed payload digest: " + record.computedPayloadDigest.digestString);
            }
            TestBaseUtils.printDiagnoses(record.diagnostics.getErrors());
            TestBaseUtils.printDiagnoses(record.diagnostics.getWarnings());
            */
            expectedErrors = (Object[][])records[i][3];
            TestBaseUtils.compareDiagnoses(expectedErrors, record.diagnostics.getErrors());
            TestBaseUtils.compareDiagnoses(expectedWarnings, record.diagnostics.getWarnings());
        }
        record = reader.getNextRecord();
        reader.close();
        Assert.assertNull(record);
    }

}
