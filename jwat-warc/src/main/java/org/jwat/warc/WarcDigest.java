package org.jwat.warc;

import org.jwat.common.Digest;

/**
 * This class represents the parsed and format validated information provided
 * from a WARC digest header value.
 *
 * @author nicl
 */
public class WarcDigest extends Digest {

    /**
     * Construct an object with the supplied parameters.
     * @param algorithm digest algorithm
     * @param digestValue digest value in Base<x> format.
     */
    private WarcDigest(String algorithm, String digestValue) {
        this.algorithm = algorithm;
        this.digestString = digestValue;
    }

    /**
     * Parse and validate the format of a WARC digest header value.
     * @param labelledDigest WARC digest header value
     * @return <code>WarcDigest</code> object or <code>null</code>
     */
    public static Digest parseDigest(String labelledDigest) {
        if (labelledDigest == null || labelledDigest.length() == 0) {
            return null;
        }
        String algorithm;
        String digestValue;
        int cIdx = labelledDigest.indexOf(':');
        if (cIdx != -1) {
            algorithm = labelledDigest.substring(0, cIdx).trim();
            digestValue = labelledDigest.substring(cIdx + 1).trim();
            if (algorithm.length() > 0 && digestValue.length() > 0) {
                return new WarcDigest(algorithm, digestValue);
            }
        }
        return null;
    }

    /**
     * Returns a header representation of the class state.
     * @return header representation of the class state
     */
    @Override
    public String toString() {
        return (algorithm + ":" + digestString);
    }

}
