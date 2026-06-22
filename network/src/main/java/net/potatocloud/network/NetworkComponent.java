package net.potatocloud.network;

import net.potatocloud.common.Closeable;
import net.potatocloud.network.packet.Packet;
import net.potatocloud.network.packet.PacketHandler;

public interface NetworkComponent extends Closeable {

    <T extends Packet> void on(Class<T> packetClass, PacketHandler<T> context);

}
