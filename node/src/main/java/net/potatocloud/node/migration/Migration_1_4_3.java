package net.potatocloud.node.migration;

import net.potatocloud.api.property.Property;
import net.potatocloud.api.utils.version.Version;
import net.potatocloud.core.migration.Migration;
import net.potatocloud.core.migration.MigrationManager;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.utils.YamlUtils;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Migration_1_4_3 extends Migration {

    private final Path groupsDirectory;

    public Migration_1_4_3(Path groupsDirectory, MigrationManager manager) {
        super("Migration 1.4.3", Version.of(1, 4, 2), Version.of(1, 4, 3));
        this.groupsDirectory = groupsDirectory;

        manager.registerMigration(this);
    }

    @Override
    public void execute() {
        try {
            backupGroupFiles();
            migrateGroupFiles();
        } catch (IOException e) {
            System.err.println("Failed to migrate from version 1.4.2 to version 1.4.3");
        }
    }

    private void backupGroupFiles() throws IOException {
        final Path backupsDirectory = createBackupsDirectory("groups");

        final List<Path> files = listGroupFiles();

        for (Path path : files) {
            final Path target = backupsDirectory.resolve(path.toFile().getName());
            Files.copy(path, target);
        }
    }

    private void migrateGroupFiles() throws IOException {
        final List<Path> files = listGroupFiles();

        for (Path path : files) {
            final YamlFile config = new YamlFile(path.toFile());
            config.load();

            final String name = config.getString("name");
            final String platformFullName = config.getString("platform");
            final String[] platformParts = platformFullName.split("-", 2);
            final String platform = platformParts[0];
            final String platformVersion = platformParts[1];

            final List<String> templates = config.getStringList("templates");
            final int minOnlineCount = config.getInt("minOnlineCount");
            final int maxOnlineCount = config.getInt("maxOnlineCount");
            final int maxPlayers = config.getInt("maxPlayers");
            final int maxMemory = config.getInt("maxMemory");
            final boolean fallback = config.getBoolean("fallback");
            final boolean staticServer = config.getBoolean("static");
            final int startPriority = config.getInt("startPriority");
            final int startPercentage = config.getInt("startPercentage");
            final String javaCommand = config.getString("javaCommand");
            final List<String> customJvmFlags = config.getStringList("customJvmFlags");
            final Map<String, Property<?>> properties = YamlUtils.getProperties(config);

            final boolean setJvmFlags = config.isSet("customJvmFlags");

            YamlUtils.clear(config);

            config.set("name", name);
            config.set("platform", platform);
            config.set("platform-version", platformVersion);
            config.set("java-command", javaCommand);
            if (setJvmFlags) {
                config.set("jvm-flags", customJvmFlags);
            }

            config.set("max-players", maxPlayers);
            config.set("max-memory", maxMemory);
            config.set("min-online-count", minOnlineCount);
            config.set("max-online-count", maxOnlineCount);

            config.set("static", staticServer);
            config.set("fallback", fallback);
            config.set("start-priority", startPriority);
            config.set("start-percentage", startPercentage);

            config.set("templates", templates);

            if (!properties.isEmpty()) {
                for (Property<?> property : properties.values()) {
                    config.set("properties." + property.getName() + ".value", property.getValue());
                    config.set("properties." + property.getName() + ".default", property.getDefaultValue());
                }
            }

            config.save();
        }
    }

    private List<Path> listGroupFiles() {
        return FileUtils.list(groupsDirectory).stream()
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".yml"))
                .toList();
    }
}