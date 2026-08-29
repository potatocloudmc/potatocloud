package net.potatocloud.network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketManager;

import java.net.SocketException;

public final class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private final NettyNetworkServer server;
    private final PacketManager packetManager;

    public NettyServerHandler(NettyNetworkServer server, PacketManager packetManager) {
        this.server = server;
        this.packetManager = packetManager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof SocketException) {
            return;
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final NettyNetworkConnection connection = new NettyNetworkConnection(ctx.channel());
        server.registerConnection(ctx.channel(), connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final NetworkConnection connection = server.removeConnection(ctx.channel());
        if (connection != null) {
            server.handleDisconnect(connection);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof Packet packet)) {
            return;
        }
        final NetworkConnection connection = server.connection(ctx.channel());
        if (connection != null) {
            packetManager.dispatch(connection, packet);
        }
    }
}
