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
 * Tests the Base64 encoder/decoder with loads of random data of various
 * lengths to cover all of the code.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestBase64 {

	private int min;
	private int max;
	private int runs;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 256, 10}
		});
	}

	public TestBase64(int min, int max, int runs) {
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

		String base64a;
		String base64s;

		byte[] dstArr;
		String dstStr;

		String base16sa;
		String base16ss;
		String base16da;
		String base16ds;

		for ( int r=0; r<runs; ++r) {
			for ( int n=min; n<max; ++n ) {
				srcArr = new byte[ n ];
				random.nextBytes( srcArr );

				srcSb.setLength( 0 );
				for ( int i=0; i<srcArr.length; ++i ) {
					srcSb.append( (char)(srcArr[ i ] & 255) );
				}
				srcStr = srcSb.toString();

				base64a = Base64.encodeArray( srcArr );
				base64s = Base64.encodeString( srcStr );

				dstArr = Base64.decodeToArray( base64a );
				dstStr = Base64.decodeToString( base64s );

				base16sa = Base16.encodeArray( srcArr );
				base16ss = Base16.encodeString( srcStr );

				base16da = Base16.encodeArray( dstArr );
				base16ds = Base16.encodeString( dstStr );

				/*
				System.out.println( base16sa );
				System.out.println( base16ss );
				System.out.println( base64a );
				System.out.println( base64s );
				System.out.println( base16da );
				System.out.println( base16ds );
				*/

				Assert.assertArrayEquals( srcArr, dstArr );
				Assert.assertEquals( base64a, base64s );
				Assert.assertEquals( srcStr, dstStr );
				Assert.assertEquals( base16sa, base16ss );
				Assert.assertEquals( base16da, base16ds );
			}
		}

		/*
		 * encode(String)
		 */

		srcSb.setLength( 0 );
		srcSb.append( (char)0x100 );
		srcStr = srcSb.toString();
		base64s = Base64.encodeString( srcStr );
		Assert.assertNull( base64s );

		/*
		 * decodeToArray
		 */

		dstArr = Base64.decodeToArray( "aaaa" );
		Assert.assertNotNull( dstArr );

		dstArr = Base64.decodeToArray( "aaa=" );
		Assert.assertNotNull( dstArr );

		dstArr = Base64.decodeToArray( "aa==" );
		Assert.assertNotNull( dstArr );

		dstArr = Base64.decodeToArray( "a===" );
		Assert.assertNull( dstArr );

		dstArr = Base64.decodeToArray( "####" );
		Assert.assertNull( dstArr );

		dstArr = Base64.decodeToArray( "###=" );
		Assert.assertNull( dstArr );

		dstArr = Base64.decodeToArray( "##==" );
		Assert.assertNull( dstArr );

		dstArr = Base64.decodeToArray( "#===" );
		Assert.assertNull( dstArr );

		/*
		 * decodeToArray
		 */

		dstStr = Base64.decodeToString( "aaaa" );
		Assert.assertNotNull( dstStr );

		dstStr = Base64.decodeToString( "aaa=" );
		Assert.assertNotNull( dstStr );

		dstStr = Base64.decodeToString( "aa==" );
		Assert.assertNotNull( dstStr );

		dstStr = Base64.decodeToString( "a===" );
		Assert.assertNull( dstStr );

		dstStr = Base64.decodeToString( "####" );
		Assert.assertNull( dstStr );

		dstStr = Base64.decodeToString( "###=" );
		Assert.assertNull( dstStr );

		dstStr = Base64.decodeToString( "##==" );
		Assert.assertNull( dstStr );

		dstStr = Base64.decodeToString( "#===" );
		Assert.assertNull( dstStr );
	}

}
