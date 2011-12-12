package dk.netarkivet.common;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a content-type string parsed, validated and decomposed
 * into its separate components. It's based on the rfc2616 text and accordingly
 * fairly strict concerning whitespace's. Whitespace is only permissible after
 * a ';' and before and after the whole content-type string.
 *
 * @author nicl
 */
public class ContentType {

	/** Beginning state. */
	public static final int S_START = 0;
	/** Parsing content-type state. */
	public static final int S_CONTENTTYPE = 1;
	/** Parsing media-type state. */
	public static final int S_MEDIATYPE = 2;
	/** Lenient parsing of trailing whitespace after media-type state. */
	public static final int S_MEDIATYPE_WHITESPACE = 3;
	/** Parsing after a ';' character state. */
	public static final int S_SEMICOLON = 4;
	/** Parsing the parameter name state. */
	public static final int S_PARAM_NAME = 5;
	/** Parsing after a '=' character state. */
	public static final int S_PARAM_EQ = 6;
	/** Parsing a parameter value token state. */
	public static final int S_PARAM_VALUE = 7;
	/** Parsing a quote parameter value state. */
	public static final int S_PARAM_QUOTED_VALUE = 8;
	/** Lenient parsing of trailing whitespace after argument value state. */
	public static final int S_PARAM_VALUE_WHITESPACE = 9;

	/** Parsed Content-type. */
	public String contentType;

	/** Parsed Media-type. */
	public String mediaType;

	/** Optional <code>Map</code> of parameters. */
	public Map<String, String> parameters;

	/** Table of separator and control characters. */
    protected static final boolean[] separatorsCtlsTab = new boolean[256];

    /** rfc2616 separator minus space and tab. */
    protected static final String separators = "()<>@,;:\\\"/[]?={} \t";

    /*
     * Populate table with separator and control characters.
     */
    static {
        for (int i=0; i<separators.length(); ++i) {
        	separatorsCtlsTab[separators.charAt(i)] = true;
        }
        for (int i=0; i<32; ++i) {
        	if (i != '\t') {
            	separatorsCtlsTab[i] = true;
        	}
        }
    }

