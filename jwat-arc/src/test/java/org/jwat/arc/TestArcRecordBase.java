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
import java.net.InetAddress;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ContentType;

@RunWith(JUnit4.class)
public class TestArcRecordBase {

    @Test
    public void test_arcrecordbase() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String tmpStr;
        try {
            out.reset();
            out.write("2 0 InternetArchive".getBytes());
            out.write("\n".getBytes());
            out.write(ArcConstants.VERSION_2_BLOCK_DEF.getBytes());
            out.write("\n".getBytes());
            byte[] versionblock = out.toByteArray();

            out.reset();
            out.write("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz 192.168.1.2 20120712144000 text/htlm 200 checksum location 1234 filename ".getBytes());
            out.write(Integer.toString(versionblock.length).getBytes());
            out.write("\n".getBytes());
            byte[] recordline = out.toByteArray();

            out.reset();
            out.write(recordline);
            out.write(versionblock);
            out.write("\n".getBytes());

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            ArcReader reader = ArcReaderFactory.getReader(in);

            ArcRecordBase record = reader.getNextRecord();
            Assert.assertNotNull(record);
            ArcHeader header = record.header;
            Assert.assertNotNull(header);

            Assert.assertEquals(2, header.parsedFieldsVersion);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            Assert.assertEquals("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz", header.urlStr);
            Assert.assertEquals("192.168.1.2", header.ipAddressStr);
            Assert.assertEquals("20120712144000", header.archiveDateStr);
            Assert.assertEquals("text/htlm", header.contentTypeStr);
            Assert.assertEquals("200", header.resultCodeStr);
            Assert.assertEquals("checksum", header.checksumStr);
            Assert.assertEquals("location", header.locationStr);
            Assert.assertEquals("1234", header.offsetStr);
            Assert.assertEquals("filename", header.filenameStr);
            Assert.assertEquals(Integer.toString(versionblock.length), header.archiveLengthStr);

            Assert.assertEquals(URI.create("filedesc://BNF-inktomi_arc39.20011005200622.arc.gz"), header.urlUri);
            Assert.assertEquals("filedesc", header.urlScheme);
            Assert.assertEquals(InetAddress.getByName("192.168.1.2"), header.inetAddress);
            Assert.assertEquals(ArcDateParser.getDate("20120712144000"), header.archiveDate);
            Assert.assertEquals(ContentType.parseContentType("text/htlm"), header.contentType);
            Assert.assertEquals(new Integer(200), header.resultCode);
            Assert.assertEquals(new Long(1234), header.offset);
            Assert.assertEquals(new Long(versionblock.length), header.archiveLength);

            Assert.assertEquals(header.urlStr, record.getUrlStr());
            Assert.assertEquals(header.ipAddressStr, record.getIpAddress());
            Assert.assertEquals(header.archiveDateStr, record.getArchiveDateStr());
            Assert.assertEquals(header.contentTypeStr, record.getContentTypeStr());
            Assert.assertEquals(header.resultCodeStr, record.getResultCodeStr());
            Assert.assertEquals(header.checksumStr, record.getChecksum());
            Assert.assertEquals(header.locationStr, record.getLocation());
            Assert.assertEquals(header.offsetStr, record.getOffsetStr());
            Assert.assertEquals(header.filenameStr, record.getFileName());
            Assert.assertEquals(header.archiveLengthStr, record.getArchiveLengthStr());

            Assert.assertEquals(header.urlUri, record.getUrl());
            Assert.assertEquals(header.urlScheme, record.getScheme());
            Assert.assertEquals(header.inetAddress, record.getInetAddress());
            Assert.assertEquals(header.archiveDate, record.getArchiveDate());
            Assert.assertEquals(header.contentType, record.getContentType());
            Assert.assertEquals(header.resultCode, record.getResultCode());
            Assert.assertEquals(header.offset, record.getOffset());
            Assert.assertEquals(header.archiveLength, record.getArchiveLength());

            Assert.assertFalse(header.diagnostics.hasWarnings());
            Assert.assertFalse(header.diagnostics.hasErrors());

            record.close();
            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
