package net.potatocloud.plugins.hub.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.plugins.hub.MessagesConfig;

import java.util.Comparator;
import java.util.Optional;

@RequiredArgsConstructor
public class HubCommand implements SimpleCommand {

    private final MessagesConfig messagesConfig;
    private final ProxyServer server;

    @Override
    public void execute(Invocation invocation) {
        final CommandSource source = invocation.source();
        final String[] args = invocation.arguments();

        if (!(source instanceof Player player)) {
            return;
        }

        final Service playerService = CloudAPI.getInstance().getPlayerManager().getCloudPlayer(player.getUniqueId()).getConnectedService();
        if (playerService.getServiceGroup().isFallback()) {
            player.sendMessage(this.messagesConfig.get("alreadyOnFallback"));
            return;
        }

        final Optional<RegisteredServer> fallback = this.getBestFallbackServer();
        if (fallback.isEmpty()) {
            player.sendMessage(this.messagesConfig.get("noFallbackFound"));
            return;
        }

        final RegisteredServer registeredServer = fallback.get();
        player.createConnectionRequest(registeredServer).fireAndForget();
        player.sendMessage(this.messagesConfig.get("connect")
                .replaceText(text -> text.match("%service%").replacement(registeredServer.getServerInfo().getName())));
    }

    private Optional<RegisteredServer> getBestFallbackServer() {
        return CloudAPI.getInstance().getServiceManager().getAllServices().stream()
                .filter(service -> service.getServiceGroup().isFallback())
                .filter(service -> service.getStatus() == ServiceStatus.RUNNING)
                .sorted(Comparator.comparingInt(Service::getOnlinePlayerCount))
                .map(service -> server.getServer(service.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
