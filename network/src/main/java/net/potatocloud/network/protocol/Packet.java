package net.potatocloud.network.protocol;

import net.potatocloud.network.codec.PacketBuffer;

public interface Packet {

    interface Codec<T extends Packet> {
        void encode(T packet, PacketBuffer buf);

        T decode(PacketBuffer buf);
    }
}
