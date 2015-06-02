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

/**
 * Implementations of this interface are used to name the ARC files written by the <code>ArcFileWriter</code>.
 *
 * @author nicl
 */
public interface ArcFileNaming {

    /**
     * Does this naming implementation support multiple files.
     * @return boolean indicating support for multiple files
     */
    public boolean supportMultipleFiles();

    /**
     * Return the next file name to use.
     * @param sequenceNr sequence number to use
     * @param bCompressed is the file compressed or not
     * @return the next file name to use
     */
    public String getFilename(int sequenceNr, boolean bCompressed);

}
