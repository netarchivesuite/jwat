package org.jwat.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestHttpResponseIsProtocolValid {

    @Test
    public void test_httpresponse_isprotocolvalid() {
        HttpResponse hr = new HttpResponse();
        boolean isValid;

        isValid = hr.isHttpStatusLineValid( null );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "" );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( " " );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "  " );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( " HTTP/1.1 OK " );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "MONKEY/1.1 OK " );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1" );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1 " );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1  " );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1  100" );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1  100 " );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1 001" );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1 1000" );
        Assert.assertFalse( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1 MONKEY!" );
        Assert.assertFalse( isValid );

        isValid = hr.isHttpStatusLineValid( "HTTP/1.1 100" );
        Assert.assertTrue( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1 100 " );
        Assert.assertTrue( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1 100  " );
        Assert.assertTrue( isValid );
        isValid = hr.isHttpStatusLineValid( "HTTP/1.1 100 Monkeys are OK" );
        Assert.assertTrue( isValid );
    }

}
