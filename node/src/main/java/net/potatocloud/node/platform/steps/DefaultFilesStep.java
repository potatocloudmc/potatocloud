package net.potatocloud.node.platform.steps;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.node.Node;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.platform.AbstractPrepareStep;
import net.potatocloud.node.utils.ProxyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultFilesStep extends AbstractPrepareStep {

    @Override
    public void execute(String serviceName, Platform platform, Path serverDirectory) {
        try {
            final NodeConfig config = Node.instance().config();

            if (platform.bukkitBased()) {
                final Path serverProperties = serverDirectory.resolve("server.properties");
                if (!serverProperties.toFile().exists()) {
                    Files.copy(Path.of(config.folders().data(), "server.properties"), serverProperties);
                }

                // The spigot yml is only needed when velocity uses legacy forwarding
                if (!ProxyUtils.isProxyModernForwarding()) {
                    final Path spigotYml = serverDirectory.resolve("spigot.yml");

                    if (!Files.exists(spigotYml)) {
                        Files.copy(Path.of(config.folders().data(), "spigot.yml"), spigotYml);
                    }
                }

                if (platform.paperBased() && ProxyUtils.isProxyModernForwarding()) {
                    final Path paperYml = serverDirectory.resolve("config").resolve("paper-global.yml");

                    if (!Files.exists(paperYml)) {
                        Files.createDirectories(paperYml.getParent());
                        Files.copy(Path.of(config.folders().data(), "paper-global.yml"), paperYml);
                    }
                }
                return;
            }

            if (platform.velocityBased()) {
                final Path velocityToml = serverDirectory.resolve("velocity.toml");
                if (!Files.exists(velocityToml)) {
                    Files.copy(Path.of(config.folders().data(), "velocity.toml"), velocityToml);
                    return;
                }
            }

            if (platform.limboBased()) {
                final Path serverProperties = serverDirectory.resolve("server.properties");

                if (!Files.exists(serverProperties)) {
                    Files.copy(Path.of(config.folders().data(), "limbo-server.properties"), serverProperties);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute DefaultFilesStep for service: " + serviceName, e);
        }
    }

    @Override
    public String name() {
        return "default-files";
    }
}
