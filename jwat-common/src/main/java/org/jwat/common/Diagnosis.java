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
 * Class representing a diagnosis which was found while validating an entity.
 *
 * @author nicl
 */
public class Diagnosis {

    /** Diagnosis type. */
    public final DiagnosisType type;

    /** Source entity on which the diagnosis is relevant. */
    public final String entity;

    /** Any optional information which may be relevant for the specific
     * diagnosis. */
    public final String[] information;

    /**
     * Construct a diagnosis instance given the type, entity and optional
     * information.
     * @param type diagnosis type
     * @param entity entity relevant for this diagnosis
     * @param information optional information relevant for this diagnosis
     */
    public Diagnosis(DiagnosisType type, String entity, String... information) {
        if (type == null) {
            throw new IllegalArgumentException("'type' is null!");
        }
        if (entity == null) {
            throw new IllegalArgumentException("'entity' is null!");
        }
        this.type = type;
        this.entity = entity;
        this.information = information;
    }

    /**
     * Returns an array comprising of the entity followed by the information
     * array elements.
     * @return array with the entity followed by the information array elements
     */
    public Object[] getMessageArgs() {
        Object[] messageArgs = new Object[information.length + 1];
        messageArgs[0] = entity;
        if (information.length > 0) {
            System.arraycopy(information, 0, messageArgs, 1, information.length);
        }
        return messageArgs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Diagnosis)) {
            return false;
        }
        Diagnosis diagnosisObj = (Diagnosis)obj;
        if (!type.equals(diagnosisObj.type)) {
            return false;
        }
        if (!entity.equals(diagnosisObj.entity)) {
            return false;
        }
        if (information != null && diagnosisObj.information != null) {
            if (information.length != diagnosisObj.information.length) {
                return false;
            }
            for (int i=0; i<information.length; ++i) {
                if (information[i] != null) {
                    if (!information[i].equals(diagnosisObj.information[i])) {
                        return false;
                    }
                } else if (diagnosisObj.information[i] != null) {
                    return false;
                }
            }
        } else if (information != null || diagnosisObj.information != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = type.hashCode();
        hashCode ^= entity.hashCode();
        if (information != null) {
            hashCode ^= 31331;
            for (int i=0; i<information.length; ++i) {
                if (information[ i ] != null) {
                    hashCode ^= information[ i ].hashCode();
                }
            }
        }
        return hashCode;
    }

}
