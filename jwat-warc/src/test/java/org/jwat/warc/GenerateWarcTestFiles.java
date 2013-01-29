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
package org.jwat.warc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GenerateWarcTestFiles {

    public static final byte[] lfByte = new byte[] {0x0a};

    public static int saveTestWarcHeaderFieldPolicyCount = 1;

    public static synchronized void saveTestWarcHeaderFieldPolicy(byte[] bytes) throws IOException {
        saveFile("invalid-warcheaderfieldpolicy-" + saveTestWarcHeaderFieldPolicyCount + ".warc", bytes);
        ++saveTestWarcHeaderFieldPolicyCount;
    }

    public static int saveTestWarcHeaderVersionCount = 1;

    public static synchronized void saveTestWarcHeaderVersion(byte[] bytes) throws IOException {
        if ( bytes.length != 0) {
            saveFile("invalid-warcheaderversion-" + saveTestWarcHeaderVersionCount + ".warc", bytes);
            ++saveTestWarcHeaderVersionCount;
        }
    }

    public static int saveTestWarcReader_DiagnosisCount = 1;

    public static synchronized void saveTestWarcReader_Diagnosis(byte[] bytes) throws IOException {
        saveFile("invalid-warcreader-diagnosis-" + saveTestWarcReader_DiagnosisCount + ".warc", bytes);
        ++saveTestWarcReader_DiagnosisCount;
    }

    public static int saveTestWarcRecordInvalidCount = 1;
    public static int saveTestWarcRecordValidCount = 1;

    public static synchronized void saveTestWarcRecord(byte[] bytes, boolean valid) throws IOException {
        if (valid) {
            saveFile("valid-warcrecord-" + saveTestWarcRecordValidCount + ".warc", bytes);
            ++saveTestWarcRecordValidCount;
        } else {
            saveFile("invalid-warcrecord-" + saveTestWarcRecordInvalidCount + ".warc", bytes);
            ++saveTestWarcRecordInvalidCount;
        }
    }

    public static int saveTestWarcRecordDigestsInvalidCount = 1;
    public static int saveTestWarcRecordDigestsValidCount = 1;

    public static synchronized void saveTestWarcRecordDigests(byte[] bytes, boolean valid) throws IOException {
        if (valid) {
            saveFile("valid-warcrecorddigests-" + saveTestWarcRecordDigestsValidCount + ".warc", bytes);
            ++saveTestWarcRecordDigestsValidCount;
        } else {
            saveFile("invalid-warcrecorddigests-" + saveTestWarcRecordDigestsInvalidCount + ".warc", bytes);
            ++saveTestWarcRecordDigestsInvalidCount;
        }
    }

    public static void saveFile(String fname, byte[] bytes) throws IOException {
        /*
        File file = new File(fname);
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0L);
        raf.setLength(0L);
        raf.write(bytes);
        raf.close();
        */
    }

}
