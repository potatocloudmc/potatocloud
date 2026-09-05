package net.potatocloud.network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketManager;

public final class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final PacketManager packetManager;
    private final NettyNetworkClient client;

    public NettyClientHandler(PacketManager packetManager, NettyNetworkClient client) {
        this.packetManager = packetManager;
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Packet packet) {
            packetManager.dispatch(client.connection(), packet);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        client.disconnected(new IllegalStateException("Client disconnected"));
    }
}
