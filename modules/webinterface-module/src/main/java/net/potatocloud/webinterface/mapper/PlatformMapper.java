package net.potatocloud.webinterface.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformBase;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.webinterface.dto.request.PlatformCreateRequest;
import net.potatocloud.webinterface.dto.request.PlatformUpdateRequest;
import net.potatocloud.webinterface.dto.request.PlatformVersionRequest;
import net.potatocloud.webinterface.dto.response.PlatformDetailResponse;
import net.potatocloud.webinterface.dto.response.PlatformSummaryResponse;
import net.potatocloud.webinterface.model.ApiPlatform;
import net.potatocloud.webinterface.model.ApiPlatformVersion;

import java.util.List;

@ApplicationScoped
public class PlatformMapper {

    public ApiPlatform toApi(Platform platform) {
        return new ApiPlatform()
                .name(platform.name())
                .base(platform.base().name())
                .downloadUrl(platform.downloadUrl())
                .custom(platform.custom())
                .proxy(platform.proxy())
                .bukkitBased(platform.bukkitBased())
                .paperBased(platform.paperBased())
                .velocityBased(platform.velocityBased())
                .limboBased(platform.limboBased())
                .versions(mapVersions(platform.versions()))
                .prepareSteps(platform.prepareSteps());
    }

    public List<ApiPlatform> toApi(List<Platform> platforms) {
        return platforms == null ? List.of() : platforms.stream()
                .map(this::toApi)
                .toList();
    }

    public List<ApiPlatformVersion> mapVersions(List<PlatformVersion> versions) {
        return versions == null ? List.of() : versions.stream()
                .map(this::toApi)
                .toList();
    }

    public ApiPlatformVersion toApi(PlatformVersion version) {
        return new ApiPlatformVersion()
                .name(version.name())
                .fullName(version.fullName())
                .downloadUrl(version.downloadUrl())
                .fileHash(version.fileHash())
                .local(version.local())
                .legacy(version.legacy());
    }

    public PlatformSummaryResponse toSummaryApi(Platform platform) {
        return new PlatformSummaryResponse()
                .name(platform.name())
                .base(platform.base().name())
                .downloadUrl(platform.downloadUrl())
                .custom(platform.custom())
                .proxy(platform.proxy())
                .bukkitBased(platform.bukkitBased())
                .paperBased(platform.paperBased())
                .velocityBased(platform.velocityBased())
                .limboBased(platform.limboBased())
                .prepareSteps(platform.prepareSteps());
    }

    public List<PlatformSummaryResponse> toSummaryApi(List<Platform> platforms) {
        return platforms == null ? List.of() : platforms.stream()
                .map(this::toSummaryApi)
                .toList();
    }

    public PlatformDetailResponse toDetailApi(Platform platform) {
        PlatformDetailResponse detail = new PlatformDetailResponse();

        detail.name(platform.name())
                .base(platform.base().name())
                .downloadUrl(platform.downloadUrl())
                .custom(platform.custom())
                .proxy(platform.proxy())
                .bukkitBased(platform.bukkitBased())
                .paperBased(platform.paperBased())
                .velocityBased(platform.velocityBased())
                .limboBased(platform.limboBased())
                .prepareSteps(platform.prepareSteps());

        detail.versions(mapVersions(platform.versions()));

        return detail;
    }

    public Platform toPlatform(PlatformCreateRequest request) {
        return CloudAPI.instance().platformManager().builder(request.name())
                .base(PlatformBase.fromId(request.base()))
                .downloadUrl(defaultIfNull(request.downloadUrl()))
                .custom(true)
                .proxy(request.proxy())
                .preCacheBuilder(defaultIfNull(request.preCacheBuilder()))
                .parser(defaultIfNull(request.parser()))
                .hashType(defaultIfNull(request.hashType()))
                .prepareSteps(request.prepareSteps() != null ? request.prepareSteps() : List.of())
                .build();
    }

    public Platform buildUpdatedPlatform(Platform existing, PlatformUpdateRequest request) {
        return CloudAPI.instance().platformManager().builder(existing.name())
                .base(existing.base())
                .downloadUrl(request.downloadUrl() != null ? request.downloadUrl() : existing.downloadUrl())
                .custom(existing.custom())
                .proxy(request.proxy() != null ? request.proxy() : existing.proxy())
                .preCacheBuilder(request.preCacheBuilder() != null ? request.preCacheBuilder() : existing.preCacheBuilder())
                .parser(request.parser() != null ? request.parser() : existing.parser())
                .hashType(request.hashType() != null ? request.hashType() : existing.hashType())
                .prepareSteps(request.prepareSteps() != null ? request.prepareSteps() : existing.prepareSteps())
                .build();
    }

    public PlatformVersion toPlatformVersion(String platformName, PlatformVersionRequest request) {
        return new PlatformVersionImpl(
                platformName,
                request.name(),
                request.local(),
                request.downloadUrl(),
                request.fileHash(),
                request.legacy()
        );
    }

    private String defaultIfNull(String value) {
        return value == null ? "" : value;
    }
}
