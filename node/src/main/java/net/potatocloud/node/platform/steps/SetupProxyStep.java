package net.potatocloud.node.platform.steps;

import lombok.SneakyThrows;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.platform.VelocityForwardingSecret;
import net.potatocloud.node.utils.PropertiesUtils;
import net.potatocloud.node.utils.ProxyUtils;
import org.simpleyaml.configuration.file.YamlFile;

import java.nio.file.Path;
import java.util.Properties;

public class SetupProxyStep implements PrepareStep {

    @Override
    @SneakyThrows
    public void execute(Service service, Platform platform, Path serverDirectory) {
        // nothing to do, a spigot config with bungeecord enabled was already copied
        if (platform.isBukkitBased() && !ProxyUtils.isProxyModernForwarding()) {
            return;
        }

        if (platform.isPaperBased() && ProxyUtils.isProxyModernForwarding()) {
            final Path paperYml = serverDirectory.resolve("config").resolve("paper-global.yml");
            final YamlFile yaml = new YamlFile(paperYml.toFile());

            yaml.load();
            yaml.set("proxies.velocity.enabled", true);
            yaml.set("proxies.velocity.secret", VelocityForwardingSecret.FORWARDING_SECRET);
            yaml.save();

            return;
        }

        if (platform.isLimboBased()) {
            final Path propertiesPath = serverDirectory.resolve("server.properties");
            final Properties properties = PropertiesUtils.loadProperties(propertiesPath);

            if (!ProxyUtils.isProxyModernForwarding()) {
                properties.setProperty("bungeecord", "true");
            } else {
                properties.setProperty("forwarding-secrets", VelocityForwardingSecret.FORWARDING_SECRET);
                properties.setProperty("velocity-modern", "true");
            }

            PropertiesUtils.saveProperties(properties, propertiesPath);
        }
    }

    @Override
    public String getName() {
        return "setup-proxy";
    }
}
