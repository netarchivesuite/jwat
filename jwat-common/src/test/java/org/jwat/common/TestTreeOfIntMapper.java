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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestTreeOfIntMapper {

    @Test
    public void test_treeofintmapper() {
        TreeOfIntMapper<String> toiMap = new TreeOfIntMapper<>();
        String str;

        toiMap.add("0.18", new int[] {0, 18});
        toiMap.add("0.19", new int[] {0, 19});
        toiMap.add("1.0", new int[] {1, 0});
        toiMap.add("1.1", new int[] {1, 1});

        /*
        toiMap.add("1.0", 1, 0);
        toiMap.add("1.1", 1, 1);
        toiMap.add("0.18", 0, 18);
        toiMap.add("0.19", 0, 19);
        */

        str = toiMap.toString();
        System.out.println(str);

        Assert.assertEquals("0.18", toiMap.lookup( new int[] {0, 18}));
        Assert.assertEquals("0.19", toiMap.lookup( new int[] {0, 19}));
        Assert.assertEquals("1.0", toiMap.lookup( new int[] {1, 0}));
        Assert.assertEquals("1.1", toiMap.lookup( new int[] {1, 1}));
    }

}
