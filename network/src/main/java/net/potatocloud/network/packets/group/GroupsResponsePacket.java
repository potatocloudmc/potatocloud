package net.potatocloud.network.packets.group;

import net.potatocloud.api.group.Group;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public record GroupsResponsePacket(List<Group> groups) implements ResponsePacket {

    public static final Codec<GroupsResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(GroupsResponsePacket packet, PacketBuffer buf) {
            buf.write(packet.groups(), CollectionSerializers.list(Group.class));
        }

        @Override
        public GroupsResponsePacket decode(PacketBuffer buf) {
            return new GroupsResponsePacket(buf.read(CollectionSerializers.list(Group.class)));
        }
    };
}
