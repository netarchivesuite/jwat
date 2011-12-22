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
    ArcVersion(int major, int minor, String vBType, String aRType){
        if (major < 1) {
            throw new IllegalArgumentException("Parameter 'major' < 1");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("Parameter 'minor' < 0");
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
