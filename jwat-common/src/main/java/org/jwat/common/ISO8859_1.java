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
package org.jwat.common;

import java.io.ByteArrayOutputStream;

/**
 * Small class to decode and encode ISO-8859-1 strings while also validating
 * them. Invalid characters are removed in the conversion.
 * Non static because not all information can be returned by the methods. 
 *
 * @author nicl
 */
public class ISO8859_1 {

	/** Array of valid bytes according to the ISO-8859-1 specification. */
	public static final byte[] validBytes = new byte[256];

	/** Populate array of valid ISO-8859-1 bytes. */
	static {
		int i;
		for (i=0; i<32; ++i) {
			validBytes[i] = 0;
		}
		for (i=32; i<127; ++i) {
			validBytes[i] = (byte)i;
		}
		validBytes[127] = 0;
		for (i=128; i<256; ++i) {
			validBytes[i] = (byte)i;
		}
	}

	/**
	 * Construct an instance that can be used to convert and validate.
	 */
	public ISO8859_1() {
	}

	/** Encoded string after call to encode method. */
	public byte[] encoded;

	/**
	 * Converts a string to a byte array removing invalid characters in the
	 * process and returning the validity status. The converted byte array
	 * is accessible through a separate field.
	 * @param inStr string to be converted and validated
	 * @param exceptions invalid characters which are allowed
	 * @return validity status of string to byte array conversion
	 */
	public boolean encode(String inStr, String exceptions) {
		boolean valid = true;
		StringBuffer sb = new StringBuffer();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		char c;
		for (int i=0; i<inStr.length(); ++i) {
			c = inStr.charAt(i);
			if (c < 256) {
				if (validBytes[c] != 0 || exceptions.indexOf(c) != -1) {
					sb.append((char)c);
					out.write(c);
				} else {
					valid = false;
				}
			} else {
				valid = false;
			}
		}
		decoded = sb.toString();
		encoded = out.toByteArray();
		return valid;
	}

	/** Decode byte array after call to decode method. */
	public String decoded;

	/**
	 * Converts a byte array to a string removing invalid characters in the
	 * process and returning the validity status. The converted string is
	 * accessible through a separate field.
	 * @param inBytes byte array to be converted and validated
	 * @param exceptions invalid characters which are allowed
	 * @return validity status of byte array to string conversion
	 */
	public boolean decode(byte[] inBytes, String exceptions) {
		boolean valid = true;
		StringBuffer sb = new StringBuffer();
		int c;
		for (int i=0; i<inBytes.length; ++i) {
			c = inBytes[i] & 255;
			if (validBytes[c] != 0 || exceptions.indexOf(c) != -1 ) {
				sb.append((char)c);
			} else {
				valid = false;
			}
		}
		decoded = sb.toString();
		return valid;
	}

}
