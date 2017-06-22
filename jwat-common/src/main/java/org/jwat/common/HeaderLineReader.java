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

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Advanced header/line reader which can be configured into difference modes.
 * The reader can either read normal lines or header lines.
 * Supported encodings are raw, US-ASCII, ISO-8859-1 and UTF-8.
 *
 * Furthermore header lines can employ linear white space (LWS), quoted text
 * and encoded words.
 *
 * After calling the readLine method additional information is available from
 * public fields on the reader.
 *
 * @author nicl
 */
public class HeaderLineReader {

    /*
     * Internal states.
     */

    /** Initial state for reading a normal line. */
    protected static final int S_LINE = 0;
    /** Initial state for reading a header line. */
    protected static final int S_NAME = 1;
    /** State for reading a header value. */
    protected static final int S_VALUE = 2;
    /** State for reading a LWS character sequence. */
    protected static final int S_LWS = 3;
    /** State for reading a quoted string. */
    protected static final int S_QUOTED_TEXT = 4;
    /** State for reading a quoted pair character. */
    protected static final int S_QUOTED_PAIR = 5;
    /** State for reading a quoted LWS character sequence. */
    protected static final int S_QUOTED_LWS = 6;
    /** Status for reading an encoded word character sequence. */
    //protected static final int S_ENCODED_WORD_EQ = 7;

    /*
     * 8-bit character characteristics.
     */

    /** Control character characteristic. */
    protected static final int CC_CONTROL = 1;
    /** Separator character characteristic. */
    protected static final int CC_SEPARATOR_WS = 2;

    /** rfc2616 separator characters. */
    public static final String separatorsWs = "()<>@,;:\\\"/[]?={} \t";

    /** Table of separator characters. */
    public static final byte[] charCharacteristicsTab = new byte[256];

    /**
     * Populate table of separators.
     */
    static {
        for (int i=0; i<32; ++i) {
            if (i != '\t') {
                charCharacteristicsTab[i] |= CC_CONTROL;
            }
        }
        charCharacteristicsTab[127] |= CC_CONTROL;
        for (int i=0; i<separatorsWs.length(); ++i) {
            charCharacteristicsTab[separatorsWs.charAt(i)] |= CC_SEPARATOR_WS;
        }
    }

    /*
     * Encoding.
     */

    /** Raw encoding identifier. */
    public static final int ENC_RAW = 0;
    /** US-ASCII encoding identifier. */
    public static final int ENC_US_ASCII = 1;
    /** ISO-8859-1 encoding identifier. */
    public static final int ENC_ISO8859_1 = 2;
    /** UTF-8 encoding identifier. */
    public static final int ENC_UTF8 = 3;

    /** Reusable UTF-8 validation object instance. */
    protected final UTF8 utf8 = new UTF8();

    /*
     * EOL.
     */

    /** LF end of line identifier. */
    public static final int EOL_LF = 0;
    /** CRLF end of line identifier. */
    public static final int EOL_CRLF = 1;

    /*
     * Configuration.
     */

    /** Parses a headerline if true and a normal line if false. */
    public boolean bNameValue;
    /** Identifier for the expected character encoding. */
    public int encoding = ENC_RAW;
    /** Identifier for the Expected end of line character sequence. */
    public int eol = EOL_CRLF;
    /** Support LWS. */
    public boolean bLWS;
    /** Support quoted text. */
    public boolean bQuotedText;
    /** Support encoded words. */
    public boolean bEncodedWords;

    /** Reusable <code>StringBuffer</code> for lines. */
    protected final StringBuffer lineSb = new StringBuffer();
    /** Reusable <code>StringBuffer</code> for name/value strings. */
    protected final StringBuffer nvSb = new StringBuffer();
    /** Stream used to record the raw characters read by the parser. */
    protected ByteArrayOutputStreamWithUnread bytesOut = new ByteArrayOutputStreamWithUnread();

    /*
     * Error reporting.
     */

