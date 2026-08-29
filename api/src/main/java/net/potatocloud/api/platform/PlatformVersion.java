package net.potatocloud.api.platform;

/**
 * Represents one version of a server platform.
 */
public interface PlatformVersion {

    /**
     * Gets the name of the platform version.
     *
     * @return the name of the platform version
     */
    String name();

    /**
     * Gets the actual version name when using an alias such as {@code latest}.
     *
     * @return the actual version name
     */
    default String resolvedName() {
        return name();
    }

    /**
     * Checks whether the platform version is local.
     * <p>
     * Local versions are not downloaded automatically.
     * Instead, the required JAR file must already exist in the platform directory.
     *
     * @return {@code true} if the platform version uses a local file, otherwise {@code false}
     */
    boolean local();

    /**
     * Gets the download URL of the platform version.
     *
     * @return the download URL of the platform version
     */
    String downloadUrl();

    /**
     * Gets the hash of the platform version.
     *
     * @return the hash of the platform version
     */
    String fileHash();

    /**
     * Checks whether the platform version is legacy.
     *
     * @return {@code true} if the platform version is legacy, otherwise {@code false}
     */
    boolean legacy();

    /**
     * Gets the full name of the platform version.
     *
     * @return the full name of the platform version
     */
    default String fullName() {
        return platform().name() + "-" + name();
    }

    /**
     * Gets the platform of the platform version.
     *
     * @return the platform of the platform version
     * @throws IllegalStateException if the owning platform cannot be resolved
     */
    Platform platform();
}
