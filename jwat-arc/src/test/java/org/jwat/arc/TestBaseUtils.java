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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.jwat.common.Diagnosis;
import org.jwat.common.HttpHeader;

public class TestBaseUtils {

    private TestBaseUtils() {
    }

    public static void compareDiagnoses(Object[][] expectedDiagnoses, List<Diagnosis> diagnosisList) {
        Diagnosis diagnosis;
        // debug
        /*
        System.out.println(diagnosisList.size());
        for (int i=0; i<diagnosisList.size(); ++i) {
            diagnosis = diagnosisList.get(i);
            System.out.println(diagnosis.type);
            System.out.println(diagnosis.entity);
            System.out.println(diagnosis.information.length);
        }
        */
        Assert.assertEquals(expectedDiagnoses.length, diagnosisList.size());
        for (int i=0; i<expectedDiagnoses.length; ++i) {
            diagnosis = diagnosisList.get(i);
            Assert.assertEquals(expectedDiagnoses[i][0], diagnosis.type);
            Assert.assertEquals(expectedDiagnoses[i][1], diagnosis.entity);
            Assert.assertEquals(expectedDiagnoses[i][2], diagnosis.information.length);
        }
    }

    public static void printRecord(ArcRecordBase record) {
        ArcHeader header = record.header;
        System.out.println( "------------" );
        System.out.println( "         url: " + header.urlStr + " - " + header.urlUri );
        if ( header.urlUri != null ) {
            System.out.println( "              " + header.urlUri.getScheme() );
            System.out.println( "              " + header.urlUri.getSchemeSpecificPart() );
        }
        System.out.println( "      ipaddr: " + header.ipAddressStr + " - " + header.inetAddress );
        System.out.println( "        date: " + header.archiveDateStr + " - " + header.archiveDate );
        System.out.println( "content-type: " + header.contentTypeStr + " - " + header.contentType.toStringShort() );
        System.out.println( " result-code: " + header.resultCodeStr + " - " + header.resultCode );
        System.out.println( "    checksum: " + header.checksumStr );
        System.out.println( "    location: " + header.locationStr );
        System.out.println( "      offset: " + header.offsetStr + " - " + header.offset );
        System.out.println( "    filename: " + header.filenameStr );
        System.out.println( "      length: " + header.archiveLengthStr + " - " + header.archiveLength );
        if ( record.versionHeader != null ) {
            System.out.println( "       major: " + record.versionHeader.versionNumberStr + " - " + record.versionHeader.versionNumber );
            System.out.println( "       minor: " + record.versionHeader.reservedStr + " - " + record.versionHeader.reserved );
            System.out.println( "      origin: " + record.versionHeader.originCode );
            System.out.println( "     version: " + record.versionHeader.version );
            System.out.println( "     isValid: " + record.versionHeader.isValid() );
            System.out.println( "validVersion: " + record.versionHeader.isVersionValid );
            System.out.println( "ValidFldDesc: " + record.versionHeader.isValidBlockdDesc );
        }
        if (record.httpHeader != null ) {
            System.out.println( " result-code: " + record.httpHeader.statusCode );
            System.out.println( "protocol-ver: " + record.httpHeader.httpVersion );
            System.out.println( "content-type: " + record.httpHeader.contentType );
            System.out.println( " object-size: " + record.httpHeader.payloadLength );
            System.out.println( "     isValid: " + record.httpHeader.isValid() );
            //saveHttpResponse( arcRecord.recUrl, arcRecord.httpResponse );
        }
        System.out.println( "      errors: " + record.diagnostics.hasErrors() );
        System.out.println( "    warnings: " + record.diagnostics.hasWarnings() );
    }

    public static void printStatus(int records, int errors, int warnings) {
        System.out.println("------------");
        System.out.println("     Records: " + records);
        System.out.println("      Errors: " + errors);
        System.out.println("    Warnings: " + warnings);
    }

    public static void printRecordErrors(ArcRecordBase record) {
        List<Diagnosis> diagnosisList;
        Iterator<Diagnosis> diagnosisIterator;
        Diagnosis diagnosis;
        if (record.diagnostics.hasErrors()) {
            diagnosisList = record.diagnostics.getErrors();
            if (diagnosisList != null && diagnosisList.size() > 0) {
                diagnosisIterator = diagnosisList.iterator();
                while (diagnosisIterator.hasNext()) {
                    diagnosis = diagnosisIterator.next();
                    System.out.println( "Error" );
                    System.out.println( diagnosis.type );
                    System.out.println( diagnosis.entity );
                    if (diagnosis.information != null) {
                        for (int i=0; i<diagnosis.information.length; ++i) {
                            System.out.println( diagnosis.information[i] );
                        }
                    }
                }
            }
        }
        if (record.diagnostics.hasWarnings()) {
            diagnosisList = record.diagnostics.getWarnings();
            if (diagnosisList != null && diagnosisList.size() > 0) {
                diagnosisIterator = diagnosisList.iterator();
                while (diagnosisIterator.hasNext()) {
                    diagnosis = diagnosisIterator.next();
                    System.out.println( "Warning:" );
                    System.out.println( diagnosis.type );
                    System.out.println( diagnosis.entity );
                    if (diagnosis.information != null) {
                        for (int i=0; i<diagnosis.information.length; ++i) {
                            System.out.println( diagnosis.information[i] );
                        }
                    }
                }
            }
        }
    }

    public static void saveHttpResponse(String url, HttpHeader httpResponse) {
        if ( "200".equals(httpResponse.statusCode) && url != null && url.length() > 0 && httpResponse.payloadLength > 0L ) {
            if ( url.startsWith("http://") ) {
                int fidx = "http://".length();
                fidx = url.indexOf( '/', fidx );
                if ( fidx == -1 ) {
                    fidx = url.length();
                }
                if ( fidx < url.length() && url.charAt( fidx ) == '/' ) {
                    ++fidx;
                }
                int lidx = url.indexOf( '?', fidx );
                if ( lidx == -1 ) {
                     lidx = url.length();
                }
                if ( lidx > 0 && lidx > fidx && url.charAt( lidx - 1) == '/' ) {
                    --lidx;
                }
                String filename;
                if ( lidx == fidx ) {
                    filename = "_index.html";
                } else {
                    filename = url.substring( fidx, lidx ).replace( '/', '_' );
                }
                try {
                    byte[] bytes = new byte[ 1024 ];
                    int read;
                    File file = new File( "tmp/", filename );
                    System.out.println( "            > " + file.getPath() );
                    RandomAccessFile ram = new RandomAccessFile( file, "rw" );
                    ram.setLength( 0 );
                    ram.seek( 0 );
                    while ( (read = httpResponse.getPayloadInputStream().read( bytes )) != -1 ) {
                        ram.write( bytes, 0,  read );
                    }
                    ram.close();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
