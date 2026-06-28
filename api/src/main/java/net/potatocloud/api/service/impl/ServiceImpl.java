package net.potatocloud.api.service.impl;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class ServiceImpl implements Service {

    private final int serviceId;
    private final int port;
    private final String host;
    private final String name;
    private final String groupName;
    private final Map<PropertyKey<?>, Object> properties;

    private Instant startedAt;
    private ServiceState state;
    private int maxPlayers;
    private int usedMemory;

    public ServiceImpl(int serviceId, String host, int port, String name, String groupName, Map<PropertyKey<?>, Object> properties, Instant startedAt, ServiceState state, int maxPlayers, int usedMemory) {
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.name = name;
        this.groupName = groupName;
        this.properties = properties;
        this.startedAt = startedAt;
        this.state = state;
        this.maxPlayers = maxPlayers;
        this.usedMemory = usedMemory;
    }

    @Override
    public Group group() {
        return CloudAPI.instance().groupManager().find(groupName).orElse(null);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int id() {
        return serviceId;
    }

    @Override
    public Optional<ClusterNode> node() {
        final Group group = group();
        if (group == null) {
            return Optional.empty();
        }
        return group.node();
    }

    @Override
    public ServiceState state() {
        return state;
    }

    @Override
    public void state(ServiceState state) {
        this.state = state;
    }

    @Override
    public Instant startedAt() {
        return startedAt;
    }

    public void startedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public Duration uptime() {
        return Duration.between(startedAt, Instant.now());
    }

    @Override
    public int maxPlayers() {
        return maxPlayers;
    }

    @Override
    public void maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    @Override
    public int usedMemory() {
        return usedMemory;
    }

    public void usedMemory(int usedMemory) {
        this.usedMemory = usedMemory;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public int port() {
        return port;
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
        if (!(o instanceof ServiceImpl service)) {
            return false;
        }
        return serviceId == service.serviceId;
    }

    @Override
    public int hashCode() {
        return serviceId;
    }
}
