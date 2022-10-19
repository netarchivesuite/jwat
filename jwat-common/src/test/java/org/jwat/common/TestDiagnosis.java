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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestDiagnosis {

    @Test
    public void test_diagnosis() {
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

        dt = DiagnosisType.ERROR;
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

        dt = DiagnosisType.RECOMMENDED_MISSING;
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

        d = new Diagnosis(DiagnosisType.EMPTY, "Void");
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.EMPTY, d.type);
        Assert.assertEquals("Void", d.entity);
        Assert.assertEquals(0, d.information.length);

        messageArgs = d.getMessageArgs();
        Assert.assertEquals(1, messageArgs.length);
        Assert.assertEquals("Void", messageArgs[0]);

        d = new Diagnosis(DiagnosisType.UNKNOWN, "help", "me");
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.UNKNOWN, d.type);
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

        d = new Diagnosis(DiagnosisType.EMPTY, "entity", (String[])null);
        Assert.assertNotNull(d);
        d = new Diagnosis(DiagnosisType.EMPTY, "entity", new String[] {});
        Assert.assertNotNull(d);
        d = new Diagnosis(DiagnosisType.EMPTY, "entity", "info1");
        Assert.assertNotNull(d);
        d = new Diagnosis(DiagnosisType.EMPTY, "entity", "info1", "info2");
        Assert.assertNotNull(d);

        try {
            d = new Diagnosis(DiagnosisType.ERROR, "entity", (String[])null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        try {
            d = new Diagnosis(DiagnosisType.ERROR, "entity", new String[] {});
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {}
        d = new Diagnosis(DiagnosisType.ERROR, "entity", "info1");
        Assert.assertNotNull(d);
        d = new Diagnosis(DiagnosisType.ERROR, "entity", "info1", "info2");
        Assert.assertNotNull(d);

        /*
         * Diagnostics.
         */

        Diagnostics ds;
        Diagnostics ds2;

        ds = new Diagnostics();
        Assert.assertFalse(ds.hasErrors());
        Assert.assertFalse(ds.hasWarnings());
        Assert.assertEquals(0, ds.getErrors().size());
        Assert.assertEquals(0, ds.getWarnings().size());

        d = new Diagnosis(DiagnosisType.EMPTY, "Void");
        Assert.assertNotNull(d);
        ds.addError(d);
        d = new Diagnosis(DiagnosisType.UNKNOWN, "help", "me");
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
        Assert.assertEquals(DiagnosisType.EMPTY, d.type);
        Assert.assertEquals("Void", d.entity);
        Assert.assertEquals(0, d.information.length);

        d = ds.getWarnings().get(0);
        Assert.assertNotNull(d);
        str = d.toString();
        Assert.assertNotNull(str);
        Assert.assertEquals(DiagnosisType.UNKNOWN, d.type);
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

        /*
         * addAll.
         */

        ds = new Diagnostics();
        ds2 = new Diagnostics();

        Assert.assertFalse(ds.hasErrors());
        Assert.assertFalse(ds.hasWarnings());
        Assert.assertEquals(0, ds.getErrors().size());
        Assert.assertEquals(0, ds.getWarnings().size());

        Assert.assertFalse(ds2.hasErrors());
        Assert.assertFalse(ds2.hasWarnings());
        Assert.assertEquals(0, ds2.getErrors().size());
        Assert.assertEquals(0, ds2.getWarnings().size());

        d = new Diagnosis(DiagnosisType.ERROR, "e_one_entity", "e_one_info");
        Assert.assertNotNull(d);
        ds.addError(d);

        d = new Diagnosis(DiagnosisType.UNKNOWN, "w_one_entity", "w_one_info");
        Assert.assertNotNull(d);
        ds.addWarning(d);

        d = new Diagnosis(DiagnosisType.INVALID, "e_two_entity", "e_two_info");
        Assert.assertNotNull(d);
        ds2.addError(d);

        d = new Diagnosis(DiagnosisType.RESERVED, "w_two_entity", "w_two_info");
        Assert.assertNotNull(d);
        ds2.addWarning(d);

        Assert.assertTrue(ds.hasErrors());
        Assert.assertTrue(ds.hasWarnings());
        Assert.assertEquals(1, ds.getErrors().size());
        Assert.assertEquals(1, ds.getWarnings().size());

        Assert.assertTrue(ds2.hasErrors());
        Assert.assertTrue(ds2.hasWarnings());
        Assert.assertEquals(1, ds2.getErrors().size());
        Assert.assertEquals(1, ds2.getWarnings().size());

        ds.addAll(null);
        ds2.addAll(null);
        ds.addAll(ds);
        ds2.addAll(ds2);

        Assert.assertTrue(ds.hasErrors());
        Assert.assertTrue(ds.hasWarnings());
        Assert.assertEquals(1, ds.getErrors().size());
        Assert.assertEquals(1, ds.getWarnings().size());

        Assert.assertTrue(ds2.hasErrors());
        Assert.assertTrue(ds2.hasWarnings());
        Assert.assertEquals(1, ds2.getErrors().size());
        Assert.assertEquals(1, ds2.getWarnings().size());

        d = ds.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, d.type);
        Assert.assertEquals("e_one_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("e_one_info", d.information[0]);

        d = ds.getWarnings().get(0);
        Assert.assertEquals(DiagnosisType.UNKNOWN, d.type);
        Assert.assertEquals("w_one_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("w_one_info", d.information[0]);

        Assert.assertTrue(ds2.hasErrors());
        Assert.assertTrue(ds2.hasWarnings());
        Assert.assertEquals(1, ds2.getErrors().size());
        Assert.assertEquals(1, ds2.getWarnings().size());

        d = ds2.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.INVALID, d.type);
        Assert.assertEquals("e_two_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("e_two_info", d.information[0]);

        d = ds2.getWarnings().get(0);
        Assert.assertEquals(DiagnosisType.RESERVED, d.type);
        Assert.assertEquals("w_two_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("w_two_info", d.information[0]);

        ds.addAll(ds2);

        Assert.assertTrue(ds2.hasErrors());
        Assert.assertTrue(ds2.hasWarnings());
        Assert.assertEquals(1, ds2.getErrors().size());
        Assert.assertEquals(1, ds2.getWarnings().size());

        d = ds2.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.INVALID, d.type);
        Assert.assertEquals("e_two_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("e_two_info", d.information[0]);

        d = ds2.getWarnings().get(0);
        Assert.assertEquals(DiagnosisType.RESERVED, d.type);
        Assert.assertEquals("w_two_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("w_two_info", d.information[0]);

        Assert.assertTrue(ds.hasErrors());
        Assert.assertTrue(ds.hasWarnings());
        Assert.assertEquals(2, ds.getErrors().size());
        Assert.assertEquals(2, ds.getWarnings().size());

        d = ds.getErrors().get(0);
        Assert.assertEquals(DiagnosisType.ERROR, d.type);
        Assert.assertEquals("e_one_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("e_one_info", d.information[0]);

        d = ds.getWarnings().get(0);
        Assert.assertEquals(DiagnosisType.UNKNOWN, d.type);
        Assert.assertEquals("w_one_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("w_one_info", d.information[0]);

        d = ds.getErrors().get(1);
        Assert.assertEquals(DiagnosisType.INVALID, d.type);
        Assert.assertEquals("e_two_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("e_two_info", d.information[0]);

        d = ds.getWarnings().get(1);
        Assert.assertEquals(DiagnosisType.RESERVED, d.type);
        Assert.assertEquals("w_two_entity", d.entity);
        Assert.assertEquals(1, d.information.length);
        Assert.assertEquals("w_two_info", d.information[0]);

        /*
         * reset.
         */

        Assert.assertTrue(ds.hasErrors());
        Assert.assertTrue(ds.hasWarnings());
        Assert.assertEquals(2, ds.getErrors().size());
        Assert.assertEquals(2, ds.getWarnings().size());

        ds.reset();

        Assert.assertFalse(ds.hasErrors());
        Assert.assertFalse(ds.hasWarnings());
        Assert.assertEquals(0, ds.getErrors().size());
        Assert.assertEquals(0, ds.getWarnings().size());

        Assert.assertTrue(ds2.hasErrors());
        Assert.assertTrue(ds2.hasWarnings());
        Assert.assertEquals(1, ds2.getErrors().size());
        Assert.assertEquals(1, ds2.getWarnings().size());

        ds2.reset();

        Assert.assertFalse(ds2.hasErrors());
        Assert.assertFalse(ds2.hasWarnings());
        Assert.assertEquals(0, ds2.getErrors().size());
        Assert.assertEquals(0, ds2.getWarnings().size());
    }

    @Test
    public void test_digest_equals_hashcode() {
        Diagnosis d1;
        Diagnosis d2;
        String str = "42";

        /*
         * Equals.
         */

        d1 = new Diagnosis(DiagnosisType.EMPTY, "empty");
        d2 = new Diagnosis(DiagnosisType.EMPTY, "empty");

        Assert.assertEquals(d1, d2);
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d1 = new Diagnosis(DiagnosisType.INVALID, "empty", (String[]) null);
        d2 = new Diagnosis(DiagnosisType.INVALID, "empty", (String[]) null);

        Assert.assertEquals(d1, d2);
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d1 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "one");
        d2 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "one");

        Assert.assertEquals(d1, d2);
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d1 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "two");
        d2 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "two");

        Assert.assertEquals(d1, d2);
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d1 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "one", null);
        d2 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "one", null);

        Assert.assertEquals(d1, d2);
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        d1 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "one", "two");
        d2 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "one", "two");

        Assert.assertEquals(d1, d2);
        Assert.assertEquals(d1.hashCode(), d2.hashCode());

        /*
         * Nulls.
         */

        Assert.assertFalse(d1.equals(null));
        Assert.assertFalse(d2.equals(null));
        Assert.assertFalse(d1.equals(str));
        Assert.assertFalse(d2.equals(str));

        /*
         * Different.
         */

        d1 = new Diagnosis(DiagnosisType.INVALID, "empty");
        d2 = new Diagnosis(DiagnosisType.EMPTY, "empty");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.INVALID, "empty");
        d2 = new Diagnosis(DiagnosisType.INVALID, "error");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty", (String[]) null);
        d2 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty", "info");
        d2 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty");
        d2 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty", "info");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty");
        d2 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty", "info");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));
        d1 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty", "info");
        d2 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty", (String[]) null);

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty", (String[]) null);
        d2 = new Diagnosis(DiagnosisType.RECOMMENDED_MISSING, "empty", "info");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "ofni");
        d2 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "info");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "info", null);
        d2 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "info", "ofni");

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));

        d1 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "info", "ofni");
        d2 = new Diagnosis(DiagnosisType.UNKNOWN, "empty", "info", null);

        Assert.assertFalse(d1.equals(d2));
        Assert.assertThat(d1.hashCode(), is(not(equalTo(d2.hashCode()))));
    }

}
