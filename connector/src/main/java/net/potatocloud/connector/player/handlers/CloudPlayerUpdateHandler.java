package net.potatocloud.connector.player.handlers;

import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.player.CloudPlayerUpdatePacket;

public final class CloudPlayerUpdateHandler implements PacketHandler<CloudPlayerUpdatePacket> {

    private final CloudPlayerManagerImpl playerManager;

    public CloudPlayerUpdateHandler(CloudPlayerManagerImpl playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public void handle(PacketContext<CloudPlayerUpdatePacket> ctx) {
        final CloudPlayerUpdatePacket packet = ctx.packet();

        playerManager.find(packet.playerUniqueId()).ifPresent(player -> {
            if (player instanceof CloudPlayerImpl playerImpl) {
                playerImpl.proxyName(packet.connectedProxyName());
                playerImpl.serviceName(packet.connectedServiceName());

                playerImpl.properties().clear();
                packet.propertyMap().forEach((key, value) -> PropertyUtil.setUnchecked(playerImpl, key, value));
            }
        });
    }
}

