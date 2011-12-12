package dk.netarkivet.common;

import java.io.IOException;
import java.net.InetAddress;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test IPAddressParser class with various legal and illegal parameters.
 *
 * @author nicl
 */
@RunWith(JUnit4.class)
public class TestParams {

	@Test
	public void test() throws IOException {
		/*
		 * IpAddressParser.
		 */

		InetAddress ia;

		ia = IPAddressParser.getAddress(null);
		Assert.assertNull(ia);

		ia = IPAddressParser.getAddress("fail");
		Assert.assertNull(ia);

		ia = IPAddressParser.getAddress("0.0.0");
		Assert.assertNull(ia);

		ia = IPAddressParser.getAddress("0.0.0.0.0");
		Assert.assertNull(ia);

		ia = IPAddressParser.getAddress("a.b.c.d");
		Assert.assertNull(ia);

		ia = IPAddressParser.getAddress("192.168.1.1");
		Assert.assertNotNull(ia);

		ia = IPAddressParser.getAddress("dead::beef:cafe:f800:0000");
		Assert.assertNotNull(ia);
	}

}
