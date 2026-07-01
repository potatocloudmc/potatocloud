package net.potatocloud.network.codec.serializers;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.nio.charset.StandardCharsets;

public final class StringSerializer implements TypeSerializer<String> {

    @Override
    public void write(PacketBuffer buffer, String value) {
        if (value == null) {
            buffer.writeVarInt(-1);
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buffer.writeVarInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    @Override
    public String read(PacketBuffer buffer) {
        int length = buffer.readVarInt();
        if (length == -1) {
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
