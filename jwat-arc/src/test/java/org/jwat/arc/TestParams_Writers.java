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
package org.jwat.arc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestParams_Writers {

    @Test
    public void test_params_writers() {
        ByteArrayOutputStream out;

        try {
            /*
             * ArcWriterUncompressed.
             */

            ArcWriter writer;
            out = null;
            byte[] headerArr = null;
            ArcRecordBase record = null;

            try {
                writer = new ArcWriterUncompressed(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = new ArcWriterUncompressed(null, 512);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            out = new ByteArrayOutputStream();
            try {
                writer = new ArcWriterUncompressed(out, -1);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = new ArcWriterUncompressed(out, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }

            writer = new ArcWriterUncompressed(out);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = new ArcWriterUncompressed(out, 512);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            try {
                writer.writeRawHeader(headerArr, null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writeRawHeader(new byte[1], -1L);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writeHeader(record);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.streamPayload(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writePayload(null);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                writer.writePayload(null, 0, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            writer.writeRawHeader(new byte[1], null);
            // Content-Length mismatch.
            try {
                writer.close();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            /*
             * ArcWriterCompressed.
             */

            out = null;

            try {
                writer = new ArcWriterCompressed(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = new ArcWriterCompressed(null, 512);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            out = new ByteArrayOutputStream();
            try {
                writer = new ArcWriterCompressed(out, -1);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = new ArcWriterCompressed(out, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }

            writer = new ArcWriterCompressed(out);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            writer = new ArcWriterCompressed(out, 512);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            try {
                writer.writeRawHeader(headerArr, null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writeRawHeader(new byte[1], -1L);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writeHeader(record);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.streamPayload(null);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                writer.writePayload(null);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                writer.writePayload(null, 0, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            writer.writeRawHeader(new byte[1], null);
            // Content-Length mismatch.
            try {
                writer.close();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }

            /*
             * ArcWriterFactory.
             */

            out = null;

            try {
                writer = ArcWriterFactory.getWriter(null, false);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriter(null, 512, false);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriterUncompressed(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriterUncompressed(null, 512);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriterCompressed(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriterCompressed(null, 512);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            out = new ByteArrayOutputStream();
            try {
                writer = ArcWriterFactory.getWriter(out, -1, false);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriter(out, 0, false);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriterUncompressed(out, -1);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriterUncompressed(out, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriterCompressed(out, -1);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = ArcWriterFactory.getWriterCompressed(out, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }

            writer = ArcWriterFactory.getWriter(out, false);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = ArcWriterFactory.getWriter(out, 512, false);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = ArcWriterFactory.getWriter(out, true);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            writer = ArcWriterFactory.getWriter(out, 512, true);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            writer = ArcWriterFactory.getWriterUncompressed(out);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = ArcWriterFactory.getWriterUncompressed(out, 512);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = ArcWriterFactory.getWriterCompressed(out);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            writer = ArcWriterFactory.getWriterCompressed(out, 512);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            ArcWriterFactory arcWriterFactory = new ArcWriterFactory();
            Assert.assertNotNull(arcWriterFactory);

            Assert.assertTrue(writer.bExceptionOnContentLengthMismatch);
            Assert.assertTrue(writer.exceptionOnContentLengthMismatch());
            writer.setExceptionOnContentLengthMismatch(false);
            Assert.assertFalse(writer.exceptionOnContentLengthMismatch());
            Assert.assertFalse(writer.bExceptionOnContentLengthMismatch);
            writer.setExceptionOnContentLengthMismatch(true);
            Assert.assertTrue(writer.bExceptionOnContentLengthMismatch);
            Assert.assertTrue(writer.exceptionOnContentLengthMismatch());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
