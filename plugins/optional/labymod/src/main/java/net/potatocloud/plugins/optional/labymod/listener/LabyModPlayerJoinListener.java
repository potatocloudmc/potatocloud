package net.potatocloud.plugins.optional.labymod.listener;

import net.labymod.serverapi.server.bukkit.event.LabyModPlayerJoinEvent;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.common.config.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LabyModPlayerJoinListener implements Listener {

    private final Config config;

    public LabyModPlayerJoinListener(Config config) {
        this.config = config;
    }

    @EventHandler
    public void onJoin(LabyModPlayerJoinEvent event) {
        CloudAPI.instance().serviceManager().current().ifPresent(service -> {
           final String notifyMessage = config.get("notify-message").asString()
                   .replace("%service%", service.name())
                   .replace("%group%", service.group().name())
                   .replace("%id%", String.valueOf(service.id()));

           // Send the game mode
           event.labyModPlayer().sendPlayingGameMode(notifyMessage);
        });
    }
}
