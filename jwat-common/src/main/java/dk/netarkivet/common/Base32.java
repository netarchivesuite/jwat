package dk.netarkivet.common;

import java.io.ByteArrayOutputStream;

public class Base32 {

	public static String convtab = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

	/**
	 * Static class.
	 */
	private Base32() {
	}

	/**
	 * Decodes a base32 encoded string.
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
				if ( cin == '=' ) {
					b = false;
				}
				else {
					cIdx = convtab.indexOf( Character.toUpperCase( cin ) );
					if ( cIdx != -1 ) {
						switch ( mod ) {
							case 0:
								cout = cIdx << 3;
								break;
							case 1:
								cout |= (cIdx >> 2);
								outStr.append( (char)cout );
								cout = (cIdx << 6) & 255;
								break;
							case 2:
								cout |= (cIdx << 1);
								break;
							case 3:
								cout |= (cIdx >> 7);
								outStr.append( (char)cout );
								cout = (cIdx << 4) & 255;
								break;
							case 4:
								cout |= (cIdx >> 1);
								outStr.append( (char)cout );
								cout = (cIdx << 7) & 255;
								break;
							case 5:
								cout = cIdx << 2;
								break;
							case 6:
								cout |= (cIdx >> 3);
								outStr.append( (char)cout );
								cout = (cIdx << 5) & 255;
								break;
							case 7:
								cout |= cIdx;
								outStr.append( (char)cout );
								break;
						}
						mod = ( mod + 1 ) % 8;
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

		if ( mod != 1 && mod != 3 && mod != 6 ) {
			return outStr.toString();
		}
		else {
			// In state 1 only 5 bits of the next 40 bit has been decoded.
			// In state 3 1 bit must have also been written in the next byte.
			// In state 6 2 bit must have also been written in the next byte.
			return null;
		}
	}

	/**
	 * Decodes a base32 encoded string.
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
				if ( cin == '=' ) {
					b = false;
				}
				else {
					cIdx = convtab.indexOf( cin );
					if ( cIdx != -1 ) {
						switch ( mod ) {
							case 0:
								cout = cIdx << 3;
								break;
							case 1:
								cout |= (cIdx >> 2);
								os.write( cout );
								cout = (cIdx << 6) & 255;
								break;
							case 2:
								cout |= (cIdx << 1);
								break;
							case 3:
								cout |= (cIdx >> 7);
								os.write( cout );
								cout = (cIdx << 4) & 255;
								break;
							case 4:
								cout |= (cIdx >> 1);
								os.write( cout );
								cout = (cIdx << 7) & 255;
								break;
							case 5:
								cout = cIdx << 2;
								break;
							case 6:
								cout |= (cIdx >> 3);
								os.write( cout );
								cout = (cIdx << 5) & 255;
								break;
							case 7:
								cout |= cIdx;
								os.write( cout );
								break;
						}
						mod = ( mod + 1 ) % 8;
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

		if ( mod != 1 && mod != 3 && mod != 6 ) {
			return os.toByteArray();
		}
		else {
			// In state 1 only 5 bits of the next 40 bit has been decoded.
			// In state 3 1 bit must have also been written in the next byte.
			// In state 6 2 bit must have also been written in the next byte.
			return null;
		}
	}

	/**
	 * Encodes a string with base32.
	 * @param inStr unencoded text string.
	 * @return encoded string.
	 */
	public static String encodeFromString(String inStr) {
		StringBuffer outStr = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		int cin;
		int cout = 0;
		int mod = 0;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < inStr.length() ) {
				cin = inStr.charAt( idx++ );
				switch ( mod ) {
					case 0:
						cout = ( cin >> 3 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin << 2 ) & 31;
						break;
					case 1:
						cout |= ( cin >> 6 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin >> 1 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin << 4 ) & 31;
						break;
					case 2:
						cout |= ( cin >> 4) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin << 1 ) & 31;
						break;
					case 3:
						cout |= ( cin >> 7 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin >> 2) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin << 3 ) & 31;
						break;
					case 4:
						cout |= ( cin >> 5 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = cin & 31;
						outStr.append( convtab.charAt( cout ) );
						break;
				}
				mod = ( mod + 1 ) % 5;
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
				outStr.append( convtab.charAt( cout ) );
				outStr.append( "======" );
				break;
			case 2:
				outStr.append( convtab.charAt( cout ) );
				outStr.append( "====" );
				break;
			case 3:
				outStr.append( convtab.charAt( cout ) );
				outStr.append( "===" );
				break;
			case 4:
				outStr.append( convtab.charAt( cout ) );
				outStr.append( "=" );
				break;
		}

		return outStr.toString();
	}

	/**
	 * Encodes a string with base32.
	 * @param inStr unencoded text string.
	 * @return encoded string.
	 */
	public static String encodeFromArray(byte[] inStr) {
		StringBuffer outStr = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		int cin;
		int cout = 0;
		int mod = 0;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < inStr.length ) {
				cin = inStr[ idx++ ];
				switch ( mod ) {
					case 0:
						cout = ( cin >> 3 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin << 2 ) & 31;
						break;
					case 1:
						cout |= ( cin >> 6 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin >> 1 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin << 4 ) & 31;
						break;
					case 2:
						cout |= ( cin >> 4) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin << 1 ) & 31;
						break;
					case 3:
						cout |= ( cin >> 7 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin >> 2) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = ( cin << 3 ) & 31;
						break;
					case 4:
						cout |= ( cin >> 5 ) & 31;
						outStr.append( convtab.charAt( cout ) );
						cout = cin & 31;
						outStr.append( convtab.charAt( cout ) );
						break;
				}
				mod = ( mod + 1 ) % 5;
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
				outStr.append( convtab.charAt( cout ) );
				outStr.append( "======" );
				break;
			case 2:
				outStr.append( convtab.charAt( cout ) );
				outStr.append( "====" );
				break;
			case 3:
				outStr.append( convtab.charAt( cout ) );
				outStr.append( "===" );
				break;
			case 4:
				outStr.append( convtab.charAt( cout ) );
				outStr.append( "=" );
				break;
		}

		return outStr.toString();
	}

}
