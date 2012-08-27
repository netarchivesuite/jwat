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

import junit.framework.Assert;

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

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
            /*
             * V1.1 and v1 block desc.
             */
            bytes = ("1 1 InternetArchive\n" + ArcConstants.VERSION_1_BLOCK_DEF + "\n").getBytes();
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

            tmpStr = header.toString();
            Assert.assertNotNull(tmpStr);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
