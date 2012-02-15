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
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * The standard <code>DigestInputStream</code> does not update the digest
 * when performing a skip operation. Since this is completely useless in
 * most situations, the following implementation remedies this.
 *
 * @author nicl
 */
public class DigestInputStreamNoSkip extends DigestInputStream {

    /** Buffer size to use when read skipping. */
    public static final int SKIP_READ_BUFFER_SIZE = 1024;

    /** Read buffer used by the skip method. */
    protected byte[] skip_read_buffer = new byte[SKIP_READ_BUFFER_SIZE];

    /**
     * Construct a <code>DigestInputStream</code> with the skip method
     * overridden.
     * @param stream input stream to digest
     * @param digest digest implementation to use
     */
    public DigestInputStreamNoSkip(InputStream stream, MessageDigest digest) {
        super(stream, digest);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long skip(long n) throws IOException {
        long remaining = n;
        long skipped = 0;
        long readLast = 0;
        while (remaining > 0 && readLast != -1) {
            remaining -= readLast;
            skipped += readLast;
            readLast = read(skip_read_buffer, 0, (int) Math.min(remaining,
                                                      SKIP_READ_BUFFER_SIZE));
        }
        return skipped;
    }

}
