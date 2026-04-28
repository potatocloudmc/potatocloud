package net.potatocloud.core.networking.packet.packets.translation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.core.networking.netty.PacketBuffer;
import net.potatocloud.core.networking.packet.Packet;
import net.potatocloud.core.networking.packet.PacketIds;

import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranslationRemovePacket implements Packet {

    private String key;

    @Override
    public int getId() {
        return PacketIds.TRANSLATION_REMOVE;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(key);
    }

    @Override
    public void read(PacketBuffer buf) {
        key = buf.readString();
    }
}
