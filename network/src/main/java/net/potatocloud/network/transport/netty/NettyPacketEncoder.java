package net.potatocloud.network.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.request.RequestPacket;
import net.potatocloud.network.request.ResponsePacket;

public class NettyPacketEncoder extends MessageToByteEncoder<Packet> {

    private final PacketManager packetManager;

    public NettyPacketEncoder(PacketManager packetManager) {
        this.packetManager = packetManager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        final NettyPacketBuffer packetBuffer = new NettyPacketBuffer(out);

        final int packetId = packetManager.packetId(packet);
        packetBuffer.writeVarInt(packetId);

        if (packet instanceof RequestPacket requestPacket) {
            packetBuffer.writeVarInt(packetManager.requestId(requestPacket));
        } else if (packet instanceof ResponsePacket responsePacket) {
            packetBuffer.writeVarInt(packetManager.requestId(responsePacket));
        } else {
            packetBuffer.writeVarInt(0);
        }

        final Packet.Codec<Packet> codec = packetManager.codec(packetId);
        if (codec == null) {
            throw new IllegalStateException("No codec for packet: " + packetId);
        }

        codec.encode(packet, packetBuffer);
    }
}
