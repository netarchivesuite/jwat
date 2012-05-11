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
    DUPLICATE,

    /** Empty entity value. */
    EMPTY,

    /** Entity is erroneous. */
    ERROR,

    /** Entity is erroneous and something else was expected. */
    ERROR_EXPECTED,

    /** Invalid circumstance surrounding entity. */
    INVALID,

    /** Invalid data encountered. */
    INVALID_DATA,

    /** Invalid encoding encountered. */
    INVALID_ENCODING,

    /** Invalid data, expected something else. */
    INVALID_EXPECTED,

    /** Entity value differs from recommended value. */
    RECOMMENDED,

    /** Required entity has an invalid value. */
    REQUIRED_INVALID,

    /** Something reserved is being used. */
    RESERVED,

    /** Entity is undesired. */
    UNDESIRED_DATA,

    /** Entity has an unknown value according to some specification. */
    UNKNOWN;

}
