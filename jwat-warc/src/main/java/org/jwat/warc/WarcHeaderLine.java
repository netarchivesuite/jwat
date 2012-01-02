package org.jwat.warc;

/**
 * Parsed header entry split into a (name, value) pair. Unless the header
 * entry is not formatted correctly in which case the raw line is presented
 * instead.
 *
 * @author nicl
 */
public class WarcHeaderLine {

    /** Header name. */
    public String name;

    /** Header value. */
    public String value;

    /** The raw line if no colon is present. */
    public String line;

}
