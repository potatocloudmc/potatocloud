package net.potatocloud.webinterface.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.webinterface.model.ApiPlayer;

@ApplicationScoped
public class PlayerMapper {

    @Inject
    GroupMapper groupMapper;

    @Inject
    PropertyMapper propertyMapper;

    public ApiPlayer toApi(CloudPlayer cloudPlayer) {
        return new ApiPlayer()
                .username(cloudPlayer.username())
                .uniqueId(cloudPlayer.uniqueId())
                .proxyId(cloudPlayer.proxy().id())
                .serverId(cloudPlayer.service().orElse(null) != null ? cloudPlayer.service().get().id() : -1)
                .proxyName(cloudPlayer.proxy().name())
                .serverName(cloudPlayer.service().orElse(null) != null ? cloudPlayer.service().get().name() : null)
                .proxyGroup(cloudPlayer.proxy().group().name())
                .serverGroup(cloudPlayer.service().orElse(null) != null ? cloudPlayer.service().get().group().name() : null)
                .properties(cloudPlayer.properties().stream().map(propertyMapper::toApi).toList());
    }

}
