package net.potatocloud.core.networking.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import net.potatocloud.core.networking.*;

@RequiredArgsConstructor
public class NettyNetworkClient implements NetworkClient {

    private final PacketManager packetManager;
    private EventLoopGroup group;
    private Channel channel;
    private NetworkConnection connection;

    @Override
    public void connect(String host, int port) {
        PacketRegistry.registerPackets(packetManager);

        // TODO: Switch to the new way of creating the event loop group just like in the server
        // The last time I tried I had problems with it so I left it like that
        group = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        connection = new NettyNetworkConnection(channel);
                        final ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new NettyPacketDecoder(packetManager));
                        pipeline.addLast(new NettyPacketEncoder());
                        pipeline.addLast(new NettyClientHandler(packetManager, connection));
                    }
                });

        final ChannelFuture future = bootstrap.connect(host, port).syncUninterruptibly();
        channel = future.channel();
    }

    @Override
    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

    @Override
    public void disconnect() {
        channel.close();
        group.shutdownGracefully();
    }

    @Override
    public <T extends Packet> void registerPacketListener(int id, PacketListener<T> listener) {
        packetManager.registerListener(id, listener);
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }
}
