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
import java.util.UUID;

import org.jwat.common.RandomAccessFileOutputStream;
import org.jwat.common.Uri;
import org.jwat.warc.WarcWriter;
import org.jwat.warc.WarcWriterFactory;

public class WarcFileWriter {

    public static final String ACTIVE_SUFFIX = ".open";

    /*
     * Configuration.
     */

    protected WarcFileWriterConfig warcFileConfig;

    /*
     * Filename.
     */

    protected WarcFileNaming warcFileNaming;

    protected int sequenceNr = 0;

    /*
     * File.
     */

    protected File writerFile;

    protected RandomAccessFile writer_raf;

    protected RandomAccessFileOutputStream writer_rafout;

    public WarcWriter writer;

    /*
     * Metadata.
     */

    //protected String warcFields;

    public Uri warcinfoRecordId;

    protected WarcFileWriter() {
    }

    public static WarcFileWriter getWarcWriterInstance(WarcFileNaming warcFileNaming, WarcFileWriterConfig warcFileConfig) {
		WarcFileWriter wfw = new WarcFileWriter();
		wfw.warcFileNaming = warcFileNaming;
		wfw.warcFileConfig = warcFileConfig;
        /*
        StringBuilder sb = new StringBuilder();
        sb.append("software");
        sb.append(": ");
        sb.append("Netarchivesuite");
        sb.append("\r\n");
        sb.append("format");
        sb.append(": ");
        sb.append("WARC file version 1.0");
        sb.append("\r\n");
        sb.append("conformsTo");
        sb.append(": ");
        sb.append("http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf");
        sb.append("\r\n");
        wfw.warcFields = sb.toString();
        */
		return wfw;
	}

    public File getFile() {
    	return writerFile;
    }

    public void open() throws IOException {
		String finishedFilename = warcFileNaming.getFilename(sequenceNr++, warcFileConfig.bCompression);
		String activeFilename = finishedFilename + ACTIVE_SUFFIX;
        File finishedFile = new File(warcFileConfig.targetDir, finishedFilename);
        writerFile = new File(warcFileConfig.targetDir, activeFilename);
        if (writerFile.exists()) {
            throw new IOException(writerFile + " already exists, will not overwrite");
        }
        if (finishedFile.exists()) {
        	if (warcFileConfig.bOverwrite) {
        		finishedFile.delete();
        	} else {
                throw new IOException(finishedFile + " already exists, will not overwrite");
        	}
        }
        writer_raf = new RandomAccessFile(writerFile, "rw");
        writer_raf.seek(0L);
        writer_raf.setLength(0L);
        writer_rafout = new RandomAccessFileOutputStream(writer_raf);
        writer = WarcWriterFactory.getWriter(writer_rafout, 8192, warcFileConfig.bCompression);
    }

    public void nextWriter() throws Exception {
    	boolean bNewWriter = false;
    	if (writer_raf == null) {
    		bNewWriter = true;
    	} else if (writer_raf.length() > warcFileConfig.maxFileSize) {
        	close();
    		bNewWriter = true;
    	}
    	if (bNewWriter) {
    		open();
            //byte[] warcFieldsBytes = warcFields.getBytes("ISO-8859-1");
            //ByteArrayInputStream bin = new ByteArrayInputStream(warcFieldsBytes);
            warcinfoRecordId = new Uri("urn:uuid:" + UUID.randomUUID());
            /*
            WarcRecord record = WarcRecord.createRecord(writer);
            WarcHeader header = record.header;
            header.warcTypeIdx = WarcConstants.RT_IDX_WARCINFO;
            header.warcDate = new Date();
            header.warcFilename = finishedFilename;
            header.warcRecordIdUri = warcinfoRecordId;
            header.contentTypeStr = WarcConstants.CT_APP_WARC_FIELDS;
            header.contentLength = new Long(warcFieldsBytes.length);
            writer.writeHeader(record);
            writer.streamPayload(bin);
            writer.closeRecord();
            */
    	}
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (writer_rafout != null) {
            writer_rafout.close();
            writer_rafout = null;
        }
        if (writer_raf != null) {
            writer_raf.close();
            writer_raf = null;
        }
        warcinfoRecordId = null;
        if (writerFile != null && writerFile.getName().endsWith(ACTIVE_SUFFIX)) {
            String finishedName = writerFile.getName().substring(0, writerFile.getName().length() - ACTIVE_SUFFIX.length());
            File finishedFile = new File(writerFile.getParent(), finishedName);
            if (finishedFile.exists()) {
                throw new IOException("unable to rename " + writerFile + " to " + finishedFile + " - destination file already exists");
            }
            boolean success = writerFile.renameTo(finishedFile);
            if (!success) {
                throw new IOException("unable to rename " + writerFile + " to " + finishedFile + " - unknown problem");
            }
        }
        writerFile = null;
    }

}
