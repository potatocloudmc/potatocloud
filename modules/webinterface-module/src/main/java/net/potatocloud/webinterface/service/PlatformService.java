package net.potatocloud.webinterface.service;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.webinterface.model.ApiPlatform;

import java.util.List;

public interface PlatformService {

    List<ApiPlatform> findAll();

    List<Platform> findAllRaw();

    Platform findByNameRaw(String name);

    ApiPlatform findByName(String name);

    boolean existsByName(String name);

}
