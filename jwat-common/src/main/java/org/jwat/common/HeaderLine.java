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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Depending on the parsing mode either a line or a header entry
 * pair(name, value) is returned. Any problem(s) reading the (header)line
 * is reported in the bfErrors field.
 *
 * @author nicl
 */
public class HeaderLine {

    /** Read line initial size. */
    public static final int READLINE_INITIAL_SIZE = 128;

    /** Normal line parsed. */
    public static final byte HLT_LINE = 1;

    /** Header line parsed. */
    public static final byte HLT_HEADERLINE = 2;

    /** Type of the parsed line. */
    public byte type = 0;

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

    /** List of additional headers with the same name. */
    public List<HeaderLine> lines = new LinkedList<HeaderLine>();

    /**
     * Read a single line into a header line.
     * @param in inputstream
     * @return single string line
     * @throws IOException if an io error occurs while reading line
     */
    public static HeaderLine readLine(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer(READLINE_INITIAL_SIZE);
        ByteArrayOutputStream out = new ByteArrayOutputStream(READLINE_INITIAL_SIZE);
        int b;
        while (true) {
            b = in.read();
            if (b == -1) {
                return null;    //Unexpected EOF
            }
            out.write(b);
            if (b == '\n'){
                break;
            }
            if (b != '\r') {
                sb.append((char) b);
            }
        }
        HeaderLine headerLine = new HeaderLine();
        headerLine.line = sb.toString();
        headerLine.raw = out.toByteArray();
        return headerLine;
    }

}
