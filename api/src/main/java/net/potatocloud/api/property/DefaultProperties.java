package net.potatocloud.api.property;

import java.util.Set;

public class DefaultProperties {

    public static final Property<String> GAME_STATE = Property.ofString("gameState", "INGAME");
    public static final Property<Boolean> VELOCITY_MODERN_FORWARDING = Property.ofBoolean("velocityModernForwarding", false);
    public static final Property<Boolean> ALWAYS_OVERRIDE_FORWARDING_SECRET = Property.ofBoolean("alwaysOverrideForwardingSecret", true);

    public static Set<Property<?>> asSet() {
        return Set.of(GAME_STATE, VELOCITY_MODERN_FORWARDING, ALWAYS_OVERRIDE_FORWARDING_SECRET);
    }
}
