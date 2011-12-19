package dk.netarkivet.warclib;

/**
 * This class represents the parsed and format validated information provided
 * from a WARC digest header value.
 *
 * @author nicl
 */
public class WarcDigest {

	/** Digest algorithm. */
	public String algorithm;

	/** Digest value, in Base<x> format. (Where x is 16, 32 or 64) */
	public String digestValue;

	/**
	 * Construct an object with the supplied parameters.
	 * @param algorithm digest algorithm
	 * @param digestValue digest value in Base<x> format.
	 */
	private WarcDigest(String algorithm, String digestValue) {
		this.algorithm = algorithm;
		this.digestValue = digestValue;
	}

	/**
	 * Parse and validate the format of a WARC digest header value.
	 * @param labelledDigest WARC digest header value
	 * @return <code>WarcDigest</code> object or <code>null</code>
	 */
	public static WarcDigest parseDigest(String labelledDigest) {
		if (labelledDigest == null || labelledDigest.length() == 0) {
			return null;
		}
		String algorithm;
		String digestValue;
		int cIdx = labelledDigest.indexOf(':');
		if (cIdx != -1) {
			algorithm = new String(labelledDigest.substring(0, cIdx).trim());
			digestValue = new String(labelledDigest.substring(cIdx + 1).trim());
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
		return new String(algorithm + ":" + digestValue);
	}

	/*
	public String toDebugString() {
		return new String("[algorithm=" + algorithm + ",digest value=" + digestValue + "]");
	}
	*/

}
