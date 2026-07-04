package net.potatocloud.network.netty;

import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.nio.NioIoHandler;

public final class NettyUtils {

    private NettyUtils() {
    }

    public static MultithreadEventLoopGroup createEventLoopGroup() {
        final IoHandlerFactory factory = Epoll.isAvailable() ? EpollIoHandler.newFactory() : NioIoHandler.newFactory();
        return new MultiThreadIoEventLoopGroup(factory);
    }
}
