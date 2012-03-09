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

        /*
        WarcValidationError wve;

        try {
            new WarcValidationError(null, null, null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        try {
            wve = new WarcValidationError(WarcErrorType.INVALID, null, null);
            Assert.fail("Exception expected!");
        } catch (IllegalArgumentException e) {
        }

        wve = new WarcValidationError(WarcErrorType.WANTED, "help", null);
        Assert.assertNotNull(wve);
        str = wve.toString();
        Assert.assertNotNull(str);

        wve = new WarcValidationError(WarcErrorType.WANTED, "help", "me");
        Assert.assertNotNull(wve);
        str = wve.toString();
        Assert.assertNotNull(str);
        */
    }
}
