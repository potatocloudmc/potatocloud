package net.potatocloud.plugins.optional.sign;

import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.plugins.optional.sign.command.SignCommand;
import net.potatocloud.plugins.optional.sign.listener.SignListener;
import net.potatocloud.plugins.optional.sign.sign.SignService;
import net.potatocloud.plugins.optional.sign.sign.SignStorage;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class SignPlugin extends JavaPlugin {

    private static final String FOLDER = "plugins/potatocloud-sign";

    @Override
    public void onEnable() {
        final SignConfig config = new SignConfig(Path.of(FOLDER, "config.yml"));
        final Config signConfig = new YamlConfig(Path.of(FOLDER, "signs.yml"));
        final MessagesConfig messages = new MessagesConfig(FOLDER);
        config.load();
        signConfig.load();
        messages.load();

        final SignService signService = new SignService(new SignStorage(signConfig, getLogger()), config);
        signService.load();

        getServer().getPluginManager().registerEvents(new SignListener(signService, config, messages), this);

        final PluginCommand command = getCommand("sign");
        command.setPermission(config.permission());
        command.setExecutor(new SignCommand(signService, config, messages));

        getServer().getScheduler().runTaskTimer(this, signService::update, 20L, config.updateInterval());
    }
}
