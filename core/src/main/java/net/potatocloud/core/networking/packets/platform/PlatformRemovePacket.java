package net.potatocloud.core.networking.packets.platform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.netty.PacketBuffer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformRemovePacket implements Packet {

    private String platformName;

    @Override
    public int getId() {
        return PacketIds.PLATFORM_REMOVE;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(platformName);
    }

    @Override
    public void read(PacketBuffer buf) {
        platformName = buf.readString();
    }
}
