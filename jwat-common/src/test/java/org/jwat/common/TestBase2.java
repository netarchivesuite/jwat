package org.jwat.common;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.Base2;

/**
 * Tests the Base2 encoder/decoder with loads of random data of various lengths to cover all of the code.
 *
 * @author Nicholas
 */
@RunWith(Parameterized.class)
public class TestBase2 {

	private int min;
	private int max;
	private int runs;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 256, 10}
		});
	}

	public TestBase2(int min, int max, int runs) {
		this.min = min;
		this.max = max;
		this.runs = runs;
	}

	@Test
	public void test() {
		SecureRandom random = new SecureRandom();
		byte[] srcArr;
		StringBuffer srcSb = new StringBuffer( 256 );
		String srcStr;

		String base2a;
		String base2s;

		byte[] dstArr;
		String dstStr;

		/*
		try {
			Assert.assertArrayEquals( a256, s256.getBytes("ISO-8859-1") );
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/

		base2a = Base2.encodeArray( null );
		Assert.assertNull( base2a );
		base2a = Base2.encodeArray( new byte[ 0 ] );
		Assert.assertEquals( "", base2a );

		base2s = Base2.encodeString( null );
		Assert.assertNull( base2s );
		base2s = Base2.encodeString( "" );
		Assert.assertEquals( "", base2s );

		dstArr = Base2.decodeToArray( null );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "" );
		Assert.assertArrayEquals( new byte[0], dstArr );

		dstStr = Base2.decodeToString( null );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "" );
		Assert.assertEquals( "", dstStr );

		dstStr = Base2.delimit( null, 8, '.' );
		Assert.assertNull( dstStr );
		dstStr = Base2.delimit( "", 8, '.' );
		Assert.assertEquals( "", dstStr );

		for ( int r=0; r<runs; ++r ) {
			for ( int n=min; n<max; ++n ) {
				srcArr = new byte[ n ];
				random.nextBytes( srcArr );

				srcSb.setLength( 0 );
				for ( int i=0; i<srcArr.length; ++i ) {
					srcSb.append( (char)(srcArr[ i ] & 255) );
				}
				srcStr = srcSb.toString();

				base2a = Base2.encodeArray( srcArr );
				base2s = Base2.encodeString( srcStr );

				dstArr = Base2.decodeToArray( base2a );
				dstStr = Base2.decodeToString( base2s );

				/*
				System.out.println( Base16.encodeArray( srcArr ) );
				System.out.println( Base16.encodeArray( dstArr ) );
				System.out.println( base2a );
				System.out.println( base2s );
				System.out.println( Base16.encodeString( srcStr ) );
				System.out.println( Base16.encodeString( dstStr ) );
				*/

				Assert.assertArrayEquals( srcArr, dstArr );
				Assert.assertEquals( base2a, base2s );
				Assert.assertEquals( srcStr, dstStr );
			}
		}

		srcArr = new byte[ 256 ];
		random.nextBytes( srcArr );
		base2a = Base2.encodeArray( srcArr );
		String limited;

		limited = Base2.delimit( base2a, 0, '.' );
		Assert.assertEquals( base2a, limited );

		StringBuffer destSb = new StringBuffer();

		for ( int i=1; i<256; ++i ) {
			limited = Base2.delimit( base2a, i, '.' );

			destSb.setLength( 0 );

			for ( int l=0; l<limited.length(); ++l ) {
				if ( (l % (i + 1)) == i ) {
					if ( limited.charAt( l ) != '.' ) {
						Assert.fail( "Delimiter expected!" );
					}
				}
				else {
					destSb.append( limited.charAt( l ) );
				}
			}

			Assert.assertEquals( base2a, destSb.toString() );
		}

		/*
		 * encode(String)
		 */

		srcSb.setLength( 0 );
		srcSb.append( (char)0x100 );
		srcStr = srcSb.toString();
		base2s = Base2.encodeString( srcStr );
		Assert.assertNull( base2s );

		/*
		 * decodeToArray
		 */

		dstArr = Base2.decodeToArray( "1010101001010101" );
		Assert.assertNotNull( dstArr );
		dstArr = Base2.decodeToArray( "101010100101010" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "10101010010101" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "1010101001010" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "101010100101" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "10101010010" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "1010101001" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "101010100" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "10101010" );
		Assert.assertNotNull( dstArr );
		dstArr = Base2.decodeToArray( "1010101" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "101010" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "10101" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "1010" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "101" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "10" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "1" );
		Assert.assertNull( dstArr );

		dstArr = Base2.decodeToArray( "babababaabababab" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babababaabababa" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babababaababab" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babababaababa" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babababaabab" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babababaaba" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babababaab" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babababaa" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babababa" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "bababab" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "bababa" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "babab" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "baba" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "bab" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "ba" );
		Assert.assertNull( dstArr );
		dstArr = Base2.decodeToArray( "b" );
		Assert.assertNull( dstArr );

		/*
		 * decodeToArray
		 */

		dstStr = Base2.decodeToString( "1010101001010101" );
		Assert.assertNotNull( dstStr );
		dstStr = Base2.decodeToString( "101010100101010" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "10101010010101" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "1010101001010" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "101010100101" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "10101010010" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "1010101001" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "101010100" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "10101010" );
		Assert.assertNotNull( dstStr );
		dstStr = Base2.decodeToString( "1010101" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "101010" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "10101" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "1010" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "101" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "10" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "1" );
		Assert.assertNull( dstStr );

		dstStr = Base2.decodeToString( "babababaabababab" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babababaabababa" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babababaababab" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babababaababa" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babababaabab" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babababaaba" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babababaab" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babababaa" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babababa" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "bababab" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "bababa" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "babab" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "baba" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "bab" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "ba" );
		Assert.assertNull( dstStr );
		dstStr = Base2.decodeToString( "b" );
		Assert.assertNull( dstStr );
	}

}
