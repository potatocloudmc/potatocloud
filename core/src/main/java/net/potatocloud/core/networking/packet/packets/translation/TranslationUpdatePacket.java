package net.potatocloud.core.networking.packet.packets.translation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.core.networking.netty.PacketBuffer;
import net.potatocloud.core.networking.packet.Packet;
import net.potatocloud.core.networking.packet.PacketIds;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationUpdatePacket implements Packet {

    private String key;
    private String value;

    @Override
    public int getId() {
        return PacketIds.TRANSLATION_UPDATE;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(key);
        buf.writeString(value);
    }

    @Override
    public void read(PacketBuffer buf) {
        key = buf.readString();
        value = buf.readString();
    }
}
