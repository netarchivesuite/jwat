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
import java.io.PushbackInputStream;

public class HeaderLineReader {

    private static final int S_LINE = 0;
    private static final int S_NAME = 1;
    private static final int S_VALUE = 2;
    private static final int S_LWS = 3;
    private static final int S_QUOTED_TEXT = 4;
    private static final int S_QUOTED_PAIR = 5;
    private static final int S_QUOTED_LWS = 6;

    private static final int CC_CONTROL = 1;
    private static final int CC_SEPARATOR_WS = 2;

    public static final int ENC_RAW = 0;
    public static final int ENC_US_ASCII = 1;
    public static final int ENC_ISO8859_1 = 2;
    public static final int ENC_UTF8 = 3;

    private final UTF8 utf8 = new UTF8();

    private final StringBuffer lineSb = new StringBuffer();
    private final StringBuffer nvSb = new StringBuffer();

    /** rfc2616 separator characters. */
    public static final String separatorsWs = "()<>@,;:\\\"/[]?={} \t";

    /** Table of separator characters. */
    private static final byte[] charCharacteristicsTab = new byte[256];

    public boolean bNameValue;
    public int encoding = ENC_RAW;
    public boolean bLWS;
    public boolean bQuotedText;
    public boolean bEncodedWords;

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

    private HeaderLineReader() {
    }

    public static HeaderLineReader getReader() {
        return new HeaderLineReader();
    }

    public static HeaderLineReader getLineReader() {
        HeaderLineReader hlr = new HeaderLineReader();
        hlr.bNameValue = false;
        hlr.encoding = ENC_US_ASCII;
        return hlr;
    }

    public static HeaderLineReader getHeaderLineReader() {
        HeaderLineReader hlr = new HeaderLineReader();
        hlr.bNameValue = true;
        hlr.encoding = ENC_ISO8859_1;
        hlr.bLWS = true;
        hlr.bQuotedText = true;
        hlr.bEncodedWords = true;
        return hlr;
    }

