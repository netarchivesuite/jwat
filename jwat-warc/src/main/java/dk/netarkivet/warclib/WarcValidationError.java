package dk.netarkivet.warclib;

/**
 * Defines validation error.
 *
 * @author lbihanic, selghissassi
 */
public class WarcValidationError {

    /** Supported error types {@link WarcErrorType}. */
    public final WarcErrorType error;

    /** Field name. */
    public final String field;

    /** Field value. */
    public final String value;

    /**
     * Creates new <code>ValidationError</code>.
     * @param error error type {@link WarcErrorType}.
     * @param field field name.
     * @param value field value.
     */
    public WarcValidationError(WarcErrorType error, String field, String value) {
        if (error == null) {
            throw new IllegalArgumentException("error");
        }
        if ((field == null) || (field.length() == 0)) {
            throw new IllegalArgumentException("field");
        }
        this.error = error;
        this.field = field;
        this.value = value;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append("error: ");
        builder.append(error);
        builder.append(", field: ");
        builder.append(field);
        if(value != null && value.length() != 0){
            builder.append(", value: ");
            builder.append(value);
        }
         builder.append(']');
        return builder.toString();
    }

}
