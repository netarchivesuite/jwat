package org.jwat.arc;

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
        	bytes = ArcConstants.ARC_MAGIC_HEADER.getBytes();
        	pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
			Assert.assertTrue(ArcReaderFactory.isArcFile(pbin));
	    	pbin.close();

        	bytes = "filedesc://url".getBytes();
        	pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
			Assert.assertTrue(ArcReaderFactory.isArcFile(pbin));
	    	pbin.close();

        	bytes = ArcConstants.ARC_MAGIC_HEADER.toUpperCase().getBytes();
        	pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
			Assert.assertFalse(ArcReaderFactory.isArcFile(pbin));
	    	pbin.close();

        	bytes = "FILEDESC://url".getBytes();
        	pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
			Assert.assertFalse(ArcReaderFactory.isArcFile(pbin));
	    	pbin.close();

        	bytes = "filedesc".getBytes();
        	pbin = new ByteCountingPushBackInputStream(new ByteArrayInputStream(bytes), 16);
			Assert.assertFalse(ArcReaderFactory.isArcFile(pbin));
	    	pbin.close();

    	} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Exception not expected!");
		}
    }

}
