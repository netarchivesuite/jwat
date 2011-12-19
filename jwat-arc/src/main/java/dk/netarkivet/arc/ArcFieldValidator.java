package dk.netarkivet.arc;

import java.util.HashMap;
import java.util.Map;

/**
 * Field validator utility class.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcFieldValidator {

    /** Array of field names. */
    protected String[] fieldNames;

    /** <code>Map</code> of field name to field index mappings. */
    protected Map<String, Integer> fieldIdxMap = new HashMap<String, Integer>();

    /**
     * Protected constructor to ensure correct instantiation.
     */
    protected ArcFieldValidator() {
    }

    /**
     * Prepares a validator for use on a given set of fields.
     * @param fieldNames array of field names.
     * @return validator used to look field by field name->index.
     */
    public static ArcFieldValidator prepare(String[] fieldNames) {
        ArcFieldValidator fv = new ArcFieldValidator();
        fv.fieldNames = fieldNames;
        for (int i = 0; i < fieldNames.length; ++i) {
            fv.fieldIdxMap.put(fieldNames[i], i);
        }
        return fv;
    }

    /**
     * Checks to if the index is out of bounds in the give array and also
     * if the value if null or empty.
     * @param array source value array
     * @param idx desired index into array
     * @return value index in array or null
     */
    public static String getArrayValue(String[] array, int idx) {
        return ((array.length > idx) && (array[idx] != null)
                && (array[idx].length() != 0))? array[idx] : null;
    }

}
