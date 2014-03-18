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
package org.jwat.warc;

import java.io.OutputStream;

/**
 * Factory used for creating <code>WarcWriter</code> instances.
 * Factory methods are available for creating <code>WarcWriter</code>
 * instances for writing either compressed or uncompressed records.
 * Use of buffered methods and/or buffering speeds up the writer considerably.
 *
 * @author nicl
 */
public class WarcWriterFactory {

    /**
     * Private constructor to enforce factory methods.
     */
    protected WarcWriterFactory() {
    }

    /**
     * Creates a new unbuffered <code>WarcWriter</code> from an
     * <code>OutputStream</code>.
     * Returns a compressing or non compressing writer according to the arguments.
     * @param out output stream to write to
     * @param compressed compression switch
     * @return unbuffered <code>WarcWriter</code>
     */
    public static WarcWriter getWriter(OutputStream out, boolean compressed) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        if (compressed) {
            return new WarcWriterCompressed(out);
        } else {
            return new WarcWriterUncompressed(out);
        }
    }

    /**
     * Creates a new buffered <code>WarcWriter</code> from an
     * <code>OutputStream</code>.
     * Returns a compressing or non compressing writer according to the arguments.
     * @param out output stream to write to
     * @param buffer_size buffer size to use
     * @param compressed compression switch
     * @return buffered <code>WarcWriter</code>
     */
    public static WarcWriter getWriter(OutputStream out, int buffer_size, boolean compressed) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' parameter is less than or equal to zero!");
        }
        if (compressed) {
            return new WarcWriterCompressed(out, buffer_size);
        } else {
            return new WarcWriterUncompressed(out, buffer_size);
        }
    }

    /**
     * Creates a new unbuffered non compressing <code>WarcWriter</code> from an
     * <code>OutputStream</code>.
     * @param out output stream to write to
     * @return unbuffered non compressing <code>WarcWriter</code>
     */
    public static WarcWriter getWriterUncompressed(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        return new WarcWriterUncompressed(out);
    }

    /**
     * Creates a new buffered non compressing <code>WarcWriter</code> from an
     * <code>OutputStream</code>.
     * @param out output stream to write to
     * @param buffer_size buffer size to use
     * @return buffered non compressing <code>WarcWriter</code>
     */
    public static WarcWriter getWriterUncompressed(OutputStream out, int buffer_size) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' parameter is less than or equal to zero!");
        }
        return new WarcWriterUncompressed(out, buffer_size);
    }

    /**
     * Creates a new unbuffered compressing <code>WarcWriter</code> from an
     * <code>OutputStream</code>.
     * @param out output stream to write to
     * @return unbuffered compressing <code>WarcWriter</code>
     */
    public static WarcWriter getWriterCompressed(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        return new WarcWriterCompressed(out);
    }

    /**
     * Creates a new buffered compressing <code>WarcWriter</code> from an
     * <code>OutputStream</code>.
     * @param out output stream to write to
     * @param buffer_size buffer size to use
     * @return buffered compressing <code>WarcWriter</code>
     */
    public static WarcWriter getWriterCompressed(OutputStream out, int buffer_size) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
        if (buffer_size <= 0) {
            throw new IllegalArgumentException(
                    "The 'buffer_size' parameter is less than or equal to zero!");
        }
        return new WarcWriterCompressed(out, buffer_size);
    }

}
