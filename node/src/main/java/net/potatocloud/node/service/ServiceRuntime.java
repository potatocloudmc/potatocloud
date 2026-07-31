package net.potatocloud.node.service;

import net.potatocloud.api.service.Service;

import java.nio.file.Path;
import java.util.Optional;

public interface ServiceRuntime {

    void prepare(Service service);

    void start(Service service);

    void stop(Service service);

    void executeCommand(String command);

    boolean isAlive();

    int usedMemory();

    Optional<Path> directory();

}
