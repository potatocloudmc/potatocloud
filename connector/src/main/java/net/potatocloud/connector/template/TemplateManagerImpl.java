package net.potatocloud.connector.template;

import net.potatocloud.api.template.Template;
import net.potatocloud.api.template.TemplateManager;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.packets.template.RequestTemplatesPacket;
import net.potatocloud.network.packets.template.TemplateCreatePacket;
import net.potatocloud.network.packets.template.TemplateDeletePacket;
import net.potatocloud.network.packets.template.TemplatesResponsePacket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TemplateManagerImpl implements TemplateManager {

    private final NetworkClient client;
    private final CopyOnWriteArrayList<Template> templates = new CopyOnWriteArrayList<>();

    public TemplateManagerImpl(NetworkClient client) {
        this.client = client;

        client.on(TemplateCreatePacket.class, ctx -> templates.addIfAbsent(ctx.packet().template()));
        client.on(TemplateDeletePacket.class, ctx -> templates.remove(ctx.packet().template()));

        client.request(new RequestTemplatesPacket(), TemplatesResponsePacket.class).thenAccept(response -> templates.addAll(response.templates()));
    }

    @Override
    public List<Template> templates() {
        return List.copyOf(templates);
    }

    @Override
    public void create(Template template) {
        if (exists(template.name())) {
            return;
        }

        templates.add(template);
        client.send(new TemplateCreatePacket(template));
    }

    @Override
    public void delete(Template template) {
        if (!templates.remove(template)) {
            return;
        }

        client.send(new TemplateDeletePacket(template));
    }
}
