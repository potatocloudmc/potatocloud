package net.potatocloud.network.packets.player;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record CloudPlayerKickPacket(String playerUsername, String reason) implements Packet {

    public static final Codec<CloudPlayerKickPacket> CODEC = new Codec<>() {

        @Override
        public void encode(CloudPlayerKickPacket packet, PacketBuffer buf) {
            buf.writeString(packet.playerUsername());
            buf.writeString(packet.reason());
        }

        @Override
        public CloudPlayerKickPacket decode(PacketBuffer buf) {
            return new CloudPlayerKickPacket(buf.readString(), buf.readString());
        }
    };
}
