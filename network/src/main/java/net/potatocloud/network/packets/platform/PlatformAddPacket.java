package net.potatocloud.network.packets.platform;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record PlatformAddPacket(Platform platform) implements Packet {

    public static final Codec<PlatformAddPacket> CODEC = new Codec<>() {

        @Override
        public void encode(PlatformAddPacket packet, PacketBuffer buf) {
            buf.writePlatform(packet.platform());
        }

        @Override
        public PlatformAddPacket decode(PacketBuffer buf) {
            return new PlatformAddPacket(buf.readPlatform());
        }
    };
}