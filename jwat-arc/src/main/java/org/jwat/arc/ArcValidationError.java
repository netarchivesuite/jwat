package org.jwat.arc;

/**
 * Defines validation error.
 *
 * @author lbihanic, selghissassi
 */
public class ArcValidationError {

    /** Supported error types {@link ArcErrorType}. */
    public final ArcErrorType error;

    /** Field name. */
    public final String field;

    /** Field value. */
    public final String value;

    /**
     * Creates new <code>ValidationError</code>.
     * @param error error type {@link ArcErrorType}.
     * @param field field name.
     * @param value field value.
     */
    public ArcValidationError(ArcErrorType error, String field, String value) {
        if (error == null) {
            throw new IllegalArgumentException("Parameter 'error' is null");
        }
        if ((field == null) || (field.length() == 0)) {
            throw new IllegalArgumentException(
                    "Parameter 'field' is either null or the empty string");
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
