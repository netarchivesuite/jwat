package dk.netarkivet.warclib;

/**
 * Parsed header entry split into a (name, value) pair. Unless the header 
 * entry is not formatted correctly in which case the line is present instead.  
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
