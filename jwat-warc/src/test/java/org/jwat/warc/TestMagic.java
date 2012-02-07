package org.jwat.warc;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.jwat.common.ByteCountingPushBackInputStream;

@RunWith(JUnit4.class)
public class TestMagic {

    @Test
    public void test_magicbytes() {
        byte[] bytes;
        ByteCountingPushBackInputStream pbin;
        try {
            bytes = WarcConstants.WARC_MAGIC_HEADER.getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

            bytes = "WARC/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

            bytes = WarcConstants.WARC_MAGIC_HEADER.toLowerCase().getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

            bytes = "warc/1.0".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

            bytes = "WARC".getBytes();
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(WarcReaderFactory.isWarcFile(pbin));
            pbin.close();

        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

}
