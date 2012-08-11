/**
 * Java Web Archive Toolkit - Software to read and validate ARC, WARC
 * and GZip files. (http://jwat.org/)
 * Copyright 2011-2012 Netarkivet.dk (http://netarkivet.dk/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwat.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestHttpHeader {

    @Test
    public void test_httpheader() {
        byte[] srcArr = new byte[ 0 ];

        ByteCountingPushBackInputStream pbin;
        HttpHeader httpHeader;

        httpHeader = new HttpHeader();
        String tmpStr = httpHeader.toString();
        Assert.assertNotNull(tmpStr);

        Assert.assertTrue( HttpHeader.isSupported( "http" ) );
        Assert.assertTrue( HttpHeader.isSupported( "https" ) );
        Assert.assertTrue( HttpHeader.isSupported( "Http" ) );
        Assert.assertTrue( HttpHeader.isSupported( "Https" ) );

        Assert.assertFalse( HttpHeader.isSupported( "httpss" ) );
        Assert.assertFalse( HttpHeader.isSupported( "ftp" ) );
        Assert.assertFalse( HttpHeader.isSupported( "ftps" ) );

        try {
            httpHeader = HttpHeader.processPayload( 0, null, 0, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            httpHeader = HttpHeader.processPayload( HttpHeader.HT_RESPONSE, null, 0, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            pbin = new ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
            httpHeader = HttpHeader.processPayload( HttpHeader.HT_RESPONSE, pbin, -1, null );
            Assert.fail( "Exception expected!" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

        try {
            pbin = new  ByteCountingPushBackInputStream( new ByteArrayInputStream( srcArr ), 8192 );
            httpHeader = HttpHeader.processPayload( HttpHeader.HT_RESPONSE, pbin, 0, "shit1" );
            Assert.assertFalse(httpHeader.isValid());
            Assert.assertNull( httpHeader.getDigest() );
            try {
                httpHeader.getPayloadLength();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getTotalLength();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getUnavailable();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getInputStreamComplete();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getPayloadInputStream();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
            try {
                httpHeader.getRemaining();
                Assert.fail("Exception expected!");
            } catch (IllegalStateException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
