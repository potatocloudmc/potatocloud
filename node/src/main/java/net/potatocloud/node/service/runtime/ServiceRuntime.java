package net.potatocloud.node.service.runtime;

import net.potatocloud.api.service.Service;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public interface ServiceRuntime {

    void prepare(Service service);

    void start(Service service, Consumer<String> logOutput);

    void stop(Service service);

    void executeCommand(String command);

    boolean isAlive();

    int usedMemory();

    Optional<Path> directory();

}