    /** Bit denoting unexpected EOF. */
    public static final int E_BIT_EOF = 1 << 0;
    /** Bit denoting a misplaced CR. */
    public static final int E_BIT_MISPLACED_CR = 1 << 1;
    /** Bit denoting a missing CR. */
    public static final int E_BIT_MISSING_CR = 1 << 2;
    /** Bit denoting an unexpected CR. */
    public static final int E_BIT_UNEXPECTED_CR = 1 << 3;
    /** Bit denoting an invalid UTF-8 encoded character. */
    public static final int E_BIT_INVALID_UTF8_ENCODING = 1 << 4;
    /** Bit denoting an invalid US-ASCII character. */
    public static final int E_BIT_INVALID_US_ASCII_CHAR = 1 << 5;
    /** Bit denoting an invalid control character. */
    public static final int E_BIT_INVALID_CONTROL_CHAR = 1 << 6;
    /** Bit denoting an invalid separator character. */
    public static final int E_BIT_INVALID_SEPARATOR_CHAR = 1 << 7;
    /** Bit denoting a missing quote character. */
    public static final int E_BIT_MISSING_QUOTE = 1 << 8;
    /** Bit denoting a missing quoted pair character. */
    public static final int E_BIT_MISSING_QUOTED_PAIR_CHAR = 1 << 9;
    /** Bit denoting an invalid quoted pair character. */
    public static final int E_BIT_INVALID_QUOTED_PAIR_CHAR = 1 << 10;
    /** Bit denoting an invalid encoding. */
    public static final int E_BIT_INVALID_CHARSET = 1 << 11;

    /*
     * Internal state.
     */

    /** True if the previous character was a CR. */
    protected boolean bCr = false;

    /** Used by decode method to indicated valid or non valid character. */
    protected boolean bValidChar;

    /*
     * Exposed state.
     */

    /** Boolean indicating whether or not EOF has been reached on stream. */
    public boolean bEof;

    /** Bit field of errors encountered while attempting to read a line. */
    public int bfErrors;

    /**
     * Prohibit public construction.
     */
    protected HeaderLineReader() {
    }

    /**
     * Returns a reader with default configuration values.
     * @return a reader with default configuration values
     */
    public static HeaderLineReader getReader() {
        return new HeaderLineReader();
    }

    /**
     * Returns a reader initialized to read normal lines.
     * Normal lines being lines with no LWS or key:value headers.
     * The reader is configured to expect US-ASCII characters.
     * @return a reader to read normal lines
     */
    public static HeaderLineReader getLineReader() {
        HeaderLineReader hlr = new HeaderLineReader();
        hlr.bNameValue = false;
        hlr.encoding = ENC_US_ASCII;
        return hlr;
    }

    /**
     * Returns a reader initialized to read header lines.
     * The reader is configured to expect ISO-8859-1 encoding, LWS,
     * quoted text and encoded words. Besides reading key:value headers it will
     * also read and return normal lines as defined in the method above.
     * @return a reader to read header lines
     */
    public static HeaderLineReader getHeaderLineReader() {
        HeaderLineReader hlr = new HeaderLineReader();
        hlr.bNameValue = true;
        hlr.encoding = ENC_ISO8859_1;
        hlr.eol = EOL_CRLF;
        hlr.bLWS = true;
        hlr.bQuotedText = true;
        hlr.bEncodedWords = true;
        return hlr;
    }

