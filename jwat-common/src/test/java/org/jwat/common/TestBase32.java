package org.jwat.common;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.common.Base16;
import org.jwat.common.Base32;

/**
 * Tests the Base32 encoder/decoder with loads of random data of various
 * lengths to cover all of the code.
 *
 * @author nicl
 */
@RunWith(Parameterized.class)
public class TestBase32 {

	private int min;
	private int max;
	private int runs;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{1, 256, 10}
		});
	}

	public TestBase32(int min, int max, int runs) {
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

		String base32a;
		String base32s;

		byte[] dstArr;
		String dstStr;

		byte[] dstArrLc;
		String dstStrLc;

		String base16sa;
		String base16ss;
		String base16da;
		String base16ds;

		base32a = Base32.encodeArray( null );
		Assert.assertNull( base32a );
		base32a = Base32.encodeArray( new byte[ 0 ] );
		Assert.assertEquals( "", base32a );

		base32s = Base32.encodeString( null );
		Assert.assertNull( base32s );
		base32s = Base32.encodeString( "" );
		Assert.assertEquals( "", base32s );

		dstArr = Base32.decodeToArray( null );
		Assert.assertNull( dstArr );
		dstArr = Base32.decodeToArray( "" );
		Assert.assertArrayEquals( new byte[0], dstArr );

		dstStr = Base32.decodeToString( null );
		Assert.assertNull( dstStr );
		dstStr = Base32.decodeToString( "" );
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

				base32a = Base32.encodeArray( srcArr );
				base32s = Base32.encodeString( srcStr );

				dstArr = Base32.decodeToArray( base32a );
				dstStr = Base32.decodeToString( base32s );

				dstArrLc = Base32.decodeToArray( base32a.toLowerCase() );
				dstStrLc = Base32.decodeToString( base32s.toLowerCase() );

				base16sa = Base16.encodeArray( srcArr );
				base16ss = Base16.encodeString( srcStr );

				base16da = Base16.encodeArray( dstArr );
				base16ds = Base16.encodeString( dstStr );

				/*
				System.out.println( base16sa );
				System.out.println( base16ss );
				System.out.println( base32a );
				System.out.println( base32s );
				System.out.println( base16da );
				System.out.println( base16ds );
				*/

				Assert.assertArrayEquals( srcArr, dstArr );
				Assert.assertEquals( base32a, base32s );
				Assert.assertEquals( srcStr, dstStr );
				Assert.assertArrayEquals( dstArr, dstArrLc );
				Assert.assertEquals( dstStr, dstStrLc );
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
		base32s = Base32.encodeString( srcStr );
		Assert.assertNull( base32s );

		/*
		 * decodeToArray
		 */

		dstArr = Base32.decodeToArray( "aaaaaaaa" );
		Assert.assertNotNull( dstArr );

		dstArr = Base32.decodeToArray( "aaaaaaa=" );
		Assert.assertNotNull( dstArr );

		dstArr = Base32.decodeToArray( "aaaaaa==" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "aaaaa===" );
		Assert.assertNotNull( dstArr );

		dstArr = Base32.decodeToArray( "aaaa====" );
		Assert.assertNotNull( dstArr );

		dstArr = Base32.decodeToArray( "aaa=====" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "aa======" );
		Assert.assertNotNull( dstArr );

		dstArr = Base32.decodeToArray( "a=======" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "########" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "#######=" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "######==" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "#####===" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "####====" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "###=====" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "##======" );
		Assert.assertNull( dstArr );

		dstArr = Base32.decodeToArray( "#=======" );
		Assert.assertNull( dstArr );

		/*
		 * decodeToArray
		 */

		dstStr = Base32.decodeToString( "aaaaaaaa" );
		Assert.assertNotNull( dstStr );

		dstStr = Base32.decodeToString( "aaaaaaa=" );
		Assert.assertNotNull( dstStr );

		dstStr = Base32.decodeToString( "aaaaaa==" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "aaaaa===" );
		Assert.assertNotNull( dstStr );

		dstStr = Base32.decodeToString( "aaaa====" );
		Assert.assertNotNull( dstStr );

		dstStr = Base32.decodeToString( "aaa=====" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "aa======" );
		Assert.assertNotNull( dstStr );

		dstStr = Base32.decodeToString( "a=======" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "########" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "#######=" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "######==" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "#####===" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "####====" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "###=====" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "##======" );
		Assert.assertNull( dstStr );

		dstStr = Base32.decodeToString( "#=======" );
		Assert.assertNull( dstStr );
	}

}
