package org.jwat.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.HttpResponse;

@RunWith(Parameterized.class)
public class TestHttpResponse {

	private int min;
	private int max;
	private int runs;
	private String digestAlgorithm;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 256, 1, null},
				{1, 256, 1, "sha1"}
		});
	}

	public TestHttpResponse(int min, int max, int runs, String digestAlgorithm) {
		this.min = min;
		this.max = max;
		this.runs = runs;
		this.digestAlgorithm = digestAlgorithm;
	}

	public static byte[] headerArr;

	static {
		String header = "";
		header += "HTTP/1.1 200 OK\r\n";
		header += "Date: Wed, 30 Apr 2008 20:53:30 GMT\r\n";
		header += "Server: Apache/2.0.54 (Ubuntu) PHP/5.0.5-2ubuntu1.4 mod_ssl/2.0.54 OpenSSL/0.9.7g\r\n";
		header += "X-Powered-By: PHP/5.0.5-2ubuntu1.4\r\n";
		header += "Connection: close\r\n";
		header += "Content-Type: text/html; charset=UTF-8\r\n";
		header += "\r\n";
		headerArr = header.getBytes();
	}

	@Test
	public void test() {
		SecureRandom random = new SecureRandom();

		byte[] payloadArr;
		ByteArrayOutputStream srcOut = new ByteArrayOutputStream();
		byte[] srcArr = new byte[ 0 ];
		ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
		byte[] dstArr;

		ByteCountingPushBackInputStream pbin;
		HttpResponse httpResponse;

		Assert.assertTrue( HttpResponse.isSupported( "http" ) );
		Assert.assertTrue( HttpResponse.isSupported( "https" ) );
		Assert.assertTrue( HttpResponse.isSupported( "Http" ) );
		Assert.assertTrue( HttpResponse.isSupported( "Https" ) );

		Assert.assertFalse( HttpResponse.isSupported( "httpss" ) );
		Assert.assertFalse( HttpResponse.isSupported( "ftp" ) );
		Assert.assertFalse( HttpResponse.isSupported( "ftps" ) );

		try {
			httpResponse = HttpResponse.processPayload( null, 0, null );
			Assert.fail( "Exception expected!" );
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
		}

		try {
			pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
			httpResponse = HttpResponse.processPayload( pbin, -1, null );
			Assert.fail( "Exception expected!" );
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
		}

		try {
			pbin = new  ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
			httpResponse = HttpResponse.processPayload( pbin, 0, "shit1" );
			Assert.assertNull( httpResponse.getMessageDigest() );
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		InputStream in;
		long remaining;
		byte[] tmpBuf = new byte[ 256 ];
		int read;

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance( "SHA1" );
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		for ( int r=0; r<runs; ++r) {
			for ( int n=min; n<max; ++n ) {
				payloadArr = new byte[ n ];
				random.nextBytes( payloadArr );

				try {
					srcOut.reset();
					srcOut.write( headerArr );
					srcOut.write( payloadArr );
					srcArr = srcOut.toByteArray();
					/*
					 * HttpResponse Payload.
					 */
					pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
					httpResponse = HttpResponse.processPayload( pbin, srcArr.length, digestAlgorithm );

					in = httpResponse.getPayloadInputStream();
					Assert.assertEquals( srcArr.length, httpResponse.getTotalLength() );

					dstOut.reset();

					remaining = httpResponse.getTotalLength() - httpResponse.getHeader().length;
					read = 0;
					while ( remaining > 0 && read != -1 ) {
						dstOut.write(tmpBuf, 0, read);
						remaining -= read;

						// This wont work with buffered streams...
						//Assert.assertEquals( remaining, payload.getUnavailable() );
						//Assert.assertEquals( remaining, payload.getRemaining() );

						read = random.nextInt( 15 ) + 1;
						read = in.read(tmpBuf, 0, read);
					}

					Assert.assertEquals( 0, remaining );
					Assert.assertEquals( 0, httpResponse.getUnavailable() );
					Assert.assertEquals( 0, httpResponse.getRemaining() );

					dstArr = dstOut.toByteArray();
					Assert.assertEquals( payloadArr.length, dstArr.length );
					Assert.assertArrayEquals( payloadArr, dstArr );

					in.close();

					Assert.assertEquals( "HTTP/1.1", httpResponse.getProtocolVersion() );
					Assert.assertEquals( "200", httpResponse.getProtocolResultCode() );
					Assert.assertEquals( "text/html; charset=UTF-8", httpResponse.getProtocolContentType() );
					Assert.assertEquals( n, httpResponse.getPayloadLength() );

					httpResponse.close();

					Assert.assertNotNull( httpResponse.toString() );
					/*
					 * HttpResponse Payload Digest.
					 */
					if ( digestAlgorithm != null ) {
						Assert.assertNotNull( httpResponse.getMessageDigest() );

						md.reset();
						byte[] digest1 = md.digest( payloadArr );

						byte[] digest2 = httpResponse.getMessageDigest().digest();

						Assert.assertArrayEquals( digest1, digest2 );
					}
					else {
						Assert.assertNull( httpResponse.getMessageDigest() );
					}
					/*
					 * HttpResponse Complete
					 */
					pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
					httpResponse = HttpResponse.processPayload( pbin, srcArr.length, digestAlgorithm );

					in = httpResponse.getInputStreamComplete();
					Assert.assertEquals( srcArr.length, httpResponse.getTotalLength() );

					dstOut.reset();

					remaining = httpResponse.getTotalLength();
					read = 0;
					while ( remaining > 0 && read != -1 ) {
						dstOut.write(tmpBuf, 0, read);
						remaining -= read;

						// This wont work with buffered streams...
						//Assert.assertEquals( remaining, payload.getUnavailable() );
						//Assert.assertEquals( remaining, payload.getRemaining() );

						read = random.nextInt( 15 ) + 1;
						read = in.read(tmpBuf, 0, read);
					}

					Assert.assertEquals( 0, remaining );
					Assert.assertEquals( 0, httpResponse.getUnavailable() );
					Assert.assertEquals( 0, httpResponse.getRemaining() );

					dstArr = dstOut.toByteArray();
					Assert.assertEquals( srcArr.length, dstArr.length );
					Assert.assertArrayEquals( srcArr, dstArr );

					in.close();

					Assert.assertEquals( "HTTP/1.1", httpResponse.getProtocolVersion() );
					Assert.assertEquals( "200", httpResponse.getProtocolResultCode() );
					Assert.assertEquals( "text/html; charset=UTF-8", httpResponse.getProtocolContentType() );
					Assert.assertEquals( n, httpResponse.getPayloadLength() );

					httpResponse.close();

					Assert.assertNotNull( httpResponse.toString() );
					/*
					 * HttpResponse Payload Digest.
					 */
					if ( digestAlgorithm != null ) {
						Assert.assertNotNull( httpResponse.getMessageDigest() );

						md.reset();
						byte[] digest1 = md.digest( payloadArr );

						byte[] digest2 = httpResponse.getMessageDigest().digest();

						Assert.assertArrayEquals( digest1, digest2 );
					}
					else {
						Assert.assertNull( httpResponse.getMessageDigest() );
					}
				}
				catch (IOException e) {
					Assert.fail( "Exception not expected!" );
					e.printStackTrace();
				}
			}
		}
	}

}
