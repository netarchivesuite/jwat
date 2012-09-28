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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWarcWriters_Params {

    @Test
    public void test_params_writers() {
        ByteArrayOutputStream out;

        try {
            /*
             * WarcWriterUncompressed.
             */

            WarcWriter writer;
            out = null;

            try {
                writer = new WarcWriterUncompressed(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = new WarcWriterUncompressed(null, 512);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            out = new ByteArrayOutputStream();
            try {
                writer = new WarcWriterUncompressed(out, -1);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = new WarcWriterUncompressed(out, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }

            writer = new WarcWriterUncompressed(out);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = new WarcWriterUncompressed(out, 512);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            try {
                writer.writeRawHeader(null, null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writeRawHeader(new byte[1], -1L);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writeHeader(null);
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
             * WarcWriterCompressed.
             */

            out = null;

            try {
                writer = new WarcWriterCompressed(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = new WarcWriterCompressed(null, 512);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            out = new ByteArrayOutputStream();
            try {
                writer = new WarcWriterCompressed(out, -1);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = new WarcWriterCompressed(out, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }

            writer = new WarcWriterCompressed(out);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            writer = new WarcWriterCompressed(out, 512);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            try {
                writer.writeRawHeader(null, null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writeRawHeader(new byte[1], -1L);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer.writeHeader(null);
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
             * WarcWriterFactory.
             */

            out = null;

            try {
                writer = WarcWriterFactory.getWriter(null, false);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriter(null, 512, false);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriterUncompressed(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriterUncompressed(null, 512);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriterCompressed(null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriterCompressed(null, 512);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            out = new ByteArrayOutputStream();
            try {
                writer = WarcWriterFactory.getWriter(out, -1, false);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriter(out, 0, false);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriterUncompressed(out, -1);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriterUncompressed(out, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriterCompressed(out, -1);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                writer = WarcWriterFactory.getWriterCompressed(out, 0);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }

            writer = WarcWriterFactory.getWriter(out, false);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = WarcWriterFactory.getWriter(out, 512, false);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = WarcWriterFactory.getWriter(out, true);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            writer = WarcWriterFactory.getWriter(out, 512, true);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            writer = WarcWriterFactory.getWriterUncompressed(out);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = WarcWriterFactory.getWriterUncompressed(out, 512);
            Assert.assertNotNull(writer);
            Assert.assertFalse(writer.isCompressed());

            writer = WarcWriterFactory.getWriterCompressed(out);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            writer = WarcWriterFactory.getWriterCompressed(out, 512);
            Assert.assertNotNull(writer);
            Assert.assertTrue(writer.isCompressed());

            WarcWriterFactory warcWriterFactory = new WarcWriterFactory();
            Assert.assertNotNull(warcWriterFactory);

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
