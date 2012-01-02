package org.jwat.warc;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;

/**
 * Base class for WARC writer implementations.
 *
 * @author nicl
 */
public abstract class WarcWriter {

    /** WARC <code>DateFormat</code> as specified by the WARC ISO standard. */
    protected DateFormat warcDateFormat = WarcDateParser.getWarcDateFormat();

    /** Block Digesting enabled/disabled. */
    protected boolean bDigestBlock = false;

    /**
     * Is this writer producing compressed output.
     * @return boolean indicating whether compressed output is produced
     */
    public abstract boolean isCompressed();

    /**
     * Is this writer set to block digest payload.
     * @return boolean indicating payload block digesting
     */
    public boolean digestBlock() {
        return bDigestBlock;
    }

    /**
     * Set the writers payload block digest mode
     * @param enabled boolean indicating digest on/off
     */
    public void setDigestBlock(boolean enabled) {
        bDigestBlock = enabled;
    }

    /**
     * Close WARC writer and free its resources.
     */
    public abstract void close();

    /**
     * Write a WARC header to the WARC output stream.
     * @param record WARC record to output
     * @throws IOException if an exception occurs while writing header data
     */
    public abstract void write(WarcRecord record) throws IOException;

    /**
     *
     * @param in input stream containing payload data
     * @param length payload length
     * @return written length of payload data
     * @throws IOException if an exception occurs while writing payload data
     */
    public abstract long transfer(InputStream in, long length) throws IOException;

    /**
     * Close the WARC record.
     * @throws IOException if an exception occurs while closing the record
     */
    public abstract void closeRecord() throws IOException;

}
