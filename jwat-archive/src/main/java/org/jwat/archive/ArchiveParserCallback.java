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

import java.io.File;
import java.io.IOException;

import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcRecordBase;
import org.jwat.gzip.GzipEntry;
import org.jwat.gzip.GzipReader;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcRecord;

/**
 * Callback handler invoked by the parse method of <code>ArchiveParser</code>. ({@link ArchiveParser})
 * @author nicl
 */
public interface ArchiveParserCallback {

    /** Report the file and what it has been identified as. */
    public void apcFileId(File file, int fileId);

    /** Notity that a GZIP entry was encountered. */
    public void apcGzipEntryStart(GzipEntry gzipEntry, long startOffset);

    /** Notity that an ARC record was encountered. */
    public void apcArcRecordStart(ArcRecordBase arcRecord, long startOffset, boolean compressed) throws IOException;

    /** Notity that a WARC record was encountered. */
    public void apcWarcRecordStart(WarcRecord warcRecord, long startOffset, boolean compressed) throws IOException;

    /** Report updated consumed bytes number. */
    public void apcUpdateConsumed(long consumed);

    /** Report a runtime exception was encountered during the parsing of a file. (Should hopefully not happen!) */
    public void apcRuntimeError(Throwable t, long offset, long consumed);

    /** Parser done, no more records/entries. */
    public void apcDone(GzipReader gzipReader, ArcReader arcReader, WarcReader warcReader);

}
