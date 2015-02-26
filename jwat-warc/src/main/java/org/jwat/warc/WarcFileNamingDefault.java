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

public class WarcFileNamingDefault implements WarcFileNaming {

    protected DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    protected String filePrefix;

    protected Date date;

    protected String dateStr;

    protected String hostname;

    protected String extension;

    public WarcFileNamingDefault(String filePrefix, Date date, String hostname, String extension) {
    	if (filePrefix != null) {
    		this.filePrefix = filePrefix;
    	} else {
    		filePrefix = "JWAT";
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
                throw new RuntimeException(e);
            }
    	}
    	if (extension != null) {
        	this.extension = extension;
    	} else {
    		this.extension = ".warc";
    	}
        dateStr = dateFormat.format(date);
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