    /**
     * Reads a header/line according to the configuration.
     * After calling the readLine method additional information is available
     * from public fields on the reader.
     * @param in <code>InputStream</code> with characters
     * @return result wrapped in a <code>HeaderLine</code> object
     * @throws IOException if an I/O error occurs in the underlying input stream
     */
    public HeaderLine readLine(PushbackInputStream in) throws IOException {
        HeaderLine headerLine = new HeaderLine();
        int state;
        if (!bNameValue) {
            state = S_LINE;
        } else {
            state = S_NAME;
        }
        lineSb.setLength(0);
        nvSb.setLength(0);
        bytesOut = new ByteArrayOutputStreamWithUnread();
        bfErrors = 0;
        int c;
        bCr = false;
        boolean bLoop = true;
        while (bLoop) {
            c = in.read();
            if (c != -1) {
                bytesOut.write(c);
            }
            switch (state) {
            case S_LINE:
                switch (c) {
                case -1:
                    // EOF.
                    bfErrors |= E_BIT_EOF;
                    headerLine.type = HeaderLine.HLT_LINE;
                    headerLine.line = lineSb.toString();
                    lineSb.setLength(0);
                    bLoop = false;
                    break;
                case '\r':
                    bCr = true;
                    break;
                case '\n':
                    headerLine.type = HeaderLine.HLT_LINE;
                    headerLine.line = lineSb.toString();
                    lineSb.setLength(0);
                    // Check EOL.
                    check_eol();
                    bLoop = false;
                    break;
                default:
                    if (bCr) {
                        // Misplaced CR.
                        bfErrors |= E_BIT_MISPLACED_CR;
                        bCr = false;
                    }
                    // Decode character.
                    c = decode(c, in);
                    if (c == -1) {
                        // EOF.
                        bfErrors |= E_BIT_EOF;
                        headerLine.type = HeaderLine.HLT_LINE;
                        headerLine.line = lineSb.toString();
                        lineSb.setLength(0);
                        bLoop = false;
                    } else {
                        if (bValidChar && encoding != ENC_RAW) {
                            if (c < 256 && ((charCharacteristicsTab[c] & CC_CONTROL) == CC_CONTROL)) {
                                bValidChar = false;
                                // Invalid control char
                                bfErrors |= E_BIT_INVALID_CONTROL_CHAR;
                            }
                        }
                        if (bValidChar) {
                            lineSb.append((char) c);
                        }
                    }
                    break;
                }
                break;
            case S_NAME:
                switch (c) {
                case -1:
                    // EOF.
                    bfErrors |= E_BIT_EOF;
                    headerLine.type = HeaderLine.HLT_LINE;
                    headerLine.line = lineSb.toString();
                    lineSb.setLength(0);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                case '\r':
                    bCr = true;
                    break;
                case '\n':
                    headerLine.type = HeaderLine.HLT_LINE;
                    headerLine.line = lineSb.toString();
                    lineSb.setLength(0);
                    nvSb.setLength(0);
                    // Check EOL.
                    check_eol();
                    bLoop = false;
                    break;
                case ':':
                    headerLine.type = HeaderLine.HLT_HEADERLINE;
                    headerLine.name = nvSb.toString();
                    lineSb.setLength(0);
                    nvSb.setLength(0);
                    if (bCr) {
                        // Misplaced CR.
                        bfErrors |= E_BIT_MISPLACED_CR;
                        bCr = false;
                    }
                    state = S_VALUE;
                    break;
                default:
                    if (bCr) {
                        // Misplaced CR.
                        bfErrors |= E_BIT_MISPLACED_CR;
                        bCr = false;
                    }
                    // Decode character.
                    c = decode(c, in);
                    if (c == -1) {
                        // EOF.
                        bfErrors |= E_BIT_EOF;
                        headerLine.type = HeaderLine.HLT_LINE;
                        headerLine.line = lineSb.toString();
                        lineSb.setLength(0);
                        nvSb.setLength(0);
                        bLoop = false;
                    } else {
                        if (bValidChar && encoding != ENC_RAW) {
                            if (c < 256 && ((charCharacteristicsTab[c] & CC_CONTROL) == CC_CONTROL)) {
                                bValidChar = false;
                                // Invalid control char
                                bfErrors |= E_BIT_INVALID_CONTROL_CHAR;
                            }
                        }
                        if (bValidChar) {
                            lineSb.append((char) c);
                            if (c < 256 && ((charCharacteristicsTab[c] & CC_SEPARATOR_WS) == CC_SEPARATOR_WS)) {
                                bValidChar = false;
                                // Invalid separator in name
                                bfErrors |= E_BIT_INVALID_SEPARATOR_CHAR;
                            }
                        }
                        if (bValidChar) {
                            nvSb.append((char) c);
                        }
                    }
                    break;
                }
                break;
            case S_VALUE:
                switch (c) {
                case -1:
                    // EOF.
                    bfErrors |= E_BIT_EOF;
                    headerLine.value = trim(nvSb);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                case '\r':
                    bCr = true;
                    break;
                case '\n':
                    // Check EOL.
                    check_eol();
                    if (bLWS) {
                        state = S_LWS;
                    } else {
                        headerLine.value = trim(nvSb);
                        nvSb.setLength(0);
                        bLoop = false;
                    }
                    break;
                default:
                    if (bCr) {
                        // Misplaced CR.
                        bfErrors |= E_BIT_MISPLACED_CR;
                        bCr = false;
                    }
                    // Decode character.
                    c = decode(c, in);
                    if (c == -1) {
                        // EOF.
                        bfErrors |= E_BIT_EOF;
                        headerLine.value = trim(nvSb);
                        nvSb.setLength(0);
                        bLoop = false;
                    } else {
                        if (bValidChar && encoding != ENC_RAW) {
                            if (c < 256 && ((charCharacteristicsTab[c] & CC_CONTROL) == CC_CONTROL)) {
                                bValidChar = false;
                                // Invalid control char
                                bfErrors |= E_BIT_INVALID_CONTROL_CHAR;
                            }
                        }
                        if (bValidChar) {
                            switch (c) {
                            case '\"':
                                nvSb.append((char)c);
                                if (bQuotedText) {
                                    state = S_QUOTED_TEXT;
                                }
                                break;
/*
                            case '=':
                                if (bEncodedWords) {
                                    state = S_ENCODED_WORD_EQ;
                                } else {
                                    nvSb.append((char)c);
                                }
                                break;
*/
                            default:
                                nvSb.append((char)c);
                                break;
                            }
                        }
                    }
                    break;
                }
                break;
            case S_LWS:
                switch (c) {
                case -1:
                    // EOF.
                    //bfErrors |= E_BIT_EOF;
                    headerLine.value = trim(nvSb);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                case ' ':
                case '\t':
                    nvSb.append(" ");
                    state = S_VALUE;
                    break;
                default:
                    in.unread(c);
                    bytesOut.unread(c);
                    headerLine.value = trim(nvSb);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                }
                break;
            case S_QUOTED_TEXT:
                switch (c) {
                case -1:
                    // EOF.
                    bfErrors |= E_BIT_MISSING_QUOTE | E_BIT_EOF;
                    headerLine.value = trim(nvSb);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                case '\"':
                    if (bCr) {
                        // Misplaced CR.
                        bfErrors |= E_BIT_MISPLACED_CR;
                        bCr = false;
                    }
                    nvSb.append((char)c);
                    state = S_VALUE;
                    break;
                case '\\':
                    if (bCr) {
                        // Misplaced CR.
                        bfErrors |= E_BIT_MISPLACED_CR;
                        bCr = false;
                    }
                    state = S_QUOTED_PAIR;
                    break;
                case '\r':
                    bCr = true;
                    break;
                case '\n':
                    // Check EOL.
                    check_eol();
                    if (bLWS) {
                        state = S_QUOTED_LWS;
                    } else {
                        headerLine.value = trim(nvSb);
                        nvSb.setLength(0);
                        bLoop = false;
                    }
                    break;
                default:
                    if (bCr) {
                        // Misplaced CR.
                        bfErrors |= E_BIT_MISPLACED_CR;
                        bCr = false;
                    }
                    // Decode character.
                    c = decode(c, in);
                    if (c == -1) {
                        // EOF.
                        bfErrors |= E_BIT_MISSING_QUOTE | E_BIT_EOF;
                        headerLine.value = trim(nvSb);
                        nvSb.setLength(0);
                        bLoop = false;
                    } else {
                        if (bValidChar && encoding != ENC_RAW) {
                            if (c < 256 && ((charCharacteristicsTab[c] & CC_CONTROL) == CC_CONTROL)) {
                                bValidChar = false;
                                // Invalid control char
                                bfErrors |= E_BIT_INVALID_CONTROL_CHAR;
                            }
                        }
                        if (bValidChar) {
                            nvSb.append((char)c);
                        }
                    }
                    break;
                }
                break;
            case S_QUOTED_PAIR:
                switch (c) {
                case -1:
                    nvSb.append('\\');
                    // EOF.
                    bfErrors |= E_BIT_MISSING_QUOTED_PAIR_CHAR | E_BIT_MISSING_QUOTE | E_BIT_EOF;
                    headerLine.value = trim(nvSb);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                default:
                    // Decode character.
                    c = decode(c, in);
                    if (c == -1) {
                        // EOF.
                        bfErrors |= E_BIT_MISSING_QUOTED_PAIR_CHAR | E_BIT_MISSING_QUOTE | E_BIT_EOF;
                        headerLine.value = trim(nvSb);
                        nvSb.setLength(0);
                        bLoop = false;
                    } else {
                        nvSb.append('\\');
                        nvSb.append((char)c);
                        if (!bValidChar) {
                            bfErrors |= E_BIT_INVALID_QUOTED_PAIR_CHAR;
                        }
                        state = S_QUOTED_TEXT;
                    }
                    break;
                }
                break;
            case S_QUOTED_LWS:
                switch (c) {
                case -1:
                    // EOF.
                    bfErrors |= E_BIT_MISSING_QUOTE;
                    headerLine.value = trim(nvSb);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                case ' ':
                case '\t':
                    nvSb.append(" ");
                    state = S_QUOTED_TEXT;
                    break;
                default:
                    in.unread(c);
                    bytesOut.unread(c);
                    bfErrors |= E_BIT_MISSING_QUOTE;
                    headerLine.value = trim(nvSb);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                }
                break;
/*
            case S_ENCODED_WORD_EQ:
                switch (c) {
                case -1:
                    nvSb.append('=');
                    // EOF.
                    bfErrors |= E_BIT_EOF;
                    headerLine.value = trim(nvSb);
                    nvSb.setLength(0);
                    bLoop = false;
                    break;
                case '?':
                    //  Unread "=?", so it can be parsed as an EncodedWord which always starts with "=?"
                    in.unread('?');
                    in.unread('=');
                    bytesOut.unread('?');
                    bytesOut.unread('=');
                    EncodedWords ew = EncodedWords.parseEncodedWords(in, true);
*/
                    /*
                    if (!ew.bIsValid) {
                        // TODO Decide whether to report encoded word errors or interpret as non encoded words.
                    }
                    */
/*
                    if (!ew.bValidCharset) {
                        // In case of invalid charset errors, try to report.
                        bfErrors |= E_BIT_INVALID_CHARSET;
                        // Possible message : "Invalid charset : " + ew.charsetStr;
                    }
                    nvSb.append("=?");
                    in.unread(ew.line, 2, ew.line.length - 2);
                    bytesOut.write("=?".getBytes());
                    state = S_VALUE;
                    break;
                default:
                    nvSb.append('=');
                    in.unread(c);
                    bytesOut.unread(c);
                    state = S_VALUE;
                    break;
                }
                break;
*/
            }
        }
        headerLine.raw = bytesOut.toByteArray();
        headerLine.bfErrors = bfErrors;
        bEof = (headerLine.raw.length == 0);
        return headerLine;
    }

