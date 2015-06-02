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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * General configuration of <code>WarcFileWriter</code>.
 *
 * @author nicl
 */
public class WarcFileWriterConfig {

    /** Standard/default max file size. */
    public static final long DEFAULT_MAX_FILE_SIZE = 1073741824L;

    /** Target directory in which to write ARC file(s). */
    protected File targetDir;

    /** Compress archive(s). */
    protected boolean bCompression;

    /** Max file size used to determine when to close the current ARC file and start writing to the next one. */
    protected Long maxFileSize = DEFAULT_MAX_FILE_SIZE;

    /** Overwrite existing file(s). */
    public boolean bOverwrite;

    /** Array of metadata. */
    protected LinkedHashMap<String, Map.Entry<String, String>> metadata = new LinkedHashMap<String, Map.Entry<String, String>>();

    /**
     * Construct instance with largely default values, except the targetDir which is null.
     */
    public WarcFileWriterConfig() {
    }

    /**
     * Construct an instance with custom values.
     * @param targetDir target directory in which to write WARC file(s)
     * @param bCompression compress archive(s)
     * @param maxFileSize max file size to determine when to move on to a fresh WARC file
     * @param bOverwrite overwrite existing file(s)
     */
    public WarcFileWriterConfig(File targetDir, boolean bCompression, long maxFileSize, boolean bOverwrite) {
        this.targetDir = targetDir;
        this.bCompression = bCompression;
        this.maxFileSize = maxFileSize;
        this.bOverwrite = bOverwrite;
    }

    /*
    public void addMetadata(String key, String value) {
        metadata.put( key, new SimpleEntry<String, String>(key, value) );
    }
    */

}
