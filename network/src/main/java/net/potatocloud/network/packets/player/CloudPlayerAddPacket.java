package net.potatocloud.network.packets.player;

import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record CloudPlayerAddPacket(CloudPlayer player) implements Packet {

    public static final Codec<CloudPlayerAddPacket> CODEC = new Codec<>() {

        @Override
        public void encode(CloudPlayerAddPacket packet, PacketBuffer buf) {
            buf.writeCloudPlayer(packet.player());
        }

        @Override
        public CloudPlayerAddPacket decode(PacketBuffer buf) {
            return new CloudPlayerAddPacket(buf.readCloudPlayer());
        }
    };
}
