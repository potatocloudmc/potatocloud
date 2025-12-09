package net.potatocloud.api.property;

import java.util.Set;

public class DefaultProperties {

    // Shows the current game state of the service
    // When the game state changes to "INGAME", the cloud automatically starts a new service
    public static final Property<String> GAME_STATE = Property.ofString("gameState", "LOBBY");

    // If true, the service uses Velocitys modern forwarding
    public static final Property<Boolean> VELOCITY_MODERN_FORWARDING = Property.ofBoolean("velocityModernForwarding", false);

    // If true, the cloud always replaces the velocity forwarding secret with its own
    public static final Property<Boolean> ALWAYS_OVERRIDE_FORWARDING_SECRET = Property.ofBoolean("alwaysOverrideForwardingSecret", true);

    public static Set<Property<?>> asSet() {
        return Set.of(GAME_STATE, VELOCITY_MODERN_FORWARDING, ALWAYS_OVERRIDE_FORWARDING_SECRET);
    }
}
