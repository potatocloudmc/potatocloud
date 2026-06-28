package net.potatocloud.api.property;

import java.util.Set;

/**
 * Common properties with built-in functionality.
 */
public final class DefaultProperties {

    private DefaultProperties() {
    }

    /**
     * Represents the current game state of a service.
     * <p>
     * When the value changes to {@code "INGAME"}, the cloud
     * automatically starts another service if required.
     */
    public static final PropertyKey<String> GAME_STATE = PropertyKey.of("gameState", "LOBBY");

    /**
     * Whether the service uses Velocity modern forwarding or not.
     * Only works when the service is running on Velocity platform.
     */
    public static final PropertyKey<Boolean> VELOCITY_MODERN_FORWARDING = PropertyKey.of("velocityModernForwarding", false);

    /**
     * Whether the cloud should always replace the configured Velocity forwarding secret with its own.
     */
    public static final PropertyKey<Boolean> ALWAYS_OVERRIDE_FORWARDING_SECRET = PropertyKey.of("alwaysOverrideForwardingSecret", true);

    private static final Set<PropertyKey<?>> VALUES =
            Set.of(GAME_STATE,
                    VELOCITY_MODERN_FORWARDING,
                    ALWAYS_OVERRIDE_FORWARDING_SECRET
            );

    /**
     * Returns all default properties.
     *
     * @return all default properties
     */
    public static Set<PropertyKey<?>> values() {
        return VALUES;
    }
}
