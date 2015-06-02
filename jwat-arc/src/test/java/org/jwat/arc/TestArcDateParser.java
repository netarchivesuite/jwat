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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestArcDateParser {

    @Test
    public void test_arcdateparser() {
        /*
         * Date.
         */

        Date arcDate;

        arcDate = ArcDateParser.getDate(null);
        Assert.assertNull(arcDate);

        arcDate = ArcDateParser.getDate("");
        Assert.assertNull(arcDate);

        arcDate = ArcDateParser.getDate("fail");
        Assert.assertNull(arcDate);

        arcDate = ArcDateParser.getDate("yyyyMMddHHmmss");
        Assert.assertNull(arcDate);

        arcDate = ArcDateParser.getDate("20111224193000");
        Assert.assertNotNull(arcDate);

        Date date = new Date(0);
        String dateStr = ArcDateParser.getDateFormat().format(date);
        arcDate = ArcDateParser.getDate(dateStr);
        Assert.assertNull(arcDate);
    }

}
