package dk.netarkivet.warclib;

/**
 * Parsed Warc Header split into (name, value) pair. Unless the header is not 
 * formattet correctly in which case the line is present instead.  
 *
 * @author nicl
 */
public class WarcHeader {

	/** Header name. */
	public String name;

	/** Header value. */
	public String value;

	/** Line which is void of colon. */
	public String line;

}
