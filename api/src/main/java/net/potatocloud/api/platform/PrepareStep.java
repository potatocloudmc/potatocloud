package net.potatocloud.api.platform;

import net.potatocloud.api.service.Service;

import java.nio.file.Path;

public interface PrepareStep {

    String getName();

    void execute(Service service, Platform platform, Path serverDirectory);

}
