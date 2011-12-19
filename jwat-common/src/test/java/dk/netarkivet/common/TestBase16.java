package dk.netarkivet.common;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the Base16 encoder/decoder with loads of random data of various
 * lengths to cover all of the code.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestBase16 {

	private int min;
	private int max;
	private int runs;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 256, 10}
		});
	}

	public TestBase16(int min, int max, int runs) {
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

		String base16a;
		String base16s;

		byte[] dstArr;
		String dstStr;

		byte[] dstArrLc;
		String dstStrLc;

		/*
		try {
			Assert.assertArrayEquals( a256, s256.getBytes("ISO-8859-1") );
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/

		base16a = Base16.encodeArray( null );
		Assert.assertNull( base16a );
		base16a = Base16.encodeArray( new byte[ 0 ] );
		Assert.assertNull( base16a );

		base16s = Base16.encodeString( null );
		Assert.assertNull( base16s );
		base16s = Base16.encodeString( "" );
		Assert.assertNull( base16s );

		dstArr = Base16.decodeToArray( null );
		Assert.assertNull( dstArr );
		dstArr = Base16.decodeToArray( "" );
		Assert.assertNull( dstArr );

		dstStr = Base16.decodeToString( null );
		Assert.assertNull( dstStr );
		dstStr = Base16.decodeToString( "" );
		Assert.assertNull( dstStr );

		for ( int r=0; r<runs; ++r ) {
			for ( int n=min; n<max; ++n ) {
				srcArr = new byte[ n ];
				random.nextBytes( srcArr );

				srcSb.setLength( 0 );
				for ( int i=0; i<srcArr.length; ++i ) {
					srcSb.append( (char)(srcArr[ i ] & 255) );
				}
				srcStr = srcSb.toString();

				base16a = Base16.encodeArray( srcArr );
				base16s = Base16.encodeString( srcStr );

				dstArr = Base16.decodeToArray( base16a );
				dstStr = Base16.decodeToString( base16s );

				dstArrLc = Base16.decodeToArray( base16a.toLowerCase() );
				dstStrLc = Base16.decodeToString( base16s.toLowerCase() );

				/*
				System.out.println( Base16.encodeArray( srcArr ) );
				System.out.println( Base16.encodeArray( dstArr ) );
				System.out.println( base16a );
				System.out.println( base16s );
				System.out.println( Base16.encodeString( srcStr ) );
				System.out.println( Base16.encodeString( dstStr ) );
				*/

				Assert.assertArrayEquals( srcArr, dstArr );
				Assert.assertEquals( base16a, base16s );
				Assert.assertEquals( srcStr, dstStr );
				Assert.assertArrayEquals( dstArr, dstArrLc );
				Assert.assertEquals( dstStr, dstStrLc );
			}
		}

		/*
		 * encode(String)
		 */

		srcSb.setLength( 0 );
		srcSb.append( (char)0x100 );
		srcStr = srcSb.toString();
		base16s = Base16.encodeString( srcStr );
		Assert.assertNull( base16s );

		/*
		 * decodeToArray
		 */

		dstArr = Base16.decodeToArray( "fa" );
		Assert.assertNotNull( dstArr );

		dstArr = Base16.decodeToArray( "f" );
		Assert.assertNull( dstArr );

		dstArr = Base16.decodeToArray( "zx" );
		Assert.assertNull( dstArr );

		dstArr = Base16.decodeToArray( "z" );
		Assert.assertNull( dstArr );

		/*
		 * decodeToArray
		 */

		dstStr = Base16.decodeToString( "fa" );
		Assert.assertNotNull( dstStr );

		dstStr = Base16.decodeToString( "f" );
		Assert.assertNull( dstStr );

		dstStr = Base16.decodeToString( "zx" );
		Assert.assertNull( dstStr );

		dstStr = Base16.decodeToString( "z" );
		Assert.assertNull( dstStr );
	}

}
