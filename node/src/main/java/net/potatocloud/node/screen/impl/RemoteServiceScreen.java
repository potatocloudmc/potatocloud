package net.potatocloud.node.screen.impl;

import net.potatocloud.api.service.Service;
import net.potatocloud.network.packets.service.ServiceScreenSubscribePacket;
import net.potatocloud.network.packets.service.ServiceScreenUnsubscribePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.console.Console;

public final class RemoteServiceScreen extends ServiceScreen {

    private final ClusterManagerImpl clusterManager;

    public RemoteServiceScreen(Service service, Console console, ClusterManagerImpl clusterManager) {
        super(service, console);
        this.clusterManager = clusterManager;
    }

    @Override
    public void open() {
        service.node().ifPresent(node ->
                clusterManager.sendTo(node.name(), new ServiceScreenSubscribePacket(service.name())));

        console.prompt(buildPrompt());
    }

    @Override
    public void close() {
        service.node().ifPresent(node ->
                clusterManager.sendTo(node.name(), new ServiceScreenUnsubscribePacket(service.name())));
    }
}
