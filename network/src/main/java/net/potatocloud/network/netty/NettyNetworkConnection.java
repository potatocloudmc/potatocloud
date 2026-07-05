package net.potatocloud.network.netty;

import io.netty.channel.Channel;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.protocol.Packet;

import java.util.UUID;

public final class NettyNetworkConnection implements NetworkConnection {

    private final UUID id = UUID.randomUUID();
    private final Channel channel;
    private ConnectionType type = ConnectionType.CONNECTOR;

    public NettyNetworkConnection(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

    @Override
    public ConnectionType type() {
        return type;
    }

    @Override
    public NetworkConnection type(ConnectionType type) {
        this.type = type;
        return this;
    }

    public Channel channel() {
        return channel;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final NettyNetworkConnection other = (NettyNetworkConnection) obj;
        return channel.id().equals(other.channel.id());
    }

    @Override
    public int hashCode() {
        return channel.id().hashCode();
    }

    @Override
    public void close() {
        channel.close().syncUninterruptibly();
    }
}
