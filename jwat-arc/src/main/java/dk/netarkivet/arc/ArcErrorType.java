package dk.netarkivet.arc;

/**
 * Supported error types.
 *
 * @author lbihanic, selghissassi
 */
public enum ArcErrorType {

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
    private ArcErrorType(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return this.name;
    }

}
