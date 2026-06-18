package net.potatocloud.webinterface.service;

import net.potatocloud.webinterface.model.ApiPlatform;

import java.util.List;

public interface PlatformService {

    List<ApiPlatform> findAll();

    ApiPlatform findByName(String name);

    boolean existsByName(String name);

}
