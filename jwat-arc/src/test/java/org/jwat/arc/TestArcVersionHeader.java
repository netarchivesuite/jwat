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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;

@RunWith(JUnit4.class)
public class TestArcVersionHeader {

    @Test
    public void test_arcversionheader() {
        ByteArrayInputStream in;
        ByteCountingPushBackInputStream pbin;
        ArcFieldParsers fieldParsers = new ArcFieldParsers();
        Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();
        fieldParsers.diagnostics = diagnostics;
        ArcVersionHeader header;
        byte[] bytes;
        String digestAlgorithm = null;
        Long length = 0L;
        Object[][] expectedDiagnoses;
        String tmpStr;
        try {
            bytes = new byte[0];
            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);
            /*
             * Invalid parameters.
             */
            try {
                ArcVersionHeader.create(null, null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                ArcVersionHeader.create(ArcVersion.VERSION_1, null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                ArcVersionHeader.create(ArcVersion.VERSION_1, "");
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                header = ArcVersionHeader.processPayload(null, length, digestAlgorithm, fieldParsers, diagnostics);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                header = ArcVersionHeader.processPayload(pbin, -1, digestAlgorithm, fieldParsers, diagnostics);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, null, diagnostics);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            try {
                header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, null);
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
            /*
             * Empty stream.
             */
            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertFalse(header.isVersionValid);
            Assert.assertNull(header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * Single newline.
             */
            bytes = "\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertFalse(header.isVersionValid);
            Assert.assertNull(header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * 2 newlines.
             */
            bytes = "\n\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertFalse(header.isVersionValid);
            Assert.assertNull(header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * Newline and v1 block desc.
             */
            bytes = ("\n" + ArcConstants.VERSION_1_BLOCK_DEF + "\n").getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertFalse(header.isVersionValid);
            Assert.assertNull(header.version);
            Assert.assertTrue(header.isValidBlockdDesc);
            Assert.assertEquals(1, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * Newline and v2 block desc.
             */
            bytes = ("\n" + ArcConstants.VERSION_2_BLOCK_DEF + "\n").getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertFalse(header.isVersionValid);
            Assert.assertNull(header.version);
            Assert.assertTrue(header.isValidBlockdDesc);
            Assert.assertEquals(2, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.
             */
            bytes = "1 0 InternetArchive\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1, header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.1.
             */
            bytes = "1 1 InternetArchive\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1_1, header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V2.
             */
            bytes = "2 0 InternetArchive\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_2, header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * Vx.0.
             */
            bytes = "x 0 InternetArchive\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertFalse(header.isVersionValid);
            Assert.assertNull(header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_VERSION_NUMBER + "' value", 2},
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.x.
             */
            bytes = "1 x InternetArchive\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertFalse(header.isVersionValid);
            Assert.assertNull(header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID_EXPECTED, "'" + ArcConstants.FN_RESERVED + "' value", 2},
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.0 invalid block desc.
             */
            bytes = "1 0 InternetArchive\nAtomic Twister!\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1, header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.0 incomplete.
             */
            bytes = "1 0\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1, header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.0 too long.
             */
            bytes = "1 0 Internet Archive\n".getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1, header.version);
            Assert.assertFalse(header.isValidBlockdDesc);
            Assert.assertEquals(0, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1},
                    {DiagnosisType.ERROR, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V2.0 and v1 block desc.
             */
            bytes = ("2 0 InternetArchive\n" + ArcConstants.VERSION_1_BLOCK_DEF + "\n").getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_2, header.version);
            Assert.assertTrue(header.isValidBlockdDesc);
            Assert.assertEquals(1, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.0 and v2 block desc.
             */
            bytes = ("1 0 InternetArchive\n" + ArcConstants.VERSION_2_BLOCK_DEF + "\n").getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1, header.version);
            Assert.assertTrue(header.isValidBlockdDesc);
            Assert.assertEquals(2, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * Newline and v2 block desc.
             */
            bytes = ("1 1 InternetArchive\n" + ArcConstants.VERSION_2_BLOCK_DEF + "\n").getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertFalse(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1_1, header.version);
            Assert.assertTrue(header.isValidBlockdDesc);
            Assert.assertEquals(2, header.blockDescVersion);

            Assert.assertTrue(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 0);

            expectedDiagnoses = new Object[][] {
                    {DiagnosisType.INVALID, ArcConstants.ARC_VERSION_BLOCK, 1}
            };
            TestBaseUtils.compareDiagnoses(expectedDiagnoses, header.diagnostics.getErrors());
            diagnostics.reset();

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.0 and v1 block desc.
             */
            bytes = ("1 0 InternetArchive\n" + ArcConstants.VERSION_1_BLOCK_DEF + "\n").getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertTrue(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1, header.version);
            Assert.assertTrue(header.isValidBlockdDesc);
            Assert.assertEquals(1, header.blockDescVersion);

            Assert.assertFalse(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 10);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.1 and v1 block desc.
             */
            String mdData;
            mdData = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n";
            mdData += "<arcmetadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" +
                    " xmlns:dcterms=\"http://purl.org/dc/terms/\"" +
                    " xmlns:arc=\"http://archive.org/arc/1.0/\"" +
                    " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                    " xmlns=\"http://archive.org/arc/1.0/\"" +
                    " xsi:schemaLocation=\"http://archive.org/arc/1.0/ http://www.archive.org/arc/1.0/arc.xsd\">\r\n";
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
            bytes = ("1 1 InternetArchive\n" + ArcConstants.VERSION_1_BLOCK_DEF + "\n" + mdData).getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertTrue(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_1_1, header.version);
            Assert.assertTrue(header.isValidBlockdDesc);
            Assert.assertEquals(1, header.blockDescVersion);

            Assert.assertFalse(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 11);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);

            /*
             * V2.0 and v2 block desc.
             */
            bytes = ("2 0 InternetArchive\n" + ArcConstants.VERSION_2_BLOCK_DEF + "\n").getBytes();
            length = (long)bytes.length;

            in = new ByteArrayInputStream(bytes);
            pbin = new ByteCountingPushBackInputStream(in, 8192);

            header = ArcVersionHeader.processPayload(pbin, length, digestAlgorithm, fieldParsers, diagnostics);
            Assert.assertTrue(header.isValid());
            Assert.assertTrue(header.isVersionValid);
            Assert.assertEquals(ArcVersion.VERSION_2, header.version);
            Assert.assertTrue(header.isValidBlockdDesc);
            Assert.assertEquals(2, header.blockDescVersion);

            Assert.assertFalse(header.diagnostics.hasErrors());
            Assert.assertFalse(header.diagnostics.hasWarnings());

            // Save testfile.
            SaveArcTestFiles.saveTestArcVersionHeader(bytes,
                    !header.diagnostics.hasErrors() && !header.diagnostics.hasWarnings(), 20);

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }

    }

    // Reflecting new Enum's breaks JUnit.
    public void test_arcversionheader_reflection() {
        try {
            Constructor<?> con = ArcVersion.class.getDeclaredConstructors()[0];
            Method[] methods = con.getClass().getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equals("acquireConstructorAccessor")) {
                    m.setAccessible(true);
                    m.invoke(con, new Object[0]);
                }
            }
            Field[] fields = con.getClass().getDeclaredFields();
            Object ca = null;
            for (Field f : fields) {
                if (f.getName().equals("constructorAccessor")) {
                    f.setAccessible(true);
                    ca = f.get(con);
                }
            }
            Method m = ca.getClass().getMethod( "newInstance", new Class[] { Object[].class });
            m.setAccessible(true);
            ArcVersion v = (ArcVersion) m.invoke(ca, new Object[] {
                new Object[] {
                    "VERSION_3_2", new Integer(42), new Integer(3), new Integer(2), "", "" }
                }
            );

            //System.out.println(v.getClass() + ":" + v.name() + ":" + v.ordinal());
            //System.out.println(v.toString());
            //v = ArcVersion.VERSION_1_1;
            //System.out.println(v.getClass() + ":" + v.name() + ":" + v.ordinal());
            //System.out.println(v.toString());

            try {
                ArcVersionHeader.create(v, "netarkivet.dk");
                Assert.fail("Exception expected!");
            } catch (IllegalArgumentException e) {
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (SecurityException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }
}
