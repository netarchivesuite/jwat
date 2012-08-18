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

/**
 * Class used to decode and validate a Quoted-Printable string as specified
 * in RFC 2047 (Section 4.2).
 *
 * @author nicl
 */
public class QuotedPrintable {

    /**
     * Static class.
     */
    protected QuotedPrintable() {
    }

    /**
     * Returns a decoded Quoted-Printable string or null if it is not valid.
     * @param encoded_text Quoted-Printable string
     * @return decoded string or null
     */
    public static byte[] decode(String encoded_text) {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        int idx = 0;
        int c;
        int cout;
        while (idx < encoded_text.length()) {
            c = encoded_text.charAt(idx++);
            if (c == '_') {
                // Underscore is shorthand for <space>.
                bytesOut.write(' ');
            } else if (c == '=') {
                // "=" followed by two hexadecimal digits.
                if ((idx + 2) <= encoded_text.length()) {
                    c = encoded_text.charAt(idx++);
                    c = Base16.decodeTab[c];
                    if (c != -1) {
                        cout = c << 4;
                        c = encoded_text.charAt(idx++);
                        c = Base16.decodeTab[c];
                        if (c != -1) {
                            cout |= c;
                            bytesOut.write(cout);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else if (c > 32 && c < 127) {
                bytesOut.write(c);
            } else {
                return null;
            }
        }
        return bytesOut.toByteArray();
    }

}
