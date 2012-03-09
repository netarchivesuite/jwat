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

import org.jwat.common.Diagnosis;

public class RecordDebugBase {

    private RecordDebugBase() {
    }

    public static void printRecord(WarcRecord record) {
        System.out.println("--------------");
        System.out.println("       Version: " + record.bMagicIdentified + " " + record.bVersionParsed + " " + record.major + "." + record.minor);
        System.out.println("       TypeIdx: " + record.warcTypeIdx);
        System.out.println("          Type: " + record.warcTypeStr);
        System.out.println("      Filename: " + record.warcFilename);
        System.out.println("     Record-ID: " + record.warcRecordIdUri);
        System.out.println("          Date: " + record.warcDate);
        System.out.println("Content-Length: " + record.contentLength);
        System.out.println("  Content-Type: " + record.contentType);
        System.out.println("     Truncated: " + record.warcTruncatedStr);
        System.out.println("   InetAddress: " + record.warcInetAddress);
        System.out.println("  ConcurrentTo: " + record.warcConcurrentToUriList);
        System.out.println("      RefersTo: " + record.warcRefersToUri);
        System.out.println("     TargetUri: " + record.warcTargetUriUri);
        System.out.println("   WarcInfo-Id: " + record.warcWarcInfoIdUri);
        System.out.println("   BlockDigest: " + record.warcBlockDigest);
        System.out.println(" PayloadDigest: " + record.warcPayloadDigest);
        System.out.println("IdentPloadType: " + record.warcIdentifiedPayloadType);
        System.out.println("       Profile: " + record.warcProfileStr);
        System.out.println("      Segment#: " + record.warcSegmentNumber);
        System.out.println(" SegmentOrg-Id: " + record.warcSegmentOriginIdUrl);
        System.out.println("SegmentTLength: " + record.warcSegmentTotalLength);
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
