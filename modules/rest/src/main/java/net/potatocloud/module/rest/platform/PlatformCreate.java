package net.potatocloud.module.rest.platform;

import java.util.List;

public record PlatformCreate(
        String name,
        String downloadUrl,
        boolean proxy,
        String base,
        String preCacheBuilder,
        String parser,
        String hashType,
        List<String> prepareSteps
) {}
