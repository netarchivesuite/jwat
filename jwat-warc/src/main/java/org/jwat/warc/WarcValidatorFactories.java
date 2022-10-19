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

import org.jwat.common.TreeOfIntMapper;

/**
 * WARC validator factory lookup service.
 *
 * @author nicl
 */
public class WarcValidatorFactories {

    public static abstract class WarcValidatorFactory {
        /**
         * Returns a validator. The supplied version is the header it will be used on.
         * @param arr integer array denoting a version
         * @return a validator
         */
        public abstract WarcValidatorBase getValidatorFor(int[] arr);
    }

    /**
     * WARC/1.0 validator factory.
     */
    public static class WarcValidator10Factory extends WarcValidatorFactory {
        @Override
        public WarcValidator10 getValidatorFor(int[] arr) {
            return new WarcValidator10(arr);
        }
    }

    /**
     * WARC/1.1 validator factory.
     */
    public static class WarcValidator11Factory extends WarcValidatorFactory {
        @Override
        public WarcValidator11 getValidatorFor(int[] arr) {
            return new WarcValidator11(arr);
        }
    }

    /** Tree structure with validator factories store by their version denoted as an interger array */
    protected TreeOfIntMapper<WarcValidatorFactory> treeOfValidatorFactories;

    /**
     * Construct a WARC validator factory lookup service.
     */
    public WarcValidatorFactories() {
        treeOfValidatorFactories = new TreeOfIntMapper<>();
        treeOfValidatorFactories.add(new WarcValidator10Factory(), new int[] {0, 17});
        treeOfValidatorFactories.add(new WarcValidator10Factory(), new int[] {0, 18});
        treeOfValidatorFactories.add(new WarcValidator10Factory(), new int[] {1, 0});
        treeOfValidatorFactories.add(new WarcValidator11Factory(), new int[] {1, 1});
    }

    /**
     * Returns a WARC validator factory to create a validator for a specific WARC version.
     * @param arr int array denoting the WARC version to get a validator factory for
     * @return a WARC validator factory
     */
    public WarcValidatorFactory get(int[] arr) {
        WarcValidatorFactory vfac = treeOfValidatorFactories.lookup(arr);
        return vfac;
    }

}
