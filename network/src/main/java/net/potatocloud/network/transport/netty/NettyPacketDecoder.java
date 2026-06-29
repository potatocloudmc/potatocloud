package net.potatocloud.network.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.exception.PacketTooBigException;
import net.potatocloud.network.request.RequestPacket;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public class NettyPacketDecoder extends ByteToMessageDecoder {

    private final PacketManager packetManager;

    public NettyPacketDecoder(PacketManager packetManager) {
        this.packetManager = packetManager;
    }

    private static final int MAX_PACKET_SIZE = 65536;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();

        // Read packet length and stop if too big
        final int length = in.readInt();
        if (length > MAX_PACKET_SIZE) {
            ctx.close();
            throw new PacketTooBigException(length);
        }

        // Wait until the full packet is received
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        final int packetId = in.readInt();
        final int requestId = in.readInt();

        final Packet.Codec<?> codec = packetManager.codec(packetId);
        if (codec == null) {
            in.skipBytes(length - 8);
            return;
        }

        final Packet packet = codec.decode(new PacketBuffer(in));

        if (packet instanceof RequestPacket requestPacket) {
            packetManager.requestId(requestPacket, requestId);
        } else if (packet instanceof ResponsePacket responsePacket) {
            packetManager.requestId(responsePacket, requestId);
        }

        out.add(packet);
    }
}
