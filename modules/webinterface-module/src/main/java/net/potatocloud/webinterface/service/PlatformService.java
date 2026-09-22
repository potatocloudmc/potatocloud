package net.potatocloud.webinterface.service;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.webinterface.dto.request.PlatformCreateRequest;
import net.potatocloud.webinterface.dto.request.PlatformUpdateRequest;
import net.potatocloud.webinterface.dto.request.PlatformVersionRequest;
import net.potatocloud.webinterface.model.ApiPlatform;

import java.util.List;

public interface PlatformService {

    List<ApiPlatform> findAll();

    List<Platform> findAllRaw();

    Platform findByNameRaw(String name);

    ApiPlatform findByName(String name);

    boolean existsByName(String name);

    boolean create(PlatformCreateRequest request);

    boolean update(String name, PlatformUpdateRequest request);

    boolean delete(String name);

    boolean addVersion(String platformName, PlatformVersionRequest request);

    boolean removeVersion(String platformName, String versionName);

}
