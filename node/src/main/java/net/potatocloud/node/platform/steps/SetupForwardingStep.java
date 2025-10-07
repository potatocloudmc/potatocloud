package net.potatocloud.node.platform.steps;

import lombok.SneakyThrows;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.platform.VelocityForwardingSecret;
import net.potatocloud.node.utils.ProxyUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class SetupForwardingStep implements PrepareStep {

    @Override
    @SneakyThrows
    public void execute(Service service, Platform platform, Path serverDirectory) {
        if (!service.getServiceGroup().getPlatform().isVelocityBased()) {
            return;
        }

        final Path forwardingSecret = serverDirectory.resolve("forwarding.secret");

        if (!ProxyUtils.isProxyModernForwarding()) {
            // velocity proxy uses legacy forwarding
            // a forwarding secret file still has to be created or else velocity will throw an error
            if (!Files.exists(forwardingSecret)) {
                Files.writeString(forwardingSecret, UUID.randomUUID().toString(), StandardOpenOption.CREATE);
            }
            return;
        }

        // velocity proxy uses modern forwarding
        // change forwarding mode to modern
        final Path velocityToml = serverDirectory.resolve("velocity.toml");
        if (Files.exists(velocityToml)) {
            String fileContent = Files.readString(velocityToml);
            fileContent = fileContent.replace(
                    "player-info-forwarding-mode = \"legacy\"",
                    "player-info-forwarding-mode = \"modern\""
            );

            Files.writeString(velocityToml, fileContent);
        }

        // if the group does not have the property, use the properties default
        final Property<Boolean> property = service.getServiceGroup().getProperty(DefaultProperties.ALWAYS_OVERRIDE_FORWARDING_SECRET);
        final boolean alwaysOverride = property != null ? property.getValue() : DefaultProperties.ALWAYS_OVERRIDE_FORWARDING_SECRET.getDefaultValue();

        // now create the forwarding secret file with the correct secret
        if (!Files.exists(forwardingSecret) || alwaysOverride) {
            Files.writeString(forwardingSecret, VelocityForwardingSecret.FORWARDING_SECRET, StandardOpenOption.CREATE);
        }
    }

    @Override
    public String getName() {
        return "setup-forwarding";
    }
}
