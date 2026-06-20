package net.potatocloud.node.cluster.listeners;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.cluster.ClusterSyncPacket;
import net.potatocloud.network.packet.packets.group.GroupAddPacket;
import net.potatocloud.network.packet.packets.player.CloudPlayerAddPacket;
import net.potatocloud.network.packet.packets.service.ServiceAddPacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.group.GroupManagerImpl;
import net.potatocloud.node.player.CloudPlayerManagerImpl;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.screen.impl.RemoteServiceScreen;
import net.potatocloud.node.service.ServiceManagerImpl;

public class ClusterSyncListener implements PacketListener<ClusterSyncPacket> {

    private final GroupManagerImpl groupManager;
    private final ServiceManagerImpl serviceManager;
    private final CloudPlayerManagerImpl playerManager;
    private final NetworkServer server;
    private final ScreenManager screenManager;
    private final ClusterManagerImpl clusterManager;

    public ClusterSyncListener(GroupManagerImpl groupManager, ServiceManagerImpl serviceManager, CloudPlayerManagerImpl playerManager, NetworkServer server, ScreenManager screenManager, ClusterManagerImpl clusterManager) {
        this.groupManager = groupManager;
        this.serviceManager = serviceManager;
        this.playerManager = playerManager;
        this.server = server;
        this.screenManager = screenManager;
        this.clusterManager = clusterManager;
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

            final Console console = Node.getInstance().console();
            final Screen screen = new RemoteServiceScreen(service, console, clusterManager);
            screenManager.register(screen);

            server.broadcast().connectors().send(new ServiceAddPacket(service, null));
        }

        for (CloudPlayer player : packet.players()) {
            if (playerManager.find(player.uniqueId()).isPresent()) {
                continue;
            }
            playerManager.registerPlayer(player);
            server.broadcast().connectors().send(new CloudPlayerAddPacket(player));
        }
    }
}
