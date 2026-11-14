package net.potatocloud.module.rest.service;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.module.rest.property.PropertyResponse;

import java.time.Instant;
import java.util.Map;

public record ServiceResponse(
        String name,
        int id,
        String group,
        String node,
        String host,
        int port,
        ServiceState state,
        Instant startedAt,
        int maxPlayers,
        int onlinePlayers,
        int usedMemory,
        Map<String, Object> properties
) {

    public static ServiceResponse from(Service service) {
        return new ServiceResponse(
                service.name(),
                service.id(),
                service.group().name(),
                service.node().map(ClusterNode::name).orElse(null),
                service.host(),
                service.port(),
                service.state(),
                service.startedAt(),
                service.maxPlayers(),
                service.playerCount(),
                service.usedMemory(),
                PropertyResponse.map(service)
        );
    }
}
