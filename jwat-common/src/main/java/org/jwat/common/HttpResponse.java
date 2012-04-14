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
 * This class represents a recognized HttpResponse payload.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class HttpResponse {

    /** Http scheme. */
    public static final String PROTOCOL_HTTP = "http";
    /** Https scheme. */
    public static final String PROTOCOL_HTTPS = "https";

    /** Http protocol. */
    protected static final String HTTP = "HTTP";

    /** Content-type header name. */
    protected static final String CONTENT_TYPE = "Content-Type".toUpperCase();

    /** Has record been closed before. */
    protected boolean bClosed;

    /** <code>InputStream</code> to read payload. */
    protected ByteCountingPushBackInputStream in_pb;

    /** Payload length. */
    protected long totalLength;

    /** Stream used to ensure we don't use more than the pushback buffer on
     *  headers and at the same time store everything read in an array. */
    protected MaxLengthRecordingInputStream in_flr;

    /** Actual message digest algorithm used. */
    protected MessageDigest md;

    /** Automatic digesting of payload input stream. */
    protected DigestInputStream in_digest;

    /** Boolean indicating no such algorithm exception under initialization. */
    protected boolean bNoSuchAlgorithmException;

    /** Http payload stream returned to user. */
    protected InputStream in_payload;

    protected SequenceInputStream in_complete;

    /** Object size, in bytes. */
    public long payloadLength = 0L;

    /** Warnings detected when processing HTTP protocol response. */
    protected List<String> warnings = null;

    public final Diagnostics<Diagnosis> diagnostics = new Diagnostics<Diagnosis>();

    /*
     * Header-Fields.
     */

    /** Http protocol version. */
    public String protocolVersion;

    /** Http result code. */
    public String resultCodeStr;

    /** Http result code. */
    public Integer resultCode;

    /** Http result message. */
    public String resultMessage;

    /** List of parsed header fields. */
    protected List<HeaderLine> headerList = new LinkedList<HeaderLine>();

    /** Map of parsed header fields. */
    protected Map<String, HeaderLine> headerMap = new HashMap<String, HeaderLine>();

    /** Http content Content-type. */
    public String contentType;

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
     * It is important to understand that the maximum size of a parsed header
     * is equals to the size of the PushbackInputStream's buffer!
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
        hr.in_pb = pbin;
        hr.totalLength = length;
        hr.in_flr = new MaxLengthRecordingInputStream(
                                        hr.in_pb, hr.in_pb.getPushbackSize());
        hr.payloadLength = hr.readHttpResponse(hr.in_flr, length);
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
            hr.in_digest = new DigestInputStreamNoSkip(hr.in_pb, hr.md);
            hr.in_payload = hr.in_digest;
        } else {
            hr.in_payload = hr.in_pb;
        }
        /*
         * Ensure close() is not called on the payload stream!
         */
        hr.in_payload = new FilterInputStream(hr.in_payload) {
            @Override
            public void close() throws IOException {
            }
        };
        hr.in_complete = new SequenceInputStream(new ByteArrayInputStream(hr.in_flr.getRecording()), hr.in_payload);
        return hr;
    }

    /**
     * Reads the protocol response.
     * @param in the input stream to parse.
     * @param payloadLength the record length.
     * @return the bytes read
     * @throws IOException io exception while reading http headers
     */
    protected long readHttpResponse(MaxLengthRecordingInputStream in, long payloadLength)
                            throws IOException {
        PushbackInputStream pbin = new PushbackInputStream(in, 16);
        HeaderLineReader hlr = HeaderLineReader.getHeaderLineReader();
        hlr.encoding = HeaderLineReader.ENC_UTF8;
        boolean bValidHttpResponse = false;
        HeaderLine line = hlr.readLine(pbin);
        if (!hlr.bEof && line.type == HeaderLine.HLT_LINE && line.line != null && line.line.length() > 0) {
            bValidHttpResponse = isHttpStatusLineValid(line.line);
        }
        boolean bLoop = bValidHttpResponse;
        while (bLoop) {
            line = hlr.readLine(pbin);
            if (!hlr.bEof) {
                switch (line.type) {
                case HeaderLine.HLT_HEADERLINE:
                    //System.out.println(line.name);
                    //System.out.println(line.value);
                    if (CONTENT_TYPE.equals(line.name.toUpperCase())) {
                        contentType = line.value;
                    }
                    // Uses a list because there can be multiple occurrences.
                    // TODO concat multiple identical headers separated by ,
                    headerMap.put(line.name.toLowerCase(), line);
                    headerList.add(line);
                    break;
                case HeaderLine.HLT_LINE:
                    if (line.line.length() == 0) {
                        bLoop = false;
                    } else {
                        // TODO invalid header
                    }
                    break;
                case HeaderLine.HLT_RAW:
                    System.out.println("Epic fail!");
                    bValidHttpResponse = false;
                    bLoop = false;
                    break;
                }
            } else {
                System.out.println("Epic fail!");
                bValidHttpResponse = false;
                bLoop = false;
            }
        }
        if (bValidHttpResponse) {
        }
        long remaining = payloadLength - in.record.size();
        /*
        boolean firstLineMatched = false;
        boolean invalidProtocolResponse = false;

        //mark the current position. An exception is thrown if the input
        //stream does not support mark operation
        in.mark(4096);
        while (true) {
            if (remaining == 0L) {
                break;
            }
            LineToken token = readLine(in);
            remaining -= token.consumed;
            String line = token.line;
            if (token.missingCr) {
                linesWithoutCrEnding++;
            }
            if (token.missingLf) {
                linesWithoutLfEnding++;
            }
            if (!firstLineMatched && (line.length() != 0)) {
                if (!isHttpStatusLineValid(line)){
                    this.addWarning("Invalid HTTP response header: " + line);
                    invalidProtocolResponse = true;
                    break;
                }
                firstLineMatched = true;
            }
            if (line.length() == 0) {
                break;
            }
            if (line.toUpperCase().startsWith(CONTENT_TYPE)) {
                this.contentType = line.substring(line.indexOf(':') + 1).trim();
            }
            // Temporary hack to save header lines.
            int idx = line.indexOf(':');
            HeaderLine headerLine = new HeaderLine();
            if (idx != -1) {
                headerLine.name = line.substring(0, idx);
                headerLine.value = line.substring(idx + 1).trim();
                // Uses a map for fast lookup of single header.
                headerMap.put(headerLine.name.toLowerCase(), headerLine);
            }
            else {
                headerLine.line = line;
            }
            // Uses a list because there can be multiple occurrences.
            headerList.add(headerLine);
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
        */
        return remaining;
    }

    /**
     * Checks a Http Response Status-Line for validity.
     * @param statusLine the Status-Line of the HTTP Response
     * @return true/false based on whether the Status-Line is valid or not.
     */
    protected boolean isHttpStatusLineValid(String statusLine) {
        int idx;
        int prevIdx;
        boolean isValid = (statusLine != null) && (statusLine.length() > 0);
        if (isValid) {
            idx = statusLine.indexOf(' ');
            if (idx > 0) {
                protocolVersion = statusLine.substring(0, idx);
                if (!protocolVersion.startsWith(HTTP)) {
                    isValid = false;
                }
            } else {
                isValid = false;
            }
            if (isValid) {
                prevIdx = ++idx;
                idx = statusLine.indexOf(' ', idx);
                if (idx == -1) {
                    idx = statusLine.length();
                }
                if (idx > prevIdx) {
                    resultCodeStr = statusLine.substring(prevIdx, idx);
                    try {
                        resultCode = Integer.parseInt(resultCodeStr);
                        if (resultCode < 100 || resultCode > 999) {
                            isValid = false;
                        }
                    } catch(NumberFormatException e) {
                        isValid = false;
                    }
                } else {
                    isValid = false;
                }
                if (isValid) {
                    if (idx < statusLine.length()) {
                        ++idx;
                        resultMessage = statusLine.substring(idx);
                    }
                }
            }
        }
        return isValid;
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
        if (headerMap != null && field != null) {
            return headerMap.get(field.toLowerCase());
        } else {
            return null;
        }
    }

    /**
     * Result-Code string getter
     * @return the ResultCode
     */
    public String getProtocolResultCodeStr() {
        return resultCodeStr;
    }

    /**
     * Result-Code integer getter
     * @return the ResultCode
     */
    public Integer getProtocolResultCode() {
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
        return payloadLength;
    }

    /**
     * Get the raw http header as bytes.
     * @return raw http header as bytes
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
     * Get payload total length, header and payload.
     * @return payload total length, header and payload
     */
    public long getTotalLength() {
        return totalLength;
    }

    /**
     * Get the number of unavailable bytes missing due to unexpected EOF.
     * This method always returns <code>0</code> as long as the stream is open.
     * @return number of unavailable bytes missing due to unexpected EOF
     * @throws IOException if errors occur calling available method on stream
     */
    public long getUnavailable() throws IOException {
        return totalLength - in_pb.getConsumed();
    }

    /**
     * Get an <code>InputStream</code> containing both the header and the
     * payload.
     * @return <code>InputStream</code> containing both the header and the
     * payload.
     */
    public InputStream getInputStreamComplete() {
        return in_complete;
    }

    /**
     * Get an <code>InputStream</code> containing only the payload.
     * @return <code>InputStream</code> containing only the payload.
     */
    public InputStream getPayloadInputStream() {
        return in_payload;
    }

    /**
     * Get payload remaining length.
     * @return payload remaining length
     * @throws IOException if errors occur calling available method on stream
     */
    public long getRemaining() throws IOException {
        return totalLength - in_pb.getConsumed();
    }

    /**
     * Check to see if the http response has been closed.
     * @return boolean indicating whether this http response is closed or not
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

}
