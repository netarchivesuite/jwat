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

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.jwat.arc.ArcRecordBase;
import org.jwat.arc.ArcWriter;
import org.jwat.arc.ArcWriterFactory;
import org.jwat.common.RandomAccessFileOutputStream;
import org.jwat.warc.WarcRecord;
import org.jwat.warc.WarcWriter;
import org.jwat.warc.WarcWriterFactory;

/**
 * Simple class used to clone every record read from a (W)ARC file.
 * Used for debugging purposes.
 *
 * @author nicl
 */
public class Cloner {

	/** Buffer size used when writing (W)ARC data. */
	public static final int DEFAULT_WRITER_BUFFER_SIZE = 8192;

	private static Cloner cloner;

    public static synchronized Cloner getCloner() {
        if (cloner == null) {
            cloner = new Cloner();
        }
        return cloner;
    }

    private Cloner() {
    }

    private RandomAccessFile arcRaf;
    private RandomAccessFileOutputStream arcRafOut;
    private ArcWriter arcWriter;

    private RandomAccessFile warcRaf;
    private RandomAccessFileOutputStream warcRafOut;
    private WarcWriter warcWriter;

    public synchronized void cloneArcRecord(ArcRecordBase record, ManagedPayload managedPayload) throws IOException {
        if (arcWriter == null) {
            arcRaf = new RandomAccessFile("erroneous.arc", "rw");
            arcRaf.seek(0);
            arcRaf.setLength(0);
            arcRafOut = new RandomAccessFileOutputStream(arcRaf);
            arcWriter = ArcWriterFactory.getWriter(arcRafOut, DEFAULT_WRITER_BUFFER_SIZE, false);
        }
        arcWriter.writeHeader(record);
        InputStream httpHeaderStream = managedPayload.getHttpHeaderStream();
        if (httpHeaderStream != null) {
            arcWriter.streamPayload(httpHeaderStream);
            httpHeaderStream.close();
            httpHeaderStream = null;
        }
        InputStream payloadStream = managedPayload.getPayloadStream();
        if (payloadStream != null) {
            arcWriter.streamPayload(payloadStream);
            payloadStream.close();
            payloadStream = null;
        }
        arcWriter.closeRecord();
    }

    public synchronized void cloneWarcRecord(WarcRecord record, ManagedPayload managedPayload) throws IOException {
        if (warcWriter == null) {
            warcRaf = new RandomAccessFile("erroneous.warc", "rw");
            warcRaf.seek(0);
            warcRaf.setLength(0);
            warcRafOut = new RandomAccessFileOutputStream(warcRaf);
            warcWriter = WarcWriterFactory.getWriter(warcRafOut, DEFAULT_WRITER_BUFFER_SIZE, false);
        }
        warcWriter.writeHeader(record);
        InputStream httpHeaderStream = managedPayload.getHttpHeaderStream();
        if (httpHeaderStream != null) {
            warcWriter.streamPayload(httpHeaderStream);
            httpHeaderStream.close();
            httpHeaderStream = null;
        }
        InputStream payloadStream = managedPayload.getPayloadStream();
        if (payloadStream != null) {
            warcWriter.streamPayload(payloadStream);
            payloadStream.close();
            payloadStream = null;
        }
        warcWriter.closeRecord();
    }

    public void close() throws IOException {
        if (arcWriter != null) {
            arcWriter.close();
            arcWriter = null;
        }
        if (warcWriter != null) {
            warcWriter.close();
            warcWriter = null;
        }
        if (arcRafOut != null) {
            arcRafOut.close();
            arcRafOut = null;
        }
        if (warcRafOut != null) {
            warcRafOut.close();
            warcRafOut = null;
        }
        if (arcRaf != null) {
            arcRaf.close();
            arcRaf = null;
        }
        if (warcRaf != null) {
            warcRaf.close();
            warcRaf = null;
        }
    }

}
