package net.potatocloud.node.cluster.handlers;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.cluster.ClusterSyncPacket;
import net.potatocloud.network.packets.group.GroupAddPacket;
import net.potatocloud.network.packets.player.CloudPlayerAddPacket;
import net.potatocloud.network.packets.property.PropertyAddPacket;
import net.potatocloud.network.packets.service.ServiceAddPacket;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.group.GroupManagerImpl;
import net.potatocloud.node.player.CloudPlayerManagerImpl;
import net.potatocloud.node.properties.NodePropertiesHolder;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.screen.impl.RemoteServiceScreen;
import net.potatocloud.node.service.NodeServiceManager;

import java.util.Map;

public final class ClusterSyncHandler implements PacketHandler<ClusterSyncPacket> {

    private final GroupManagerImpl groupManager;
    private final NodeServiceManager serviceManager;
    private final CloudPlayerManagerImpl playerManager;
    private final NetworkServer server;
    private final ScreenManager screenManager;
    private final ClusterManagerImpl clusterManager;
    private final NodePropertiesHolder properties;

    public ClusterSyncHandler(
            GroupManagerImpl groupManager,
            NodeServiceManager serviceManager,
            CloudPlayerManagerImpl playerManager,
            NetworkServer server,
            ScreenManager screenManager,
            ClusterManagerImpl clusterManager,
            NodePropertiesHolder properties
    ) {
        this.groupManager = groupManager;
        this.serviceManager = serviceManager;
        this.playerManager = playerManager;
        this.server = server;
        this.screenManager = screenManager;
        this.clusterManager = clusterManager;
        this.properties = properties;
    }

    @Override
    public void handle(PacketContext<ClusterSyncPacket> ctx) {
        final ClusterSyncPacket packet = ctx.packet();

        for (Group group : packet.groups()) {
            if (groupManager.exists(group.name())) {
                continue;
            }
            groupManager.registerGroup(group);
            server.broadcast().connectors().send(new GroupAddPacket(group));
        }

        for (Service service : packet.services()) {
            if (serviceManager.find(service.name()).isPresent()) {
                continue;
            }
            serviceManager.addService(service);

            final Console console = Node.instance().console();
            final Screen screen = new RemoteServiceScreen(service, console, clusterManager);
            screenManager.register(screen);

            server.broadcast().connectors().send(new ServiceAddPacket(service));
        }

        for (CloudPlayer player : packet.players()) {
            if (playerManager.find(player.uniqueId()).isPresent()) {
                continue;
            }
            playerManager.registerPlayer(player);
            server.broadcast().connectors().send(new CloudPlayerAddPacket(player));
        }

        final Map<PropertyKey<?>, Object> receivedProperties = packet.properties();
        if (receivedProperties != null && !receivedProperties.isEmpty()) {
            properties.properties().putAll(receivedProperties);

            for (Map.Entry<PropertyKey<?>, Object> entry : receivedProperties.entrySet()) {
                server.broadcast().connectors().send(new PropertyAddPacket(entry.getKey().name(), entry.getKey().defaultValue(), entry.getValue()));
            }
        }
    }
}
