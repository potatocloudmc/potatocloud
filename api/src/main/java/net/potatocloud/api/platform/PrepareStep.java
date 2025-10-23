package net.potatocloud.api.platform;

import net.potatocloud.api.service.Service;

import java.nio.file.Path;

public interface PrepareStep {

    /**
     * Gets the name of the prepare step
     *
     * @return the name of the prepare step
     */
    String getName();

    /**
     * Executes the prepare step
     *
     * @param service the service to execute the prepare step on
     * @param platform the platform to execute the prepare step on
     * @param serverDirectory the directory of the server
     */
    void execute(Service service, Platform platform, Path serverDirectory);

}
