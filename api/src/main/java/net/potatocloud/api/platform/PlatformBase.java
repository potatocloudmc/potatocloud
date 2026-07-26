package net.potatocloud.api.platform;

import java.util.Arrays;

public enum PlatformBase {

    BUKKIT("bukkit"),
    SPIGOT("spigot"),
    PAPER("paper"),
    VELOCITY("velocity"),
    LIMBO("limbo"),
    FABRIC("fabric"),
    NEOFORGE("neoforge"),
    UNKNOWN("unknown");

    private final String id;

    PlatformBase(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static PlatformBase fromId(String id) {
        if (id == null || id.isBlank()) {
            return UNKNOWN;
        }
        return Arrays.stream(values()).filter(base -> base.id().equalsIgnoreCase(id)).findFirst().orElse(UNKNOWN);
    }
}
