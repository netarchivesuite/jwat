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
    public void test() throws IOException {
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
