package net.potatocloud.core.networking.packet.packets.logging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.core.networking.netty.PacketBuffer;
import net.potatocloud.core.networking.packet.Packet;
import net.potatocloud.core.networking.packet.PacketIds;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessagePacket implements Packet {

    private String level;
    private String message;

    @Override
    public int getId() {
        return PacketIds.LOG_MESSAGE;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(level);
        buf.writeString(message);
    }

    @Override
    public void read(PacketBuffer buf) {
        level = buf.readString();
        message = buf.readString();
    }
}
