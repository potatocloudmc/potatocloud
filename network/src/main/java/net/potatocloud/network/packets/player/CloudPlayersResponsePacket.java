package net.potatocloud.network.packets.player;

import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public record CloudPlayersResponsePacket(List<CloudPlayer> players) implements ResponsePacket {

    public static final Codec<CloudPlayersResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(CloudPlayersResponsePacket packet, PacketBuffer buf) {
            buf.write(packet.players(), CollectionSerializers.list(CloudPlayer.class));
        }

        @Override
        public CloudPlayersResponsePacket decode(PacketBuffer buf) {
            return new CloudPlayersResponsePacket(buf.read(CollectionSerializers.list(CloudPlayer.class)));
        }
    };
}
