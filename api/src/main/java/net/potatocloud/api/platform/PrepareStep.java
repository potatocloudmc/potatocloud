package net.potatocloud.api.platform;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents one step used to prepare a service.
 */
public interface PrepareStep {

    /**
     * Gets the name of the prepare step.
     *
     * @return the name of the prepare step
     */
    String name();

    /**
     * Executes the prepare step.
     *
     * @param serviceName     the name of the service to prepare
     * @param platform        the platform to execute the prepare step on
     * @param serverDirectory the directory of the server
     */
    void execute(String serviceName, Platform platform, Path serverDirectory);

    /**
     * Gets the data of the prepare step.
     *
     * @return the data of the prepare step
     */
    Map<String, Object> data();

}
