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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class used to keep track of a collection of error and warning diagnosis
 * objects.
 *
 * @param <T> diagnosis class stored in this diagnostics instance.
 *
 * @author nicl
 */
public class Diagnostics<T> {

    /** List of error diagnoses. */
    protected List<T> errors = new LinkedList<T>();

    /** List of warning diagnoses. */
    protected List<T> warnings = new LinkedList<T>();

    /**
     * Does collection have an error diagnosis.
     * @return boolean indicating the presence of an error diagnosis
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Does collection have a warning diagnosis.
     * @return boolean indicating the presence of a warning diagnosis
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Add all errors/warnings from supplied object to this one.
     * @param diagnostics other diagnostics with errors/warnings
     */
    public void addAll(Diagnostics<T> diagnostics) {
        errors.addAll(diagnostics.errors);
        warnings.addAll(diagnostics.warnings);
    }

    /**
     * Add errors diagnosis to the collection.
     * @param d error diagnosis
     */
    public void addError(T d) {
        errors.add(d);
    }

    /**
     * Add warning diagnosis to the collection.
     * @param d warning diagnosis
     */
    public void addWarning(T d) {
        warnings.add(d);
    }

    /**
     * Returns unmodifiable list of error diagnoses.
     * @return unmodifiable list of error diagnoses
     */
    public List<T> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns unmodifiable list of warning diagnoses.
     * @return unmodifiable list of warning diagnoses
     */
    public List<T> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

}
