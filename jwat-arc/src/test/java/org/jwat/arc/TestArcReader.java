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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestArcReader {

    @Test
    public void test_arcreader_iterator_exceptions() {
        ArcReader reader = new ArcReader() {
            @Override
            public boolean isCompressed() {
                return false;
            }
            @Override
            public void close() {
            }
            @Override
            protected void recordClosed() {
            }
            @Override
            public long getStartOffset() {
                return 0;
            }
            @Override
            public long getOffset() {
                return 0;
            }
            @Override
            public long getConsumed() {
                return 0;
            }
            @Override
            public ArcRecordBase getNextRecord() throws IOException {
                throw new IOException();
            }
            @Override
            public ArcRecordBase getNextRecordFrom(InputStream in,
                    long offset) throws IOException {
                return null;
            }
            @Override
            public ArcRecordBase getNextRecordFrom(InputStream in,
                    long offset, int buffer_size) throws IOException {
                return null;
            }
        };
        Iterator<ArcRecordBase> iter = reader.iterator();

        Assert.assertNull(reader.iteratorExceptionThrown);
        iter.hasNext();
        Assert.assertNotNull(reader.iteratorExceptionThrown);
        try {
            iter.next();
            Assert.fail("Exception expected!");
        } catch (NoSuchElementException e) {
        }
        Assert.assertNotNull(reader.iteratorExceptionThrown);
    }

}
