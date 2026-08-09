package net.potatocloud.api.platform;

import java.util.List;
import java.util.Optional;

/**
 * Represents the platform manager.
 */
public interface PlatformManager {

    /**
     * Gets all platforms.
     *
     * @return a list of all platforms
     */
    List<Platform> platforms();

    /**
     * Gets a platform by its name.
     *
     * @param name the name of the platform
     * @return the platform, or an empty optional if not found
     */
    Optional<Platform> find(String name);

    /**
     * Checks whether a platform with the given name exists.
     *
     * @param name the name of the platform
     * @return {@code true} if the platform exists, otherwise {@code false}
     */
    default boolean exists(String name) {
        return find(name).isPresent();
    }

    /**
     * Creates a builder for a new platform.
     *
     * @param name the name of the platform
     * @return the builder
     */
    default PlatformBuilder builder(String name) {
        return new PlatformBuilder(name);
    }

    /**
     * Creates a new platform from the given object.
     *
     * @param platform the platform to create
     */
    void create(Platform platform);

    /**
     * Deletes the given platform.
     *
     * @param platform the platform to delete
     */
    void delete(Platform platform);

    /**
     * Updates an existing platform.
     *
     * @param platform the platform to update
     */
    void update(Platform platform);
}
