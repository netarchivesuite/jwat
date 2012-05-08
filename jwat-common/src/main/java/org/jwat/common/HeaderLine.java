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
 * Parsed header entry split into a (name, value) pair. Unless the header
 * entry is not formatted correctly in which case the raw line is presented
 * instead.
 *
 * @author nicl
 */
public class HeaderLine {

    /** Invalid line, for reporting the raw data read. */
    public static final byte HLT_RAW = 0;
    /** Normal line parsed. */
    public static final byte HLT_LINE = 1;
    /** Header line parsed. */
    public static final byte HLT_HEADERLINE = 2;

    /** Type of the parsed line. */
    public byte type = HLT_RAW;;

    /** Header name. */
    public String name;

    /** Header value. */
    public String value;

    /** The raw line if no colon is present. */
    public String line;

    /** The original byte stream. */
    public byte[] raw;

    /** Bit field of errors encountered while attempting to read a line. */
    public int bfErrors;

}
