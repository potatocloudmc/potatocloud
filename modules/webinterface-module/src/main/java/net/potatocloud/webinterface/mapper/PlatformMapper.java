package net.potatocloud.webinterface.mapper;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.webinterface.model.ApiPlatform;
import net.potatocloud.webinterface.model.ApiPlatformVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface PlatformMapper {

    @Mapping(target = "name", expression = "java(platform.name())")
    @Mapping(target = "base", expression = "java(platform.base().name())")
    @Mapping(target = "downloadUrl", expression = "java(platform.downloadUrl())")
    @Mapping(target = "custom", expression = "java(platform.custom())")
    @Mapping(target = "proxy", expression = "java(platform.proxy())")
    @Mapping(target = "bukkitBased", expression = "java(platform.bukkitBased())")
    @Mapping(target = "paperBased", expression = "java(platform.paperBased())")
    @Mapping(target = "velocityBased", expression = "java(platform.velocityBased())")
    @Mapping(target = "limboBased", expression = "java(platform.limboBased())")
    @Mapping(target = "versions", expression = "java(mapVersions(platform.versions()))")
    @Mapping(target = "prepareSteps", expression = "java(platform.prepareSteps())")
    ApiPlatform toApi(Platform platform);

    List<ApiPlatform> toApi(List<Platform> platforms);

    default List<ApiPlatformVersion> mapVersions(List<PlatformVersion> versions) {
        return versions == null ? List.of() : versions.stream()
                .map(this::toApiPlatformVersion)
                .toList();
    }


    @Mapping(target = "name", expression = "java(version.name())")
    @Mapping(target = "fullName", expression = "java(version.fullName())")
    @Mapping(target = "downloadUrl", expression = "java(version.downloadUrl())")
    @Mapping(target = "fileHash", expression = "java(version.fileHash())")
    @Mapping(target = "local", expression = "java(version.local())")
    @Mapping(target = "legacy", expression = "java(version.legacy())")
    ApiPlatformVersion toApiPlatformVersion(PlatformVersion version);
}
