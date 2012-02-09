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
package org.jwat.arc;

/**
 * Defines validation error.
 *
 * @author lbihanic, selghissassi
 */
public class ArcValidationError {

    /** Supported error types {@link ArcErrorType}. */
    public final ArcErrorType error;

    /** Field name. */
    public final String field;

    /** Field value. */
    public final String value;

    /**
     * Creates new <code>ValidationError</code>.
     * @param error error type {@link ArcErrorType}.
     * @param field field name.
     * @param value field value.
     */
    public ArcValidationError(ArcErrorType error, String field, String value) {
        if (error == null) {
            throw new IllegalArgumentException("Parameter 'error' is null");
        }
        if ((field == null) || (field.length() == 0)) {
            throw new IllegalArgumentException(
                    "Parameter 'field' is either null or the empty string");
        }
        this.error = error;
        this.field = field;
        this.value = value;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append("error: ");
        builder.append(error);
        builder.append(", field: ");
        builder.append(field);
        if(value != null && value.length() != 0){
            builder.append(", value: ");
            builder.append(value);
        }
         builder.append(']');
        return builder.toString();
    }

}
