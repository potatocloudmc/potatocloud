package net.potatocloud.node.platform.steps;

import lombok.SneakyThrows;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.utils.PropertiesUtils;

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
            final Properties properties = PropertiesUtils.loadProperties(propertiesPath);

            properties.setProperty("server-port", String.valueOf(service.getPort()));
            properties.setProperty("query.port", String.valueOf(service.getPort()));

            PropertiesUtils.saveProperties(properties, propertiesPath);
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
            final Properties properties = PropertiesUtils.loadProperties(propertiesPath);

            properties.setProperty("server-port", String.valueOf(service.getPort()));

            PropertiesUtils.saveProperties(properties, propertiesPath);
        }
    }

    @Override
    public String getName() {
        return "port";
    }
}
