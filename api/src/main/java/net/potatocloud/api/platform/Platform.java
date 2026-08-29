package net.potatocloud.api.platform;

import java.util.List;
import java.util.Optional;

/**
 * Represents a server platform.
 */
public interface Platform {

    /**
     * Gets the name of the platform.
     *
     * @return the name of the platform
     */
    String name();

    /**
     * Gets the download URL of the platform.
     *
     * @return the download URL of the platform
     */
    String downloadUrl();

    /**
     * Gets whether the platform is custom.
     *
     * @return {@code true} if the platform is custom, otherwise {@code false}
     */
    boolean custom();

    /**
     * Gets whether the platform is a proxy.
     *
     * @return {@code true} if the platform is a proxy, otherwise {@code false}
     */
    boolean proxy();

    /**
     * Gets the platform versions.
     *
     * @return the list of platform versions
     */
    List<PlatformVersion> versions();

    /**
     * Sets the platform versions.
     *
     * @param versions the list of platform versions
     */
    void versions(List<PlatformVersion> versions);

    /**
     * Gets the prepare steps of the platform.
     *
     * @return the list of prepare steps of the platform
     */
    List<String> prepareSteps();

    /**
     * Gets the base of the platform.
     *
     * @return the base of the platform
     */
    PlatformBase base();

    /**
     * Gets the pre-cache builder of the platform.
     *
     * @return the pre-cache builder of the platform
     */
    String preCacheBuilder();

    /**
     * Gets the parser of the platform.
     *
     * @return the parser of the platform
     */
    String parser();

    /**
     * Gets the hash type of the platform.
     *
     * @return the hash type of the platform
     */
    String hashType();

    /**
     * Adds a version to the platform.
     *
     * @param version the version to add
     */
    void addVersion(PlatformVersion version);

    /**
     * Gets a version by its name.
     *
     * @param name the name of the version
     * @return the version, or an empty optional if not found
     */
    default Optional<PlatformVersion> version(String name) {
        return versions().stream()
                .filter(version -> version.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Checks whether the platform has a version with the given name.
     *
     * @param name the name of the version
     * @return {@code true} if the platform has a version with the given name, otherwise {@code false}
     */
    default boolean hasVersion(String name) {
        return version(name).isPresent();
    }

    /**
     * Checks whether the platform is a Bukkit-based platform.
     *
     * @return {@code true} if the platform is Bukkit-based, otherwise {@code false}
     */
    default boolean bukkitBased() {
        return base() == PlatformBase.BUKKIT || base() == PlatformBase.SPIGOT || paperBased();
    }

    /**
     * Checks whether the platform is a paper-based platform.
     *
     * @return {@code true} if the platform is a paper-based platform, otherwise {@code false}
     */
    default boolean paperBased() {
        return base() == PlatformBase.PAPER;
    }

    /**
     * Checks whether the platform is a velocity-based platform.
     *
     * @return {@code true} if the platform is a Velocity-based platform, otherwise {@code false}
     */
    default boolean velocityBased() {
        return base() == PlatformBase.VELOCITY;
    }

    /**
     * Checks whether the platform is a limbo-based platform.
     *
     * @return {@code true} if the platform is a Limbo-based platform, otherwise {@code false}
     */
    default boolean limboBased() {
        return base() == PlatformBase.LIMBO;
    }

    /**
     * Checks whether the platform is a Fabric-based platform.
     *
     * @return {@code true} if the platform is a Fabric-based platform, otherwise {@code false}
     */
    default boolean fabricBased() {
        return base() == PlatformBase.FABRIC;
    }

    /**
     * Checks whether the platform is a NeoForge-based platform.
     *
     * @return {@code true} if the platform is a NeoForge-based platform, otherwise {@code false}
     */
    default boolean neoForgeBased() {
        return base() == PlatformBase.NEOFORGE;
    }

    /**
     * Checks whether the platform is a modded platform.
     *
     * @return {@code true} if the platform is a modded platform, otherwise {@code false}
     */
    default boolean moddedBased() {
        return fabricBased() || neoForgeBased();
    }
}
