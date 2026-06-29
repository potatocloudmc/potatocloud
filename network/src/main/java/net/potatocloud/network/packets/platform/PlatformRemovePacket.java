package net.potatocloud.network.packets.platform;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record PlatformRemovePacket(String platformName) implements Packet {

    public static final Codec<PlatformRemovePacket> CODEC = new Codec<>() {

        @Override
        public void encode(PlatformRemovePacket packet, PacketBuffer buf) {
            buf.writeString(packet.platformName());
        }

        @Override
        public PlatformRemovePacket decode(PacketBuffer buf) {
            return new PlatformRemovePacket(buf.readString());
        }
    };
}