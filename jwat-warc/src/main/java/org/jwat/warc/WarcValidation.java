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

import org.jwat.common.DiagnosisType;
import org.jwat.common.Diagnostics;
import org.jwat.common.TreeOfIntMapper;
import org.jwat.warc.WarcValidatorFactories.WarcValidatorFactory;

/**
 * Central version dependent WARC validation class.
 * Has the factories for the different validator factories.
 * Also used to get version relevant validators based on a WARC header.
 *
 * @author nicl
 */
public class WarcValidation {

    protected WarcValidatorFactories factories;

    protected TreeOfIntMapper<WarcValidatorBase> treeOfValidators;

    /**
     * Construct a validation object to hold validator factories and validator instances.
     */
    public WarcValidation() {
        this.factories = new WarcValidatorFactories();
        treeOfValidators = new TreeOfIntMapper<>();
    }

    /**
     * Returns a WARC validator that can be used for the supplied WARC header.
     * @param warcHeader WARC header for which we need a validator
     * @param diagnostics <code>Diagnostics</code> object used to report problems
     * @return a WARC validator
     */
    public WarcValidatorBase getValidatorFor(WarcHeader warcHeader, Diagnostics diagnostics) {
        /*
        if (warcHeader.bVersionParsed && warcHeader.versionArr.length == 2) {
            switch (warcHeader.major) {
            case 1:
                if (warcHeader.minor == 0) {
                    warcHeader.bValidVersion = true;
                }
                if (warcHeader.minor == 1) {
                    warcHeader.bValidVersion = true;
                }
                break;
            case 0:
                switch (warcHeader.minor) {
                case 17:
                case 18:
                    warcHeader.bValidVersion = true;
                    break;
                }
                break;
            default:
                break;
            }
            if (!warcHeader.bValidVersion) {
                warcHeader.diagnostics.addError(new Diagnosis(DiagnosisType.UNKNOWN, "Magic version number", warcHeader.versionStr));
            }
        }
        else {
            warcHeader.diagnostics.addError(new Diagnosis(DiagnosisType.INVALID_DATA, "Magic Version string", warcHeader.versionStr));
        }
        */
        // TODO Add version class.
        WarcValidatorFactory factory;
        WarcValidatorBase validator = null;
        if (warcHeader.bVersionParsed && warcHeader.versionArr.length == 2) {
            validator = treeOfValidators.lookup(warcHeader.versionArr);
            if (validator != null) {
                warcHeader.bValidVersion = true;
            }
            else {
                factory = factories.get(warcHeader.versionArr);
                if (factory != null) {
                    validator = factory.getValidatorFor(warcHeader.versionArr);
                    warcHeader.bValidVersion = true;
                    treeOfValidators.add(validator, warcHeader.versionArr);
                }
                else {
                    validator = new WarcValidator11(warcHeader.versionArr);
                }
            }
            if (!warcHeader.bValidVersion) {
                diagnostics.addError(DiagnosisType.UNKNOWN, "Magic version number", warcHeader.versionStr);
            }
        }
        else {
            diagnostics.addError(DiagnosisType.INVALID_DATA, "Magic Version string", warcHeader.versionStr);
        }
        if (validator == null) {
            validator = new WarcValidator10(warcHeader.versionArr);
        }
        return validator;
    }

}
