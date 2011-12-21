package dk.netarkivet.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a recognized HttpResponse payload.
 *
 * @author lbihanic, selghissassi
 */
public class HttpResponse {

    /** Http scheme. */
	public static final String PROTOCOL_HTTP = "http";
	/** Https scheme. */
	public static final String PROTOCOL_HTTPS = "https";

	/** Line feed constant. */
	protected static final int LF = '\n';
	/** Carriage return constant. */
	protected static final int CR = '\r';

	/** Http protocol. */
	protected static final String HTTP = "HTTP";

	/** Content-type header name. */
	protected static final String CONTENT_TYPE = "Content-Type:".toUpperCase();

	/** <code>InputStream</code> to read payload. */
	protected ByteCountingPushBackInputStream pbin;

    /** Payload length. */
    protected long length;

    /** Stream used to ensure we don't use more than the pushback buffer on
     *  headers and at the same time store everything read in an array. */
	protected FixedLengthRecordingInputStream flrin;

	/** Actual message digest algorithm used. */
    protected MessageDigest md;

    /** Automatic digesting of payload input stream. */
    protected DigestInputStream din;

    /** Boolean indicating no such algorithm exception under initialization. */
    protected boolean bNoSuchAlgorithmException;

    /** Http payload stream. */
    protected InputStream in;

    /** Http result code. */
	public String resultCode;

	/** Http protocol version. */
	public String protocolVersion;

	/** Http content Content-type. */
	public String contentType;

	/** Object size, in bytes. */
	public long objectSize = 0L;

	/** Warnings detected when processing HTTP protocol response. */
	protected List<String> warnings = null;

	/**
	 * Non public constructor.
	 */
	protected HttpResponse() {
	}

	/**
	 * Boolean indicating whether a protocol is supported by this 
	 * payload inspector.
	 * @param protocol protocol name
	 * @return true/false if the protocol is supported or not.
	 */
	public static boolean isSupported(String protocol) {
		return ((PROTOCOL_HTTP.equalsIgnoreCase(protocol)
		        || PROTOCOL_HTTPS.equalsIgnoreCase(protocol)));
	}

