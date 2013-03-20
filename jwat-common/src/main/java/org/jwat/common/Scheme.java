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
 * Simple class to validate a scheme in the beginning of a byte array.
 *
 * @author nicl
 */
public class Scheme {

    /** Bit field used to identify valid first/follow characters. */
    protected static int[] bf = new int[256];

    /**
     * Disable instantiation.
     */
    protected Scheme() {
    }

    /*
     * Initialize first/follow bit field.
     */
    static {
        String alphas = "abcdefghijklmnopqrstuvwxyz";
        for (int i=0; i<alphas.length(); ++i) {
            bf[alphas.charAt(i)] = 3;
        }
        String digits = "1234567890";
        for (int i=0; i<digits.length(); ++i) {
            bf[digits.charAt(i)] = 2;
        }
        String scheme = "+-.";
        for (int i=0; i<scheme.length(); ++i) {
            bf[scheme.charAt(i)] = 2;
        }
    }

    /**
     * Returns true if the start of the array is a valid scheme.
     * @param bytes byte array in which to look for a valid scheme at the beginning
     * @return true if the start of the array is a valid scheme
     */
    public static boolean startsWithScheme(byte[] bytes) {
        boolean result = false;
        if (bytes != null) {
            int idx = 0;
            boolean bLoop = true;
            int c;
            while (bLoop) {
                if (idx < bytes.length) {
                    c = bytes[idx];
                    if (c == ':') {
                        bLoop = false;
                        if (idx > 0) {
                            result = true;
                        }
                    } else if (idx > 0) {
                        bLoop = ((bf[c & 255] & 2) != 0);
                    } else {
                        bLoop = ((bf[c & 255] & 1) != 0);
                    }
                } else {
                    bLoop = false;
                }
                ++idx;
            }
        }
        return result;
    }

    /**
     * Returns a scheme if the given string has a valid scheme at the beginning
     * of null if no scheme was found.
     * @param uri a possible URI string
     * @return a scheme if one is found or null
     */
    public static String getScheme(String uri) {
        if (uri != null) {
            StringBuilder sb = new StringBuilder();
            int idx = 0;
            boolean bLoop = true;
            int c;
            while (bLoop) {
                if (idx < uri.length()) {
                    c = uri.charAt(idx);
                    if (c < 256) {
                        if (c == ':') {
                            //bLoop = false;
                            if (idx > 0) {
                                return sb.toString();
                            }
                        } else if (idx > 0) {
                            sb.append((char)c);
                            bLoop = ((bf[c] & 2) != 0);
                        } else {
                            sb.append((char)c);
                            bLoop = ((bf[c] & 1) != 0);
                        }
                    } else {
                        bLoop = false;
                    }
                } else {
                    bLoop = false;
                }
                ++idx;
            }
        }
        return null;
    }

}
