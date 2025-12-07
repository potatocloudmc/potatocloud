package net.potatocloud.plugins.proxy.maintenance;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.potatocloud.plugins.proxy.Config;
import net.potatocloud.plugins.proxy.MessagesConfig;

@RequiredArgsConstructor
public class LoginListener {

    private final Config config;
    private final MessagesConfig messagesConfig;

    @Subscribe
    public void handle(LoginEvent event) {
        if (!(config.maintenance())) {
            return;
        }

        final Player player = event.getPlayer();
        final String username = player.getUsername();

        if (config.whitelist().contains(username) || player.hasPermission("*")) {
            return;
        }

        event.setResult(ResultedEvent.ComponentResult.denied(messagesConfig.getWithoutPrefix("notWhitelist")));
    }
}
