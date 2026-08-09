package net.potatocloud.api.version;

/**
 * A simple version with an optional tag.
 *
 * @param major the major version
 * @param minor the minor version
 * @param patch the patch version
 * @param tag the optional version tag
 */
public record Version(int major, int minor, int patch, String tag) implements Comparable<Version> {

    /**
     * Creates a version without a tag.
     *
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     * @return the created version
     */
    public static Version of(int major, int minor, int patch) {
        return new Version(major, minor, patch, null);
    }

    /**
     * Creates a version with a tag.
     *
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     * @param tag the version tag
     * @return the created version
     */
    public static Version of(int major, int minor, int patch, String tag) {
        return new Version(major, minor, patch, tag);
    }

    /**
     * Parses a version string such as {@code 1.2.3} or {@code v1.2.3-beta}.
     *
     * @param value the value to parse
     * @return the parsed version, or {@code null} when the value is invalid
     */
    public static Version fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String version = value.trim();

        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1);
        }

        String tag = null;
        final String[] split = version.split("-", 2);
        version = split[0];

        if (split.length == 2) {
            tag = split[1];
        }

        final String[] parts = version.split("\\.");

        if (parts.length != 3) {
            return null;
        }

        try {
            return new Version(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    tag
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Compares this version with another version.
     *
     * @param other the version to compare with
     * @return a negative value, zero, or a positive value when this version
     *         is lower than, equal to, or higher than the other version
     */
    @Override
    public int compareTo(Version other) {
        if (major != other.major) {
            return Integer.compare(major, other.major);
        }
        if (minor != other.minor) {
            return Integer.compare(minor, other.minor);
        }
        if (patch != other.patch) {
            return Integer.compare(patch, other.patch);
        }
        if (tag == null && other.tag == null) {
            return 0;
        }
        if (tag == null) {
            return 1;
        }
        if (other.tag == null) {
            return -1;
        }
        return tag.compareToIgnoreCase(other.tag);
    }

    /**
     * Returns this version as text.
     *
     * @return the version text
     */
    @Override
    public String toString() {
        return tag == null ? major + "." + minor + "." + patch : major + "." + minor + "." + patch + "-" + tag;
    }
}
