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
public class TestArcRecord {

    @Test
    public void test_arcrecord() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArcWriter writer;
        ArcRecordBase record;
        byte[] recordHeader;
        ByteArrayInputStream in;
        byte[] payload;

        String mdData;
        try {
            out.reset();
            writer = ArcWriterFactory.getWriter(out, false);

            writer.setExceptionOnContentLengthMismatch(false);

            mdData = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n";
            mdData += "<arcmetadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:arc=\"http://archive.org/arc/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://archive.org/arc/1.0/\" xsi:schemaLocation=\"http://archive.org/arc/1.0/ http://www.archive.org/arc/1.0/arc.xsd\">\r\n";
            mdData += "<arc:software>Heritrix @VERSION@ http://crawler.archive.org</arc:software>\r\n";
            mdData += "<arc:hostname>blackbook</arc:hostname>\r\n";
            mdData += "<arc:ip>192.168.1.13</arc:ip>\r\n";
            mdData += "<dcterms:isPartOf>archive.org-shallow</dcterms:isPartOf>\r\n";
            mdData += "<dc:description>archive.org shallow</dc:description>\r\n";
            mdData += "<arc:operator>Admin</arc:operator>\r\n";
            mdData += "<ns0:date xmlns:ns0=\"http://purl.org/dc/elements/1.1/\" xsi:type=\"dcterms:W3CDTF\">2008-04-30T20:48:24+00:00</ns0:date>\r\n";
            mdData += "<arc:http-header-user-agent>Mozilla/5.0 (compatible; heritrix/1.14.0 +http://crawler.archive.org)</arc:http-header-user-agent>\r\n";
            mdData += "<arc:http-header-from>archive-crawler-agent@lists.sourceforge.net</arc:http-header-from>\r\n";
            mdData += "<arc:robots>classic</arc:robots>\r\n";
            mdData += "<dc:format>ARC file version 1.1</dc:format>\r\n";
            mdData += "<dcterms:conformsTo xsi:type=\"dcterms:URI\">http://www.archive.org/web/researcher/ArcFileFormat.php</dcterms:conformsTo>\r\n";
            mdData += "</arcmetadata>\r\n";
            payload = mdData.getBytes();

            record = ArcVersionBlock.create(writer);
            record.header.urlStr = "filedesc://BNF-inktomi_arc39.20011005200622.arc.gz";
            record.header.ipAddressStr = "0.0.0.0";
            record.header.archiveDateStr = "20011005200622";
            record.header.contentTypeStr = "text/plain";
            record.header.archiveLengthStr = "76";
            writer.writeHeader(record);

            writer.writePayload(payload);

            writer.closeRecord();

            writer.close();

            System.out.println(out);

            in = new ByteArrayInputStream(out.toByteArray());
            ArcReader reader = ArcReaderFactory.getReader(in, 1024);

            record = reader.getNextRecord();
            Assert.assertNotNull(record);

            record = reader.getNextRecord();
            // TODO wrong
            Assert.assertNotNull(record);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
