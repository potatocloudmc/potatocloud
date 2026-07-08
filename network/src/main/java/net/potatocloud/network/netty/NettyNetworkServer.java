package net.potatocloud.network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.protocol.PacketManager;
import net.potatocloud.network.protocol.PacketRegistry;
import net.potatocloud.network.request.RequestManager;
import net.potatocloud.network.security.SecurityConfig;
import net.potatocloud.network.security.SecurityProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
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

    private final SecurityConfig securityConfig;
    private final SecurityProvider<SslContext> securityProvider;

    private boolean running;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private int port;

    public NettyNetworkServer(SecurityConfig securityConfig) {
        this.requestManager = new RequestManager();
        this.packetManager = new PacketManager(requestManager);
        this.securityConfig = securityConfig;
        this.securityProvider = new NettySecurityProvider(securityConfig);
        PacketRegistry.registerPackets(packetManager);
    }

    @Override
    public void start(String hostname, int port) {
        this.port = port;

        generateCertificates();

        final SslContext sslContext = securityProvider.createServerContext();

        this.bossGroup = NettyUtils.createEventLoopGroup();
        this.workerGroup = NettyUtils.createEventLoopGroup();

        this.channel = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new NettyServerInitializer(packetManager, requestManager, this, sslContext))
                .bind(new InetSocketAddress(hostname, port))
                .syncUninterruptibly()
                .channel();

        this.running = true;
    }

    private void generateCertificates() {
        if (!securityConfig.sslEnabled()) {
            return;
        }

        try {
            Files.createDirectories(securityConfig.securityDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create security directory: " + securityConfig.securityDirectory(), e);
        }

        securityProvider.generate("server");
        securityProvider.generate("client");
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
    public <T extends Packet> void on(Class<T> packetClass, PacketHandler<T> handler) {
        packetManager.on(packetClass, handler);
    }

    @Override
    public void sendTo(NetworkConnection connection, Packet packet) {
        connection.send(packet);
    }

    @Override
    public void onClientDisconnected(Consumer<NetworkConnection> handler) {
        disconnectHandlers.add(handler);
    }

    public void handleDisconnect(NetworkConnection connection) {
        disconnectHandlers.forEach(handler -> handler.accept(connection));
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

    public PacketManager packetManager() {
        return packetManager;
    }

    public RequestManager requestManager() {
        return requestManager;
    }
}