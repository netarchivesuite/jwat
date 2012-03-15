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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestDiagnosis {

    @Test
    public void test() {
        String str;

        /*
         * DiagnosisType.
         */

        DiagnosisType dt;

        dt = DiagnosisType.DUPLICATE;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.EMPTY;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.ERROR_EXPECTED;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.INVALID;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.INVALID_DATA;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.INVALID_ENCODING;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.INVALID_EXPECTED;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.RECOMMENDED;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.REQUIRED_INVALID;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.RESERVED;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.UNDESIRED_DATA;
        str = dt.toString();
        Assert.assertNotNull(str);

        dt = DiagnosisType.UNKNOWN;
        str = dt.toString();
        Assert.assertNotNull(str);

        /*
         * Diagnosis.
         */

        Diagnosis d;
        Object[] messageArgs;

        try {
            new Diagnosis(null, null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            new Diagnosis(DiagnosisType.INVALID, null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        d = new Diagnosis(DiagnosisType.UNKNOWN, "Void");
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.UNKNOWN, d.type);
        Assert.assertEquals("Void", d.entity);
        Assert.assertEquals(0, d.information.length);

        messageArgs = d.getMessageArgs();
        Assert.assertEquals(1, messageArgs.length);
        Assert.assertEquals("Void", messageArgs[0]);

        d = new Diagnosis(DiagnosisType.RECOMMENDED, "help", "me");
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.RECOMMENDED, d.type);
        Assert.assertEquals("help", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("me", d.information[0]);

        messageArgs = d.getMessageArgs();
        Assert.assertEquals(2, messageArgs.length);
        Assert.assertEquals("help", messageArgs[0]);
        Assert.assertEquals("me", messageArgs[1]);

        d = new Diagnosis(DiagnosisType.RECOMMENDED, "help", "me", "NOW!");
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.RECOMMENDED, d.type);
        Assert.assertEquals("help", d.entity);
        Assert.assertEquals(2, d.information.length);
        Assert.assertEquals("me", d.information[0]);
        Assert.assertEquals("NOW!", d.information[1]);

        messageArgs = d.getMessageArgs();
        Assert.assertEquals(3, messageArgs.length);
        Assert.assertEquals("help", messageArgs[0]);
        Assert.assertEquals("me", messageArgs[1]);
        Assert.assertEquals("NOW!", messageArgs[2]);

        /*
         * Diagnostics.
         */

        Diagnostics<Diagnosis> ds;

        ds = new Diagnostics<Diagnosis>();
        Assert.assertFalse(ds.hasErrors());
        Assert.assertFalse(ds.hasWarnings());
        Assert.assertEquals(0, ds.getErrors().size());
        Assert.assertEquals(0, ds.getWarnings().size());

        d = new Diagnosis(DiagnosisType.UNKNOWN, "Void");
        Assert.assertNotNull(d);
        ds.addError(d);
        d = new Diagnosis(DiagnosisType.RECOMMENDED, "help", "me");
        Assert.assertNotNull(d);
        ds.addWarning(d);

        Assert.assertTrue(ds.hasErrors());
        Assert.assertTrue(ds.hasWarnings());
        Assert.assertEquals(1, ds.getErrors().size());
        Assert.assertEquals(1, ds.getWarnings().size());

        d = ds.getErrors().get(0);
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.UNKNOWN, d.type);
        Assert.assertEquals("Void", d.entity);
        Assert.assertEquals(0, d.information.length);

        d = ds.getWarnings().get(0);
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.RECOMMENDED, d.type);
        Assert.assertEquals("help", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("me", d.information[0]);

        d = new Diagnosis(DiagnosisType.RECOMMENDED, "help", "me", "NOW!");
        Assert.assertNotNull(d);
        ds.addWarning(d);

        Assert.assertTrue(ds.hasErrors());
        Assert.assertTrue(ds.hasWarnings());
        Assert.assertEquals(1, ds.getErrors().size());
        Assert.assertEquals(2, ds.getWarnings().size());

        d = ds.getWarnings().get(1);
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.RECOMMENDED, d.type);
        Assert.assertEquals("help", d.entity);
        Assert.assertEquals(2, d.information.length);
        Assert.assertEquals("me", d.information[0]);
        Assert.assertEquals("NOW!", d.information[1]);
    }
}
