package net.potatocloud.core.networking.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.potatocloud.core.networking.Packet;

public class NettyPacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        // Create a new buffer for the packet with the id and packet data
        final ByteBuf buf = ctx.alloc().buffer();

        // Write the packet id and packet data into the buffer
        buf.writeInt(packet.getId());
        packet.write(new PacketBuffer(buf));

        // Payload length
        out.writeInt(buf.readableBytes());

        // Write the payload
        out.writeBytes(buf);
        buf.release();
    }
}
