package net.potatocloud.node.template;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.common.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class TemplateManager {

    private final Logger logger;
    private final Path templatesDirectory;

    public void createTemplate(String templateName) {
        final Path templateFolder = templatesDirectory.resolve(templateName);
        try {
            if (Files.notExists(templateFolder)) {
                Files.createDirectories(templateFolder);
            }
        } catch (IOException e) {
            logger.error("Failed to create template folder: " + templateFolder);
        }
    }

    public void copyTemplate(String templateName, Path serviceDirectory) {
        final Path sourceDirectory = templatesDirectory.resolve(templateName);
        if (Files.notExists(sourceDirectory)) {
            logger.error("Template " + templateName + " does not exist!");
            return;
        }

        FileUtils.copyDirectory(sourceDirectory, serviceDirectory);
    }
}
