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

@RunWith(Parameterized.class)
public class TestStreamsInStreams {

    private int min;
    private int max;
    private int runs;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {768, 1024, 1}
        });
    }

    public TestStreamsInStreams(int min, int max, int runs) {
        this.min = min;
        this.max = max;
        this.runs = runs;
    }

    @Test
    public void test_streams_in_streams() {
        MessageDigest md = null;
        MessageDigest md2 = null;
        try {
            md = MessageDigest.getInstance( "sha1" );
            md2 = MessageDigest.getInstance( "sha1" );
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        InputStream in;

        ByteCountingInputStream bcin1;
    	ByteCountingPushBackInputStream pbin1;
    	DigestInputStreamNoSkip disns1;
    	FixedLengthInputStream flin1;
    	MaxLengthRecordingInputStream mlrin1;

    	ByteCountingInputStream bcin2;
    	ByteCountingPushBackInputStream pbin2;
    	DigestInputStreamNoSkip disns2;
    	FixedLengthInputStream flin2;
    	MaxLengthRecordingInputStream mlrin2;

        SecureRandom random = new SecureRandom();

        byte[] srcArr = new byte[ 1 ];
        ByteArrayOutputStream dstOut = new ByteArrayOutputStream();
        byte[] dstArr;

        long remaining;
        long consumed;
        byte[] tmpBuf = new byte[ 16 ];
        int read;
        int mod;


        md.reset();

        for ( int r=0; r<runs; ++r) {
            for ( int n=min; n<max; ++n ) {
                srcArr = new byte[ n ];
                random.nextBytes( srcArr );

                try {
                    /*
                     * Read.
                     */
                    in = new ByteArrayInputStream( srcArr );
                	bcin1 = new ByteCountingInputStream(in);
                	pbin1 = new ByteCountingPushBackInputStream(bcin1, 16);
                	disns1 = new DigestInputStreamNoSkip(pbin1, md);
                	flin1 = new FixedLengthInputStream(disns1, srcArr.length);
                	mlrin1 = new MaxLengthRecordingInputStream(flin1, srcArr.length);
                	disns2 = new DigestInputStreamNoSkip(mlrin1, md2);
                	mlrin2 = new MaxLengthRecordingInputStream(disns2, srcArr.length);
                	bcin2 = new ByteCountingInputStream(mlrin2);
                	pbin2 = new ByteCountingPushBackInputStream(bcin2, 16);
                	flin2 = new FixedLengthInputStream(pbin2, srcArr.length);

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
                            Assert.assertEquals( consumed, bcin1.consumed );
                            Assert.assertEquals( consumed, pbin1.consumed );
                            //Assert.assertEquals( consumed, disns1.consumed );
                            //Assert.assertEquals( consumed, flin1.consumed );
                            //Assert.assertEquals( consumed, mlrin1.consumed );
                            //Assert.assertEquals( consumed, disns2.consumed );
                            //Assert.assertEquals( consumed, mlrin2.consumed );
                            Assert.assertEquals( consumed, bcin2.consumed );
                            Assert.assertEquals( consumed, pbin2.consumed );
                            //Assert.assertEquals( consumed, flin2.consumed );
                            break;
                        case 1:
                        case 2:
                            dstOut.write( tmpBuf, 0, read );
                            remaining -= read;
                            consumed += read;
                            Assert.assertEquals( consumed, bcin1.consumed );
                            Assert.assertEquals( consumed, pbin1.consumed );
                            //Assert.assertEquals( consumed, disns1.consumed );
                            //Assert.assertEquals( consumed, flin1.consumed );
                            //Assert.assertEquals( consumed, mlrin1.consumed );
                            //Assert.assertEquals( consumed, disns2.consumed );
                            //Assert.assertEquals( consumed, mlrin2.consumed );
                            Assert.assertEquals( consumed, bcin2.consumed );
                            Assert.assertEquals( consumed, pbin2.consumed );
                            //Assert.assertEquals( consumed, flin2.consumed );
                            break;
                        }

                        mod = (mod + 1) % 3;

                        switch ( mod ) {
                        case 0:
                            read = flin2.read();
                            break;
                        case 1:
                            read = flin2.read( tmpBuf );
                            break;
                        case 2:
                            read = random.nextInt( 15 ) + 1;
                            read = flin2.read( tmpBuf, 0, read );
                            break;
                        }
                    }

                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( n, consumed );
                    Assert.assertEquals( n, bcin1.consumed );
                    Assert.assertEquals( n, bcin1.counter );
                    Assert.assertEquals( consumed, bcin1.getConsumed() );
                    Assert.assertEquals( consumed, bcin1.getCounter() );
                    Assert.assertEquals( n, pbin1.consumed );
                    Assert.assertEquals( n, pbin1.counter );
                    Assert.assertEquals( consumed, pbin1.getConsumed() );
                    Assert.assertEquals( consumed, pbin1.getCounter() );
                    Assert.assertEquals( n, bcin2.consumed );
                    Assert.assertEquals( n, bcin2.counter );
                    Assert.assertEquals( consumed, bcin2.getConsumed() );
                    Assert.assertEquals( consumed, bcin2.getCounter() );
                    Assert.assertEquals( n, pbin2.consumed );
                    Assert.assertEquals( n, pbin2.counter );
                    Assert.assertEquals( consumed, pbin2.getConsumed() );
                    Assert.assertEquals( consumed, pbin2.getCounter() );

                    Assert.assertEquals( 0, flin1.remaining );
                    Assert.assertEquals( 0, flin1.available() );
                    Assert.assertEquals( 0, mlrin1.available );
                    Assert.assertEquals( 0, mlrin1.available() );
                    Assert.assertEquals( 0, flin2.remaining );
                    Assert.assertEquals( 0, flin2.available() );
                    Assert.assertEquals( 0, mlrin2.available );
                    Assert.assertEquals( 0, mlrin2.available() );

                    Assert.assertArrayEquals(md.digest(), md2.digest());

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length );
                    Assert.assertArrayEquals( srcArr, dstArr );

                    Assert.assertEquals( srcArr.length, mlrin1.getRecording().length );
                    Assert.assertArrayEquals( srcArr, mlrin1.getRecording() );
                    Assert.assertEquals( srcArr.length, mlrin2.getRecording().length );
                    Assert.assertArrayEquals( srcArr, mlrin2.getRecording() );

                    flin2.close();
                    /*
                     * Skip.
                     */
                    in = new ByteArrayInputStream( srcArr );
                	bcin1 = new ByteCountingInputStream(in);
                	pbin1 = new ByteCountingPushBackInputStream(bcin1, 16);
                	disns1 = new DigestInputStreamNoSkip(pbin1, md);
                	flin1 = new FixedLengthInputStream(disns1, srcArr.length);
                	mlrin1 = new MaxLengthRecordingInputStream(flin1, srcArr.length);
                	disns2 = new DigestInputStreamNoSkip(mlrin1, md2);
                	mlrin2 = new MaxLengthRecordingInputStream(disns2, srcArr.length);
                	bcin2 = new ByteCountingInputStream(mlrin2);
                	pbin2 = new ByteCountingPushBackInputStream(bcin2, 16);
                	flin2 = new FixedLengthInputStream(pbin2, srcArr.length);

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
                            Assert.assertEquals( consumed, bcin1.consumed );
                            Assert.assertEquals( consumed, pbin1.consumed );
                            Assert.assertEquals( consumed, bcin2.consumed );
                            Assert.assertEquals( consumed, pbin2.consumed );
                            break;
                        case 1:
                        case 2:
                            dstOut.write( tmpBuf, 0, read );
                            remaining -= read;
                            consumed += read;
                            Assert.assertEquals( consumed, bcin1.consumed );
                            Assert.assertEquals( consumed, pbin1.consumed );
                            Assert.assertEquals( consumed, bcin2.consumed );
                            Assert.assertEquals( consumed, pbin2.consumed );
                            break;
                        case 3:
                            remaining -= read;
                            consumed += read;
                            skipped += read;
                            Assert.assertEquals( consumed, bcin1.consumed );
                            Assert.assertEquals( consumed, pbin1.consumed );
                            Assert.assertEquals( consumed, bcin2.consumed );
                            Assert.assertEquals( consumed, pbin2.consumed );
                            break;
                        }

                        mod = (mod + 1) % 4;

                        switch ( mod ) {
                        case 0:
                            read = flin2.read();
                            break;
                        case 1:
                            read = flin2.read( tmpBuf );
                            break;
                        case 2:
                            read = random.nextInt( 15 ) + 1;
                            read = flin2.read( tmpBuf, 0, read );
                            break;
                        case 3:
                            read = random.nextInt( 15 ) + 1;
                            read = (int)flin2.skip( read );
                            break;
                        }
                    }

                    Assert.assertEquals( 0, remaining );
                    Assert.assertEquals( n, consumed );
                    Assert.assertEquals( n, bcin1.consumed );
                    Assert.assertEquals( n, bcin1.counter );
                    Assert.assertEquals( consumed, bcin1.getConsumed() );
                    Assert.assertEquals( consumed, bcin1.getCounter() );
                    Assert.assertEquals( n, pbin1.consumed );
                    Assert.assertEquals( n, pbin1.counter );
                    Assert.assertEquals( consumed, pbin1.getConsumed() );
                    Assert.assertEquals( consumed, pbin1.getCounter() );
                    Assert.assertEquals( n, bcin2.consumed );
                    Assert.assertEquals( n, bcin2.counter );
                    Assert.assertEquals( consumed, bcin2.getConsumed() );
                    Assert.assertEquals( consumed, bcin2.getCounter() );
                    Assert.assertEquals( n, pbin2.consumed );
                    Assert.assertEquals( n, pbin2.counter );
                    Assert.assertEquals( consumed, pbin2.getConsumed() );
                    Assert.assertEquals( consumed, pbin2.getCounter() );

                    Assert.assertEquals( 0, flin1.remaining );
                    Assert.assertEquals( 0, flin1.available() );
                    Assert.assertEquals( 0, mlrin1.available );
                    Assert.assertEquals( 0, mlrin1.available() );
                    Assert.assertEquals( 0, mlrin2.available );
                    Assert.assertEquals( 0, mlrin2.available() );
                    Assert.assertEquals( 0, flin2.remaining );
                    Assert.assertEquals( 0, flin2.available() );

                    Assert.assertArrayEquals(md.digest(), md2.digest());

                    dstArr = dstOut.toByteArray();
                    Assert.assertEquals( srcArr.length, dstArr.length + skipped );

                    Assert.assertEquals( srcArr.length, mlrin1.getRecording().length );
                    Assert.assertArrayEquals( srcArr, mlrin1.getRecording() );
                    Assert.assertEquals( srcArr.length, mlrin2.getRecording().length );
                    Assert.assertArrayEquals( srcArr, mlrin2.getRecording() );

                    flin2.close();
                }
                catch (IOException e) {
                    Assert.fail( "Exception not expected!" );
                    e.printStackTrace();
                }
            }
        }
    }

}
