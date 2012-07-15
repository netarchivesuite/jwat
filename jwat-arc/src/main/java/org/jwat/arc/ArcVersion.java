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

/**
 * Supported ARC file versions.
 *
 * @author lbihanic,selghissassi
 */
public enum ArcVersion {

    /** Version 1.0 enum. */
    VERSION_1(1, 0, "version-1-block", "URL-record-v1"),
    /** Version 1.1 enum. */
    VERSION_1_1(1, 1, "version-1-block", "URL-record-v1"),
    /** Version 2.0 enum. */
    VERSION_2(2, 0, "version-2-block", "URL-record-v2");

    /** Major version number. */
    public final int major;

    /** Minor version number. */
    public final int minor;

    /** Version block field type. */
    public final String versionBlockType;

    /** Arc record field type. */
    public final String arcRecordType;

    /**
     * Enum constructor based on version specific parameters.
     * @param major major version number
     * @param minor minor version number
     * @param vBType version block field type
     * @param aRType arc record field type
     */
    private ArcVersion(int major, int minor, String vBType, String aRType){
        this.major = major;
        this.minor = minor;
        this.versionBlockType = vBType;
        this.arcRecordType = aRType;
    }

    /**
     * Given a version number return the corresponding <code>ArcVersion</code>
     * object or null.
     * @param major major version number
     * @param minor minor version number
     * @return <code>ArcVersion</code> object or null.
     */
    public static ArcVersion fromValues(int major, int minor) {
        ArcVersion version = null;
        for (ArcVersion v : ArcVersion.values()) {
            if ((v.major == major) && (v.minor == minor)) {
                version = v;
                break;
            }
        }
        return version;
    }

    @Override
    public String toString() {
        return "v" + this.major + '.' + this.minor;
    }

}
