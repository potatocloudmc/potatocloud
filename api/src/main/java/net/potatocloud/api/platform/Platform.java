package net.potatocloud.api.platform;

import net.potatocloud.api.CloudAPI;

import java.util.List;

public interface Platform {

    String getName();

    String getDownloadUrl();

    boolean isCustom();

    boolean isProxy();

    List<PlatformVersion> getVersions();

    void setVersions(List<PlatformVersion> versions);

    List<String> getPrepareSteps();

    String getBase();

    String getPreCacheBuilder();

    String getParser();

    String getHashType();

    void addVersion(PlatformVersion version);

    default void update() {
        CloudAPI.getInstance().getPlatformManager().updatePlatform(this);
    }

    default PlatformVersion getVersion(String name) {
        return getVersions().stream()
                .filter(version -> version.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    default boolean hasVersion(String name) {
        return getVersion(name) != null;
    }

    default boolean isBukkitBased() {
        return getBase().equalsIgnoreCase("bukkit") || getBase().equalsIgnoreCase("spigot") || isPaperBased();
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
