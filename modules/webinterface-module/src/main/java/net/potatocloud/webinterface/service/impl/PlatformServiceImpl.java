package net.potatocloud.webinterface.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.webinterface.mapper.PlatformMapper;
import net.potatocloud.webinterface.model.ApiPlatform;
import net.potatocloud.webinterface.service.PlatformService;

import java.util.List;

@ApplicationScoped
public class PlatformServiceImpl implements PlatformService {

    @Inject
    PlatformMapper platformMapper;

    @Override
    public List<ApiPlatform> findAll() {
        List<Platform> platforms = CloudAPI.instance().platformManager().platforms();
        return platformMapper.toApi(platforms);
    }

    @Override
    public ApiPlatform findByName(String name) {
        return CloudAPI.instance().platformManager().find(name)
                .map(platformMapper::toApi)
                .orElse(null);
    }

    @Override
    public boolean existsByName(String name) {
        return CloudAPI.instance().platformManager().find(name).isPresent();
    }

}
