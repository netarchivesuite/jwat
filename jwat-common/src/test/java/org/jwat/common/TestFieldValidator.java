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
public class TestFieldValidator {

    @Test
    public void test_fieldvalidator() {
        FieldValidator fv = new FieldValidator();
        Assert.assertNotNull(fv);

        String[] names = new String[] {"a", "b", "c", "d"};
        fv = FieldValidator.prepare(names);
        Assert.assertNotNull(fv);
        Assert.assertArrayEquals(names, fv.fieldNames);

        Assert.assertEquals(names[0], FieldValidator.getArrayValue(names, 0));
        Assert.assertEquals(names[1], FieldValidator.getArrayValue(names, 1));
        Assert.assertEquals(names[2], FieldValidator.getArrayValue(names, 2));
        Assert.assertEquals(names[3], FieldValidator.getArrayValue(names, 3));
        Assert.assertNull(names[3], FieldValidator.getArrayValue(names, 4));

        String[] nulls = new String[] {null, null, null, null};

        Assert.assertEquals(nulls[0], FieldValidator.getArrayValue(nulls, 0));
        Assert.assertEquals(nulls[1], FieldValidator.getArrayValue(nulls, 1));
        Assert.assertEquals(nulls[2], FieldValidator.getArrayValue(nulls, 2));
        Assert.assertEquals(nulls[3], FieldValidator.getArrayValue(nulls, 3));
        Assert.assertNull(FieldValidator.getArrayValue(nulls, 4));

        String[] empty = new String[0];
        Assert.assertNull(FieldValidator.getArrayValue(empty, 0));
    }

}
