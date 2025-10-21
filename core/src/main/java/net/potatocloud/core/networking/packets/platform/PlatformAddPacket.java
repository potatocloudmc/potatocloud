package net.potatocloud.core.networking.packets.platform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.netty.PacketBuffer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformAddPacket implements Packet {

    private Platform platform;

    @Override
    public int getId() {
        return PacketIds.PLATFORM_ADD;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writePlatform(platform);
    }

    @Override
    public void read(PacketBuffer buf) {
        platform = buf.readPlatform();
    }
}
