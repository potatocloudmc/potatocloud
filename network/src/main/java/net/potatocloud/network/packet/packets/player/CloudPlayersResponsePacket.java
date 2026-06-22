package net.potatocloud.network.packet.packets.player;

import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.Packet;
import net.potatocloud.network.packet.request.ResponsePacket;

import java.util.List;

public record CloudPlayersResponsePacket(List<CloudPlayer> players) implements ResponsePacket {

    public static final Codec<CloudPlayersResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(CloudPlayersResponsePacket packet, PacketBuffer buf) {
            buf.writeCloudPlayerList(packet.players());
        }

        @Override
        public CloudPlayersResponsePacket decode(PacketBuffer buf) {
            return new CloudPlayersResponsePacket(buf.readCloudPlayerList());
        }
    };
}