	/**
	 * Tries to parse and validate the given content-type string. Also tries
	 * to parse and validate any optional parameters present in the string.
	 * @param contentTypeStr content-type string
	 * @return
	 */
	public static ContentType parseContentType(String contentTypeStr) {
		if (contentTypeStr == null || contentTypeStr.length() == 0) {
			return null;
		}
		ContentType ct = new ContentType();
		StringBuffer nameSb = new StringBuffer();
		StringBuffer valueSb = null;
		int state = S_START;
		int idx = 0;
		int c;
		boolean bLoop = true;
		while (bLoop) {
			if (idx < contentTypeStr.length()) {
				c = contentTypeStr.charAt(idx);
			} else {
				c = -1;
				bLoop = false;
			}
			switch (state) {
			case S_START:
				if (c == ' ' || c == '\t') {
					++idx;
				} else if ((c >= 0 && c < 256 && !separatorsCtlsTab[c]) || c >= 256) {
					nameSb.setLength(0);
					nameSb.append((char) c);
					++idx;
					state = S_CONTENTTYPE;
				} else {
					// /;= (-1) etc.
					return null;
				}
				break;
			case S_CONTENTTYPE:
				if ((c >= 0 && c < 256 && !separatorsCtlsTab[c]) || c >= 256) {
					nameSb.append((char) c);
					++idx;
				} else if (c == '/') {
					// Name always at least one.
					ct.contentType = nameSb.toString().toLowerCase();
					++idx;
					nameSb.setLength(0);
					state = S_MEDIATYPE;
				} else {
					// ;= (-1) etc.
					return null;
				}
				break;
			case S_MEDIATYPE:
				if ((c >= 0 && c < 256 && !separatorsCtlsTab[c]) || c >= 256) {
					nameSb.append((char) c);
					++idx;
				} else if (c == -1) {
					if (nameSb.length() == 0) {
						return null;
					}
					ct.mediaType = nameSb.toString().toLowerCase();
				} else if (c == ';') {
					if (nameSb.length() == 0) {
						return null;
					}
					ct.mediaType = nameSb.toString().toLowerCase();
					++idx;
					valueSb = new StringBuffer();
					ct.parameters = new HashMap<String, String>();
					state = S_SEMICOLON;
				} else if (c == ' ' || c == '\t') {
					if (nameSb.length() == 0) {
						return null;
					}
					ct.mediaType = nameSb.toString().toLowerCase();
					++idx;
					state = S_MEDIATYPE_WHITESPACE;
				} else {
					return null;
				}
				break;
			case S_MEDIATYPE_WHITESPACE:
				if (c == ' ' || c == '\t') {
					++idx;
				} else if (c != -1) {
					return null;
				}
				break;
			case S_SEMICOLON:
				if (c == ' ' || c == '\t') {
					++idx;
				} else if ((c >= 0 && c < 256 && !separatorsCtlsTab[c]) || c >= 256) {
					nameSb.setLength(0);
					valueSb.setLength(0);
					nameSb.append((char) c);
					++idx;
					state = S_PARAM_NAME;
				} else if (c == -1) {
					// Allow contenttype/mediatype; and optional parameters
				} else {
					// /;= etc.
					return null;
				}
				break;
			case S_PARAM_NAME:
				if ((c >= 0 && c < 256 && !separatorsCtlsTab[c]) || c >= 256) {
					nameSb.append((char) c);
					++idx;
				} else if (c == '=') {
					// Name always at least one.
					++idx;
					state = S_PARAM_EQ;
				} else {
					// (-1) etc.
					return null;
				}
				break;
			case S_PARAM_EQ:
				if ((c >= 0 && c < 256 && !separatorsCtlsTab[c]) || c >= 256) {
					valueSb.append((char) c);
					++idx;
					state = S_PARAM_VALUE;
				} else if (c == '"') {
					++idx;
					state = S_PARAM_QUOTED_VALUE;
				}
				else {
					// (-1) etc.
					return null;
				}
				break;
			case S_PARAM_VALUE:
				if ((c >= 0 && c < 256 && !separatorsCtlsTab[c]) || c >= 256) {
					valueSb.append((char) c);
					++idx;
				} else if (c == -1) {
					ct.parameters.put(nameSb.toString().toLowerCase(),
							valueSb.toString());
				} else if (c == ';') {
					ct.parameters.put(nameSb.toString().toLowerCase(),
							valueSb.toString());
					++idx;
					state = S_SEMICOLON;
				} else if (c == ' ' || c == '\t') {
					ct.parameters.put(nameSb.toString().toLowerCase(),
							valueSb.toString());
					++idx;
					state = S_PARAM_VALUE_WHITESPACE;
				} else {
					// etc.
					return null;
				}
				break;
			case S_PARAM_QUOTED_VALUE:
				if (c == '"') {
					ct.parameters.put(nameSb.toString().toLowerCase(),
							valueSb.toString());
					++idx;
					state = S_PARAM_VALUE_WHITESPACE;
				} else if (c != -1) {
					valueSb.append((char) c);
					++idx;
				} else {
					// (-1)
					return null;
				}
				break;
			case S_PARAM_VALUE_WHITESPACE:
				if (c == ' ' || c == '\t') {
					++idx;
				} else if (c == ';') {
					++idx;
					state = S_SEMICOLON;
				} else if (c != -1) {
					return null;
				}
				break;
			}
		}
		return ct;
	}

	/**
	 * Return parameter value associated with supplied parameter name.
	 * @param name parameter name
	 * @return parameter value or null
	 */
	public String getParameter(String name) {
		if (name == null || name.length() == 0 || parameters == null) {
			return null;
		}
		return parameters.get(name.toLowerCase());
	}

}
