/**
 * JHOVE2 - Next-generation architecture for format-aware characterization
 *
 * Copyright (c) 2009 by The Regents of the University of California,
 * Ithaka Harbors, Inc., and The Board of Trustees of the Leland Stanford
 * Junior University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * o Neither the name of the University of California/California Digital
 *   Library, Ithaka Harbors/Portico, or Stanford University, nor the names of
 *   its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jhove2.module.format.arc;

/**
 * Supported error types.
 *
 * @author lbihanic, selghissassi, nicl
 */
public final class ArcConstants {

	/** Arc file magic number */
	public static final String ARC_SCHEME = "filedesc://";

	/** Version block content type */
	public static final String VERSION_BLOCK_CONTENT_TYPE = "text/plain"; 

	/**
	 * Version block fields.
	 */
	public static final String URL_FIELD                = "URL";
	public static final String IP_ADDRESS_FIELD         = "IP-address";
	public static final String DATE_FIELD               = "Archive-date";
	public static final String CONTENT_TYPE_FIELD       = "Content-type";
	public static final String LENGTH_FIELD             = "Archive-length";
	public static final String RESULT_CODE_FIELD        = "Result-code";
	public static final String CHECKSUM_FIELD           = "Checksum";
	public static final String LOCATION_FIELD           = "Location";
	public static final String OFFSET_FIELD             = "Offset";
	public static final String FILENAME_FIELD           = "Filename";
	public static final String VERSION_FIELD            = "Version-number";
	public static final String RESERVED_FIELD           = "Reserved";
	public static final String ORIGIN_FIELD             = "Origin-code";

	/** Version-1-block fields */
	public static final String[] VERSION_1_BLOCK_FIELDS = {
	        URL_FIELD, IP_ADDRESS_FIELD, DATE_FIELD, CONTENT_TYPE_FIELD,
	        LENGTH_FIELD};
	/** Version-2-block fields */
	public static final String[] VERSION_2_BLOCK_FIELDS = {
	        URL_FIELD, IP_ADDRESS_FIELD, DATE_FIELD, CONTENT_TYPE_FIELD,
	        RESULT_CODE_FIELD, CHECKSUM_FIELD, LOCATION_FIELD,
	        OFFSET_FIELD, FILENAME_FIELD,
	        LENGTH_FIELD };

	/** Version description fields */
	public static final String[] VERSION_DESC_FIELDS = {
		VERSION_FIELD, RESERVED_FIELD, ORIGIN_FIELD };

	public static final String VERSION_1_BLOCK_DEF =
					join(' ', VERSION_1_BLOCK_FIELDS);
	public static final String VERSION_2_BLOCK_DEF =
					join(' ', VERSION_2_BLOCK_FIELDS);

	private static final String join(char sep, String... elts) {
		StringBuilder buf = new StringBuilder();
		for (String s : elts) {
			buf.append(s).append(sep);
		}
		buf.setLength(buf.length() - 1);
		return buf.toString();
	}

}
