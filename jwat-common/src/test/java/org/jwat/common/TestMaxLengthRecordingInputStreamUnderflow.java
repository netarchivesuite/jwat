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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestMaxLengthRecordingInputStreamUnderflow {

    @Test
    public void test_inputstream_maxlengthrecording_underflow() {
        SecureRandom random = new SecureRandom();

        byte[] srcArr;
        MaxLengthRecordingInputStream in;
        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        long remaining;
        byte[] tmpBuf = new byte[ 16 ];
        int read;
        int mod;

        srcArr = new byte[ 8192 ];
        random.nextBytes( srcArr );

        try {
            /*
             * Read.
             */
            in = new MaxLengthRecordingInputStream( new ByteArrayInputStream( srcArr ), 16384 );

            dstOut.reset();

            remaining = srcArr.length;
            read = 0;
            mod = 2;
            while ( read != -1 ) {
                switch ( mod ) {
                case 0:
                    dstOut.write( read );
                    --remaining;
                    break;
                case 1:
                case 2:
                    dstOut.write( tmpBuf, 0, read );
                    remaining -= read;
                    break;
                }

                mod = (mod + 1) % 3;

                switch ( mod ) {
                case 0:
                    read = in.read();
                    break;
                case 1:
                    read = in.read( tmpBuf );
                    break;
                case 2:
                    read = random.nextInt( 15 ) + 1;
                    read = in.read( tmpBuf, 0, read );
                    break;
                }
            }

            Assert.assertEquals( 0, remaining );
            Assert.assertEquals( 8192, in.available );

            read = in.read();
            Assert.assertEquals(-1, read);

            read = in.read(tmpBuf);
            Assert.assertEquals(-1, read);

            long skipped = in.skip(16);
            Assert.assertEquals(0, skipped);

            dstArr = dstOut.toByteArray();
            Assert.assertEquals( srcArr.length, dstArr.length );
            Assert.assertArrayEquals( srcArr, dstArr );

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception!");
        }
    }

}
