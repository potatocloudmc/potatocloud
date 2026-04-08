package net.potatocloud.node.module;

import lombok.Getter;
import net.potatocloud.api.module.AbstractModule;
import net.potatocloud.api.module.PotatoModule;
import net.potatocloud.node.console.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

@Getter
public class ModuleLoader {

    private final Map<String, PotatoModule> modules = new HashMap<>();
    private final Logger logger;

    public ModuleLoader(Logger logger) {
        this.logger = logger;
    }

    public void loadModules() {
        Path modulesDir = Path.of("modules");
        try {
            if (!Files.exists(modulesDir)) {
                Files.createDirectories(modulesDir);
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(modulesDir, "*.jar")) {
                for (Path jarFile : stream) {
                    loadModule(jarFile);
                }
            }

            logger.info("Loaded " + modules.size() + " module(s) from modules directory");
        } catch (IOException ex) {
            logger.error("Failed to create modules directory: " + ex.getMessage());
        }
    }

    public void loadModule(Path jarFile) {
        try {
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{jarFile.toUri().toURL()},
                    this.getClass().getClassLoader()
            );

            ServiceLoader<PotatoModule> serviceLoader = ServiceLoader.load(PotatoModule.class, classLoader);

            for (PotatoModule module : serviceLoader) {
                if (module instanceof AbstractModule abstractModule) {
                    abstractModule.setLogger(new NodeModuleLogger(logger));
                }

                modules.put(module.getName(), module);

                Thread.currentThread().setContextClassLoader(classLoader);
                module.onEnable();
            }
        } catch (Exception ex) {
            logger.error("Failed to load module: " + jarFile.getFileName() + " - " + ex.getMessage());
        }
    }

}
