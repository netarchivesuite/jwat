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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWarcConstants {

    @Test
    public void test_warcconstants() {
        WarcConstants constants = new WarcConstants();
        Assert.assertNotNull(constants);

        Assert.assertEquals(WarcConstants.FN_INDEX_OF_LAST, WarcConstants.FN_IDX_STRINGS.length);
        Assert.assertEquals(WarcConstants.FN_NUMBER, WarcConstants.fieldNameIdxMap.size());
        Assert.assertEquals(WarcConstants.FN_INDEX_OF_LAST, WarcConstants.FN_IDX_DT.length);
        Assert.assertEquals(WarcConstants.FN_INDEX_OF_LAST, WarcConstants.fieldNamesRepeatableLookup.length);
        Assert.assertEquals(WarcConstants.RT_INDEX_OF_LAST, WarcConstants.RT_IDX_STRINGS.length);
        Assert.assertEquals(WarcConstants.RT_NUMBER, WarcConstants.recordTypeIdxMap.size());
        Assert.assertEquals(WarcConstants.RT_INDEX_OF_LAST, WarcConstants.field_policy.length);
    }

}
