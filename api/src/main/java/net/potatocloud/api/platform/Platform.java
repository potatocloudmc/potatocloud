package net.potatocloud.api.platform;

import net.potatocloud.api.CloudAPI;

import java.util.List;

public interface Platform {

    /**
     * Gets the name of the platform.
     *
     * @return the name of the platform
     */
    String getName();

    /**
     * Gets the download URL of the platform.
     *
     * @return the download URL of the platform
     */
    String getDownloadUrl();

    /**
     * Gets whether the group is custom.
     *
     * @return {@code true} if the platform is custom, otherwise {@code false}
     */
    boolean isCustom();

    /**
     * Gets whether the platform is a proxy.
     *
     * @return {@code true} if the platform is a proxy, otherwise {@code false}
     */
    boolean isProxy();

    /**
     * Gets the platform versions.
     *
     * @return the list of platform versions
     */
    List<PlatformVersion> getVersions();

    /**
     * Sets the platform versions.
     *
     * @param versions the list of platform versions
     */
    void setVersions(List<PlatformVersion> versions);

    /**
     * Gets the prepare steps of the platform.
     *
     * @return the list of prepare steps of the platform
     */
    List<String> getPrepareSteps();

    /**
     * Gets the base of the platform.
     *
     * @return the base of the platform
     */
    String getBase();

    /**
     * Gets the pre-cache builder of the platform.
     *
     * @return the pre-cache builder of the platform
     */
    String getPreCacheBuilder();

    /**
     * Gets the parser of the platform.
     *
     * @return the parser of the platform
     */
    String getParser();

    /**
     * Gets the hash type of the platform.
     *
     * @return the hash type of the platform
     */
    String getHashType();

    /**
     * Adds a version to the platform.
     *
     * @param version the version to add
     */
    void addVersion(PlatformVersion version);

    /**
     * Updates the platform.
     */
    default void update() {
        CloudAPI.getInstance().getPlatformManager().updatePlatform(this);
    }

    /**
     * Gets a version by its name.
     *
     * @param name the name of the version
     * @return the version
     */
    default PlatformVersion getVersion(String name) {
        return getVersions().stream()
                .filter(version -> version.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks whether the platform has a version with the given name.
     *
     * @param name the name of the version
     * @return {@code true} if the platform has a version with the given name, otherwise {@code false}
     */
    default boolean hasVersion(String name) {
        return getVersion(name) != null;
    }

    /**
     * Checks whether the platform is a bukkit based platform.
     *
     * @return {@code true} if the platform is a bukkit based platform, otherwise {@code false}
     */
    default boolean isBukkitBased() {
        return getBase().equalsIgnoreCase("bukkit") || getBase().equalsIgnoreCase("spigot") || isPaperBased();
    }

    /**
     * Checks whether the platform is a paper based platform.
     *
     * @return {@code true} if the platform is a paper based platform, otherwise {@code false}
     */
    default boolean isPaperBased() {
        return getBase().equalsIgnoreCase("paper");
    }

    /**
     * Checks whether the platform is a velocity based platform.
     *
     * @return {@code true} if the platform is a velocity based platform, otherwise {@code false}
     */
    default boolean isVelocityBased() {
        return getBase().equalsIgnoreCase("velocity");
    }

    /**
     * Checks whether the platform is a limbo based platform.
     *
     * @return {@code true} if the platform is a limbo based platform, otherwise {@code false}
     */
    default boolean isLimboBased() {
        return getBase().equalsIgnoreCase("limbo");
    }
}
