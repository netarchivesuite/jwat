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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * Simple class to allow the use of an in-memory buffer as an alternate input or output stream.
 * 
 * @author nicl
 */
public class ByteArrayIOStream {

	/** Default buffer size if none specified. */
	public static final int DEFAULT_BUFFER_SIZE = 10*1024*1024;

	/** Buffer lock. */
	protected Semaphore lock = new Semaphore(1);

	/** Internal byte array. */
	protected byte[] bytes;

	/** Internal <code>ByteBuffer</code> wrapper of internal byte array. */
    protected ByteBuffer byteBuffer;

    /** Current <code>ByteBuffer</code> limit. */
    protected int limit = 0;

    /**
     * Construct object with default buffer size.
     */
    public ByteArrayIOStream() {
    	this(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Construct object with supplied buffer size.
     * @param bufferSize buffer size to use
     */
    public ByteArrayIOStream(int bufferSize) {
    	bytes = new byte[bufferSize];
    	byteBuffer = ByteBuffer.wrap(bytes);
    }

    /**
     * Return <code>OutputStream</code> and lock this buffer
     * @return <code>OutputStream</code> wrapper of this buffer
     */
    public OutputStream getOutputStream() {
    	if (!lock.tryAcquire()) {
    		throw new IllegalStateException();
    	}
		byteBuffer.clear();
		limit = 0;
    	return new OutputStreamImpl(this);
    }

    /**
     * Return internal byte array.
     * @return internal byte array
     */
    public byte[] getBytes() {
    	return bytes;
    }

    /**
     * Return allocated length of internal buffer.
     * @return allocated length of internal buffer
     */
    public int getLength() {
    	return bytes.length;
    }

    /**
     * Return internal <code>ByteBuffer</code> limit.
     * @return internal <code>ByteBuffer</code> limit
     */
    public int getLimit() {
    	return limit;
    }

    /**
     * Return internal <code>ByteBuffer</code>.
     * @return internal <code>ByteBuffer</code>
     */
    public ByteBuffer getBuffer() {
    	ByteBuffer buffer = ByteBuffer.wrap(bytes);
    	buffer.position(0);
    	buffer.limit(limit);
    	return buffer;
    }

    /**
     * Return <code>InputStream</code> and lock this buffer
     * @return <code>InputStream</code> wrapper of this buffer
     */
    public InputStream getInputStream() {
    	if (!lock.tryAcquire()) {
    		throw new IllegalStateException();
    	}
    	byteBuffer.clear();
    	byteBuffer.limit(limit);
    	return new InputStreamImpl(this);
    }

    /**
     * Release lock, called when input/output stream is closed.
     */
    protected void release() {
    	lock = null;
    	byteBuffer = null;
    	bytes = null;
    	limit = 0;
    }

    /**
     * Byte array OutputStream.
     */
    public static class OutputStreamImpl extends OutputStream {

    	/** Owning <code>ByteArrayIOStream</code>. */
		protected ByteArrayIOStream baios;
    	/** <code>ByteArrayIOStream</code>'s <code>ByteBuffer</code>. */
		protected ByteBuffer byteBuffer;

		/**
		 * Construct <code>OutputStream</code> object.
		 * @param baios owning <code>ByteArrayIOStream</code>
		 */
    	protected OutputStreamImpl(ByteArrayIOStream baios) {
    		this.baios = baios;
    		this.byteBuffer = baios.byteBuffer;
    	}

    	@Override
    	public void close() {
    		if (baios != null) {
    			baios.limit = baios.byteBuffer.position();
    			baios.lock.release();
    			baios = null;
    			byteBuffer = null;
    		}
    	}

    	@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			byteBuffer.put(b, off, len);
		}

		@Override
		public void write(byte[] b) throws IOException {
			byteBuffer.put(b);
		}

    	@Override
		public void write(int b) throws IOException {
    		byteBuffer.put((byte)b);
		}

    }

    /**
     * Byte array InputStream.
     */
    public static class InputStreamImpl extends InputStream {

    	/** Owning <code>ByteArrayIOStream</code>. */
    	protected ByteArrayIOStream baios;
    	/** <code>ByteArrayIOStream</code>'s <code>ByteBuffer</code>. */
		protected ByteBuffer byteBuffer;

		/**
		 * Construct <code>InputStream</code> object.
		 * @param baios owning <code>ByteArrayIOStream</code>
		 */
    	protected InputStreamImpl(ByteArrayIOStream baios) {
    		this.baios = baios;
    		this.byteBuffer = baios.byteBuffer;
    	}

    	@Override
    	public void close() {
    		if (baios != null) {
    			baios.lock.release();
    			baios = null;
    			byteBuffer = null;
    		}
    	}

		@Override
		public int available() throws IOException {
			return byteBuffer.limit() - byteBuffer.position();
		}

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public synchronized void mark(int readlimit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public synchronized void reset() throws IOException {
			throw new UnsupportedOperationException();
		}

    	@Override
		public int read() throws IOException {
    		if (byteBuffer.remaining() > 0) {
    			return byteBuffer.get();
    		} else {
    			return -1;
    		}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (len == 0) {
				return 0;
			}
			int remaining = byteBuffer.remaining();
			if (len > remaining) {
				len = remaining;
			}
			if (len > 0) {
				byteBuffer.get(b, off, len);
				return len;
			} else {
				return -1;
			}
		}

		@Override
		public int read(byte[] b) throws IOException {
			int len = b.length;
			if (len == 0) {
				return 0;
			}
			int remaining = byteBuffer.remaining();
			if (len > remaining) {
				len = remaining;
			}
			if (len > 0) {
				byteBuffer.get(b, 0, len);
				return len;
			} else {
				return -1;
			}
		}

		@Override
		public long skip(long n) throws IOException {
			int remaining = byteBuffer.remaining();
			if (n > remaining) {
				n = remaining;
			}
			if (n > 0) {
				byteBuffer.position(byteBuffer.position() + (int)n);
			}
			return n;
		}

    }

}
