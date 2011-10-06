/**
 * JHOVE2 - Next-generation architecture for format-aware characterization
 *
 * Copyright (c) 2009 by The Regents of the University of California,
 * Ithaka Harbors, Inc., and The Board of Trustees of the Leland Stanford
 * Junior University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * o Neither the name of the University of California/California Digital
 *   Library, Ithaka Harbors/Portico, or Stanford University, nor the names of
 *   its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jhove2.module.format.arc;

import java.util.HashMap;
import java.util.Map;

/**
 * Field validator utility class.
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
        FieldValidator fd = new FieldValidator();
        fd.fieldNames = fieldNames;
        for (int i=0; i<fieldNames.length; ++i) {
            fd.fieldIdxMap.put(fieldNames[i], i);
        }
        return fd;
    }

    /**
     * Checks to if the index is out of bounds in the give array and also
     * if the value if null or empty.
     * @param array source value array
     * @param idx desired index into array
     * @return value index in array or null
     */
    public static String getArrayValue(String[] array, int idx) {
        return ((array.length > idx) && (array[idx] != null) &&
                (array[idx].length() != 0))? array[idx] : null;
    }

    /*
    private final Map<String,String> fields = new TreeMap<String,String>();

    public String getField(String name) {
        String v = this.fields.get(name);
        if ((v != null) && ((v.length() == 0) || ("-".equals(v)))) {
            v = null;
        }
        return v;
    }

    private String[] parse(String data, String[] fields,
                                        Map<String,String> target) {
        String[] elts = data.split(" ", -1);
        for (int i=0, max=Math.min(fields.length, elts.length);
                                                            i<max; i++) {
            target.put(fields[i], elts[i]);
        }
        return elts;
    }
    */

}
