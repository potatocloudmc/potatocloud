package net.potatocloud.node.cluster;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterManager;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.Service;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.cluster.*;
import net.potatocloud.network.request.RequestManager;
import net.potatocloud.network.netty.NettyNetworkClient;
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.packets.group.GroupDeletePacket;
import net.potatocloud.network.packets.player.CloudPlayerRemovePacket;
import net.potatocloud.network.packets.service.ServiceRemovePacket;
import net.potatocloud.node.cluster.handlers.NodeDisconnectHandler;
import net.potatocloud.node.cluster.handlers.NodeDiscoveryHandler;
import net.potatocloud.node.cluster.handlers.NodeJoinHandler;
import net.potatocloud.node.cluster.handlers.NodeLeaveHandler;
import net.potatocloud.node.cluster.handlers.ClusterSyncHandler;
import net.potatocloud.node.config.ClusterConfig;
import net.potatocloud.node.group.GroupManagerImpl;
import net.potatocloud.node.player.CloudPlayerManagerImpl;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.ServiceManagerImpl;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterManagerImpl implements ClusterManager {

    private final ClusterNodeImpl localNode;

    private final ClusterConfig config;
    private final RequestManager requestManager;
    private final PacketManager packetManager;
    private final NetworkServer server;
    private final Logger logger;

    private final Map<String, ClusterNodeImpl> nodes = new ConcurrentHashMap<>();
    private final Set<NetworkConnection> outboundConnections = ConcurrentHashMap.newKeySet();
    private final List<NettyNetworkClient> clients = new ArrayList<>();
    private final Set<String> connectingAddresses = ConcurrentHashMap.newKeySet();

    private HeartbeatScheduler heartbeatScheduler;

    private GroupManagerImpl groupManager;
    private ServiceManagerImpl serviceManager;
    private CloudPlayerManagerImpl playerManager;

    public ClusterManagerImpl(String localHost, int localPort, ClusterConfig config, RequestManager requestManager, PacketManager packetManager, NetworkServer server, Logger logger) {
        this.config = config;
        this.requestManager = requestManager;
        this.packetManager = packetManager;
        this.server = server;
        this.logger = logger;
        this.localNode = new ClusterNodeImpl(config.name(), localHost, localPort, Instant.now(), null);

        server.on(RequestClusterNodesPacket.class, ctx -> ctx.reply(new ClusterNodesResponsePacket(localNode, new ArrayList<>(remoteNodes()))));
    }

    public void start(GroupManagerImpl groupManager, ServiceManagerImpl serviceManager, CloudPlayerManagerImpl playerManager, ScreenManager screenManager) {
        this.groupManager = groupManager;
        this.serviceManager = serviceManager;
        this.playerManager = playerManager;

        server.on(NodeJoinPacket.class, new NodeJoinHandler(localNode, this, config.token(), logger, groupManager, serviceManager, playerManager));
        server.on(NodeJoinRejectPacket.class, ctx -> logger.warn("Could not join the cluster&8: &c" + ctx.packet().reason()));
        server.on(NodeLeavePacket.class, new NodeLeaveHandler(this, logger));
        server.on(HeartbeatPacket.class, ctx -> remoteNode(ctx.packet().nodeName()).ifPresent(ClusterNodeImpl::updateHeartbeat));
        server.on(NodeDiscoveryPacket.class, new NodeDiscoveryHandler(this));
        server.on(ClusterSyncPacket.class, new ClusterSyncHandler(groupManager, serviceManager, playerManager, server, screenManager, this));
        server.addDisconnectHandler(new NodeDisconnectHandler(this, logger));

        heartbeatScheduler = new HeartbeatScheduler(this, localNode, logger);
        heartbeatScheduler.start();

        logger.info("Cluster enabled &8(&aname&8: &a" + localNode.name() + "&8)");

        if (config.nodes() != null) {
            connectAll();
        }
    }

    private void connectAll() {
        for (String address : config.nodes()) {
            final String[] parts = address.split(":", 2);

            if (parts.length != 2) {
                logger.warn("Invalid cluster node address (expected host:port): " + address);
                continue;
            }

            connect(parts[0], Integer.parseInt(parts[1]));
        }
    }

    public void connect(String host, int port) {
        final String address = host + ":" + port;
        if (!connectingAddresses.add(address)) {
            return;
        }

        final NettyNetworkClient client = new NettyNetworkClient(requestManager, packetManager);

        client.addConnectionHandler(() -> {
            final NetworkConnection connection = client.connection();
            outboundConnections.add(connection);
            connection.send(new NodeJoinPacket(localNode.name(), localNode.host(), localNode.port(), localNode.startedAt().toEpochMilli(), CloudAPI.VERSION.toString(), config.token()));
        });

        try {
            client.connect(host, port);
            clients.add(client);
        } catch (Exception e) {
            logger.warn("Failed to connect to cluster node " + host + ":" + port + " &8(&7" + e.getMessage() + "&8)");
            connectingAddresses.remove(address);
        }
    }

    public void add(ClusterNodeImpl node) {
        nodes.put(node.name(), node);
        server.broadcast().connectors().send(new ClusterNodeAddPacket(node));
    }

    public void remove(ClusterNodeImpl node) {
        final String nodeName = node.name();

        playerManager.players().stream()
                .filter(player ->
                        player.service()
                                .flatMap(Service::node)
                                .map(ClusterNode::name)
                                .filter(nodeName::equals)
                                .isPresent()
                )
                .toList()
                .forEach(player -> {
                    playerManager.unregisterPlayer(player);
                    server.broadcast().connectors().send(new CloudPlayerRemovePacket(player.uniqueId()));
                });

        serviceManager.services().stream()
                .filter(service ->
                        service.node()
                                .map(ClusterNode::name)
                                .filter(nodeName::equals)
                                .isPresent()
                )
                .toList()
                .forEach(service -> {
                    serviceManager.removeService(service);
                    server.broadcast().connectors().send(new ServiceRemovePacket(service.name(), service.port()));
                });

        groupManager.groups().stream()
                .filter(group ->
                        group.node()
                                .map(ClusterNode::name)
                                .filter(nodeName::equals)
                                .isPresent()
                )
                .toList()
                .forEach(group -> {
                    groupManager.unregisterGroup(group.name());
                    server.broadcast().connectors().send(new GroupDeletePacket(group.name()));
                });

        nodes.remove(nodeName);
        if (node.connection() != null) {
            outboundConnections.remove(node.connection());
        }
        server.broadcast().connectors().send(new ClusterNodeRemovePacket(nodeName));
    }

    // returns true if we opened this connection
    public boolean isOutbound(NetworkConnection connection) {
        return outboundConnections.contains(connection);
    }

    public boolean isLocal(String nodeName) {
        return nodeName == null || nodeName.equals(localNode.name());
    }

    public void sendTo(String nodeName, Packet packet) {
        final ClusterNodeImpl node = nodes.get(nodeName);
        if (node == null || node.connection() == null) {
            return;
        }
        node.connection().send(packet);
    }

    public void broadcast(Packet packet) {
        nodes.values().stream()
                .filter(node -> node.connection() != null)
                .forEach(node -> node.connection().send(packet));
    }

    public void close() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.stop();
        }

        broadcast(new NodeLeavePacket(localNode.name()));
        clients.forEach(NettyNetworkClient::close);
    }

    @Override
    public ClusterNode localNode() {
        return localNode;
    }

    @Override
    public List<ClusterNode> nodes() {
        final List<ClusterNode> all = new ArrayList<>(nodes.values());
        all.add(localNode);
        return Collections.unmodifiableList(all);
    }

    public Collection<ClusterNodeImpl> remoteNodes() {
        return nodes.values();
    }

    @Override
    public Optional<ClusterNode> find(String name) {
        if (localNode.name().equals(name)) {
            return Optional.of(localNode);
        }
        return Optional.ofNullable(nodes.get(name));
    }

    public Optional<ClusterNodeImpl> remoteNode(String nodeName) {
        return Optional.ofNullable(nodes.get(nodeName));
    }

    public Optional<ClusterNodeImpl> remoteNode(NetworkConnection connection) {
        return nodes.values().stream()
                .filter(node -> connection.equals(node.connection()))
                .findFirst();
    }
}
