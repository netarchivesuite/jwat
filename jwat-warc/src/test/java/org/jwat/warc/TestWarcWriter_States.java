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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWarcWriter_States {

    @Test
    public void test_warcwriter_compressed() {
        test_warc_writer_states(true);
    }

    @Test
    public void test_warcwriter_uncompressed() {
        test_warc_writer_states(false);
    }

    public void test_warc_writer_states(boolean compress) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WarcWriter writer;
        WarcRecord record;
        byte[] recordHeader;
        ByteArrayInputStream in;
        byte[] payload;
        try {
            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);

            Assert.assertNull(writer.header);
            Assert.assertNull(writer.headerContentLength);
            Assert.assertEquals(0, writer.payloadWrittenTotal);

            payload = "Welcome to dænemark!".getBytes("UTF-8");

            record = WarcRecord.createRecord(writer);
            record.header.addHeader("WARC-Type", "warcinfo");
            record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
            record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
            record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
            //record.header.addHeader("Content-Length", "483");
            record.header.addHeader("Content-Length", Integer.toString(payload.length));
            record.header.addHeader("Content-Type", "application/warc-fields");

            recordHeader = writer.writeHeader(record);

            Assert.assertEquals(record.header, writer.header);
            Assert.assertEquals(record.header.contentLength, writer.headerContentLength);
            Assert.assertEquals(0, writer.payloadWrittenTotal);

            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);

            Assert.assertEquals(record.header, writer.header);
            Assert.assertEquals(new Long(payload.length), record.header.contentLength);
            Assert.assertEquals(new Long(payload.length), writer.headerContentLength);
            Assert.assertEquals(payload.length, writer.payloadWrittenTotal);

            writer.closeRecord();

            Assert.assertNull(writer.header);
            Assert.assertNull(writer.headerContentLength);
            Assert.assertEquals(payload.length, writer.payloadWrittenTotal);

            writer.close();

            out.close();

            // debug
            //String tmpStr = new String(out.toByteArray());
            //System.out.print(tmpStr);

            /*
             * Test invalid closeRecord state.
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            try {
                writer.closeRecord();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_INIT, writer.state);

            Assert.assertNull(writer.header);
            Assert.assertNull(writer.headerContentLength);
            Assert.assertEquals(0, writer.payloadWrittenTotal);

            /*
             * Test header written back to back.
             */

            record = WarcRecord.createRecord(writer);
            record.header.addHeader("WARC-Type", "warcinfo");
            record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
            record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
            record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
            //record.header.addHeader("Content-Length", "483");
            record.header.addHeader("Content-Length", "0");
            record.header.addHeader("Content-Type", "application/warc-fields");

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            Assert.assertEquals(record.header, writer.header);
            Assert.assertEquals(new Long(0), record.header.contentLength);
            Assert.assertEquals(new Long(0), writer.headerContentLength);
            Assert.assertEquals(0, writer.payloadWrittenTotal);

            try {
                writer.writeHeader(record);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            Assert.assertEquals(record.header, writer.header);
            Assert.assertEquals(new Long(0), record.header.contentLength);
            Assert.assertEquals(new Long(0), writer.headerContentLength);
            Assert.assertEquals(0, writer.payloadWrittenTotal);

            /*
             * Test writing header after a payload with implicit record closing.
             */

            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);

            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);

            // Hack!
            writer.headerContentLength = (long) (payload.length * 2);

            Assert.assertEquals(record.header, writer.header);
            Assert.assertEquals(new Long(0), record.header.contentLength);
            Assert.assertEquals(new Long(payload.length * 2), writer.headerContentLength);
            Assert.assertEquals(payload.length * 2, writer.payloadWrittenTotal);

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            /*
             * Test double closeRecord().
             */

            writer.closeRecord();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);

            Assert.assertNull(writer.header);
            Assert.assertNull(writer.headerContentLength);
            Assert.assertEquals(0, writer.payloadWrittenTotal);

            writer.closeRecord();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);

            /*
             * Test double close().
             */

            writer.close();
            Assert.assertNull(writer.out);
            writer.close();
            Assert.assertNull(writer.out);

            /*
             * Test close() after writeHeader().
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            Assert.assertEquals(record.header, writer.header);
            Assert.assertEquals(new Long(0), record.header.contentLength);
            Assert.assertEquals(new Long(0), writer.headerContentLength);
            Assert.assertEquals(0, writer.payloadWrittenTotal);

            writer.close();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);
            Assert.assertNull(writer.out);

            /*
             * Test close() after streamPayload().
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);

            record.header.addHeader(WarcConstants.FN_CONTENT_LENGTH, payload.length, null);

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            Assert.assertEquals(record.header, writer.header);
            Assert.assertEquals(new Long(payload.length), record.header.contentLength);
            Assert.assertEquals(new Long(payload.length), writer.headerContentLength);
            Assert.assertEquals(0, writer.payloadWrittenTotal);

            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);

            Assert.assertEquals(record.header, writer.header);
            Assert.assertEquals(new Long(payload.length), record.header.contentLength);
            Assert.assertEquals(new Long(payload.length), writer.headerContentLength);
            Assert.assertEquals(payload.length, writer.payloadWrittenTotal);

            writer.close();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);
            Assert.assertNull(writer.out);

            Assert.assertNull(writer.header);
            Assert.assertNull(writer.headerContentLength);
            Assert.assertEquals(payload.length, writer.payloadWrittenTotal);

            /*
             * Test streamPayload() in wrong states.
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            try {
                in = new ByteArrayInputStream(payload);
                writer.streamPayload(in);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_INIT, writer.state);

            try {
                writer.writePayload(payload);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_INIT, writer.state);

            try {
                writer.writePayload(payload, 0, 10);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_INIT, writer.state);

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);
            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);
            writer.closeRecord();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);

            try {
                in = new ByteArrayInputStream(payload);
                writer.streamPayload(in);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);

            try {
                writer.writePayload(payload);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);

            try {
                writer.writePayload(payload, 0, 10);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);

            /*
             * Test header bytes written back to back.
             */

            writer.writeRawHeader(recordHeader, null);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);
            try {
                writer.writeRawHeader(recordHeader, null);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            /*
             * Test writing header bytes after a payload with implicit record closing.
             */

            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);
            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);

            // Hack!
            writer.headerContentLength = (long) (payload.length * 2);

            writer.writeRawHeader(recordHeader, null);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);
        } catch (IOException e) {
        }
    }

}
