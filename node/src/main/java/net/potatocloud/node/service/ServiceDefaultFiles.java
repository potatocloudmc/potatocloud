package net.potatocloud.node.service;

import lombok.experimental.UtilityClass;
import net.potatocloud.common.ResourceFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class ServiceDefaultFiles {

    public void copyDefaultFiles(Path dataPath) {
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
                    "potatocloud-plugin-limbo.jar"
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
