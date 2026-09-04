package net.potatocloud.webinterface.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import net.potatocloud.api.service.Service;
import net.potatocloud.webinterface.model.ApiService;

@ApplicationScoped
public class ServerMapper {

    public ApiService toApi(Service service) {
        return new ApiService()
                .id(service.id())
                .group(service.group().name())
                .name(service.name())
                .uptime(service.uptime())
                .state(service.state())
                .port(service.port())
                .maxPlayers(service.maxPlayers())
                .playerCount(service.playerCount())
                .usedMemory(service.usedMemory());
    }

}
