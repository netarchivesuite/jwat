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

public class WarcWriterFactory {

    /**
     * Private constructor to enforce factory method.
     */
    protected WarcWriterFactory() {
    }

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

	public static WarcWriter getWriterUncompressed(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
		return new WarcWriterUncompressed(out);
	}

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

	public static WarcWriter getWriterCompressed(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException(
                    "The 'out' parameter is null!");
        }
	    return new WarcWriterCompressed(out);
	}

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
