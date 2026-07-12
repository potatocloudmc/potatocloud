package net.potatocloud.network.packets.group;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record GroupDeletePacket(String groupName) implements Packet {

    public static final Codec<GroupDeletePacket> CODEC = new Codec<>() {

        @Override
        public void encode(GroupDeletePacket packet, PacketBuffer buf) {
            buf.writeString(packet.groupName());
        }

        @Override
        public GroupDeletePacket decode(PacketBuffer buf) {
            return new GroupDeletePacket(buf.readString());
        }
    };
}