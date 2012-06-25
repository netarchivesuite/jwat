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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents a message digest including information about algorithm
 * and encoding used.
 *
 * @author nicl
 */
public class Digest {

    /** Digest algorithm used. */
    public String algorithm;

    /** Digest in bytes as returned by e.g. <code>MessageDigest</code>. */
    public byte[] digestBytes;

    /** Digest string, encoded. */
    public String digestString;

    /** Digest encoding used. (E.g. Base16, 32 or 64) */
    public String encoding;

    /** Cache length of algorithm digest output. */
    protected static Map<String, Integer> digestAlgoLengthache = new TreeMap<String, Integer>();

    /**
     * Returns the length of an algorithms digest output or -1 if it is an
     * invalid digest algorithm.
     * @param digestAlgorithm digest algorithm
     * @return digest algorithm output length or -1 if invalid digest algorithm
     */
    public static synchronized int digestAlgorithmLength(String digestAlgorithm) {
        if (digestAlgorithm == null || digestAlgorithm.length() == 0) {
            throw new IllegalArgumentException("'digestAlgorithm' is empty or null");
        }
        Integer cachedLen = digestAlgoLengthache.get(digestAlgorithm);
        if (cachedLen == null) {
            try {
                MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
                byte[] digest = md.digest(new byte[16]);
                cachedLen = digest.length;
                md.reset();
                md = null;
            } catch (NoSuchAlgorithmException e) {
            }
            if (cachedLen == null) {
                cachedLen = -1;
            }
            digestAlgoLengthache.put(digestAlgorithm,  cachedLen);
        }
        return cachedLen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        Digest digestObj = (Digest)obj;
        if (!Arrays.equals(digestBytes, digestObj.digestBytes)) {
            return false;
        }
        if (algorithm != null) {
            if (!algorithm.equals(digestObj.algorithm)) {
                return false;
            }
        } else if (digestObj.algorithm != null) {
            return false;
        }
        if (digestString != null) {
            if (!digestString.equals(digestObj.digestString)) {
                return false;
            }
        } else if (digestObj.digestString != null) {
            return false;
        }
        if (encoding != null) {
            if (!encoding.equals(digestObj.encoding)) {
                return false;
            }
        } else if (digestObj.encoding != null) {
            return false;
        }
        return true;
    }

}
