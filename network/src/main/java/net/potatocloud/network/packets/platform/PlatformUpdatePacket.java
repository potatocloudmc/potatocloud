package net.potatocloud.network.packets.platform;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record PlatformUpdatePacket(Platform platform) implements Packet {

    public static final Codec<PlatformUpdatePacket> CODEC = new Codec<>() {

        @Override
        public void encode(PlatformUpdatePacket packet, PacketBuffer buf) {
            buf.write(packet.platform(), Platform.class);
        }

        @Override
        public PlatformUpdatePacket decode(PacketBuffer buf) {
            return new PlatformUpdatePacket(buf.read(Platform.class));
        }
    };
}