package net.potatocloud.node.service.runtime;

import net.potatocloud.node.service.NodeService;

public interface ServiceRuntime {

    void prepare(NodeService service);

    void start(NodeService service);

    void stop(NodeService service);

    void executeCommand(String command);

    boolean isAlive();

    int usedMemory();

}
