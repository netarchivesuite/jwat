package dk.netarkivet.common;

import java.io.IOException;

/**
 * Callback handler used by the payload implementation to notify the initiating
 * caller that the payload has been closed and all payload data have been read
 * or skipped.
 *
 * @author nicl
 */
public interface PayloadOnClosedHandler {

    /**
     * Method called when the associated payload has been fully read or
     * skipped.
     * @throws IOException io exception while closing payload
     */
    void payloadClosed() throws IOException;

}
