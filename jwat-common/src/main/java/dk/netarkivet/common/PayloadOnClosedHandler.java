package dk.netarkivet.common;

import java.io.IOException;

public interface PayloadOnClosedHandler {

	public void payloadClosed() throws IOException;

}
