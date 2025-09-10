package net.potatocloud.api.platform;

import java.util.List;

public interface Platform {

    String getName();

    String getDownloadUrl();

    boolean isCustom();

    boolean isProxy();

    List<PlatformVersion> getVersions();

    List<String> getPrepareSteps();

    String getBase();

    default PlatformVersion getVersion(String name) {
        return getVersions().stream()
                .filter(version -> version.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    default boolean isBukkit() {
        return getBase().equalsIgnoreCase("bukkit");
    }

    default boolean isVelocity() {
        return getBase().equalsIgnoreCase("velocity");
    }
}
