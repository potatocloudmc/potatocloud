package net.potatocloud.network.packet.packets.group;

import net.potatocloud.api.group.Group;
import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.Packet;
import net.potatocloud.network.packet.request.ResponsePacket;

import java.util.List;

public record GroupsResponsePacket(List<Group> groups) implements ResponsePacket {

    public static final Codec<GroupsResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(GroupsResponsePacket packet, PacketBuffer buf) {
            buf.writeGroupList(packet.groups());
        }

        @Override
        public GroupsResponsePacket decode(PacketBuffer buf) {
            return new GroupsResponsePacket(buf.readGroupList());
        }
    };
}
