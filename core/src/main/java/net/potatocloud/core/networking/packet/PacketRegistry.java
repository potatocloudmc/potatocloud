package net.potatocloud.core.networking.packet;

import net.potatocloud.core.networking.packet.packets.event.EventPacket;
import net.potatocloud.core.networking.packet.packets.group.GroupAddPacket;
import net.potatocloud.core.networking.packet.packets.group.GroupDeletePacket;
import net.potatocloud.core.networking.packet.packets.group.GroupUpdatePacket;
import net.potatocloud.core.networking.packet.packets.group.RequestGroupsPacket;
import net.potatocloud.core.networking.packet.packets.logging.LogMessagePacket;
import net.potatocloud.core.networking.packet.packets.platform.PlatformAddPacket;
import net.potatocloud.core.networking.packet.packets.platform.PlatformRemovePacket;
import net.potatocloud.core.networking.packet.packets.platform.PlatformUpdatePacket;
import net.potatocloud.core.networking.packet.packets.platform.RequestPlatformsPacket;
import net.potatocloud.core.networking.packet.packets.player.*;
import net.potatocloud.core.networking.packet.packets.property.PropertyAddPacket;
import net.potatocloud.core.networking.packet.packets.property.PropertyRemovePacket;
import net.potatocloud.core.networking.packet.packets.property.PropertyUpdatePacket;
import net.potatocloud.core.networking.packet.packets.property.RequestPropertiesPacket;
import net.potatocloud.core.networking.packet.packets.service.*;
import net.potatocloud.core.networking.packet.packets.translation.RequestTranslationPacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationAddPacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationRemovePacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationUpdatePacket;

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

        manager.register(PacketIds.LOG_MESSAGE, LogMessagePacket::new);

        manager.register(PacketIds.TRANSLATION_ADD, TranslationAddPacket::new);
        manager.register(PacketIds.TRANSLATION_REMOVE, TranslationRemovePacket::new);
        manager.register(PacketIds.TRANSLATION_UPDATE, TranslationUpdatePacket::new);
        manager.register(PacketIds.REQUEST_TRANSLATION, RequestTranslationPacket::new);
    }
}
