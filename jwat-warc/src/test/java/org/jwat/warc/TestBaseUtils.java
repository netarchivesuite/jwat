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
package org.jwat.warc;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.jwat.common.Diagnosis;
import org.jwat.common.DiagnosisType;

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

    public static void printDiagnoses(List<Diagnosis> diagnosisList) {
        Diagnosis diagnosis;
        for (int i=0; i<diagnosisList.size(); ++i) {
            diagnosis = diagnosisList.get(i);
            System.out.println("[" + i + "]: " + diagnosis.type + " - " + diagnosis.entity + " - " + diagnosis.information.length);
            for (int j=0; j<diagnosis.information.length; ++j) {
                System.out.println("    " + diagnosis.information[j]);
            }
        }
    }

    public static void printDiagnoses(Object[][] diagnosisArray) {
        for (int i=0; i<diagnosisArray.length; ++i) {
            System.out.println("[" + i + "]: " + (DiagnosisType)diagnosisArray[i][0] + " - " + (String)diagnosisArray[i][1] + " - " + (Integer)diagnosisArray[i][2]);
        }
    }

    public static void printRecord(WarcRecord record) {
        WarcHeader header = record.header;
        System.out.println("--------------");
        System.out.println("       Version: " + header.bMagicIdentified + " " + header.bVersionParsed + " " + header.major + "." + header.minor);
        System.out.println("       TypeIdx: " + header.warcTypeIdx);
        System.out.println("          Type: " + header.warcTypeStr);
        System.out.println("      Filename: " + header.warcFilename);
        System.out.println("     Record-ID: " + header.warcRecordIdUri);
        System.out.println("          Date: " + header.warcDate);
        System.out.println("Content-Length: " + header.contentLength);
        System.out.println("  Content-Type: " + header.contentType);
        System.out.println("     Truncated: " + header.warcTruncatedStr);
        System.out.println("   InetAddress: " + header.warcInetAddress);
        for (int i=0; i<header.warcConcurrentToList.size(); ++i) {
            System.out.println("  ConcurrentTo: " + header.warcConcurrentToList.get(i));
        }
        System.out.println("      RefersTo: " + header.warcRefersToUri);
        System.out.println("     TargetUri: " + header.warcTargetUriUri);
        System.out.println("   WarcInfo-Id: " + header.warcWarcinfoIdUri);
        System.out.println("   BlockDigest: " + header.warcBlockDigest);
        System.out.println(" PayloadDigest: " + header.warcPayloadDigest);
        System.out.println("IdentPloadType: " + header.warcIdentifiedPayloadType);
        System.out.println("       Profile: " + header.warcProfileStr);
        System.out.println("      Segment#: " + header.warcSegmentNumber);
        System.out.println(" SegmentOrg-Id: " + header.warcSegmentOriginIdUrl);
        System.out.println("SegmentTLength: " + header.warcSegmentTotalLength);
    }

    public static void printStatus(int records, int errors, int warnings) {
        System.out.println("--------------");
        System.out.println("       Records: " + records);
        System.out.println("        Errors: " + errors);
        System.out.println("      Warnings: " + warnings);
    }

    public static void printRecordErrors(WarcRecord record) {
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

}
