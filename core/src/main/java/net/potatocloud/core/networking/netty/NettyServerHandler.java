package net.potatocloud.core.networking.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketManager;

@RequiredArgsConstructor
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private final NettyNetworkServer server;
    private final PacketManager packetManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        server.getConnectedSessions().add(new NettyNetworkConnection(ctx.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        server.getConnectedSessions().removeIf(session -> ((NettyNetworkConnection) session).getChannel().equals(ctx.channel()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Packet packet) {
            // Find the session the packet was sent to and handle it
            server.getConnectedSessions().stream()
                    .filter(conn -> conn instanceof NettyNetworkConnection nettyConn && nettyConn.getChannel().equals(ctx.channel()))
                    .findFirst().ifPresent(connection -> packetManager.onPacket(connection, packet));

        }
    }
}
