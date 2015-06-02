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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestArcVersion {

    @Test
    public void test_arcversion() {
        String tmpStr;
        /*
         * ArcVersion.
         */

        ArcVersion av;

        av = ArcVersion.VERSION_1;
        Assert.assertNotNull(av);
        tmpStr = av.toString();
        Assert.assertNotNull(tmpStr);

        av = ArcVersion.VERSION_1_1;
        Assert.assertNotNull(av);
        tmpStr = av.toString();
        Assert.assertNotNull(tmpStr);

        av = ArcVersion.VERSION_2;
        Assert.assertNotNull(av);
        tmpStr = av.toString();
        Assert.assertNotNull(tmpStr);

        Assert.assertNull(ArcVersion.fromValues(0, 0));
        Assert.assertNull(ArcVersion.fromValues(0, 1));
        Assert.assertNull(ArcVersion.fromValues(0, 9));
        Assert.assertNull(ArcVersion.fromValues(1, 2));
        Assert.assertNull(ArcVersion.fromValues(1, 9));
        Assert.assertNull(ArcVersion.fromValues(2, 1));

        Assert.assertEquals(ArcVersion.VERSION_1, ArcVersion.fromValues(1, 0));
        Assert.assertEquals(ArcVersion.VERSION_1_1, ArcVersion.fromValues(1, 1));
        Assert.assertEquals(ArcVersion.VERSION_2, ArcVersion.fromValues(2, 0));

        /*
        try {
            Constructor<ArcVersion> arcVersionConstructor = ArcVersion.class.getDeclaredConstructor(int.class, int.class, String.class, String.class);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        */

        /*
        try {
            Constructor[] constructors = ArcVersion.class.getDeclaredConstructors();
            constructors[0].setAccessible(true);
            constructors[0].newInstance(0, 0, null, null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        */
    }

}
