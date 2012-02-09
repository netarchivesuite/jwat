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
import java.io.InputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingInputStream;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.CharCountingStringReader;
import org.jwat.common.DigestInputStreamNoSkip;
import org.jwat.common.FixedLengthInputStream;
import org.jwat.common.MaxLengthRecordingInputStream;

@RunWith(JUnit4.class)
public class TestSkip {

    @Test
    public void test_inputstream_skipping() throws IOException {
        InputStream in;
        StringReader sr;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance( "sha1" );
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write( '!' );
        byte[] srcArr = out.toByteArray();

        String srcStr = "!";

        in = new ByteCountingInputStream( new ByteArrayInputStream( srcArr ) );
        Assert.assertEquals( 1, in.skip( 10 ) );
        Assert.assertEquals( 0, in.skip( 10 ) );

        in = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 1 );
        Assert.assertEquals( 1, in.skip( 10 ) );
        Assert.assertEquals( 0, in.skip( 10 ) );

        sr = new CharCountingStringReader( srcStr );
        Assert.assertEquals( 1, sr.skip( 10 ) );
        Assert.assertEquals( 0, sr.skip( 10 ) );

        in = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), md );
        Assert.assertEquals( 1, in.skip( 10 ) );
        Assert.assertEquals( 0, in.skip( 10 ) );

        in = new FixedLengthInputStream( new ByteArrayInputStream( srcArr ), srcArr.length );
        Assert.assertEquals( 1, in.skip( 10 ) );
        Assert.assertEquals( 0, in.skip( 10 ) );

        in = new MaxLengthRecordingInputStream( new ByteArrayInputStream( srcArr ), srcArr.length );
        Assert.assertEquals( 1, in.skip( 10 ) );
        Assert.assertEquals( 0, in.skip( 10 ) );
    }

}
