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
public class TranslationAddPacket implements Packet {

    private String group;
    private Locale locale;
    private String key;
    private String value;

    @Override
    public int getId() {
        return PacketIds.TRANSLATION_ADD;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(group);
        buf.writeLocale(locale);
        buf.writeString(key);
        buf.writeString(value);

    }

    @Override
    public void read(PacketBuffer buf) {
        group = buf.readString();
        locale = buf.readLocale();
        key = buf.readString();
        value = buf.readString();
    }
}
