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

/**
 * WARC/1.1 specification validation relevant information.
 *
 * @author nicl
 */
public class WarcValidator11 extends WarcValidatorBase {

    public WarcValidator11(int[] arr) {
        setVersions(arr);
        field_policy = new int[RT_INDEX_OF_LAST][FN_INDEX_OF_LAST];
        setWarcHeaderPolicyMatrix();
    }

    @Override
    public void setWarcHeaderPolicyMatrix() {
        WarcValidator10.setWarc10HeaderPolicyMatrix(field_policy);
        // WARC-Refers-To-Target-URI
        // WARC-Refers-To-Date
        for (int i=1; i<=RT_NUMBER; ++i) {
            field_policy[i][FN_IDX_WARC_REFERS_TO_TARGET_URI] = POLICY_SHALL_NOT;
            field_policy[i][FN_IDX_WARC_REFERS_TO_DATE] = POLICY_SHALL_NOT;
        }
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_REFERS_TO_TARGET_URI] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_REFERS_TO_DATE] = POLICY_MAY;
    }

}
