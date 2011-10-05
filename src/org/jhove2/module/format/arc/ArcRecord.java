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

import java.io.IOException;
import java.util.Collection;

/**
 * ARC record parser.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcRecord extends ArcRecordBase {

	public static final String CONTENT_TYPE_NO_TYPE = "no-type";

	public HttpResponse httpResponse = null;

	protected ArcRecord() {
	}

	public static ArcRecord parseArcRecord(ByteCountingInputStream in,
										ArcVersionBlock versionBlock) {
		ArcRecord ar = new ArcRecord();
		ar.versionBlock = versionBlock;
		ar.version = versionBlock.version;
		try {
			// Read record line.
			// Looping past empty lines.
			ar.startOffset = in.getOffset();
			String recordLine = in.readLine();
			while ((recordLine != null) && (recordLine.length() == 0)) { 
				ar.startOffset = in.getOffset();
				recordLine = in.readLine();
			}
			if (recordLine != null) {
				ar.parseRecord(recordLine);
			}
			else {
				// EOF
				ar = null;
			}
			if (ar != null) {
				ar.processPayload(in);
			}
		}
		catch (IOException e) {
		}
		return ar;
	}

	/* (non-Javadoc)
	 * @see org.jhove2.module.format.arc.ArcRecordBase#parseNetworkDoc()
	 */
	protected void processPayload(ByteCountingInputStream in) throws IOException {
	    payload = null;
		if (r_length != null && r_length > 0L) {
			payload = new ArcPayload(in, r_length.longValue());
			if (HttpResponse.isSupported(protocol)
							&& !CONTENT_TYPE_NO_TYPE.equals(r_contentType)) {
				httpResponse = HttpResponse.parseProtocolResponse(payload.in,
														r_length.longValue());
			}
		}
		else if (HttpResponse.isSupported(protocol)
							&& !CONTENT_TYPE_NO_TYPE.equals(r_contentType)) {
			// TODO warning payload expected
		}
	    return;
	}
	
	/**
	 * Checks if the ARC record has warnings.
	 * @return true/false based on whether the ARC record has warnings or not 
	 */
	@Override
	public boolean hasWarnings() {
	    return ((httpResponse != null) && (httpResponse.hasWarnings()));
	}

	/**
	 * Gets Network doc warnings.
	 * @return validation errors list/
	 */
	@Override
	public Collection<String> getWarnings() {
	    return (hasWarnings())? httpResponse.getWarnings() : null;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(256);
		builder.append("\nArcRecord [");
		builder.append( super.toString() );
		builder.append(']');
		if ( payload != null ) {
			builder.append( payload.toString() );
		}
		return builder.toString();
	}
}
