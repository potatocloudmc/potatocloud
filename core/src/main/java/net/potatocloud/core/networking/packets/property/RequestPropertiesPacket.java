package net.potatocloud.core.networking.packets.property;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.netty.PacketBuffer;

@Data
@NoArgsConstructor
public class RequestPropertiesPacket implements Packet {

    @Override
    public int getId() {
        return PacketIds.REQUEST_PROPERTIES;
    }

    @Override
    public void write(PacketBuffer buf) {

    }

    @Override
    public void read(PacketBuffer buf) {

    }
}
