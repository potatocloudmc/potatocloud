package net.potatocloud.network.codec.serializers;

import net.potatocloud.api.template.Template;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

public final class TemplateSerializer implements TypeSerializer<Template> {

    @Override
    public void write(PacketBuffer buffer, Template template) {
        buffer.writeString(template.name());
    }

    @Override
    public Template read(PacketBuffer buffer) {
        return Template.of(buffer.readString());
    }
}
