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
package org.jwat.gzip;

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing all relevant GZip constants and structures.
 * Including but not limited to compression methods, flags
 * and known Operating Systems.
 * Also includes non statically initialized structures for validation.
 *
 * @author nicl
 */
public class GzipConstants {

    /** GZip header magic number. */
    public static final byte[] GZIP_MAGIC_HEADER = new byte[] {(byte)0x1f, (byte)0x8b};

    /** GZip header magic number. */
    public static final int GZIP_MAGIC = 0x8b1f;

    /** Friendly name for the deflate compression method. */
    public static final String CM_STRING_DEFLATE = "deflate";

    /** Deflate compression method. */
    public static final int CM_DEFLATE = 8;

    public static final int DEFLATE_XFL_MAXIMUM_COMPRESSION = 2;
    public static final int DEFLATE_XFL_FASTEST_COMPRESSION = 4;

    /** File header flags. */
    public static final int FLG_FTEXT = 1;          // Extra text
    public static final int FLG_FHCRC = 2;          // Header CRC
    public static final int FLG_FEXTRA = 4;         // Extra field
    public static final int FLG_FNAME = 8;          // File name
    public static final int FLG_FCOMMENT = 16;      // File comment
    public static final int FLG_FRESERVED = 224;    // Reserved flags

    public static final String OS_STRING_FAT_FS = "FAT filesystem (MS-DOS, OS/2, NT/Win32)";
    public static final String OS_STRING_AMIGA = "Amiga";
    public static final String OS_STRING_VMS = "VMS (or OpenVMS)";
    public static final String OS_STRING_UNIX = "Unix";
    public static final String OS_STRING_VM = "VM/CMS";
    public static final String OS_STRING_ATARI = "Atari TOS";
    public static final String OS_STRING_HPFS_FS = "HPFS filesystem (OS/2, NT)";
    public static final String OS_STRING_MACINTOSH = "Macintosh";
    public static final String OS_STRING_Z_SYSTEM = "Z-System";
    public static final String OS_STRING_CPM = "CP/M";
    public static final String OS_STRING_TOPS_20 = "TOPS-20";
    public static final String OS_STRING_NTFS_FS = "NTFS filesystem (NT)";
    public static final String OS_STRING_QDOS = "QDOS";
    public static final String OS_STRING_ACORN = "Acorn RISCOS";
    public static final String OS_STRING_UNKNOWN = "unknown";

    public static final int OS_FAT_FS = 0;
    public static final int OS_AMIGA = 1;
    public static final int OS_VMS = 2;
    public static final int OS_UNIX = 3;
    public static final int OS_VM = 4;
    public static final int OS_ATARI = 5;
    public static final int OS_HPFS_FS = 6;
    public static final int OS_MACINTOSH = 7;
    public static final int OS_Z_SYSTEM = 8;
    public static final int OS_CPM = 9;
    public static final int OS_TOPS_20 = 10;
    public static final int OS_NTFS_FS = 11;
    public static final int OS_QDOS = 12;
    public static final int OS_ACORN = 13;
    public static final int OS_UNKNOWN = 255;

    /** Operating Systems lookup map used to identify OS values. */
    public static final Map<Integer, String> osIdxStr = new HashMap<Integer, String>();

    /**
     * Populate the lookup map with known Operating Systems.
     */
    static {
        osIdxStr.put(OS_FAT_FS, OS_STRING_FAT_FS);
        osIdxStr.put(OS_AMIGA, OS_STRING_AMIGA);
        osIdxStr.put(OS_VMS, OS_STRING_VMS);
        osIdxStr.put(OS_UNIX, OS_STRING_UNIX);
        osIdxStr.put(OS_VM, OS_STRING_VM);
        osIdxStr.put(OS_ATARI, OS_STRING_ATARI);
        osIdxStr.put(OS_HPFS_FS, OS_STRING_HPFS_FS);
        osIdxStr.put(OS_MACINTOSH, OS_STRING_MACINTOSH);
        osIdxStr.put(OS_Z_SYSTEM, OS_STRING_Z_SYSTEM);
        osIdxStr.put(OS_CPM, OS_STRING_CPM);
        osIdxStr.put(OS_TOPS_20, OS_STRING_TOPS_20);
        osIdxStr.put(OS_NTFS_FS, OS_STRING_NTFS_FS);
        osIdxStr.put(OS_QDOS, OS_STRING_QDOS);
        osIdxStr.put(OS_ACORN, OS_STRING_ACORN);
        osIdxStr.put(OS_UNKNOWN, OS_STRING_UNKNOWN);
    }

    /**
     * This utility class does not require instantiation.
     */
    private GzipConstants() {
    }

}
