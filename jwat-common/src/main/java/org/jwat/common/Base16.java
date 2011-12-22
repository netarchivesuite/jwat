package org.jwat.common;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Base16 encoder/decoder implementation based on the specifications in
 * rfc3548.
 *
 * @author nicl
 */
public class Base16 {

	/** Ascii table used to encode. */
	public static String encodeTab = "0123456789ABCDEF";

	/** Table used to decode. */
	public static byte[] decodeTab = new byte[ 256 ];

	/** Populate decode table. */
	static {
		Arrays.fill( decodeTab, (byte)0xff );
		for ( int i=0; i<encodeTab.length(); ++i ) {
			decodeTab[ Character.toUpperCase( encodeTab.charAt( i ) ) ] = (byte)i;
			decodeTab[ Character.toLowerCase( encodeTab.charAt( i ) ) ] = (byte)i;
		}
	}

	/**
	 * Static class.
	 */
	private Base16() {
	}

	/**
	 * Decodes an encoded string.
	 * @param in encoded string.
	 * @return decoded string or null
	 */
	public static String decodeToString(String in) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return "";
		}

		StringBuffer out = new StringBuffer( 256 );

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
			if ( idx < in.length() ) {
				cin = in.charAt( idx++ );
				cIdx = decodeTab[ cin ];
				if ( cIdx != -1 ) {
					switch ( mod ) {
						case 0:
							cout = cIdx << 4;
							break;
						case 1:
							cout |= cIdx;
							out.append( (char)cout );
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
			return out.toString();
		}
		else {
			// In state 1 only if decoded a half byte.
			return null;
		}
	}

	/**
	 * Decodes an encoded string.
	 * @param in encoded string.
	 * @return decoded byte array or null
	 */
	public static byte[] decodeToArray(String in) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return new byte[0];
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

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
			if ( idx < in.length() ) {
				cin = in.charAt( idx++ );
				cIdx = decodeTab[ cin ];
				if ( cIdx != -1 ) {
					switch ( mod ) {
						case 0:
							cout = cIdx << 4;
							break;
						case 1:
							cout |= cIdx;
							out.write( cout );
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
			return out.toByteArray();
		}
		else {
			// In state 1 only if decoded a half byte.
			return null;
		}
	}

	/**
	 * Encodes a string.
	 * @param in unencoded text string.
	 * @return encoded string or null
	 */
	public static String encodeString(String in) {
		if (in == null) {
			return null;
		}
		if (in.length() == 0) {
			return "";
		}

		StringBuffer out = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		int cin;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < in.length() ) {
				cin = in.charAt( idx++ );
				if ( cin < 256 ) {
					out.append( encodeTab.charAt( (cin >> 4) & 15 ) );
					out.append( encodeTab.charAt( cin & 15 ) );
				}
				else {
					return null;
				}
			}
			else {
				b = false;
			}
		}

		return out.toString();
	}

	/**
	 * Encodes a byte array.
	 * @param in byte array.
	 * @return encoded string or null
	 */
	public static String encodeArray(byte[] in) {
		if (in == null) {
			return null;
		}
		if (in.length == 0) {
			return "";
		}

		StringBuffer out = new StringBuffer( 256 );

		boolean b = true;
		int idx = 0;
		int cin;

		/*
		 * Loop.
		 */

		while ( b ) {
			if ( idx < in.length ) {
				cin = in[ idx++ ] & 255;
				out.append( encodeTab.charAt( (cin >> 4) & 15 ) );
				out.append( encodeTab.charAt( cin & 15 ) );
			}
			else {
				b = false;
			}
		}

		return out.toString();
	}

}
