package net.potatocloud.node.config;

import lombok.Getter;
import lombok.SneakyThrows;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;

@Getter
public class NodeConfig {

    private String prompt = "&7&a%user%&7@cloud ~> ";
    private boolean enableBanner = true;
    private int primaryColorCode = 42;
    private boolean logPlayerConnections = true;

    private int serviceStartPort = 30000;
    private int proxyStartPort = 25565;
    private String splitter = "-";
    private boolean platformAutoUpdate = true;

    private String groupsFolder = "groups";
    private String staticFolder = "services/static";
    private String tempServicesFolder = "services/temp";
    private String templatesFolder = "templates";
    private String platformsFolder = "platforms";
    private String logsFolder = "logs";
    private String dataFolder = "data";

    private String nodeHost = "127.0.0.1";
    private int nodePort = 3434;

    @SneakyThrows
    public NodeConfig() {
        final File file = new File("config.yml");
        final YamlFile yaml = new YamlFile(file);

        if (!file.exists()) {
            file.createNewFile();
            save(yaml);
        }

        yaml.load();

        prompt = yaml.getString("console.prompt", prompt);
        enableBanner = yaml.getBoolean("console.enable-banner", enableBanner);
        primaryColorCode = yaml.getInt("console.primary-color", primaryColorCode);
        logPlayerConnections = yaml.getBoolean("console.log-player-connections", logPlayerConnections);

        serviceStartPort = yaml.getInt("service.service-start-port", serviceStartPort);
        proxyStartPort = yaml.getInt("service.proxy-start-port", proxyStartPort);
        splitter = yaml.getString("service.service-splitter", splitter);
        platformAutoUpdate = yaml.getBoolean("service.auto-update-platforms", platformAutoUpdate);

        groupsFolder = yaml.getString("folders.groups", groupsFolder);
        staticFolder = yaml.getString("folders.static", staticFolder);
        tempServicesFolder = yaml.getString("folders.temp-services", tempServicesFolder);
        templatesFolder = yaml.getString("folders.templates", templatesFolder);
        platformsFolder = yaml.getString("folders.platforms", platformsFolder);
        logsFolder = yaml.getString("folders.logs", logsFolder);
        dataFolder = yaml.getString("folders.data", dataFolder);

        nodeHost = yaml.getString("node.host", nodeHost);
        nodePort = yaml.getInt("node.port", nodePort);
    }

    @SneakyThrows
    private void save(YamlFile yaml) {
        yaml.setCommentFormat(YamlCommentFormat.PRETTY);

        yaml.setComment("console.prompt", "Console prompt text (%user% = current user)");
        yaml.set("console.prompt", prompt);
        yaml.set("console.enable-banner", enableBanner);
        yaml.setComment("console.primary-color", "Primary color code for console messages and prompt (Supported colors: https://www.ditig.com/256-colors-cheat-sheet)");
        yaml.set("console.primary-color", primaryColorCode);
        yaml.set("console.log-player-connections", logPlayerConnections);

        addSpacer(yaml, "service");

        yaml.set("service.service-start-port", serviceStartPort);
        yaml.set("service.proxy-start-port", proxyStartPort);
        yaml.set("service.service-splitter", splitter);
        yaml.setComment("service.auto-update-platforms", "Auto updates platform jars to latest build in the same MC version. Also updates MC version if using 'platform-latest'");
        yaml.set("service.auto-update-platforms", platformAutoUpdate);

        addSpacer(yaml, "folders");

        yaml.set("folders.groups", groupsFolder);
        yaml.set("folders.static", staticFolder);
        yaml.set("folders.temp-services", tempServicesFolder);
        yaml.set("folders.templates", templatesFolder);
        yaml.set("folders.platforms", platformsFolder);
        yaml.set("folders.logs", logsFolder);
        yaml.set("folders.data", dataFolder);

        addSpacer(yaml, "node");

        yaml.set("node.host", nodeHost);
        yaml.set("node.port", nodePort);

        yaml.save();
    }

    private void addSpacer(YamlFile yaml, String key) {
        yaml.setComment(key, "\n");
    }
}
