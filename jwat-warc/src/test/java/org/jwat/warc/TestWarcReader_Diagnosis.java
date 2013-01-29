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
public class TestWarcReader_Diagnosis {

    @Test
    public void test_warcreader_diagnosis() {
        Diagnosis d;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("\r\n".getBytes());
            out.write("The monkeys are a coming!\r\n".getBytes());
            out.write("\r\n".getBytes());
            out.write("Donkey Kong country...\r\n".getBytes());
            out.write("\r\n".getBytes());

            // Save testfile.
            GenerateWarcTestFiles.saveTestWarcReader_Diagnosis(out.toByteArray());

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            WarcReader reader = WarcReaderFactory.getReaderUncompressed();

            WarcRecord record = reader.getNextRecordFrom(in, 0);

            Assert.assertNull(record);

            Assert.assertTrue(reader.diagnostics.hasErrors());
            Assert.assertFalse(reader.diagnostics.hasWarnings());
            Assert.assertEquals(3, reader.diagnostics.getErrors().size());
            Assert.assertEquals(0, reader.diagnostics.getWarnings().size());

            d = reader.diagnostics.getErrors().get(0);
            Assert.assertEquals(DiagnosisType.INVALID, d.type);
            Assert.assertEquals("Data before WARC version", d.entity);
            Assert.assertEquals(0, d.information.length);

            d = reader.diagnostics.getErrors().get(1);
            Assert.assertEquals(DiagnosisType.INVALID, d.type);
            Assert.assertEquals("Empty lines before WARC version", d.entity);
            Assert.assertEquals(0, d.information.length);

            d = reader.diagnostics.getErrors().get(2);
            Assert.assertEquals(DiagnosisType.ERROR_EXPECTED, d.type);
            Assert.assertEquals("WARC file", d.entity);
            Assert.assertEquals(1, d.information.length);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
