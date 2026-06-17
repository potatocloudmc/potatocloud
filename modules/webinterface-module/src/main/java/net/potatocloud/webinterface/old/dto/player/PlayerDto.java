package net.potatocloud.webinterface.old.dto.player;

import lombok.Builder;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.webinterface.old.dto.group.GroupDto;
import net.potatocloud.webinterface.old.dto.group.PropertyDto;

import java.util.List;
import java.util.Optional;

@Builder
public record PlayerDto(
        String username,
        String uniqueId,
        int connectedProxyId,
        int connectedServerId,
        String connectedProxyName,
        String connectedServerName,
        GroupDto serverGroup,
        GroupDto proxyGroup,
        List<PropertyDto> properties
) {

    public static PlayerDto from(CloudPlayer player, boolean localNodeReady) {
        Optional<Service> connectedServer = player.service();
        Service connectedProxy = player.proxy();

        if (connectedServer.isEmpty() || connectedProxy == null) {
            return PlayerDto.builder()
                    .username(player.username())
                    .uniqueId(player.uniqueId().toString())
                    .connectedProxyId(-1)
                    .connectedServerId(-1)
                    .properties(player.properties().stream().map(PropertyDto::from).toList())
                    .build();
        }

        Group serverGroup = connectedServer.get().group();
        Group proxyGroup = connectedProxy.group();

        return PlayerDto.builder()
                .username(player.username())
                .uniqueId(player.uniqueId().toString())
                .connectedProxyId(connectedProxy.id())
                .connectedServerId(connectedServer.get().id())
                .connectedProxyName(connectedProxy.name())
                .connectedServerName(connectedServer.get().name())
                .serverGroup(GroupDto.from(serverGroup, localNodeReady))
                .proxyGroup(GroupDto.from(proxyGroup, localNodeReady))
                .properties(player.properties().stream().map(PropertyDto::from).toList())
                .build();
    }
}



