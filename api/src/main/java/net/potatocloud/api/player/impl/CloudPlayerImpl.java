package net.potatocloud.api.player.impl;

import lombok.*;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.property.Property;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "uniqueId")
@AllArgsConstructor
public class CloudPlayerImpl implements CloudPlayer {

    private final String username;
    private final String nickname;
    private final UUID uniqueId;
    private final Locale locale;
    private String connectedProxyName;
    private String connectedServiceName;
    private final Map<String, Property<?>> propertyMap;

    private final Long sessionStartTime = System.currentTimeMillis();
    private final UUID sessionId = UUID.randomUUID();

    public CloudPlayerImpl(String username, String nickname, UUID uniqueId, Locale locale, String connectedProxyName) {
        this.username = username;
        this.nickname = nickname;
        this.uniqueId = uniqueId;
        this.locale = locale;
        this.connectedProxyName = connectedProxyName;
        this.propertyMap = new HashMap<>();
    }

    @Override
    public String getPropertyHolderName() {
        return getUsername();
    }
}
