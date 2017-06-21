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
package org.jwat.archive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jwat.arc.ArcReaderFactory;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.RandomAccessFileInputStream;
import org.jwat.gzip.GzipEntry;
import org.jwat.gzip.GzipReader;
import org.jwat.warc.WarcReaderFactory;

public final class FileIdent {

	/** Buffer size used to read the input stream. */
    public static final int DEFAULT_READER_BUFFER_SIZE = 8192;

    /** Buffer size used to peek into the input stream. */
    public static final int DEFAULT_PUSHBASH_BUFFER_SIZE = 32;

    public static final int FILEID_ERROR = -1;
    public static final int FILEID_UNKNOWN = 0;
    public static final int FILEID_GZIP = 1;
    public static final int FILEID_ARC = 2;
    public static final int FILEID_WARC = 3;
    public static final int FILEID_ARC_GZ = 4;
    public static final int FILEID_WARC_GZ = 5;

    public int filenameId;

    public int streamId;

    public static FileIdent ident(File file) {
        FileIdent fId = new FileIdent();
        fId.filenameId = identFileName(file);
        fId.streamId = identFileStream(file);
        return fId;
    }

    public static int identFileName(File file) {
        int fileId = FILEID_UNKNOWN;
        String fname = file.getName().toLowerCase();
        if (fname.endsWith(".arc.gz")) {
            fileId = FILEID_ARC_GZ;
        } else if (fname.endsWith(".warc.gz")) {
            fileId = FILEID_WARC_GZ;
        } else if (fname.endsWith(".arc")) {
            fileId = FILEID_ARC;
        } else if (fname.endsWith(".warc")) {
            fileId = FILEID_WARC;
        } else if (fname.endsWith(".gz")) {
            fileId = FILEID_GZIP;
        }
        return fileId;
    }

    public static int identFileStream(File file) {
        int fileId = FILEID_UNKNOWN;
        byte[] magicBytes = new byte[32];
        int read;
        RandomAccessFile raf = null;
        RandomAccessFileInputStream rafin;
        ByteCountingPushBackInputStream pbin = null;
        GzipReader gzipReader = null;
        GzipEntry gzipEntry = null;
        try {
            raf = new RandomAccessFile( file, "r" );
            rafin = new RandomAccessFileInputStream( raf );
            pbin = new ByteCountingPushBackInputStream(rafin, DEFAULT_PUSHBASH_BUFFER_SIZE);
            read = pbin.peek(magicBytes);
            if (read == 32) {
                if (GzipReader.isGzipped(pbin)) {
                    gzipReader = new GzipReader( pbin );
                    ByteCountingPushBackInputStream in;
                    if ( (gzipEntry = gzipReader.getNextEntry()) != null ) {
                        in = new ByteCountingPushBackInputStream( new BufferedInputStream( gzipEntry.getInputStream(), DEFAULT_READER_BUFFER_SIZE ), DEFAULT_PUSHBASH_BUFFER_SIZE );
                        if (ArcReaderFactory.isArcRecord(in)) {
                            fileId = FILEID_ARC_GZ;
                        } else if (WarcReaderFactory.isWarcRecord(in)) {
                            fileId = FILEID_WARC_GZ;
                        } else {
                            fileId = FILEID_GZIP;
                        }
                    }
                    gzipReader.close();
                } else if (ArcReaderFactory.isArcRecord(pbin)) {
                    fileId = FILEID_ARC;
                } else if (WarcReaderFactory.isWarcRecord(pbin)) {
                    fileId = FILEID_WARC;
                }
            }
        } catch (FileNotFoundException e) {
            fileId = FILEID_ERROR;
            System.out.println("Error reading: " + file.getPath());
        } catch (IOException e) {
            fileId = FILEID_ERROR;
            e.printStackTrace();
        }
        finally {
            if (pbin != null) {
                try {
                    pbin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileId;
    }

}
