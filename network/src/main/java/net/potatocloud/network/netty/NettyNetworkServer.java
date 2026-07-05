package net.potatocloud.network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.protocol.PacketRegistry;
import net.potatocloud.network.request.RequestManager;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class NettyNetworkServer implements NetworkServer {

    private final RequestManager requestManager;
    private final PacketManager packetManager;
    private final Map<Channel, NetworkConnection> sessionMap = new ConcurrentHashMap<>();
    private final List<Consumer<NetworkConnection>> disconnectHandlers = new CopyOnWriteArrayList<>();

    private boolean running;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private int port;

    public NettyNetworkServer() {
        this.requestManager = new RequestManager();
        this.packetManager = new PacketManager(requestManager);
        PacketRegistry.registerPackets(packetManager);
    }

    @Override
    public void start(String hostname, int port) {
        this.port = port;

        this.bossGroup = NettyUtils.createEventLoopGroup();
        this.workerGroup = NettyUtils.createEventLoopGroup();

        this.channel = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new NettyServerInitializer(packetManager, requestManager, this))
                .bind(new InetSocketAddress(hostname, port))
                .syncUninterruptibly()
                .channel();

        this.running = true;
    }

    @Override
    public void close() {
        if (!running) {
            return;
        }

        running = false;

        for (NetworkConnection session : connections()) {
            session.close();
        }

        channel.close().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
    }

    @Override
    public boolean running() {
        return running;
    }

    @Override
    public Collection<NetworkConnection> connections() {
        return sessionMap.values();
    }

    @Override
    public int port() {
        return port;
    }

    public Map<Channel, NetworkConnection> sessionMap() {
        return sessionMap;
    }

    @Override
    public void onClientDisconnected(Consumer<NetworkConnection> handler) {
        disconnectHandlers.add(handler);
    }

    public void handleDisconnect(NetworkConnection connection) {
        disconnectHandlers.forEach(handler -> handler.accept(connection));
    }

    public PacketManager packetManager() {
        return packetManager;
    }

    @Override
    public <T extends Packet> void on(Class<T> packetClass, PacketHandler<T> handler) {
        packetManager.on(packetClass, handler);
    }

    @Override
    public void sendTo(NetworkConnection connection, Packet packet) {
        connection.send(packet);
    }
}
