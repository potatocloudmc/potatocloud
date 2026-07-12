package net.potatocloud.api.player.impl;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CloudPlayerImpl implements CloudPlayer {

    private final String username;
    private final UUID uniqueId;
    private String proxyName;
    private String serviceName;
    private final Map<PropertyKey<?>, Object> properties;

    public CloudPlayerImpl(String username, UUID uniqueId, String proxyName) {
        this.username = username;
        this.uniqueId = uniqueId;
        this.proxyName = proxyName;
        this.properties = new HashMap<>();
    }

    public CloudPlayerImpl(String username, UUID uniqueId, String proxyName, String serviceName, Map<PropertyKey<?>, Object> properties) {
        this.username = username;
        this.uniqueId = uniqueId;
        this.proxyName = proxyName;
        this.serviceName = serviceName;
        this.properties = properties;
    }

    @Override
    public UUID uniqueId() {
        return uniqueId;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public Service proxy() {
        return CloudAPI.instance().serviceManager().find(proxyName)
                .orElseThrow(() -> new IllegalStateException("Proxy not found for player: " + proxyName + ", " + username));
    }

    public void proxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    @Override
    public Optional<Service> service() {
        return CloudAPI.instance().serviceManager().find(serviceName);
    }

    public void serviceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String name() {
        return username();
    }

    @Override
    public Map<PropertyKey<?>, Object> properties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloudPlayerImpl other)) {
            return false;
        }
        return uniqueId.equals(other.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
