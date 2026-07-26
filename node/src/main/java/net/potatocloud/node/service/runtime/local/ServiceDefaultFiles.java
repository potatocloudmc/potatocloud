package net.potatocloud.node.service.runtime.local;

import net.potatocloud.common.ResourceFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServiceDefaultFiles {

    private ServiceDefaultFiles() {
    }

    public static void copyDefaultFiles(Path dataPath) {
        try {
            Files.createDirectories(dataPath);

            final String[] files = {
                    "server.properties",
                    "spigot.yml",
                    "paper-global.yml",
                    "velocity.toml",
                    "limbo-server.properties",
                    "potatocloud-plugin-spigot.jar",
                    "potatocloud-plugin-spigot-legacy.jar",
                    "potatocloud-plugin-velocity.jar",
                    "potatocloud-plugin-limbo.jar",
                    "potatocloud-plugin-fabric-1.21.11.jar",
                    "potatocloud-plugin-fabric-26.1.jar",
                    "potatocloud-plugin-neoforge-1.21.11.jar",
                    "potatocloud-plugin-neoforge-26.1.jar"
            };

            for (String name : files) {
                ResourceFileUtils.copyResourceFile(
                        "default-files/" + name,
                        dataPath.resolve(name)
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy default service files", e);
        }
    }
}
