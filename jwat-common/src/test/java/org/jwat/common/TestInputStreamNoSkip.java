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
import java.io.FilterInputStream;
import java.io.IOException;
import java.security.SecureRandom;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestInputStreamNoSkip {

    @Test
    public void test_inputstreamnoskip() {
        SecureRandom random = new SecureRandom();

        byte[] srcArr = new byte[65336];
        random.nextBytes(srcArr);

        final ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        MaxLengthRecordingInputStream in = new MaxLengthRecordingInputStream( new ByteArrayInputStream( srcArr ), srcArr.length );

        FilterInputStream fin = new FilterInputStream(in) {
            @Override
            public int read() throws IOException {
                int b = super.read();
                if (b != -1) {
                    dstOut.write(b);
                }
                return b;
            }
            @Override
            public int read(byte[] b) throws IOException {
                int read = super.read(b);
                if (read > 0) {
                    dstOut.write(b, 0, read);
                }
                return read;
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int read = super.read(b, off, len);
                if (read > 0) {
                    dstOut.write(b, off, read);
                }
                return read;
            }
            @Override
            public long skip(long n) throws IOException {
                throw new IOException("Method call unexpected!");
            }
        };

        InputStreamNoSkip isns = new InputStreamNoSkip(fin);

        long remaining;
        byte[] tmpBuf = new byte[ 16 ];
        long skipped;

        try {
            remaining = srcArr.length;
            skipped = 0;
            while ( remaining > 0 && skipped != -1 ) {
                remaining -= skipped;
                Assert.assertEquals( remaining, in.available );

                skipped = random.nextInt( 15 ) + 1;
                skipped = isns.skip( skipped );
            }

            Assert.assertEquals( 0, remaining );
            Assert.assertEquals( 0, in.available );

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
