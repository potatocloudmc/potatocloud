package net.potatocloud.network.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import net.potatocloud.network.NetworkConstants;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.request.RequestManager;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private final PacketManager packetManager;
    private final RequestManager requestManager;
    private final NettyNetworkClient client;

    public NettyClientInitializer(PacketManager packetManager, RequestManager requestManager, NettyNetworkClient client) {
        this.packetManager = packetManager;
        this.requestManager = requestManager;
        this.client = client;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(NetworkConstants.MAX_PACKET_SIZE, 0, 3, 0, 3));
        pipeline.addLast(new NettyPacketDecoder(packetManager, requestManager));

        pipeline.addLast(new LengthFieldPrepender(3));
        pipeline.addLast(new NettyPacketEncoder(packetManager, requestManager));

        pipeline.addLast(new NettyClientHandler(packetManager, client));
    }
}