	/**
	 * Reads the HTTP protocol response and return it as an object.
	 * @param pbin payload input stream
	 * @param length payload length
     * @param digestAlgorithm digest algorithm to use on payload or null if we
     * don't want a digest of the payload
	 * @return <code>HttpResponse</code> based on the http headers
	 * @throws IOException if an error occur while processing http header.
	 */
	public static HttpResponse processPayload(ByteCountingPushBackInputStream pbin,
					long length, String digestAlgorithm) throws IOException {
        if (pbin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'pbin' is null");
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "The 'length' is less than zero: " + length);
        }
		HttpResponse hr = new HttpResponse();
		hr.pbin = pbin;
        hr.length = length;
		hr.flrin = new FixedLengthRecordingInputStream(hr.pbin, 8192);		// TODO payload pushback buffer size
		hr.objectSize = hr.readProtocolResponse(hr.flrin, length);
        /*
         * Block Digest.
         */
        if (digestAlgorithm != null) {
            try {
                hr.md = MessageDigest.getInstance(digestAlgorithm);
            } catch (NoSuchAlgorithmException e) {
            	hr.bNoSuchAlgorithmException = true;
            }
        }
        if (hr.md != null) {
            hr.din = new DigestInputStreamNoSkip(hr.pbin, hr.md);
            hr.in = hr.din;
        } else {
        	hr.in = hr.pbin;
        }
        /*
         * Ensure close() is not called on the payload stream!
         */
        hr.in = new FilterInputStream(hr.in) {
            @Override
            public void close() throws IOException {
            }
        };
		return hr;
	}

	/**
	 * Checks protocol response validity.
	 * @param firstLine the first line of the HTTP response 
	 * @return true/false based on whether the protocol response is valid or not.
	 */
	protected boolean isProtocolResponseValid(String firstLine) {
		boolean isValid = (firstLine != null);
		if(isValid){
			String [] parameters = firstLine.split(" ");
			if(parameters.length < 2 || !parameters[0].startsWith(HTTP)) {
				isValid = false;
			}
			if (isValid) {
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
	 * @throws IOException io exception while reading http headers
	 */
	protected long readProtocolResponse(InputStream in, long recordlength) 
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
		if (invalidProtocolResponse) {
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
	 * Read a CRLF terminated string in the stream (keep track of the position).
	 * @param in the input stream to parse
	 * @param max the max length 
	 * @return a CRLF terminated string
	 * @throws IOException io exception while reading line
	 */
	protected LineToken readStringUntilCRLF(InputStream in, long max)
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
	 * Warnings getter.
	 * @return the warnings
	 */
	public List<String> getWarnings() {
		return Collections.unmodifiableList(this.warnings);
	}

	/**
	 * Boolean indicating whether this http noted any warnings while parsing 
	 * the headers.
	 * @return true/false indicating if there are warnings
	 */
	public boolean hasWarnings() {
	    return ((warnings != null) && warnings.isEmpty());
	}

	/**
	 * Add an additional warning message to the list.
	 * @param w warning message
	 */
	private void addWarning(String w) {
	    if (this.warnings == null) {
	        this.warnings = new LinkedList<String>();
	    }
	    this.warnings.add(w);
	}

	/**
	 * Network doc setter.
	 * @throws IOException
	 */
	/*
	public void setNetworkDoc() throws IOException{
		ArcPayload networkDoc = this.processNetworkDoc();
		if(networkDoc != null){
			this.payload = networkDoc;
			this.protocolResultCode = this.parseInteger(networkDoc.resultCode,
					                                    null,true);
			this.protocolVersion = this.parseString(networkDoc.protocolVersion,
					                                null, true);
			this.protocolContentType = this.parseString(networkDoc.contentType,
					                                    null,true);
		}
		this.validateNetworkDoc();
	}
	*/

	/**
	 * Result-Code getter
	 * @return the ResultCode
	 */
	public String getProtocolResultCode() {
		return resultCode;
	}

	/**
	 * protocolVersion getter
	 * @return the protocolVersion
	 */
	public String getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * Content-Type getter
	 * @return the Content-Type
	 */
	public String getProtocolContentType() {
		return contentType;
	}

	/**
	 * Get http response payload length.
	 * @return http response payload length
	 */
	public long getPayloadLength() {
		return objectSize;
	}

	/**
	 * Get the raw http header as bytes.
	 * @return raw http header as bytes
	 */
	public byte[] getHeader() {
		return flrin.getRecording();
	}

	/**
     * Returns the <code>MessageDigest</code> used on payload stream.
     * @return <code>MessageDigest</code> used on payload stream
     */
    public MessageDigest getMessageDigest() {
        return md;
    }

    /**
     * Get payload total length, header and payload.
     * @return payload total length, header and payload
     */
    public long getTotalLength() {
        return length;
    }

    /**
     * Get the number of unavailable bytes missing due to unexpected EOF.
     * This method always returns <code>0</code> as long as the stream is open.
     * @return number of unavailable bytes missing due to unexpected EOF
     * @throws IOException if errors occur calling available method on stream
     */
    public long getUnavailable() throws IOException {
        return length - pbin.getConsumed();
    }

    /**
     * Get an <code>InputStream</code> containing both the header and the
     * payload.
     * @return <code>InputStream</code> containing both the header and the
     * payload.
     */
	public InputStream getInputStreamComplete() {
		return new SequenceInputStream(new ByteArrayInputStream(flrin.getRecording()), in);
	}

	/**
	 * Get an <code>InputStream</code> containing only the payload.
	 * @return <code>InputStream</code> containing only the payload.
	 */
	public InputStream getPayloadInputStream() {
		return in;
	}

    /**
     * Get payload remaining length.
     * @return payload remaining length
     * @throws IOException if errors occur calling available method on stream
     */
    public long getRemaining() throws IOException {
    	return length - pbin.getConsumed();
    }

    /**
     * Closes the this payload stream, skipping unread bytes in the process.
     * @throws IOException io exception in closing process
     */
	public void close() throws IOException {
        if (md != null) {
        	// Skip remaining unread bytes to ensure payload is completely
        	// digested. Skipping because the DigestInputStreamNoSkip
        	// has been altered to read when skipping.
            long s;
            while ((s = din.skip(length)) != -1) {
            }
        }
        if (pbin != null) {
            pbin.close();
            pbin = null;
        }
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(256);
		builder.append("\nHttpResponse : [\n");
		if (resultCode != null) {
		    builder.append(", HttpResultCode: ")
		    	.append(resultCode);
		}
		if (protocolVersion != null) {
		    builder.append(", HttpProtocolVersion: ")
		    	.append(protocolVersion);
		}
		if (contentType != null) {
		    builder.append(", HttpContentType: ")
		    	.append(contentType);
		}
		builder.append("]\n");
		return builder.toString();
	}

	private static final class LineToken {
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
