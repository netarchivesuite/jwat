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
 * WARC/1.0 specification validation relevant information.
 *
 * @author nicl
 */
public class WarcValidator10 extends WarcValidatorBase {

    public WarcValidator10(int[] arr) {
        setVersions(arr);
        field_policy = new int[RT_INDEX_OF_LAST][FN_INDEX_OF_LAST];
        setWarcHeaderPolicyMatrix();
    }

    /**
     * Initialize a multi-array with the WARC/1.0 type/header policies.
     * @param field_policy policy multi-array to initialize
     */
    public static final void setWarc10HeaderPolicyMatrix(int[][] field_policy) {
        // Warc-Record-id
        // Warc-Type
        // Warc-Date
        // Content-Length
        // Also required for unknown warc-types.
        for (int i=0; i<=RT_NUMBER; ++i) {
            field_policy[i][FN_IDX_WARC_RECORD_ID] = POLICY_MANDATORY;
            field_policy[i][FN_IDX_WARC_TYPE] = POLICY_MANDATORY;
            field_policy[i][FN_IDX_WARC_DATE] = POLICY_MANDATORY;
            field_policy[i][FN_IDX_CONTENT_LENGTH] = POLICY_MANDATORY;
        }

        // Content-Type
        field_policy[RT_IDX_CONTINUATION][FN_IDX_CONTENT_TYPE] = POLICY_SHALL_NOT;

        // Warc-Ip-Address
        field_policy[RT_IDX_REQUEST][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_RESPONSE][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_RESOURCE][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_METADATA][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_IP_ADDRESS] = POLICY_MAY;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_IP_ADDRESS] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONVERSION][FN_IDX_WARC_IP_ADDRESS] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_IP_ADDRESS] = POLICY_SHALL_NOT;

        // Warc-Concurrent-To
        field_policy[RT_IDX_REQUEST][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_RESPONSE][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_RESOURCE][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_METADATA][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_CONCURRENT_TO] = POLICY_MAY;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_CONCURRENT_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONVERSION][FN_IDX_WARC_CONCURRENT_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_CONCURRENT_TO] = POLICY_SHALL_NOT;

        // Warc-Refers-To
        field_policy[RT_IDX_METADATA][FN_IDX_WARC_REFERS_TO] = POLICY_MAY;
        field_policy[RT_IDX_CONVERSION][FN_IDX_WARC_REFERS_TO] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_REFERS_TO] = POLICY_MAY;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_REQUEST][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_RESPONSE][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_RESOURCE][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_REFERS_TO] = POLICY_SHALL_NOT;

        // Warc-Target-Uri
        field_policy[RT_IDX_REQUEST][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_RESPONSE][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_RESOURCE][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_CONVERSION][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL;
        field_policy[RT_IDX_METADATA][FN_IDX_WARC_TARGET_URI] = POLICY_MAY;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_TARGET_URI] = POLICY_SHALL_NOT;

        // Warc-Warcinfo-Id
        // Warc-Filename
        // Warc-Profile
        // Warc-Segment-Origin-Id
        // Warc-Segment-Total-Length
        for (int i=1; i<=RT_NUMBER; ++i) {
            field_policy[i][FN_IDX_WARC_WARCINFO_ID] = POLICY_MAY;
            field_policy[i][FN_IDX_WARC_FILENAME] = POLICY_SHALL_NOT;
            field_policy[i][FN_IDX_WARC_PROFILE] = POLICY_IGNORE;
            field_policy[i][FN_IDX_WARC_SEGMENT_ORIGIN_ID] = POLICY_SHALL_NOT;
            field_policy[i][FN_IDX_WARC_SEGMENT_ORIGIN_ID] = POLICY_SHALL_NOT;
        }
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_WARCINFO_ID] = POLICY_MAY_NOT;
        field_policy[RT_IDX_WARCINFO][FN_IDX_WARC_FILENAME] = POLICY_MAY;
        field_policy[RT_IDX_REVISIT][FN_IDX_WARC_PROFILE] = POLICY_MANDATORY;
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_SEGMENT_ORIGIN_ID] = POLICY_MANDATORY;

        // Warc-Segment-Number
        field_policy[RT_IDX_CONTINUATION][FN_IDX_WARC_SEGMENT_NUMBER] = POLICY_MANDATORY;
    }

    @Override
    public void setWarcHeaderPolicyMatrix() {
        setWarc10HeaderPolicyMatrix(field_policy);
        // WARC-Refers-To-Target-URI
        // WARC-Refers-To-Date
        for (int i=1; i<=RT_NUMBER; ++i) {
            field_policy[i][FN_IDX_WARC_REFERS_TO_TARGET_URI] = POLICY_IGNORE;
            field_policy[i][FN_IDX_WARC_REFERS_TO_DATE] = POLICY_IGNORE;
        }
    }

}
