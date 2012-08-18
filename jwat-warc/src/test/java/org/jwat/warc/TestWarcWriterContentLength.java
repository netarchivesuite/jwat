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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;

@RunWith(JUnit4.class)
public class TestWarcWriterContentLength {

    @Test
    public void test_warcwriter_compressed() {
        test_warcwriter_contentlength(true);
    }

    @Test
    public void test_warcwriter_uncompressed() {
        test_warcwriter_contentlength(false);
    }

    public void test_warcwriter_contentlength(boolean compress) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WarcWriter writer;
        WarcRecord record;
        byte[] recordHeader;
        ByteArrayInputStream in;
        byte[] payload;
        Diagnosis diagnosis;
        try {
            payload = "Welcome to dænemark!".getBytes("UTF-8");

            /*
             * Exceptions enabled.
             * Write Header.
             * streamPayload().
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(true);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            recordHeader = writer.writeHeader(record);
            try {
                writer.closeRecord();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(1, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());
            diagnosis = record.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.ERROR_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(true);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            record.header.addHeader(WarcConstants.FN_CONTENT_LENGTH, 1024L, null);
            writer.writeHeader(record);
            try {
                writer.closeRecord();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(1, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());
            diagnosis = record.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.INVALID_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(true);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            record.header.addHeader(WarcConstants.FN_CONTENT_LENGTH, 1024L, null);
            writer.writeHeader(record);
            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            try {
                writer.closeRecord();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(1, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());
            diagnosis = record.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.INVALID_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(true);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            record.header.addHeader(WarcConstants.FN_CONTENT_LENGTH, payload.length, null);
            writer.writeHeader(record);
            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            writer.closeRecord();

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(0, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            /*
             * Exceptions enabled.
             * Write HeaderBytes.
             * writePayload().
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(true);

            writer.writeRawHeader(recordHeader, null);
            try {
                writer.closeRecord();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(0, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());

            Assert.assertTrue(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(1, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());
            diagnosis = writer.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.ERROR_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(true);

            writer.writeRawHeader(recordHeader, 1024L);
            writer.writePayload(payload, 0, 10);
            writer.writePayload(payload, 10, payload.length - 10);
            try {
                writer.closeRecord();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(0, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());

            Assert.assertTrue(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(1, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());
            diagnosis = writer.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.INVALID_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(true);

            writer.writeRawHeader(recordHeader, (long)payload.length * 2);
            writer.writePayload(payload);
            writer.writePayload(payload);
            writer.closeRecord();

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(0, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            /*
             * Exceptions disabled.
             * Write Header.
             * streamPayload().
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            writer.writeHeader(record);
            writer.closeRecord();

            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(1, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());
            diagnosis = record.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.ERROR_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            record.header.addHeader(WarcConstants.FN_CONTENT_LENGTH, 1024L, null);
            writer.writeHeader(record);
            writer.closeRecord();

            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(1, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());
            diagnosis = record.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.INVALID_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            record.header.addHeader(WarcConstants.FN_CONTENT_LENGTH, 1024L, null);
            writer.writeHeader(record);
            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            writer.closeRecord();

            Assert.assertTrue(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(1, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());
            diagnosis = record.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.INVALID_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            record.header.addHeader(WarcConstants.FN_CONTENT_LENGTH, payload.length, null);
            writer.writeHeader(record);
            in = new ByteArrayInputStream(payload);
            writer.streamPayload(in);
            writer.closeRecord();

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(0, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());

            /*
             * Exceptions disabled.
             * Write HeaderBytes.
             * writePayload().
             */

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            writer.writeRawHeader(recordHeader, null);
            writer.closeRecord();

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(0, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());

            Assert.assertTrue(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(1, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());
            diagnosis = writer.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.ERROR_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            writer.writeRawHeader(recordHeader, 1024L);
            writer.writePayload(payload, 0, 10);
            writer.writePayload(payload, 10, payload.length - 10);
            writer.closeRecord();

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(0, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());

            Assert.assertTrue(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(1, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());
            diagnosis = writer.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.INVALID_EXPECTED, diagnosis.type);
            Assert.assertEquals("'" + WarcConstants.FN_CONTENT_LENGTH + "' header", diagnosis.entity);

            out.reset();
            writer = WarcWriterFactory.getWriter(out, compress);
            writer.setExceptionOnContentLengthMismatch(false);

            writer.writeRawHeader(recordHeader, (long)payload.length * 2);
            writer.writePayload(payload);
            writer.writePayload(payload);
            writer.closeRecord();

            Assert.assertFalse(record.diagnostics.hasErrors());
            Assert.assertFalse(record.diagnostics.hasWarnings());
            Assert.assertEquals(0, record.diagnostics.getErrors().size());
            Assert.assertEquals(0, record.diagnostics.getWarnings().size());

            Assert.assertFalse(writer.diagnostics.hasErrors());
            Assert.assertFalse(writer.diagnostics.hasWarnings());
            Assert.assertEquals(0, writer.diagnostics.getErrors().size());
            Assert.assertEquals(0, writer.diagnostics.getWarnings().size());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
