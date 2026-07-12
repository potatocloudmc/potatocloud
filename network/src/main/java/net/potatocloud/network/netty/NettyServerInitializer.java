package net.potatocloud.network.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import net.potatocloud.network.NetworkConstants;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.request.RequestManager;

public final class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final PacketManager packetManager;
    private final RequestManager requestManager;
    private final NettyNetworkServer server;
    private final SslContext sslContext;

    public NettyServerInitializer(PacketManager packetManager, RequestManager requestManager, NettyNetworkServer server, SslContext sslContext) {
        this.packetManager = packetManager;
        this.requestManager = requestManager;
        this.server = server;
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        if (sslContext != null) {
            pipeline.addLast("ssl", sslContext.newHandler(channel.alloc()));
        }

        pipeline.addLast(new LengthFieldBasedFrameDecoder(NetworkConstants.MAX_PACKET_SIZE, 0, 3, 0, 3));
        pipeline.addLast(new NettyPacketDecoder(packetManager, requestManager));

        pipeline.addLast(new LengthFieldPrepender(3));
        pipeline.addLast(new NettyPacketEncoder(packetManager, requestManager));

        pipeline.addLast(new NettyServerHandler(server, packetManager));
    }
}