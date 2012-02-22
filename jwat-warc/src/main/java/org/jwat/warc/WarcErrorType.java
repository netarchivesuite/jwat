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

/**
 * Supported error types.
 *
 * @author nicl
 */
public enum WarcErrorType {

    /** Error type referring to a empty value where one is expected. */
    EMPTY("empty"),

    /** Error type referring to an invalid value. */
    INVALID("invalid"),

    /** Error type referring to a duplicate field encountered. */
    DUPLICATE("duplicate"),

    /** Error type referring to an existing but unrecognized value. */
    UNKNOWN("unknown"),

    /** Error type referring to something missing. */
    WANTED("wanted"),

    /** Error type referring to an unwanted value. */
    UNWANTED("unwanted"),

    /** Error type referring to a recommended value not being used. */
    RECOMMENDED("recommended");

    /** Error type name. */
    private String name;

    /**
     * Construct an error type enum value.
     * @param name error type name
     */
    private WarcErrorType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
