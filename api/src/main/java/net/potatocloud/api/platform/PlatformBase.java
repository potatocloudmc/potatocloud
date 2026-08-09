package net.potatocloud.api.platform;

import java.util.Arrays;

/**
 * Supported platform bases.
 */
public enum PlatformBase {

    /**
     * Bukkit.
     */
    BUKKIT("bukkit"),
    /**
     * Spigot.
     */
    SPIGOT("spigot"),
    /**
     * Paper.
     */
    PAPER("paper"),
    /**
     * Velocity.
     */
    VELOCITY("velocity"),
    /**
     * Limbo.
     */
    LIMBO("limbo"),
    /**
     * Fabric.
     */
    FABRIC("fabric"),
    /**
     * NeoForge.
     */
    NEOFORGE("neoforge"),
    /**
     * Unknown platform.
     */
    UNKNOWN("unknown");

    private final String id;

    PlatformBase(String id) {
        this.id = id;
    }

    /**
     * Returns the platform ID.
     *
     * @return the platform ID
     */
    public String id() {
        return id;
    }

    /**
     * Finds a platform base by ID.
     *
     * @param id the platform ID
     * @return the matching base, or {@link #UNKNOWN}
     */
    public static PlatformBase fromId(String id) {
        if (id == null || id.isBlank()) {
            return UNKNOWN;
        }
        return Arrays.stream(values()).filter(base -> base.id().equalsIgnoreCase(id)).findFirst().orElse(UNKNOWN);
    }
}
