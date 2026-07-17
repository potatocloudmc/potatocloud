package net.potatocloud.node.platform.cache;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface CacheBuilder {

    boolean supports(PlatformVersion version);

    CompletableFuture<Void> build(Group group, Platform platform, PlatformVersion version, Path cacheFolder);

    void copyToService(Path cacheFolder, Path serviceDir);

}
