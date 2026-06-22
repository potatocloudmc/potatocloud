package net.potatocloud.network.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.potatocloud.network.ConnectionHandler;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.netty.NettyNetworkConnection;
import net.potatocloud.network.netty.NettyUtils;
import net.potatocloud.network.packet.Packet;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.PacketManager;
import net.potatocloud.network.packet.request.RequestPacket;
import net.potatocloud.network.packet.request.ResponsePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NettyNetworkClient implements NetworkClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 5000;

    private final PacketManager packetManager;
    private volatile Channel channel;
    private EventLoopGroup group;
    private volatile NetworkConnection connection;
    private final List<ConnectionHandler> connectionHandlers = new ArrayList<>();

    public NettyNetworkClient(PacketManager packetManager) {
        this.packetManager = packetManager;
    }

    @Override
    public void connect(String host, int port) {
        group = NettyUtils.createEventLoopGroup();

        final ChannelFuture connectFuture = new Bootstrap()
                .group(group)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .handler(new NettyNetworkClientInitializer(packetManager, this))
                .connect(host, port).syncUninterruptibly();

        channel = connectFuture.channel();
        connection = new NettyNetworkConnection(channel);
        onConnected();
    }

    @Override
    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

    @Override
    public void addConnectionHandler(ConnectionHandler handler) {
        connectionHandlers.add(handler);
    }

    public void onConnected() {
        connectionHandlers.forEach(ConnectionHandler::onConnected);
    }

    @Override
    public void close() {
        channel.close().syncUninterruptibly();
        group.shutdownGracefully().syncUninterruptibly();
    }

    @Override
    public <T extends Packet> void on(Class<T> packetClass, PacketHandler<T> handler) {
        packetManager.on(packetClass, handler);
    }

    @Override
    public <T extends ResponsePacket> CompletableFuture<T> request(RequestPacket packet, Class<T> type) {
        return packetManager.request(connection, packet, type);
    }

    public NetworkConnection connection() {
        return connection;
    }

    public PacketManager packetManager() {
        return packetManager;
    }
}
