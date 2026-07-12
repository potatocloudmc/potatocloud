package net.potatocloud.network.packets.event;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record EventPacket(String eventClass, String eventJson) implements Packet {

    public static final Codec<EventPacket> CODEC = new Codec<>() {

        @Override
        public void encode(EventPacket packet, PacketBuffer buf) {
            buf.writeString(packet.eventClass());
            buf.writeString(packet.eventJson());
        }

        @Override
        public EventPacket decode(PacketBuffer buf) {
            return new EventPacket(
                    buf.readString(),
                    buf.readString()
            );
        }
    };
}