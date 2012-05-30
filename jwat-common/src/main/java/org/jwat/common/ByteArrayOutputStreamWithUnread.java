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

import java.io.ByteArrayOutputStream;

/**
 * A ByteArrayOutputStream with unread capability.
 *
 * @author nicl
 */
public class ByteArrayOutputStreamWithUnread extends ByteArrayOutputStream {

    /**
     * Construct an object without specifying an buffer size.
     */
    public ByteArrayOutputStreamWithUnread() {
        super();
    }

    /**
     * Construct an object with the specifying buffer size.
     * @param size buffer size
     */
    public ByteArrayOutputStreamWithUnread(int size)  {
        super(size);
    }

    /**
     * Unread a byte by removing it from the buffer.
     * @param b the int value whose low-order byte is to be unread.
     */
    public void unread(int b) {
        --count;
        if (count < 0) {
            System.out.println(new String(buf));
            System.out.println(1);
            System.out.println(count);
            throw new IllegalStateException("Can not unread more that buffered!");
        }
    }

    /**
     * Unread an array of bytes by removing them from the  buffer.
     * @param b the byte array to push back
     */
    public void unread(byte[] b) {
        count -= b.length;
        if (count < 0) {
            System.out.println(new String(buf));
            System.out.println(b.length);
            System.out.println(count);
            throw new IllegalStateException("Can not unread more that buffered!");
        }
    }

    /**
     * Unread a portion of an array of bytes by removing them from the buffer.
     * @param b the byte array to unread.
     * @param off the start offset of the data.
     * @param len the number of bytes to unread.
     */
    public void unread(byte[] b, int off, int len) {
        count -= len;
        if (count < 0) {
            System.out.println(new String(buf));
            System.out.println(len);
            System.out.println(count);
            throw new IllegalStateException("Can not unread more that buffered!");
        }
    }

}
