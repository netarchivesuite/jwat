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
public class TestWarcWriter {

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

            record = WarcRecord.createRecord(writer);
            record.header.addHeader("WARC-Type", "warcinfo");
            record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
            record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
            record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
            record.header.addHeader("Content-Length", "483");
            record.header.addHeader("Content-Type", "application/warc-fields");

            payload = "Welcome to dænemark!".getBytes("UTF-8");

            in = new ByteArrayInputStream(payload);

            recordHeader = writer.writeHeader(record);
            writer.streamPayload(in, payload.length);
            writer.closeRecord();
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

            /*
             * Test header written back to back.
             */

            record = WarcRecord.createRecord(writer);
            record.header.addHeader("WARC-Type", "warcinfo");
            record.header.addHeader("WARC-Record-ID", "<urn:uuid:35f02b38-eb19-4f0d-86e4-bfe95815069c>");
            record.header.addHeader("WARC-Date", "2008-04-30T20:48:25Z");
            record.header.addHeader("WARC-Filename", "IAH-20080430204825-00000-blackbook.warc.gz");
            record.header.addHeader("Content-Length", "483");
            record.header.addHeader("Content-Type", "application/warc-fields");

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);
            try {
                writer.writeHeader(record);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            /*
             * Test writing header after a payload with implicit record closing.
             */

            writer.streamPayload(in, payload.length);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);
            writer.streamPayload(in, payload.length);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            /*
             * Test double closeRecord().
             */

            writer.closeRecord();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);
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
            writer.close();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);
            Assert.assertNull(writer.out);

            /*
             * Test close() after streamPayload().
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);
            writer.streamPayload(in, payload.length);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);
            writer.close();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);
            Assert.assertNull(writer.out);

            /*
             * Test streamPayload() in wrong states.
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            try {
                writer.streamPayload(in, payload.length);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_INIT, writer.state);

            writer.writeHeader(record);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);
            writer.streamPayload(in, payload.length);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);
            writer.closeRecord();
            Assert.assertEquals(WarcWriter.S_RECORD_CLOSED, writer.state);

            /*
             * Test header bytes written back to back.
             */

            writer.writeHeader(recordHeader);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);
            try {
                writer.writeHeader(recordHeader);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);

            /*
             * Test writing header bytes after a payload with implicit record closing.
             */

            writer.streamPayload(in, payload.length);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);
            writer.streamPayload(in, payload.length);
            Assert.assertEquals(WarcWriter.S_PAYLOAD_WRITTEN, writer.state);

            writer.writeHeader(recordHeader);
            Assert.assertEquals(WarcWriter.S_HEADER_WRITTEN, writer.state);
        } catch (IOException e) {
        }
    }

}
