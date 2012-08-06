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
 * Class defining different diagnosis types used for reporting problems.
 *
 * @author nicl
 */
public enum DiagnosisType {

    /** An entity has more than one value definition. */
    DUPLICATE(1),

    /** Empty entity value. */
    EMPTY(0),

    /** Entity is erroneous. */
    ERROR(1),

    /** Entity is erroneous and something else was expected. */
    ERROR_EXPECTED(1),

    /** Invalid circumstance surrounding entity. */
    INVALID(0),

    /** Invalid data encountered. */
    INVALID_DATA(1),

    /** Invalid encoding encountered. */
    INVALID_ENCODING(2),

    /** Invalid data, expected something else. */
    INVALID_EXPECTED(2),

    /** Entity value differs from recommended value. */
    RECOMMENDED(2),

    /** Entity value missing but is recommended. */
    RECOMMENDED_MISSING(0),

    /** Required entity has an invalid value. */
    REQUIRED_INVALID(1),

    /** Something reserved is being used. */
    RESERVED(1),

    /** Entity is undesired. */
    UNDESIRED_DATA(1),

    /** Entity has an unknown value according to some specification. */
    UNKNOWN(1);

    /** Minimum number of information strings expected. */
    public final int expected_information;

    /**
     * Enum constructor.
     * @param expected_information minimum number of information strings expected
     */
    private DiagnosisType(int expected_information) {
        this.expected_information = expected_information;
    }

}
