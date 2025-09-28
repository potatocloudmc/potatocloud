package net.potatocloud.node.platform.steps;

import lombok.SneakyThrows;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.platform.VelocityForwardingSecret;
import net.potatocloud.node.utils.ProxyUtils;
import org.simpleyaml.configuration.file.YamlFile;

import java.nio.file.Path;

public class SetupProxyStep implements PrepareStep {

    @Override
    @SneakyThrows
    public void execute(Service service, Platform platform, Path serverDirectory) {
        // nothing to do a spigot config with bungeecord enabled was already copied
        if (!ProxyUtils.isProxyModernForwarding()) {
            return;
        }

        final Path paperYml = serverDirectory.resolve("config").resolve("paper-global.yml");
        final YamlFile yaml = new YamlFile(paperYml.toFile());

        yaml.load();

        yaml.set("proxies.velocity.enabled", true);
        yaml.set("proxies.velocity.secret", VelocityForwardingSecret.FORWARDING_SECRET);

        yaml.save();
    }

    @Override
    public String getName() {
        return "setup-proxy";
    }

}
