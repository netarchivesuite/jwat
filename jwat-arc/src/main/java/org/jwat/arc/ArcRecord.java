package org.jwat.arc;

import java.io.IOException;
import java.util.Collection;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.HttpResponse;
import org.jwat.common.Payload;

/**
 * This class represents a parsed ARC record header including possible
 * validation and format warnings/errors encountered in the process.
 * This class also contains the specific ARC record parser which is
 * intended to be called by the <code>ARCReader</code>.
 * The payload of the ARC record is accessible through a wrapped payload
 * object.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcRecord extends ArcRecordBase {

    /** Pushback size used in payload. */
    public static final int PAYLOAD_PUSHBACK_SIZE = 8192;

    /** Buffer size used in toString(). */
    public static final int TOSTRING_BUFFER_SIZE = 256;

    /**
     * Protected constructor to force instantiation of record header
     * from stream.
     */
    protected ArcRecord() {
    }

    /**
     * Creates new <code>ArcRecord</code> based on data read from input
     * stream.
     * @param in <code>InputStream</code> used to read record header
     * @param versionBlock ARC file <code>VersionBlock</code>
     * @param reader <code>ArcReader</code> used, with access to user defined
     * options
     * @return an <code>ArcRecord</code> or null if none was found.
     * @throws IOException io exception while parsing arc record
     */
    public static ArcRecord parseArcRecord(ByteCountingPushBackInputStream in,
                              ArcVersionBlock versionBlock, ArcReader reader)
                                                          throws IOException {
        ArcRecord ar = new ArcRecord();
        ar.versionBlock = versionBlock;
        ar.version = versionBlock.version;

        // Read record line.
        // Looping past empty lines.
        ar.startOffset = in.getConsumed();
        String recordLine = in.readLine();
        while ((recordLine != null) && (recordLine.length() == 0)) {
            ar.startOffset = in.getConsumed();
            recordLine = in.readLine();
        }
        if (recordLine != null) {
            ar.parseRecord(recordLine);
        } else {
            // EOF
            ar = null;
        }
        if (ar != null) {
            ar.processPayload(in, reader);
        }
        return ar;
    }

    @Override
    protected void processPayload(ByteCountingPushBackInputStream in,
                                        ArcReader reader) throws IOException {
        payload = null;
        // Digest currently not supported by ARC reader.
        if (recLength != null && recLength > 0L) {
            String digestAlgorithm = null;
            if (reader.bBlockDigest) {
                digestAlgorithm = reader.blockDigestAlgorithm;
            }
            payload = Payload.processPayload(in, recLength.longValue(),
                                  PAYLOAD_PUSHBACK_SIZE, digestAlgorithm);
            payload.setOnClosedHandler(this);
            if (HttpResponse.isSupported(protocol)
                            && !ArcConstants.CONTENT_TYPE_NO_TYPE.equals(
                                    recContentType)) {
                digestAlgorithm = null;
                if (reader.bPayloadDigest) {
                    digestAlgorithm = reader.payloadDigestAlgorithm;
                }
                httpResponse = HttpResponse.processPayload(
                            payload.getInputStream(), recLength.longValue(),
                            digestAlgorithm);
                if (httpResponse != null) {
                    payload.setHttpResponse(httpResponse);
                }
            }
        } else if (HttpResponse.isSupported(protocol)
                            && !ArcConstants.CONTENT_TYPE_NO_TYPE.equals(
                                    recContentType)) {
            addValidationError(ArcErrorType.INVALID, ARC_FILE,
                    "Expected payload not found in the record block");
        }
        return;
    }

    /**
     * Checks if the ARC record payload has warnings.
     * @return true/false based on whether the ARC record has warnings or not
     */
    @Override
    public boolean hasWarnings() {
        return ((httpResponse != null) && (httpResponse.hasWarnings()));
    }

    /**
     * Returns the ARC record payload warnings.
     * @return validation errors list/
     */
    @Override
    public Collection<String> getWarnings() {
        return (hasWarnings()) ? httpResponse.getWarnings() : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(TOSTRING_BUFFER_SIZE);
        builder.append("\nArcRecord [");
        builder.append(super.toString());
        builder.append(']');
        if (httpResponse != null) {
            builder.append(httpResponse.toString());
        }
        return builder.toString();
    }

}
