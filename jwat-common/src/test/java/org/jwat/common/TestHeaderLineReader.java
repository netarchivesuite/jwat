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
package org.jwat.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestHeaderLineReader {

    @Test
    public void test_headerlinereader_report_error() {
        Diagnosis diagnosis;

        int bf_errors = 0;
        Diagnostics<Diagnosis> diagnostics = null;
        try {
            HeaderLineReader.report_error(bf_errors, diagnostics);
        } catch (IllegalArgumentException e) {
        }

        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertFalse(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(0, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());

        bf_errors = HeaderLineReader.E_BIT_EOF;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Unexpected EOF", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_MISPLACED_CR;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Misplaced CR", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_MISSING_CR;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Missing CR", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_EXCESSIVE_CR;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Excessive CR", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_INVALID_UTF8_ENCODING;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Invalid UTF-8 encoded character", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_INVALID_US_ASCII_CHAR;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Invalid US-ASCII character", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_INVALID_CONTROL_CHAR;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Invalid control character", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_INVALID_SEPARATOR_CHAR;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Invalid separator character", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_MISSING_QUOTE;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Missing quote character", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_MISSING_QUOTED_PAIR_CHAR;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Missing quoted pair character", diagnosis.information[0]);

        bf_errors = HeaderLineReader.E_BIT_INVALID_QUOTED_PAIR_CHAR;
        diagnostics = new Diagnostics<Diagnosis>();
        HeaderLineReader.report_error(bf_errors, diagnostics);

        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertFalse(diagnostics.hasWarnings());
        Assert.assertEquals(1, diagnostics.getErrors().size());
        Assert.assertEquals(0, diagnostics.getWarnings().size());
        diagnosis = diagnostics.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, diagnosis.type);
        Assert.assertEquals("header/line", diagnosis.entity);
        Assert.assertEquals("Invalid quoted pair character", diagnosis.information[0]);
    }

}
