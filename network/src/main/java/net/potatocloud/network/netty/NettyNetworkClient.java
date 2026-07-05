package net.potatocloud.network.netty;

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
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.protocol.PacketRegistry;
import net.potatocloud.network.request.RequestManager;
import net.potatocloud.network.request.RequestPacket;
import net.potatocloud.network.request.ResponsePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class NettyNetworkClient implements NetworkClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 5000;

    private final RequestManager requestManager;
    private final PacketManager packetManager;
    private final List<ConnectionHandler> connectionHandlers = new ArrayList<>();

    private boolean running;
    private Channel channel;
    private EventLoopGroup group;
    private NetworkConnection connection;

    public NettyNetworkClient() {
        this.requestManager = new RequestManager();
        this.packetManager = new PacketManager(requestManager);
        PacketRegistry.registerPackets(packetManager);
    }

    @Override
    public void connect(String host, int port) {
        this.group = NettyUtils.createEventLoopGroup();

        final ChannelFuture connectFuture = new Bootstrap()
                .group(group)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .handler(new NettyClientInitializer(packetManager, requestManager, this))
                .connect(host, port)
                .syncUninterruptibly();

        this.channel = connectFuture.channel();
        this.connection = new NettyNetworkConnection(channel);
        this.running = true;
        onConnected();
    }

    @Override
    public void send(Packet packet) {
        if (!running) {
            throw new IllegalStateException("Client is not connected");
        }
        channel.writeAndFlush(packet);
    }

    @Override
    public void onConnected(ConnectionHandler handler) {
        connectionHandlers.add(handler);
    }

    public void onConnected() {
        connectionHandlers.forEach(ConnectionHandler::onConnected);
    }

    @Override
    public void close() {
        if (!running) {
            return;
        }

        running = false;

        channel.close().syncUninterruptibly();
        group.shutdownGracefully().syncUninterruptibly();
    }

    @Override
    public <T extends Packet> void on(Class<T> packetClass, PacketHandler<T> handler) {
        packetManager.on(packetClass, handler);
    }

    @Override
    public <T extends ResponsePacket> CompletableFuture<T> request(RequestPacket packet, Class<T> type) {
        if (!running) {
            throw new IllegalStateException("Client is not connected");
        }
        return requestManager.request(connection, packet, type);
    }

    public NetworkConnection connection() {
        return connection;
    }

    public PacketManager packetManager() {
        return packetManager;
    }
}
