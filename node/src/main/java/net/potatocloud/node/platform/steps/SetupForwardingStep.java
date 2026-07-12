package net.potatocloud.node.platform.steps;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.platform.AbstractPrepareStep;
import net.potatocloud.node.platform.VelocityForwardingSecret;
import net.potatocloud.node.utils.ProxyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class SetupForwardingStep extends AbstractPrepareStep {

    @Override
    public void execute(String serviceName, Platform platform, Path serverDirectory) {
        try {
            if (!platform.velocityBased()) {
                return;
            }

            final Path forwardingSecret = serverDirectory.resolve("forwarding.secret");

            if (!ProxyUtils.isProxyModernForwarding()) {
                // a forwarding secret file still has to be created or else Velocity will throw an error
                if (!Files.exists(forwardingSecret)) {
                    Files.writeString(forwardingSecret, UUID.randomUUID().toString(), StandardOpenOption.CREATE);
                }
                return;
            }

            // if velocity uses modern forwarding, switch forwarding mode to modern
            final Path velocityToml = serverDirectory.resolve("velocity.toml");
            if (Files.exists(velocityToml)) {
                FileUtils.replaceInFile(velocityToml, "player-info-forwarding-mode = \"legacy\"", "player-info-forwarding-mode = \"modern\"");
            }

            final Group group = (Group) data().get("group");

            // check if the forwarding secret should always be replaced
            final boolean alwaysOverride = group.get(DefaultProperties.ALWAYS_OVERRIDE_FORWARDING_SECRET);

            // now create the forwarding secret file with the correct secret
            if (!Files.exists(forwardingSecret) || alwaysOverride) {
                Files.writeString(forwardingSecret, VelocityForwardingSecret.FORWARDING_SECRET, StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute SetupForwardingStep for service: " + serviceName, e);
        }
    }

    @Override
    public String name() {
        return "setup-forwarding";
    }
}