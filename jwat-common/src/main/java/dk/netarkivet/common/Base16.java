package dk.netarkivet.common;

import java.io.ByteArrayOutputStream;

public class Base16 {

	public static String convtab = "0123456789ABCDEF";

	/**
	 * Static class.
	 */
	private Base16() {
	}

	/**
	 * Decodes a base16 encoded string.
	 * @param inStr encoded string.
	 * @return decodes base64 string.
	 */
	public static String decodeToString(String inStr) {
		StringBuffer outStr = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		char cin;
		int mod = 0;
		int cIdx;
		int cout = 0;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < inStr.length() ) {
				cin = inStr.charAt( idx++ );
				cIdx = convtab.indexOf( Character.toUpperCase( cin ) );
				if ( cIdx != -1 ) {
					switch ( mod ) {
						case 0:
							cout = cIdx << 4;
							break;
						case 1:
							cout |= cIdx;
							outStr.append( (char)cout );
							break;
					}
					mod = ( mod + 1 ) % 2;
				}
				else {
					return null;
				}
			}
			else {
				b = false;
			}
		}

		/*
		 * Padding.
		 */

		if ( mod != 1 ) {
			return outStr.toString();
		}
		else {
			// In state 1 only if decoded a half byte.
			return null;
		}
	}

	/**
	 * Decodes a base16 encoded string.
	 * @param inStr encoded string.
	 * @return decodes base64 string.
	 */
	public static byte[] decodeToArray(String inStr) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		boolean b = true;
		int idx = 0;
		char cin;
		int mod = 0;
		int cIdx;
		int cout = 0;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < inStr.length() ) {
				cin = inStr.charAt( idx++ );
				cIdx = convtab.indexOf( cin );
				if ( cIdx != -1 ) {
					switch ( mod ) {
						case 0:
							cout = cIdx << 4;
							break;
						case 1:
							cout |= cIdx;
							os.write( cout );
							break;
					}
					mod = ( mod + 1 ) % 2;
				}
				else {
					return null;
				}
			}
			else {
				b = false;
			}
		}

		/*
		 * Padding.
		 */

		if ( mod != 1 ) {
			return os.toByteArray();
		}
		else {
			// In state 1 only if decoded a half byte.
			return null;
		}
	}

	/**
	 * Encodes a string with base16.
	 * @param inStr unencoded text string.
	 * @return encoded string.
	 */
	public static String encodeFromString(String inStr) {
		StringBuffer outStr = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		int cin;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < inStr.length() ) {
				cin = inStr.charAt( idx++ );
				outStr.append( convtab.charAt( (cin >> 4) & 15 ) );
				outStr.append( convtab.charAt( cin & 15 ) );
			}
			else {
				b = false;
			}
		}

		return outStr.toString();
	}

	/**
	 * Encodes a string with base16.
	 * @param inStr unencoded text string.
	 * @return encoded string.
	 */
	public static String encodeFromArray(byte[] inStr) {
		StringBuffer outStr = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		int cin;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < inStr.length ) {
				cin = inStr[ idx++ ];
				outStr.append( convtab.charAt( (cin >> 4) & 15 ) );
				outStr.append( convtab.charAt( cin & 15 ) );
			}
			else {
				b = false;
			}
		}

		return outStr.toString();
	}

}
