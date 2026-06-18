package net.potatocloud.webinterface.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.webinterface.dto.service.ServerOverviewDto;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ServerService {

    private final CloudAPI cloudAPI;

    public List<ServerOverviewDto> getServices() {
        return cloudAPI.serviceManager().services().stream()
                .map(ServerOverviewDto::from)
                .toList();
    }

    public void stopService(String serviceId) {
        // TODO: refresh websocket sessions of the service (details and console)
        Optional<Service> service = cloudAPI.serviceManager().find(serviceId);
        if (service.isEmpty()) return;

        cloudAPI.serviceManager().stop(service.get());
    }

    public ServerOverviewDto getService(String name) {
        Optional<Service> service = cloudAPI.serviceManager().find(name);

        return service.map(ServerOverviewDto::from).orElse(null);

    }
}

