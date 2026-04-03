package net.potatocloud.node.migration;

import net.potatocloud.api.utils.version.Version;
import net.potatocloud.core.migration.Migration;
import net.potatocloud.core.migration.MigrationManager;
import net.potatocloud.node.config.NodeConfig;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Migration_1_4_4 extends Migration {

    private final Path configFilePath = Path.of(NodeConfig.CONFIG_FILE_NAME);

    public Migration_1_4_4(MigrationManager manager) {
        super("Migration 1.4.4", Version.of(1, 4, 3), Version.of(1, 4, 4));

        manager.registerMigration(this);
    }

    @Override
    public void execute() {
        try {
            backupConfigFile();
            migrateConfigFile();
        } catch (IOException e) {
            System.err.println("Failed to migrate from version 1.4.3 to version 1.4.4");
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

        if (config.isSet("service.service-splitter")) {
            final String oldSplitter = config.getString("service.service-splitter");
            config.set("service.splitter", oldSplitter);
            config.remove("service.service-splitter");
        }

        if (!config.isSet("service.max-services")) {
            config.set("service.max-services", -1);
        }

        if (!config.isSet("service.max-starting-services")) {
            config.set("service.max-starting-services", -1);
        }

        if (!config.isSet("service.kill-timeout")) {
            config.set("service.kill-timeout", 10);
        }

        if (!config.isSet("disable-update-checker")) {
            config.set("disable-update-checker", false);
        }

        if (!config.isSet("debug")) {
            config.set("debug", false);
        }

        config.save();
    }
}
