package dk.netarkivet.arc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import dk.netarkivet.common.ByteCountingPushBackInputStream;
import dk.netarkivet.gzip.GzipEntry;
import dk.netarkivet.gzip.GzipInputStream;

/**
 * ARC Reader used on GZip compressed files.
 *
 * @author nicl
 */
public class ArcReaderCompressed extends ArcReader {

    /** WARC file <code>InputStream</code>. */
	protected GzipInputStream in;

	/** Buffer size, if any, to use on GZip entry <code>InputStream</code>. */
	protected int bufferSize;

	ArcReaderCompressed() {
	}

	/**
	 * Construct object using supplied <code>GzipInputStream</code>.
	 * @param in <code>GzipInputStream</code>
	 */
	ArcReaderCompressed(GzipInputStream in) {
		this.in = in;
	}

	/**
	 * Construct object using supplied <code>GzipInputStream</code>.
	 */
	ArcReaderCompressed(GzipInputStream in, int buffer_size) {
		this.in = in;
		this.bufferSize = buffer_size;
	}

	@Override
	public boolean isCompressed() {
		return true;
	}

	@Override
	public void close() {
        if (arcRecord != null) {
            try {
                arcRecord.close();
            }
            catch (IOException e) { /* ignore */ }
            arcRecord = null;
        }
		if (in != null) {
			try {
				in.close();
			}
			catch (IOException e) { /* ignore */ }
			in = null;
		}
	}

    /**
     * Get the current offset in the ARC <code>GzipInputStream</code>.
     * @return offset in ARC <code>InputStream</code>
     */
    @Override
    public long getOffset() {
    	// Important: This is not precise!
    	// Use GzipEntry.getOffset() for record offset.
    	return in.getOffset();
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
        versionBlock = null;
		GzipEntry entry = in.getNextEntry();
		if (entry != null) {
			if (bufferSize > 0) {
		        versionBlock = ArcVersionBlock.parseVersionBlock(new ByteCountingPushBackInputStream(new BufferedInputStream(in.getEntryInputStream(), bufferSize), 16));
			}
			else {
		        versionBlock = ArcVersionBlock.parseVersionBlock(new ByteCountingPushBackInputStream(in.getEntryInputStream(), 16));
			}
		}
		if (versionBlock != null) {
			versionBlock.startOffset = entry.getOffset();
		}
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
        versionBlock = null;
		GzipInputStream gzin = new GzipInputStream(in);
		GzipEntry entry = gzin.getNextEntry();
		if (entry != null) {
			if (bufferSize > 0) {
		        versionBlock = ArcVersionBlock.parseVersionBlock(new ByteCountingPushBackInputStream(new BufferedInputStream(gzin.getEntryInputStream(), bufferSize), 16));
			}
			else {
		        versionBlock = ArcVersionBlock.parseVersionBlock(new ByteCountingPushBackInputStream(gzin.getEntryInputStream(), 16));
			}
		}
		if (versionBlock != null) {
			versionBlock.startOffset = -1L;
		}
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
		arcRecord = null;
		GzipEntry entry = in.getNextEntry();
		if (entry != null) {
			if (bufferSize > 0) {
		        arcRecord = ArcRecord.parseArcRecord(new ByteCountingPushBackInputStream(new BufferedInputStream(in.getEntryInputStream(), bufferSize), 16), versionBlock);
			}
			else {
		        arcRecord = ArcRecord.parseArcRecord(new ByteCountingPushBackInputStream(in.getEntryInputStream(), 16), versionBlock);
			}
		}
		if (arcRecord != null) {
	        arcRecord.startOffset = entry.getOffset();
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
		arcRecord = null;
		GzipInputStream gzin = new GzipInputStream(in);
		GzipEntry entry = gzin.getNextEntry();
		if (entry != null) {
			ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(gzin.getEntryInputStream(), 16);
	        arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock);
		}
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
		arcRecord = null;
		GzipInputStream gzin = new GzipInputStream(in);
		GzipEntry entry = gzin.getNextEntry();
		if (entry != null) {
			ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(new BufferedInputStream(gzin.getEntryInputStream(), buffer_size), 16);
	        arcRecord = ArcRecord.parseArcRecord(pbin, versionBlock);
		}
		if (arcRecord != null) {
	        arcRecord.startOffset = offset;
		}
		previousRecord = arcRecord;
        return arcRecord;
    }

}
