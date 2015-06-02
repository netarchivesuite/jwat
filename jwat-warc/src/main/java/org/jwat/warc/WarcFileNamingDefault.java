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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Default WARC file naming implementation used for writing to multiple files.
 * (prefix-date-sequenceNr-hostname.extension)
 *
 * @author nicl
 */
public class WarcFileNamingDefault implements WarcFileNaming {

    /** <code>DateFormat</code> to the following format 'yyyyMMddHHmmss'. */
    protected DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    /** Prefix component. */
    protected String filePrefix;

    /** Date component. */
    protected Date date;

    /** Date component converted into a human readable string. */
    protected String dateStr;

    /** Host name component. */
    protected String hostname;

    /** Extension component (including leading "."). */
    protected String extension;

    /**
     * Construct file naming instance.
     * @param filePrefix prefix or null, will default to "JWAT"
     * @param date date or null, if you want current date
     * @param hostname host name or null, if you want to use default local host name
     * @param extension extension or null, will default to ".warc"
     */
    public WarcFileNamingDefault(String filePrefix, Date date, String hostname, String extension) {
        if (filePrefix != null) {
            this.filePrefix = filePrefix;
        } else {
            this.filePrefix = "JWAT";
        }
        if (date != null) {
            this.date = date;
        } else {
            this.date = new Date();
        }
        if (hostname != null ) {
            this.hostname = hostname;
        } else {
            try {
                this.hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
            } catch (UnknownHostException e) {
                this.hostname = "unknown";
            }
        }
        if (extension != null) {
            this.extension = extension;
        } else {
            this.extension = ".warc";
        }
        dateStr = dateFormat.format(this.date);
    }

    @Override
    public boolean supportMultipleFiles() {
        return true;
    }

    @Override
    public String getFilename(int sequenceNr, boolean bCompressed) {
        String filename = filePrefix + "-" + dateStr + "-" + String.format("%05d", sequenceNr++) + "-" + hostname + extension;
        if (bCompressed) {
            filename += ".gz";
        }
        return filename;
    }

}
