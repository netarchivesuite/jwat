package org.jwat.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestInvalid {

    @Test
    public void test() throws IOException {
        InputStream in;
        GzipInputStream gzin;
        GzipEntry entry;

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-compression.gz");
        gzin = new GzipInputStream(in);
        try {
            while ((entry = gzin.getNextEntry()) != null) {
                gzin.closeEntry();
            }
            gzin.close();
            Assert.fail("Exception expected!");
        }
        catch (ZipException e) {
        }
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-entries.gz");
        gzin = new GzipInputStream(in);
        while ((entry = gzin.getNextEntry()) != null) {
            gzin.closeEntry();
        }
        gzin.close();
        in.close();

        in = this.getClass().getClassLoader().getResourceAsStream("invalid-magic.gz");
        gzin = new GzipInputStream(in);
        try {
            while ((entry = gzin.getNextEntry()) != null) {
                gzin.closeEntry();
            }
            gzin.close();
            Assert.fail("Exception expected!");
        }
        catch (ZipException e) {
        }
        in.close();
    }

}
