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
import java.io.OutputStream;

/**
 * Small class to decode and encode UTF-8 characters. Decoding also keeps
 * track of encoding errors. Processes one UTF-8 character at a time.
 * The decoding method reports encoding validity in a field.
 *
 * Char. number range  |        UTF-8 octet sequence
 *       (hexadecimal)    |              (binary)
 *    --------------------+---------------------------------------------
 *    0000 0000-0000 007F | 0xxxxxxx
 *    0000 0080-0000 07FF | 110xxxxx 10xxxxxx
 *    0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
 *    0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
 *
 * @author nicl
 */
public class UTF8 {

    /** Complete or partial UTF-8 character, depending on conversion errors. */
    public int utf8_c;

    public byte[] chars;

    /** UTF-8 validity status on last read character. */
    public boolean bValidChar = false;

    /**
     * Given a character and an input stream returns the next decoded UTF-8
     * character. The encoded UTF-8 character is between 1 and 4 bytes long.
     * In order to preserve the validity and character value, the character is
     * returned by the method and its validity is available through the
     * @see bValidChar field.
     * @param c initial character
     * @param in input stream used to read extra UTF-8 encoded data
     * @return UTF-8 character or -1
     * @throws IOException if an io error occurs while reading
     */
    public int readUtf8(int c, InputStream in) throws IOException {
    	ByteArrayOutputStream charsOut = new ByteArrayOutputStream(4);
    	byte utf8_read;
        byte utf8_octets;
        utf8_c = 0;
        bValidChar = false;
        if ((c & 0x80) == 0x00) {
            // US-ASCII/UTF-8: 0000 0000-0000 007F | 0xxxxxxx
            bValidChar = true;
            utf8_c = c;
        } else {
            utf8_read = 1;
            bValidChar = true;
            if ((c & 0xE0) == 0xC0) {
                // UTF-8: 0000 0080-0000 07FF | 110xxxxx 10xxxxxx
                utf8_c = c & 0x1F;
                utf8_octets = 2;
            } else if ((c & 0xF0) == 0xE0) {
                // UTF-8: 0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
                utf8_c = c & 0x0F;
                utf8_octets = 3;
            } else if ((c & 0xF8) == 0xF0) {
                // UTF-8: 0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                utf8_c = c & 0x07;
                utf8_octets = 4;
            } else {
                // Invalid UTF-8 octet.
                utf8_c = 0;
                utf8_read = 0;
                utf8_octets = 0;
                bValidChar = false;
            }
            // Read he remaning octets.
            while (bValidChar && utf8_read < utf8_octets) {
                c = in.read();
                if (c == -1) {
                    // EOF.
                    bValidChar = false;
                    chars = charsOut.toByteArray();
                    return -1;
                } else {
                    charsOut.write(c);
                    if ((c & 0xC0) == 0x80) {
                        utf8_c = (utf8_c << 6) | (c & 0x3F);
                        ++utf8_read;
                    } else {
                        // Invalid UTF-8 octet.
                        bValidChar = false;
                    }
                }
            }
            // Correctly encoded.
            if (utf8_read == utf8_octets) {
                switch (utf8_octets) {
                case 2:
                    if (utf8_c < 0x00000080) {
                        // Incorrectly encoded value.
                        bValidChar = false;
                    }
                    break;
                case 3:
                    if (utf8_c < 0x00000800) {
                        // Incorrectly encoded value.
                        bValidChar = false;
                    }
                    break;
                case 4:
                    if (utf8_c < 0x00010000) {
                        // Incorrectly encoded value.
                        bValidChar = false;
                    }
                    break;
                }
            }
            c = utf8_c;
        }
        chars = charsOut.toByteArray();
        return c;
    }

    /**
     * UTF-8 encodes a character and outputs in onto the stream.
     * Returns the number of bytes used to encode the character.
     * @param c character to UTF-8 encode
     * @param out UTF-8 output stream
     * @return the number of bytes used to encode the character
     * @throws IOException if an io error occurs while writing
     */
    public int writeUtf8(int c, OutputStream out) throws IOException {
        byte utf8_write = 1;
        byte utf8_octets;
        int shift;
        int b;
        if (c < 0x00000080) {
            // US-ASCII/UTF-8: 0000 0000-0000 007F | 0xxxxxxx
            out.write(c);
            utf8_octets = 1;
            shift = 0;
        } else if (c < 0x00000800) {
            // UTF-8: 0000 0080-0000 07FF | 110xxxxx 10xxxxxx
            b = (c >> 6) | 0xC0;
            out.write(b);
            utf8_octets = 2;
            shift = 0;
        } else if (c < 0x00010000) {
            // UTF-8: 0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
            b = (c >> 12) | 0xE0;
            out.write(b);
            utf8_octets = 3;
            shift = 6;
        } else if (c < 0x00110000) {
            // UTF-8: 0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            b = (c >> 18) | 0xF0;
            out.write(b);
            utf8_octets = 4;
            shift = 12;
        } else {
            throw new IOException("Not UTF-8 encodable!");
        }
        while (utf8_write < utf8_octets) {
            b = ((c >> shift) & 0x3F) | 0x80;
            out.write(b);
            shift -= 6;
            ++utf8_write;
        }
        return utf8_write;
    }

}
