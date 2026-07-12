package net.potatocloud.network.packets.player;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record CloudPlayerConnectPacket(String playerUsername, String serviceName) implements Packet {

    public static final Codec<CloudPlayerConnectPacket> CODEC = new Codec<>() {

        @Override
        public void encode(CloudPlayerConnectPacket packet, PacketBuffer buf) {
            buf.writeString(packet.playerUsername());
            buf.writeString(packet.serviceName());
        }

        @Override
        public CloudPlayerConnectPacket decode(PacketBuffer buf) {
            return new CloudPlayerConnectPacket(buf.readString(), buf.readString());
        }
    };
}