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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestArcWriter {

    @Test
    public void test_arcwriter_compressed() {
        test_arc_writer_states(true);
    }

    @Test
    public void test_arcwriter_uncompressed() {
        test_arc_writer_states(false);
    }

    public void test_arc_writer_states(boolean compress) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArcWriter writer;
        ArcRecordBase record;
        byte[] recordHeader;
        ByteArrayInputStream in;
        byte[] payload;

        try {
            out.reset();
            writer = ArcWriterFactory.getWriter(out, compress);

            //payload = "".getBytes

            record = ArcVersionBlock.create(writer);
            record.header.urlStr = "filedesc://BNF-inktomi_arc39.20011005200622.arc.gz";
            record.header.ipAddressStr = "0.0.0.0";
            record.header.archiveDateStr = "20011005200622";
            record.header.contentTypeStr = "text/plain";
            record.header.archiveLengthStr = "76";
            writer.writeHeader(record);

            record = ArcRecord.create(writer);
            record.header.urlStr = "http://cctr.umkc.edu:80/user/jbenz/tst.htm";
            record.header.ipAddressStr = "134.193.4.1";
            record.header.archiveDateStr = "19970417175710";
            record.header.contentTypeStr = "text/html";
            record.header.archiveLengthStr = "4270";
            //writer.writeHeader(record);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
