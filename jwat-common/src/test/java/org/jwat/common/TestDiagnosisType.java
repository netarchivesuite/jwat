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
public class TestDiagnosisType {

    @Test
    public void test_diagnosistype() {
        DiagnosisType[] diagnosisTypes = DiagnosisType.values();
        Assert.assertEquals( 15, diagnosisTypes.length);

        Assert.assertEquals(DiagnosisType.DUPLICATE, DiagnosisType.valueOf("DUPLICATE"));
        Assert.assertEquals(DiagnosisType.EMPTY, DiagnosisType.valueOf("EMPTY"));
        Assert.assertEquals(DiagnosisType.ERROR, DiagnosisType.valueOf("ERROR"));
        Assert.assertEquals(DiagnosisType.ERROR_EXPECTED, DiagnosisType.valueOf("ERROR_EXPECTED"));
        Assert.assertEquals(DiagnosisType.INVALID, DiagnosisType.valueOf("INVALID"));
        Assert.assertEquals(DiagnosisType.INVALID_DATA, DiagnosisType.valueOf("INVALID_DATA"));
        Assert.assertEquals(DiagnosisType.INVALID_ENCODING, DiagnosisType.valueOf("INVALID_ENCODING"));
        Assert.assertEquals(DiagnosisType.INVALID_EXPECTED, DiagnosisType.valueOf("INVALID_EXPECTED"));
        Assert.assertEquals(DiagnosisType.RECOMMENDED, DiagnosisType.valueOf("RECOMMENDED"));
        Assert.assertEquals(DiagnosisType.RECOMMENDED_MISSING, DiagnosisType.valueOf("RECOMMENDED_MISSING"));
        Assert.assertEquals(DiagnosisType.REQUIRED_INVALID, DiagnosisType.valueOf("REQUIRED_INVALID"));
        Assert.assertEquals(DiagnosisType.REQUIRED_MISSING, DiagnosisType.valueOf("REQUIRED_MISSING"));
        Assert.assertEquals(DiagnosisType.RESERVED, DiagnosisType.valueOf("RESERVED"));
        Assert.assertEquals(DiagnosisType.UNDESIRED_DATA, DiagnosisType.valueOf("UNDESIRED_DATA"));
        Assert.assertEquals(DiagnosisType.UNKNOWN, DiagnosisType.valueOf("UNKNOWN"));
    }

}
