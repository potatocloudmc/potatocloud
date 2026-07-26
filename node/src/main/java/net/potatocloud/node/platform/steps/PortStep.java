package net.potatocloud.node.platform.steps;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.platform.AbstractPrepareStep;
import net.potatocloud.node.utils.PropertiesFileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PortStep extends AbstractPrepareStep {

    @Override
    public void execute(String serviceName, Platform platform, Path serverDirectory) {
        try {
            final int port = (int) data().get("port");

            if (platform.bukkitBased() || platform.moddedBased()) {
                final Path propertiesPath = serverDirectory.resolve("server.properties");
                final Properties properties = PropertiesFileUtils.loadProperties(propertiesPath);

                properties.setProperty("server-port", String.valueOf(port));
                properties.setProperty("query.port", String.valueOf(port));

                PropertiesFileUtils.saveProperties(properties, propertiesPath);
                return;
            }

            if (platform.proxy() && platform.velocityBased()) {
                final Path velocityToml = serverDirectory.resolve("velocity.toml");
                if (!Files.exists(velocityToml)) {
                    return;
                }

                FileUtils.replaceInFile(velocityToml, "bind = \"0.0.0.0:25565\"", "bind = \"0.0.0.0:" + port + "\"");
            }

            if (platform.limboBased()) {
                final Path propertiesPath = serverDirectory.resolve("server.properties");
                final Properties properties = PropertiesFileUtils.loadProperties(propertiesPath);

                properties.setProperty("server-port", String.valueOf(port));

                PropertiesFileUtils.saveProperties(properties, propertiesPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute PortStep for service: " + serviceName, e);
        }
    }

    @Override
    public String name() {
        return "port";
    }
}