    /**
     * Decode a character according to the expected encoding.
     * @param c first character of the possibly encoded character sequence
     * @param in <code>InputStream</code> with possible extra encoded characters.
     * @return decoded character
     * @throws IOException if an I/O error occurs in the underlying input stream
     */
    protected int decode(int c, InputStream in) throws IOException {
        switch (encoding) {
        case ENC_UTF8:
            c = utf8.readUtf8(c, in);
            bytesOut.write(utf8.chars_read);
            bValidChar = utf8.bValidChar;
            if (c != -1) {
                if (!bValidChar) {
                    // Invalid UTF-8 char
                    bfErrors |= E_BIT_INVALID_UTF8_ENCODING;
                }
            }
            break;
        case ENC_US_ASCII:
            bValidChar = (c <= 127);
            if (!bValidChar) {
                // Invalid US-ASCII char
                bfErrors |= E_BIT_INVALID_US_ASCII_CHAR;
            }
            break;
        case ENC_ISO8859_1:
            // ISO-8859-1 utilizes all 8-bits and requires no decoding.
        case ENC_RAW:
            // Raw 8-bit character needs no decoding.
        default:
             bValidChar = true;
            break;
        }
        return c;
    }

    /**
     * Check and report whether the line ended as expected.
     */
    protected void check_eol() {
        switch (eol) {
        case EOL_LF:
            if (!bCr) {
                // Unexpected CR.
                bfErrors |= E_BIT_UNEXPECTED_CR;
            }
            break;
        case EOL_CRLF:
            if (!bCr) {
                // Missing CR.
                bfErrors |= E_BIT_MISSING_CR;
            }
            break;
        }
        bCr = false;
    }

