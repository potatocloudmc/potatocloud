package net.potatocloud.connector.player.handlers;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.player.CloudPlayerUpdatePacket;

@RequiredArgsConstructor
public class CloudPlayerUpdateHandler implements PacketHandler<CloudPlayerUpdatePacket> {

    private final CloudPlayerManagerImpl playerManager;

    @Override
    public void handle(PacketContext<CloudPlayerUpdatePacket> ctx) {
        final CloudPlayerUpdatePacket packet = ctx.packet();

        playerManager.find(packet.playerUniqueId()).ifPresent(player -> {
            if (player instanceof CloudPlayerImpl playerImpl) {
                playerImpl.proxyName(packet.connectedProxyName());
                playerImpl.serviceName(packet.connectedServiceName());

                playerImpl.propertyMap().clear();
                for (Property<?> property : packet.propertyMap().values()) {
                    PropertyUtil.setPropertyUnchecked(playerImpl, property);
                }
            }
        });
    }
}

