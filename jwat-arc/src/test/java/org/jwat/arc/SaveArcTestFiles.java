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
package org.jwat.arc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class is used to save ARC test files to disk so they can be used
 * to unit test applications that use the JWAT libraries for validation.
 *
 * @author nicl
 */
public class SaveArcTestFiles {

    public static final byte[] lfByte = new byte[] {0x0a};

    public static int saveTestArcHeaderInvalidCount = 1;
    public static int saveTestArcHeaderValidCount = 1;

    public static synchronized void saveTestArcHeader(byte[] bytes, boolean valid) throws IOException {
        byte[] filedesc = null;
        if (bytes.length != 0) {
            if (valid) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                switch (saveTestArcHeaderValidCount) {
                case 1:
                    filedesc = (
                            "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain 76\n"
                            + "1 0 InternetArchive\n"
                            + "URL IP-address Archive-date Content-type Archive-length\n").getBytes();
                    break;
                case 2:
                case 3:
                    filedesc = (
                            "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain 200 checksum location 1234 filename 122\n"
                            + "2 0 InternetArchive\n"
                            + "URL IP-address Archive-date Content-type Result-code Checksum Location Offset Filename Archive-length\n").getBytes();
                    break;
                }
                byte[] payload = (
                        "HTTP/1.1 404 Not Found\r\n"
                        + "Server: Mayhem WebServer/0.87 (JDK V1.6; Linux/2.6.32-trunk-686 i386)\r\n"
                        + "Date: Thu, 22 Sep 2011 13:12:13 GMT\r\n"
                        + "Connection: close\r\n"
                        + "Set-Cookie: JSESSIONID=243068592.7295797090016410982.4043211045; path=/; expires=Thursday, 22-Sep-2011 13:42:13 GMT\r\n"
                        + "Content-Length: 291\r\n"
                        + "Content-Type: text/html; charset=ISO-8859-1\r\n"
                        + "Content-Language: en\r\n"
                        + "\r\n"
                        + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n"
                        + "<HTML><HEAD>\r\n"
                        + "<TITLE>Not Found</TITLE>\r\n"
                        + "</HEAD><BODY>\r\n"
                        + "<H1>404 Not Found</H1>\r\n"
                        + "The requested URL /robots.txt was not found on this server.<P>\r\n"
                        + "<HR>\r\n"
                        + "<ADDRESS>Mayhem WebServer/0.87 Server at www.rebels.dk Port 80</ADDRESS>\r\n"
                        + "</BODY></HTML>\r\n"
                        ).getBytes();
                out.write(filedesc);
                out.write(lfByte);
                out.write(bytes);
                out.write(payload);
                out.write(lfByte);
                out.close();
                bytes = out.toByteArray();
                saveFile("valid-archeader-" + saveTestArcHeaderValidCount + ".arc", bytes);
                ++saveTestArcHeaderValidCount;
            } else {
                saveFile("invalid-archeader-" + saveTestArcHeaderInvalidCount + ".arc", bytes);
                ++saveTestArcHeaderInvalidCount;
            }
        }
    }

    public static int saveTestArcRecordInvalidCount = 1;
    public static int saveTestArcRecordValidCount = 1;

    public static synchronized void saveTestArcRecord(byte[] bytes, boolean valid) throws IOException {
        if (valid) {
            saveFile("valid-arcrecord-" + saveTestArcRecordValidCount + ".arc", bytes);
            ++saveTestArcRecordValidCount;
        } else {
            saveFile("invalid-arcrecord-" + saveTestArcRecordInvalidCount + ".arc", bytes);
            ++saveTestArcRecordInvalidCount;
        }
    }

    public static int saveTestArcRecordBaseInvalidCount = 1;
    public static int saveTestArcRecordBaseValidCount = 1;

    public static synchronized void saveTestArcRecordBase(byte[] bytes, boolean valid) throws IOException {
        if (valid) {
            saveFile("valid-arcrecordbase-" + saveTestArcRecordBaseValidCount + ".arc", bytes);
            ++saveTestArcRecordBaseValidCount;
        } else {
            saveFile("invalid-arcrecordbase-" + saveTestArcRecordBaseInvalidCount + ".arc", bytes);
            ++saveTestArcRecordBaseInvalidCount;
        }
    }

    public static int saveTestArcVersionBlockInvalidCount = 1;
    public static int saveTestArcVersionBlockValidCount = 1;

    public static synchronized void saveTestArcVersionBlock(byte[] bytes, boolean valid) throws IOException {
        if (valid) {
            saveFile("valid-versionblock-" + saveTestArcVersionBlockValidCount + ".arc", bytes);
            ++saveTestArcVersionBlockValidCount;
        } else {
            saveFile("invalid-versionblock-" + saveTestArcVersionBlockInvalidCount + ".arc", bytes);
            ++saveTestArcVersionBlockInvalidCount;
        }
    }

    public static int saveTestArcVersionHeaderInvalidCount = 1;
    public static int saveTestArcVersionHeaderValidCount = 1;

    public static synchronized void saveTestArcVersionHeader(byte[] bytes, boolean valid, int version) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] v1 = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain ".getBytes("ISO-8859-1");
        byte[] v2 = "filedesc://BNF-FS-072076.arc.gz 0.0.0.0 20060305082251 text/plain 200 checksum location 1234 filename ".getBytes("ISO-8859-1");

        out.reset();
        out.write(v1);
        out.write(Integer.toString(bytes.length).getBytes("ISO-8859-1"));
        out.write(lfByte);
        out.write(bytes);
        if (valid && (version == 10 || version == 11)) {
            saveFile("valid-versionheader-" + saveTestArcVersionHeaderValidCount + ".arc", out.toByteArray());
            ++saveTestArcVersionHeaderValidCount;
        } else {
            saveFile("invalid-versionheader-" + saveTestArcVersionHeaderInvalidCount + ".arc", out.toByteArray());
            ++saveTestArcVersionHeaderInvalidCount;
        }

        out.reset();
        out.write(v2);
        out.write(Integer.toString(bytes.length).getBytes("ISO-8859-1"));
        out.write(lfByte);
        out.write(bytes);
        if (valid && version == 20) {
            saveFile("valid-versionheader-" + saveTestArcVersionHeaderValidCount + ".arc", out.toByteArray());
            ++saveTestArcVersionHeaderValidCount;
        } else {
            saveFile("invalid-versionheader-" + saveTestArcVersionHeaderInvalidCount + ".arc", out.toByteArray());
            ++saveTestArcVersionHeaderInvalidCount;
        }
    }

    // Uncomment to save files.
    public static void saveFile(String fname, byte[] bytes) throws IOException {
        /*
        File file = new File(fname);
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0L);
        raf.setLength(0L);
        raf.write(bytes);
        raf.close();
        */
    }

}
