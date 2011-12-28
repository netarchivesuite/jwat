package org.jwat.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestByteCountingPushbackInputStream {

	private int min;
	private int max;
	private int runs;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 1024, 2}
		});
	}

	public TestByteCountingPushbackInputStream(int min, int max, int runs) {
		this.min = min;
		this.max = max;
		this.runs = runs;
	}

	@Test
	public void test() {
		SecureRandom random = new SecureRandom();

		byte[] srcArr = new byte[ 1 ];
		ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
		byte[] dstArr;

		ByteCountingPushBackInputStream in = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 16 );

		Assert.assertFalse( in.markSupported() );
		in.mark( 1 );
		try {
			in.reset();
			Assert.fail( "Exception expected!" );
		}
		catch (IOException e) {
			Assert.fail( "Exception expected!" );
		}
		catch (UnsupportedOperationException e) {
		}

		long remaining;
		long consumed;
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
					in = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 16 );

					dstOut.reset();

					remaining = srcArr.length;
					consumed = 0;
					read = 0;
					mod = 2;
					while ( remaining > 0 && read != -1 ) {
						switch ( mod ) {
						case 0:
							dstOut.write( read );
							--remaining;
							++consumed;
							Assert.assertEquals( consumed, in.consumed );
							break;
						case 1:
						case 2:
							dstOut.write( tmpBuf, 0, read );
							remaining -= read;
							consumed += read;
							Assert.assertEquals( consumed, in.consumed );
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
					Assert.assertEquals( n, consumed );
					Assert.assertEquals( n, in.consumed );
					Assert.assertEquals( n, in.counter );
					Assert.assertEquals( in.consumed, in.getConsumed() );
					Assert.assertEquals( in.counter, in.getCounter() );

					dstArr = dstOut.toByteArray();
					Assert.assertEquals( srcArr.length, dstArr.length );
					Assert.assertArrayEquals( srcArr, dstArr );

					in.close();
					/*
					 * Skip.
					 */
					in = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 16 );
					in.setCounter( n );

					dstOut.reset();

					remaining = srcArr.length;
					consumed = 0;
					read = 0;
					mod = 3;
					int skipped = 0;
					while ( remaining > 0 && read != -1 ) {
						switch ( mod ) {
						case 0:
							dstOut.write( read );
							--remaining;
							++consumed;
							Assert.assertEquals( consumed, in.consumed );
							break;
						case 1:
						case 2:
							dstOut.write( tmpBuf, 0, read );
							remaining -= read;
							consumed += read;
							Assert.assertEquals( consumed, in.consumed );
							break;
						case 3:
							remaining -= read;
							consumed += read;
							skipped += read;
							Assert.assertEquals( consumed, in.consumed );
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
					Assert.assertEquals( n, consumed );
					Assert.assertEquals( n, in.consumed );
					Assert.assertEquals( 2 * n, in.counter );
					Assert.assertEquals( in.consumed, in.getConsumed() );
					Assert.assertEquals( in.counter, in.getCounter() );

					dstArr = dstOut.toByteArray();
					Assert.assertEquals( srcArr.length, dstArr.length + skipped );

					in.close();
					/*
					 * Unread.
					 */
					in = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 16 );
					in.setCounter( -n );

					dstArr = new byte[ n ];

					remaining = srcArr.length;
					consumed = 0;
					read = 0;
					mod = 2;
					while ( remaining > 0 && read != -1 ) {
						switch ( mod ) {
						case 0:
							dstArr[ (int)consumed ] = (byte)read;
							--remaining;
							++consumed;
							Assert.assertEquals( consumed, in.consumed );
							in.unread( read );
							Assert.assertEquals( consumed - 1, in.consumed );
							in.skip( 1 );
							Assert.assertEquals( consumed, in.consumed );
							break;
						case 1:
						case 2:
							System.arraycopy( tmpBuf, 0, dstArr, (int)consumed, read );
							remaining -= read;
							consumed += read;
							Assert.assertEquals( consumed, in.consumed );
							if ( read == tmpBuf.length ) {
								in.unread( tmpBuf );
								Assert.assertEquals( consumed - read, in.consumed );
								in.skip( read );
								Assert.assertEquals( consumed, in.consumed );
							}
							else {
								in.unread( tmpBuf, 0, read );
								Assert.assertEquals( consumed - read, in.consumed );
								in.skip( read );
								Assert.assertEquals( consumed, in.consumed );
							}
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
					Assert.assertEquals( n, consumed );
					Assert.assertEquals( n, in.consumed );
					Assert.assertEquals( 0, in.counter );
					Assert.assertEquals( in.consumed, in.getConsumed() );
					Assert.assertEquals( in.counter, in.getCounter() );

					Assert.assertEquals( srcArr.length, dstArr.length );
					Assert.assertArrayEquals( srcArr, dstArr );

					in.close();
				}
				catch (IOException e) {
					Assert.fail( "Exception not expected!" );
					e.printStackTrace();
				}
			}
		}
	}

}
