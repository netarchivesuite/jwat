package org.jwat.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestHttpHeader_IsValidMethods {

    @Test
    public void test_httpheader_isHttpStatusLineValid() {
        HttpHeader hh;
        boolean isValid;
        Object[][] cases = new Object[][] {
                {false, null,
                    null, null, null, null, null, null},
                {false, "",
                    null, null, null, null, null, null},
                {false, " ",
                    null, null, null, null, null, null},
                {false, "  ",
                    null, null, null, null, null, null},
                {false, " HTTP/1.2 OK ",
                    null, null, null, null, null, null},
                {false, "MONKEY/1.2 OK ",
                    "MONKEY/1.2", null, null, null, null, null},
                {false, "HTTP/1.2",
                    "HTTP/1.2", null, null, null, null, null},
                {false, "HTTP/1.2 ",
                    "HTTP/1.2", 1, 2, null, null, null},
                {false, "HTTP/1.2  ",
                    "HTTP/1.2", 1, 2, null, null, null},
                {false, "HTTP/1.2  100",
                    "HTTP/1.2", 1, 2, null, null, null},
                {false, "HTTP/1.2  100 ",
                    "HTTP/1.2", 1, 2, null, null, null},
                {false, "HTTP/1.2 001",
                    "HTTP/1.2", 1, 2, "001", 1, null},
                {false, "HTTP/1.2 1000",
                    "HTTP/1.2", 1, 2, "1000", 1000, null},
                {false, "HTTP/1.2 MONKEY!",
                    "HTTP/1.2", 1, 2, "MONKEY!", null, null},
                {false, "HTTP/ 100",
                    "HTTP/", null, null, null, null, null},
                {false, "HTTP/1. 100",
                    "HTTP/1.", 1, null, null, null, null},
                {false, "HTTP/. 100",
                    "HTTP/.", null, null, null, null, null},
                {false, "HTTP/.2 100",
                    "HTTP/.2", null, 2, null, null, null},
                {false, "HTTP/x.2 100",
                    "HTTP/x.2", null, 2, null, null, null},
                {false, "HTTP/1.x 100",
                    "HTTP/1.x", 1, null, null, null, null},
                {false, "HTTP/-1.2 100",
                    "HTTP/-1.2", -1, 2, null, null, null},
                {false, "HTTP/1.-2 100",
                    "HTTP/1.-2", 1, -2, null, null, null},
                {false, " HTTP/1.1 100",
                    null, null, null, null, null, null},
                {false, " HTTP/1.1 100 ",
                    null, null, null, null, null, null},
                {false, " HTTP/1.1 100  ",
                    null, null, null, null, null, null},
                {false, " HTTP/1.1 100 Monkeys are OK",
                    null, null, null, null, null, null},
                {true, "HTTP/1.0 100",
                    "HTTP/1.0", 1, 0, "100", 100, null},
                {true, "HTTP/1.0 100 ",
                    "HTTP/1.0", 1, 0, "100", 100, ""},
                {true, "HTTP/1.0 100  ",
                    "HTTP/1.0", 1, 0, "100", 100, " "},
                {true, "HTTP/1.0 100 Monkeys are OK",
                    "HTTP/1.0", 1, 0, "100", 100, "Monkeys are OK"},
                {true, "HTTP/1.0 100  Monkeys are OK",
                    "HTTP/1.0", 1, 0, "100", 100, " Monkeys are OK"}
        };
        for (int i=0; i<cases.length; ++i) {
            boolean expected = (Boolean)cases[i][0];
            String statusLine = (String)cases[i][1];
            String expectedVersion = (String)cases[i][2];
            Integer expectedMajor = (Integer)cases[i][3];
            Integer expectedMinor = (Integer)cases[i][4];
            String expectedStatusCodeStr = (String)cases[i][5];
            Integer expectedStatusCode = (Integer)cases[i][6];
            String expectedReasonPhrase = (String)cases[i][7];
            hh = new HttpHeader();
            isValid = hh.isHttpStatusLineValid(statusLine);
            // debug
            //System.out.println("\"" + statusLine + "\" \"" + hr.httpVersion + "\" "+ hr.httpVersionMajor + " " + hr.httpVersionMinor + " \"" + hr.statusCodeStr + "\" " + hr.statusCode + " \"" + hr.reasonPhrase + "\"");
            Assert.assertEquals(expected, isValid);
            Assert.assertEquals(expectedVersion, hh.httpVersion);
            Assert.assertEquals(expectedMajor, hh.httpVersionMajor);
            Assert.assertEquals(expectedMinor, hh.httpVersionMinor);
            Assert.assertEquals(expectedStatusCodeStr, hh.statusCodeStr);
            Assert.assertEquals(expectedStatusCode, hh.statusCode);
            Assert.assertEquals(expectedReasonPhrase, hh.reasonPhrase);
        }
    }

    @Test
    public void test_httpheader_isHttpRequestLineValid() {
        HttpHeader hh;
        boolean isValid;
        Object[][] cases = new Object[][] {
                {false, null,
                    null, null, null, null, null},
                {false, "",
                    null, null, null, null, null},
                {false, " ",
                    null, null, null, null, null},
                {false, "  ",
                    null, null, null, null, null},
                {false, "GET",
                    "GET", null, null, null, null},
                {false, "GET ",
                    "GET", "", null, null, null},
                {false, "GET  ",
                    "GET", null, null, null, null},
                {false, "GET /",
                    "GET", "/", null, null, null},
                {false, "GET / ",
                    "GET", "/", "", null, null},
                {false, "GET /  ",
                    "GET", "/", " ", null, null},
                {false, " GET / HTTP/1.2",
                    null, null, null, null, null},
                {false, "GET  / HTTP/1.2",
                    "GET", null, null, null, null},
                {false, "GET /  HTTP/1.2",
                    "GET", "/", " HTTP/1.2", null, null},
                {false, "GET / HTTP/1.2 ",
                    "GET", "/", "HTTP/1.2 ", 1, null},
                {false, "GET / HTTP/",
                    "GET", "/", "HTTP/", null, null},
                {false, "GET / HTTP/1.",
                    "GET", "/", "HTTP/1.", 1, null},
                {false, "GET / HTTP/.",
                    "GET", "/", "HTTP/.", null, null},
                {false, "GET / HTTP/.2",
                    "GET", "/", "HTTP/.2", null, 2},
                {false, "GET / HTTP/1.x",
                    "GET", "/", "HTTP/1.x", 1, null},
                {false, "GET / HTTP/x.2",
                    "GET", "/", "HTTP/x.2", null, 2},
                {false, "GET / HTTP/-1.2",
                    "GET", "/", "HTTP/-1.2", -1, 2},
                {false, "GET / HTTP/1.-2",
                    "GET", "/", "HTTP/1.-2", 1, -2},
                {true, "GET / HTTP/0.9",
                    "GET", "/", "HTTP/0.9", 0, 9},
                {true, "GET / HTTP/1.0",
                    "GET", "/", "HTTP/1.0", 1, 0},
                {true, "GET / HTTP/1.1",
                    "GET", "/", "HTTP/1.1", 1, 1}
        };
        for (int i=0; i<cases.length; ++i) {
            boolean expected = (Boolean)cases[i][0];
            String requestLine = (String)cases[i][1];
            String expectedMethod = (String)cases[i][2];
            String expectedReqUri = (String)cases[i][3];
            String expectedVersion = (String)cases[i][4];
            Integer expectedMajor = (Integer)cases[i][5];
            Integer expectedMinor = (Integer)cases[i][6];
            hh = new HttpHeader();
            isValid = hh.isHttpRequestLineValid(requestLine);
            // debug
            //System.out.println("\"" + requestLine + "\" \"" + hr.method + "\" \"" + hr.requestUri + "\" \"" + hr.httpVersion + "\" "+ hr.httpVersionMajor + " " + hr.httpVersionMinor);
            Assert.assertEquals(expected, isValid);
            Assert.assertEquals(expectedMethod, hh.method);
            Assert.assertEquals(expectedReqUri, hh.requestUri);
            Assert.assertEquals(expectedVersion, hh.httpVersion);
            Assert.assertEquals(expectedMajor, hh.httpVersionMajor);
            Assert.assertEquals(expectedMinor, hh.httpVersionMinor);
        }
        // TODO token
    }

}
