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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Arc record payload.
 *
 * @author lbihanic, selghissassi
 */
public class HttpResponse {

	private final static int LF = '\n';
	private final static int CR = '\r';
	private final static String HTTP = "HTTP";
	private final static String CONTENT_TYPE = "Content-Type:".toUpperCase();

	/** Protocol response result code */
	public String resultCode;

	/** Protocol response version */
	public String protocolVersion;

	/** Protocol response content type */
	public String contentType;

	/** Object size, in bytes */
	public long objectSize = 0L;

	/** Warnings detected when processing HTTP protocol response. */
	private List<String> warnings = null;

	/**
	 * Reads the HTTP protocol response, if required.
	 * @param in input response
	 * @param containsProtocolResponse specifies if the network doc must 
	 * contain protocol response or not.  
	 * @throws IOException
	 */
	/*
	private void parseProtocolResponse(InputStream in,
                                           boolean containsProtocolResponse) 
	                                                     throws IOException{
		if (containsProtocolResponse) {
			objectSize = this.readProtocolResponse(this.in, length);
		}
	}
	*/

	/**
	 * Checks protocol response validity.
	 * @param firstLine the first line of the HTTP response 
	 * @return true/false based on whether the protocol response is valid or not.
	 */
	private boolean isProtocolResponseValid(String firstLine){
		boolean isValid = (firstLine != null);
		if(isValid){
			String [] parameters = firstLine.split(" ");
			if(parameters.length < 2 || !parameters[0].startsWith(HTTP)) {
				isValid = false;
			}
			if(isValid) {
				try {
					int parameter = Integer.parseInt(parameters[1]);
					if(parameter < 100 || parameter > 999) {
						isValid = false;
					}
				}
				catch(NumberFormatException e) {
					isValid = false;
				}
			}
		}
		return isValid;
	}

	/**
	 * Reads the protocol response.
	 * @param in the input stream to parse.
	 * @param recordlength the record length.
	 * @return the bytes read
	 * @throws IOException
	 */
	private long readProtocolResponse(InputStream in, long recordlength) 
							throws IOException {
		long remaining = recordlength;
		boolean firstLineMatched = false;
		boolean invalidProtocolResponse = false;
		int linesWithoutCrEnding = 0;
		int linesWithoutLfEnding = 0;
		//mark the current position. An exception is thrown if the input 
		//stream does not support mark operation
		in.mark(4096);
		while (true) {
			if (remaining == 0L) {
				break;
			}
			LineToken token = readStringUntilCRLF(in, remaining);
			remaining -= token.consumed;
			String l = token.line;
			if (token.missingCr) {
			    linesWithoutCrEnding++;
			}
			if (token.missingLf) {
			    linesWithoutLfEnding++;
			}
			if (!firstLineMatched && (l.length() != 0)) {
				if (!isProtocolResponseValid(l)){
					this.addWarning("Invalid HTTP response header: " + l);
					invalidProtocolResponse = true;
					break;
				}
				String [] parameters = l.split(" ");
				this.protocolVersion = parameters[0];
				this.resultCode = parameters[1];
				firstLineMatched = true;
			}
			if (l.length() == 0) {
				break;
			}
			if (l.toUpperCase().startsWith(CONTENT_TYPE)) {
				this.contentType = l.substring(l.indexOf(':') + 1).trim();
			}
		}
		if (invalidProtocolResponse){
			//if the protocol response is invalid, re-read the input stream. In this case
			//the object of the ARC record is equal to network doc.
			in.reset();
			remaining = recordlength;
		}
		if (linesWithoutCrEnding != 0) {
		    this.addWarning("" + linesWithoutCrEnding +
		        " LF-only line ending(s) found in HTTP response header");
		}
		if (linesWithoutLfEnding != 0) {
                    this.addWarning("" + linesWithoutLfEnding +
                        " CR-only line ending(s) found in HTTP response header");
		}
		return remaining;
	}

	/**
	 * Read a CRLF terminated string in the stream (keep track of the position)
	 * @param in the input stream to parse
	 * @param max the max length 
	 * @return
	 * @throws IOException
	 */
	public LineToken readStringUntilCRLF(InputStream in, long max)
	                                                    throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
		int b;
		long pos = 0;
		boolean bCRFound = false;
		int sizeEnd = 1;
		while (true) {
			b = in.read();
			if (b == -1) {
				//Unexpected end of file
				throw new EOFException();
			}
			if (b == CR) {
				bCRFound = true;
				continue;
			}
			if (b == LF) {
				if (bCRFound) {
					sizeEnd = 2;
					break;
				}
				break;
			}
			if (bCRFound) {
				bos.write(b);
				break;
			}
			bos.write(b);
			pos++;
			if (pos >= max) {
				sizeEnd = 0;
				break;
			}
		}
		String line = bos.toString();
		return new LineToken(line, line.length() + sizeEnd,
		                     (bCRFound && sizeEnd != 2), (! bCRFound));
	}

	/**
	 * warnings getter
	 * @return the warnings
	 */
	public List<String> getWarnings() {
		return Collections.unmodifiableList(this.warnings);
	}

	public boolean hasWarnings() {
	    return ((this.warnings != null) && (this.warnings.isEmpty() == false));
	}

	private void addWarning(String w) {
	    if (this.warnings == null) {
	        this.warnings = new LinkedList<String>();
	    }
	    this.warnings.add(w);
	}

    private final static class LineToken
    {
        public final String line;
        public final int consumed;
        public final boolean missingLf;
        public final boolean missingCr;

        public LineToken(String line, int consumed, boolean missingLf,
                                                    boolean missingCr) {
            this.line      = line;
            this.consumed  = consumed;
            this.missingLf = missingLf;
            this.missingCr = missingCr;
        }
    }
}
