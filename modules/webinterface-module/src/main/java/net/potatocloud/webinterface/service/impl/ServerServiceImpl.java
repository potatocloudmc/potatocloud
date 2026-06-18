package net.potatocloud.webinterface.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.webinterface.mapper.ServerMapper;
import net.potatocloud.webinterface.model.ApiService;
import net.potatocloud.webinterface.service.ServerService;

import java.util.List;

@ApplicationScoped
public class ServerServiceImpl implements ServerService {

    @Inject
    ServerMapper serverMapper;

    @Override
    public List<ApiService> findAll() {
        return CloudAPI.instance().serviceManager().services().stream()
                .map(service -> serverMapper.toApi(service))
                .toList();
    }

    @Override
    public ApiService findByName(String name) {
        return CloudAPI.instance().serviceManager().services().stream()
                .filter(service -> service.name().equals(name))
                .findFirst()
                .map(service -> serverMapper.toApi(service))
                .orElse(null);
    }

    @Override
    public boolean exists(String name) {
        return CloudAPI.instance().serviceManager().services().stream()
                .anyMatch(service -> service.name().equals(name));
    }

    @Override
    public boolean shutdown(String name) {
        return CloudAPI.instance().serviceManager().services().stream()
                .filter(service -> service.name().equals(name))
                .findFirst()
                .map(service -> {
                    CloudAPI.instance().serviceManager().stop(service);
                    return true;
                })
                .orElse(false);
    }

}
