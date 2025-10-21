package net.potatocloud.api.platform;

import java.util.List;

public interface PlatformManager {

    List<Platform> getPlatforms();

    default Platform getPlatform(String name) {
        return getPlatforms().stream().filter(platform -> platform.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    default boolean exists(String name) {
        return getPlatform(name) != null;
    }

    Platform createPlatform(String name, String downloadUrl, boolean custom, boolean isProxy, String base, String preCacheBuilder, String parser, String hashType, List<String> prepareSteps);

    void updatePlatform(Platform platform);

}
