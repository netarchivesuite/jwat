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

public class WarcFileWriterConfig {

	protected File targetDir;

    protected boolean bCompression = false;

    protected Long maxFileSize = 1073741824L;

    public boolean bOverwrite;

    protected LinkedHashMap<String, Map.Entry<String, String>> metadata = new LinkedHashMap<String, Map.Entry<String, String>>();

    public WarcFileWriterConfig() {
    }

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
