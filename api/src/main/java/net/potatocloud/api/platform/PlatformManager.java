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
}
