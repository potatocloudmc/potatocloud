package net.potatocloud.network;

import net.potatocloud.common.Closeable;
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.protocol.PacketHandler;

public interface NetworkComponent extends Closeable {

    <T extends Packet> void on(Class<T> packetClass, PacketHandler<T> context);

}
