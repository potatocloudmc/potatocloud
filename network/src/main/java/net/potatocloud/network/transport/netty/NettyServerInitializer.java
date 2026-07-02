package net.potatocloud.network.transport.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import net.potatocloud.network.NetworkConstants;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.request.RequestManager;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final PacketManager packetManager;
    private final RequestManager requestManager;
    private final NettyNetworkServer server;

    public NettyServerInitializer(PacketManager packetManager, RequestManager requestManager, NettyNetworkServer server) {
        this.packetManager = packetManager;
        this.requestManager = requestManager;
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(NetworkConstants.MAX_PACKET_SIZE, 0, 3, 0, 3));
        pipeline.addLast(new NettyPacketDecoder(packetManager, requestManager));

        pipeline.addLast(new LengthFieldPrepender(3));
        pipeline.addLast(new NettyPacketEncoder(packetManager, requestManager));

        pipeline.addLast(new NettyServerHandler(server, packetManager));
    }
}
