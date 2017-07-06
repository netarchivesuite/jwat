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
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecordBase;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.RandomAccessFileInputStream;
import org.jwat.common.UriProfile;
import org.jwat.gzip.GzipEntry;
import org.jwat.gzip.GzipReader;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

/**
 * Provide event based reading of GZIP/ARC/WARC records.
 * Callback methods are called for specific events. (See {@link ArchiveParserCallback})
 * @author nicl
 */
public class ArchiveParser {

	/** Buffer size used to read the input stream. */
    public static final int DEFAULT_READER_BUFFER_SIZE = 8192;

    /** Buffer size used to peek into the input stream. */
    public static final int DEFAULT_PUSHBASH_BUFFER_SIZE = 32;

    /*
     * Settings.
     */

    /** URI profile to use. */
    public UriProfile uriProfile = UriProfile.RFC3986;

    /** Enable block digest calculation/validation. */
    public boolean bBlockDigestEnabled = true;

    /** Enable payload digest calculation/validation. */
    public boolean bPayloadDigestEnabled = true;

    /** Max record header size. */
    public int recordHeaderMaxSize = 8192;

    /** Max payload header size (http header etc.). */
    public int payloadHeaderMaxSize = 32768;

    /*
     * State.
     */

    /** Access to used GZIP reader to read state. */
    public GzipReader gzipReader = null;
    /** Access to used ARC reader to read state. */
    public ArcReader arcReader = null;
    /** Access to used WARC reader to read state. */
    public WarcReader warcReader = null;

    /** Temporary buffer. */
    protected byte[] buffer = new byte[ 8192 ];

    /**
     * Construct instance. Modify the fields directly to customise.
     */
    public ArchiveParser() {
    }

