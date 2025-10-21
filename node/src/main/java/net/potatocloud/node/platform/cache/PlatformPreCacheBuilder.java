package net.potatocloud.node.platform.cache;

import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;

import java.nio.file.Path;

public interface PlatformPreCacheBuilder {

    void buildCache(Platform platform, PlatformVersion version, ServiceGroup group, Path cacheFolder);

    void copyCacheToService(Path cacheFolder, Path serviceDir);

}
