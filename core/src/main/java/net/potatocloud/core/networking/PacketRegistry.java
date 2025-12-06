package net.potatocloud.core.networking;

import net.potatocloud.core.networking.packets.EventPacket;
import net.potatocloud.core.networking.packets.group.GroupAddPacket;
import net.potatocloud.core.networking.packets.group.GroupDeletePacket;
import net.potatocloud.core.networking.packets.group.GroupUpdatePacket;
import net.potatocloud.core.networking.packets.group.RequestGroupsPacket;
import net.potatocloud.core.networking.packets.platform.PlatformAddPacket;
import net.potatocloud.core.networking.packets.platform.PlatformRemovePacket;
import net.potatocloud.core.networking.packets.platform.PlatformUpdatePacket;
import net.potatocloud.core.networking.packets.platform.RequestPlatformsPacket;
import net.potatocloud.core.networking.packets.player.*;
import net.potatocloud.core.networking.packets.property.PropertyAddPacket;
import net.potatocloud.core.networking.packets.property.PropertyRemovePacket;
import net.potatocloud.core.networking.packets.property.PropertyUpdatePacket;
import net.potatocloud.core.networking.packets.property.RequestPropertiesPacket;
import net.potatocloud.core.networking.packets.service.*;

public class PacketRegistry {

    public static void registerPackets(PacketManager manager) {
        manager.register(PacketIds.SERVICE_ADD, ServiceAddPacket::new);
        manager.register(PacketIds.SERVICE_REMOVE, ServiceRemovePacket::new);
        manager.register(PacketIds.SERVICE_UPDATE, ServiceUpdatePacket::new);
        manager.register(PacketIds.SERVICE_STARTED, ServiceStartedPacket::new);
        manager.register(PacketIds.REQUEST_SERVICES, RequestServicesPacket::new);
        manager.register(PacketIds.START_SERVICE, StartServicePacket::new);
        manager.register(PacketIds.STOP_SERVICE, StopServicePacket::new);
        manager.register(PacketIds.SERVICE_EXECUTE_COMMAND, ServiceExecuteCommandPacket::new);
        manager.register(PacketIds.SERVICE_COPY, ServiceCopyPacket::new);
        manager.register(PacketIds.SERVICE_MEMORY_UPDATE, ServiceMemoryUpdatePacket::new);

        manager.register(PacketIds.REQUEST_GROUPS, RequestGroupsPacket::new);
        manager.register(PacketIds.GROUP_ADD, GroupAddPacket::new);
        manager.register(PacketIds.GROUP_UPDATE, GroupUpdatePacket::new);
        manager.register(PacketIds.GROUP_DELETE, GroupDeletePacket::new);

        manager.register(PacketIds.PLAYER_ADD, CloudPlayerAddPacket::new);
        manager.register(PacketIds.PLAYER_REMOVE, CloudPlayerRemovePacket::new);
        manager.register(PacketIds.PLAYER_UPDATE, CloudPlayerUpdatePacket::new);
        manager.register(PacketIds.PLAYER_CONNECT, CloudPlayerConnectPacket::new);
        manager.register(PacketIds.REQUEST_PLAYERS, RequestCloudPlayersPacket::new);

        manager.register(PacketIds.EVENT, EventPacket::new);

        manager.register(PacketIds.PLATFORM_ADD, PlatformAddPacket::new);
        manager.register(PacketIds.PLATFORM_REMOVE, PlatformRemovePacket::new);
        manager.register(PacketIds.REQUEST_PLATFORMS, RequestPlatformsPacket::new);
        manager.register(PacketIds.PLATFORM_UPDATE, PlatformUpdatePacket::new);

        manager.register(PacketIds.REQUEST_PROPERTIES, RequestPropertiesPacket::new);
        manager.register(PacketIds.PROPERTY_ADD, PropertyAddPacket::new);
        manager.register(PacketIds.PROPERTY_REMOVE, PropertyRemovePacket::new);
        manager.register(PacketIds.PROPERTY_UPDATE, PropertyUpdatePacket::new);
    }
}
