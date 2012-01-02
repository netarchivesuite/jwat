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
public class TestMaxLengthRecordInputStream {

    private int min;
    private int max;
    private int runs;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {1, 1024, 2}
        });
    }

    public TestMaxLengthRecordInputStream(int min, int max, int runs) {
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

        MaxLengthRecordingInputStream in = new MaxLengthRecordingInputStream( new ByteArrayInputStream( srcArr ), 0 );

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

        in = new MaxLengthRecordingInputStream( new ByteArrayInputStream( srcArr ), Integer.MAX_VALUE + 1L );
        try {
            Assert.assertEquals( Integer.MAX_VALUE, in.available() );
            in.close();
        }
        catch (IOException e1) {
            Assert.fail( "Exception not expected!" );
        }

        in = new MaxLengthRecordingInputStream( new ByteArrayInputStream( srcArr ), Integer.MAX_VALUE - 1L );
        try {
            Assert.assertEquals( Integer.MAX_VALUE - 1, in.available() );
            in.close();
        }
        catch (IOException e1) {
            Assert.fail( "Exception not expected!" );
        }

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
                    in = new MaxLengthRecordingInputStream( new ByteArrayInputStream( srcArr ), n );

                    dstOut.reset();

                    remaining = srcArr.length;
                    read = 0;
                    mod = 2;
                    while ( remaining > 0 && read != -1 ) {
                        switch ( mod ) {
                        case 0:
                            dstOut.write( read );
                            --remaining;
                            Assert.assertEquals( remaining, in.available );
                            break;
                        case 1:
                        case 2:
                            dstOut.write( tmpBuf, 0, read );
                            remaining -= read;
                            Assert.assertEquals( remaining, in.available );
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
                    Assert.assertEquals( 0, in.available );

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    in.close();

                    /*
                     * Skip.
                     */
                    in = new MaxLengthRecordingInputStream( new ByteArrayInputStream( srcArr ), n );

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
                            Assert.assertEquals( remaining, in.available );
                            break;
                        case 1:
                        case 2:
                            dstOut.write( tmpBuf, 0, read );
                            remaining -= read;
                            Assert.assertEquals( remaining, in.available );
                            break;
                        case 3:
                            remaining -= read;
                            skipped += read;
                            Assert.assertEquals( remaining, in.available );
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
                    Assert.assertEquals( 0, in.available );

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length + skipped );

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
