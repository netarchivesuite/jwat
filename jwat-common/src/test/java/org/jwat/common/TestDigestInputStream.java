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
import org.jwat.common.DigestInputStreamNoSkip;

@RunWith(Parameterized.class)
public class TestDigestInputStream {

	private int min;
	private int max;
	private int runs;
	private String digestAlgorithm;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 256, 1, "sha1"}
		});
	}

	public TestDigestInputStream(int min, int max, int runs, String digestAlgorithm) {
		this.min = min;
		this.max = max;
		this.runs = runs;
		this.digestAlgorithm = digestAlgorithm;
	}

	@Test
	public void test() {
		SecureRandom random = new SecureRandom();

		byte[] srcArr = new byte[ 0 ];
		ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
		byte[] dstArr;

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance( digestAlgorithm );
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		DigestInputStreamNoSkip din;

		din = new DigestInputStreamNoSkip( null, null );
		Assert.assertNotNull( din );

		din = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), null );
		Assert.assertNotNull( din );

		din = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), md );
		Assert.assertNotNull( din );

		din = new DigestInputStreamNoSkip( null, md );
		Assert.assertNotNull( din );

		Assert.assertFalse( din.markSupported() );
		din.mark( 1 );
		try {
			din.reset();
			Assert.fail( "Exception expected!" );
		}
		catch (IOException e) {
			Assert.fail( "Exception expected!" );
		}
		catch (UnsupportedOperationException e) {
		}

		InputStream in;
		long remaining;
		byte[] tmpBuf = new byte[ 16 ];
		int read;
		int mod;

		for ( int r=0; r<runs; ++r) {
			for ( int n=min; n<max; ++n ) {
				srcArr = new byte[ n ];
				random.nextBytes( srcArr );

				try {
					/*
					 * Read.
					 */
					md.reset();
					byte[] digest1 = md.digest( srcArr );

					md.reset();
					in = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), md );

					dstOut.reset();

					remaining = srcArr.length;
					read = 0;
					mod = 2;
					while ( remaining > 0 && read != -1 ) {
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

					dstArr = dstOut.toByteArray();
					Assert.assertEquals( srcArr.length, dstArr.length );
					Assert.assertArrayEquals( srcArr, dstArr );

					in.close();

					byte[] digest2 = md.digest();

					Assert.assertArrayEquals( digest1, digest2 );

					md.reset();
					byte[] digest3 = md.digest( dstArr );

					Assert.assertArrayEquals( digest1, digest3 );
					/*
					 * Skip.
					 */
					md.reset();
					in = new DigestInputStreamNoSkip( new ByteArrayInputStream( srcArr ), md );

					dstOut.reset();

					remaining = srcArr.length;
					read = 0;
					mod = 3;
					int skipped = 0;
					while ( remaining > 0 && read != -1 ) {
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
						case 3:
							remaining -= read;
							skipped += read;
							break;
						}

						mod = (mod + 1) % 4;

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
						case 3:
							read = random.nextInt( 15 ) + 1;
							read = (int)in.skip( read );
							break;
						}
					}

					Assert.assertEquals( 0, remaining );

					dstArr = dstOut.toByteArray();
					Assert.assertEquals( srcArr.length, dstArr.length + skipped );

					in.close();

					byte[] digest4 = md.digest();

					Assert.assertArrayEquals( digest1, digest4 );
				}
				catch (IOException e) {
					Assert.fail( "Exception not expected!" );
					e.printStackTrace();
				}
			}
		}
	}

}
