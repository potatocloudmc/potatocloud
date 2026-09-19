package net.potatocloud.node.template;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.template.Template;
import net.potatocloud.common.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TemplateManager {

    private final Logger logger;
    private final Path templatesDirectory;

    public TemplateManager(Logger logger, Path templatesDirectory) {
        this.logger = logger;
        this.templatesDirectory = templatesDirectory;
    }

    public void createTemplate(Template template) {
        final Path templateFolder = templatesDirectory.resolve(template.name());
        try {
            if (Files.notExists(templateFolder)) {
                Files.createDirectories(templateFolder);
            }
        } catch (IOException e) {
            logger.error("Failed to create template folder: " + templateFolder);
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
