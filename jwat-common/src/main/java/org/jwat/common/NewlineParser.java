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

public class NewlineParser {

    /** Did the reader detect a missing CR while parsing newlines. */
    public boolean bMissingCr = false;

    /** Did the reader detect a missing LF while parsing newlines. */
    public boolean bMissingLf = false;

    /** Did the reader detect a misplaced CR while parsing newlines. */
    public boolean bMisplacedCr = false;

    /** Did the reader detect a misplaced LF while parsing newlines. */
    public boolean bMisplacedLf = false;

    /**
     * Looks forward in the inputstream and counts the number of newlines
     * found. Non newlines characters are pushed back onto the inputstream.
     * @param in data inputstream
     * @return newlines found in inputstream
     * @throws IOException if an error occurs while reading data
     */
    public int parseLFs(ByteCountingPushBackInputStream in, Diagnostics<Diagnosis> diagnostics) throws IOException {
        bMissingCr = false;
        bMissingLf = false;
        bMisplacedCr = false;
        bMisplacedLf = false;
        int newlines = 0;
        byte[] buffer = new byte[2];
        boolean b = true;
        while (b) {
            int read = in.read(buffer);
            switch (read) {
            case 1:
                if (buffer[0] == '\n') {
                    ++newlines;
                } else if ((buffer[0] == '\r')) {
                    ++newlines;
                    bMissingLf = true;
                    bMisplacedCr = true;
                } else {
                    in.unread(buffer[0]);
                    b = false;
                }
                break;
            case 2:
                if (buffer[0] == '\n') {
                    if (buffer[1] == '\n') {
                        newlines += 2;
                    } else  if (buffer[1] != '\r') {
                        ++newlines;
                        in.unread(buffer[1]);
                    } else {
                        ++newlines;
                        bMisplacedCr = true;
                    }
                } else if (buffer[0] == '\r') {
                    if (buffer[1] == '\n') {
                        ++newlines;
                        bMisplacedCr = true;
                        bMisplacedLf = true;
                    } else {
                        ++newlines;
                        bMisplacedCr = true;
                        bMissingLf = true;
                        in.unread(buffer[1]);
                    }
                } else {
                    in.unread(buffer);
                    b = false;
                }
                break;
            default:
                b = false;
                break;
            }
        }
        /*
        if (bMissingCr) {
            diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    "Missing CR"));
        }
        */
        if (bMissingLf) {
            diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    "Missing LF"));
        }
        if (bMisplacedCr) {
            diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    "Misplaced CR"));
        }
        if (bMisplacedLf) {
            diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    "Misplaced LF"));
        }
        return newlines;
    }

    /**
     * Looks forward in the inputstream and counts the number of newlines
     * found. Non newlines characters are pushed back onto the inputstream.
     * @param in data inputstream
     * @return newlines found in inputstream
     * @throws IOException if an error occurs while reading data
     */
    public int parseCRLFs(ByteCountingPushBackInputStream in, Diagnostics<Diagnosis> diagnostics) throws IOException {
        bMissingCr = false;
        bMissingLf = false;
        bMisplacedCr = false;
        bMisplacedLf = false;
        int newlines = 0;
        byte[] buffer = new byte[2];
        boolean b = true;
        while (b) {
            int read = in.read(buffer);
            switch (read) {
            case 1:
                if (buffer[0] == '\n') {
                    ++newlines;
                    bMissingCr = true;
                } else if ((buffer[0] == '\r')) {
                    ++newlines;
                    bMissingLf = true;
                } else {
                    in.unread(buffer[0]);
                    b = false;
                }
                break;
            case 2:
                if (buffer[0] == '\r') {
                    if (buffer[1] == '\n') {
                        ++newlines;
                    } else {
                        ++newlines;
                        bMissingLf = true;
                        in.unread(buffer[1]);
                    }
                } else if (buffer[0] == '\n') {
                    if (buffer[1] == '\r') {
                        ++newlines;
                        bMisplacedCr = true;
                        bMisplacedLf = true;
                    } else {
                        ++newlines;
                        bMissingCr = true;
                        in.unread(buffer[1]);
                    }
                } else {
                    in.unread(buffer);
                    b = false;
                }
                break;
            default:
                b = false;
                break;
            }
        }
        if (bMissingCr) {
            diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    "Missing CR"));
        }
        if (bMissingLf) {
            diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    "Missing LF"));
        }
        if (bMisplacedCr) {
            diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    "Misplaced CR"));
        }
        if (bMisplacedLf) {
            diagnostics.addWarning(new Diagnosis(DiagnosisType.ERROR_EXPECTED,
                    "Misplaced LF"));
        }
        return newlines;
    }

}
