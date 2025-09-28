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

    String getPreCacheBuilder();

    default PlatformVersion getVersion(String name) {
        return getVersions().stream()
                .filter(version -> version.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    default boolean isBukkitBased() {
        return getBase().equalsIgnoreCase("bukkit") || isPaperBased();
    }

    default boolean isPaperBased() {
        return getBase().equalsIgnoreCase("paper");
    }

    default boolean isVelocityBased() {
        return getBase().equalsIgnoreCase("velocity");
    }

    default boolean isLimboBased() {
        return getBase().equalsIgnoreCase("limbo");
    }
}
