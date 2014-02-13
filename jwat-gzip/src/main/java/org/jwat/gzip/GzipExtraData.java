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
package org.jwat.gzip;

/**
 * GZip extra data container.
 *
 * @author nicl
 */
public class GzipExtraData {

	/** Subfield ID1. */
	public byte si1;

	/** Subfield ID2. */
	public byte si2;

	/** Subfield data. */
	public byte[] data;

	public GzipExtraData() {
	}

	public GzipExtraData(byte si1, byte si2, byte[] data) {
		this.si1 = si1;
		this.si2 = si2;
		this.data = data;
	}

}
