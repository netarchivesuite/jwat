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

import org.jwat.common.RandomAccessFileOutputStream;

/**
 * Simple ARC file writer wrapping some of the trivial code related to writing records.
 * Handles automatic max file size closing and renaming of old file and opening of new file.
 * The preferred workflow is to class nextWriter() and close(). Using open() does not expose a clean internal state.
 *
 * @author nicl
 */
public class ArcFileWriter {

    /** Suffix used for open files. */
    protected static final String ACTIVE_SUFFIX = ".open";

    /** Overall ARC file writer configuration. */
    protected ArcFileWriterConfig arcFileConfig;

    /** ARC file naming Configuration. */
    protected ArcFileNaming arcFileNaming;

    /** Current sequence number. */
    protected int sequenceNr = -1;

    /** Current ARC file. */
    protected File writerFile;

    /** Current random access file. */
    protected RandomAccessFile writer_raf;

    /** Current random access output stream. */
    protected RandomAccessFileOutputStream writer_rafout;

    /** Current ARC writer. */
    public ArcWriter writer;

    /**
     * Constructor for internal and unit test use.
     */
    protected ArcFileWriter() {
    }

    /**
     * Returns a configured ARC file writer.
     * @param arcFileNaming ARC file naming configuration
     * @param arcFileConfig overall ARC writer configuration
     * @return ARC file writer instance using the supplied configuration
     */
    public static ArcFileWriter getArcWriterInstance(ArcFileNaming arcFileNaming, ArcFileWriterConfig arcFileConfig) {
        ArcFileWriter wfw = new ArcFileWriter();
        wfw.arcFileNaming = arcFileNaming;
        wfw.arcFileConfig = arcFileConfig;
        return wfw;
    }

    /**
     * Returns the current sequence number.
     * @return the current sequence number
     */
    public int getSequenceNr() {
        return sequenceNr;
    }

    /**
     * Returns the current ARC file object.
     * @return current ARC file object
     */
    public File getFile() {
        return writerFile;
    }

    /**
     * Returns the current ARC writer object.
     * @return current ARC writer object
     */
    public ArcWriter getWriter() {
        return writer;
    }

    /**
     * Open new file with active prefix and prepare for writing.
     * @throws IOException if an I/O exception occurs while opening file
     */
    public void open() throws IOException {
        if (writer == null) {
            ++sequenceNr;
            String finishedFilename = arcFileNaming.getFilename(sequenceNr, arcFileConfig.bCompression);
            String activeFilename = finishedFilename + ACTIVE_SUFFIX;
            File finishedFile = new File(arcFileConfig.targetDir, finishedFilename);
            writerFile = new File(arcFileConfig.targetDir, activeFilename);
            if (writerFile.exists()) {
                if (arcFileConfig.bOverwrite) {
                    writerFile.delete();
                } else {
                    throw new IOException("'" + writerFile + "' already exists, will not overwrite");
                }
            }
            if (finishedFile.exists()) {
                if (arcFileConfig.bOverwrite) {
                    finishedFile.delete();
                } else {
                    throw new IOException("'" + finishedFile + "' already exists, will not overwrite");
                }
            }
            writer_raf = new RandomAccessFile(writerFile, "rw");
            writer_raf.seek(0L);
            writer_raf.setLength(0L);
            writer_rafout = new RandomAccessFileOutputStream(writer_raf);
            writer = ArcWriterFactory.getWriter(writer_rafout, 8192, arcFileConfig.bCompression);
        }
    }

    /**
     * Checks to see whether a new file needs to be created. Depending on the configuration this also checks if the max file size has been reached and closes/renames the old file and opens a new one.
     * @return boolean indicating whether new writer/file was created
     * @throws Exception if an exception occurs
     */
    public boolean nextWriter() throws Exception {
        boolean bNewWriter = false;
        if (writer_raf == null) {
            bNewWriter = true;
        } else if (arcFileNaming.supportMultipleFiles() && writer_raf.length() > arcFileConfig.maxFileSize) {
            close();
            bNewWriter = true;
        }
        if (bNewWriter) {
            open();
        }
        return bNewWriter;
    }

    /**
     * Close writer and release all resources.
     * @throws IOException in an I/O exception occurs while closing resources
     */
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
                throw new IOException("unable to rename '" + writerFile + "' to '" + finishedFile + "' - destination file already exists");
            }
            boolean success = writerFile.renameTo(finishedFile);
            if (!success) {
                throw new IOException("unable to rename '" + writerFile + "' to '" + finishedFile + "' - unknown problem");
            }
        }
        writerFile = null;
    }

}
