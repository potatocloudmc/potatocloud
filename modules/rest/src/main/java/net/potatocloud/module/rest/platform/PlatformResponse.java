package net.potatocloud.module.rest.platform;

import net.potatocloud.api.platform.Platform;

import java.util.List;

public record PlatformResponse(
        String name,
        String downloadUrl,
        boolean custom,
        boolean proxy,
        String base,
        String preCacheBuilder,
        String parser,
        String hashType,
        List<String> prepareSteps,
        List<PlatformVersionResponse> versions
) {

    public static PlatformResponse from(Platform platform) {
        return new PlatformResponse(
                platform.name(),
                platform.downloadUrl(),
                platform.custom(),
                platform.proxy(),
                platform.base().id(),
                platform.preCacheBuilder(),
                platform.parser(),
                platform.hashType(),
                platform.prepareSteps(),
                platform.versions().stream().map(PlatformVersionResponse::from).toList()
        );
    }
}
