package net.potatocloud.node.platform.steps;

import lombok.SneakyThrows;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.api.service.Service;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PortStep implements PrepareStep {

    @Override
    @SneakyThrows
    public void execute(Service service, Platform platform, Path serverDirectory) {
        if (platform.isBukkitBased()) {
            final Path propertiesPath = serverDirectory.resolve("server.properties");
            final Properties properties = new Properties();

            try (FileInputStream in = new FileInputStream(propertiesPath.toFile())) {
                properties.load(in);
            }

            properties.setProperty("server-port", String.valueOf(service.getPort()));
            properties.setProperty("query.port", String.valueOf(service.getPort()));

            try (OutputStream out = Files.newOutputStream(propertiesPath)) {
                properties.store(out, null);
            }
            return;
        }

        if (platform.isProxy() && platform.isVelocityBased()) {
            final Path velocityToml = serverDirectory.resolve("velocity.toml");

            String fileContent = Files.readString(velocityToml);
            fileContent = fileContent.replace(
                    "bind = \"0.0.0.0:25565\"",
                    "bind = \"0.0.0.0:" + service.getPort() + "\""
            );

            Files.writeString(velocityToml, fileContent);
        }

        if (platform.isLimboBased()) {
            final Path propertiesPath = serverDirectory.resolve("server.properties");
            final Properties properties = new Properties();

            try (FileInputStream in = new FileInputStream(propertiesPath.toFile())) {
                properties.load(in);
            }

            properties.setProperty("server-port", String.valueOf(service.getPort()));

            try (OutputStream out = Files.newOutputStream(propertiesPath)) {
                properties.store(out, null);
            }
        }
    }

    @Override
    public String getName() {
        return "port";
    }
}
