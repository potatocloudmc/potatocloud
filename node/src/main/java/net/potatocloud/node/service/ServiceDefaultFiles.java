package net.potatocloud.node.service;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Logger;
import org.apache.commons.io.FileUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@UtilityClass
public class ServiceDefaultFiles {

    @SneakyThrows
    public void copyDefaultFiles(Logger logger, NodeConfig config, ClassLoader classLoader) {
        final Path dataFolder = Path.of(config.getDataFolder());
        final List<String> files = List.of("server.properties", "spigot.yml", "paper-global.yml",
                "velocity.toml", "limbo-server.properties", "potatocloud-plugin-spigot.jar",
                "potatocloud-plugin-velocity.jar", "potatocloud-plugin-limbo.jar");

        Files.createDirectories(dataFolder);
        for (String name : files) {
            try (InputStream stream = classLoader.getResourceAsStream("default-files/" + name)) {
                if (stream == null) {
                    continue;
                }

                FileUtils.copyInputStreamToFile(stream, dataFolder.resolve(name).toFile());
            } catch (Exception e) {
                logger.warn("Failed to copy default service file: " + name);
            }
        }
    }
}
