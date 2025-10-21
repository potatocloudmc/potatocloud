package net.potatocloud.core.networking.packets.platform;

import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.netty.PacketBuffer;

public class RequestPlatformsPacket implements Packet {

    @Override
    public int getId() {
        return PacketIds.REQUEST_PLATFORMS;
    }

    @Override
    public void write(PacketBuffer buf) {

    }

    @Override
    public void read(PacketBuffer buf) {

    }
}
