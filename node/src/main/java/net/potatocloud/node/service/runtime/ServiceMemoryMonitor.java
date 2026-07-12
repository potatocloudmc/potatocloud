package net.potatocloud.node.service.runtime;

import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.service.ServiceMemoryUpdatePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;

import java.util.Map;

public final class ServiceMemoryMonitor implements Runnable {

    private final Map<String, ServiceRuntime> runtimes;
    private final ServiceManager serviceManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    public ServiceMemoryMonitor(Map<String, ServiceRuntime> runtimes, ServiceManager serviceManager, NetworkServer server, ClusterManagerImpl clusterManager) {
        this.runtimes = runtimes;
        this.serviceManager = serviceManager;
        this.server = server;
        this.clusterManager = clusterManager;
    }

    @Override
    public void run() {
        for (Map.Entry<String, ServiceRuntime> entry : runtimes.entrySet()) {
            final String name = entry.getKey();
            final ServiceRuntime runtime = entry.getValue();

            if (!runtime.isAlive()) {
                continue;
            }

            serviceManager.find(name).ifPresent(service -> {
                if (!service.running()) {
                    return;
                }

                final int memory = runtime.usedMemory();

                if (service instanceof ServiceImpl serviceImpl) {
                    serviceImpl.usedMemory(memory);
                }

                final ServiceMemoryUpdatePacket packet = new ServiceMemoryUpdatePacket(name, memory);
                server.broadcast().connectors().send(packet);
                clusterManager.broadcast(packet);
            });
        }
    }
}
