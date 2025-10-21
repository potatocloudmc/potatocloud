package net.potatocloud.node.platform;

import lombok.experimental.UtilityClass;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.node.Node;

import java.io.File;
import java.nio.file.Path;

@UtilityClass
public class PlatformUtils {

    public Path getPlatformFolder(Platform platform, PlatformVersion version) {
        return Path.of(Node.getInstance().getConfig().getPlatformsFolder())
                .resolve(platform.getName())
                .resolve(version.getName());
    }

    public File getPlatformJarFile(Platform platform, PlatformVersion version) {
        return getPlatformFolder(platform, version)
                .resolve(version.getFullName() + ".jar")
                .toFile();
    }
}

