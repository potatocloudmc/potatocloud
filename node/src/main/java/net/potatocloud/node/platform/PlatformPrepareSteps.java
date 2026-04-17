package net.potatocloud.node.platform;

import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.node.platform.steps.*;

public final class PlatformPrepareSteps {

    private PlatformPrepareSteps() {
    }

    public static PrepareStep getStep(String stepName) {
        return switch (stepName.toLowerCase()) {
            case "default-files" -> new DefaultFilesStep();
            case "eula" -> new EulaStep();
            case "port" -> new PortStep();
            case "setup-forwarding" -> new SetupForwardingStep();
            case "setup-proxy" -> new SetupProxyStep();
            default -> null;
        };
    }
}
