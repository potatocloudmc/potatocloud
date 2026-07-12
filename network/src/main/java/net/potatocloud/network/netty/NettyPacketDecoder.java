package net.potatocloud.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.request.RequestManager;
import net.potatocloud.network.request.RequestPacket;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public final class NettyPacketDecoder extends ByteToMessageDecoder {

    private final PacketManager packetManager;
    private final RequestManager requestManager;

    public NettyPacketDecoder(PacketManager packetManager, RequestManager requestManager) {
        this.packetManager = packetManager;
        this.requestManager = requestManager;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final NettyPacketBuffer packetBuffer = new NettyPacketBuffer(in);

        final int packetId = packetBuffer.readVarInt();
        final int requestId = packetBuffer.readVarInt();

        final Packet.Codec<? extends Packet> codec = packetManager.codec(packetId);
        if (codec == null) {
            throw new IllegalStateException("No codec for packet: " + packetId);
        }

        final Packet packet = codec.decode(packetBuffer);

        if (packet instanceof RequestPacket requestPacket) {
            requestManager.requestId(requestPacket, requestId);
        } else if (packet instanceof ResponsePacket responsePacket) {
            requestManager.requestId(responsePacket, requestId);
        }

        out.add(packet);
    }
}