    public HeaderLine readLine(PushbackInputStream in) throws IOException {
        HeaderLine headerLine = null;
        int state;
        if (!bNameValue) {
            state = S_LINE;
        } else {
            state = S_NAME;
        }
        lineSb.setLength(0);
        nvSb.setLength(0);
        int c;
        boolean bCr = false;
        boolean bLoop = true;
        while (bLoop) {
            c = in.read();
            switch (state) {
            case S_LINE:
                switch (c) {
                case -1:
                    // EOF.
                    return null;
                case '\r':
                    bCr = true;
                    break;
                case '\n':
                    // TODO what types of encoding have we seen to far?
                    headerLine = new HeaderLine();
                    headerLine.line = lineSb.toString();
                    if (!bCr) {
                        // Missing CR.
                        bCr = false;
                    }
                    bLoop = false;
                    break;
                default:
                    if (!bCr) {
                        // Misplaced CR.
                        bCr = false;
                    }
                    boolean bValidChar;
                    switch (encoding) {
                    case ENC_UTF8:
                        c = utf8.readUtf8(c, in);
                        if (c == -1) {
                            // EOF.
                            return null;
                        }
                        bValidChar = utf8.bValidChar;
                        if (!bValidChar) {
                            // TODO invalid UTF-8 char
                        }
                        break;
                    case ENC_US_ASCII:
                        bValidChar = (c <= 127);
                        if (!bValidChar) {
                            // TODO invalid US-ASCII char
                        }
                        break;
                    case ENC_ISO8859_1:
                    case ENC_RAW:
                    default:
                         bValidChar = true;
                        break;
                    }
                    if (encoding != ENC_RAW) {
                        if (c < 256 && ((charCharacteristicsTab[c] & CC_CONTROL) == CC_CONTROL)) {
                            bValidChar = false;
                            // TODO invalid control char
                        }
                    }
                    if (bValidChar) {
                        lineSb.append((char) c);
                    }
                    break;
                }
                break;
            case S_NAME:
                switch (c) {
                case -1:
                    // EOF.
                    return null;
                case '\r':
                    bCr = true;
                    break;
                case '\n':
                    // TODO what types of encoding have we seen to far?
                    headerLine = new HeaderLine();
                    headerLine.line = lineSb.toString();
                    if (!bCr) {
                        // Missing CR.
                        bCr = false;
                    }
                    bLoop = false;
                    break;
                case ':':
                    // TODO what types of encoding have we seen to far?
                    headerLine = new HeaderLine();
                    headerLine.name = nvSb.toString();
                    nvSb.setLength(0);
                    if (bCr) {
                        // Misplaced CR.
                        bCr = false;
                    }
                    state = S_VALUE;
                    break;
                default:
                    if (bCr) {
                        // Misplaced CR.
                        bCr = false;
                    }
                    boolean bValidChar;
                    switch (encoding) {
                    case ENC_UTF8:
                        c = utf8.readUtf8(c, in);
                        if (c == -1) {
                            // EOF.
                            return null;
                        }
                        bValidChar = utf8.bValidChar;
                        if (!bValidChar) {
                            // TODO invalid UTF-8 char
                        }
                        break;
                    case ENC_US_ASCII:
                        bValidChar = (c <= 127);
                        if (!bValidChar) {
                            // TODO invalid US-ASCII char
                        }
                        break;
                    case ENC_ISO8859_1:
                    case ENC_RAW:
                    default:
                         bValidChar = true;
                        break;
                    }
                    if (encoding != ENC_RAW) {
                        if (c < 256 && ((charCharacteristicsTab[c] & CC_CONTROL) == CC_CONTROL)) {
                            bValidChar = false;
                            // TODO invalid control char
                        }
                    }
                    if (bValidChar) {
                        lineSb.append((char) c);
                        if (c < 256 && ((charCharacteristicsTab[c] & CC_SEPARATOR_WS) == CC_SEPARATOR_WS)) {
                            bValidChar = false;
                            // TODO invalid separator in name
                        }
                    }
                    if (bValidChar) {
                        nvSb.append((char) c);
                    }
                    break;
                }
                break;
            case S_VALUE:
                switch (c) {
                case -1:
                    // EOF.
                    return null;
                case '\r':
                    bCr = true;
                    break;
                case '\n':
                    if (!bCr) {
                        // Missing CR.
                        bCr = false;
                    }
                    if (bLWS) {
                        state = S_LWS;
                    } else {
                        // TODO what types of encoding etc. have we seen so far
                        headerLine.value = nvSb.toString().trim();
                        bLoop = false;
                    }
                    break;
                default:
                    if (bCr) {
                        // Misplaced CR.
                        bCr = false;
                    }
                    boolean bValidChar;
                    switch (encoding) {
                    case ENC_UTF8:
                        c = utf8.readUtf8(c, in);
                        if (c == -1) {
                            // EOF.
                            return null;
                        }
                        bValidChar = utf8.bValidChar;
                        if (!bValidChar) {
                            // TODO invalid UTF-8 char
                        }
                        break;
                    case ENC_US_ASCII:
                        bValidChar = (c <= 127);
                        if (!bValidChar) {
                            // TODO invalid US-ASCII char
                        }
                        break;
                    case ENC_ISO8859_1:
                    case ENC_RAW:
                    default:
                         bValidChar = true;
                        break;
                    }
                    if (encoding != ENC_RAW) {
                        if (c < 256 && ((charCharacteristicsTab[c] & CC_CONTROL) == CC_CONTROL)) {
                            bValidChar = false;
                            // TODO invalid control char
                        }
                    }
                    if (bValidChar) {
                        switch (c) {
                        case '\"':
                            nvSb.append((char)c);
                            state = S_QUOTED_TEXT;
                            break;
                        case '=':
                            nvSb.append((char)c);
                            break;
                        default:
                            nvSb.append((char)c);
                            break;
                        }
                    }
                    break;
                }
                break;
            case S_LWS:
                switch (c) {
                case -1:
                    // EOF.
                    // TODO what types of encoding etc. have we seen so far
                    headerLine.value = nvSb.toString().trim();
                    bLoop = false;
                    break;
                case ' ':
                case '\t':
                    nvSb.append(" ");
                    state = S_VALUE;
                    break;
                default:
                    in.unread(c);
                    // TODO what types of encoding etc. have we seen so far
                    headerLine.value = nvSb.toString().trim();
                    bLoop = false;
                    break;
                }
                break;
            case S_QUOTED_TEXT:
                switch (c) {
                case '\"':
                    if (bCr) {
                        // Misplaced CR.
                        bCr = false;
                    }
                    nvSb.append((char)c);
                    state = S_VALUE;
                    break;
                case '\\':
                    if (bCr) {
                        // Misplaced CR.
                        bCr = false;
                    }
                    nvSb.append((char)c);
                    state = S_QUOTED_PAIR;
                    break;
                case '\r':
                    bCr = true;
                    break;
                case '\n':
                    if (!bCr) {
                        // Missing CR.
                        bCr = false;
                    }
                    state = S_QUOTED_LWS;
                    break;
                default:
                    if (bCr) {
                        // Misplaced CR.
                        bCr = false;
                    }
                    boolean bValidChar;
                    switch (encoding) {
                    case ENC_UTF8:
                        c = utf8.readUtf8(c, in);
                        if (c == -1) {
                            // EOF.
                            return null;
                        }
                        bValidChar = utf8.bValidChar;
                        if (!bValidChar) {
                            // TODO invalid UTF-8 char
                        }
                        break;
                    case ENC_US_ASCII:
                        bValidChar = (c <= 127);
                        if (!bValidChar) {
                            // TODO invalid US-ASCII char
                        }
                        break;
                    case ENC_ISO8859_1:
                    case ENC_RAW:
                    default:
                         bValidChar = true;
                        break;
                    }
                    if (encoding != ENC_RAW) {
                        if (c < 256 && ((charCharacteristicsTab[c] & CC_CONTROL) == CC_CONTROL)) {
                            bValidChar = false;
                            // TODO invalid control char
                        }
                    }
                    if (bValidChar) {
                        nvSb.append((char)c);
                    }
                    break;
                }
                break;
            case S_QUOTED_PAIR:
                nvSb.append((char)c);
                state = S_QUOTED_TEXT;
                break;
            case S_QUOTED_LWS:
                if (c == ' ' || c == '\t') {
                    nvSb.append(" ");
                    state = S_QUOTED_TEXT;
                } else {
                    // TODO Non LWS force end of quoted text parsing and header line.
                    in.unread(c);
                    headerLine.value = nvSb.toString().trim();
                    bLoop = false;
                }
                break;
            }
        }
        return headerLine;
    }

}
