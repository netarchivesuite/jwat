package dk.netarkivet.common;

import java.util.Map;

public class ContentType {

	public static final int S_START = 0;
	public static final int S_CONTENTTYPE = 1;
	public static final int S_MEDIATYPE = 2;
	public static final int S_WHITESPACE = 3;
	public static final int S_PARAM_NAME = 4;
	public static final int S_PARAM_EQ = 5;
	public static final int S_PARAM_VALUE = 6;
	public static final int S_PARAM_QUOTED_VALUE = 7;

	public String contentType;

	public String mediaType;

	public Map<String, String> parameters;

	public static ContentType parseContentType(String contentTypeStr) {
		if (contentTypeStr == null || contentTypeStr.length() == 0) {
			return null;
		}
		ContentType ct = new ContentType();
		StringBuffer nameSb = new StringBuffer();
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
				if (c == ' ') {
					++idx;
				} else if (Character.isLetterOrDigit(c)) {
					nameSb.setLength(0);
					nameSb.append(c);
					++idx;
					state = S_CONTENTTYPE;
				} else {
					// /;= (-1) etc.
					return null;
				}
				break;
			case S_CONTENTTYPE:
				if (Character.isLetterOrDigit(c)) {
					nameSb.append(c);
					++idx;
				} else if (c == '/') {
					ct.contentType = nameSb.toString();
					++idx;
					nameSb.setLength(0);
					state = S_MEDIATYPE;
				} else {
					// ;= (-1) etc.
					return null;
				}
				break;
			case S_MEDIATYPE:
				if (Character.isLetterOrDigit(c)) {
					nameSb.append(c);
					++idx;
				} else if (c == ';') {
					if (nameSb.length() == 0) {
						return null;
					}
					ct.mediaType = nameSb.toString();
					++idx;
					state = S_WHITESPACE;
				} else if (c == -1) {
					if (nameSb.length() == 0) {
						return null;
					}
					ct.mediaType = nameSb.toString();
				} else {
					return null;
				}
				break;
			case S_WHITESPACE:
				if (c == ' ') {
					++idx;
				} else if (Character.isLetterOrDigit(c)) {
					nameSb.setLength(0);
					nameSb.append(c);
					++idx;
					state = S_PARAM_NAME;
				} else if (c == -1) {
					// Allow contenttype/mediatype;
				} else {
					// /;= etc.
					return null;
				}
				break;
			case S_PARAM_NAME:
				break;
			case S_PARAM_EQ:
				break;
			case S_PARAM_VALUE:
				break;
			case S_PARAM_QUOTED_VALUE:
				break;
			}
		}
		return ct;
	}

}
