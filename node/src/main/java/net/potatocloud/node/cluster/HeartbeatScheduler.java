package net.potatocloud.node.cluster;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.network.packets.cluster.HeartbeatPacket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatScheduler {

    private static final long HEARTBEAT_INTERVAL = 5000;
    private static final long TIMEOUT_MS = 15000;

    private final ClusterManagerImpl clusterManager;
    private final ClusterNodeImpl localNode;
    private final Logger logger;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

    public HeartbeatScheduler(ClusterManagerImpl clusterManager, ClusterNodeImpl localNode, Logger logger) {
        this.clusterManager = clusterManager;
        this.localNode = localNode;
        this.logger = logger;
    }

    public void start() {
        executor.scheduleAtFixedRate(this::run, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void run() {
        clusterManager.broadcast(new HeartbeatPacket(localNode.name()));

        for (ClusterNodeImpl node : clusterManager.remoteNodes()) {
            if (node.isTimedOut(TIMEOUT_MS)) {
                clusterManager.remove(node);
                logger.warn("Cluster node &a" + node.name() + " &7timed out");
            }
        }
    }

    public void stop() {
        executor.shutdownNow();
    }
}
