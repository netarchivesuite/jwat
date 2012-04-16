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

import java.util.Map;
import java.util.TreeMap;

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

    /*
     * Compression.
     */

    /** Friendly name for the deflate compression method. */
    public static final String CM_STRING_DEFLATE = "deflate";

    /** Deflate compression method. */
    public static final int CM_DEFLATE = 8;

    /** Maximum compression flag bit. */
    public static final int DEFLATE_XFL_MAXIMUM_COMPRESSION = 2;
    /** Fastest compression flag bit. */
    public static final int DEFLATE_XFL_FASTEST_COMPRESSION = 4;
    /** Compression flag bit mask. */
    public static final int DEFLATE_XFL_COMPRESSION_MASK =
            DEFLATE_XFL_MAXIMUM_COMPRESSION + DEFLATE_XFL_FASTEST_COMPRESSION;
    /** Reserved compression bits mask. */
    public static final int DEFLATE_XLF_RESERVED = 1 + 8 + 16 + 32 + 64 + 128;

    /*
     * File header flags.
     */

    /** Extra text flag. */
    public static final int FLG_FTEXT = 1;
    /** Header CRC flag. */
    public static final int FLG_FHCRC = 2;
    /** Extra field flag. */
    public static final int FLG_FEXTRA = 4;
    /** File name flag. */
    public static final int FLG_FNAME = 8;
    /** File comment flag. */
    public static final int FLG_FCOMMENT = 16;
    /** Reserved flag bits mask. */
    public static final int FLG_FRESERVED = 224;

    /** FAT filesystem OS name. */
    public static final String OS_STRING_FAT_FS = "FAT filesystem (MS-DOS, OS/2, NT/Win32)";
    /** Amiga OS name. */
    public static final String OS_STRING_AMIGA = "Amiga";
    /** VMS OS name. */
    public static final String OS_STRING_VMS = "VMS (or OpenVMS)";
    /** Unix OS name. */
    public static final String OS_STRING_UNIX = "Unix";
    /** VM/CMS OS name. */
    public static final String OS_STRING_VM = "VM/CMS";
    /** Atari TOS OS name. */
    public static final String OS_STRING_ATARI = "Atari TOS";
    /** HPFS filesystem OS name. */
    public static final String OS_STRING_HPFS_FS = "HPFS filesystem (OS/2, NT)";
    /** Macintosh OS name. */
    public static final String OS_STRING_MACINTOSH = "Macintosh";
    /** Z-System OS name. */
    public static final String OS_STRING_Z_SYSTEM = "Z-System";
    /** CP/M OS name. */
    public static final String OS_STRING_CPM = "CP/M";
    /** TOPS-20 OS name. */
    public static final String OS_STRING_TOPS_20 = "TOPS-20";
    /** NTFS filesystem OS name. */
    public static final String OS_STRING_NTFS_FS = "NTFS filesystem (NT)";
    /** QDOS OS name. */
    public static final String OS_STRING_QDOS = "QDOS";
    /** Acorn OS name. */
    public static final String OS_STRING_ACORN = "Acorn RISCOS";
    /** Unknown OS name. */
    public static final String OS_STRING_UNKNOWN = "unknown";

    /** FAT filesystem OS value. */
    public static final int OS_FAT_FS = 0;
    /** Amiga OS value. */
    public static final int OS_AMIGA = 1;
    /** VMS OS value. */
    public static final int OS_VMS = 2;
    /** Unix OS value. */
    public static final int OS_UNIX = 3;
    /** VM/CMS OS value. */
    public static final int OS_VM = 4;
    /** Atari TOS OS value. */
    public static final int OS_ATARI = 5;
    /** HPFS filesystem OS value. */
    public static final int OS_HPFS_FS = 6;
    /** Macintosh OS value. */
    public static final int OS_MACINTOSH = 7;
    /** Z-System OS value. */
    public static final int OS_Z_SYSTEM = 8;
    /** CP/M OS value. */
    public static final int OS_CPM = 9;
    /** TOPS-20 OS value. */
    public static final int OS_TOPS_20 = 10;
    /** NTFS filesystem OS value. */
    public static final int OS_NTFS_FS = 11;
    /** QDOS OS value. */
    public static final int OS_QDOS = 12;
    /** Acorn OS value. */
    public static final int OS_ACORN = 13;
    /** Unknown OS value. */
    public static final int OS_UNKNOWN = 255;

    /** Operating Systems lookup map used to identify OS values. */
    public static final Map<Integer, String> osIdxStr = new TreeMap<Integer, String>();

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
