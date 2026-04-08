package net.potatocloud.node.module;

import lombok.Getter;
import net.potatocloud.api.module.AbstractModule;
import net.potatocloud.api.module.PotatoModule;
import net.potatocloud.api.utils.version.Version;
import net.potatocloud.node.console.Logger;
import org.simpleyaml.configuration.implementation.snakeyaml.lib.Yaml;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

            try (var inputStream = classLoader.getResourceAsStream("module.yml")) {
                if (inputStream == null) return;

                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(inputStream);

                String mainClassPath = (String) data.get("main");
                String name = (String) data.get("name");
                String versionStr = (String) data.get("version");

                Class<?> clazz = Class.forName(mainClassPath, true, classLoader);
                PotatoModule module = (PotatoModule) clazz.getDeclaredConstructor().newInstance();

                if (module instanceof AbstractModule abstractModule) {
                    abstractModule.setName(name);
                    abstractModule.setVersion(Version.fromString(versionStr));
                    abstractModule.setLogger(new NodeModuleLogger(logger));
                }

                Thread.currentThread().setContextClassLoader(classLoader);

                modules.put(name, module);
                module.onEnable();
            }
        } catch (Exception ex) {
            logger.error("Error loading " + jarFile.getFileName() + ": " + ex.getMessage());
        }
    }

}
