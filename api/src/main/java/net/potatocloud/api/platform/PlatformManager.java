package net.potatocloud.api.platform;

import java.util.List;

public interface PlatformManager {

    /**
     * Gets all platforms
     *
     * @return a list of all platforms
     */
    List<Platform> getPlatforms();

    /**
     * Gets a platform by its name
     *
     * @param name the name of the platform
     * @return the platform
     */
    default Platform getPlatform(String name) {
        return getPlatforms().stream().filter(platform -> platform.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Checks if a platform exists by name.
     *
     * @param name the name of the platform
     * @return {@code true} if the platform exists, otherwise {@code false}
     */
    default boolean exists(String name) {
        return getPlatform(name) != null;
    }

    /**
     * Creates a new platform with the given configuration.
     */
    Platform createPlatform(String name, String downloadUrl, boolean custom, boolean isProxy, String base, String preCacheBuilder, String parser, String hashType, List<String> prepareSteps);

    /**
     * Updates an existing platform.
     *
     * @param platform the platform to update
     */
    void updatePlatform(Platform platform);

}
