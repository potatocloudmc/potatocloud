package net.potatocloud.network.packet.packets.platform;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.Packet;
import net.potatocloud.network.packet.request.ResponsePacket;

import java.util.List;

public record PlatformsResponsePacket(List<Platform> platforms) implements ResponsePacket {

    public static final Codec<PlatformsResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(PlatformsResponsePacket packet, PacketBuffer buf) {
            buf.writePlatformList(packet.platforms());
        }

        @Override
        public PlatformsResponsePacket decode(PacketBuffer buf) {
            return new PlatformsResponsePacket(buf.readPlatformList());
        }
    };
}
