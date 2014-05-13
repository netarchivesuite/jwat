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

import java.util.List;

import junit.framework.Assert;

import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.HeaderLine;
import org.jwat.common.UriProfile;

public class TestWarcHeaderHelper {

    protected String[] headers;
    protected Object[][] cases;
    protected WarcHeader header;
    protected HeaderLine headerLine;
    protected List<Diagnosis> errors;
    protected List<Diagnosis> warnings;
    protected Diagnosis diagnosis;

    public WarcHeader getTestHeader() {
        WarcHeader header = new WarcHeader();
        header.uriProfile = UriProfile.RFC3986;
        header.warcTargetUriProfile = UriProfile.RFC3986;
        header.fieldParsers = new WarcFieldParsers();
        header.warcDateFormat = WarcDateParser.getDateFormat();
        header.diagnostics = new Diagnostics<Diagnosis>();
        header.fieldParsers.diagnostics = header.diagnostics;
        return header;
    }

    public abstract class TestHeaderCallback {
        public abstract void callback(WarcHeader header);
    }

    public void test_result(Object[][] expectedErrors, Object[][] expectedWarnings, TestHeaderCallback callback) {
        errors = header.diagnostics.getErrors();
        // debug
        //TestBaseUtils.printDiagnoses(errors);
        if (expectedErrors != null) {
            Assert.assertEquals(expectedErrors.length, errors.size());
            for (int k=0; k<expectedErrors.length; ++k) {
                diagnosis = errors.get(k);
                Assert.assertEquals((DiagnosisType)expectedErrors[k][0], diagnosis.type);
                Assert.assertEquals((String)expectedErrors[k][1], diagnosis.entity);
                Assert.assertEquals((Integer)expectedErrors[k][2], new Integer(diagnosis.information.length));
            }
        } else {
            Assert.assertEquals(0, errors.size());
        }
        warnings = header.diagnostics.getWarnings();
        // debug
        //TestBaseUtils.printDiagnoses(warnings);
        if (expectedWarnings != null) {
            Assert.assertEquals(expectedWarnings.length, warnings.size());
            for (int k=0; k<expectedWarnings.length; ++k) {
                diagnosis = warnings.get(k);
                Assert.assertEquals((DiagnosisType)expectedWarnings[k][0], diagnosis.type);
                Assert.assertEquals((String)expectedWarnings[k][1], diagnosis.entity);
                Assert.assertEquals((Integer)expectedWarnings[k][2], new Integer(diagnosis.information.length));
            }
        } else {
            Assert.assertEquals(0, warnings.size());
        }
        if (callback != null) {
            callback.callback(header);
        }
    }

}
