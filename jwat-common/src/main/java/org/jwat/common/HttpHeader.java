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

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.SequenceInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a recognized HTTP response or request header with optional payload.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class HttpHeader {

    /** Response header type. */
    public static final int HT_RESPONSE = 1;

    /** Request header type. */
    public static final int HT_REQUEST = 2;

    /** HTTP scheme. */
    public static final String PROTOCOL_HTTP = "http";

    /** HTTPS scheme. */
    public static final String PROTOCOL_HTTPS = "https";

    /** HTTP Version suffix. */
    protected static final String HTTP_VERSION_SUFFIX = "HTTP/";

    /** Content-type header name. */
    protected static final String CONTENT_TYPE = "Content-Type".toUpperCase();

    /** Has record been closed before. */
    protected boolean bClosed;

    /** <code>InputStream</code> to read payload. */
    protected ByteCountingPushBackInputStream in_pb;

    /** Payload length. */
    protected long totalLength;

    /** Could the HTTP header be validated. */
    protected boolean bIsValid;

    /** Header type of this object. */
    public int headerType;

    /** Stream used to ensure we don't use more than the pushback buffer on
     *  headers and at the same time store everything read in an array. */
    protected MaxLengthRecordingInputStream in_flr;

    /** Actual message digest algorithm used. */
    protected MessageDigest md;

    /** Automatic digesting of payload input stream. */
    protected DigestInputStream in_digest;

    /** Boolean indicating no such algorithm exception under initialization. */
    protected boolean bNoSuchAlgorithmException;

    /** HTTP payload stream returned to user. */
    protected InputStream in_payload;

    /** Sequence of the header as a stream and the payload stream. */
    protected SequenceInputStream in_complete;

    /** Object size, in bytes. */
    public long payloadLength = 0L;

    /** Validation errors and warnings. */
    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /*
     * Request-Line.
     */

    /** HTTP Method. */
    public String method;

    /** HTTP Request-URI. */
    public String requestUri;

    /*
     * Http-Version.
     */

    /** HTTP-Version. */
    public String httpVersion;

    /** HTTP-Version major. */
    public Integer httpVersionMajor;

    /** HTTP-Version minor. */
    public Integer httpVersionMinor;

    /*
     * Status-Line.
     */

    /** HTTP Status-Code. */
    public String statusCodeStr;

    /** HTTP Status-Code. */
    public Integer statusCode;

    /** HTTP Reason-Phrase. */
    public String reasonPhrase;

    /*
     * Header-Fields.
     */

    /** List of parsed header fields. */
    protected List<HeaderLine> headerList = new LinkedList<HeaderLine>();

    /** Map of parsed header fields. */
    protected Map<String, HeaderLine> headerMap = new HashMap<String, HeaderLine>();

    /** HTTP content Content-type. */
    public String contentType;

    /**
     * Non public constructor.
     */
    protected HttpHeader() {
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
     * Reads the HTTP protocol response or request and returns it as an object.
     * It is important to understand that the maximum size of a parsed header
     * is equals to the size of the PushbackInputStream's buffer!
     * @param pbin payload input stream
     * @param length payload length
     * @param digestAlgorithm digest algorithm to use on payload or null if we
     * don't want a digest of the payload
     * @return <code>HttpResponse</code> based on the http headers
     * @throws IOException if an error occur while processing http header.
     */
    public static HttpHeader processPayload(int headerType, ByteCountingPushBackInputStream pbin,
                    long length, String digestAlgorithm) throws IOException {
        if (headerType != HT_RESPONSE && headerType != HT_REQUEST) {
            throw new IllegalArgumentException(
                    "Invalid 'headerType' argument: " + headerType);
        }
        if (pbin == null) {
            throw new IllegalArgumentException(
                    "The inputstream 'pbin' is null");
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "The 'length' is less than zero: " + length);
        }
        HttpHeader hh = new HttpHeader();
        hh.headerType = headerType;
        hh.in_pb = pbin;
        hh.totalLength = length;
        hh.in_flr = new MaxLengthRecordingInputStream(
                                        hh.in_pb, hh.in_pb.getPushbackSize());
        hh.bIsValid = hh.readHttpHeader(hh.in_flr, length);
        if (hh.bIsValid) {
            /*
             * Block Digest.
             */
            if (digestAlgorithm != null) {
                try {
                    hh.md = MessageDigest.getInstance(digestAlgorithm);
                } catch (NoSuchAlgorithmException e) {
                    hh.bNoSuchAlgorithmException = true;
                }
            }
            if (hh.md != null) {
                hh.in_digest = new DigestInputStreamNoSkip(hh.in_pb, hh.md);
                hh.in_payload = hh.in_digest;
            } else {
                hh.in_payload = hh.in_pb;
            }
            /*
             * Ensure close() is not called on the payload stream!
             */
            hh.in_payload = new FilterInputStream(hh.in_payload) {
                @Override
                public void close() throws IOException {
                }
            };
            hh.in_complete = new SequenceInputStream(new ByteArrayInputStream(hh.in_flr.getRecording()), hh.in_payload);
        } else {
            // Undo read and leave callers input stream in original state.
            hh.in_pb.unread(hh.in_flr.getRecording());
            hh.bClosed = true;
        }
        return hh;
    }

    /**
     * Reads the protocol response or request.
     * Updates the payloadLength field if the response or request  is valid.
     * @param in the input stream to parse.
     * @param payloadLength the record length.
     * @return boolean indicating whether the http header could be read
     * @throws IOException io exception while reading http headers
     */
    protected boolean readHttpHeader(MaxLengthRecordingInputStream in, long payloadLength)
                            throws IOException {
        PushbackInputStream pbin = new PushbackInputStream(in, 16);
        HeaderLineReader hlr = HeaderLineReader.getHeaderLineReader();
        hlr.encoding = HeaderLineReader.ENC_UTF8;
        boolean bValidHttpHeader = false;
        HeaderLine line = hlr.readLine(pbin);
        int bfErrors = 0;
        if (!hlr.bEof && line.type == HeaderLine.HLT_LINE && line.line != null && line.line.length() > 0) {
            bfErrors = (line.bfErrors & ~HeaderLineReader.E_BIT_INVALID_SEPARATOR_CHAR);
            if (headerType == HT_RESPONSE) {
                bValidHttpHeader = isHttpStatusLineValid(line.line);
            } else if (headerType == HT_REQUEST) {
                bValidHttpHeader = isHttpRequestLineValid(line.line);
            } else {
                throw new IllegalStateException("Invalid headerType!");
            }
        }
        HeaderLine tmpLine;
        boolean bLoop = bValidHttpHeader;
        while (bLoop) {
            line = hlr.readLine(pbin);
            bfErrors |= line.bfErrors;
            if (!hlr.bEof) {
                switch (line.type) {
                case HeaderLine.HLT_HEADERLINE:
                    //System.out.println(line.name);
                    //System.out.println(line.value);
                    if (CONTENT_TYPE.equals(line.name.toUpperCase())) {
                        contentType = line.value;
                    }
                    // A HeaderLine object contains a list of additional lines.
                    tmpLine = headerMap.get(line.name.toLowerCase());
                    if (tmpLine == null) {
                        headerMap.put(line.name.toLowerCase(), line);
                    } else {
                        tmpLine.lines.add(line);
                    }
                    headerList.add(line);
                    break;
                case HeaderLine.HLT_LINE:
                    if (line.line.length() == 0) {
                        bLoop = false;
                    } else {
                        // Errors reported by bfErrors.
                    }
                    break;
                }
            } else {
                // Accept truncated http header if it is the length of the payload.
                if ((bfErrors & HeaderLineReader.E_BIT_EOF) == 0 || in.record.size() != payloadLength) {
                    /*
                    System.out.println("Epic fail!");
                    System.out.println(Integer.toBinaryString(hlr.bfErrors));
                    System.out.println(new String(in.getRecording()));
                    */
                    bValidHttpHeader = false;
                }
                bLoop = false;
            }
        }
        HeaderLineReader.report_error(bfErrors, diagnostics);
        if (bValidHttpHeader) {
            this.payloadLength = payloadLength - in.record.size();
        }
        return bValidHttpHeader;
    }

    /**
     * Checks a HTTP Response Status-Line for validity.
     * @param statusLine the Status-Line of the HTTP Response
     * @return true/false based on whether the Status-Line is valid or not.
     */
    protected boolean isHttpStatusLineValid(String statusLine) {
        int idx;
        int prevIdx;
        boolean bIsHttpStatusLineValid = (statusLine != null) && (statusLine.length() > 0);
        if (bIsHttpStatusLineValid) {
            idx = statusLine.indexOf(' ');
            if (idx > 0) {
                bIsHttpStatusLineValid = isHttpVersionValid(statusLine.substring(0, idx));
            } else {
                if (idx == -1) {
                    httpVersion = statusLine;
                }
                bIsHttpStatusLineValid = false;
            }
            if (bIsHttpStatusLineValid) {
                prevIdx = ++idx;
                idx = statusLine.indexOf(' ', idx);
                if (idx == -1) {
                    idx = statusLine.length();
                }
                if (idx > prevIdx) {
                    statusCodeStr = statusLine.substring(prevIdx, idx);
                    try {
                        statusCode = Integer.parseInt(statusCodeStr);
                        if (statusCode < 100 || statusCode > 999) {
                            bIsHttpStatusLineValid = false;
                        }
                    } catch(NumberFormatException e) {
                        bIsHttpStatusLineValid = false;
                    }
                } else {
                    bIsHttpStatusLineValid = false;
                }
                if (bIsHttpStatusLineValid) {
                    if (idx < statusLine.length()) {
                        ++idx;
                        reasonPhrase = statusLine.substring(idx);
                    }
                }
            }
        }
        return bIsHttpStatusLineValid;
    }

    /**
     * Checks a HTTP-Version string for validity.
     * @param versionString the HTTP-Version of the HTTP header
     * @return true/false based on whether the HTTP-Version is valid or not.
     */
    protected boolean isHttpVersionValid(String versionString) {
        int idx;
        httpVersion = versionString;
        boolean bIsHttpVersionValid = versionString.startsWith(HTTP_VERSION_SUFFIX);
        if (bIsHttpVersionValid) {
            idx = versionString.indexOf('.', HTTP_VERSION_SUFFIX.length());
            if (idx > 0) {
                try {
                    httpVersionMajor = Integer.parseInt(versionString.substring(HTTP_VERSION_SUFFIX.length(), idx));
                    if (httpVersionMajor < 0) {
                        bIsHttpVersionValid = false;
                    }
                } catch (NumberFormatException e) {
                    bIsHttpVersionValid = false;
                }
                try {
                    httpVersionMinor = Integer.parseInt(versionString.substring(idx + 1));
                    if (httpVersionMinor < 0) {
                        bIsHttpVersionValid = false;
                    }
                } catch (NumberFormatException e) {
                    bIsHttpVersionValid = false;
                }
            } else {
                bIsHttpVersionValid = false;
            }
        }
        return bIsHttpVersionValid;
    }

    /**
     * Checks a HTTP Request Request-Line for validity.
     * @param requestLine the Request-Line of the HTTP Request
     * @return true/false based on whether the Request-Line is valid or not.
     */
    protected boolean isHttpRequestLineValid(String requestLine) {
        int idx;
        int prevIdx;
        boolean bIsHttpRequestLineValid = (requestLine != null) && (requestLine.length() > 0);
        if (bIsHttpRequestLineValid) {
            idx = requestLine.indexOf(' ');
            if (idx > 0) {
                method = requestLine.substring(0, idx);
            } else {
                if (idx == -1) {
                    method = requestLine;
                }
                bIsHttpRequestLineValid = false;
            }
            if (bIsHttpRequestLineValid) {
                prevIdx = ++idx;
                idx = requestLine.indexOf(' ', idx);
                if (idx > prevIdx) {
                    requestUri = requestLine.substring(prevIdx, idx);
                } else {
                    if (idx == -1) {
                        requestUri = requestLine.substring(prevIdx);
                    }
                    bIsHttpRequestLineValid = false;
                }
                if (bIsHttpRequestLineValid) {
                    ++idx;
                    bIsHttpRequestLineValid = isHttpVersionValid(requestLine.substring(idx));
                }
            }
        }
        return bIsHttpRequestLineValid;
    }

    /**
     * Returns the result of the HTTP header validation.
     * @return the result of the HTTP header validation
     */
    public boolean isValid() {
        return bIsValid;
    }

    /**
     * Get a <code>List</code> of all the headers found during parsing.
     * @return <code>List</code> of <code>HeaderLine</code>
     */
    public List<HeaderLine> getHeaderList() {
        return Collections.unmodifiableList(headerList);
    }

    /**
     * Get a header line structure or null, if no header line structure is
     * stored with the given header name.
     * @param field header name
     * @return WARC header line structure or null
     */
    public HeaderLine getHeader(String field) {
        if (field != null && field.length() > 0) {
            return headerMap.get(field.toLowerCase());
        } else {
            return null;
        }
    }

    /**
     * Result-Code string getter
     * @return the ResultCode
     */
    public String getProtocolStatusCodeStr() {
        return statusCodeStr;
    }

    /**
     * Result-Code integer getter
     * @return the ResultCode
     */
    public Integer getProtocolStatusCode() {
        return statusCode;
    }

    /**
     * protocolVersion getter
     * @return the protocolVersion
     */
    public String getProtocolVersion() {
        return httpVersion;
    }

    /**
     * Content-Type getter
     * @return the Content-Type
     */
    public String getProtocolContentType() {
        return contentType;
    }

    /**
     * Get the raw HTTP header as bytes.
     * @return raw HTTP header as bytes
     */
    public byte[] getHeader() {
        return in_flr.getRecording();
    }

    /**
     * Returns the <code>MessageDigest</code> used on payload stream.
     * @return <code>MessageDigest</code> used on payload stream
     */
    public MessageDigest getMessageDigest() {
        return md;
    }

    /**
     * Get HTTP payload length.
     * @return HTTP payload length
     */
    public long getPayloadLength() {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return payloadLength;
    }

    /**
     * Get payload total length, header and payload.
     * @return payload total length, header and payload
     */
    public long getTotalLength() {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return totalLength;
    }

    /**
     * Get the number of unavailable bytes missing due to unexpected EOF.
     * This method always returns <code>0</code> as long as the stream is open.
     * @return number of unavailable bytes missing due to unexpected EOF
     * @throws IOException if errors occur calling available method on stream
     */
    public long getUnavailable() throws IOException {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return totalLength - in_pb.getConsumed();
    }

    /**
     * Get an <code>InputStream</code> containing both the header and the
     * payload.
     * @return <code>InputStream</code> containing both the header and the
     * payload.
     */
    public InputStream getInputStreamComplete() {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return in_complete;
    }

    /**
     * Get an <code>InputStream</code> containing only the payload.
     * @return <code>InputStream</code> containing only the payload.
     */
    public InputStream getPayloadInputStream() {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return in_payload;
    }

    /**
     * Get payload remaining length.
     * @return payload remaining length
     * @throws IOException if errors occur calling available method on stream
     */
    public long getRemaining() throws IOException {
        if (!bIsValid) {
            throw new IllegalStateException("HttpHeader not valid");
        }
        return totalLength - in_pb.getConsumed();
    }

    /**
     * Check to see if the HTTP header has been closed.
     * @return boolean indicating whether this HTTP header is closed or not
     */
    public boolean isClosed() {
        return bClosed;
    }

    /**
     * Closes the this payload stream, skipping unread bytes in the process.
     * @throws IOException io exception in closing process
     */
    public void close() throws IOException {
        if (!bClosed) {
            if (md != null) {
                // Skip remaining unread bytes to ensure payload is completely
                // digested. Skipping because the DigestInputStreamNoSkip
                // has been altered to read when skipping.
                while (in_digest.skip(totalLength) > 0) {
                }
            }
            if (in_pb != null) {
                in_pb.close();
                in_pb = null;
            }
            bClosed = true;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("\nHttpResponse : [\n");
        if (statusCode != null) {
            builder.append(", HttpResultCode: ")
                .append(statusCode);
        }
        if (httpVersion != null) {
            builder.append(", HttpProtocolVersion: ")
                .append(httpVersion);
        }
        if (contentType != null) {
            builder.append(", HttpContentType: ")
                .append(contentType);
        }
        builder.append("]\n");
        return builder.toString();
    }

}
