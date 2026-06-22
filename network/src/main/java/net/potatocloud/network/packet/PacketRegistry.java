package net.potatocloud.network.packet;

import net.potatocloud.network.packet.packets.cluster.*;
import net.potatocloud.network.packet.packets.event.EventPacket;
import net.potatocloud.network.packet.packets.group.GroupAddPacket;
import net.potatocloud.network.packet.packets.group.GroupDeletePacket;
import net.potatocloud.network.packet.packets.group.GroupUpdatePacket;
import net.potatocloud.network.packet.packets.group.GroupsResponsePacket;
import net.potatocloud.network.packet.packets.group.RequestGroupsPacket;
import net.potatocloud.network.packet.packets.logging.LogMessagePacket;
import net.potatocloud.network.packet.packets.platform.PlatformAddPacket;
import net.potatocloud.network.packet.packets.platform.PlatformRemovePacket;
import net.potatocloud.network.packet.packets.platform.PlatformUpdatePacket;
import net.potatocloud.network.packet.packets.platform.RequestPlatformsPacket;
import net.potatocloud.network.packet.packets.platform.PlatformsResponsePacket;
import net.potatocloud.network.packet.packets.player.*;
import net.potatocloud.network.packet.packets.property.PropertyAddPacket;
import net.potatocloud.network.packet.packets.property.PropertyRemovePacket;
import net.potatocloud.network.packet.packets.property.PropertyUpdatePacket;
import net.potatocloud.network.packet.packets.property.RequestPropertiesPacket;
import net.potatocloud.network.packet.packets.service.*;

public final class PacketRegistry {

    private PacketRegistry() {
    }

    public static void registerPackets(PacketManager manager) {
        manager.register(0, ServiceAddPacket.class, ServiceAddPacket.CODEC);
        manager.register(1, ServiceRemovePacket.class, ServiceRemovePacket.CODEC);
        manager.register(2, ServiceUpdatePacket.class, ServiceUpdatePacket.CODEC);
        manager.register(3, ServiceStartedPacket.class, ServiceStartedPacket.CODEC);
        manager.register(4, RequestServicesPacket.class, RequestServicesPacket.CODEC);
        manager.register(5, StartServicePacket.class, StartServicePacket.CODEC);
        manager.register(6, StopServicePacket.class, StopServicePacket.CODEC);
        manager.register(7, ServiceExecuteCommandPacket.class, ServiceExecuteCommandPacket.CODEC);
        manager.register(8, ServiceCopyPacket.class, ServiceCopyPacket.CODEC);
        manager.register(9, ServiceMemoryUpdatePacket.class, ServiceMemoryUpdatePacket.CODEC);
        manager.register(10, ServiceStartingPacket.class, ServiceStartingPacket.CODEC);
        manager.register(11, ServiceScreenSubscribePacket.class, ServiceScreenSubscribePacket.CODEC);
        manager.register(12, ServiceScreenUnsubscribePacket.class, ServiceScreenUnsubscribePacket.CODEC);
        manager.register(13, ServiceScreenLogPacket.class, ServiceScreenLogPacket.CODEC);
        manager.register(14, ServicesResponsePacket.class, ServicesResponsePacket.CODEC);

        manager.register(100, RequestGroupsPacket.class, RequestGroupsPacket.CODEC);
        manager.register(101, GroupAddPacket.class, GroupAddPacket.CODEC);
        manager.register(102, GroupUpdatePacket.class, GroupUpdatePacket.CODEC);
        manager.register(103, GroupDeletePacket.class, GroupDeletePacket.CODEC);
        manager.register(104, GroupsResponsePacket.class, GroupsResponsePacket.CODEC);

        manager.register(200, CloudPlayerAddPacket.class, CloudPlayerAddPacket.CODEC);
        manager.register(201, CloudPlayerRemovePacket.class, CloudPlayerRemovePacket.CODEC);
        manager.register(202, CloudPlayerUpdatePacket.class, CloudPlayerUpdatePacket.CODEC);
        manager.register(203, CloudPlayerConnectPacket.class, CloudPlayerConnectPacket.CODEC);
        manager.register(204, RequestCloudPlayersPacket.class, RequestCloudPlayersPacket.CODEC);
        manager.register(205, CloudPlayersResponsePacket.class, CloudPlayersResponsePacket.CODEC);

        manager.register(300, EventPacket.class, EventPacket.CODEC);

        manager.register(400, PlatformAddPacket.class, PlatformAddPacket.CODEC);
        manager.register(401, PlatformRemovePacket.class, PlatformRemovePacket.CODEC);
        manager.register(402, RequestPlatformsPacket.class, RequestPlatformsPacket.CODEC);
        manager.register(403, PlatformUpdatePacket.class, PlatformUpdatePacket.CODEC);
        manager.register(404, PlatformsResponsePacket.class, PlatformsResponsePacket.CODEC);

        manager.register(500, RequestPropertiesPacket.class, RequestPropertiesPacket.CODEC);
        manager.register(501, PropertyUpdatePacket.class, PropertyUpdatePacket.CODEC);
        manager.register(502, PropertyRemovePacket.class, PropertyRemovePacket.CODEC);
        manager.register(503, PropertyAddPacket.class, PropertyAddPacket.CODEC);

        manager.register(600, LogMessagePacket.class, LogMessagePacket.CODEC);

        manager.register(700, NodeJoinPacket.class, NodeJoinPacket.CODEC);
        manager.register(701, NodeLeavePacket.class, NodeLeavePacket.CODEC);
        manager.register(702, HeartbeatPacket.class, HeartbeatPacket.CODEC);
        manager.register(703, NodeDiscoveryPacket.class, NodeDiscoveryPacket.CODEC);
        manager.register(704, ClusterSyncPacket.class, ClusterSyncPacket.CODEC);
        manager.register(705, NodeJoinRejectPacket.class, NodeJoinRejectPacket.CODEC);
        manager.register(706, RequestClusterNodesPacket.class, RequestClusterNodesPacket.CODEC);
        manager.register(707, ClusterNodesResponsePacket.class, ClusterNodesResponsePacket.CODEC);
        manager.register(708, ClusterNodeAddPacket.class, ClusterNodeAddPacket.CODEC);
        manager.register(709, ClusterNodeRemovePacket.class, ClusterNodeRemovePacket.CODEC);
    }
}
