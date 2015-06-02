package org.jwat.arc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

/**
 * TODO javadoc
 * @author Nicholas
 * Created on 28/12/2012
 */
public class TestHelpers {

    protected static ClassLoader clsLdr = TestHelpers.class.getClassLoader();

    public static final File getTestResourceFile(String fname) {
        URL url = clsLdr.getResource( fname );
        if ( url ==  null ) {
            throw new IllegalStateException( "Could not find resource, '" + fname + "'." );
        }
        String path = url.getFile();
        if ( path == null ) {
            throw new IllegalStateException( "Could not resolve path for resource, " + fname + "'." );
        }
        path = path.replaceAll( "%5b", "[" );
        path = path.replaceAll( "%5d", "]" );
        File file = new File( path );
        return file;
    }

    public static final InputStream getTestResourceAsStream(String fname) {
        return clsLdr.getResourceAsStream( fname );
    }

    public static void assertArrayEquals(boolean[] expecteds, boolean[] actuals) {
        Assert.assertEquals( expecteds.length, actuals.length );
        for ( int i=0; i<expecteds.length; ++i ) {
            Assert.assertEquals( expecteds[ i ], actuals[ i ] );
        }
    }

    public static void assertArrayEquals(float[] expecteds, float[] actuals) {
        Assert.assertEquals( expecteds.length, actuals.length );
        for ( int i=0; i<expecteds.length; ++i ) {
            Assert.assertEquals( (Float)expecteds[ i ], (Float)actuals[ i ] );
        }
    }

    public static void assertArrayEquals(double[] expecteds, double[] actuals) {
        Assert.assertEquals( expecteds.length, actuals.length );
        for ( int i=0; i<expecteds.length; ++i ) {
            Assert.assertEquals( (Double)expecteds[ i ], (Double)actuals[ i ] );
        }
    }

    public static byte[] filterWhitespaces(byte[] in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c;
        for (int i=0; i<in.length; ++ i) {
            c = in[ i ] & 255;
            switch ( c ) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            default:
                out.write( c );
                break;
            }
        }
        return out.toByteArray();
    }

    public static void saveFile(String fname, byte[] bytes) throws IOException {
        File file = new File( fname );
        if ( file.exists() ) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile( file, "rw" );
        raf.seek( 0L );
        raf.setLength( 0L );
        raf.write( bytes );
        raf.close();
    }

    public static<T> LinkedList<T> arrayToLinkedList(T[] arr) {
        //return (ArrayList<T>)Arrays.asList( arr );
        LinkedList<T> list = new LinkedList<T>();
        for ( int i=0; i<arr.length; ++i ) {
            list.add( arr[ i ] );
        }
        return list;
    }

    public static<T> ArrayList<T> arrayToArrayList(T[] arr) {
        //return (ArrayList<T>)Arrays.asList( arr );
        ArrayList<T> list = new ArrayList<T>();
        for ( int i=0; i<arr.length; ++i ) {
            list.add( arr[ i ] );
        }
        return list;
    }

    public static<T> void assertListEquals(List<T> expecteds, List<T> actuals) {
        if (expecteds == null) {
            if (actuals != null) {
                Assert.fail( "Expected 'null' but got: " + actuals );
            }
        }
        else {
            Assert.assertEquals( "Sizes do not match, expected + " + expecteds.size() + " got " + actuals.size(), expecteds.size(), actuals.size() );
            for ( int i=0; i<expecteds.size(); ++i ) {
                Assert.assertEquals( "Elements at index: " + i + " do not match!", expecteds.get( i ), actuals.get( i ) );
            }
        }
    }

}
