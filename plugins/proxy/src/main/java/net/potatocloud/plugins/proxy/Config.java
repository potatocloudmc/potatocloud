package net.potatocloud.plugins.proxy;

import lombok.SneakyThrows;
import net.potatocloud.plugins.proxy.motd.Motd;
import net.potatocloud.plugins.proxy.tablist.Tablist;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class Config {

    private final File configFile;
    private YamlFile config;

    public Config() {
        this.configFile = new File("plugins/potatocloud-proxy", "config.yml");
    }

    @SneakyThrows
    public void load() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();

            try (InputStream defaultConfigStream = ProxyPlugin.class.getClassLoader().getResourceAsStream("config.yml")) {
                if (defaultConfigStream != null) {
                    Files.copy(defaultConfigStream, configFile.toPath());
                }
            }
        }

        config = YamlFile.loadConfiguration(configFile);
        config.load();
    }

    @SneakyThrows
    public void reload() {
        config = YamlFile.loadConfiguration(configFile);
        config.load();
    }

    public void saveConfig() {
        try {
            this.config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean useTablistBanner() {
        return this.config.getBoolean("useTablistBanner");
    }

    public String getTablistBannerUrl() {
        return this.config.getString("tablistImageURL");
    }

    public boolean useTablist() {
        return this.config.getBoolean("useTablist");
    }

    public boolean useMotd() {
        return this.config.getBoolean("useMotd");
    }

    public List<String> whitelist() {
        return this.config.getStringList("whitelist");
    }

    public void whitelist(List<String> whitelist) {
        this.config.set("whitelist", whitelist);
        this.saveConfig();
    }

    public boolean maintenance() {
        return this.config.getBoolean("maintenance");
    }

    public void maintenance(boolean maintenance) {
        this.config.set("maintenance", maintenance);
        this.saveConfig();
    }


    public Tablist tablist() {
        return new Tablist(this.config.getString("tablist.header"), this.config.getString("tablist.footer"));
    }

    public Motd defaultMotd() {
        return new Motd(this.config.getString("motd.default.firstLine"),
                this.config.getString("motd.default.secondLine"));
    }

    public Motd maintenanceMotd() {
        return new Motd(this.config.getString("motd.maintenance.firstLine"),
                this.config.getString("motd.maintenance.secondLine"),
                this.config.getString("motd.maintenance.version"));
    }


    public String getPermission() {
        return this.config.getString("permission");
    }
}
