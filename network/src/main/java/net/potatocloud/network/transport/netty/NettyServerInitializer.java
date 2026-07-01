package net.potatocloud.network.transport.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import net.potatocloud.network.NetworkConstants;
import net.potatocloud.network.protocol.PacketManager;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final PacketManager packetManager;
    private final NettyNetworkServer server;

    public NettyServerInitializer(PacketManager packetManager, NettyNetworkServer server) {
        this.packetManager = packetManager;
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(NetworkConstants.MAX_PACKET_SIZE, 0, 3, 0, 3));
        pipeline.addLast(new NettyPacketDecoder(packetManager));

        pipeline.addLast(new LengthFieldPrepender(3));
        pipeline.addLast(new NettyPacketEncoder(server.packetManager()));

        pipeline.addLast(new NettyServerHandler(server, packetManager));
    }
}
