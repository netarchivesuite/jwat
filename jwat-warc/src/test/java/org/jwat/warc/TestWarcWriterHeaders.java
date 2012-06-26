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
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ContentType;

@RunWith(JUnit4.class)
public class TestWarcWriterHeaders {

    @Test
    public void test_warcwriter_compressed() {
        test_warc_writer_headers(true);
    }

    @Test
    public void test_warcwriter_uncompressed() {
        test_warc_writer_headers(false);
    }

    public void test_warc_writer_headers(boolean compress) {
        String segmentNrStr = "42";
        Integer segmentNrObj = Integer.parseInt(segmentNrStr);
        /*
         * Long.
         */
        String contentLengthStr = "1234567890123456";
        Long contentLengthObj = Long.parseLong(contentLengthStr);
        String segmentTotalLengthStr = "9876543210987654";
        Long segmentTotalLengthObj = Long.parseLong(segmentTotalLengthStr);
        /*
         * Digest.
         */
        String blockDigestStr = "sha1:Y4N5SWNQBIBIGQ66IFXDMLGJW6FZFV6U";
        WarcDigest blockDigestObj = WarcDigest.parseWarcDigest(blockDigestStr);
        String payloadDigestStr = "sha1:BCCYP7NW6QIIOSM523Y5XHQKE5KWLMBD";
        WarcDigest payloadDigestObj = WarcDigest.parseWarcDigest(payloadDigestStr);
        /*
         * ContentType.
         */
        String contentTypeStr = "application/http; msgtype=request";
        ContentType contentTypeObj = ContentType.parseContentType(contentTypeStr);
        String identifiedPayloadTypeStr = "application/http; msgtype=response";
        ContentType identifiedPayloadTypeObj = ContentType.parseContentType(identifiedPayloadTypeStr);
        /*
         * Date.
         */
        String dateStr = "2010-06-23T13:33:21Z";
        Date dateObj = WarcDateParser.getDate(dateStr);
        /*
         * InetAddress.
         */
        String inetAddressStr = "174.36.20.141";
        InetAddress inetAddressObj = null;
        try {
            inetAddressObj = InetAddress.getByName(inetAddressStr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
        /*
         * URI.
         */
        String recordIdStr = "urn:uuid:12eab1ec-8615-4f09-b6d2-976d96552073";
        URI recordIdObj = URI.create(recordIdStr);
        String refersToStr = "urn:uuid:bfa9d26b-ff19-402a-8508-e7ff852d4ded";
        URI refersToObj = URI.create(refersToStr);
        String targetUriStr = "urn:uuid:de715d47-9f36-4cdf-84db-eb3b47dcd0e3";
        URI targetUriObj = URI.create(targetUriStr);
        String warcinfoIdStr = "urn:uuid:1cb0e3be-d9e2-4058-bf00-9775c75a71a6";
        URI warcinfoIdObj = URI.create(warcinfoIdStr);
        String segmentOriginIdStr = "urn:uuid:c4fc410a-4c7b-4bcc-a251-382b1d669f9a";
        URI segmentOriginIdObj = URI.create(segmentOriginIdStr);
        /*
         * ConcurrentTo.
         */
        Object[][] concurrentHeaders = new Object[][] {
                {URI.create("urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88"), "urn:uuid:173a3db1-9ba4-496e-9eaf-6e550a62fd88"},
                {URI.create("urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1"), "urn:uuid:660b74e7-076e-4698-abba-4eeeb8e09bf1"},
                {URI.create("urn:uuid:e6c0d888-b384-4ef4-a698-6166bc0875b8"), "urn:uuid:e6c0d888-b384-4ef4-a698-6166bc0875b8"}
        };
        /*
         * String.
         */
        String warctypeStr = "warcinfo";
        Integer warcTypeObj = WarcConstants.RT_IDX_WARCINFO;
        String warcFilenameStr = "BnF-cibl2010-20100623133319-00000-atlas20.bnf.fr.warc.gz";
        String warcTruncatedStr = WarcConstants.TT_LENGTH;
        Integer warcTruncatedObj = WarcConstants.TT_IDX_LENGTH;
        String warcProfileStr = WarcConstants.PROFILE_IDENTICAL_PAYLOAD_DIGEST;
        Integer warcProfileObj = WarcConstants.PROFILE_IDX_IDENTICAL_PAYLOAD_DIGEST;
        /*
         * Fields.
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WarcWriter writer;
        WarcRecord record;
        WarcHeader header;
        byte[] recordHeader;
        ByteArrayInputStream in;
        byte[] payload;
        try {
            payload = "Welcome to dænemark!".getBytes("UTF-8");
            contentLengthObj = new Long(payload.length);
            contentLengthStr = contentLengthObj.toString();

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            /*
             * Empty headers.
             */
            record = WarcRecord.createRecord(writer);
            header = record.header;

            recordHeader = writer.writeHeader(record);

            writer.closeRecord();
            /*
             * String headers.
             */
            record = WarcRecord.createRecord(writer);
            header = record.header;
            header.warcSegmentNumberStr = segmentNrStr;
            header.contentLengthStr = contentLengthStr;
            header.warcSegmentTotalLengthStr = segmentTotalLengthStr;
            header.contentTypeStr = contentTypeStr;
            header.warcBlockDigestStr = blockDigestStr;
            header.warcPayloadDigestStr = payloadDigestStr;
            header.warcIdentifiedPayloadTypeStr = identifiedPayloadTypeStr;
            header.warcDateStr = dateStr;
            header.warcIpAddress = inetAddressStr;
            header.warcRecordIdStr = recordIdStr;
            header.warcRefersToStr = refersToStr;
            header.warcTargetUriStr = targetUriStr;
            header.warcWarcinfoIdStr = warcinfoIdStr;
            header.warcSegmentOriginIdStr = segmentOriginIdStr;
            for (int i=0; i<concurrentHeaders.length; ++i) {
                WarcConcurrentTo concurrentTo = new WarcConcurrentTo();
                concurrentTo.warcConcurrentToStr = (String)concurrentHeaders[i][1];
                concurrentTo.warcConcurrentToUri = null;
                header.warcConcurrentToList.add(concurrentTo);
            }
            header.warcTypeStr = warctypeStr;
            header.warcFilename = warcFilenameStr;
            header.warcTruncatedStr = warcTruncatedStr;
            header.warcProfileStr = warcProfileStr;

            in = new ByteArrayInputStream(payload);

            recordHeader = writer.writeHeader(record);
            writer.streamPayload(in, payload.length);
            writer.closeRecord();
            /*
             * Object headers.
             */
            record = WarcRecord.createRecord(writer);
            header = record.header;
            header.warcSegmentNumber = segmentNrObj;
            header.contentLength = contentLengthObj;
            header.warcSegmentTotalLength = segmentTotalLengthObj;
            header.contentType = contentTypeObj;
            header.warcBlockDigest = blockDigestObj;
            header.warcPayloadDigest = payloadDigestObj;
            header.warcIdentifiedPayloadType = identifiedPayloadTypeObj;
            header.warcDate = dateObj;
            header.warcInetAddress = inetAddressObj;
            header.warcRecordIdUri = recordIdObj;
            header.warcRefersToUri = refersToObj;
            header.warcTargetUriUri = targetUriObj;
            header.warcWarcInfoIdUri = warcinfoIdObj;
            header.warcSegmentOriginIdUrl = segmentOriginIdObj;
            for (int i=0; i<concurrentHeaders.length; ++i) {
                WarcConcurrentTo concurrentTo = new WarcConcurrentTo();
                concurrentTo.warcConcurrentToStr = null;
                concurrentTo.warcConcurrentToUri = (URI)concurrentHeaders[i][0];
                header.warcConcurrentToList.add(concurrentTo);
            }
            header.warcTypeIdx = warcTypeObj;
            header.warcFilename = warcFilenameStr;
            header.warcTruncatedIdx = warcTruncatedObj;
            header.warcProfileIdx = warcProfileObj;

            in = new ByteArrayInputStream(payload);

            recordHeader = writer.writeHeader(record);
            writer.streamPayload(in, payload.length);
            writer.closeRecord();
            /*
             * addHeader Strings.
             */
            record = WarcRecord.createRecord(writer);
            header = record.header;

            header.addHeader(WarcConstants.FN_WARC_SEGMENT_NUMBER, segmentNrStr);
            header.addHeader(WarcConstants.FN_CONTENT_LENGTH, contentLengthStr);
            header.addHeader(WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH, segmentTotalLengthStr);
            header.addHeader(WarcConstants.FN_CONTENT_TYPE, contentTypeStr);
            header.addHeader(WarcConstants.FN_WARC_BLOCK_DIGEST, blockDigestStr);
            header.addHeader(WarcConstants.FN_WARC_PAYLOAD_DIGEST, payloadDigestStr);
            header.addHeader(WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE, identifiedPayloadTypeStr);
            header.addHeader(WarcConstants.FN_WARC_DATE, dateStr);
            header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, inetAddressStr);
            header.addHeader(WarcConstants.FN_WARC_RECORD_ID, recordIdStr);
            header.addHeader(WarcConstants.FN_WARC_REFERS_TO, refersToStr);
            header.addHeader(WarcConstants.FN_WARC_TARGET_URI, targetUriStr);
            header.addHeader(WarcConstants.FN_WARC_WARCINFO_ID, warcinfoIdStr);
            header.addHeader(WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID, segmentOriginIdStr);
            for (int i=0; i<concurrentHeaders.length; ++i) {
                header.addHeader(WarcConstants.FN_WARC_CONCURRENT_TO, (String)concurrentHeaders[i][1]);
            }
            header.addHeader(WarcConstants.FN_WARC_TYPE, warctypeStr);
            header.addHeader(WarcConstants.FN_WARC_FILENAME, warcFilenameStr);
            header.addHeader(WarcConstants.FN_WARC_TRUNCATED, warcTruncatedStr);
            header.addHeader(WarcConstants.FN_WARC_PROFILE, warcProfileStr);

            in = new ByteArrayInputStream(payload);

            recordHeader = writer.writeHeader(record);
            writer.streamPayload(in, payload.length);
            writer.closeRecord();
            /*
             * addHeader datatypes.
             */
            record = WarcRecord.createRecord(writer);
            header = record.header;

            header.addHeader(WarcConstants.FN_WARC_SEGMENT_NUMBER, segmentNrObj, null);
            header.addHeader(WarcConstants.FN_CONTENT_LENGTH, contentLengthObj, null);
            header.addHeader(WarcConstants.FN_WARC_SEGMENT_TOTAL_LENGTH, segmentTotalLengthObj, null);
            header.addHeader(WarcConstants.FN_CONTENT_TYPE, contentTypeObj, null);
            header.addHeader(WarcConstants.FN_WARC_BLOCK_DIGEST, blockDigestObj, null);
            header.addHeader(WarcConstants.FN_WARC_PAYLOAD_DIGEST, payloadDigestObj, null);
            header.addHeader(WarcConstants.FN_WARC_IDENTIFIED_PAYLOAD_TYPE, identifiedPayloadTypeObj, null);
            header.addHeader(WarcConstants.FN_WARC_DATE, dateObj, null);
            header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, inetAddressObj, null);
            header.addHeader(WarcConstants.FN_WARC_RECORD_ID, recordIdObj, null);
            header.addHeader(WarcConstants.FN_WARC_REFERS_TO, refersToObj, null);
            header.addHeader(WarcConstants.FN_WARC_TARGET_URI, targetUriObj, null);
            header.addHeader(WarcConstants.FN_WARC_WARCINFO_ID, warcinfoIdObj, null);
            header.addHeader(WarcConstants.FN_WARC_SEGMENT_ORIGIN_ID, segmentOriginIdObj, null);
            for (int i=0; i<concurrentHeaders.length; ++i) {
                header.addHeader(WarcConstants.FN_WARC_CONCURRENT_TO, (URI)concurrentHeaders[i][0], null);
            }
            header.addHeader(WarcConstants.FN_WARC_TYPE, warctypeStr);
            header.addHeader(WarcConstants.FN_WARC_FILENAME, warcFilenameStr);
            header.addHeader(WarcConstants.FN_WARC_TRUNCATED, warcTruncatedStr);
            header.addHeader(WarcConstants.FN_WARC_PROFILE, warcProfileStr);

            in = new ByteArrayInputStream(payload);

            recordHeader = writer.writeHeader(record);
            writer.streamPayload(in, payload.length);
            writer.closeRecord();

            writer.close();
            out.close();

            /*
            record.header.addHeader("WARC-Type", "warcinfo");
            record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
            record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
            record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
            record.header.addHeader("Content-Length", "483");
            record.header.addHeader("Content-Type", "application/warc-fields");
            */

            // debug
            String tmpStr = new String(out.toByteArray());
            System.out.print(tmpStr);

            ByteArrayInputStream bytesIn = new ByteArrayInputStream(out.toByteArray());

            WarcReader reader = WarcReaderFactory.getReader(bytesIn, 1024);
            int recordNr = 0;
            while ((record = reader.getNextRecord()) != null) {
                ++recordNr;
                header = record.header;
                switch (recordNr) {
                case 1:
                    break;
                default:
                    Assert.assertEquals(segmentNrStr, header.warcSegmentNumberStr);
                    Assert.assertEquals(segmentNrObj, header.warcSegmentNumber);
                    Assert.assertEquals(contentLengthStr, header.contentLengthStr);
                    Assert.assertEquals(contentLengthObj, header.contentLength);
                    Assert.assertEquals(segmentTotalLengthStr, header.warcSegmentTotalLengthStr);
                    Assert.assertEquals(segmentTotalLengthObj, header.warcSegmentTotalLength);
                    Assert.assertEquals(contentTypeStr, header.contentTypeStr);
                    Assert.assertEquals(contentTypeObj, header.contentType);
                    Assert.assertEquals(blockDigestStr, header.warcBlockDigestStr);
                    Assert.assertEquals(blockDigestObj, header.warcBlockDigest);
                    Assert.assertEquals(payloadDigestStr, header.warcPayloadDigestStr);
                    Assert.assertEquals(payloadDigestObj, header.warcPayloadDigest);
                    Assert.assertEquals(identifiedPayloadTypeStr, header.warcIdentifiedPayloadTypeStr);
                    Assert.assertEquals(identifiedPayloadTypeObj, header.warcIdentifiedPayloadType);
                    Assert.assertEquals(dateStr, header.warcDateStr);
                    Assert.assertEquals(dateObj, header.warcDate);
                    Assert.assertEquals(inetAddressStr, header.warcIpAddress);
                    Assert.assertEquals(inetAddressObj, header.warcInetAddress);
                    //Assert.assertEquals(recordIdStr, header.warcRecordIdStr);
                    Assert.assertEquals(recordIdObj, header.warcRecordIdUri);
                    //Assert.assertEquals(refersToStr, header.warcRefersToStr);
                    Assert.assertEquals(refersToObj, header.warcRefersToUri);
                    //Assert.assertEquals(targetUriStr, header.warcTargetUriStr);
                    Assert.assertEquals(targetUriObj, header.warcTargetUriUri);
                    //Assert.assertEquals(warcinfoIdStr, header.warcWarcinfoIdStr);
                    Assert.assertEquals(warcinfoIdObj, header.warcWarcInfoIdUri);
                    //Assert.assertEquals(segmentOriginIdStr, header.warcSegmentOriginIdStr);
                    Assert.assertEquals(segmentOriginIdObj, header.warcSegmentOriginIdUrl);
                    Assert.assertEquals(concurrentHeaders.length, header.warcConcurrentToList.size());
                    for (int i=0; i<concurrentHeaders.length; ++i) {
                        WarcConcurrentTo concurrentTo = header.warcConcurrentToList.get(i);;
                        //Assert.assertEquals((String)concurrentHeaders[i][1], concurrentTo.warcConcurrentToStr);
                        Assert.assertEquals((URI)concurrentHeaders[i][0], concurrentTo.warcConcurrentToUri);
                    }
                    Assert.assertEquals(warctypeStr, header.warcTypeStr);
                    Assert.assertEquals(warcTypeObj, header.warcTypeIdx);
                    Assert.assertEquals(warcFilenameStr, header.warcFilename);
                    Assert.assertEquals(warcFilenameStr, header.warcFilename);
                    Assert.assertEquals(warcTruncatedStr, header.warcTruncatedStr);
                    Assert.assertEquals(warcTruncatedObj, header.warcTruncatedIdx);
                    Assert.assertEquals(warcProfileStr, header.warcProfileStr);
                    Assert.assertEquals(warcProfileObj, header.warcProfileIdx);
                    break;
                }
            }
            reader.close();

            // debug
            System.out.println(recordNr);

        } catch (IOException e) {
        }
    }

}
