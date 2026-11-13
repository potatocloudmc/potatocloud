package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public record ServiceLogsResponsePacket(List<String> lines) implements ResponsePacket {

    public static final Codec<ServiceLogsResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceLogsResponsePacket packet, PacketBuffer buf) {
            buf.write(packet.lines(), CollectionSerializers.list(String.class));
        }

        @Override
        public ServiceLogsResponsePacket decode(PacketBuffer buf) {
            return new ServiceLogsResponsePacket(buf.read(CollectionSerializers.list(String.class)));
        }
    };
}