    /**
     * Parse a file invoking the appropriate callback methods according to the input file.
     * @param file input file
     * @param callbacks callback handler
     * @return number of bytes consumed by the parsing, should correspond to the size of the input file
     */
    public long parse(File file, ArchiveParserCallback callbacks) {
        RandomAccessFile raf = null;
        RandomAccessFileInputStream rafin;
        ByteCountingPushBackInputStream pbin = null;
        GzipEntry gzipEntry = null;
        ArcRecordBase arcRecord = null;
        WarcRecord warcRecord = null;
        try {
            raf = new RandomAccessFile( file, "r" );
            rafin = new RandomAccessFileInputStream( raf );
            pbin = new ByteCountingPushBackInputStream( new BufferedInputStream( rafin, DEFAULT_READER_BUFFER_SIZE ), DEFAULT_PUSHBASH_BUFFER_SIZE );
            if ( GzipReader.isGzipped( pbin ) ) {
                gzipReader = new GzipReader( pbin );
                ByteCountingPushBackInputStream in;
                int gzipEntries = 0;
                while ( (gzipEntry = gzipReader.getNextEntry()) != null ) {
                    in = new ByteCountingPushBackInputStream( new BufferedInputStream( gzipEntry.getInputStream(), DEFAULT_READER_BUFFER_SIZE ), DEFAULT_PUSHBASH_BUFFER_SIZE );
                    ++gzipEntries;
                    //System.out.println(gzipEntries + " - " + gzipEntry.getStartOffset() + " (0x" + (Long.toHexString(gzipEntry.getStartOffset())) + ")");
                    if ( gzipEntries == 1 ) {
                        if ( ArcReaderFactory.isArcFile( in ) ) {
                            arcReader = ArcReaderFactory.getReaderUncompressed();
                            arcReader.setUriProfile(uriProfile);
                            arcReader.setBlockDigestEnabled( bBlockDigestEnabled );
                            arcReader.setPayloadDigestEnabled( bPayloadDigestEnabled );
                            arcReader.setRecordHeaderMaxSize( recordHeaderMaxSize );
                            arcReader.setPayloadHeaderMaxSize( payloadHeaderMaxSize );
                            callbacks.apcFileId(file, FileIdent.FILEID_ARC_GZ);
                        }
                        else if ( WarcReaderFactory.isWarcFile( in ) ) {
                            warcReader = WarcReaderFactory.getReaderUncompressed();
                            warcReader.setWarcTargetUriProfile(uriProfile);
                            warcReader.setBlockDigestEnabled( bBlockDigestEnabled );
                            warcReader.setPayloadDigestEnabled( bPayloadDigestEnabled );
                            warcReader.setRecordHeaderMaxSize( recordHeaderMaxSize );
                            warcReader.setPayloadHeaderMaxSize( payloadHeaderMaxSize );
                            callbacks.apcFileId(file, FileIdent.FILEID_WARC_GZ);
                        }
                        else {
                            callbacks.apcFileId(file, FileIdent.FILEID_GZIP);
                        }
                    }
                    if ( arcReader != null ) {
                        while ( (arcRecord = arcReader.getNextRecordFrom( in, gzipEntry.getStartOffset() )) != null ) {
                            callbacks.apcArcRecordStart(arcRecord, gzipReader.getStartOffset(), true);
                        }
                    }
                    else if ( warcReader != null ) {
                        while ( (warcRecord = warcReader.getNextRecordFrom( in, gzipEntry.getStartOffset() ) ) != null ) {
                            callbacks.apcWarcRecordStart(warcRecord, gzipReader.getStartOffset(), true);
                        }
                    }
                    else {
                        while ( in.read(buffer) != -1 ) {
                        }
                    }
                    in.close();
                    gzipEntry.close();
                    callbacks.apcGzipEntryStart(gzipEntry, gzipReader.getStartOffset());
                    callbacks.apcUpdateConsumed(pbin.getConsumed());
                }
            }
            else if ( ArcReaderFactory.isArcFile( pbin ) ) {
                arcReader = ArcReaderFactory.getReaderUncompressed( pbin );
                arcReader.setUriProfile(uriProfile);
                arcReader.setBlockDigestEnabled( bBlockDigestEnabled );
                arcReader.setPayloadDigestEnabled( bPayloadDigestEnabled );
                arcReader.setRecordHeaderMaxSize( recordHeaderMaxSize );
                arcReader.setPayloadHeaderMaxSize( payloadHeaderMaxSize );
                callbacks.apcFileId(file, FileIdent.FILEID_ARC);
                while ( (arcRecord = arcReader.getNextRecord()) != null ) {
                    callbacks.apcArcRecordStart(arcRecord, arcReader.getStartOffset(), false);
                    callbacks.apcUpdateConsumed(pbin.getConsumed());
                }
                arcReader.close();
            }
            else if ( WarcReaderFactory.isWarcFile( pbin ) ) {
                warcReader = WarcReaderFactory.getReaderUncompressed( pbin );
                warcReader.setWarcTargetUriProfile(uriProfile);
                warcReader.setBlockDigestEnabled( bBlockDigestEnabled );
                warcReader.setPayloadDigestEnabled( bPayloadDigestEnabled );
                warcReader.setRecordHeaderMaxSize( recordHeaderMaxSize );
                warcReader.setPayloadHeaderMaxSize( payloadHeaderMaxSize );
                callbacks.apcFileId(file, FileIdent.FILEID_WARC);
                while ( (warcRecord = warcReader.getNextRecord()) != null ) {
                    callbacks.apcWarcRecordStart(warcRecord, warcReader.getStartOffset(), false);
                    callbacks.apcUpdateConsumed(pbin.getConsumed());
                }
                warcReader.close();
            }
            else {
                callbacks.apcFileId(file, FileIdent.identFileName(file));
            }
            callbacks.apcDone(gzipReader, arcReader, warcReader);
        }
        catch (Throwable t) {
            // TODO just use reader.getStartOffset?
            long startOffset = -1;
            Long length = null;
            if (arcRecord != null) {
                startOffset = arcRecord.getStartOffset();
                length = arcRecord.header.archiveLength;
            }
            if (warcRecord != null) {
                startOffset = warcRecord.getStartOffset();
                length = warcRecord.header.contentLength;
            }
            if (gzipEntry != null) {
                startOffset = gzipEntry.getStartOffset();
                // TODO correct entry size including header+trailer.
                length = gzipEntry.compressed_size;
            }
            if (length != null) {
                startOffset += length;
            }
            callbacks.apcRuntimeError(t, startOffset, pbin.getConsumed());
        }
        finally {
            if ( arcReader != null ) {
                arcReader.close();
            }
            if ( warcReader != null ) {
                warcReader.close();
            }
            if (gzipReader != null) {
                try {
                    gzipReader.close();
                }
                catch (IOException e) {
                }
            }
            if (pbin != null) {
                try {
                    pbin.close();
                }
                catch (IOException e) {
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                }
                catch (IOException e) {
                }
            }
        }
        return pbin.getConsumed();
    }

}
