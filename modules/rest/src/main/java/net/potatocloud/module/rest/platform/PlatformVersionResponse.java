package net.potatocloud.module.rest.platform;

import net.potatocloud.api.platform.PlatformVersion;

public record PlatformVersionResponse(String name, boolean local, String downloadUrl, String fileHash, boolean legacy) {

    public static PlatformVersionResponse from(PlatformVersion version) {
        return new PlatformVersionResponse(
                version.name(),
                version.local(),
                version.downloadUrl(),
                version.fileHash(),
                version.legacy()
        );
    }
}
