package dk.netarkivet.warclib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WarcDateParser {

    /** Allowed format string. */
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
            if ((dateStr != null)
                            && dateStr.length() == WARC_DATE_FORMAT.length() - 4) {
                date = dateFormat.parse(dateStr);
            }
        } catch (Exception e) { /* Ignore */
        	System.out.println( e );
        }
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

