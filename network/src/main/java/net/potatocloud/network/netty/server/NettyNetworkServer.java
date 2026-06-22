package net.potatocloud.network.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.netty.NettyUtils;
import net.potatocloud.network.packet.Packet;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.PacketManager;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class NettyNetworkServer implements NetworkServer {

    private final PacketManager packetManager;
    private final Map<Channel, NetworkConnection> sessionMap = new ConcurrentHashMap<>();
    private final List<Consumer<NetworkConnection>> disconnectHandlers = new CopyOnWriteArrayList<>();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private int port;

    public NettyNetworkServer(PacketManager packetManager) {
        this.packetManager = packetManager;
    }

    @Override
    public void start(String hostname, int port) {
        this.port = port;

        bossGroup = NettyUtils.createEventLoopGroup();
        workerGroup = NettyUtils.createEventLoopGroup();

        channel = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new NettyNetworkServerInitializer(packetManager, this))
                .bind(new InetSocketAddress(hostname, port)).syncUninterruptibly().channel();
    }

    @Override
    public void close() {
        for (NetworkConnection session : connectedSessions()) {
            session.close();
        }
        channel.close().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
    }

    @Override
    public boolean running() {
        return channel != null && channel.isActive();
    }

    @Override
    public Collection<NetworkConnection> connectedSessions() {
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
    public void addDisconnectHandler(Consumer<NetworkConnection> handler) {
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
    public void send(NetworkConnection connection, Packet packet) {
        connection.send(packet);
    }
}
