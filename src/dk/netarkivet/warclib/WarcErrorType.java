package dk.netarkivet.warclib;

/**
 * Supported error types.
 *
 * @author nicl
 */
public enum WarcErrorType {

    /** Error type referring to something missing. */
    MISSING("missing"),

    /** Error type referring to something invalid. */
    INVALID("invalid");

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
