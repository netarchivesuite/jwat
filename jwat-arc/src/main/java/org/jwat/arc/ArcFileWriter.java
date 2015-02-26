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
package org.jwat.arc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jwat.arc.ArcWriter;
import org.jwat.arc.ArcWriterFactory;
import org.jwat.common.RandomAccessFileOutputStream;

public class ArcFileWriter {

    protected static final String ACTIVE_SUFFIX = ".open";

    /*
     * Configuration.
     */

    protected ArcFileWriterConfig arcFileConfig;

    /*
     * Filename.
     */

    protected ArcFileNaming arcFileNaming;

    protected int sequenceNr = 0;

    /*
     * File.
     */

    protected File writerFile;

    protected RandomAccessFile writer_raf;

    protected RandomAccessFileOutputStream writer_rafout;

    public ArcWriter writer;

    /*
     * Metadata.
     */

    protected ArcFileWriter() {
    }

    public static ArcFileWriter getArcWriterInstance(ArcFileNaming arcFileNaming, ArcFileWriterConfig arcFileConfig) {
		ArcFileWriter wfw = new ArcFileWriter();
		wfw.arcFileNaming = arcFileNaming;
		wfw.arcFileConfig = arcFileConfig;
		return wfw;
	}

    public File getFile() {
    	return writerFile;
    }

    public void open() throws IOException {
		String finishedFilename = arcFileNaming.getFilename(sequenceNr++, arcFileConfig.bCompression);
		String activeFilename = finishedFilename + ACTIVE_SUFFIX;
        File finishedFile = new File(arcFileConfig.targetDir, finishedFilename);
        writerFile = new File(arcFileConfig.targetDir, activeFilename);
        if (writerFile.exists()) {
            throw new IOException(writerFile + " already exists, will not overwrite");
        }
        if (finishedFile.exists()) {
        	if (arcFileConfig.bOverwrite) {
        		finishedFile.delete();
        	} else {
                throw new IOException(finishedFile + " already exists, will not overwrite");
        	}
        }
        writer_raf = new RandomAccessFile(writerFile, "rw");
        writer_raf.seek(0L);
        writer_raf.setLength(0L);
        writer_rafout = new RandomAccessFileOutputStream(writer_raf);
        writer = ArcWriterFactory.getWriter(writer_rafout, 8192, arcFileConfig.bCompression);
    }

    public void nextWriter() throws Exception {
    	boolean bNewWriter = false;
    	if (writer_raf == null) {
    		bNewWriter = true;
    	} else if (writer_raf.length() > arcFileConfig.maxFileSize) {
        	close();
    		bNewWriter = true;
    	}
    	if (bNewWriter) {
    		open();
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
