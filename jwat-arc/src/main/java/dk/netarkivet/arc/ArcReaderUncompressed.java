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

package dk.netarkivet.arc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.common.ByteCountingPushBackInputStream;

/**
 * ARC Reader used on uncompressed files.
 *
 * @author lbihanic, selghissassi, nicl
 */
public class ArcReaderUncompressed extends ArcReader {

    /** ARC file <code>InputStream</code>. */
    protected ByteCountingPushBackInputStream in;

	/**
	 * Construct object not associated with any input stream.
	 * The reader must be supplied an input stream for each record read.
	 * This method is for use with random access to records.
	 * @param in <code>WarcInputStream</code>
	 */
    ArcReaderUncompressed() {
    }

    /**
	 * Construct object using supplied <code>WarcInputStream</code>.
	 * This method is primarily for linear access to records.
	 * @param in <code>WarcInputStream</code>
	 */
    ArcReaderUncompressed(ByteCountingPushBackInputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("in");
        }
        this.in = in;
    }

	@Override
	public boolean isCompressed() {
		return false;
	}

    /**
     * Close current record resource(s) and input stream(s). 
     */
    @Override
    public void close() {
        if (arcRecord != null) {
            try {
                arcRecord.close();
            } catch (IOException e) { /* ignore */ }
            arcRecord = null;
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) { /* ignore */ }
            in = null;
        }
    }

    /**
     * Get the currect offset in the ARC <code>InputStream</code>.
     * @return offset in ARC <code>InputStream</code>
     */
    @Override
    public long getOffset() {
        return in.getConsumed();
    }

    /**
     * Parses and gets the version block of the ARC file.
     * @return the version block of the ARC file
     * @throws IOException io exception in reading process
     */
    @Override
    public ArcVersionBlock getVersionBlock() throws IOException {
        if (previousRecord != null) {
        	previousRecord.close();
        }
        if (in == null) {
        	throw new IllegalStateException("in");
        }
        versionBlock = ArcVersionBlock.parseVersionBlock(in);
		previousRecord = versionBlock;
        return versionBlock;
    }

    @Override
    public ArcVersionBlock getVersionBlock(InputStream in) throws IOException {
        if (previousRecord != null) {
        	previousRecord.close();
        }
        if (in == null) {
        	throw new IllegalStateException("in");
        }
        ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(in, 16);
        versionBlock = ArcVersionBlock.parseVersionBlock(pbin);
		previousRecord = versionBlock;
        return versionBlock;
    }

    /**
     * Parses and gets the next ARC record.
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    @Override
    public ArcRecord getNextRecord() throws IOException {
        if (previousRecord != null) {
        	previousRecord.close();
        }
        if (in == null) {
        	throw new IllegalStateException("in");
        }
        arcRecord = ArcRecord.parseArcRecord(in, versionBlock);
		previousRecord = arcRecord;
        return arcRecord;
    }

    /**
     * Parses and gets the next ARC record.
     * @param inExt ARC record <code>InputStream</code>
     * @param offset offset dictated by external factors
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    @Override
    public ArcRecord getNextRecordFrom(InputStream in, long offset) throws IOException {
        if (previousRecord != null) {
        	previousRecord.close();
        }
        if (in == null) {
        	throw new InvalidParameterException("in");
        }
        if (offset < 0) {
        	throw new InvalidParameterException("offset");
        }
        ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(in, 16);
        arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock);
        if (arcRecord != null) {
            arcRecord.startOffset = offset;
        }
		previousRecord = arcRecord;
        return arcRecord;
    }

    /**
     * Parses and gets the next ARC record.
     * @param inExt ARC record <code>InputStream</code>
     * @param offset offset dictated by external factors
     * @return the next ARC record
     * @throws IOException io exception in reading process
     */
    @Override
    public ArcRecord getNextRecordFrom(InputStream in, int buffer_size,
    										long offset) throws IOException {
        if (previousRecord != null) {
        	previousRecord.close();
        }
        if (in == null) {
        	throw new InvalidParameterException("in");
        }
        if (buffer_size <= 0) {
        	throw new InvalidParameterException("buffer_size");
        }
        if (offset < 0) {
        	throw new InvalidParameterException("offset");
        }
        ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(new BufferedInputStream(in, buffer_size), 16);
        arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock);
        if (arcRecord != null) {
            arcRecord.startOffset = offset;
        }
		previousRecord = arcRecord;
        return arcRecord;
    }

}
