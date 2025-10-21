package net.potatocloud.api.player.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.property.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "uniqueId")
public class CloudPlayerImpl implements CloudPlayer {

    private final String username;
    private final UUID uniqueId;
    private String connectedProxyName;
    private String connectedServiceName;
    private final Map<String, Property<?>> propertyMap;

    public CloudPlayerImpl(String username, UUID uniqueId, String connectedProxyName) {
        this.username = username;
        this.uniqueId = uniqueId;
        this.connectedProxyName = connectedProxyName;
        this.propertyMap = new HashMap<>();
    }

    @Override
    public String getPropertyHolderName() {
        return getUsername();
    }
}
