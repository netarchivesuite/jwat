package org.jwat.warc;

/**
 * Supported error types.
 *
 * @author nicl
 */
public enum WarcErrorType {

    /** Error type referring to a empty value where one is expected. */
    EMPTY("empty"),

    /** Error type referring to an invalid value. */
    INVALID("invalid"),

    /** Error type referring to a duplicate field encountered. */
    DUPLICATE("duplicate"),

    /** Error type referring to an existing but unrecognized value. */
    UNKNOWN("unknown"),

    /** Error type referring to something missing. */
    WANTED("wanted"),

    /** Error type referring to an unwanted value. */
    UNWANTED("unwanted"),

    /** Error type referring to a recommended value not being used. */
    RECOMMENDED("recommended");

    /** Error type name. */
    private String name;

    /**
     * Construct an error type enum value.
     * @param name error type name
     */
    private WarcErrorType(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return this.name;
    }

}
