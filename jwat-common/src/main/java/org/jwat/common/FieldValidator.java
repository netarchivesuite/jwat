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

import java.util.HashMap;
import java.util.Map;

/**
 * Field validation utility class.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class FieldValidator {

    /** Array of field names. */
    protected String[] fieldNames;

    /** <code>Map</code> of field name to field index mappings. */
    protected Map<String, Integer> fieldIdxMap = new HashMap<String, Integer>();

    /**
     * Protected constructor to ensure correct instantiation.
     */
    protected FieldValidator() {
    }

    /**
     * Prepares a validator for use on a given set of fields.
     * @param fieldNames array of field names.
     * @return validator used to look field by field name->index.
     */
    public static FieldValidator prepare(String[] fieldNames) {
        FieldValidator fv = new FieldValidator();
        fv.fieldNames = fieldNames;
        for (int i = 0; i < fieldNames.length; ++i) {
            fv.fieldIdxMap.put(fieldNames[i], i);
        }
        return fv;
    }

    /**
     * Checks to if the index is out of bounds in the give array and also
     * if the value if null or empty.
     * @param array source value array
     * @param idx desired index into array
     * @return value index in array or null
     */
    public static String getArrayValue(String[] array, int idx) {
        return ((array.length > idx) && (array[idx] != null)
                && (array[idx].length() != 0))? array[idx] : null;
    }

}
