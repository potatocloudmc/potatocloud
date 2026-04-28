package net.potatocloud.node.service;

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
