package org.jwat.gzip;

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
            bytes = GzipConstants.GZIP_MAGIC_HEADER;
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(GzipInputStream.isGziped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x1f, (byte)0x8b, (byte)0x2f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertTrue(GzipInputStream.isGziped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x8b, (byte)0x1f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipInputStream.isGziped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x1f};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipInputStream.isGziped(pbin));
            pbin.close();

            bytes = new byte[] {(byte)0x8b};
            pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
            Assert.assertFalse(GzipInputStream.isGziped(pbin));
            pbin.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Exception not expected!");
        }
    }

}
