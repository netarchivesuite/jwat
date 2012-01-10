package org.jwat.common;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Base2 encoder/decoder implementation.
 *
 * @author nicl
 */
public class Base2 {

    /** Ascii table used to encode. */
    public static String[] encodeTab = {
        "0000",
        "0001",
        "0010",
        "0011",
        "0100",
        "0101",
        "0110",
        "0111",
        "1000",
        "1001",
        "1010",
        "1011",
        "1100",
        "1101",
        "1110",
        "1111"
    };

    /** Table used to decode. */
    public static byte[] decodeTab = new byte[ 256 ];

    /** Populate decode table. */
    static {
        Arrays.fill( decodeTab, (byte)0xff );
        decodeTab[ '0' ] = 0;
        decodeTab[ '1' ] = 1;
    }

    /**
     * Static class.
     */
    private Base2() {
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
        while ( b ) {
            if ( idx < in.length() ) {
                cin = in.charAt( idx++ );
                cIdx = decodeTab[ cin ];
                if ( cIdx != -1 ) {
                    cout = (cout << 1) | cIdx;
                    mod = ( mod + 1 ) % 8;
                    if ( mod == 0 ) {
                        out.append( (char)cout );
                        cout = 0;
                    }
                }
                else {
                    return null;
                }
            }
            else {
                b = false;
            }
        }
        if ( mod == 0 ) {
            return out.toString();
        }
        else {
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
        while ( b ) {
            if ( idx < in.length() ) {
                cin = in.charAt( idx++ );
                cIdx = decodeTab[ cin ];
                if ( cIdx != -1 ) {
                    cout = (cout << 1) | cIdx;
                    mod = ( mod + 1 ) % 8;
                    if ( mod == 0 ) {
                        out.write( cout );
                    }
                }
                else {
                    return null;
                }
            }
            else {
                b = false;
            }
        }
        if ( mod == 0 ) {
            return out.toByteArray();
        }
        else {
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
        StringBuffer out = new StringBuffer( in.length() << 3 );
        int cin;
        for ( int i=0; i<in.length(); ++i ) {
            cin = in.charAt( i );
            if ( cin < 256 ) {
                out.append( encodeTab[ (cin >> 4) & 15  ] );
                out.append( encodeTab[ cin & 15 ] );
            }
            else {
                return null;
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
        StringBuffer out = new StringBuffer( in.length << 3 );
        for ( int i=0; i<in.length; ++i ) {
            out.append( encodeTab[ (in[ i ] >> 4) & 15  ] );
            out.append( encodeTab[ in[ i ] & 15 ] );
        }
        return out.toString();
    }

    /**
     * Insert regular delimiters in a string.
     * @param inStr input string
     * @param width interval width
     * @param delimiter delimit character
     * @return input string with delimiters inserted at specified intervals
     */
    public static String delimit(String inStr, int width, char delimiter) {
        if (inStr == null) {
            return null;
        }
        if (inStr.length() == 0) {
            return "";
        }
        if ( width <= 0 ) {
            return inStr;
        }
        StringBuffer outSb = new StringBuffer( inStr.length() + (inStr.length() + width - 1) / width );
        int idx = 0;
        int lIdx = width;
        while ( idx < inStr.length() ) {
            if ( idx == lIdx ) {
                outSb.append( delimiter );
                lIdx += width;
            }
            outSb.append( inStr.charAt( idx++ ) );
        }
        return outSb.toString();
    }

}
