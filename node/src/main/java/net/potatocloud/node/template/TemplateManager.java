package net.potatocloud.node.template;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.potatocloud.node.console.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;

@RequiredArgsConstructor
public class TemplateManager {

    private final Logger logger;
    private final Path templatesFolder;

    public void createTemplate(String templateName) {
        final File templateFolder = templatesFolder.resolve(templateName).toFile();
        if (!templateFolder.exists()) {
            templateFolder.mkdirs();
        }
    }

    @SneakyThrows
    public void copyTemplate(String templateName, Path serviceDirectory) {
        final File sourceFolder = templatesFolder.resolve(templateName).toFile();
        if (!sourceFolder.exists()) {
            logger.error("Template " + templateName + " does not exists!");
            return;
        }
        FileUtils.copyDirectory(sourceFolder, serviceDirectory.toFile());
    }
}
