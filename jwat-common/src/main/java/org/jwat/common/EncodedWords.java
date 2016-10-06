/**
 * Java Web Archive Toolkit - Software to read and validate ARC, WARC
 * and GZip files. (http://jwat.org/)
 * Copyright 2011-2012 Netarkivet.dk (http://netarkivet.dk/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwat.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Class used to decode and validate an Encoded-Word string as specified
 * in RFC 2047.
 *
 * Examples:
 * =?US-ASCII?Q?Keith_Moore?=
 * =?ISO-8859-1?Q?Keld_J=F8rn_Simonsen?=
 * =?ISO-8859-1?Q?Andr=E9?=
 * =?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?=
 * =?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=
 *
 * @author nicl
 */
public class EncodedWords {

    /** Looking for an equal sign state. */
    protected static final int S_START_EQ = 0;
    /** Looking for a question mark state. */
    protected static final int S_START_QM = 1;
    /** Reading the charset state. */
    protected static final int S_CHARSET = 2;
    /** Reading the encoding state. */
    protected static final int S_ENCODING = 3;
    /** Reding the encoded words state. */
    protected static final int S_ENCODED_WORDS = 4;
    /** Looking for the last equal sign state. */
    protected static final int S_END_EQ = 5;

    /** Base64 encoding id. */
    public static final int ENC_BASE64 = 1;
    /** Quoted-Printable encoding id. */
    public static final int ENC_QUOTEDPRINTABLE = 2;

    /** Control character characteristic. */
    protected static final int CC_CONTROL = 1;
    /** Separator character characteristic. */
    protected static final int CC_SEPARATOR_WS = 2;

    /** rfc2616 separator minus space and tab. */
    protected static final String separators = "()<>@,;:\\\"/[]?={} \t";

    /** Table of separator and control characters. */
    protected static final byte[] charCharacteristicsTab = new byte[256];

    /*
     * Populate table with separator and control characters.
     */
    static {
        for (int i=0; i<separators.length(); ++i) {
            charCharacteristicsTab[separators.charAt(i)] = CC_SEPARATOR_WS;
        }
        for (int i=0; i<32; ++i) {
            charCharacteristicsTab[i] = CC_CONTROL;
        }
    }

    /** Indicates whether the charset is valid or not. */
    public boolean bValidCharset;

    /** Raw charset name. */
    public String charsetStr;

    /** Identified encoding, 0 indicates an unknown encoding. */
    public int encoding;

    /** Raw encoding name. */
    public String encodingStr;

    /** Raw encoded text string */
    public String encoded_text;

    /** Indicates whether there were errors during character conversion. */
    public boolean bConversionError;

    /** Decoded text, after decoding and character conversion. */
    public String decoded_text;

    /** Raw bytes parsed, usable to re-insert original string after validation. */
    public byte[] line;

    /** Indicates where the encoded words could be validated and decoded. */
    public boolean bIsValid = false;

