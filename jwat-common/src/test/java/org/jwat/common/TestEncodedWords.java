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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestEncodedWords {

    @Test
    public void test_encodedwords() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] strUtf8Full;
        byte[] strUtf8Part;
        String base64UtfFull;
        String base64UtfPart;
        byte[] ewUtf8FullBase64Full;
        byte[] ewUtf8PartBase64Full;
        byte[] ewUtf8FullBase64Part;
        byte[] ewUtf8PartBase64Part;
        Object[][] cases;
        try {
            strUtf8Full = "æ".getBytes("UTF-8");
            strUtf8Part = new byte[1];
            System.arraycopy(strUtf8Full, 0, strUtf8Part, 0, 1);
            base64UtfFull = Base64.encodeArray(strUtf8Full);
            base64UtfPart = Base64.encodeArray(strUtf8Part);

            /*
             * Test with =? included in the parsing.
             */
            out.reset();
            out.write("=?UTF-8?B?".getBytes());
            out.write(base64UtfFull.getBytes());
            out.write("?=".getBytes());
            ewUtf8FullBase64Full = out.toByteArray();

            out.reset();
            out.write("=?UTF-8?B?".getBytes());
            out.write(base64UtfPart.getBytes());
            out.write("?=".getBytes());
            ewUtf8PartBase64Full = out.toByteArray();

            out.reset();
            out.write("=?UTF-8?B?".getBytes());
            out.write(base64UtfFull.getBytes(), 0, 2);
            out.write("?=".getBytes());
            ewUtf8FullBase64Part = out.toByteArray();

            out.reset();
            out.write("=?UTF-8?B?".getBytes());
            out.write(base64UtfPart.getBytes(), 0, 2);
            out.write("?=".getBytes());
            ewUtf8PartBase64Part = out.toByteArray();

            cases = new Object[][] {
                    {false, false, "".getBytes("ISO8859-1"),
                        null, null, 0, null, null
                    },
                    {false, false, "=".getBytes("ISO8859-1"),
                        null, null, 0, null, null
                    },
                    {false, false, "=?".getBytes("ISO8859-1"),
                        null, null, 0, null, null
                    },
                    {false, false, "=??".getBytes("ISO8859-1"),
                        "", null, 0, null, null
                    },
                    {false, false, "=?-?".getBytes("ISO8859-1"),
                        "-", null, 0, null, null
                    },
                    {false, false, "=?tony-the-tiger?".getBytes("ISO8859-1"),
                        "TONY-THE-TIGER", null, 0, null, null
                    },
                    {false, false, "=?iso-8859-1?".getBytes("ISO8859-1"),
                        "ISO-8859-1", null, 0, null, null
                    },
                    {false, false, "=?\tiso-8859-1?".getBytes("ISO8859-1"),
                        null, null, 0, null, null
                    },
                    {false, false, "=?isö-8859-1?".getBytes("ISO8859-1"),
                        null, null, 0, null, null
                    },
                    {false, false, "=?iso-8859-1??".getBytes("ISO8859-1"),
                        "ISO-8859-1", "", 0, null, null
                    },
                    {false, false, "=?iso-8859-1?boo?".getBytes("ISO8859-1"),
                        "ISO-8859-1", "BOO", 0, null, null
                    },
                    {false, false, "=?iso-8859-1?b?".getBytes("ISO8859-1"),
                        "ISO-8859-1", "B", EncodedWords.ENC_BASE64, null, null
                    },
                    {false, false, "=?iso-8859-1?Q?".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, null, null
                    },
                    {false, false, "=?iso-8859-1?Q??".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "", ""
                    },
                    {true, false, "=?iso-8859-1?Q??=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "", ""
                    },
                    {false, false, "=?iso-8859-1?\tQ??=".getBytes("ISO8859-1"),
                        "ISO-8859-1", null, 0, null, null
                    },
                    {false, false, "=?iso-8859-1?Qö??=".getBytes("ISO8859-1"),
                        "ISO-8859-1", null, 0, null, null
                    },
                    {false, false, "=?iso-8859-1?Q??-".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "", ""
                    },
                    {true, false, "=?iso-8859-1?q?this=20is=20some=20text?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "this=20is=20some=20text",
                        "this is some text"
                    },
                    {true, false, "=?US-ASCII?Q?Keith_Moore?=".getBytes("ISO8859-1"),
                        "US-ASCII", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Keith_Moore",
                        "Keith Moore"
                    },
                    {true, false, "=?ISO-8859-1?Q?Keld_J=F8rn_Simonsen?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Keld_J=F8rn_Simonsen",
                        "Keld Jørn Simonsen"
                    },
                    {true, false, "=?ISO-8859-1?Q?Andr=E9?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Andr=E9",
                        "André"
                    },
                    {true, false, "=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "B", EncodedWords.ENC_BASE64, "SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=",
                        "If you can read this yo"
                    },
                    {true, false, "=?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=".getBytes("ISO8859-1"),
                        "ISO-8859-2", "B", EncodedWords.ENC_BASE64, "dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==",
                        "u understand the example."
                    },
                    {false, false, "=?ISO-8859-1?Q?Keld_J=FGrn_Simonsen?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Keld_J=FGrn_Simonsen",
                        null
                    },
                    {false, false, "=?ISO-8859-1?Q?Keld_J=G8rn_Simonsen?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Keld_J=G8rn_Simonsen",
                        null
                    },
                    {false, false, "=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHR(o)aXMgeW8=?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "B", EncodedWords.ENC_BASE64, "SWYgeW91IGNhbiByZWFkIHR(o)aXMgeW8=",
                        null
                    },
                    {false, false, "=?ISO-8859-1?Q?André?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, null,
                        null
                    },
                    {false, false, "=?ISO-8859-1?Q?\tAndr=E9?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, null,
                        null
                    },
                    {true, false, ewUtf8FullBase64Full,
                        "UTF-8", "B", EncodedWords.ENC_BASE64, base64UtfFull,
                        "æ"
                    },
                    {true, true, ewUtf8PartBase64Full,
                        "UTF-8", "B", EncodedWords.ENC_BASE64, base64UtfPart,
                        "?"
                    },
                    {false, false, ewUtf8FullBase64Part,
                        "UTF-8", "B", EncodedWords.ENC_BASE64, base64UtfFull.substring(0, 2),
                        null
                    },
                    {false, false, ewUtf8PartBase64Part,
                        "UTF-8", "B", EncodedWords.ENC_BASE64, base64UtfPart.substring(0, 2),
                        null
                    }
            };
            test_cases(cases, true);
            /*
             * Test with =? excluded in the parsing.
             */
            out.reset();
            out.write("UTF-8?B?".getBytes());
            out.write(base64UtfFull.getBytes());
            out.write("?=".getBytes());
            ewUtf8FullBase64Full = out.toByteArray();

            out.reset();
            out.write("UTF-8?B?".getBytes());
            out.write(base64UtfPart.getBytes());
            out.write("?=".getBytes());
            ewUtf8PartBase64Full = out.toByteArray();

            out.reset();
            out.write("UTF-8?B?".getBytes());
            out.write(base64UtfFull.getBytes(), 0, 2);
            out.write("?=".getBytes());
            ewUtf8FullBase64Part = out.toByteArray();

            out.reset();
            out.write("UTF-8?B?".getBytes());
            out.write(base64UtfPart.getBytes(), 0, 2);
            out.write("?=".getBytes());
            ewUtf8PartBase64Part = out.toByteArray();

            cases = new Object[][] {
                    {false, false, "".getBytes("ISO8859-1"),
                        null, null, 0, null, null
                    },
                    {false, false, "?".getBytes("ISO8859-1"),
                        "", null, 0, null, null
                    },
                    {false, false, "-?".getBytes("ISO8859-1"),
                        "-", null, 0, null, null
                    },
                    {false, false, "tony-the-tiger?".getBytes("ISO8859-1"),
                        "TONY-THE-TIGER", null, 0, null, null
                    },
                    {false, false, "iso-8859-1?".getBytes("ISO8859-1"),
                        "ISO-8859-1", null, 0, null, null
                    },
                    {false, false, "\tiso-8859-1?".getBytes("ISO8859-1"),
                        null, null, 0, null, null
                    },
                    {false, false, "isö-8859-1?".getBytes("ISO8859-1"),
                        null, null, 0, null, null
                    },
                    {false, false, "iso-8859-1??".getBytes("ISO8859-1"),
                        "ISO-8859-1", "", 0, null, null
                    },
                    {false, false, "iso-8859-1?boo?".getBytes("ISO8859-1"),
                        "ISO-8859-1", "BOO", 0, null, null
                    },
                    {false, false, "iso-8859-1?b?".getBytes("ISO8859-1"),
                        "ISO-8859-1", "B", EncodedWords.ENC_BASE64, null, null
                    },
                    {false, false, "iso-8859-1?Q?".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, null, null
                    },
                    {false, false, "iso-8859-1?Q??".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "", ""
                    },
                    {true, false, "iso-8859-1?Q??=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "", ""
                    },
                    {false, false, "iso-8859-1?\tQ??=".getBytes("ISO8859-1"),
                        "ISO-8859-1", null, 0, null, null
                    },
                    {false, false, "iso-8859-1?Qö??=".getBytes("ISO8859-1"),
                        "ISO-8859-1", null, 0, null, null
                    },
                    {false, false, "iso-8859-1?Q??-".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "", ""
                    },
                    {true, false, "iso-8859-1?q?this=20is=20some=20text?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "this=20is=20some=20text",
                        "this is some text"
                    },
                    {true, false, "US-ASCII?Q?Keith_Moore?=".getBytes("ISO8859-1"),
                        "US-ASCII", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Keith_Moore",
                        "Keith Moore"
                    },
                    {true, false, "ISO-8859-1?Q?Keld_J=F8rn_Simonsen?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Keld_J=F8rn_Simonsen",
                        "Keld Jørn Simonsen"
                    },
                    {true, false, "ISO-8859-1?Q?Andr=E9?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Andr=E9",
                        "André"
                    },
                    {true, false, "ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "B", EncodedWords.ENC_BASE64, "SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=",
                        "If you can read this yo"
                    },
                    {true, false, "ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=".getBytes("ISO8859-1"),
                        "ISO-8859-2", "B", EncodedWords.ENC_BASE64, "dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==",
                        "u understand the example."
                    },
                    {false, false, "ISO-8859-1?Q?Keld_J=FGrn_Simonsen?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Keld_J=FGrn_Simonsen",
                        null
                    },
                    {false, false, "ISO-8859-1?Q?Keld_J=G8rn_Simonsen?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, "Keld_J=G8rn_Simonsen",
                        null
                    },
                    {false, false, "ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHR(o)aXMgeW8=?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "B", EncodedWords.ENC_BASE64, "SWYgeW91IGNhbiByZWFkIHR(o)aXMgeW8=",
                        null
                    },
                    {false, false, "ISO-8859-1?Q?André?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, null,
                        null
                    },
                    {false, false, "ISO-8859-1?Q?\tAndr=E9?=".getBytes("ISO8859-1"),
                        "ISO-8859-1", "Q", EncodedWords.ENC_QUOTEDPRINTABLE, null,
                        null
                    },
                    {true, false, ewUtf8FullBase64Full,
                        "UTF-8", "B", EncodedWords.ENC_BASE64, base64UtfFull,
                        "æ"
                    },
                    {true, true, ewUtf8PartBase64Full,
                        "UTF-8", "B", EncodedWords.ENC_BASE64, base64UtfPart,
                        "?"
                    },
                    {false, false, ewUtf8FullBase64Part,
                        "UTF-8", "B", EncodedWords.ENC_BASE64, base64UtfFull.substring(0, 2),
                        null
                    },
                    {false, false, ewUtf8PartBase64Part,
                        "UTF-8", "B", EncodedWords.ENC_BASE64, base64UtfPart.substring(0, 2),
                        null
                    }
            };
            test_cases(cases, false);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected expection!");
        }
    }

    public void test_cases(Object[][] cases, boolean bParseEqQm) throws IOException {
        byte[] bytes;
        InputStream in;
        PushbackInputStream pbin;
        EncodedWords ew;

        for (int i=0; i<cases.length; ++i) {
            bytes = (byte[])cases[i][2];
            in = new ByteArrayInputStream(bytes);
            pbin = new PushbackInputStream(in, 16);
            ew = EncodedWords.parseEncodedWords(pbin, bParseEqQm);
            // debug
            // System.out.println(new String(bytes) + " -> " + ew.decoded_text );
            Assert.assertNotNull(ew);
            Assert.assertEquals(cases[i][0], ew.bIsValid);
            Assert.assertEquals(cases[i][1], ew.bConversionError);
            Assert.assertEquals(cases[i][3], ew.charsetStr);
            Assert.assertEquals(cases[i][4], ew.encodingStr);
            Assert.assertEquals(cases[i][5], ew.encoding);
            Assert.assertEquals(cases[i][6], ew.encoded_text);
            Assert.assertEquals(cases[i][7], ew.decoded_text);
        }
    }

}
