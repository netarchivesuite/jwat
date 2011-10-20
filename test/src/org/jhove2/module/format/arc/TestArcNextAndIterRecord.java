package org.jhove2.module.format.arc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestArcNextAndIterRecord {

    private int expected_records;
    private String arcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {101, "/home/test/QUICKSTART/oldjobs/1_1316696892071/arcs/1-1-20110922131213-00000-svc-VirtualBox.arc"},
                {5, "/home/nicl/BnF/jhove2-bnf/src/test/resources/examples/arc/small_BNF.arc"},
                {238, "/home/test/QUICKSTART/oldjobs/4_1317731601951/arcs/4-3-20111004123336-00000-svc-VirtualBox.arc"},
                {299, "/home/nicl/Downloads/IAH-20080430204825-00000-blackbook.arc"}
        });
    }

    public TestArcNextAndIterRecord(int records, String arcFile) {
        this.expected_records = records;
        this.arcFile = arcFile;
    }

    @Test
    public void test() {
        File file = new File( arcFile );
        InputStream in;

        ArcParser parser;
        ArcVersionBlock version;
        ArcRecord arcRecord;

        int n_records = 0;
        int n_errors = 0;

        int i_records = 0;
        int i_errors = 0;

        try {
            /*
             * getNextArcRecord.
             */

            in = new FileInputStream( file );

            parser = new ArcParser( in );
            version = parser.getVersionBlock();

            if ( version != null ) {
                TestArc.printVersionBlock( version );

                boolean b = true;
                while ( b ) {
                    arcRecord = parser.getNextArcRecord();
                    if ( arcRecord != null ) {
                        TestArc.printRecord( arcRecord );

                        ++n_records;

                        if ( arcRecord.hasErrors() ) {
                            n_errors += arcRecord.getWarnings().size();
                        }
                    }
                    else {
                        b = false;
                    }
                }
                System.out.println( "------------" );
                System.out.println( "     Records: " + n_records );
            }

            parser.close();
            in.close();

            /*
             * Iterator.
             */

            in = new FileInputStream( file );

            parser = new ArcParser( in );
            version = parser.getVersionBlock();

            if ( version != null ) {
                TestArc.printVersionBlock( version );

                boolean b = true;
                while ( b ) {
                    arcRecord = parser.getNextArcRecord();
                    if ( arcRecord != null ) {
                        TestArc.printRecord( arcRecord );

                        ++i_records;

                        if ( arcRecord.hasErrors() ) {
                            i_errors += arcRecord.getWarnings().size();
                        }
                    }
                    else {
                        b = false;
                    }
                }
                System.out.println( "------------" );
                System.out.println( "     Records: " + i_records );
            }

            parser.close();
            in.close();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assert.assertEquals(n_records, i_records);
        Assert.assertEquals(n_errors, i_errors);

        Assert.assertEquals(expected_records, n_records);
        Assert.assertEquals(expected_records, i_records);

        Assert.assertEquals(0, n_errors);
        Assert.assertEquals(0, i_errors);
    }

}
