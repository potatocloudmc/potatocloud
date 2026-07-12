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

public final class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private final PacketManager packetManager;
    private final RequestManager requestManager;
    private final NettyNetworkClient client;
    private final SslContext sslContext;

    public NettyClientInitializer(PacketManager packetManager, RequestManager requestManager, NettyNetworkClient client, SslContext sslContext) {
        this.packetManager = packetManager;
        this.requestManager = requestManager;
        this.client = client;
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

        pipeline.addLast(new NettyClientHandler(packetManager, client));
    }
}