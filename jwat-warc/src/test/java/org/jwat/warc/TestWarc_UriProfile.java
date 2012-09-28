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
import org.jwat.common.UriProfile;

@RunWith(JUnit4.class)
public class TestWarc_UriProfile {

    @Test
    public void test_warcreader_uriprofile() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WarcReader reader;
        WarcWriter writer;
        WarcRecord record;

        try {
            reader = WarcReaderFactory.getReaderUncompressed();
            Assert.assertEquals(UriProfile.RFC3986, reader.warcTargetUriProfile);
            Assert.assertEquals(UriProfile.RFC3986, reader.uriProfile);
            Assert.assertEquals(reader.warcTargetUriProfile, reader.getWarcTargetUriProfile());
            Assert.assertEquals(reader.uriProfile, reader.getUriProfile());
            reader.setWarcTargerUriProfile(UriProfile.RFC3986_ABS_16BIT);
            reader.setUriProfile(UriProfile.RFC3986_ABS_16BIT_LAX);
            Assert.assertEquals(UriProfile.RFC3986_ABS_16BIT, reader.warcTargetUriProfile);
            Assert.assertEquals(UriProfile.RFC3986_ABS_16BIT_LAX, reader.uriProfile);
            Assert.assertEquals(reader.warcTargetUriProfile, reader.getWarcTargetUriProfile());
            Assert.assertEquals(reader.uriProfile, reader.getUriProfile());
            reader.setWarcTargerUriProfile(null);
            reader.setUriProfile(null);
            Assert.assertEquals(UriProfile.RFC3986, reader.warcTargetUriProfile);
            Assert.assertEquals(UriProfile.RFC3986, reader.uriProfile);
            Assert.assertEquals(reader.warcTargetUriProfile, reader.getWarcTargetUriProfile());
            Assert.assertEquals(reader.uriProfile, reader.getUriProfile());

            writer = WarcWriterFactory.getWriter(out, 8192, false);
            Assert.assertEquals(UriProfile.RFC3986, writer.warcTargetUriProfile);
            Assert.assertEquals(UriProfile.RFC3986, writer.uriProfile);
            Assert.assertEquals(writer.warcTargetUriProfile, writer.getWarcTargetUriProfile());
            Assert.assertEquals(writer.uriProfile, writer.getUriProfile());
            writer.setWarcTargerUriProfile(UriProfile.RFC3986_ABS_16BIT);
            writer.setUriProfile(UriProfile.RFC3986_ABS_16BIT_LAX);
            Assert.assertEquals(UriProfile.RFC3986_ABS_16BIT, writer.warcTargetUriProfile);
            Assert.assertEquals(UriProfile.RFC3986_ABS_16BIT_LAX, writer.uriProfile);
            Assert.assertEquals(writer.warcTargetUriProfile, writer.getWarcTargetUriProfile());
            Assert.assertEquals(writer.uriProfile, writer.getUriProfile());
            writer.setWarcTargerUriProfile(null);
            writer.setUriProfile(null);
            Assert.assertEquals(UriProfile.RFC3986, writer.warcTargetUriProfile);
            Assert.assertEquals(UriProfile.RFC3986, writer.uriProfile);
            Assert.assertEquals(writer.warcTargetUriProfile, writer.getWarcTargetUriProfile());
            Assert.assertEquals(writer.uriProfile, writer.getUriProfile());

            out.reset();
            writer = WarcWriterFactory.getWriter(out, 8192, false);

            record = WarcRecord.createRecord(writer);
            record.header.addHeader(WarcConstants.FN_WARC_TYPE, "response");
            record.header.addHeader(WarcConstants.FN_WARC_TARGET_URI, "http://parolesdejeunes.free.fr/");
            record.header.addHeader(WarcConstants.FN_WARC_DATE, "2010-06-23T13:33:18Z");
            record.header.addHeader(WarcConstants.FN_WARC_IP_ADDRESS, "172.20.10.12");
            record.header.addHeader(WarcConstants.FN_WARC_RECORD_ID, "urn:uuid:909dc94b-8bef-4c23-927a-19ed107fa80e");
            record.header.addHeader(WarcConstants.FN_CONTENT_TYPE, "application/binary");
            record.header.addHeader(WarcConstants.FN_CONTENT_LENGTH, 0L, null);

            writer.writeHeader(record);
            writer.closeRecord();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