    /**
     * Attempt to validate and decode an encoded-word from the input stream.
     * In case of failure the already parsed raw data is available for further
     * processing.
     * @param in input stream containing a possible encoded-word
     * @param bParseEqQm true if we should look for leading "=?", false if not
     * @return result of the validation and decoding
     * @throws IOException if an i/o error occurs during parsing
     */
    public static EncodedWords parseEncodedWords(InputStream in, boolean bParseEqQm) throws IOException {
        EncodedWords ew = new EncodedWords();
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        StringBuffer sb = new StringBuffer();
        Charset charset = null;
        int state;
        if (bParseEqQm) {
            state = S_START_EQ;
        } else {
            state = S_CHARSET;
        }
        int c;
        boolean bLoop = true;
        while (bLoop) {
            c = in.read();
            if (c != -1) {
                lineOut.write(c);
            }
            switch (state) {
            case S_START_EQ:
                if (c == '=') {
                    state = S_START_QM;
                } else {
                    // -1 etc.
                    bLoop = false;
                }
                break;
            case S_START_QM:
                if (c == '?') {
                    state = S_CHARSET;
                } else {
                    // -1 etc.
                    bLoop = false;
                }
                break;
            case S_CHARSET:
                switch (c) {
                case -1:
                case '\r':
                case '\n':
                    bLoop = false;
                    break;
                case '?':
                    ew.charsetStr = sb.toString().toUpperCase();
                    sb.setLength(0);
                    if (ew.charsetStr.length() > 0) {
                        try {
                            charset = Charset.forName(ew.charsetStr);
                            ew.bValidCharset = true;
                        } catch (IllegalCharsetNameException e) {
                            //System.err.println("ERROR IllegalCharsetNameException Charset: " + ew.charsetStr + " => " + e.getMessage());
                            charset = Charset.defaultCharset();
                            ew.bValidCharset = false;
                        } catch (UnsupportedCharsetException e) {
                            // System.err.println("ERROR UnsupportedCharsetException Charset: " + ew.charsetStr + " => " + e.getMessage());
                            charset = Charset.defaultCharset();
                            ew.bValidCharset = false;
                        }
                        state = S_ENCODING;
                    } else {
                        bLoop = false;
                    }
                    break;
                default:
                    if (charCharacteristicsTab[c] == 0 && c < 127) {
                        sb.append((char) c);
                    } else {
                        bLoop = false;
                    }
                    break;
                }
                break;
            case S_ENCODING:
                switch (c) {
                case -1:
                case '\r':
                case '\n':
                    bLoop = false;
                    break;
                case '?':
                    ew.encodingStr = sb.toString().toUpperCase();
                    sb.setLength(0);
                    if (ew.encodingStr.length() > 0) {
                        if ("b".equalsIgnoreCase(ew.encodingStr)) {
                            ew.encoding = ENC_BASE64;
                        } else if ("q".equalsIgnoreCase(ew.encodingStr)) {
                            ew.encoding = ENC_QUOTEDPRINTABLE;
                        }
                        state = S_ENCODED_WORDS;
                    } else {
                        bLoop = false;
                    }
                    break;
                default:
                    if (charCharacteristicsTab[c] == 0 && c < 127) {
                        sb.append((char) c);
                    } else {
                        bLoop = false;
                    }
                    break;
                }
                break;
            case S_ENCODED_WORDS:
                switch (c) {
                case -1:
                case '\r':
                case '\n':
                    bLoop = false;
                    break;
                case '?':
                    ew.encoded_text = sb.toString();
                    sb.setLength(0);
                    byte[] decoded = null;
                    if (ew.encoding == ENC_BASE64) {
                        decoded = Base64.decodeToArray(ew.encoded_text, true);
                    } else if (ew.encoding == ENC_QUOTEDPRINTABLE) {
                        decoded = QuotedPrintable.decode(ew.encoded_text);
                    } else {
                        // debug
                        //System.out.println("Encoding: " + ew.encodingStr);
                    }
                    if (decoded != null) {
                        ByteBuffer bb = ByteBuffer.wrap(decoded);
                        CharBuffer cb = CharBuffer.allocate(bb.capacity());
                        CharsetDecoder decoder = charset.newDecoder();
                        decoder.onMalformedInput(CodingErrorAction.REPORT);
                        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                        try {
                            boolean bDecodeLoop = true;
                            while (bDecodeLoop) {
                                CoderResult result = decoder.decode(bb, cb, true);
                                sb.append(cb.array(), cb.arrayOffset(), cb.position());
                                cb.clear();
                                if (result == CoderResult.UNDERFLOW) {
                                       bDecodeLoop = false;
                                } else if (result.isError()) {
                                    bb.position(Math.min(bb.position() + result.length(), bb.limit()));
                                    sb.append('?');
                                    ew.bConversionError = true;
                                }
                            }
                        } catch (CoderMalfunctionError e) {
                        }
                        ew.decoded_text = sb.toString();
                    }
                    state = S_END_EQ;
                    break;
                default:
                    if (c > 32 && c < 127) {
                        sb.append((char) c);
                    } else {
                        bLoop = false;
                    }
                    break;
                }
                break;
            case S_END_EQ:
                if (c == -1) {
                    bLoop = false;
                } else if (c == '=') {
                    ew.bIsValid = true;
                    bLoop = false;
                }
                break;
            }
        }
        ew.line = lineOut.toByteArray();
        ew.bIsValid = ew.bIsValid & ew.bValidCharset & ew.encoding != 0 && ew.decoded_text != null;
        return ew;
    }

}
