package net.potatocloud.plugins.optional.proxy.maintenance;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.proxy.ProxyPlugin;
import net.potatocloud.plugins.shared.MessagesConfig;

@RequiredArgsConstructor
public class LoginListener {

    private final ProxyPlugin plugin;
    private final Config config;
    private final MessagesConfig messages;

    @Subscribe
    public void handle(LoginEvent event) {
        final boolean isMaintenance = config.get("maintenance").asBoolean();

        if (!(isMaintenance)) {
            return;
        }

        final Player player = event.getPlayer();
        final String username = player.getUsername();

        final String bypassPermission = config.get("maintenance-bypass-permission").asString();
        if (plugin.getWhitelist().contains(username) || player.hasPermission(bypassPermission)) {
            return;
        }

        event.setResult(ResultedEvent.ComponentResult.denied(messages.get("notWhitelist", false)));
    }
}
