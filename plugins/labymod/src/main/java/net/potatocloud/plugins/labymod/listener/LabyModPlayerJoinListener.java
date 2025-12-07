package net.potatocloud.plugins.labymod.listener;

import lombok.RequiredArgsConstructor;
import net.labymod.serverapi.server.bukkit.event.LabyModPlayerJoinEvent;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.plugins.labymod.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class LabyModPlayerJoinListener implements Listener {

    private final Config config;

    @EventHandler
    public void handle(LabyModPlayerJoinEvent event) {
        final Service service = CloudAPI.getInstance().getServiceManager().getCurrentService();
        if (service == null) {
            return;
        }

        final String notifyMessage = config.notifyMessage()
                .replace("%service%", service.getName())
                .replace("%group%", service.getServiceGroup().getName())
                .replace("%id%", String.valueOf(service.getServiceId()));

        // Send the game mode
        event.labyModPlayer().sendPlayingGameMode(notifyMessage);
    }
}
