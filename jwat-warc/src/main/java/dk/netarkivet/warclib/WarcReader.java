/*
 * Created on 03/11/2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dk.netarkivet.warclib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public abstract class WarcReader {

	/**
     * Close current record resources and inputstream. 
     */
	public abstract void close();

    /**
     * Parses and gets the next ARC record.
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
	public abstract WarcRecord nextRecord();

    /**
     * Parses and gets the next ARC record.
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
	public abstract WarcRecord nextRecord(InputStream in);

	public abstract Iterator<WarcRecord> iterator();

}
