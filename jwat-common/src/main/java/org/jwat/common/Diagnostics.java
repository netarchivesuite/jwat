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

/**
 * Class used to keep track of a collection of error and warning diagnosis objects.
 *
 * @author nicl
 */
public class Diagnostics extends DiagnosticsGeneric<Diagnosis> {

    /**
     * Add an error diagnosis of the given type on a specific entity with
     * optional extra information. The information varies according to the
     * diagnosis type.
     * @param type diagnosis type
     * @param entity entity examined
     * @param information optional extra information
     */
    public void addError(DiagnosisType type, String entity, String... information) {
        addError(new Diagnosis(type, entity, information));
    }

    /**
     * Add a warning diagnosis of the given type on a specific entity with
     * optional extra information. The information varies according to the
     * diagnosis type.
     * @param type diagnosis type
     * @param entity entity examined
     * @param information optional extra information
     */
    public void addWarning(DiagnosisType type, String entity, String... information) {
        addWarning(new Diagnosis(type, entity, information));
    }

}
