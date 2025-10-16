package net.potatocloud.node.platform;

import lombok.experimental.UtilityClass;
import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.node.platform.steps.*;

@UtilityClass
public class PlatformPrepareSteps {

    public PrepareStep getStep(final String stepName) {
        switch (stepName.toLowerCase()) {
            case "default-files":
                return new DefaultFilesStep();
            case "eula":
                return new EulaStep();
            case "port":
                return new PortStep();
            case "setup-forwarding":
                return new SetupForwardingStep();
            case "setup-proxy":
                return new SetupProxyStep();
            default:
                return null;
        }
    }
}
