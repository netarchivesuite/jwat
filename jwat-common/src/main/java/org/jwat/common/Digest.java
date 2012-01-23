package org.jwat.common;

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

}
