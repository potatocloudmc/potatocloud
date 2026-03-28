package net.potatocloud.node.template;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.potatocloud.core.utils.FileUtils;
import net.potatocloud.node.console.Logger;

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

    @SneakyThrows
    public void copyTemplate(String templateName, Path serviceDirectory) {
        final Path sourceDirectory = templatesDirectory.resolve(templateName);
        if (Files.notExists(sourceDirectory)) {
            logger.error("Template " + templateName + " does not exist!");
            return;
        }

        FileUtils.copyDirectory(sourceDirectory, serviceDirectory);
    }
}
