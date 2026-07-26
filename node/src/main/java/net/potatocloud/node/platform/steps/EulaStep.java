package net.potatocloud.node.platform.steps;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.node.platform.AbstractPrepareStep;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class EulaStep extends AbstractPrepareStep {

    @Override
    public void execute(String serviceName, Platform platform, Path serverDirectory) {
        try {
            if (platform.bukkitBased() || platform.moddedBased()) {
                final Path eulaFile = serverDirectory.resolve("eula.txt");

                Files.writeString(eulaFile, "eula=true", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute EulaStep for service: " + serviceName, e);
        }
    }

    @Override
    public String name() {
        return "eula";
    }
}
