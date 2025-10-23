package net.potatocloud.api.platform;

import net.potatocloud.api.CloudAPI;

public interface PlatformVersion {

    /**
     * Gets the name of the platform.
     *
     * @return the name of the platform
     */
    String getPlatformName();

    /**
     * Gets the name of the platform version.
     *
     * @return the name of the platform version
     */
    String getName();

    /**
     * Gets the download URL of the platform version.
     *
     * @return the download URL of the platform version
     */
    String getDownloadUrl();

    /**
     * Gets the hash of the platform version.
     *
     * @return the hash of the platform version
     */
    String getFileHash();

    /**
     * Checks whether the platform version is legacy.
     *
     * @return {@code true} if the platform version is legacy, otherwise {@code false}
     */
    boolean isLegacy();

    /**
     * Gets the full name of the platform version.
     *
     * @return the full name of the platform version
     */
    default String getFullName() {
        return getPlatformName() + "-" + getName();
    }

    /**
     * Gets the platform of the platform version.
     *
     * @return the platform of the platform version
     */
    default Platform getPlatform() {
        return CloudAPI.getInstance().getPlatformManager().getPlatform(getPlatformName());
    }
}
