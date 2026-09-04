package net.potatocloud.webinterface.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.webinterface.dto.request.PlatformCreateRequest;
import net.potatocloud.webinterface.dto.request.PlatformUpdateRequest;
import net.potatocloud.webinterface.dto.request.PlatformVersionRequest;
import net.potatocloud.webinterface.mapper.PlatformMapper;
import net.potatocloud.webinterface.model.ApiPlatform;
import net.potatocloud.webinterface.service.PlatformService;

import java.util.ArrayList;
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
    public List<Platform> findAllRaw() {
        return CloudAPI.instance().platformManager().platforms();
    }

    @Override
    public Platform findByNameRaw(String name) {
        return CloudAPI.instance().platformManager().find(name).orElse(null);
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

    @Override
    public boolean create(PlatformCreateRequest request) {
        PlatformManager platformManager = CloudAPI.instance().platformManager();

        if (platformManager.exists(request.name())) {
            return false;
        }

        Platform platform = platformMapper.toPlatform(request);
        platformManager.create(platform);
        return true;
    }

    @Override
    public boolean update(String name, PlatformUpdateRequest request) {
        PlatformManager platformManager = CloudAPI.instance().platformManager();
        Platform existing = platformManager.find(name).orElse(null);

        if (existing == null) {
            return false;
        }

        Platform updated = platformMapper.buildUpdatedPlatform(existing, request);

        List<PlatformVersion> existingVersions = new ArrayList<>(existing.versions());
        for (PlatformVersion version : existingVersions) {
            updated.addVersion(version);
        }

        platformManager.delete(existing);
        platformManager.create(updated);
        return true;
    }

    @Override
    public boolean delete(String name) {
        Platform platform = CloudAPI.instance().platformManager().find(name).orElse(null);

        if (platform == null) {
            return false;
        }

        CloudAPI.instance().platformManager().delete(platform);
        return true;
    }

    @Override
    public boolean addVersion(String platformName, PlatformVersionRequest request) {
        Platform platform = CloudAPI.instance().platformManager().find(platformName).orElse(null);

        if (platform == null) {
            return false;
        }

        if (platform.hasVersion(request.name())) {
            return false;
        }

        PlatformVersion version = platformMapper.toPlatformVersion(platformName, request);
        platform.addVersion(version);
        CloudAPI.instance().platformManager().update(platform);
        return true;
    }

    @Override
    public boolean removeVersion(String platformName, String versionName) {
        Platform platform = CloudAPI.instance().platformManager().find(platformName).orElse(null);

        if (platform == null) {
            return false;
        }

        List<PlatformVersion> versions = platform.versions();
        boolean removed = versions.removeIf(v -> v.name().equalsIgnoreCase(versionName));

        if (removed) {
            platform.versions(versions);
            CloudAPI.instance().platformManager().update(platform);
        }

        return removed;
    }

}