    /**
     * Trims the whitespace characters found in the beginning and end of a
     * string. Differs from the String method in that it leaves control
     * characters.
     * @param sb <code>StringBuffer</code> to be trimmed
     * @return trimmed string
     */
    public static String trim(StringBuffer sb) {
        int sIdx = 0;
        int eIdx = sb.length();
        while (sIdx < eIdx && sb.charAt(sIdx) == ' ') {
            ++sIdx;
        }
        while (eIdx > sIdx && sb.charAt(eIdx - 1) == ' ') {
            --eIdx;
        }
        return sb.substring(sIdx, eIdx);
    }

    /**
     * Report bit field errors as diagnoses.
     * @param bfErrors bit field with indicated errors
     * @param diagnostics diagnostics object used to report diagnoses
     */
    public static void report_error(int bfErrors, Diagnostics<Diagnosis> diagnostics) {
        if (diagnostics == null) {
            throw new IllegalArgumentException("'diagnostics' argument is null");
        }
        if ((bfErrors & E_BIT_EOF) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Unexpected EOF"));
        }
        if ((bfErrors & E_BIT_MISPLACED_CR) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Misplaced CR"));
        }
        if ((bfErrors & E_BIT_MISSING_CR) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Missing CR"));
        }
        if ((bfErrors & E_BIT_UNEXPECTED_CR) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Unexpected CR"));
        }
        if ((bfErrors & E_BIT_INVALID_UTF8_ENCODING) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Invalid UTF-8 encoded character"));
        }
        if ((bfErrors & E_BIT_INVALID_US_ASCII_CHAR) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Invalid US-ASCII character"));
        }
        if ((bfErrors & E_BIT_INVALID_CONTROL_CHAR) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Invalid control character"));
        }
        if ((bfErrors & E_BIT_INVALID_SEPARATOR_CHAR) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Invalid separator character"));
        }
        if ((bfErrors & E_BIT_MISSING_QUOTE) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Missing quote character"));
        }
        if ((bfErrors & E_BIT_MISSING_QUOTED_PAIR_CHAR) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Missing quoted pair character"));
        }
        if ((bfErrors & E_BIT_INVALID_QUOTED_PAIR_CHAR) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Invalid quoted pair character"));
        }
        if ((bfErrors & E_BIT_INVALID_CHARSET) != 0) {
            diagnostics.addError(new Diagnosis(DiagnosisType.ERROR, "header/line", "Invalid charset"));
        }
    }

}
