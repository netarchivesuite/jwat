/**
 * JHOVE2 - Next-generation architecture for format-aware characterization
 *
 * Copyright (c) 2009 by The Regents of the University of California,
 * Ithaka Harbors, Inc., and The Board of Trustees of the Leland Stanford
 * Junior University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * o Neither the name of the University of California/California Digital
 *   Library, Ithaka Harbors/Portico, or Stanford University, nor the names of
 *   its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package dk.netarkivet.arc;

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
    ArcVersion(int major, int minor, String vBType, String aRType){
        if (major < 1) {
            throw new IllegalArgumentException("major");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("minor");
        }
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
