package dk.netarkivet.warclib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * WARC-Date parser and format validator. The format "yyyy-MM-dd'T'HH:mm:ss'Z'"
 * is specified in the WARC ISO standard.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class WarcDateParser {

    /** Allowed date format string according to the WARC ISO standard. */
    private static final String WARC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /** Allowed <code>DateFormat</code>. */
    private final DateFormat dateFormat;

    /** Basic <code>DateFormat</code> is not thread safe. */
    private static final ThreadLocal<WarcDateParser> DateParserTL =
        new ThreadLocal<WarcDateParser>() {
        public WarcDateParser initialValue() {
            return new WarcDateParser();
        }
    };

    /**
     * Creates a new <code>DateParser</code>.
     */
    private WarcDateParser() {
        dateFormat = new SimpleDateFormat(WARC_DATE_FORMAT);
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Parses a date.
     * @param dateStr date to parse
     * @return the formatted date
     */
    private Date parseDate(String dateStr) {
        Date date = null;
        try {
        	// We subtract 4 from the format because of the ' characters.
        	// These characters are to specify constants in the format string. 
            if ((dateStr != null)
                            && dateStr.length() == WARC_DATE_FORMAT.length() - 4) {
            	// Support upper/lower-case.
                date = dateFormat.parse(dateStr.toUpperCase());
            }
        } catch (Exception e) { /* Ignore */ }
        return date;
    }

    /**
     * Parses the date using the format "yyyy-MM-ddTHH:mm:ssZ".
     * @param dateStr the date to parse
     * @return the formatted date or <code>null</code> based on whether the date
     * to parse is compliant with the format "yyyy-MM-ddTHH:mm:ssZ" or not
     */
    public static Date getDate(String dateStr) {
        Date date = DateParserTL.get().parseDate(dateStr);
        boolean isValid = (date == null) ? false
                                         : (date.getTime() > 0);
        return isValid ? date : null;
    }

}

