package net.potatocloud.core.networking;

import net.potatocloud.common.Closeable;
import net.potatocloud.core.networking.packet.Packet;

import java.util.UUID;

public interface NetworkConnection extends Closeable {

    UUID getId();

    void send(Packet packet);

}
