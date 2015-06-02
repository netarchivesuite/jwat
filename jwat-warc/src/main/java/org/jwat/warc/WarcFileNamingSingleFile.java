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

/**
 * Simple WARC file naming implementation used for writing to a single file only.
 *
 * @author nicl
 */
public class WarcFileNamingSingleFile implements WarcFileNaming {

    /** File name to use. */
    protected String filename;

    /**
     * Construct a new instance with the filename to return.
     * @param filename filename to return
     */
    public WarcFileNamingSingleFile(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("filename argument null");
        }
        this.filename = filename;
    }

    /**
     * Construct a new instance with the file whose filename to return.
     * @param file file whose filename to return
     */
    public WarcFileNamingSingleFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file argument null");
        }
        filename = file.getName();
    }

    @Override
    public boolean supportMultipleFiles() {
        return false;
    }

    @Override
    public String getFilename(int sequenceNr, boolean bCompressed) {
        return filename;
    }

}
