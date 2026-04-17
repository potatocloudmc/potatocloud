package net.potatocloud.node.migration;

import net.potatocloud.api.utils.version.Version;
import net.potatocloud.core.migration.Migration;
import net.potatocloud.core.migration.MigrationManager;
import net.potatocloud.node.config.NodeConfig;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Migration_1_5_0 extends Migration {

    private final Path configFilePath = Path.of(NodeConfig.CONFIG_FILE_NAME);

    public Migration_1_5_0(MigrationManager manager) {
        super("Migration 1.5.0", Version.of(1, 4, 4), Version.of(1, 5, 0));

        manager.registerMigration(this);
    }

    @Override
    public void execute() {
        try {
            backupConfigFile();
            migrateConfigFile();
        } catch (IOException e) {
            System.err.println("Failed to migrate from version 1.4.4 to version 1.5.0");
        }
    }

    private void backupConfigFile() throws IOException {
        final Path backupsDirectory = createBackupsDirectory("config");
        Files.copy(configFilePath, backupsDirectory.resolve("config.yml"));
    }

    private void migrateConfigFile() throws IOException {
        if (Files.notExists(configFilePath)) {
            return;
        }

        final YamlFile config = new YamlFile(configFilePath.toFile());
        config.loadWithComments();

        if (!config.isSet("service.memory-check-enabled")) {
            config.set("service.memory-check-enabled", false);
        }

        if (!config.isSet("service.max-memory")) {
            config.set("service.max-memory", 16384);
        }

        if (!config.isSet("folders.modules")) {
            config.set("folders.modules", "modules");
        }

        config.save();
    }
}