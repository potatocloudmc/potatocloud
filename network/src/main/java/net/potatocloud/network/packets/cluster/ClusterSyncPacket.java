package net.potatocloud.network.packets.cluster;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

import java.util.List;
import java.util.Set;

public record ClusterSyncPacket(List<Group> groups, List<Service> services, Set<CloudPlayer> players) implements Packet {

    public static final Codec<ClusterSyncPacket> CODEC = new Codec<>() {

        @Override
        public void encode(ClusterSyncPacket packet, PacketBuffer buf) {
            buf.write(packet.groups(), CollectionSerializers.list(Group.class));
            buf.write(packet.services(), CollectionSerializers.list(Service.class));
            buf.write(packet.players(), CollectionSerializers.set(CloudPlayer.class));
        }

        @Override
        public ClusterSyncPacket decode(PacketBuffer buf) {
            return new ClusterSyncPacket(
                    buf.read(CollectionSerializers.list(Group.class)),
                    buf.read(CollectionSerializers.list(Service.class)),
                    buf.read(CollectionSerializers.set(CloudPlayer.class))
            );
        }
    };
}
