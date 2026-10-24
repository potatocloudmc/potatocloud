package net.potatocloud.node.template;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.template.Template;
import net.potatocloud.api.template.TemplateManager;
import net.potatocloud.common.FileUtils;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.template.RequestTemplatesPacket;
import net.potatocloud.network.packets.template.TemplateCreatePacket;
import net.potatocloud.network.packets.template.TemplateDeletePacket;
import net.potatocloud.network.packets.template.TemplatesResponsePacket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TemplateManagerImpl implements TemplateManager {

    private final Logger logger;
    private final Path templatesDirectory;
    private final NetworkServer server;

    public TemplateManagerImpl(Logger logger, Path templatesDirectory, NetworkServer server) {
        this.logger = logger;
        this.templatesDirectory = templatesDirectory;
        this.server = server;

        server.on(RequestTemplatesPacket.class, ctx -> ctx.reply(new TemplatesResponsePacket(templates())));
        server.on(TemplateCreatePacket.class, ctx -> {
            if (createLocal(ctx.packet().template())) {
                server.broadcast().connectors().exclude(ctx.connection()).send(ctx.packet());
            }
        });
        server.on(TemplateDeletePacket.class, ctx -> {
            if (deleteLocal(ctx.packet().template())) {
                server.broadcast().connectors().exclude(ctx.connection()).send(ctx.packet());
            }
        });
    }

    @Override
    public List<Template> templates() {
        if (Files.notExists(templatesDirectory)) {
            return List.of();
        }

        return FileUtils.list(templatesDirectory).stream()
                .filter(Files::isDirectory)
                .map(path -> Template.of(path.getFileName().toString()))
                .toList();
    }

    @Override
    public void create(Template template) {
        if (createLocal(template)) {
            server.broadcast().connectors().send(new TemplateCreatePacket(template));
        }
    }

    @Override
    public void delete(Template template) {
        if (deleteLocal(template)) {
            server.broadcast().connectors().send(new TemplateDeletePacket(template));
        }
    }

    private boolean createLocal(Template template) {
        final Path templateFolder = templatesDirectory.resolve(template.name());
        if (Files.exists(templateFolder)) {
            return false;
        }

        try {
            Files.createDirectories(templateFolder);
            return true;
        } catch (IOException e) {
            logger.error("Failed to create template folder: " + templateFolder);
            return false;
        }
    }

    private boolean deleteLocal(Template template) {
        final Path templateFolder = templatesDirectory.resolve(template.name());
        if (Files.notExists(templateFolder)) {
            return false;
        }

        try {
            FileUtils.deleteDirectory(templateFolder);
            return true;
        } catch (RuntimeException e) {
            logger.error("Failed to delete template folder: " + templateFolder);
            return false;
        }
    }

    public void copyTemplate(Template template, Path serviceDirectory) {
        final Path sourceDirectory = templatesDirectory.resolve(template.name());
        if (Files.notExists(sourceDirectory)) {
            logger.error("Template " + template.name() + " does not exist!");
            return;
        }

        FileUtils.copyDirectory(sourceDirectory, serviceDirectory);
    }
}
