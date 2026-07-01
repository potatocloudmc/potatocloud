package net.potatocloud.network.packets.player;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

import java.util.UUID;

public record CloudPlayerRemovePacket(UUID playerUniqueId) implements Packet {

    public static final Codec<CloudPlayerRemovePacket> CODEC = new Codec<>() {

        @Override
        public void encode(CloudPlayerRemovePacket packet, PacketBuffer buf) {
            buf.write(packet.playerUniqueId(), UUID.class);
        }

        @Override
        public CloudPlayerRemovePacket decode(PacketBuffer buf) {
            return new CloudPlayerRemovePacket(buf.read(UUID.class));
        }
    };
}