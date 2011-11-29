/*
 * Base64 de/encoder implemention based on the specifications in rfc2045.
 * Copyright (C) 2002, 2003, 2004  Nicholas Clarke
 *
 */

/*
 * History:
 *
 * 04-Jun-2002 : First implementation.
 * 05-Jun-2002 : Bug fix.
 * 21-Jun-2003 : Moved to antiaction.com package.
 * 21-Jun-2003 : Moved from http(d).core to core.
 * 25-Jul-2004 : Added encoder.
 * 26-Jul-2004 : Added encoder padding. Javadoc.
 * 27-Jul-2004 : Javadoc.
 * 04-Mar-2006 : Moved to common package.
 * 10-Oct-2009 : Moved test code to separate class.
 *
 */

package dk.netarkivet.common;

import java.io.ByteArrayOutputStream;

/**
 * Base64 de/encoder implementation based on the specifications in rfc2045.
 *
 * @version 1.00
 * @author Nicholas Clarke <mayhem[at]antiaction[dot]com>
 */
public class Base64 {

	/** Ascii table used to de/encode. */
	private static String convtab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	/**
	 * Static class.
	 */
	private Base64() {
	}

	/**
	 * Decodes a base64 encoded string.
	 * @param inStr encoded string.
	 * @return decodes base64 string.
	 */
	public static String decodeToString(String inStr) {
		StringBuffer outStr = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		char read;
		int mod = 0;
		int cIdx;
		int c = 0;

		/*
		 * Loop.
		 */

		while( b ) {
			if ( idx < inStr.length() ) {
				read = inStr.charAt( idx++ );
				if ( read == '=' ) {
					b = false;
				}
				else {
					cIdx = convtab.indexOf( read );
					if ( cIdx != -1 ) {
						switch ( mod ) {
							case 0:
								c = cIdx << 2;
								break;
							case 1:
								c |= (cIdx >> 4);
								outStr.append( (char)c );
								c = (cIdx << 4) & 255;
								break;
							case 2:
								c |= (cIdx >> 2);
								outStr.append( (char)c );
								c = (cIdx << 6) & 255;
								break;
							case 3:
								c |= cIdx;
								outStr.append( (char)c );
								break;
						}
						mod = ( mod + 1 ) % 4;
					}
					else {
						return null;
					}
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
			// In state 1 only 6 bits of the next 24 bit has been decoded.
			return null;
		}
	}

	/**
	 * Decodes a base64 encoded string.
	 * @param inStr encoded string.
	 * @return decodes base64 string.
	 */
	public static byte[] decodeToArray(String inStr) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		boolean b = true;
		int idx = 0;
		char read;
		int mod = 0;
		int cIdx;
		int c = 0;

		/*
		 * Loop.
		 */

		while( b ) {
			if ( idx < inStr.length() ) {
				read = inStr.charAt( idx++ );
				if ( read == '=' ) {
					b = false;
				}
				else {
					cIdx = convtab.indexOf( read );
					if ( cIdx != -1 ) {
						switch ( mod ) {
							case 0:
								c = cIdx << 2;
								break;
							case 1:
								c |= (cIdx >> 4);
								os.write( c );
								c = (cIdx << 4) & 255;
								break;
							case 2:
								c |= (cIdx >> 2);
								os.write( c );
								c = (cIdx << 6) & 255;
								break;
							case 3:
								c |= cIdx;
								os.write( c );
								break;
						}
						mod = ( mod + 1 ) % 4;
					}
					else {
						return null;
					}
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
			// In state 1 only 6 bits of the next 24 bit has been decoded.
			return null;
		}
	}

	/**
	 * Encodes a string with base64.
	 * @param inStr unencoded text string.
	 * @return encoded string.
	 */
	public static String encodeFromString(String inStr) {
		StringBuffer outStr = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		char write;
		int c = 0;
		int mod = 0;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < inStr.length() ) {
				write = inStr.charAt( idx++ );
				switch ( mod ) {
					case 0:
						c = ( write >> 2 ) & 63;
						outStr.append( convtab.charAt( c ) );
						c = ( write << 4 ) & 63;
						break;
					case 1:
						c |= ( write >> 4 ) & 63;
						outStr.append( convtab.charAt( c ) );
						c = ( write << 2 ) & 63;
						break;
					case 2:
						c |= ( write >> 6 ) & 63;
						outStr.append( convtab.charAt( c ) );
						c = write & 63;
						outStr.append( convtab.charAt( c ) );
						break;
				}
				mod = ( mod + 1 ) % 3;
			}
			else {
				b = false;
			}
		}

		/*
		 * Padding.
		 */

		switch ( mod ) {
			case 0:
				break;
			case 1:
				outStr.append( convtab.charAt( c ) );
				outStr.append( "==" );
				break;
			case 2:
				outStr.append( convtab.charAt( c ) );
				outStr.append( "=" );
				break;
		}

		return outStr.toString();
	}

	/**
	 * Encodes a string with base64.
	 * @param inStr unencoded text string.
	 * @return encoded string.
	 */
	public static String encodeFromArray(byte[] inStr) {
		StringBuffer outStr = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		int write;
		int c = 0;
		int mod = 0;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < inStr.length ) {
				write = inStr[ idx++ ];
				switch ( mod ) {
					case 0:
						c = ( write >> 2 ) & 63;
						outStr.append( convtab.charAt( c ) );
						c = ( write << 4 ) & 63;
						break;
					case 1:
						c |= ( write >> 4 ) & 63;
						outStr.append( convtab.charAt( c ) );
						c = ( write << 2 ) & 63;
						break;
					case 2:
						c |= ( write >> 6 ) & 63;
						outStr.append( convtab.charAt( c ) );
						c = write & 63;
						outStr.append( convtab.charAt( c ) );
						break;
				}
				mod = ( mod + 1 ) % 3;
			}
			else {
				b = false;
			}
		}

		/*
		 * Padding.
		 */

		switch( mod ) {
			case 0:
				break;
			case 1:
				outStr.append( convtab.charAt( c ) );
				outStr.append( "==" );
				break;
			case 2:
				outStr.append( convtab.charAt( c ) );
				outStr.append( "=" );
				break;
		}

		return outStr.toString();
	}

}
