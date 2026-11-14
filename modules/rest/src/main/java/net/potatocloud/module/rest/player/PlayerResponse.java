package net.potatocloud.module.rest.player;

import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.module.rest.property.PropertyResponse;

import java.util.Map;
import java.util.UUID;

public record PlayerResponse(UUID uniqueId, String username, String proxy, String service, Map<String, Object> properties) {

    public static PlayerResponse from(CloudPlayer player) {
        return new PlayerResponse(
                player.uniqueId(),
                player.username(),
                player.proxy() == null ? null : player.proxy().name(),
                player.service().map(Service::name).orElse(null),
                PropertyResponse.map(player)
        );
    }
}
